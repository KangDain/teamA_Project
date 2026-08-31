package mgr;

import bean.PostBean;
import java.sql.*;
import java.util.Vector;

public class PostMgr {

    private DBConnectionMgr pool;

    public PostMgr() {
        pool = DBConnectionMgr.getInstance();
    }

        public Vector<PostBean> listPost(int userId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Vector<PostBean> vlist = new Vector<>();
        try {
            con = pool.getConnection();
            String sql = "SELECT p.post_id, p.user_id, u.user_name, u.profile_image, p.content, p.image_data, p.like_count, p.created_at, " +
                         "(SELECT COUNT(*) FROM post_like pl WHERE pl.post_id = p.post_id AND pl.user_id = ?) AS is_liked " +
                         "FROM post p JOIN user u ON p.user_id = u.user_id ORDER BY p.created_at DESC";
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                PostBean bean = new PostBean();
                bean.setPostId(rs.getInt("post_id"));
                bean.setUserId(rs.getInt("user_id"));
                bean.setUserName(rs.getString("user_name"));
                bean.setProfileImage(rs.getString("profile_image"));
                bean.setContent(rs.getString("content"));
                bean.setImageData(rs.getString("image_data"));
                bean.setLikeCount(rs.getInt("like_count"));
                bean.setCreatedAt(rs.getTimestamp("created_at"));
                bean.setLiked(rs.getInt("is_liked") > 0);
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
            String sql = "INSERT INTO post (user_id, content, image_data, like_count) VALUES (?, ?, ?, 0)";
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, bean.getUserId());
            pstmt.setString(2, bean.getContent());
            pstmt.setString(3, bean.getImageData());
            if (pstmt.executeUpdate() == 1) flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt);
        }
        return flag;
    }

        public boolean toggleLike(int postId, int userId) {
        Connection con = null;
        PreparedStatement pstmtCheck = null;
        PreparedStatement pstmtLike = null;
        PreparedStatement pstmtCount = null;
        ResultSet rs = null;
        boolean isLikedNow = false;
        try {
            con = pool.getConnection();

            // 1. 이미 좋아요 눌렀는지 확인
            String checkSql = "SELECT COUNT(*) FROM post_like WHERE post_id = ? AND user_id = ?";
            pstmtCheck = con.prepareStatement(checkSql);
            pstmtCheck.setInt(1, postId);
            pstmtCheck.setInt(2, userId);
            rs = pstmtCheck.executeQuery();
            boolean alreadyLiked = false;
            if (rs.next() && rs.getInt(1) > 0) {
                alreadyLiked = true;
            }

            if (alreadyLiked) {
                // 2-a. 이미 눌렀으면 → 취소 (삭제 + 카운트 감소)
                String delSql = "DELETE FROM post_like WHERE post_id = ? AND user_id = ?";
                pstmtLike = con.prepareStatement(delSql);
                pstmtLike.setInt(1, postId);
                pstmtLike.setInt(2, userId);
                pstmtLike.executeUpdate();

                String countSql = "UPDATE post SET like_count = like_count - 1 WHERE post_id = ?";
                pstmtCount = con.prepareStatement(countSql);
                pstmtCount.setInt(1, postId);
                pstmtCount.executeUpdate();

                isLikedNow = false;
            } else {
                // 2-b. 안 눌렀으면 → 등록 (삽입 + 카운트 증가)
                String insSql = "INSERT INTO post_like (post_id, user_id) VALUES (?, ?)";
                pstmtLike = con.prepareStatement(insSql);
                pstmtLike.setInt(1, postId);
                pstmtLike.setInt(2, userId);
                pstmtLike.executeUpdate();

                String countSql = "UPDATE post SET like_count = like_count + 1 WHERE post_id = ?";
                pstmtCount = con.prepareStatement(countSql);
                pstmtCount.setInt(1, postId);
                pstmtCount.executeUpdate();

                isLikedNow = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (pstmtCount != null) try { pstmtCount.close(); } catch (Exception e) {}
            if (pstmtLike != null) try { pstmtLike.close(); } catch (Exception e) {}
            if (rs != null) try { rs.close(); } catch (Exception e) {}
            pool.freeConnection(con, pstmtCheck);
        }
        return isLikedNow;
    }

    public boolean updatePost(int postId, int userId, String newContent, String newImageData) {
        Connection con = null;
        PreparedStatement pstmt = null;
        boolean flag = false;
        try {
            con = pool.getConnection();
            String sql;
            if (newImageData != null) {
                sql = "UPDATE post SET content=?, image_data=? WHERE post_id=? AND user_id=?";
                pstmt = con.prepareStatement(sql);
                pstmt.setString(1, newContent);
                pstmt.setString(2, newImageData);
                pstmt.setInt(3, postId);
                pstmt.setInt(4, userId);
            } else {
                sql = "UPDATE post SET content=? WHERE post_id=? AND user_id=?";
                pstmt = con.prepareStatement(sql);
                pstmt.setString(1, newContent);
                pstmt.setInt(2, postId);
                pstmt.setInt(3, userId);
            }
            if (pstmt.executeUpdate() == 1) flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt);
        }
        return flag;
    }

    public boolean deletePost(int postId, int userId) {
        Connection con = null;
        PreparedStatement pstmtLike = null;
        PreparedStatement pstmt = null;
        boolean flag = false;
        try {
            con = pool.getConnection();
            con.setAutoCommit(false);
            
            // 1. post_like 삭제
            String sqlLike = "DELETE FROM post_like WHERE post_id=?";
            pstmtLike = con.prepareStatement(sqlLike);
            pstmtLike.setInt(1, postId);
            pstmtLike.executeUpdate();
            
            // 2. post 삭제
            String sql = "DELETE FROM post WHERE post_id=? AND user_id=?";
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, postId);
            pstmt.setInt(2, userId);
            if (pstmt.executeUpdate() == 1) flag = true;
            
            if (flag) con.commit();
            else con.rollback();
            
            con.setAutoCommit(true);
        } catch (Exception e) {
            e.printStackTrace();
            try { if (con != null) con.rollback(); } catch (Exception ex) {}
        } finally {
            if (pstmtLike != null) try { pstmtLike.close(); } catch (Exception ex) {}
            pool.freeConnection(con, pstmt);
        }
        return flag;
    }
}
