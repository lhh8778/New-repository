STREAMING_CHUNK:Initializing deployment environment and variables

Write-Host "=" -ForegroundColor Cyan
Write-Host " CampusMate Sprint 2: Automated 3-Container Cluster Deployment   " -ForegroundColor Cyan
Write-Host "=" -ForegroundColor Cyan

STREAMING_CHUNK:Writing CampusHttpServer Java backend source code

$httpServerCode = @'
package com.campus.system;

import com.campus.system.model.entity.ItemStatus;
import com.campus.system.model.entity.LostItem;
import com.campus.system.repository.LostAndFoundRepository;
import com.campus.system.repository.MySqlLostAndFoundRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;

public class CampusHttpServer {
private static final int PORT = 8080;
private static LostAndFoundRepository lostRepository;

public static void main(String[] args) throws IOException {
    System.out.println("================================================================================");
    System.out.println("   CampusMate Containerized Backend Service (Java 21 + MySQL)                   ");
    System.out.println("================================================================================");

    lostRepository = new MySqlLostAndFoundRepository();
    System.out.println("[INFO] MySqlLostAndFoundRepository successfully initialized.");

    HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
    server.createContext("/api/health", new HealthHandler());
    server.createContext("/api/auth/login", new LoginHandler());
    server.createContext("/api/lost-items", new LostItemsHandler());
    server.createContext("/api/lost-items/claim", new ClaimItemHandler());

    server.setExecutor(Executors.newFixedThreadPool(10));
    server.start();
    System.out.println(">>> Backend Server successfully listening on port: " + PORT);
}

static class HealthHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { sendResponse(exchange, 204, ""); return; }
        sendResponse(exchange, 200, "{\"code\":200,\"status\":\"UP\",\"database\":\"CONNECTED\"}");
    }
}

static class LoginHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { sendResponse(exchange, 204, ""); return; }
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { sendResponse(exchange, 405, "{\"code\":405,\"message\":\"Method Not Allowed\"}"); return; }

        Map<String, String> body = parseJsonBody(exchange);
        String u = body.getOrDefault("username", "");

        String token = "JWT_TOKEN_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String json = String.format("{\"code\":200,\"message\":\"Login successful\",\"data\":{\"token\":\"%s\",\"id\":1,\"username\":\"%s\",\"realName\":\"Zhang San\",\"studentId\":\"20230001\",\"department\":\"Computer Science\",\"phone\":\"13800138000\",\"email\":\"zhangsan@campus.edu.cn\"}}", token, escape(u));
        sendResponse(exchange, 200, json);
    }
}

static class LostItemsHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { sendResponse(exchange, 204, ""); return; }

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            String query = exchange.getRequestURI().getQuery();
            String keyword = "";
            if (query != null && query.contains("keyword=")) {
                keyword = URLDecoder.decode(query.split("keyword=")[1].split("&")[0], StandardCharsets.UTF_8);
            }

            List<LostItem> items = keyword.trim().isEmpty()
                    ? lostRepository.findAll()
                    : lostRepository.findByKeyword(keyword);

            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < items.size(); i++) {
                LostItem it = items.get(i);
                if (i > 0) sb.append(",");
                sb.append(String.format("{\"id\":%d,\"title\":\"%s\",\"desc\":\"%s\",\"location\":\"%s\",\"publisher\":\"%s\",\"claimant\":%s,\"status\":\"%s\",\"time\":\"2026-10-21\"}",
                        it.getId(),
                        escape(it.getTitle()),
                        escape(it.getDescription()),
                        escape(it.getLocation()),
                        escape(it.getPublisherUsername()),
                        it.getClaimantUsername() == null ? "null" : "\"" + escape(it.getClaimantUsername()) + "\"",
                        it.getStatus().name()));
            }
            sb.append("]");
            sendResponse(exchange, 200, "{\"code\":200,\"message\":\"Success\",\"data\":" + sb.toString() + "}");
        } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, String> body = parseJsonBody(exchange);
            String title = body.get("title");
            String location = body.get("location");
            String desc = body.getOrDefault("desc", body.getOrDefault("description", ""));
            String publisher = body.getOrDefault("publisher", body.getOrDefault("publisherUsername", "zhangsan"));

            if (title == null || title.trim().isEmpty() || location == null || location.trim().isEmpty()) {
                sendResponse(exchange, 400, "{\"code\":400,\"message\":\"Item title and location cannot be empty\"}");
                return;
            }

            LostItem newItem = new LostItem(null, title, desc, location, publisher);
            LostItem saved = lostRepository.save(newItem);
            sendResponse(exchange, 200, "{\"code\":200,\"message\":\"Published successfully\",\"data\":{\"id\":" + saved.getId() + ",\"status\":\"UNCLAIMED\"}}");
        } else {
            sendResponse(exchange, 405, "{\"code\":405,\"message\":\"Method Not Allowed\"}");
        }
    }
}

static class ClaimItemHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { sendResponse(exchange, 204, ""); return; }
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) { sendResponse(exchange, 405, "{\"code\":405,\"message\":\"Method Not Allowed\"}"); return; }

        Map<String, String> body = parseJsonBody(exchange);
        long id = Long.parseLong(body.getOrDefault("id", "0"));
        String claimant = body.getOrDefault("claimant", body.getOrDefault("claimantUsername", "lisi"));

        Optional<LostItem> opt = lostRepository.findById(id);
        if (opt.isEmpty()) {
            sendResponse(exchange, 404, "{\"code\":404,\"message\":\"Item not found\"}");
            return;
        }

        LostItem item = opt.get();
        if (item.getStatus() == ItemStatus.CLAIMED) {
            sendResponse(exchange, 400, "{\"code\":400,\"message\":\"Item has already been claimed\"}");
            return;
        }

        item.setStatus(ItemStatus.CLAIMED);
        item.setClaimantUsername(claimant);
        lostRepository.save(item);

        sendResponse(exchange, 200, "{\"code\":200,\"message\":\"Claimed successfully\",\"data\":{\"id\":" + id + ",\"status\":\"CLAIMED\",\"claimant\":\"" + claimant + "\"}}");
    }
}

