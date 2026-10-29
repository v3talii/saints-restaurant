package ie.saints.saints_backend.reservation;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phone;
    private LocalDate date;
    private LocalTime time;
    private int guests;

    @Column(length = 500)
    private String specialRequests;

    private Instant createdAt = Instant.now();

    protected Reservation() {}

    public Reservation(String name, String phone, LocalDate date, LocalTime time,
                       int guests, String specialRequests) {
        this.name = name;
        this.phone = phone;
        this.date = date;
        this.time = time;
        this.guests = guests;
        this.specialRequests = specialRequests;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public LocalDate getDate() { return date; }
    public LocalTime getTime() { return time; }
    public int getGuests() { return guests; }
    public String getSpecialRequests() { return specialRequests; }
    public Instant getCreatedAt() { return createdAt; }
}