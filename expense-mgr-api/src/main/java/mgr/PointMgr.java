package mgr;

import bean.PointHistoryBean;
import java.sql.*;
import java.util.Vector;

public class PointMgr {

    private DBConnectionMgr pool;

    public PointMgr() {
        pool = DBConnectionMgr.getInstance();
    }

    public boolean earnPoint(int userId, String pointType, int amount) {
        Connection con = null;
        PreparedStatement pstmt1 = null;
        PreparedStatement pstmt2 = null;
        boolean flag = false;
        try {
            con = pool.getConnection();
            String sql1 = "INSERT INTO point_history (user_id, point_type, point_amount) VALUES (?, ?, ?)";
            pstmt1 = con.prepareStatement(sql1);
            pstmt1.setInt(1, userId);
            pstmt1.setString(2, pointType);
            pstmt1.setInt(3, amount);
            pstmt1.executeUpdate();

            String sql2 = "UPDATE user SET point_balance = point_balance + ? WHERE user_id = ?";
            pstmt2 = con.prepareStatement(sql2);
            pstmt2.setInt(1, amount);
            pstmt2.setInt(2, userId);
            if (pstmt2.executeUpdate() == 1) flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (pstmt2 != null) try { pstmt2.close(); } catch (Exception e) {}
            pool.freeConnection(con, pstmt1);
        }
        return flag;
    }

    public boolean spendPoint(int userId, String pointType, int amount) {
        Connection con = null;
        PreparedStatement pstmt1 = null;
        PreparedStatement pstmt2 = null;
        boolean flag = false;
        try {
            con = pool.getConnection();
            String sql1 = "INSERT INTO point_history (user_id, point_type, point_amount) VALUES (?, ?, ?)";
            pstmt1 = con.prepareStatement(sql1);
            pstmt1.setInt(1, userId);
            pstmt1.setString(2, pointType);
            pstmt1.setInt(3, -amount);
            pstmt1.executeUpdate();

            String sql2 = "UPDATE user SET point_balance = point_balance - ? WHERE user_id = ? AND point_balance >= ?";
            pstmt2 = con.prepareStatement(sql2);
            pstmt2.setInt(1, amount);
            pstmt2.setInt(2, userId);
            pstmt2.setInt(3, amount);
            if (pstmt2.executeUpdate() == 1) flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (pstmt2 != null) try { pstmt2.close(); } catch (Exception e) {}
            pool.freeConnection(con, pstmt1);
        }
        return flag;
    }

    public Vector<PointHistoryBean> listHistory(int userId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Vector<PointHistoryBean> vlist = new Vector<>();
        try {
            con = pool.getConnection();
            String sql = "SELECT * FROM point_history WHERE user_id = ? ORDER BY created_at DESC";
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                PointHistoryBean bean = new PointHistoryBean();
                bean.setPointId(rs.getInt("point_id"));
                bean.setUserId(rs.getInt("user_id"));
                bean.setPointType(rs.getString("point_type"));
                bean.setPointAmount(rs.getInt("point_amount"));
                bean.setCreatedAt(rs.getTimestamp("created_at"));
                vlist.add(bean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return vlist;
    }
}
