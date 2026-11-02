package ie.saints.saints_backend.reservation;

import ie.saints.saints_backend.takeaway.OrderLine;
import ie.saints.saints_backend.takeaway.TakeawayOrder;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "email.enabled", havingValue = "true")
public class EmailNotifier implements BookingNotifier {

    private final RestClient client;
    private final String apiKey;
    private final String from;
    private final String to;

    public EmailNotifier(@Value("${email.api-key}") String apiKey,
                         @Value("${email.from}") String from,
                         @Value("${email.to}") String to) {
        this.apiKey = apiKey;
        this.from = from;
        this.to = to;

        HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(Duration.ofSeconds(20));
        this.client = RestClient.builder().requestFactory(factory).build();
    }

    @Override
    public void notifyNewBooking(Reservation r) {
        // Guest input stays out of the subject
        String subject = "BOOKING: " + r.getDate() + " " + r.getTime() + ", " + r.getGuests() + " guests";
        String text = """
                New table booking from the website

                Name: %s
                Phone: %s
                Date: %s
                Time: %s
                Guests: %d
                Special requests: %s

                Booking #%d
                """.formatted(
                r.getName(), r.getPhone(), r.getDate(), r.getTime(), r.getGuests(),
                blankToNone(r.getSpecialRequests()), r.getId());
        send(subject, text);
    }

    @Override
    public void notifyNewTakeaway(TakeawayOrder o) {
        String subject = "TAKEAWAY: pickup " + o.getPickupDate() + " " + o.getPickupTime()
                + ", €" + o.getTotal();

        StringBuilder lines = new StringBuilder();
        for (OrderLine l : o.getLines()) {
            lines.append(l.getQuantity()).append(" x ").append(l.getItemName())
                    .append("  (€").append(l.getUnitPrice()).append(" each)\n");
        }

        String text = """
                New TAKEAWAY order from the website

                Name: %s
                Phone: %s
                Pickup: %s at %s

                ORDER:
                %s
                TOTAL: €%s (pay on collection)

                Notes / extras: %s

                Order #%d
                """.formatted(
                o.getName(), o.getPhone(), o.getPickupDate(), o.getPickupTime(),
                lines, o.getTotal(), blankToNone(o.getNotes()), o.getId());
        send(subject, text);
    }

    private static String blankToNone(String s) {
        return s == null || s.isBlank() ? "None" : s;
    }

    private void send(String subject, String text) {
        Map<String, Object> payload = Map.of("from", from, "to", new String[] {to},
                "subject", subject, "text", text);

        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                client.post()
                        .uri("https://api.resend.com/emails")
                        .header("Authorization", "Bearer " + apiKey)
                        .body(payload)
                        .retrieve()
                        .toBodilessEntity();
                return;
            } catch (RuntimeException e) {
                if (attempt == 2) throw e;
            }
        }
    }
}