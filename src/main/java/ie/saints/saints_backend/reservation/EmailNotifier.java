package ie.saints.saints_backend.reservation;

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

        HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.client = RestClient.builder().requestFactory(factory).build();
    }

    @Override
    public void notifyNewBooking(Reservation r) {
        // Guest input stays out of the subject
        String subject = "New booking: " + r.getDate() + " " + r.getTime()
                + ", " + r.getGuests() + " guests";

        String text = """
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
                r.getId());

        client.post()
                .uri("https://api.resend.com/emails")
                .header("Authorization", "Bearer " + apiKey)
                .body(Map.of("from", from, "to", new String[] {to}, "subject", subject, "text", text))
                .retrieve()
                .toBodilessEntity();
    }
}