package api.handler;

import bean.ExpenseBean;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpExchange;
import mgr.ExpenseMgr;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;

public class ExpenseMgrHandler extends BaseMgrHandler {

    private final ExpenseMgr expenseMgr = new ExpenseMgr();

    @Override
    protected void process(HttpExchange exchange) throws Exception {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        Map<String, String> params = getQueryParams(exchange);

        if ("GET".equalsIgnoreCase(method) && path.endsWith("/total")) {
            // 총 지출 조회
            int userId = Integer.parseInt(params.getOrDefault("userId", "0"));
            int total = expenseMgr.getTotalExpense(userId);
            sendJsonResponse(exchange, 200, Map.of("userId", userId, "totalExpense", total));

        } else if ("GET".equalsIgnoreCase(method)) {
            // 지출 목록 조회 - {"data": [...]} 형태로 래핑해서 반환
            int userId = Integer.parseInt(params.getOrDefault("userId", "0"));
            Vector<ExpenseBean> list = expenseMgr.listExpense(userId);

            // 클라이언트가 기대하는 필드명으로 변환
            List<Map<String, Object>> mapped = list.stream().map(b -> {
                Map<String, Object> m = new HashMap<>();
                m.put("expenseId",      b.getExpenseId());
                m.put("userId",         b.getUserId());
                m.put("mediumId",       b.getMediumId());
                m.put("largeCategory",  b.getLargeName());   // 클라이언트 키와 일치
                m.put("mediumCategory", b.getMediumName());  // 클라이언트 키와 일치
                m.put("smallCategory",  b.getItemName());    // smallCategory = itemName
                m.put("item",           b.getMemo() != null ? b.getMemo() : ""); // 메모를 item으로 노출
                m.put("amount",         b.getExpenseAmount());
                m.put("expenseDate",    b.getSpentDate() != null ? b.getSpentDate().toString() : "");
                m.put("isFixed",        b.isFixed());
                return m;
            }).collect(Collectors.toList());

            sendJsonResponse(exchange, 200, Map.of("data", mapped));

        } else if ("POST".equalsIgnoreCase(method)) {
            // 지출 등록 - 클라이언트가 보내는 largeCategory/mediumCategory/smallCategory/item/amount/expenseDate 처리
            JsonNode body = objectMapper.readTree(exchange.getRequestBody());

            String largeCategory  = body.has("largeCategory")  ? body.get("largeCategory").asText()  : "";
            String mediumCategory = body.has("mediumCategory") ? body.get("mediumCategory").asText() : "";
            String smallCategory  = body.has("smallCategory")  ? body.get("smallCategory").asText()  : "";
            String item           = body.has("item")           ? body.get("item").asText()           : "";
            long amount           = body.has("amount")         ? body.get("amount").asLong()         : 0;
            String expenseDateStr = body.has("expenseDate")    ? body.get("expenseDate").asText()    : "";
            int userId            = body.has("userId")         ? body.get("userId").asInt()          : 0;

            // 카테고리 이름으로 mediumId 조회
            int mediumId = expenseMgr.getMediumIdByName(largeCategory, mediumCategory);
            if (mediumId <= 0) {
                sendError(exchange, 400, "카테고리를 찾을 수 없습니다: " + largeCategory + " > " + mediumCategory);
                return;
            }

            ExpenseBean bean = new ExpenseBean();
            bean.setUserId(userId);
            bean.setMediumId(mediumId);
            bean.setItemName(smallCategory.isEmpty() ? item : smallCategory); // 소분류를 itemName에 저장
            bean.setMemo(item);                                                // 상세 내용은 memo에 저장
            bean.setExpenseAmount((int) amount);
            if (!expenseDateStr.isEmpty()) {
                bean.setSpentDate(Date.valueOf(expenseDateStr));
            }
            bean.setFixed(false);

            boolean success = expenseMgr.insertExpense(bean);
            sendJsonResponse(exchange, success ? 201 : 400,
                Map.of("success", success, "message", success ? "지출 등록 성공" : "지출 등록 실패"));

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
