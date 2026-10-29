package ie.saints.saints_backend.menu;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 500)
    private String description;

    private BigDecimal price;
    private String allergens; // e.g. "1,9"
    private int sortOrder;

    @Column(length = 300)
    private String note; // extras, e.g. "Add chicken +€6"

    @ManyToOne(optional = false)
    private Category category;

    protected MenuItem() {}

    public MenuItem(String name, String description, BigDecimal price,
                    String allergens, int sortOrder, Category category, String note) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.allergens = allergens;
        this.sortOrder = sortOrder;
        this.category = category;
        this.note = note;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public String getAllergens() { return allergens; }
    public int getSortOrder() { return sortOrder; }
    public String getNote() { return note; }
    public Category getCategory() { return category; }
}