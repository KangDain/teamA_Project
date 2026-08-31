package mgr;

import bean.AppSettingBean;
import java.sql.*;
import java.sql.Statement;

public class SettingMgr {

    private DBConnectionMgr pool;

    public SettingMgr() {
        pool = DBConnectionMgr.getInstance();
        // 앱 구동 시 안전하게 컬럼 추가 시도
        try (Connection con = pool.getConnection(); Statement stmt = con.createStatement()) {
            stmt.executeUpdate("ALTER TABLE app_setting ADD COLUMN current_skin VARCHAR(50) DEFAULT 'poorman.png'");
        } catch (Exception e) {}
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
                try {
                    bean.setCurrentSkin(rs.getString("current_skin"));
                } catch(Exception ignored){}
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
            String sql = "INSERT INTO app_setting (user_id, start_day, alert_weekday, alert_threshold, current_skin) VALUES (?, ?, ?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE " +
                         "start_day = COALESCE(VALUES(start_day), start_day), " +
                         "alert_weekday = COALESCE(VALUES(alert_weekday), alert_weekday), " +
                         "alert_threshold = VALUES(alert_threshold), " +
                         "current_skin = COALESCE(VALUES(current_skin), current_skin)";
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, bean.getUserId());
            pstmt.setDate(2, bean.getStartDay() != null ? bean.getStartDay() : new java.sql.Date(System.currentTimeMillis()));
            pstmt.setString(3, bean.getAlertWeekday() != null ? bean.getAlertWeekday() : "");
            pstmt.setInt(4, bean.getAlertThreshold() != 0 ? bean.getAlertThreshold() : 80);
            pstmt.setString(5, bean.getCurrentSkin() != null ? bean.getCurrentSkin() : "poorman.png");
            if (pstmt.executeUpdate() >= 1) flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.freeConnection(con, pstmt);
        }
        return flag;
    }
}
