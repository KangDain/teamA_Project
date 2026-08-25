package api.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ProfileMgrHandler implements HttpHandler {
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                
                InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                // 무식하지만 확실하게 문자열 쪼개서 데이터 빼오기
                String userIdStr = body.split("\"userId\"\\s*:\\s*")[1].split(",")[0].trim();
                int userId = Integer.parseInt(userIdStr);
                
                String base64Image = body.split("\"profileImage\"\\s*:\\s*\"")[1].split("\"")[0];

                // TODO: 여기서 DB에 접속해서 해당 userId의 profile_image 컬럼을 base64Image로 업데이트!
                System.out.println("유저 " + userId + "의 프로필 사진 업데이트 완료!");

                String responseJson = "{\"success\": true, \"message\": \"프로필 사진이 성공적으로 변경되었습니다.\"}";
                sendRawResponse(exchange, 200, responseJson);

            } else {
                sendRawResponse(exchange, 405, "{\"message\":\"Method Not Allowed\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendRawResponse(exchange, 500, "{\"message\":\"Internal Server Error\"}");
        }
    }

    // 🌟 순정 자바 응답 발송용 커스텀 도우미 메서드
    private void sendRawResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }
}