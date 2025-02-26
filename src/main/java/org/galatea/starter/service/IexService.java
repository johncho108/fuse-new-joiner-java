package org.galatea.starter.service;

import java.util.Collections;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.galatea.starter.APIToken;
import org.galatea.starter.domain.IexLastTradedPrice;
import org.galatea.starter.domain.IexSymbol;
import org.galatea.starter.domain.IexHistoricalPrice;
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
   * @param date date as String; format YYYYMMDD
   * @return a list of historical prices for the symbol/range passed in
   */
  public List<IexHistoricalPrice> getHistoricalPrices(final String symbol, final String range,
      final String date) {
    /* range and date path variables are optional, so they can be passed as empty strings,
    as implemented below.(IEX API handles null and empty string path variables identically.)
    Alternatively, using method overloading, we would need four getHistoricalPrices methods
    that include/exclude range and/or date, which seems unnecessary.
     */
    final String clientRange = (range == null) ? "" : range;
    final String clientDate = (date == null) ? "" : date;

    return iexClient.getHistoricalPrices(token, symbol, clientRange, clientDate);
    }
}
