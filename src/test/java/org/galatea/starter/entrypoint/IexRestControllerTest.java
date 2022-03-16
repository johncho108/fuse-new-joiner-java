package org.galatea.starter.entrypoint;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import junitparams.JUnitParamsRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.galatea.starter.ASpringTest;
import org.galatea.starter.repository.HistoricalPriceRepository;
import org.galatea.starter.entity.HistoricalPrice;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.NestedServletException;


@RequiredArgsConstructor
@Slf4j
// We need to do a full application start up for this one, since we want the feign clients to be instantiated.
// It's possible we could do a narrower slice of beans, but it wouldn't save that much test run time.
@SpringBootTest
// this gives us the MockMvc variable
@AutoConfigureMockMvc
// we previously used WireMockClassRule for consistency with ASpringTest, but when moving to a dynamic port
// to prevent test failures in concurrent builds, the wiremock server was created too late and feign was
// already expecting it to be running somewhere else, resulting in a connection refused
@AutoConfigureWireMock(port = 0, files = "classpath:/wiremock")
// Use this runner since we want to parameterize certain tests.
// See runner's javadoc for more usage.
@RunWith(JUnitParamsRunner.class)
public class IexRestControllerTest extends ASpringTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private HistoricalPriceRepository historicalPriceRepository;

  @Test
  public void testGetSymbolsEndpoint() throws Exception {
    MvcResult result = this.mvc.perform(
        // note that we were are testing the fuse REST end point here, not the IEX end point.
        // the fuse end point in turn calls the IEX end point, which is WireMocked for this test.
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/iex/symbols?token=DUMMY_TKN")
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        // some simple validations, in practice I would expect these to be much more comprehensive.
        .andExpect(jsonPath("$[0].symbol", is("A")))
        .andExpect(jsonPath("$[1].symbol", is("AA")))
        .andExpect(jsonPath("$[2].symbol", is("AAAU")))
        .andReturn();
  }

  @Test
  public void testGetLastTradedPrice() throws Exception {

    MvcResult result = this.mvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .get("/iex/lastTradedPrice?token=DUMMY_TKN&symbols=FB")
            // This URL will be hit by the MockMvc client. The result is configured in the file
            // src/test/resources/wiremock/mappings/mapping-lastTradedPrice.json
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].symbol", is("FB")))
        .andExpect(jsonPath("$[0].price").value(new BigDecimal("186.3011")))
        .andReturn();
  }

  @Test
  public void testGetLastTradedPriceEmpty() throws Exception {

    MvcResult result = this.mvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .get("/iex/lastTradedPrice?token=DUMMY_TKN&symbols=")
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", is(Collections.emptyList())))
        .andReturn();
  }

  @Test
  public void testGetHistoricalPricesNoDate() throws Exception {

    MvcResult result = this.mvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/iex/historicalPrices?token=DUMMY_TKN&symbol=AAPL&range=5d")
                .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].close", is(166.23)))
        .andExpect(jsonPath("$[0].high", is(168.91)))
        .andExpect(jsonPath("$[0].low", is(165.55)))
        .andExpect(jsonPath("$[0].open", is(168.47)))
        .andExpect(jsonPath("$[0].symbol", is("AAPL")))
        .andExpect(jsonPath("$[0].volume", is(76678441)))
        .andExpect(jsonPath("$[0].date", is("2022-03-03")))
        .andExpect(jsonPath("$[2].close", is(159.3)))
        .andExpect(jsonPath("$[2].high", is(165.02)))
        .andExpect(jsonPath("$[2].low", is(159.04)))
        .andExpect(jsonPath("$[2].open", is(163.36)))
        .andExpect(jsonPath("$[2].symbol", is("AAPL")))
        .andExpect(jsonPath("$[2].volume", is(96418845)))
        .andExpect(jsonPath("$[2].date", is("2022-03-07")))
        .andReturn();
  }

  @Test
  public void testGetHistoricalPricesDate() throws Exception {

    MvcResult result = this.mvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/iex/historicalPrices?token=DUMMY_TKN&symbol=twtr&range=date&date=20200220")
                .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].close", is(38.785)))
        .andExpect(jsonPath("$[0].high", is(38.785)))
        .andExpect(jsonPath("$[0].low", is(38.785)))
        .andExpect(jsonPath("$[0].open", is(38.785)))
        .andExpect(jsonPath("$[0].symbol", is(nullValue())))
        .andExpect(jsonPath("$[0].volume", is(100)))
        .andExpect(jsonPath("$[0].date", is("2020-02-20")))
        .andExpect(jsonPath("$[9].close", is(38.56)))
        .andExpect(jsonPath("$[9].high", is(38.56)))
        .andExpect(jsonPath("$[9].low", is(38.485)))
        .andExpect(jsonPath("$[9].open", is(38.535)))
        .andExpect(jsonPath("$[9].symbol", is(nullValue())))
        .andExpect(jsonPath("$[9].volume", is(400)))
        .andExpect(jsonPath("$[9].date", is("2020-02-20")))
        .andReturn();
  }

  @Test
  public void testGetHistoricalPricesNoRange() throws Exception {

    MvcResult result = this.mvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/iex/historicalPrices?token=DUMMY_TKN&symbol=FB")
                .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].close", is(219.55)))
        .andExpect(jsonPath("$[0].high", is(230.42)))
        .andExpect(jsonPath("$[0].low", is(218.7701)))
        .andExpect(jsonPath("$[0].open", is(228.46)))
        .andExpect(jsonPath("$[0].symbol", is("FB")))
        .andExpect(jsonPath("$[0].volume", is(46156943)))
        .andExpect(jsonPath("$[0].date", is("2022-02-11")))
        .andExpect(jsonPath("$[4].close", is(207.71)))
        .andExpect(jsonPath("$[4].high", is(217.5)))
        .andExpect(jsonPath("$[4].low", is(207.1601)))
        .andExpect(jsonPath("$[4].open", is(214.02)))
        .andExpect(jsonPath("$[4].symbol", is("FB")))
        .andExpect(jsonPath("$[4].volume", is(38747533)))
        .andExpect(jsonPath("$[4].date", is("2022-02-17")))
        .andReturn();
  }

  @Test()
  public void testGetHistoricalNullSymbol() throws Exception {

    MvcResult result = this.mvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/iex/historicalPrices?token=DUMMY_TKN&range=5d")
                .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest())
        .andExpect(status().reason(containsString("Required String parameter 'symbol' is not present")))
        .andReturn();
  }

  @Test(expected=NestedServletException.class)
  public void testGetHistoricalEmptyStringSymbol() throws Exception {

    MvcResult result = this.mvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/iex/historicalPrices?token=DUMMY_TKN&symbol=&range=5d")
                .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isInternalServerError())
        .andExpect(status().reason(containsString("status 404 reading IexClient#getHistoricalPrices(String,String,String)")))
        .andReturn();
  }

  @Test(expected=NestedServletException.class)
  public void testGetHistoricalInvalidSymbol() throws Exception {

    MvcResult result = this.mvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/iex/historicalPrices?token=DUMMY_TKN&symbol=abcde&range=5d")
                .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isInternalServerError())
        .andExpect(status().reason(containsString("status 404 reading IexClient#getHistoricalPrices(String,String,String)")))
        .andReturn();
  }

