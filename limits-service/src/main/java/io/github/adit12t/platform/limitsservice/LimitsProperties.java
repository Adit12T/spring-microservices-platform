package io.github.adit12t.platform.limitsservice;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Limits come from the config server. A mutable properties bean is rebound in place on
 * POST /actuator/refresh, so a change in the config repository applies without a restart.
 */
@ConfigurationProperties("limits")
public class LimitsProperties {

    private int minimum;
    private int maximum;
    private String currency;

    public int getMinimum() { return minimum; }
    public void setMinimum(int minimum) { this.minimum = minimum; }
    public int getMaximum() { return maximum; }
    public void setMaximum(int maximum) { this.maximum = maximum; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
