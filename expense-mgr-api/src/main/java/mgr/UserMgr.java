package mgr;

import bean.UserBean;
import java.sql.*;
import java.util.Vector;

/**
 * UserMgr - myJava 스타일 회원 관리 매니저 클래스
 */
public class UserMgr {

    private DBConnectionMgr pool;

    public UserMgr() {
        pool = DBConnectionMgr.getInstance();
    }

    public UserBean login(String loginId, String password) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        UserBean bean = null;
        try {
            con = pool.getConnection();
            String sql = "SELECT * FROM user WHERE login_id = ? AND password = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, loginId);
            pstmt.setString(2, password);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                bean = mapUserBean(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return bean;
    }

    public UserBean getUserById(int userId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        UserBean bean = null;
        try {
            con = pool.getConnection();
            String sql = "SELECT * FROM user WHERE user_id = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                bean = mapUserBean(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return bean;
    }

    public boolean isLoginIdDuplicate(String loginId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean flag = false;
        try {
            con = pool.getConnection();
            String sql = "SELECT COUNT(*) FROM user WHERE login_id = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, loginId);
            rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                flag = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return flag;
    }

    public boolean insertUser(UserBean bean) {
        Connection con = null;
        PreparedStatement pstmt = null;
        boolean flag = false;
        try {
            con = pool.getConnection();
            String sql = "INSERT INTO user (login_id, password, user_name, birth_date, gender, phone, job, address, income, point_balance) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0)";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, bean.getLoginId());
            pstmt.setString(2, bean.getPassword());
            pstmt.setString(3, bean.getUserName());
            pstmt.setDate(4, bean.getBirthDate());
            pstmt.setString(5, bean.getGender());
            pstmt.setString(6, bean.getPhone());
            pstmt.setString(7, bean.getJob());
            pstmt.setString(8, bean.getAddress());
            pstmt.setInt(9, bean.getIncome());
            if (pstmt.executeUpdate() == 1) flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt);
        }
        return flag;
    }

    public boolean updateUserInfo(UserBean bean) {
        Connection con = null;
        PreparedStatement pstmt = null;
        boolean flag = false;
        try {
            con = pool.getConnection();
            String sql = "UPDATE user SET user_name=?, phone=?, job=?, address=?, income=? WHERE user_id=?";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, bean.getUserName());
            pstmt.setString(2, bean.getPhone());
            pstmt.setString(3, bean.getJob());
            pstmt.setString(4, bean.getAddress());
            pstmt.setInt(5, bean.getIncome());
            pstmt.setInt(6, bean.getUserId());
            if (pstmt.executeUpdate() == 1) flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt);
        }
        return flag;
    }

    public boolean updatePassword(int userId, String newPassword) {
        Connection con = null;
        PreparedStatement pstmt = null;
        boolean flag = false;
        try {
            con = pool.getConnection();
            String sql = "UPDATE user SET password=? WHERE user_id=?";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, newPassword);
            pstmt.setInt(2, userId);
            if (pstmt.executeUpdate() == 1) flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt);
        }
        return flag;
    }

    public boolean updatePointBalance(int userId, int newBalance) {
        Connection con = null;
        PreparedStatement pstmt = null;
        boolean flag = false;
        try {
            con = pool.getConnection();
            String sql = "UPDATE user SET point_balance=? WHERE user_id=?";
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, newBalance);
            pstmt.setInt(2, userId);
            if (pstmt.executeUpdate() == 1) flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt);
        }
        return flag;
    }

    private UserBean mapUserBean(ResultSet rs) throws SQLException {
        UserBean bean = new UserBean();
        bean.setUserId(rs.getInt("user_id"));
        bean.setLoginId(rs.getString("login_id"));
        bean.setPassword(rs.getString("password"));
        bean.setUserName(rs.getString("user_name"));
        bean.setBirthDate(rs.getDate("birth_date"));
        bean.setGender(rs.getString("gender"));
        bean.setPhone(rs.getString("phone"));
        bean.setJob(rs.getString("job"));
        bean.setAddress(rs.getString("address"));
        bean.setIncome(rs.getInt("income"));
        bean.setPointBalance(rs.getInt("point_balance"));
        return bean;
    }
}
