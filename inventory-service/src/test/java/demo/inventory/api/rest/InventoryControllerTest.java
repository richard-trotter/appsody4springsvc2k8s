package demo.inventory.api.rest;

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
import org.springframework.data.domain.Page;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;

import demo.inventory.api.model.InventoryItemModel;
import demo.inventory.api.rest.InventoryController;

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

  // TODO: add test coverage for error cases

  @Test
  public void whenGetSelectedItem_thenOkNotEmpty() throws Exception {

    int itemId = 2;
    
    MockHttpServletResponse response = mockMvc.perform(
          get("/inventory/items/"+itemId).accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk()).andReturn().getResponse();

    String body = response.getContentAsString();
    ObjectMapper mapper = new ObjectMapper();       
    ObjectReader reader = mapper.readerFor(InventoryItemModel.class);
    
    InventoryItemModel item = reader.readValue(body);
    
    assertEquals( itemId, item.getId(), "wrong InventoryItemModel id" );
  }


  @Test
  public void whenGetItemsPage_thenOkAndHasLink() throws Exception {

    MockHttpServletResponse response = mockMvc
        .perform(get("/inventory/items?page=1&size=2").accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk()).andReturn().getResponse();

    String body = response.getContentAsString();
    ObjectMapper mapper = new ObjectMapper();
    ObjectReader reader = mapper.readerFor(Page.class);

    // verify presence of top level elements
    JsonNode root = reader.readTree(body);

    for (String label : new String[] { "content", "pageable" })
      assertNotNull(root.get(label), "missing response child node: " + label);

    // verify correct number of inventory items for page size
    String label = "content";
    JsonNode node = root.get(label);
    assertNotNull(node, "missing response node: " + label);
    assertTrue(node instanceof ArrayNode, "ArrayNode expected for: " + label);
    assertEquals(2, ((ArrayNode) node).size(), "Wrong items page response size for: " + label);

    // sniff test for next link
    String linkValue = response.getHeader(HttpHeaders.LINK);
    assertNotNull(linkValue, "missing Link response header");

    Link nextLink = Link.valueOf(linkValue);
    // String href= nextLink.getHref();
    assertTrue(IanaLinkRelations.NEXT.isSameAs(nextLink.getRel()), "wrong link relation: " + nextLink.getRel());

    // sniff test for page metadata
    label = "pageable";
    node = root.get(label);
    assertNotNull(node, "missing response node: " + label);
  }

  
  @Test
  public void whenGetAllItems_thenOkResponseIsPage1() throws Exception {

    int expectedPageSize = InventoryController.DEFAULT_PAGE_SIZE;
    int expectedPageNumber = 0;

    // execute request and verify response status and content type
    MockHttpServletResponse response = mockMvc.perform(get("/inventory/items").accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk()).andReturn().getResponse();

    String body = response.getContentAsString();

    ObjectMapper mapper = new ObjectMapper();
    ObjectReader reader = mapper.readerFor(Page.class);
    JsonNode root = reader.readTree(body);

    // verify presence of top level 'pageable' element
    String label = "pageable";
    JsonNode pageableNode = root.get(label);
    assertNotNull(pageableNode, "missing response child node: " + label);

    // verify page number
    label = "pageNumber";
    JsonNode node = pageableNode.get(label);
    assertNotNull(node, "missing response child node: " + label);
    assertTrue(node instanceof IntNode, "IntNode expected for: " + label);
    assertEquals(expectedPageNumber, ((IntNode) node).asInt(), "Wrong items response page number");

    // verify page size
    label = "pageSize";
    node = pageableNode.get(label);
    assertNotNull(node, "missing response child node: " + label);
    assertTrue(node instanceof IntNode, "IntNode expected for: " + label);
    assertEquals(expectedPageSize, ((IntNode) node).asInt(), "Wrong items page response size");
  }

}