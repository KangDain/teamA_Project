package bean;

import java.sql.Date;

public class AppSettingBean {
    private int settingId;
    private int userId;
    private Date startDay;
    private String alertWeekday;
    private int alertThreshold;

    private String currentSkin = "poorman.png"; public AppSettingBean() {} public String getCurrentSkin() { return currentSkin; } public void setCurrentSkin(String s) { this.currentSkin = s; }

    public int getSettingId() { return settingId; }
    public void setSettingId(int settingId) { this.settingId = settingId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Date getStartDay() { return startDay; }
    public void setStartDay(Date startDay) { this.startDay = startDay; }

    public String getAlertWeekday() { return alertWeekday; }
    public void setAlertWeekday(String alertWeekday) { this.alertWeekday = alertWeekday; }

    public int getAlertThreshold() { return alertThreshold; }
    public void setAlertThreshold(int alertThreshold) { this.alertThreshold = alertThreshold; }
}
