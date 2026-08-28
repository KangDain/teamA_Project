package mgr;

import bean.ExpenseBean;
import java.sql.*;
import java.util.Vector;

/**
 * ExpenseMgr - myJava 스타일 지출 관리 매니저 클래스
 */
public class ExpenseMgr {

    private DBConnectionMgr pool;

    public ExpenseMgr() {
        pool = DBConnectionMgr.getInstance();
    }

    public Vector<ExpenseBean> listExpense(int userId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Vector<ExpenseBean> vlist = new Vector<>();
        try {
            con = pool.getConnection();
            String sql = "SELECT e.expense_id, e.user_id, e.medium_id, l.large_name, m.medium_name, e.item_name, e.expense_amount, e.spent_date, e.memo, e.is_fixed " +
                         "FROM expense e " +
                         "JOIN medium_category m ON e.medium_id = m.medium_id " +
                         "JOIN large_category l ON m.large_id = l.large_id " +
                         "WHERE e.user_id = ? ORDER BY e.spent_date DESC";
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                vlist.add(mapExpenseBean(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return vlist;
    }

    public ExpenseBean getExpense(int expenseId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ExpenseBean bean = null;
        try {
            con = pool.getConnection();
            String sql = "SELECT e.expense_id, e.user_id, e.medium_id, l.large_name, m.medium_name, e.item_name, e.expense_amount, e.spent_date, e.memo, e.is_fixed " +
                         "FROM expense e " +
                         "JOIN medium_category m ON e.medium_id = m.medium_id " +
                         "JOIN large_category l ON m.large_id = l.large_id " +
                         "WHERE e.expense_id = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, expenseId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                bean = mapExpenseBean(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return bean;
    }

    public int getTotalExpense(int userId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int total = 0;
        try {
            con = pool.getConnection();
            String sql = "SELECT SUM(expense_amount) FROM expense WHERE user_id = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                total = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return total;
    }

    /**
     * 대분류 이름과 중분류 이름으로 medium_id를 조회합니다.
     * 클라이언트가 이름으로 POST할 때 사용합니다.
     * @return mediumId, 찾지 못하면 -1
     */
    public int getMediumIdByName(String largeName, String mediumName) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int mediumId = -1;
        try {
            con = pool.getConnection();
            String sql = "SELECT m.medium_id FROM medium_category m " +
                         "JOIN large_category l ON m.large_id = l.large_id " +
                         "WHERE l.large_name = ? AND m.medium_name = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, largeName);
            pstmt.setString(2, mediumName);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                mediumId = rs.getInt("medium_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return mediumId;
    }

    public boolean insertExpense(ExpenseBean bean) {
        Connection con = null;
        PreparedStatement pstmt = null;
        boolean flag = false;
        try {
            con = pool.getConnection();
            String sql = "INSERT INTO expense (user_id, medium_id, item_name, expense_amount, spent_date, memo, is_fixed) VALUES (?, ?, ?, ?, ?, ?, ?)";
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, bean.getUserId());
            pstmt.setInt(2, bean.getMediumId());
            pstmt.setString(3, bean.getItemName());
            pstmt.setInt(4, bean.getExpenseAmount());
            pstmt.setDate(5, bean.getSpentDate());
            pstmt.setString(6, bean.getMemo());
            pstmt.setBoolean(7, bean.isFixed());
            if (pstmt.executeUpdate() == 1) flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt);
        }
        return flag;
    }

    public boolean updateExpense(ExpenseBean bean) {
        Connection con = null;
        PreparedStatement pstmt = null;
        boolean flag = false;
        try {
            con = pool.getConnection();
            String sql = "UPDATE expense SET medium_id=?, item_name=?, expense_amount=?, memo=?, is_fixed=? WHERE expense_id=?";
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, bean.getMediumId());
            pstmt.setString(2, bean.getItemName());
            pstmt.setInt(3, bean.getExpenseAmount());
            pstmt.setString(4, bean.getMemo());
            pstmt.setBoolean(5, bean.isFixed());
            pstmt.setInt(6, bean.getExpenseId());
            if (pstmt.executeUpdate() == 1) flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt);
        }
        return flag;
    }

    public boolean deleteExpense(int expenseId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        boolean flag = false;
        try {
            con = pool.getConnection();
            String sql = "DELETE FROM expense WHERE expense_id=?";
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, expenseId);
            if (pstmt.executeUpdate() == 1) flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt);
        }
        return flag;
    }

    private ExpenseBean mapExpenseBean(ResultSet rs) throws SQLException {
        ExpenseBean bean = new ExpenseBean();
        bean.setExpenseId(rs.getInt("expense_id"));
        bean.setUserId(rs.getInt("user_id"));
        bean.setMediumId(rs.getInt("medium_id"));
        bean.setLargeName(rs.getString("large_name"));
        bean.setMediumName(rs.getString("medium_name"));
        bean.setItemName(rs.getString("item_name"));
        bean.setExpenseAmount(rs.getInt("expense_amount"));
        bean.setSpentDate(rs.getDate("spent_date"));
        bean.setMemo(rs.getString("memo"));
        bean.setFixed(rs.getBoolean("is_fixed"));
        return bean;
    }
}
