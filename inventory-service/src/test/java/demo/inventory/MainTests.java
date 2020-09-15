package demo.inventory;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import demo.inventory.api.kafka.OrderCompletionListener;

/**
 * Unit tests for actuator endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = {Main.class})
@EnableAutoConfiguration(exclude={KafkaAutoConfiguration.class})
public class MainTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    OrderCompletionListener orderService;

    @Test
    public void testHealthEndpoint() throws Exception {
        this.mockMvc.perform(get("/actuator/health"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("UP")));
    }

    @Test
    public void testLivenessEndpoint() throws Exception {
        this.mockMvc.perform(get("/actuator/liveness"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("UP")));
    }

    @Test
    public void testMetricsEndpoint() throws Exception {
        // access a page
        testLivenessEndpoint();

        this.mockMvc.perform(get("/actuator/metrics"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("jvm.memory.used")));
    }

    @Test
    public void testPrometheusEndpoint() throws Exception {
        // access a page
        testLivenessEndpoint();

        this.mockMvc.perform(get("/actuator/prometheus"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("# TYPE jvm_buffer_count_buffers gauge")));
    }

}
