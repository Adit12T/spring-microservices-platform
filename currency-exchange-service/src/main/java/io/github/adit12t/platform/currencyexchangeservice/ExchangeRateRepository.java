package io.github.adit12t.platform.currencyexchangeservice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    Optional<ExchangeRate> findByFromAndTo(String from, String to);

    List<ExchangeRate> findAllByOrderByFromAscToAsc();
}
