package bean;

import java.sql.Timestamp;

public class TeamMemberBean {
    private int memberId;
    private int roomId;
    private int userId;
    private int goalAmount;
    private Timestamp joinedAt;
    private String userName;

    public TeamMemberBean() {}

    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getGoalAmount() { return goalAmount; }
    public void setGoalAmount(int goalAmount) { this.goalAmount = goalAmount; }

    public Timestamp getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Timestamp joinedAt) { this.joinedAt = joinedAt; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
}
