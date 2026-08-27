package mgr;

import bean.BudgetBean;
import java.sql.*;
import java.util.Vector;

public class BudgetMgr {

    private DBConnectionMgr pool;

    public BudgetMgr() {
        pool = DBConnectionMgr.getInstance();
    }

    public Vector<BudgetBean> listBudget(int userId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Vector<BudgetBean> vlist = new Vector<>();
        try {
            con = pool.getConnection();
            String sql = "SELECT * FROM budget WHERE user_id = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                BudgetBean bean = new BudgetBean();
                bean.setBudgetId(rs.getInt("budget_id"));
                bean.setUserId(rs.getInt("user_id"));
                int lId = rs.getInt("large_id");
                bean.setLargeId(rs.wasNull() ? null : lId);
                bean.setLimitAmount(rs.getInt("limit_amount"));
                bean.setBudgetScope(rs.getString("budget_scope"));
                vlist.add(bean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return vlist;
    }

    public boolean insertBudget(BudgetBean bean) {
        Connection con = null;
        PreparedStatement pstmt = null;
        boolean flag = false;
        try {
            con = pool.getConnection();
            String sql = "INSERT INTO budget (user_id, large_id, limit_amount, budget_scope) VALUES (?, ?, ?, ?)";
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, bean.getUserId());
            if (bean.getLargeId() == null) {
                pstmt.setNull(2, Types.INTEGER);
            } else {
                pstmt.setInt(2, bean.getLargeId());
            }
            pstmt.setInt(3, bean.getLimitAmount());
            pstmt.setString(4, bean.getBudgetScope() == null ? "LARGE" : bean.getBudgetScope());
            if (pstmt.executeUpdate() == 1) flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt);
        }
        return flag;
    }

    public boolean deleteBudget(int budgetId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        boolean flag = false;
        try {
            con = pool.getConnection();
            String sql = "DELETE FROM budget WHERE budget_id = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, budgetId);
            if (pstmt.executeUpdate() == 1) flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt);
        }
        return flag;
    }

        public boolean setTotalBudget(int userId, int limitAmount) {
        Connection con = null;
        PreparedStatement pstmtCheck = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean flag = false;
        try {
            con = pool.getConnection();

            // 1. 이 사용자의 TOTAL 예산 행이 이미 있는지 확인
            String checkSql = "SELECT budget_id FROM budget WHERE user_id = ? AND budget_scope = 'TOTAL'";
            pstmtCheck = con.prepareStatement(checkSql);
            pstmtCheck.setInt(1, userId);
            rs = pstmtCheck.executeQuery();

            if (rs.next()) {
                // 2-a. 있으면 → limit_amount UPDATE
                String updateSql = "UPDATE budget SET limit_amount = ? WHERE user_id = ? AND budget_scope = 'TOTAL'";
                pstmt = con.prepareStatement(updateSql);
                pstmt.setInt(1, limitAmount);
                pstmt.setInt(2, userId);
            } else {
                // 2-b. 없으면 → TOTAL 행 INSERT (large_id는 NULL)
                String insertSql = "INSERT INTO budget (user_id, large_id, limit_amount, budget_scope) VALUES (?, NULL, ?, 'TOTAL')";
                pstmt = con.prepareStatement(insertSql);
                pstmt.setInt(1, userId);
                pstmt.setInt(2, limitAmount);
            }
            if (pstmt.executeUpdate() == 1) flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (Exception e) {}
            pool.freeConnection(con, pstmtCheck, rs);
        }
        return flag;
    }
}
