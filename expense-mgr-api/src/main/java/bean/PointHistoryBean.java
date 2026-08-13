package bean;

import java.sql.Timestamp;

public class PointHistoryBean {
    private int pointId;
    private int userId;
    private String pointType;
    private int pointAmount;
    private Timestamp createdAt;

    public PointHistoryBean() {}

    public int getPointId() { return pointId; }
    public void setPointId(int pointId) { this.pointId = pointId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getPointType() { return pointType; }
    public void setPointType(String pointType) { this.pointType = pointType; }

    public int getPointAmount() { return pointAmount; }
    public void setPointAmount(int pointAmount) { this.pointAmount = pointAmount; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
