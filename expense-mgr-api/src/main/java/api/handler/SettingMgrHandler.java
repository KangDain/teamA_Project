package api.handler;

import bean.AppSettingBean;
import com.sun.net.httpserver.HttpExchange;
import mgr.SettingMgr;
import java.util.Map;

public class SettingMgrHandler extends BaseMgrHandler {

    private final SettingMgr settingMgr = new SettingMgr();

    @Override
    protected void process(HttpExchange exchange) throws Exception {
        String method = exchange.getRequestMethod();
        Map<String, String> params = getQueryParams(exchange);

        if ("GET".equalsIgnoreCase(method)) {
            int userId = Integer.parseInt(params.getOrDefault("userId", "0"));
            AppSettingBean setting = settingMgr.getSetting(userId);
            if (setting != null) {
                sendJsonResponse(exchange, 200, setting);
            } else {
                sendError(exchange, 404, "설정 정보가 없습니다.");
            }
        } else if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
            AppSettingBean bean = parseRequestBody(exchange, AppSettingBean.class);
            boolean success = settingMgr.insertOrUpdateSetting(bean);
            sendJsonResponse(exchange, success ? 200 : 400, Map.of("success", success));
        } else {
            sendError(exchange, 405, "지원하지 않는 메소드입니다.");
        }
    }
}
