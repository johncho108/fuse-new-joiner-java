package org.galatea.starter.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.galatea.starter.APIToken;
import org.galatea.starter.domain.IexLastTradedPrice;
import org.galatea.starter.domain.IexSymbol;
import org.galatea.starter.domain.IexHistoricalPrice;
import org.galatea.starter.repository.HistoricalPriceRepository;
import org.galatea.starter.entity.HistoricalPrice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * A layer for transformation, aggregation, and business required when retrieving data from IEX.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IexService {

  @NonNull
  private IexClient iexClient;
  private String token = APIToken.token;

  @Autowired
  private HistoricalPriceRepository historicalPriceRepository;

  /**
   * Get all stock symbols from IEX.
   *
   * @return a list of all Stock Symbols from IEX.
   */
  public List<IexSymbol> getAllSymbols() {
    return iexClient.getAllSymbols(token);
  }

  /**
   * Get the last traded price for each Symbol that is passed in.
   *
   * @param symbols the list of symbols to get a last traded price for.
   * @return a list of last traded price objects for each Symbol that is passed in.
   */
  public List<IexLastTradedPrice> getLastTradedPriceForSymbols(final List<String> symbols) {
    if (CollectionUtils.isEmpty(symbols)) {
      return Collections.emptyList();
    } else {
      return iexClient.getLastTradedPriceForSymbols(token, symbols.toArray(new String[0]));
    }
  }

  /**
   * Get historical prices given stock and time range.
   *
   * @param symbol stock symbol for historical prices
   * @param range time range for historical prices (e.g. 3m, 6m, 5y)
//   * @param date date as String; format YYYYMMDD
   * @return a list of historical prices for the symbol/range passed in
   */
//  @Cacheable(value="historicalPrices", key="{ #symbol, #range, #date }")
  public List<IexHistoricalPrice> getHistoricalPrices(final String symbol, final String range) {
    /* To return to IexRestController */
    List<IexHistoricalPrice> historicalPrices;

    /* Calculate starting date in range */
    long duration = Character.getNumericValue(range.charAt(0));
    char calendarUnit = range.charAt(1);
    LocalDate currentDate = LocalDate.now();
    LocalDate startingDate = null;
    if (calendarUnit == 'd') {
      startingDate = currentDate.minusDays(duration);
    } else if (calendarUnit == 'm') {
      startingDate = currentDate.minusMonths(duration);
    } else if (calendarUnit == 'y') {
      startingDate = currentDate.minusYears(duration);
    }
    log.info("duration, calendarUnit, startingDate: {}, {}, {}", duration, calendarUnit, startingDate);

    /* Create list of date strings to query against db */
    List<String> allQueryDates = new ArrayList<>();

    for (LocalDate d = startingDate; d.isBefore(currentDate); d = d.plusDays(1)) {
      String dateString = d.format(DateTimeFormatter.BASIC_ISO_DATE);
      log.info("date {}", dateString);
      allQueryDates.add(dateString);
    }

    /*
     * Check if all dates are cached in db. If they aren't, repeated hits against the IEX endpoint
     * would be necessary for all individual dates not cached.
     * Thus, we only retrieve from db if all dates are cached in db.
     */
    boolean allQueriesCached = true;
    for (String d : allQueryDates) {
      if (!historicalPriceRepository.existsByDateAndSymbol(d, symbol)) {
        allQueriesCached = false;
        break;
      }
    }
    log.info("all queries cached: {}", allQueriesCached);

    /* If all dates are cached, query db for each date, create IexHistoricalPrice objects,
     * and populate List<IexHistoricalPrice> to return to IexRestController.
     */
    if (allQueriesCached) {
      List<HistoricalPrice> historicalPriceEntities = new ArrayList<>();
      for (String d : allQueryDates) {
        historicalPriceEntities.add(historicalPriceRepository.findByDateAndSymbol(d, symbol));
      }
      historicalPrices = new ArrayList<>();
      for (HistoricalPrice h : historicalPriceEntities) {
        IexHistoricalPrice historicalPrice = IexHistoricalPrice.builder()
            .close(h.getClose())
            .high(h.getHigh())
            .low(h.getLow())
            .open(h.getOpen())
            .symbol(h.getSymbol())
            .volume(h.getVolume())
            .date(h.getDate())
            .build();
        historicalPrices.add(historicalPrice);
      }
      /*
       * If not all dates are cached, retrieve List<IexHistoricalPrice> from IexClient;
       * cache historical price data (from IexClient); and return
       * List<IexHistoricalPrice> after caching.
       */
    } else {
      historicalPrices = iexClient.getHistoricalPrices(token, symbol,
          range);

      for (IexHistoricalPrice h : historicalPrices) {
        HistoricalPrice historicalPrice = HistoricalPrice.builder()
            .close(h.getClose())
            .high(h.getHigh())
            .low(h.getLow())
            .open(h.getOpen())
            .symbol(h.getSymbol())
            .volume(h.getVolume())
            .date(h.getDate())
            .build();
        historicalPriceRepository.save(historicalPrice);
      }
    }
    return historicalPrices;
  }
}