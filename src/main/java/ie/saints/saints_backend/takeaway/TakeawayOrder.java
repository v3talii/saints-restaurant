package ie.saints.saints_backend.takeaway;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "takeaway_order")
public class TakeawayOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phone;
    private LocalDate pickupDate;
    private LocalTime pickupTime;

    @Column(length = 500)
    private String notes;

    private BigDecimal total = BigDecimal.ZERO;
    private Instant createdAt = Instant.now();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "takeaway_order_line", joinColumns = @JoinColumn(name = "order_id"))
    private List<OrderLine> lines = new ArrayList<>();

    protected TakeawayOrder() {}

    public TakeawayOrder(String name, String phone, LocalDate pickupDate,
                         LocalTime pickupTime, String notes) {
        this.name = name;
        this.phone = phone;
        this.pickupDate = pickupDate;
        this.pickupTime = pickupTime;
        this.notes = notes;
    }

    public void addLine(OrderLine line) {
        lines.add(line);
        total = total.add(line.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantity())));
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public LocalDate getPickupDate() { return pickupDate; }
    public LocalTime getPickupTime() { return pickupTime; }
    public String getNotes() { return notes; }
    public BigDecimal getTotal() { return total; }
    public Instant getCreatedAt() { return createdAt; }
    public List<OrderLine> getLines() { return lines; }
}