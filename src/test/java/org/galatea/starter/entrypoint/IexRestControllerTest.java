package org.galatea.starter.entrypoint;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import feign.FeignException;
import java.math.BigDecimal;
import java.util.Collections;
import junitparams.JUnitParamsRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.galatea.starter.ASpringTest;
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
  public void testGetHistoricalPrices() throws Exception {

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
                .get("/iex/historicalPrices?token=DUMMY_TKN&symbol=twtr")
                .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest())
        .andExpect(status().reason(containsString("Required String parameter 'range' is not present")))
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

}