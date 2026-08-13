package api.handler;

import com.sun.net.httpserver.HttpExchange;
import mgr.CategoryMgr;

public class CategoryMgrHandler extends BaseMgrHandler {

    private final CategoryMgr categoryMgr = new CategoryMgr();

    @Override
    protected void process(HttpExchange exchange) throws Exception {
        String path = exchange.getRequestURI().getPath();

        if (path.endsWith("/large")) {
            sendJsonResponse(exchange, 200, categoryMgr.listLargeCategory());
        } else if (path.contains("/medium/by-large/")) {
            String[] parts = path.split("/");
            int largeId = Integer.parseInt(parts[parts.length - 1]);
            sendJsonResponse(exchange, 200, categoryMgr.listMediumCategoryByLarge(largeId));
        } else {
            sendJsonResponse(exchange, 200, categoryMgr.listLargeCategory());
        }
    }
}
