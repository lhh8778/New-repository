package com.campus.system.repository;

import com.campus.system.model.entity.ItemStatus;
import com.campus.system.model.entity.LostItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CampusMate MySQL 持久层实现
 * 完全契合现有 LostItem 实体类结构与 LostAndFoundRepository 接口契约
 */
public class MySqlLostAndFoundRepository implements LostAndFoundRepository {

    private final String url;
    private final String user;
    private final String password;

    public MySqlLostAndFoundRepository() {
        String dbHost = System.getenv().getOrDefault("DB_HOST", "localhost");
        String dbPort = System.getenv().getOrDefault("DB_PORT", "3306");
        String dbName = System.getenv().getOrDefault("DB_NAME", "campusmate_db");
        this.url = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";
        this.user = System.getenv().getOrDefault("DB_USER", "root");
        this.password = System.getenv().getOrDefault("DB_PASSWORD", "root");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException ignored) {}
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    @Override
    public LostItem save(LostItem item) {
        if (item.getId() == null) {
            // 新增插入
            String sql = "INSERT INTO lost_items (title, description, location, publisher_username, status) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, item.getTitle());
                ps.setString(2, item.getDescription());
                ps.setString(3, item.getLocation());
                ps.setString(4, item.getPublisherUsername());
                ps.setString(5, item.getStatus() != null ? item.getStatus().name() : ItemStatus.UNCLAIMED.name());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        item.setId(rs.getLong(1));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("DB Insert Failed: " + e.getMessage(), e);
            }
        } else {
            // 更新认领状态与认领人
            String sql = "UPDATE lost_items SET status = ?, claimant_username = ? WHERE id = ?";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, item.getStatus().name());
                ps.setString(2, item.getClaimantUsername());
                ps.setLong(3, item.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("DB Update Failed: " + e.getMessage(), e);
            }
        }
        return item;
    }

    @Override
    public Optional<LostItem> findById(Long id) {
        String sql = "SELECT id, title, description, location, publisher_username, claimant_username, status FROM lost_items WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB Query Failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<LostItem> findAll() {
        List<LostItem> list = new ArrayList<>();
        String sql = "SELECT id, title, description, location, publisher_username, claimant_username, status FROM lost_items ORDER BY id DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB Query Failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<LostItem> findByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        List<LostItem> list = new ArrayList<>();
        String sql = "SELECT id, title, description, location, publisher_username, claimant_username, status FROM lost_items WHERE LOWER(title) LIKE ? OR LOWER(description) LIKE ? OR LOWER(location) LIKE ? ORDER BY id DESC";
        String pattern = "%" + keyword.toLowerCase().trim() + "%";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB Search Failed: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * 利用现有的 5 参数构造器及 setter 映射数据库行记录
     */
    private LostItem mapResultSet(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String title = rs.getString("title");
        String desc = rs.getString("description");
        String loc = rs.getString("location");
        String pub = rs.getString("publisher_username");
        String claimant = rs.getString("claimant_username");
        String statusStr = rs.getString("status");

        // 调用你的 5 参数构造函数：LostItem(Long, String, String, String, String)
        LostItem item = new LostItem(id, title, desc, loc, pub);

        // 设置可变属性
        if (claimant != null) {
            item.setClaimantUsername(claimant);
        }
        if (statusStr != null) {
            item.setStatus(ItemStatus.valueOf(statusStr));
        }

        return item;
    }
}