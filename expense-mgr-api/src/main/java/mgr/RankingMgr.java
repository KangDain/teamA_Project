package mgr;

import bean.RankBean;
import java.sql.*;
import java.util.*;

public class RankingMgr {
    private DBConnectionMgr pool;

    public RankingMgr() {
        pool = DBConnectionMgr.getInstance();
    }

    public List<RankBean> getRankings() {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<RankBean> list = new ArrayList<>();

        try {
            con = pool.getConnection();
            String sql = "SELECT u.user_id, u.user_name, u.point_balance, " +
                         "(SELECT limit_amount FROM budget WHERE user_id = u.user_id AND budget_scope = 'TOTAL' LIMIT 1) AS goal_amount, " +
                         "(SELECT COALESCE(SUM(expense_amount), 0) FROM expense WHERE user_id = u.user_id AND YEAR(spent_date) = YEAR('2026-08-30') AND MONTH(spent_date) = MONTH('2026-08-30') /* Mocked date for presentation */) AS actual_amount " +
                         "FROM user u";
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                String userName = rs.getString("user_name");
                int pointBalance = rs.getInt("point_balance");
                int goalAmount = rs.getInt("goal_amount");
                int actualAmount = rs.getInt("actual_amount");

                if (goalAmount <= 0) goalAmount = 1000000;

                double achievementRate = (double) actualAmount / goalAmount * 100.0;
                // 랭킹 판정: 쓴 돈이 적은 사람 우선 (기본 점수 100만에서 쓴 돈에 비례해 감점)
                // 포인트 비중을 기존보다 낮춤 (1포인트 = 10원 가치)
                int score = 1000000 - (actualAmount / 10) + pointBalance;

                RankBean bean = new RankBean();
                bean.setUserName(userName);
                bean.setGoalAmount(goalAmount);
                bean.setActualAmount(actualAmount);
                bean.setAchievementRate(Math.round(achievementRate * 10.0) / 10.0);
                bean.setScore(score);
                list.add(bean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }

        list.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));

        for (int i = 0; i < list.size(); i++) {
            list.get(i).setRank(i + 1);
        }

        return list;
    }
}

