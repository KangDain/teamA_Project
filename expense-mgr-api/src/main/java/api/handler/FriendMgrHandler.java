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
        String path = exchange.getRequestURI().getPath();
        Map<String, String> params = getQueryParams(exchange);

        if ("GET".equalsIgnoreCase(method) && path.endsWith("/search")) {
            // GET /api/friends/search?loginId=xxx  → 아이디로 유저 검색
            String loginId = params.get("loginId");
            if (loginId == null || loginId.isEmpty()) {
                sendError(exchange, 400, "loginId 파라미터가 필요합니다.");
                return;
            }
            int foundUserId = friendMgr.findUserIdByLoginId(loginId);
            if (foundUserId < 0) {
                sendError(exchange, 404, "존재하지 않는 아이디입니다.");
            } else {
                sendJsonResponse(exchange, 200, Map.of("userId", foundUserId, "loginId", loginId));
            }
        } else if ("GET".equalsIgnoreCase(method) && path.endsWith("/requests")) {
            // GET /api/friends/requests?userId=xxx → 받은 친구 요청 목록 조회
            int userId = Integer.parseInt(params.getOrDefault("userId", "0"));
            sendJsonResponse(exchange, 200, friendMgr.listReceivedRequests(userId));
        } else if ("GET".equalsIgnoreCase(method)) {
            // GET /api/friends?userId=xxx  → 수락된 친구 목록 조회
            int userId = Integer.parseInt(params.getOrDefault("userId", "0"));
            sendJsonResponse(exchange, 200, friendMgr.listAcceptedFriends(userId));
        } else if ("POST".equalsIgnoreCase(method) && path.endsWith("/accept")) {
            // POST /api/friends/accept?friendId=xxx&myUserId=xxx&requesterUserId=xxx
            int friendId = Integer.parseInt(params.getOrDefault("friendId", "0"));
            int myUserId = Integer.parseInt(params.getOrDefault("myUserId", "0"));
            int requesterUserId = Integer.parseInt(params.getOrDefault("requesterUserId", "0"));
            boolean success = friendMgr.acceptFriendRequest(friendId, myUserId, requesterUserId);
            sendJsonResponse(exchange, success ? 200 : 400, Map.of("success", success));
        } else if ("POST".equalsIgnoreCase(method)) {
            // POST /api/friends  → 친구 요청 
            FriendBean bean = parseRequestBody(exchange, FriendBean.class);
            boolean success = friendMgr.insertFriendRequest(bean);
            sendJsonResponse(exchange, success ? 201 : 400, Map.of("success", success));
        } else if ("DELETE".equalsIgnoreCase(method)) {
            // DELETE /api/friends?friendId=xxx  → 친구 삭제 (거절도 동일)
            String friendIdStr = params.get("friendId");
            if (friendIdStr == null) {
                sendError(exchange, 400, "friendId 파라미터가 필요합니다.");
                return;
            }
            int friendId = Integer.parseInt(friendIdStr);
            boolean success = friendMgr.deleteFriend(friendId);
            // 만약 양방향 친구(수락된 상태)를 삭제한다면, 반대쪽 레코드도 지워줘야 하지만 
            // 현재 요구사항상 내 쪽에서만 삭제해도 되거나, 더 정교한 삭제 쿼리가 필요할 수 있습니다. 
            // 여기서는 요청 거절 및 단방향 삭제용으로 둡니다.
            sendJsonResponse(exchange, success ? 200 : 400, Map.of("success", success));
        } else {
            sendError(exchange, 405, "지원하지 않는 메소드입니다.");
        }
    }
}
