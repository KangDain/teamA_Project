package api.handler;

import bean.RankBean;
import mgr.RankingMgr;
import com.sun.net.httpserver.HttpExchange;
import java.util.List;

public class RankingMgrHandler extends BaseMgrHandler {
    private RankingMgr rankingMgr;

    public RankingMgrHandler() {
        super();
        this.rankingMgr = new RankingMgr();
    }

    @Override
    protected void process(HttpExchange exchange) throws Exception {
        String method = exchange.getRequestMethod();
        if ("GET".equals(method)) {
            List<RankBean> rankings = rankingMgr.getRankings();
            sendJsonResponse(exchange, 200, rankings);
        } else {
            sendError(exchange, 405, "Method Not Allowed");
        }
    }
}
