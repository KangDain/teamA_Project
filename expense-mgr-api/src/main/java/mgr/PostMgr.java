package mgr;

import bean.PostBean;
import java.sql.*;
import java.util.Vector;

public class PostMgr {

    private DBConnectionMgr pool;

    public PostMgr() {
        pool = DBConnectionMgr.getInstance();
    }

    public Vector<PostBean> listPost() {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Vector<PostBean> vlist = new Vector<>();
        try {
            con = pool.getConnection();
            String sql = "SELECT * FROM post ORDER BY created_at DESC";
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                PostBean bean = new PostBean();
                bean.setPostId(rs.getInt("post_id"));
                bean.setUserId(rs.getInt("user_id"));
                bean.setContent(rs.getString("content"));
                bean.setLikeCount(rs.getInt("like_count"));
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

    public boolean insertPost(PostBean bean) {
        Connection con = null;
        PreparedStatement pstmt = null;
        boolean flag = false;
        try {
            con = pool.getConnection();
            String sql = "INSERT INTO post (user_id, content, like_count) VALUES (?, ?, 0)";
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, bean.getUserId());
            pstmt.setString(2, bean.getContent());
            if (pstmt.executeUpdate() == 1) flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt);
        }
        return flag;
    }

    public boolean likePost(int postId, int userId) {
        Connection con = null;
        PreparedStatement pstmt1 = null;
        PreparedStatement pstmt2 = null;
        boolean flag = false;
        try {
            con = pool.getConnection();
            String sql1 = "INSERT INTO post_like (post_id, user_id) VALUES (?, ?)";
            pstmt1 = con.prepareStatement(sql1);
            pstmt1.setInt(1, postId);
            pstmt1.setInt(2, userId);
            if (pstmt1.executeUpdate() == 1) {
                String sql2 = "UPDATE post SET like_count = like_count + 1 WHERE post_id = ?";
                pstmt2 = con.prepareStatement(sql2);
                pstmt2.setInt(1, postId);
                pstmt2.executeUpdate();
                flag = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (pstmt2 != null) try { pstmt2.close(); } catch (Exception e) {}
            pool.freeConnection(con, pstmt1);
        }
        return flag;
    }
}
