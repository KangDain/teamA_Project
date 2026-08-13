package bean;

public class ItemBean {
    private int itemId;
    private String productName;
    private String productType;
    private int pricePoint;

    public ItemBean() {}

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }

    public int getPricePoint() { return pricePoint; }
    public void setPricePoint(int pricePoint) { this.pricePoint = pricePoint; }
}
