-- Sample rates for the demo; not live market data.
INSERT INTO exchange_rate (currency_from, currency_to, rate, updated_at) VALUES
    ('USD', 'INR', 83.250000, CURRENT_TIMESTAMP),
    ('EUR', 'INR', 90.100000, CURRENT_TIMESTAMP),
    ('AUD', 'INR', 54.800000, CURRENT_TIMESTAMP),
    ('GBP', 'INR', 105.400000, CURRENT_TIMESTAMP),
    ('USD', 'EUR', 0.920000, CURRENT_TIMESTAMP),
    ('EUR', 'USD', 1.087000, CURRENT_TIMESTAMP),
    ('USD', 'GBP', 0.790000, CURRENT_TIMESTAMP),
    ('USD', 'JPY', 151.200000, CURRENT_TIMESTAMP);
