package api.handler;

import com.sun.net.httpserver.HttpExchange;
import mgr.StoreMgr;
import java.util.Map;

public class StoreMgrHandler extends BaseMgrHandler {

    private final StoreMgr storeMgr = new StoreMgr();

    @Override
    protected void process(HttpExchange exchange) throws Exception {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if ("GET".equalsIgnoreCase(method) && path.endsWith("/items")) {
            sendJsonResponse(exchange, 200, storeMgr.listItems());
        } else if ("POST".equalsIgnoreCase(method) && path.endsWith("/buy")) {
            Map req = parseRequestBody(exchange, Map.class);
            int userId = ((Number) req.get("userId")).intValue();
            int itemId = ((Number) req.get("itemId")).intValue();
            boolean success = storeMgr.buyItem(userId, itemId);
            if (success) {
                sendJsonResponse(exchange, 200, Map.of("success", true, "message", "포인트 상품 구매 성공!"));
            } else {
                sendError(exchange, 400, "포인트가 부족하거나 상품 구매 실패");
            }
        } else {
            sendJsonResponse(exchange, 200, storeMgr.listItems());
        }
    }
}
