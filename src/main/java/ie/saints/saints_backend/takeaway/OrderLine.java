package ie.saints.saints_backend.takeaway;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public class OrderLine {

    private String itemName;
    private int quantity;
    private BigDecimal unitPrice;

    protected OrderLine() {}

    public OrderLine(String itemName, int quantity, BigDecimal unitPrice) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
}