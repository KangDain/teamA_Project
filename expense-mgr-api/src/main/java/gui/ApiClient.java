package gui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ApiClient {

    private String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public ApiResponse sendRequest(String method, String endpoint, String bodyJson) {
        String fullUrl = endpoint.startsWith("http://") || endpoint.startsWith("https://")
                ? endpoint
                : baseUrl + (endpoint.startsWith("/") ? endpoint : "/" + endpoint);

        long startTime = System.currentTimeMillis();

        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .timeout(Duration.ofSeconds(10));

            if ("POST".equalsIgnoreCase(method)) {
                String payload = (bodyJson == null || bodyJson.isBlank()) ? "{}" : bodyJson;
                builder.header("Content-Type", "application/json; charset=UTF-8")
                        .POST(HttpRequest.BodyPublishers.ofString(payload));
            } else if ("PUT".equalsIgnoreCase(method)) {
                String payload = (bodyJson == null || bodyJson.isBlank()) ? "{}" : bodyJson;
                builder.header("Content-Type", "application/json; charset=UTF-8")
                        .PUT(HttpRequest.BodyPublishers.ofString(payload));
            } else if ("DELETE".equalsIgnoreCase(method)) {
                builder.DELETE();
            } else { // GET
                builder.GET();
            }

            HttpRequest request = builder.build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            long elapsed = System.currentTimeMillis() - startTime;
            String rawBody = response.body();
            String prettyBody = formatJson(rawBody);

            return new ApiResponse(method, fullUrl, bodyJson, response.statusCode(), rawBody, prettyBody, elapsed, null);
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            return new ApiResponse(method, fullUrl, bodyJson, -1, null, null, elapsed, e.getMessage());
        }
    }

    public String formatJson(String json) {
        if (json == null || json.isBlank()) {
            return "";
        }
        try {
            Object obj = objectMapper.readValue(json, Object.class);
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return json; // return raw if not valid json
        }
    }

    public static class ApiResponse {
        private final String method;
        private final String url;
        private final String requestBody;
        private final int statusCode;
        private final String rawResponseBody;
        private final String prettyResponseBody;
        private final long elapsedMs;
        private final String errorMessage;

        public ApiResponse(String method, String url, String requestBody, int statusCode,
                           String rawResponseBody, String prettyResponseBody, long elapsedMs, String errorMessage) {
            this.method = method;
            this.url = url;
            this.requestBody = requestBody;
            this.statusCode = statusCode;
            this.rawResponseBody = rawResponseBody;
            this.prettyResponseBody = prettyResponseBody;
            this.elapsedMs = elapsedMs;
            this.errorMessage = errorMessage;
        }

        public String getMethod() { return method; }
        public String getUrl() { return url; }
        public String getRequestBody() { return requestBody; }
        public int getStatusCode() { return statusCode; }
        public String getRawResponseBody() { return rawResponseBody; }
        public String getPrettyResponseBody() { return prettyResponseBody; }
        public long getElapsedMs() { return elapsedMs; }
        public String getErrorMessage() { return errorMessage; }
        public boolean isSuccess() { return statusCode >= 200 && statusCode < 300; }
    }
}
