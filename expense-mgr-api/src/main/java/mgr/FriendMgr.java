package mgr;

import bean.FriendBean;
import java.sql.*;
import java.util.Vector;

public class FriendMgr {

    private DBConnectionMgr pool;

    public FriendMgr() {
        pool = DBConnectionMgr.getInstance();
    }

    public Vector<FriendBean> listAcceptedFriends(int userId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Vector<FriendBean> vlist = new Vector<>();
        try {
            con = pool.getConnection();
            String sql = "SELECT f.friend_id, f.user_id, f.friend_user_id, f.status, f.created_at, u.user_name " +
                         "FROM friend f JOIN user u ON f.friend_user_id = u.user_id " +
                         "WHERE f.user_id = ? AND f.status = '수락'";
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                FriendBean bean = new FriendBean();
                bean.setFriendId(rs.getInt("friend_id"));
                bean.setUserId(rs.getInt("user_id"));
                bean.setFriendUserId(rs.getInt("friend_user_id"));
                bean.setStatus(rs.getString("status"));
                bean.setCreatedAt(rs.getTimestamp("created_at"));
                bean.setFriendUserName(rs.getString("user_name"));
                vlist.add(bean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return vlist;
    }

    public boolean insertFriendRequest(FriendBean bean) {
        Connection con = null;
        PreparedStatement pstmt = null;
        boolean flag = false;
        try {
            con = pool.getConnection();
            String sql = "INSERT INTO friend (user_id, friend_user_id, status) VALUES (?, ?, '요청')";
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, bean.getUserId());
            pstmt.setInt(2, bean.getFriendUserId());
            if (pstmt.executeUpdate() == 1) flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt);
        }
        return flag;
    }

    public boolean acceptFriendRequest(int friendId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        boolean flag = false;
        try {
            con = pool.getConnection();
            String sql = "UPDATE friend SET status = '수락' WHERE friend_id = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, friendId);
            if (pstmt.executeUpdate() == 1) flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt);
        }
        return flag;
    }
}
