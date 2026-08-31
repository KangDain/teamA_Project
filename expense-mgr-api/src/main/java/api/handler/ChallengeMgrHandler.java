package api.handler;

import bean.TeamMemberBean;
import bean.TeamRoomBean;
import com.sun.net.httpserver.HttpExchange;
import mgr.ChallengeMgr;
import java.util.Map;

public class ChallengeMgrHandler extends BaseMgrHandler {

    private final ChallengeMgr challengeMgr = new ChallengeMgr();

    @Override
    protected void process(HttpExchange exchange) throws Exception {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if ("GET".equalsIgnoreCase(method)) {
            sendJsonResponse(exchange, 200, challengeMgr.listAllRooms());
        } else if ("POST".equalsIgnoreCase(method) && path.endsWith("/join")) {
            TeamMemberBean bean = parseRequestBody(exchange, TeamMemberBean.class);
            boolean success = challengeMgr.joinRoom(bean);
            sendJsonResponse(exchange, success ? 200 : 400, Map.of("success", success));
        } else if ("POST".equalsIgnoreCase(method)) {
            TeamRoomBean bean = parseRequestBody(exchange, TeamRoomBean.class);
            boolean success = challengeMgr.insertRoom(bean);
            sendJsonResponse(exchange, success ? 201 : 400, Map.of("success", success));
        } else if ("DELETE".equalsIgnoreCase(method)) {
            Map<String, String> params = getQueryParams(exchange);
            int roomId = Integer.parseInt(params.getOrDefault("roomId", "0"));
            int ownerId = Integer.parseInt(params.getOrDefault("ownerId", "0"));
            boolean success = challengeMgr.deleteRoom(roomId, ownerId);
            sendJsonResponse(exchange, success ? 200 : 400, Map.of("success", success));
        } else {
            sendError(exchange, 405, "지원하지 않는 메소드입니다.");
        }
    }
}
