package bean;

import java.sql.Date;

/**
 * ExpenseBean - myJava 스타일 지출 객체
 */
public class ExpenseBean {
    private int expenseId;
    private int userId;
    private int mediumId;
    private String itemName;
    private int expenseAmount;
    private Date spentDate;
    private String memo;
    private boolean fixed;
    private String largeName;
    private String mediumName;

    public ExpenseBean() {}

    public int getExpenseId() { return expenseId; }
    public void setExpenseId(int expenseId) { this.expenseId = expenseId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getMediumId() { return mediumId; }
    public void setMediumId(int mediumId) { this.mediumId = mediumId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public int getExpenseAmount() { return expenseAmount; }
    public void setExpenseAmount(int expenseAmount) { this.expenseAmount = expenseAmount; }

    public Date getSpentDate() { return spentDate; }
    public void setSpentDate(Date spentDate) { this.spentDate = spentDate; }

    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }

    public boolean isFixed() { return fixed; }
    public void setFixed(boolean fixed) { this.fixed = fixed; }

    public String getLargeName() { return largeName; }
    public void setLargeName(String largeName) { this.largeName = largeName; }

    public String getMediumName() { return mediumName; }
    public void setMediumName(String mediumName) { this.mediumName = mediumName; }
}
