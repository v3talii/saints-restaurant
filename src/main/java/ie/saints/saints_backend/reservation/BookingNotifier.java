package ie.saints.saints_backend.reservation;

public interface BookingNotifier {
    void notifyNewBooking(Reservation reservation);
}