package demo.inventory.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import demo.inventory.api.model.InventoryItemModel;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

// TODO: need a test for "get one".
// TODO: need to document "run from command line"
// TODO: enable run against specified api server
// TODO: need a test for model unmarshaling

@EnabledIfSystemProperty(named = "spring.profiles.active", matches = ".*live-api-test.*")
public class InventoryControllerLiveTest {
  private static final Logger log = LoggerFactory.getLogger(InventoryControllerLiveTest.class);

  private static final String JSON = MediaType.APPLICATION_JSON.toString();

  private static final String apiServerUri = "http://localhost:8080/demo/inventory/items";
  
  // Expected test dataset size
  private static final int expectedTotalItems = 12;

  // setup
  
  @BeforeAll
  static void beforeClass() {
    log.info(String.format("Beginning test run using API server: \"%s\"", apiServerUri));
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
        .when().get(apiServerUri);
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
        .when().get(apiServerUri+"?page=1&size=7");
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


  // tests
  
  @Test
  public void whenPageRetrieved_thenNextLinkPresent() {
    
    // Expecting a page other than "last page" to have 'link' header for a 'next' relation
  
    Response response;
    try {
      response = RestAssured
        .given().accept(JSON).expect().statusCode(200)
        .when().get(apiServerUri+"?page=1&size=2");
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
    
    assertTrue(parts[0].contains("/items?page=2&size=2"), "Wrong 'next' link url");
    assertTrue(parts[1].equals("rel=\"next\""), "Wrong link relation value");
  }


  // tests
  
  @Test
  public void whenItemRetrieved_thenItemObjectPresent() {
    
    // Expecting an item with id 13402
  
    Response response;
    try {
      response = RestAssured
        .given().accept(JSON).expect().statusCode(200)
        .when().get(apiServerUri+"/13402");
    }
    catch( Throwable ex) {
      fail(ex.toString());
      return;
    }
  
    //String content = response.getBody().prettyPrint();
    
    ObjectMapper mapper = new ObjectMapper();       

    // Unmarshal the JSON response body as an instance of InventoryItemModel
    ObjectReader reader = mapper.readerFor(InventoryItemModel.class);
    try {
      InventoryItemModel item = reader.readValue(response.getBody().asInputStream());
      log.info("Got InventoryItemModel with id: "+item.getId());
    }
    catch (IOException e) {
      fail(e);
    }
    
  }


  // tests
  
  @Test
  public void whenPageRetrieved_thenItemListPresent() {
    
    // Expecting a page of 2 items
  
    Response response;
    try {
      response = RestAssured
        .given().accept(JSON).expect().statusCode(200)
        .when().get(apiServerUri+"?page=1&size=2");
    }
    catch( Throwable ex) {
      fail(ex.toString());
      return;
    }
  
    //String content = response.getBody().prettyPrint();
    
    // Get the 'content' part of the response page
    JsonPath path = response.jsonPath();
    String content = path.getString("content");
    assertNotNull(content, "Missing response page content");

    // Unmarshal the JSON response page content as a List of InventoryItemModel
    ObjectMapper mapper = new ObjectMapper();       
    ObjectReader reader = mapper.readerForListOf(InventoryItemModel.class);
    try {
      List<InventoryItemModel> items = reader.readValue(content);
      log.info("Got InventoryItemModel list of length: "+items.size());
    }
    catch (IOException e) {
      fail(e);
    }

  }

}
