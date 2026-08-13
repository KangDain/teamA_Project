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
}
