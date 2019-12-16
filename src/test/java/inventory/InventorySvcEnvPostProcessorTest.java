package inventory;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockEnvironment;

public class InventorySvcEnvPostProcessorTest {
    InventorySvcEnvPostProcessor inventorySvcEnvPostProcessor = new InventorySvcEnvPostProcessor();

    @Test
    public void postProcessEnvironment() {
        ConfigurableEnvironment environment = new MockEnvironment();
        SpringApplication application = Mockito.mock(SpringApplication.class);
        inventorySvcEnvPostProcessor.postProcessEnvironment(environment, application);
    }

}