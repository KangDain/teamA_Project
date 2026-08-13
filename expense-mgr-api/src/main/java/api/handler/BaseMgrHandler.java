package api.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * BaseMgrHandler - myJava Mgr 연동 REST API 공통 추상 핸들러
 */
public abstract class BaseMgrHandler implements HttpHandler {

    protected final ObjectMapper objectMapper;

    public BaseMgrHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // CORS 헤더 설정
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            process(exchange);
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "서버 내부 오류: " + e.getMessage());
        }
    }

    protected abstract void process(HttpExchange exchange) throws Exception;

    protected void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        byte[] bytes = objectMapper.writeValueAsBytes(data);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    protected void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> err = new HashMap<>();
        err.put("success", false);
        err.put("message", message);
        sendJsonResponse(exchange, statusCode, err);
    }

    protected <T> T parseRequestBody(HttpExchange exchange, Class<T> clazz) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return objectMapper.readValue(is, clazz);
        }
    }

    protected Map<String, String> getQueryParams(HttpExchange exchange) {
        Map<String, String> map = new HashMap<>();
        String query = exchange.getRequestURI().getQuery();
        if (query != null && !query.isEmpty()) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length > 1) {
                    map.put(pair[0], pair[1]);
                } else if (pair.length == 1) {
                    map.put(pair[0], "");
                }
            }
        }
        return map;
    }
}
