package ie.saints.saints_backend.reservation;

import ie.saints.saints_backend.takeaway.TakeawayOrder;

public interface BookingNotifier {
    void notifyNewBooking(Reservation reservation);
    void notifyNewTakeaway(TakeawayOrder order);
}