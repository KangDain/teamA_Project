package mgr;

import bean.TeamMemberBean;
import bean.TeamRoomBean;
import java.sql.*;
import java.util.Vector;

public class ChallengeMgr {

    private DBConnectionMgr pool;

    public ChallengeMgr() {
        pool = DBConnectionMgr.getInstance();
    }

    public Vector<TeamRoomBean> listAllRooms() {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Vector<TeamRoomBean> vlist = new Vector<>();
        try {
            con = pool.getConnection();
            String sql = "SELECT r.room_id, r.owner_id, u.user_name AS owner_name, r.room_name, r.start_date, r.end_date, r.created_at, " +
                         "(SELECT GROUP_CONCAT(u2.user_name SEPARATOR ',') FROM team_member tm JOIN user u2 ON tm.user_id = u2.user_id WHERE tm.room_id = r.room_id) AS member_names " +
                         "FROM team_room r JOIN user u ON r.owner_id = u.user_id ORDER BY r.start_date DESC";
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                TeamRoomBean bean = new TeamRoomBean();
                bean.setRoomId(rs.getInt("room_id"));
                bean.setOwnerId(rs.getInt("owner_id"));
                bean.setOwnerName(rs.getString("owner_name"));
                bean.setRoomName(rs.getString("room_name"));
                bean.setStartDate(rs.getDate("start_date"));
                bean.setEndDate(rs.getDate("end_date"));
                bean.setCreatedAt(rs.getTimestamp("created_at"));
                
                String memberNames = rs.getString("member_names");
                if (memberNames != null && !memberNames.isEmpty()) {
                    for (String name : memberNames.split(",")) {
                        bean.getMembers().add(name);
                    }
                }
                vlist.add(bean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return vlist;
    }

    public boolean insertRoom(TeamRoomBean bean) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean flag = false;
        try {
            con = pool.getConnection();
            con.setAutoCommit(false);
            
            String sql = "INSERT INTO team_room (owner_id, room_name, start_date, end_date) VALUES (?, ?, ?, ?)";
            pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, bean.getOwnerId());
            pstmt.setString(2, bean.getRoomName());
            pstmt.setDate(3, bean.getStartDate());
            pstmt.setDate(4, bean.getEndDate());
            
            if (pstmt.executeUpdate() == 1) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int roomId = rs.getInt(1);
                    String sql2 = "INSERT INTO team_member (room_id, user_id, goal_amount) VALUES (?, ?, ?)";
                    try (PreparedStatement pstmt2 = con.prepareStatement(sql2)) {
                        pstmt2.setInt(1, roomId);
                        pstmt2.setInt(2, bean.getOwnerId());
                        pstmt2.setInt(3, 300000); // Default goal amount for owner
                        pstmt2.executeUpdate();
                    }
                }
                con.commit();
                flag = true;
            }
        } catch (Exception e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) {}
            }
            e.printStackTrace();
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); } catch (SQLException ex) {}
            }
            pool.freeConnection(con, pstmt, rs);
        }
        return flag;
    }

    public boolean joinRoom(TeamMemberBean bean) {
        Connection con = null;
        PreparedStatement pstmt = null;
        boolean flag = false;
        try {
            con = pool.getConnection();
            String sql = "INSERT INTO team_member (room_id, user_id, goal_amount) VALUES (?, ?, ?)";
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, bean.getRoomId());
            pstmt.setInt(2, bean.getUserId());
            pstmt.setInt(3, bean.getGoalAmount());
            if (pstmt.executeUpdate() == 1) flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt);
        }
        return flag;
    }

    public boolean deleteRoom(int roomId, int ownerId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        boolean flag = false;
        try {
            con = pool.getConnection();
            String sql = "DELETE FROM team_room WHERE room_id = ? AND owner_id = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, roomId);
            pstmt.setInt(2, ownerId);
            if (pstmt.executeUpdate() == 1) flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt);
        }
        return flag;
    }
}
