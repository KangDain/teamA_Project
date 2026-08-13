package api.handler;

import bean.UserBean;
import com.sun.net.httpserver.HttpExchange;
import mgr.UserMgr;
import java.util.Map;

public class UserMgrHandler extends BaseMgrHandler {

    private final UserMgr userMgr = new UserMgr();

    @Override
    protected void process(HttpExchange exchange) throws Exception {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if ("POST".equalsIgnoreCase(method) && path.endsWith("/login")) {
            UserBean req = parseRequestBody(exchange, UserBean.class);
            UserBean user = userMgr.login(req.getLoginId(), req.getPassword());
            if (user != null) {
                sendJsonResponse(exchange, 200, user);
            } else {
                sendError(exchange, 401, "아이디 또는 비밀번호가 일치하지 않습니다.");
            }
        } else if ("POST".equalsIgnoreCase(method) && path.endsWith("/register")) {
            UserBean user = parseRequestBody(exchange, UserBean.class);
            if (userMgr.isLoginIdDuplicate(user.getLoginId())) {
                sendError(exchange, 400, "이미 사용 중인 아이디입니다.");
                return;
            }
            boolean success = userMgr.insertUser(user);
            sendJsonResponse(exchange, success ? 201 : 400, Map.of("success", success, "message", success ? "회원가입 성공" : "회원가입 실패"));
        } else if ("GET".equalsIgnoreCase(method) && path.endsWith("/check-duplicate")) {
            Map<String, String> params = getQueryParams(exchange);
            String loginId = params.get("loginId");
            boolean duplicate = userMgr.isLoginIdDuplicate(loginId);
            sendJsonResponse(exchange, 200, Map.of("loginId", loginId, "duplicate", duplicate));
        } else if ("GET".equalsIgnoreCase(method)) {
            String[] parts = path.split("/");
            if (parts.length >= 4) {
                int userId = Integer.parseInt(parts[3]);
                UserBean user = userMgr.getUserById(userId);
                if (user != null) {
                    sendJsonResponse(exchange, 200, user);
                } else {
                    sendError(exchange, 404, "존재하지 않는 회원입니다.");
                }
            }
        } else {
            sendError(exchange, 405, "지원하지 않는 메소드입니다.");
        }
    }
}