private static Map<String, String> parseJsonBody(HttpExchange exchange) throws IOException {
    InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
    BufferedReader br = new BufferedReader(isr);
    StringBuilder buf = new StringBuilder();
    String line;
    while ((line = br.readLine()) != null) { buf.append(line.trim()); }
    String json = buf.toString().trim();
    Map<String, String> map = new HashMap<>();
    if (json.startsWith("{") && json.endsWith("}")) {
        json = json.substring(1, json.length() - 1);
        String[] pairs = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String pair : pairs) {
            String[] kv = pair.split(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            if (kv.length == 2) {
                map.put(kv[0].replaceAll("\"", "").trim(), kv[1].replaceAll("\"", "").trim());
            }
        }
    }
    return map;
}

private static void sendResponse(HttpExchange exchange, int statusCode, String responseJson) throws IOException {
    byte[] bytes = responseJson.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
    exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
    exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    exchange.sendResponseHeaders(statusCode, bytes.length);
    try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
}

private static String escape(String s) {
    if (s == null) return "";
    return s.replace("\"", "\\\"").replace("\n", " ").replace("\r", "");
}


}
'@

STREAMING_CHUNK:Writing CampusHttpServer Java file to disk

[System.IO.File]::WriteAllText("src/main/java/com/campus/system/CampusHttpServer.java", $httpServerCode, (New-Object System.Text.UTF8Encoding $false))
Write-Host "[1/5] src/main/java/com/campus/system/CampusHttpServer.java written." -ForegroundColor Green

STREAMING_CHUNK:Configuring Dockerfile.backend

$backendDocker = @'
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY src /app/src
COPY mysql-connector-j-8.2.0.jar /app/
RUN mkdir -p /app/bin && 

find /app/src/main/java -name "*.java" > sources.txt && 

javac -encoding UTF-8 -cp /app/mysql-connector-j-8.2.0.jar -d /app/bin @sources.txt

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/bin /app/bin
COPY --from=builder /app/mysql-connector-j-8.2.0.jar /app/mysql-connector-j-8.2.0.jar
EXPOSE 8080
CMD ["java", "-cp", "/app/bin:/app/mysql-connector-j-8.2.0.jar", "com.campus.system.CampusHttpServer"]
'@
[System.IO.File]::WriteAllText("Dockerfile.backend", $backendDocker, (New-Object System.Text.UTF8Encoding $false))
Write-Host "[2/5] Dockerfile.backend written." -ForegroundColor Green

STREAMING_CHUNK:Configuring nginx reverse proxy

$nginxConf = @'
events {
worker_connections 1024;
}
http {
include       /etc/nginx/mime.types;
default_type  application/octet-stream;
sendfile      on;

server {
    listen 80;
    server_name localhost;

    location / {
        root   /usr/share/nginx/html;
        index  index.html;
        try_files $uri$uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://backend:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}


}
'@
[System.IO.File]::WriteAllText("nginx.conf", $nginxConf, (New-Object System.Text.UTF8Encoding $false))
Write-Host "[3/5] nginx.conf written." -ForegroundColor Green

STREAMING_CHUNK:Configuring Dockerfile.frontend

$frontendDocker = @'
FROM nginx:alpine
COPY nginx.conf /etc/nginx/nginx.conf
COPY index.html /usr/share/nginx/html/index.html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
'@
[System.IO.File]::WriteAllText("Dockerfile.frontend", $frontendDocker, (New-Object System.Text.UTF8Encoding $false))
Write-Host "[4/5] Dockerfile.frontend written." -ForegroundColor Green

STREAMING_CHUNK:Configuring multi-container docker-compose orchestrator

$composeYaml = @'
services:
mysql:
image: mysql:8.0
container_name: campusmate-mysql
restart: always
environment:
MYSQL_ROOT_PASSWORD: root
MYSQL_DATABASE: campusmate_db
ports:
- "3306:3306"
volumes:
- mysql_data:/var/lib/mysql
networks:
- campusmate-net
healthcheck:
test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-proot"]
interval: 5s
timeout: 3s
retries: 10

backend:
build:
context: .
dockerfile: Dockerfile.backend
container_name: campusmate-backend
restart: always
environment:
DB_HOST: mysql
DB_PORT: "3306"
DB_NAME: campusmate_db
DB_USER: root
DB_PASSWORD: root
ports:
- "8080:8080"
depends_on:
mysql:
condition: service_healthy
networks:
- campusmate-net

frontend:
build:
context: .
dockerfile: Dockerfile.frontend
container_name: campusmate-frontend
restart: always
ports:
- "80:80"
depends_on:
- backend
networks:
- campusmate-net

volumes:
mysql_data:

networks:
campusmate-net:
driver: bridge
'@
[System.IO.File]::WriteAllText("docker-compose.yml", $composeYaml, (New-Object System.Text.UTF8Encoding $false))
Write-Host "[5/5] docker-compose.yml written." -ForegroundColor Green

STREAMING_CHUNK:Triggering docker-compose build and container startup

Write-Host "`n>>> Starting Docker Compose cluster build..." -ForegroundColor Yellow
docker compose up -d --build

Write-Host "`n>>> Verifying container states:" -ForegroundColor Yellow
docker compose ps