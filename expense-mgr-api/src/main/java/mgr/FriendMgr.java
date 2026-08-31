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

    public Vector<FriendBean> listReceivedRequests(int userId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Vector<FriendBean> vlist = new Vector<>();
        try {
            con = pool.getConnection();
            // 내가 받은 요청이므로 friend_user_id = 나, user_id = 요청자
            String sql = "SELECT f.friend_id, f.user_id, f.friend_user_id, f.status, f.created_at, u.user_name " +
                         "FROM friend f JOIN user u ON f.user_id = u.user_id " +
                         "WHERE f.friend_user_id = ? AND f.status = '요청'";
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

    public boolean acceptFriendRequest(int friendId, int myUserId, int requesterUserId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        boolean flag = false;
        try {
            con = pool.getConnection();
            con.setAutoCommit(false);
            
            // 1. 기존 요청 상태를 '수락'으로 변경
            String sql1 = "UPDATE friend SET status = '수락' WHERE friend_id = ?";
            pstmt = con.prepareStatement(sql1);
            pstmt.setInt(1, friendId);
            int res1 = pstmt.executeUpdate();
            pstmt.close();
            
            // 2. 반대 방향(나 -> 상대방) 데이터도 '수락'으로 INSERT (양방향 연결)
            String sql2 = "INSERT INTO friend (user_id, friend_user_id, status) VALUES (?, ?, '수락')";
            pstmt = con.prepareStatement(sql2);
            pstmt.setInt(1, myUserId);
            pstmt.setInt(2, requesterUserId);
            int res2 = pstmt.executeUpdate();
            
            if (res1 == 1 && res2 == 1) {
                con.commit();
                flag = true;
            } else {
                con.rollback();
            }
        } catch (Exception e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (SQLException ex) {}
            pool.freeConnection(con, pstmt);
        }
        return flag;
    }

    public boolean deleteFriend(int friendId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        boolean flag = false;
        try {
            con = pool.getConnection();
            String sql = "DELETE FROM friend WHERE friend_id = ?";
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

    /**
     * 로그인 ID로 유저를 검색해서 userId를 반환 (친구 추가 시 사용)
     * @return userId, 없으면 -1
     */
    public int findUserIdByLoginId(String loginId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int userId = -1;
        try {
            con = pool.getConnection();
            String sql = "SELECT user_id FROM user WHERE login_id = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, loginId);
            rs = pstmt.executeQuery();
            if (rs.next()) userId = rs.getInt("user_id");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return userId;
    }
}
