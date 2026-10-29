package ie.saints.saints_backend.reservation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "email.enabled", havingValue = "true")
public class EmailNotifier implements BookingNotifier {

    private final JavaMailSender mailSender;
    private final String from;
    private final String to;

    public EmailNotifier(JavaMailSender mailSender,
                         @Value("${spring.mail.username}") String from,
                         @Value("${email.to}") String to) {
        this.mailSender = mailSender;
        this.from = from;
        this.to = to;
    }

    @Override
    public void notifyNewBooking(Reservation r) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(from);
        mail.setTo(to);
        // Guest input stays out of the subject
        mail.setSubject("New booking: " + r.getDate() + " " + r.getTime()
                + ", " + r.getGuests() + " guests");
        mail.setText("""
                New booking request from the website

                Name: %s
                Phone: %s
                Date: %s
                Time: %s
                Guests: %d
                Special requests: %s

                Booking #%d
                """.formatted(
                r.getName(), r.getPhone(), r.getDate(), r.getTime(), r.getGuests(),
                r.getSpecialRequests() == null || r.getSpecialRequests().isBlank()
                        ? "None" : r.getSpecialRequests(),
                r.getId()));
        mailSender.send(mail);
    }
}