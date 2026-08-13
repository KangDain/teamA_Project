package api.handler;

import bean.ExpenseBean;
import com.sun.net.httpserver.HttpExchange;
import mgr.ExpenseMgr;
import java.util.Map;
import java.util.Vector;

public class ExpenseMgrHandler extends BaseMgrHandler {

    private final ExpenseMgr expenseMgr = new ExpenseMgr();

    @Override
    protected void process(HttpExchange exchange) throws Exception {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        Map<String, String> params = getQueryParams(exchange);

        if ("GET".equalsIgnoreCase(method) && path.endsWith("/total")) {
            int userId = Integer.parseInt(params.getOrDefault("userId", "0"));
            int total = expenseMgr.getTotalExpense(userId);
            sendJsonResponse(exchange, 200, Map.of("userId", userId, "totalExpense", total));
        } else if ("GET".equalsIgnoreCase(method)) {
            int userId = Integer.parseInt(params.getOrDefault("userId", "0"));
            Vector<ExpenseBean> list = expenseMgr.listExpense(userId);
            sendJsonResponse(exchange, 200, list);
        } else if ("POST".equalsIgnoreCase(method)) {
            ExpenseBean bean = parseRequestBody(exchange, ExpenseBean.class);
            boolean success = expenseMgr.insertExpense(bean);
            sendJsonResponse(exchange, success ? 201 : 400, Map.of("success", success, "message", success ? "지출 등록 성공" : "지출 등록 실패"));
        } else if ("DELETE".equalsIgnoreCase(method)) {
            String[] parts = path.split("/");
            if (parts.length >= 4) {
                int expenseId = Integer.parseInt(parts[3]);
                boolean success = expenseMgr.deleteExpense(expenseId);
                sendJsonResponse(exchange, 200, Map.of("success", success));
            }
        } else {
            sendError(exchange, 405, "지원하지 않는 메소드입니다.");
        }
    }
}
