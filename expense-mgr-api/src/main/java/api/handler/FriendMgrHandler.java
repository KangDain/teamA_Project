package api.handler;

import bean.FriendBean;
import com.sun.net.httpserver.HttpExchange;
import mgr.FriendMgr;
import java.util.Map;

public class FriendMgrHandler extends BaseMgrHandler {

    private final FriendMgr friendMgr = new FriendMgr();

    @Override
    protected void process(HttpExchange exchange) throws Exception {
        String method = exchange.getRequestMethod();
        Map<String, String> params = getQueryParams(exchange);

        if ("GET".equalsIgnoreCase(method)) {
            int userId = Integer.parseInt(params.getOrDefault("userId", "0"));
            sendJsonResponse(exchange, 200, friendMgr.listAcceptedFriends(userId));
        } else if ("POST".equalsIgnoreCase(method)) {
            FriendBean bean = parseRequestBody(exchange, FriendBean.class);
            boolean success = friendMgr.insertFriendRequest(bean);
            sendJsonResponse(exchange, success ? 201 : 400, Map.of("success", success));
        } else {
            sendError(exchange, 405, "지원하지 않는 메소드입니다.");
        }
    }
}
