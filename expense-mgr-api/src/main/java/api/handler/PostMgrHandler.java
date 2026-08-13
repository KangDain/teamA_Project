package api.handler;

import bean.PostBean;
import com.sun.net.httpserver.HttpExchange;
import mgr.PostMgr;
import java.util.Map;

public class PostMgrHandler extends BaseMgrHandler {

    private final PostMgr postMgr = new PostMgr();

    @Override
    protected void process(HttpExchange exchange) throws Exception {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if ("GET".equalsIgnoreCase(method)) {
            sendJsonResponse(exchange, 200, postMgr.listPost());
        } else if ("POST".equalsIgnoreCase(method) && path.endsWith("/like")) {
            String[] parts = path.split("/");
            int postId = Integer.parseInt(parts[3]);
            Map<String, String> params = getQueryParams(exchange);
            int userId = Integer.parseInt(params.getOrDefault("userId", "1"));
            boolean success = postMgr.likePost(postId, userId);
            sendJsonResponse(exchange, success ? 200 : 400, Map.of("success", success));
        } else if ("POST".equalsIgnoreCase(method)) {
            PostBean bean = parseRequestBody(exchange, PostBean.class);
            boolean success = postMgr.insertPost(bean);
            sendJsonResponse(exchange, success ? 201 : 400, Map.of("success", success));
        } else {
            sendError(exchange, 405, "지원하지 않는 메소드입니다.");
        }
    }
}
