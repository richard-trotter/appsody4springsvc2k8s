package demo.inventory.client;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import demo.inventory.api.model.InventoryItemModel;
import io.restassured.RestAssured;
import io.restassured.internal.path.json.mapping.JsonPathJackson2ObjectDeserializer;
import io.restassured.mapper.factory.DefaultJackson2ObjectMapperFactory;
import io.restassured.path.json.JsonPath;
import io.restassured.path.json.config.JsonPathConfig;
import io.restassured.response.Response;

@EnabledIfSystemProperty(named = "spring.profiles.active", matches = ".*live-api-test.*")
public class InventoryControllerLiveTest {
  private static final Logger log = LoggerFactory.getLogger(InventoryControllerLiveTest.class);

  private static final String JSON = MediaType.APPLICATION_JSON.toString();

  private static String serverUri = "http://localhost:8080";
  private static final String resourcePath = "/demo/inventory/item";
  
  // Expected test dataset size
  private static final int expectedTotalItems = 12;

  // setup
  
  @BeforeAll
  static void beforeClass() {
    
    String p = System.getProperty("serverUri");
    if( p != null )
      serverUri = p;
    
    log.info(String.format("Beginning test run using API server: \"%s\"", serverUri));
  }
  
  
  // constructor
  
  public InventoryControllerLiveTest() {
    super();
  }
  
  
  // tests

  @Test
  public void whenAllItemsAreRetrieved_thenPageNotEmpty() {
    
    // Expecting "all" request to be constrained to "first page"

    Response response;
    try {
      response = RestAssured
        .given().accept(JSON).expect().statusCode(200)
        .when().get(serverUri+resourcePath);
    }
    catch( Throwable ex) {
      fail(ex.toString());
      return;
    }

    //String content = response.getBody().prettyPrint();
    
    JsonPath path = response.jsonPath();
    
    // verify page metadata
    int n = path.getInt("numberOfElements");
    assertFalse(n == 0, "Missing list of InventoryItemModel");
    
    // verify page content
    List<String> itemNames = path.getList("content.name");
    assertTrue(itemNames.size() == n, "Missing page contents");

    // verify page count
    int pageNumber = path.getInt("number");
    int pageCount = path.getInt("totalPages");
    assertTrue(pageNumber+1 < pageCount, "Expected more than one page in total");
    
    
    boolean isLast = path.getBoolean("last");
    assertFalse(isLast, "First page should be indicated as not last page");

    log.info(String.format("Found a page of %d InventoryItemModel", n));
  }


  @Test
  public void whenLastPageRetrieved_thenLastPageIsTrue() {
  
    // Expecting 2 pages of test data. Page "1" should be the second and last page.  
    
    Response response;
    try {
      response = RestAssured
        .given().accept(JSON).expect().statusCode(200)
        .when().get(serverUri+resourcePath+"?page=1&size=7");
    }
    catch( Throwable ex) {
      fail(ex.toString());
      return;
    }
  
    JsonPath path = response.jsonPath();
    
    // verify page metadata for page of (12-7=) 5 items
    int n = path.getInt("numberOfElements");
    assertFalse(n == 0, "Missing list of InventoryItemModel");
    assertTrue(n == 5, "Wrong response page size");
    
    // verify page content
    List<String> itemNames = path.getList("content.name");
    assertTrue(itemNames.size() == n, "Missing page contents");

    log.info(String.format("Found a page of %d InventoryItemModel", n));

    // verify page count
    int pageNumber = path.getInt("number");
    int pageCount = path.getInt("totalPages");
    assertTrue(pageNumber+1 == pageCount, "Expected page 1 to be the last page");    
    
    boolean isLast = path.getBoolean("last");
    assertTrue(isLast, "Page 1 should be indicated as last page");
  }


  @Test
  public void whenPageRetrieved_thenNextLinkPresent() {
    
    // Expecting a page other than "last page" to have 'link' header for a 'next' relation
  
    Response response;
    try {
      response = RestAssured
        .given().accept(JSON).expect().statusCode(200)
        .when().get(serverUri+resourcePath+"?page=1&size=2");
    }
    catch( Throwable ex) {
      fail(ex.toString());
      return;
    }
  
    //String content = response.getBody().prettyPrint();
    
    String linkValue = response.header(HttpHeaders.LINK.toLowerCase());
    assertNotNull(linkValue, "Missing 'link' header for 'next' relation");

    log.info("Found 'link' HTTP header: "+linkValue);

    String[] parts = linkValue.split(";");
    assertTrue(parts.length == 2, "Expected 2 parts to link header");
    
    assertTrue(parts[0].contains(resourcePath+"?page=2&size=2"), "Wrong 'next' link url");
    assertTrue(parts[1].equals("rel=\"next\""), "Wrong link relation value");
  }


