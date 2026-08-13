package bean;

public class BudgetBean {
    private int budgetId;
    private int userId;
    private Integer largeId;
    private int limitAmount;
    private String budgetScope;

    public BudgetBean() {}

    public int getBudgetId() { return budgetId; }
    public void setBudgetId(int budgetId) { this.budgetId = budgetId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Integer getLargeId() { return largeId; }
    public void setLargeId(Integer largeId) { this.largeId = largeId; }

    public int getLimitAmount() { return limitAmount; }
    public void setLimitAmount(int limitAmount) { this.limitAmount = limitAmount; }

    public String getBudgetScope() { return budgetScope; }
    public void setBudgetScope(String budgetScope) { this.budgetScope = budgetScope; }
}
