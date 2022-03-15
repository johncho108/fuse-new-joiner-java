package org.galatea.starter.domain.repository;

import org.galatea.starter.entity.Query;
import org.springframework.data.repository.CrudRepository;

public interface QueryRepository extends CrudRepository<Query, Long> {
  Query findBySymbolAndRangeAndDate(String symbol, String range, String date);
  boolean existsBySymbolAndRangeAndDate(String symbol, String range, String date);
}
