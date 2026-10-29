package ie.saints.saints_backend.menu;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByCategoryOrderBySortOrderAsc(Category category);
}