  @Test
  public void whenItemRetrieved_thenItemObjectPresent() {
    
    // Expecting an item with id 13402
    InventoryItemModel item = retrieveItemAtLocation(serverUri+resourcePath+"/13402");
    
    log.info("Got InventoryItemModel with id: "+item.getId());
  }
  
  @Test
  public void whenPageRetrieved_thenItemListPresent() {
    
    // Expecting a page of 2 items
  
    Response response;
    try {
      response = RestAssured
        .given().accept(JSON).expect().statusCode(200)
        .when().get(serverUri+resourcePath+"?page=1&size=2");
    }
    catch( Throwable ex) {
      fail(ex.toString());
      return;
    }
 
    //response.getBody().prettyPrint();

    // Verify the response has 'content'
    String responseBody = response.getBody().asString(); 
    assertTrue(responseBody.startsWith("{\"content\":"), "Missing response page content");
    
    // Unmarshal the JSON response page content as a List of InventoryItemModel
    JsonPathConfig config = new JsonPathConfig()
        .defaultObjectDeserializer(new JsonPathJackson2ObjectDeserializer(new DefaultJackson2ObjectMapperFactory()));
    JsonPath path = response.jsonPath(config);
    List<InventoryItemModel> items = path.getList("content", InventoryItemModel.class);
    assertTrue(items.size() == 2, "Wrong items content length for items page");

    log.info("Got InventoryItemModel list of length: "+items.size());

  }


  @Test
  public void whenCreateItem_thenCreatedAndFound() throws Exception {
  
    // create a testcase item sans id 
    InventoryItemModel testItem = new InventoryItemModel(
        "Thinkpad"              /*name*/, 
        "Laptop computer"       /*description*/, 
        new BigDecimal(1525.50) /*price*/, 
        null                    /*img_alt*/, 
        "tp450.jpg"             /*img*/, 
        7                       /*stock*/);
  
    ObjectMapper mapper = new ObjectMapper();       

    // create http request body 
    ObjectWriter writer = mapper.writerFor(InventoryItemModel.class);
    String body = writer.writeValueAsString(testItem);

    // verify 201 http response status and presence of location response header
    Response response;
    try {
      response = RestAssured
        .given().body(body).contentType(JSON)
        .expect().statusCode(201).expect().header(HttpHeaders.LOCATION, not(nullValue()))
        .when().post(serverUri+resourcePath);
    }
    catch( Throwable ex) {
      fail(ex.toString());
      return;
    }
  
    // verify location response header value
    String locationValue = response.getHeader(HttpHeaders.LOCATION);
    InventoryItemModel item = retrieveItemAtLocation(locationValue);
    assertTrue( item.getId() > 0, "get new item response does not include new item id" );
    
    log.info("Created new item with id: "+item.getId()+"\n"+item.toString());
    
    // delete test data
    try {
      RestAssured
        .given()
        .expect().statusCode(200)
        .when().delete(serverUri+resourcePath+"/"+item.getId());
    }
    catch( Throwable ex) {
      fail(ex.toString());
      return;
    }
  }


  private InventoryItemModel retrieveItemAtLocation(String resourceUri) {
    
    Response response;
    try {
      response = RestAssured
        .given().accept(JSON).expect().statusCode(200)
        .when().get(resourceUri);
    }
    catch( Throwable ex) {
      fail(ex.toString());
      return null;
    }
  
    //String content = response.getBody().prettyPrint();
    
    ObjectMapper mapper = new ObjectMapper();       

    // Unmarshal the JSON response body as an instance of InventoryItemModel
    ObjectReader reader = mapper.readerFor(InventoryItemModel.class);
    try {
      InventoryItemModel item = reader.readValue(response.getBody().asInputStream());
      return item;
    }
    catch (IOException e) {
      fail(e);
      return null;
    }
  }

}
