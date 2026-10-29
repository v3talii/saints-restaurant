package ie.saints.saints_backend.menu;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    // Public JSON shape (kept separate from entities)
    public record ItemDto(String name, String description, BigDecimal price,
                          String allergens, String note) {}
    public record CategoryDto(String name, List<ItemDto> items) {}

    private final CategoryRepository categories;
    private final MenuItemRepository items;

    public MenuController(CategoryRepository categories, MenuItemRepository items) {
        this.categories = categories;
        this.items = items;
    }

    @GetMapping
    public List<CategoryDto> getMenu() {
        return categories.findAllByOrderBySortOrderAsc().stream()
                .map(category -> new CategoryDto(
                        category.getName(),
                        items.findByCategoryOrderBySortOrderAsc(category).stream()
                                .map(i -> new ItemDto(i.getName(), i.getDescription(),
                                        i.getPrice(), i.getAllergens(), i.getNote()))
                                .toList()))
                .toList();
    }
}