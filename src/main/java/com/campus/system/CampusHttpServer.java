package com.campus.system;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CampusHttpServer {

    private static String getJdbcUrl() {
        String host = System.getenv().getOrDefault("DB_HOST", "mysql");
        String port = System.getenv().getOrDefault("DB_PORT", "3306");
        String db = System.getenv().getOrDefault("DB_NAME", "campusmate_db");
        return "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=utf-8";
    }

    private static Connection getConnection() throws SQLException {
        String user = System.getenv().getOrDefault("DB_USER", "root");
        String password = System.getenv().getOrDefault("DB_PASSWORD", "root");
        return DriverManager.getConnection(getJdbcUrl(), user, password);
    }

    public static void main(String[] args) throws IOException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver not found: " + e.getMessage());
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // 1. 用户认证模块
        server.createContext("/api/auth/register", new RegisterHandler());
        server.createContext("/api/auth/login", new LoginHandler());

        // 2. 失物招领模块
        server.createContext("/api/lost-items", new LostItemsHandler());
        server.createContext("/api/lost-items/claim", new ClaimItemHandler());

        // 3. 教室预约模块
        server.createContext("/api/classrooms", new ClassroomsHandler());
        server.createContext("/api/classrooms/book", new BookClassroomHandler());
        server.createContext("/api/classrooms/cancel", new CancelClassroomHandler());

        // 4. 在线选课系统模块
        server.createContext("/api/courses", new CoursesHandler());
        server.createContext("/api/courses/select", new SelectCourseHandler());
        server.createContext("/api/courses/drop", new DropCourseHandler());

        // 5. 健康检查
        server.createContext("/api/health", exchange -> sendJson(exchange, 200, "{\"status\":\"UP\"}"));

        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        System.out.println("CampusHttpServer 已成功启动，监听端口: 8080");
        server.start();
    }

    // ==================== 1. 用户认证 ====================
    static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            String body = readBody(exchange);
            String username = extractJson(body, "username");
            String password = extractJson(body, "password");
            String realName = extractJson(body, "realName");
            String studentId = extractJson(body, "studentId");
            String department = extractJson(body, "department");
            String email = extractJson(body, "email");
            String phone = extractJson(body, "phone");

            if (username.isEmpty() || password.isEmpty() || studentId.isEmpty()) {
                sendJson(exchange, 400, "{\"code\":400,\"message\":\"用户名、密码和学号不能为空\"}");
                return;
            }

            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false);

                try (PreparedStatement check = conn.prepareStatement("SELECT id FROM users WHERE username = ?")) {
                    check.setString(1, username);
                    if (check.executeQuery().next()) {
                        sendJson(exchange, 400, "{\"code\":400,\"message\":\"该用户名已被占用，请更换用户名！\"}");
                        return;
                    }
                }

                long userId = -1;
                String insertUser = "INSERT INTO users (username, password, email, role, created_at) VALUES (?, ?, ?, 'STUDENT', NOW())";
                try (PreparedStatement stmt = conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, username);
                    stmt.setString(2, password);
                    stmt.setString(3, email.isEmpty() ? username + "@campus.edu.cn" : email);
                    stmt.executeUpdate();
                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        userId = rs.getLong(1);
                    }
                }

                String insertProfile = "INSERT INTO user_profiles (user_id, real_name, student_id, department, phone) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertProfile)) {
                    stmt.setLong(1, userId);
                    stmt.setString(2, realName.isEmpty() ? username : realName);
                    stmt.setString(3, studentId);
                    stmt.setString(4, department.isEmpty() ? "软件工程学院" : department);
                    stmt.setString(5, phone.isEmpty() ? "未填写" : phone);
                    stmt.executeUpdate();
                }

                conn.commit();
                sendJson(exchange, 200, "{\"code\":200,\"message\":\"注册成功\",\"userId\":" + userId + "}");
            } catch (SQLException e) {
                sendJson(exchange, 500, "{\"code\":500,\"message\":\"数据库写入失败: " + e.getMessage() + "\"}");
            }
        }
    }

    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            String body = readBody(exchange);
            String username = extractJson(body, "username");
            String password = extractJson(body, "password");

            String sql = "SELECT u.id, u.username, u.email, u.role, p.real_name, p.student_id, p.department, p.phone " +
                         "FROM users u LEFT JOIN user_profiles p ON u.id = p.user_id " +
                         "WHERE u.username = ? AND u.password = ?";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    long id = rs.getLong("id");
                    String uname = rs.getString("username");
                    String email = rs.getString("email") != null ? rs.getString("email") : "";
                    String role = rs.getString("role");
                    String realName = rs.getString("real_name") != null ? rs.getString("real_name") : uname;
                    String studentId = rs.getString("student_id") != null ? rs.getString("student_id") : "";
                    String department = rs.getString("department") != null ? rs.getString("department") : "";
                    String phone = rs.getString("phone") != null ? rs.getString("phone") : "";

                    String resp = String.format("{\"code\":200,\"message\":\"登录成功\",\"data\":{\"id\":%d,\"username\":\"%s\",\"email\":\"%s\",\"role\":\"%s\",\"profile\":{\"realName\":\"%s\",\"studentId\":\"%s\",\"department\":\"%s\",\"phone\":\"%s\"}}}",
                            id, uname, email, role, realName, studentId, department, phone);
                    sendJson(exchange, 200, resp);
                } else {
                    sendJson(exchange, 401, "{\"code\":401,\"message\":\"用户名不存在或密码错误！\"}");
                }
            } catch (SQLException e) {
                sendJson(exchange, 500, "{\"code\":500,\"message\":\"数据库查询失败\"}");
            }
        }
    }

    // ==================== 2. 失物招领 ====================
    static class LostItemsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            String method = exchange.getRequestMethod();

            if ("GET".equalsIgnoreCase(method)) {
                StringBuilder json = new StringBuilder("[");
                String sql = "SELECT id, title, description, location, status, publisher_username, claimant_username, DATE_FORMAT(created_at, '%Y-%m-%d %H:%i') as fmt_time FROM lost_items ORDER BY id DESC";
                try (Connection conn = getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    boolean first = true;
                    while (rs.next()) {
                        if (!first) json.append(",");
                        json.append(String.format("{\"id\":%d,\"title\":\"%s\",\"desc\":\"%s\",\"location\":\"%s\",\"status\":\"%s\",\"publisher\":\"%s\",\"claimant\":%s,\"time\":\"%s\"}",
                                rs.getLong("id"),
                                escape(rs.getString("title")),
                                escape(rs.getString("description")),
                                escape(rs.getString("location")),
                                "CLAIMED".equalsIgnoreCase(rs.getString("status")) ? "CLAIMED" : "UNCLAIMED",
                                escape(rs.getString("publisher_username")),
                                rs.getString("claimant_username") == null ? "null" : "\"" + escape(rs.getString("claimant_username")) + "\"",
                                rs.getString("fmt_time")));
                        first = false;
                    }
                } catch (SQLException e) {
                    sendJson(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
                    return;
                }
                json.append("]");
                sendJson(exchange, 200, json.toString());
            } else if ("POST".equalsIgnoreCase(method)) {
                String body = readBody(exchange);
                String title = extractJson(body, "title");
                String location = extractJson(body, "location");
                String desc = extractJson(body, "desc");
                String publisher = extractJson(body, "publisher");

                String sql = "INSERT INTO lost_items (title, description, location, publisher_username, status, created_at) VALUES (?, ?, ?, ?, 'UNCLAIMED', NOW())";
                try (Connection conn = getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, title);
                    stmt.setString(2, desc.isEmpty() ? "拾获同学未填写附加描述" : desc);
                    stmt.setString(3, location);
                    stmt.setString(4, publisher.isEmpty() ? "anonymous" : publisher);
                    stmt.executeUpdate();
                    ResultSet rs = stmt.getGeneratedKeys();
                    long newId = rs.next() ? rs.getLong(1) : -1;
                    sendJson(exchange, 200, "{\"code\":200,\"message\":\"发布成功\",\"id\":" + newId + "}");
                } catch (SQLException e) {
                    sendJson(exchange, 500, "{\"code\":500,\"message\":\"" + e.getMessage() + "\"}");
                }
            }
        }
    }

    static class ClaimItemHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }
            String body = readBody(exchange);
            String idStr = extractJson(body, "id");
            String claimant = extractJson(body, "claimant");

            String sql = "UPDATE lost_items SET status = 'CLAIMED', claimant_username = ? WHERE id = ? AND (status != 'CLAIMED' OR status IS NULL)";
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, claimant.isEmpty() ? "认证学生" : claimant);
                stmt.setLong(2, Long.parseLong(idStr));
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    sendJson(exchange, 200, "{\"code\":200,\"message\":\"认领成功\"}");
                } else {
                    sendJson(exchange, 400, "{\"code\":400,\"message\":\"物品已被认领或不存在！\"}");
                }
            } catch (Exception e) {
                sendJson(exchange, 500, "{\"code\":500,\"message\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    // ==================== 3. 教室预约 ====================
    static class ClassroomsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            StringBuilder json = new StringBuilder("[");
            String sql = "SELECT id, room_no, building, capacity, equip, slot, status, booked_by, purpose FROM classrooms ORDER BY id ASC";
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                boolean first = true;
                while (rs.next()) {
                    if (!first) json.append(",");
                    json.append(String.format("{\"id\":%d,\"roomNo\":\"%s\",\"building\":\"%s\",\"capacity\":%d,\"equip\":\"%s\",\"slot\":\"%s\",\"status\":\"%s\",\"bookedBy\":%s,\"purpose\":%s}",
                            rs.getInt("id"),
                            escape(rs.getString("room_no")),
                            escape(rs.getString("building")),
                            rs.getInt("capacity"),
                            escape(rs.getString("equip")),
                            escape(rs.getString("slot")),
                            rs.getString("status"),
                            rs.getString("booked_by") == null ? "null" : "\"" + escape(rs.getString("booked_by")) + "\"",
                            rs.getString("purpose") == null ? "null" : "\"" + escape(rs.getString("purpose")) + "\""));
                    first = false;
                }
            } catch (SQLException e) {
                sendJson(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
                return;
            }
            json.append("]");
            sendJson(exchange, 200, json.toString());
        }
    }

    static class BookClassroomHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }
            String body = readBody(exchange);
            String idStr = extractJson(body, "id");
            String bookedBy = extractJson(body, "bookedBy");
            String purpose = extractJson(body, "purpose");

            String sql = "UPDATE classrooms SET status = 'BOOKED', booked_by = ?, purpose = ? WHERE id = ? AND status = 'FREE'";
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, bookedBy.isEmpty() ? "在线学生" : bookedBy);
                stmt.setString(2, purpose.isEmpty() ? "自习与研讨" : purpose);
                stmt.setInt(3, Integer.parseInt(idStr));
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    sendJson(exchange, 200, "{\"code\":200,\"message\":\"预约成功\"}");
                } else {
                    sendJson(exchange, 400, "{\"code\":400,\"message\":\"该教室已被占用或不存在！\"}");
                }
            } catch (Exception e) {
                sendJson(exchange, 500, "{\"code\":500,\"message\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    static class CancelClassroomHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }
            String body = readBody(exchange);
            String idStr = extractJson(body, "id");

            String sql = "UPDATE classrooms SET status = 'FREE', booked_by = NULL, purpose = NULL WHERE id = ?";
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, Integer.parseInt(idStr));
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    sendJson(exchange, 200, "{\"code\":200,\"message\":\"预约已成功取消\"}");
                } else {
                    sendJson(exchange, 404, "{\"code\":404,\"message\":\"未找到该教室记录\"}");
                }
            } catch (Exception e) {
                sendJson(exchange, 500, "{\"code\":500,\"message\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    // ==================== 4. 在线选课系统 ====================
    static class CoursesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            // 获取当前登录用户名
            String query = exchange.getRequestURI().getQuery();
            String username = "";
            if (query != null && query.contains("username=")) {
                username = query.substring(query.indexOf("username=") + 9);
                if (username.contains("&")) {
                    username = username.substring(0, username.indexOf("&"));
                }
                username = URLDecoder.decode(username, StandardCharsets.UTF_8);
            }

            StringBuilder json = new StringBuilder("[");
            String sql = "SELECT c.id, c.code, c.name, c.type, c.teacher, c.schedule, c.credits, c.enrolled, c.capacity, " +
                         "(CASE WHEN sc.id IS NOT NULL THEN 1 ELSE 0 END) AS is_selected " +
                         "FROM courses c " +
                         "LEFT JOIN student_courses sc ON c.code = sc.course_code AND sc.username = ? " +
                         "ORDER BY c.id ASC";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                boolean first = true;
                while (rs.next()) {
                    if (!first) json.append(",");
                    json.append(String.format(Locale.US, "{\"code\":\"%s\",\"name\":\"%s\",\"type\":\"%s\",\"teacher\":\"%s\",\"schedule\":\"%s\",\"credits\":%.1f,\"enrolled\":%d,\"capacity\":%d,\"selected\":%s}",
                            escape(rs.getString("code")),
                            escape(rs.getString("name")),
                            escape(rs.getString("type")),
                            escape(rs.getString("teacher")),
                            escape(rs.getString("schedule")),
                            rs.getDouble("credits"),
                            rs.getInt("enrolled"),
                            rs.getInt("capacity"),
                            rs.getInt("is_selected") == 1 ? "true" : "false"));
                    first = false;
                }
            } catch (SQLException e) {
                sendJson(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
                return;
            }
            json.append("]");
            sendJson(exchange, 200, json.toString());
        }
    }

    static class SelectCourseHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            String body = readBody(exchange);
            String username = extractJson(body, "username");
            String code = extractJson(body, "code");

            if (username.isEmpty() || code.isEmpty()) {
                sendJson(exchange, 400, "{\"code\":400,\"message\":\"参数不完整\"}");
                return;
            }

            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false);

                // 1. 检查是否重复选课
                try (PreparedStatement check = conn.prepareStatement("SELECT id FROM student_courses WHERE username = ? AND course_code = ?")) {
                    check.setString(1, username);
                    check.setString(2, code);
                    if (check.executeQuery().next()) {
                        sendJson(exchange, 400, "{\"code\":400,\"message\":\"您已选修该课程，不可重复选课！\"}");
                        return;
                    }
                }

                // 2. 检查课容量上限
                try (PreparedStatement capCheck = conn.prepareStatement("SELECT enrolled, capacity FROM courses WHERE code = ? FOR UPDATE")) {
                    capCheck.setString(1, code);
                    ResultSet rs = capCheck.executeQuery();
                    if (!rs.next()) {
                        sendJson(exchange, 404, "{\"code\":404,\"message\":\"课程不存在\"}");
                        return;
                    }
                    if (rs.getInt("enrolled") >= rs.getInt("capacity")) {
                        sendJson(exchange, 400, "{\"code\":400,\"message\":\"选课失败：该课程课容量已满！\"}");
                        return;
                    }
                }

                // 3. 记录选课关系
                try (PreparedStatement insert = conn.prepareStatement("INSERT INTO student_courses (username, course_code) VALUES (?, ?)")) {
                    insert.setString(1, username);
                    insert.setString(2, code);
                    insert.executeUpdate();
                }

                // 4. 课容量计数 +1
                try (PreparedStatement update = conn.prepareStatement("UPDATE courses SET enrolled = enrolled + 1 WHERE code = ?")) {
                    update.setString(1, code);
                    update.executeUpdate();
                }

                conn.commit();
                sendJson(exchange, 200, "{\"code\":200,\"message\":\"选课成功\"}");
            } catch (SQLException e) {
                sendJson(exchange, 500, "{\"code\":500,\"message\":\"数据库操作失败: " + e.getMessage() + "\"}");
            }
        }
    }

    static class DropCourseHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            String body = readBody(exchange);
            String username = extractJson(body, "username");
            String code = extractJson(body, "code");

            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false);

                // 1. 删除选课记录
                int deleted = 0;
                try (PreparedStatement delete = conn.prepareStatement("DELETE FROM student_courses WHERE username = ? AND course_code = ?")) {
                    delete.setString(1, username);
                    delete.setString(2, code);
                    deleted = delete.executeUpdate();
                }

                if (deleted == 0) {
                    sendJson(exchange, 400, "{\"code\":400,\"message\":\"退选失败：您尚未选修该课程！\"}");
                    return;
                }

                // 2. 课容量计数 -1
                try (PreparedStatement update = conn.prepareStatement("UPDATE courses SET enrolled = GREATEST(0, enrolled - 1) WHERE code = ?")) {
                    update.setString(1, code);
                    update.executeUpdate();
                }

                conn.commit();
                sendJson(exchange, 200, "{\"code\":200,\"message\":\"退课成功\"}");
            } catch (SQLException e) {
                sendJson(exchange, 500, "{\"code\":500,\"message\":\"数据库操作失败: " + e.getMessage() + "\"}");
            }
        }
    }

    private static boolean handleCors(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }

    private static void sendJson(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String extractJson(String body, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(body);
        if (matcher.find()) return matcher.group(1);

        Pattern numPattern = Pattern.compile("\"" + key + "\"\\s*:\\s*(\\d+)");
        Matcher numMatcher = numPattern.matcher(body);
        if (numMatcher.find()) return numMatcher.group(1);

        return "";
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("\"", "\\\"").replace("\n", " ");
    }
}