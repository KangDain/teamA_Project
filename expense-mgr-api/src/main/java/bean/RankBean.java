package bean;

public class RankBean {
    private int rank;
    private String userName;
    private int goalAmount;
    private int actualAmount;
    private double achievementRate;
    private int score;

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public int getGoalAmount() { return goalAmount; }
    public void setGoalAmount(int goalAmount) { this.goalAmount = goalAmount; }

    public int getActualAmount() { return actualAmount; }
    public void setActualAmount(int actualAmount) { this.actualAmount = actualAmount; }

    public double getAchievementRate() { return achievementRate; }
    public void setAchievementRate(double achievementRate) { this.achievementRate = achievementRate; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}
