package org.galatea.starter.repository;

import java.util.List;
import org.galatea.starter.entity.HistoricalPrice;
import org.springframework.data.repository.CrudRepository;

public interface HistoricalPriceRepository extends CrudRepository<HistoricalPrice, Long> {
  HistoricalPrice findByDateAndSymbol(String date, String symbol);
  boolean existsByDateAndSymbol(String date, String symbol);
}
