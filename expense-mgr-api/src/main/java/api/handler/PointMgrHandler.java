package api.handler;

import com.sun.net.httpserver.HttpExchange;
import mgr.PointMgr;
import mgr.UserMgr;
import java.util.Map;

public class PointMgrHandler extends BaseMgrHandler {

    private final PointMgr pointMgr = new PointMgr();
    private final UserMgr userMgr = new UserMgr();

    @Override
    protected void process(HttpExchange exchange) throws Exception {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        Map<String, String> params = getQueryParams(exchange);

        if ("GET".equalsIgnoreCase(method) && path.endsWith("/balance")) {
            int userId = Integer.parseInt(params.getOrDefault("userId", "0"));
            var user = userMgr.getUserById(userId);
            int balance = user != null ? user.getPointBalance() : 0;
            sendJsonResponse(exchange, 200, Map.of("userId", userId, "pointBalance", balance));
        } else if ("POST".equalsIgnoreCase(method) && path.endsWith("/earn")) {
            Map req = parseRequestBody(exchange, Map.class);
            int userId = ((Number) req.get("userId")).intValue();
            String type = (String) req.get("pointType");
            int amount = ((Number) req.get("amount")).intValue();
            boolean success = pointMgr.earnPoint(userId, type, amount);
            sendJsonResponse(exchange, success ? 200 : 400, Map.of("success", success));
        } else if ("GET".equalsIgnoreCase(method) && path.endsWith("/history")) {
            int userId = Integer.parseInt(params.getOrDefault("userId", "0"));
            sendJsonResponse(exchange, 200, pointMgr.listHistory(userId));
        } else {
            sendError(exchange, 405, "지원하지 않는 메소드입니다.");
        }
    }
}