//  @Test
//  public void testRepeatedCalls() throws Exception {
//
//    this.mvc.perform(
//            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
//                .get("/iex/historicalPrices?token=DUMMY_TKN&symbol=twtr&range=date&date=20200220")
//                .accept(MediaType.APPLICATION_JSON_VALUE))
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$[0].close", is(38.785)))
//        .andExpect(jsonPath("$[0].high", is(38.785)))
//        .andExpect(jsonPath("$[0].low", is(38.785)))
//        .andExpect(jsonPath("$[0].open", is(38.785)))
//        .andExpect(jsonPath("$[0].symbol", is(nullValue())))
//        .andExpect(jsonPath("$[0].volume", is(100)))
//        .andExpect(jsonPath("$[0].date", is("2020-02-20")))
//        .andExpect(jsonPath("$[9].close", is(38.56)))
//        .andExpect(jsonPath("$[9].high", is(38.56)))
//        .andExpect(jsonPath("$[9].low", is(38.485)))
//        .andExpect(jsonPath("$[9].open", is(38.535)))
//        .andExpect(jsonPath("$[9].symbol", is(nullValue())))
//        .andExpect(jsonPath("$[9].volume", is(400)))
//        .andExpect(jsonPath("$[9].date", is("2020-02-20")));
//
//    Query query = queryRepository.findBySymbolAndRangeAndDate("twtr", "date", "20200220");
//    assertEquals(query.getSymbol(), "twtr");
//    assertEquals(query.getRange(), "date");
//    assertEquals(query.getDate(), "20200220");
//
//    List<HistoricalPrice> historicalPriceEntities = historicalPriceRepository.findByQuery(query);
//    HistoricalPrice historicalPrice = historicalPriceEntities.get(0);
//
//    assertEquals(historicalPrice.getClose(), new BigDecimal(38.785).setScale(3, RoundingMode.HALF_UP));
//    assertEquals(historicalPrice.getHigh(), new BigDecimal(38.785).setScale(3, RoundingMode.HALF_UP));
//    assertEquals(historicalPrice.getLow(), new BigDecimal(38.785).setScale(3, RoundingMode.HALF_UP));
//    assertEquals(historicalPrice.getOpen(), new BigDecimal(38.785).setScale(3, RoundingMode.HALF_UP));
//    assertNull(historicalPrice.getSymbol());
//    assertEquals(historicalPrice.getVolume(), 100);
//    assertEquals(historicalPrice.getDate(), "2020-02-20");
//
//    historicalPrice = historicalPriceEntities.get(9);
//
//    assertEquals(historicalPrice.getClose(), new BigDecimal(38.56).setScale(3, RoundingMode.HALF_UP));
//    assertEquals(historicalPrice.getHigh(), new BigDecimal(38.56).setScale(3, RoundingMode.HALF_UP));
//    assertEquals(historicalPrice.getLow(), new BigDecimal(38.485).setScale(3, RoundingMode.HALF_UP));
//    assertEquals(historicalPrice.getOpen(), new BigDecimal(38.535).setScale(3, RoundingMode.HALF_UP));
//    assertNull(historicalPrice.getSymbol());
//    assertEquals(historicalPrice.getVolume(), 400);
//    assertEquals(historicalPrice.getDate(), "2020-02-20");
//
//    this.mvc.perform(
//            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
//                .get("/iex/historicalPrices?token=DUMMY_TKN&symbol=twtr&range=date&date=20200220")
//                .accept(MediaType.APPLICATION_JSON_VALUE))
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$[0].close", is(38.785)))
//        .andExpect(jsonPath("$[0].high", is(38.785)))
//        .andExpect(jsonPath("$[0].low", is(38.785)))
//        .andExpect(jsonPath("$[0].open", is(38.785)))
//        .andExpect(jsonPath("$[0].symbol", is(nullValue())))
//        .andExpect(jsonPath("$[0].volume", is(100)))
//        .andExpect(jsonPath("$[0].date", is("2020-02-20")))
//        .andExpect(jsonPath("$[9].close", is(38.56)))
//        .andExpect(jsonPath("$[9].high", is(38.56)))
//        .andExpect(jsonPath("$[9].low", is(38.485)))
//        .andExpect(jsonPath("$[9].open", is(38.535)))
//        .andExpect(jsonPath("$[9].symbol", is(nullValue())))
//        .andExpect(jsonPath("$[9].volume", is(400)))
//        .andExpect(jsonPath("$[9].date", is("2020-02-20")));
//
//  }
//
//  @Test()
//  public void testRepeatedInvalidCalls() throws Exception {
//
//    this.mvc.perform(
//            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
//                .get("/iex/historicalPrices?token=DUMMY_TKN&range=5d")
//                .accept(MediaType.APPLICATION_JSON_VALUE))
//        .andExpect(status().isBadRequest())
//        .andExpect(status().reason(containsString("Required String parameter 'symbol' is not present")));
//
//    assertFalse(queryRepository.existsBySymbolAndRangeAndDate("abcde", "date", "20200220"));
//
//    this.mvc.perform(
//            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
//                .get("/iex/historicalPrices?token=DUMMY_TKN&range=5d")
//                .accept(MediaType.APPLICATION_JSON_VALUE))
//        .andExpect(status().isBadRequest())
//        .andExpect(status().reason(containsString("Required String parameter 'symbol' is not present")));
//
//  }

}