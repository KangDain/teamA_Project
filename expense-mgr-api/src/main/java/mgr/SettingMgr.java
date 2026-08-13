package mgr;

import bean.AppSettingBean;
import java.sql.*;

public class SettingMgr {

    private DBConnectionMgr pool;

    public SettingMgr() {
        pool = DBConnectionMgr.getInstance();
    }

    public AppSettingBean getSetting(int userId) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        AppSettingBean bean = null;
        try {
            con = pool.getConnection();
            String sql = "SELECT * FROM app_setting WHERE user_id = ?";
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                bean = new AppSettingBean();
                bean.setSettingId(rs.getInt("setting_id"));
                bean.setUserId(rs.getInt("user_id"));
                bean.setStartDay(rs.getDate("start_day"));
                bean.setAlertWeekday(rs.getString("alert_weekday"));
                bean.setAlertThreshold(rs.getInt("alert_threshold"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt, rs);
        }
        return bean;
    }

    public boolean insertOrUpdateSetting(AppSettingBean bean) {
        Connection con = null;
        PreparedStatement pstmt = null;
        boolean flag = false;
        try {
            con = pool.getConnection();
            String sql = "INSERT INTO app_setting (user_id, start_day, alert_weekday, alert_threshold) VALUES (?, ?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE start_day=VALUES(start_day), alert_weekday=VALUES(alert_weekday), alert_threshold=VALUES(alert_threshold)";
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, bean.getUserId());
            pstmt.setDate(2, bean.getStartDay());
            pstmt.setString(3, bean.getAlertWeekday());
            pstmt.setInt(4, bean.getAlertThreshold());
            if (pstmt.executeUpdate() >= 1) flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt);
        }
        return flag;
    }
}
