package inventory.api.rest;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Random;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import inventory.jpa.InventoryItem;

@SpringBootTest
@WebAppConfiguration
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
public class InventoryControllerTest {

  @Autowired
  MockMvc mockMvc;
  
  @Autowired
  InventoryController controller;
  
  @Test
  public final void whenContextIsBootstrapped_thenOkReady() {
    assertTrue(mockMvc != null, "Missing MockMvc bean");  
    assertTrue(controller != null, "Missing InventoryController bean");
  }

  // TODO: improve response validation using mapped json objects
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

  @Test
	public void testMarshalToJson() throws Exception {
		final InventoryItem inv = new InventoryItem();
		final Random rnd = new Random();

		long id = rnd.nextLong();
		int price = rnd.nextInt();
		int stock = rnd.nextInt();

		final ObjectMapper mapper = new ObjectMapper();


		inv.setId(id);
		inv.setName("myInv");
		inv.setDescription("Test inventory description");
		inv.setImg("/image/myimage.jpg");
		inv.setImgAlt("image alt text");
		inv.setPrice(price);
		inv.setStock(stock);


		final String json = mapper.writeValueAsString(inv);

		// construct a json string with the above properties

		final StringBuilder myJsonStr = new StringBuilder();

		myJsonStr.append("{");
		myJsonStr.append("\"id\":").append(id).append(",");
		myJsonStr.append("\"name\":").append("\"myInv\"").append(",");
		myJsonStr.append("\"description\":").append("\"Test inventory description\"").append(",");
		myJsonStr.append("\"img\":").append("\"/image/myimage.jpg\"").append(",");
		myJsonStr.append("\"imgAlt\":").append("\"image alt text\"").append(",");
		myJsonStr.append("\"stock\":").append(stock).append(",");
		myJsonStr.append("\"price\":").append(price);
		myJsonStr.append("}");

		final String myJson = myJsonStr.toString();
		System.out.println("Marshalled Inventory to JSON:" + myJson);
		System.out.println("My JSON String:" + myJson);

		final JsonNode jsonObj = mapper.readTree(json);
		final JsonNode myJsonObj = mapper.readTree(myJson);


		assert(jsonObj.equals(myJsonObj));

	}

	@Test
	public void testMarshalFromJson() throws Exception {
		final Random rnd = new Random();

		long id = rnd.nextLong();
		int price = rnd.nextInt();
		int stock = rnd.nextInt();

		final ObjectMapper mapper = new ObjectMapper();

		// construct a json string with the above properties

		final StringBuilder myJsonStr = new StringBuilder();

		myJsonStr.append("{");
		myJsonStr.append("\"id\":").append(id).append(",");
		myJsonStr.append("\"name\":").append("\"myInv\"").append(",");
		myJsonStr.append("\"description\":").append("\"Test inventory description\"").append(",");
		myJsonStr.append("\"img\":").append("\"/image/myimage.jpg\"").append(",");
		myJsonStr.append("\"imgAlt\":").append("\"image alt text\"").append(",");
		myJsonStr.append("\"stock\":").append(stock).append(",");
		myJsonStr.append("\"price\":").append(price);
		myJsonStr.append("}");

		final String myJson = myJsonStr.toString();
		System.out.println("My JSON String:" + myJson);

		// marshall json to Inventory object

		final InventoryItem inv = mapper.readValue(myJson, InventoryItem.class);

		// make sure all the properties match up
		assert(inv.getId() == id);
		assert(inv.getName().equals("myInv"));
		assert(inv.getDescription().equals("Test inventory description"));
		assert(inv.getImg().equals("/image/myimage.jpg"));
		assert(inv.getImgAlt().equals("image alt text"));
		assert(inv.getStock() == stock);
		assert(inv.getPrice() == price);


	}

}