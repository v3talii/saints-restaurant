package ie.saints.saints_backend.menu;

import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categories;
    private final MenuItemRepository items;
    private int order;

    public DataSeeder(CategoryRepository categories, MenuItemRepository items) {
        this.categories = categories;
        this.items = items;
    }

    // Use "|" in note to split extras into separate lines
    private void add(Category c, String name, String desc, String price,
                     String allergens, String note) {
        items.save(new MenuItem(name, desc, new BigDecimal(price), allergens, ++order, c, note));
    }

    @Override
    public void run(String... args) {
        if (categories.count() > 0) return; // skip if already seeded

        Category starters = categories.save(new Category("Starters", 1));
        Category mains    = categories.save(new Category("Main Courses", 2));
        Category kids     = categories.save(new Category("Kids Menu", 3));
        Category desserts = categories.save(new Category("Desserts", 4));

        // STARTERS
        order = 0;
        add(starters, "Soup of the Day", "Served with homemade brown bread", "6.95", "1,9", null);
        add(starters, "Deep Fried Gherkins", "Served with house garlic mayonnaise", "5.50", "3", null);
        add(starters, "Cauliflower Nuggets", "Served with pickled vegetables", "7.50", null, null);
        add(starters, "Deep Fried Chicken Wings",
                "Served with hot sauce and house garlic mayonnaise", "8.95", "3,5,8,9",
                "Main portion €14.95");
        add(starters, "Classic Caesar Salad", "", "9.00", "1,3",
                "Main €12|Add chicken or prawns +€6 (2)");
        add(starters, "Roast Squash & Pumpkin Seed Salad",
                "With a raspberry maple dressing", "9.00", null,
                "Main €12|Add chicken or prawns +€6 (2)");
        add(starters, "Garlic Prawns", "In white wine and herbs with homemade bread", "11.00", "1,2", null);

        // MAIN COURSES
        order = 0;
        add(mains, "10oz Striploin Steak",
                "With beer braised onion and overnight tomato. Served with chips or potato and a choice of "
                + "house garlic and herb butter (7), pepper sauce (7,9) or whiskey and sweet pepper sauce (7,9)",
                "32.00", "7,9", "Add prawn croquette +€6 (2,7)");
        add(mains, "Chicken Supreme",
                "With a choice of house garlic and herb butter, pepper sauce or whiskey and sweet pepper sauce, "
                + "creamy mash and buttered greens",
                "23.50", "7,9", null);
        add(mains, "Battered Catch of the Day",
                "Served with chips, salad and tartare sauce", "21.50", "1,3,4,12", null);
        add(mains, "Homemade Irish Beef & Pork Burger",
                "On a brioche bun with tomato, gherkin, cheese and garlic mayonnaise, with a side of chips",
                "18.00", "1,3,7,12",
                "Go full loaded: braised onion, overnight tomato, crispy bacon and smoked cheddar +€3");
        add(mains, "Rigatoni Pasta",
                "With roast Mediterranean vegetables in a choice of homemade tomato sauce or basil pesto",
                "16.50", "1,7,8,9", "Add chicken or prawns +€6 (2)");
        add(mains, "House Curry",
                "With roast vegetables, served with steamed rice and/or chips",
                "14.50", "1,6,9", "Add chicken or prawns +€6 (2)");

        // KIDS MENU
        order = 0;
        add(kids, "Sausages & Chips", "", "9.50", "1,12", null);
        add(kids, "Beef Burger & Chips", "", "9.50", "1,12", null);
        add(kids, "Chicken Goujons & Chips", "", "9.50", "1", null);
        add(kids, "Pasta", "With homemade tomato sauce (1,9) or basil pesto (7,8)", "9.50", null, null);

        // DESSERTS
        order = 0;
        add(desserts, "Warm Chocolate Brownie",
                "With vanilla ice cream and hot chocolate sauce", "7.95", "3,7", null);
        add(desserts, "Selection of Ice Cream", "", "6.50", "3", null);
        add(desserts, "Cheesecake of the Day", "", "7.95", "1,3,7", null);
        add(desserts, "Homemade Irish Apple Tart",
                "With ice cream, whipped cream or light custard", "9.00", "1,3,7", null);

        System.out.println(">>> Menu seeded: " + items.count() + " items");
    }
}