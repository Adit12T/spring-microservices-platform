package io.github.adit12t.platform.currencyexchangeservice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "exchange_rate")
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "currency_from", nullable = false, length = 3)
    private String from;
    @Column(name = "currency_to", nullable = false, length = 3)
    private String to;
    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal rate;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Version
    private long version;

    protected ExchangeRate() {
    }

    public ExchangeRate(String from, String to, BigDecimal rate) {
        this.from = from;
        this.to = to;
        this.rate = rate;
        this.updatedAt = Instant.now();
    }

    public void changeRate(BigDecimal newRate) {
        this.rate = newRate;
        this.updatedAt = Instant.now();
    }

    public String getFrom() { return from; }
    public String getTo() { return to; }
    public BigDecimal getRate() { return rate; }
    public Instant getUpdatedAt() { return updatedAt; }
}
