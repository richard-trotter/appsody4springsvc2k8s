package inventory.api.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;

@ActiveProfiles(profiles = "test")
@SpringBootTest
@WebAppConfiguration
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude={KafkaAutoConfiguration.class})
public class InventoryControllerTest {

  private static final Logger log = LoggerFactory.getLogger(InventoryControllerTest.class);

  @Autowired
  MockMvc mockMvc;
  
  @Autowired
  InventoryController controller;
  
  @Test
  public final void whenContextIsBootstrapped_thenOkReady() {
    assertTrue(mockMvc != null, "Missing MockMvc bean");  
    assertTrue(controller != null, "Missing InventoryController bean");
  }

  // TODO: add test for get item by id
  // TODO: add test coverage for error cases
  
  @Test
  public void whenGetItems_thenOkNotEmpty() throws Exception {
  
      MockHttpServletResponse response = mockMvc
          .perform(
              get("/inventory/items")
              .contentType("application/json"))
          .andExpect(status().isOk())
          .andReturn()
          .getResponse();
      
      String body = response.getContentAsString();
      assertTrue(body.contains("Punched-card tabulating machines"), "Missing expected items"); 
  }

  @Test
  public void whenGetItemsPage_thenOkIsHalResponse() throws Exception {
  
      MockHttpServletResponse response = mockMvc
          .perform(
              get("/inventory/items?page=1&size=2")
              .accept(MediaType.APPLICATION_JSON_VALUE))
          .andExpect(status().isOk())
          .andReturn()
          .getResponse();
      
      String body = response.getContentAsString();
      
      ObjectMapper mapper = new ObjectMapper();

      ObjectReader reader = mapper.readerFor(PagedModel.class);

      // verify presence of top level elements
      JsonNode root = reader.readTree(body);
      for( String label : new String[] {"_embedded", "_links", "page"} )
        assertNotNull(root.get(label), "missing response child node: "+label);

      // verify correct number of inventory items for page size
      String label = "inventoryItemResourceList";
      JsonNode node = root.get("_embedded").get(label);
      assertNotNull(node, "missing response node: "+label);
      assertTrue(node instanceof ArrayNode, "ArrayNode expected for: "+label);
      assertEquals(2, ((ArrayNode)node).size(), "Wrong items page response size for: "+label);
      
      // sniff test for next link
      label = "next";
      node = root.get("_links").get(label);
      assertNotNull(node, "missing response node: "+label);
      
      // sniff test for page metadata
      label = "number";
      node = root.get("page").get(label);
      assertNotNull(node, "missing response node: "+label);         
  }

  
  @Test
  public void whenGetAllItems_thenOkResponseIsPage1() throws Exception {

    String halContentType = "application/hal+json";

    int pageSize = InventoryController.DEFAULT_PAGE_SIZE;

    // execute request and verify response status and content type
    MockHttpServletResponse response = mockMvc.perform(get("/inventory/items").accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(result -> halContentType.equals(result.getResponse().getHeader(HttpHeaders.CONTENT_TYPE)))
        .andReturn().getResponse();

    String body = response.getContentAsString();

    ObjectMapper mapper = new ObjectMapper();
    ObjectReader reader = mapper.readerFor(PagedModel.class);
    JsonNode root = reader.readTree(body);

    // verify presence of top level 'page' element
    String label = "page";
    JsonNode node = root.get(label);
    assertNotNull(node, "missing response child node: " + label);

    // verify page size
    label = "size";
    node = node.get(label);
    assertNotNull(node, "missing response child node: " + label);
    assertTrue(node instanceof IntNode, "IntNode expected for: " + label);
    assertEquals(pageSize, ((IntNode) node).asInt(), "Wrong items page response size");
  }

}