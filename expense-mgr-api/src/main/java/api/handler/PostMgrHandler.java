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
            Map<String, String> params = getQueryParams(exchange);
            int userId = Integer.parseInt(params.getOrDefault("userId", "0"));
            sendJsonResponse(exchange, 200, postMgr.listPost(userId));
        } else if ("POST".equalsIgnoreCase(method) && path.endsWith("/like")) {
            String[] parts = path.split("/");
            int postId = Integer.parseInt(parts[3]);
            Map<String, String> params = getQueryParams(exchange);
            int userId = Integer.parseInt(params.getOrDefault("userId", "1"));
            boolean isLiked = postMgr.toggleLike(postId, userId);
            sendJsonResponse(exchange, 200, Map.of("success", true, "isLiked", isLiked));
        } else if ("POST".equalsIgnoreCase(method)) {
            PostBean bean = parseRequestBody(exchange, PostBean.class);
            boolean success = postMgr.insertPost(bean);
            sendJsonResponse(exchange, success ? 201 : 400, Map.of("success", success));
        } else if ("PUT".equalsIgnoreCase(method)) {
            String[] parts = path.split("/");
            if (parts.length >= 4) {
                int postId = Integer.parseInt(parts[3]);
                PostBean bean = parseRequestBody(exchange, PostBean.class);
                boolean success = postMgr.updatePost(postId, bean.getUserId(), bean.getContent(), bean.getImageData());
                sendJsonResponse(exchange, success ? 200 : 400, Map.of("success", success));
            } else {
                sendError(exchange, 400, "Invalid Request");
            }
        } else if ("DELETE".equalsIgnoreCase(method)) {
            String[] parts = path.split("/");
            if (parts.length >= 4) {
                int postId = Integer.parseInt(parts[3]);
                Map<String, String> params = getQueryParams(exchange);
                int userId = Integer.parseInt(params.getOrDefault("userId", "0"));
                boolean success = postMgr.deletePost(postId, userId);
                sendJsonResponse(exchange, success ? 200 : 400, Map.of("success", success));
            } else {
                sendError(exchange, 400, "Invalid Request");
            }
        } else {
            sendError(exchange, 405, "Method Not Allowed");
        }
    }
}
