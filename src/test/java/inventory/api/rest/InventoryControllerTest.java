package inventory.api.rest;

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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;

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
              get("/inventory/item")
              .contentType("application/json"))
          .andExpect(status().isOk())
          .andReturn()
          .getResponse();
      
      String body = response.getContentAsString();
      assertTrue(body.contains("Punched-card tabulating machines"), "Missing expected items"); 
  }

}