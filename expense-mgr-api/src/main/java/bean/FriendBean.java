package bean;

import java.sql.Timestamp;

public class FriendBean {
    private int friendId;
    private int userId;
    private int friendUserId;
    private String status;
    private Timestamp createdAt;
    private String friendUserName;

    public FriendBean() {}

    public int getFriendId() { return friendId; }
    public void setFriendId(int friendId) { this.friendId = friendId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getFriendUserId() { return friendUserId; }
    public void setFriendUserId(int friendUserId) { this.friendUserId = friendUserId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getFriendUserName() { return friendUserName; }
    public void setFriendUserName(String friendUserName) { this.friendUserName = friendUserName; }
}
