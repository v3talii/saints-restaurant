package ie.saints.saints_backend.reservation;

import ie.saints.saints_backend.takeaway.TakeawayOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "email.enabled", havingValue = "false", matchIfMissing = true)
public class LogNotifier implements BookingNotifier {

    @Override
    public void notifyNewBooking(Reservation r) {
        System.out.println(">>> NEW BOOKING #" + r.getId() + ": " + r.getName()
                + ", " + r.getPhone() + ", " + r.getDate() + " " + r.getTime()
                + ", guests: " + r.getGuests() + ", notes: " + r.getSpecialRequests());
    }

    @Override
    public void notifyNewTakeaway(TakeawayOrder o) {
        System.out.println(">>> NEW TAKEAWAY #" + o.getId() + ": " + o.getName()
                + ", " + o.getPhone() + ", pickup " + o.getPickupDate() + " " + o.getPickupTime()
                + ", total: " + o.getTotal() + ", lines: " + o.getLines().size());
    }
}