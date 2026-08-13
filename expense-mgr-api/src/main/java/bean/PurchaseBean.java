package bean;

import java.sql.Timestamp;

public class PurchaseBean {
    private int purchaseId;
    private int userId;
    private int itemId;
    private int usedPoint;
    private Timestamp purchasedAt;
    private String productName;

    public PurchaseBean() {}

    public int getPurchaseId() { return purchaseId; }
    public void setPurchaseId(int purchaseId) { this.purchaseId = purchaseId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getUsedPoint() { return usedPoint; }
    public void setUsedPoint(int usedPoint) { this.usedPoint = usedPoint; }

    public Timestamp getPurchasedAt() { return purchasedAt; }
    public void setPurchasedAt(Timestamp purchasedAt) { this.purchasedAt = purchasedAt; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
}
