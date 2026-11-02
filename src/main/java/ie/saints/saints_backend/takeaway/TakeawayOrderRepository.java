package ie.saints.saints_backend.takeaway;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TakeawayOrderRepository extends JpaRepository<TakeawayOrder, Long> {}