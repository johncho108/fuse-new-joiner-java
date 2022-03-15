package org.galatea.starter.domain.repository;

import java.util.List;
import org.galatea.starter.entity.HistoricalPrice;
import org.galatea.starter.entity.Query;
import org.springframework.data.repository.CrudRepository;

public interface HistoricalPriceRepository extends CrudRepository<HistoricalPrice, Long> {
  List<HistoricalPrice> findByQuery(Query query);
}
