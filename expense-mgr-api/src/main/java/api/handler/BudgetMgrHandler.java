package api.handler;

import bean.BudgetBean;
import com.sun.net.httpserver.HttpExchange;
import mgr.BudgetMgr;
import java.util.Map;

public class BudgetMgrHandler extends BaseMgrHandler {

    private final BudgetMgr budgetMgr = new BudgetMgr();

        @Override
        protected void process(HttpExchange exchange) throws Exception {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        Map<String, String> params = getQueryParams(exchange);

        if ("GET".equalsIgnoreCase(method)) {
            int userId = Integer.parseInt(params.getOrDefault("userId", "0"));
            sendJsonResponse(exchange, 200, budgetMgr.listBudget(userId));
        } else if ("POST".equalsIgnoreCase(method) && path.endsWith("/total")) {
            // 월 목표 예산 설정 (있으면 수정, 없으면 생성)
            BudgetBean bean = parseRequestBody(exchange, BudgetBean.class);
            boolean success = budgetMgr.setTotalBudget(bean.getUserId(), bean.getLimitAmount());
            sendJsonResponse(exchange, success ? 200 : 400, Map.of("success", success));
        } else if ("POST".equalsIgnoreCase(method)) {
            BudgetBean bean = parseRequestBody(exchange, BudgetBean.class);
            boolean success = budgetMgr.insertBudget(bean);
            sendJsonResponse(exchange, success ? 201 : 400, Map.of("success", success));
        } else {
            sendError(exchange, 405, "지원하지 않는 메소드입니다.");
        }
    }
}
