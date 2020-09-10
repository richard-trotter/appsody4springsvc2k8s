package demo.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class InventorySvcEnvPostProcessor implements EnvironmentPostProcessor {

    private static final String SECRETS_ENABLED_PROPERTY = "spring.cloud.kubernetes.secrets.enabled";
    private static final String SECRETS_PATHS_PROPERTY = "spring.cloud.kubernetes.secrets.paths";

    /**
     * Print out key config properties that may be used during Spring init
     * (Note that logging has not been initialized at this point)
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

        String pfx = "[EnvironmentPostProcessor] ";

        String[] plist = {
                SECRETS_ENABLED_PROPERTY,
                SECRETS_PATHS_PROPERTY
        };

        for (String pname : plist)
            System.out.println(pfx + pname + ": " + environment.getProperty(pname));

        if (!Boolean.TRUE.toString().equals(environment.getProperty(SECRETS_ENABLED_PROPERTY)))
            return;

        System.out.println(pfx + "Checking secrets ...");
        String[] spaths = environment.getProperty(SECRETS_PATHS_PROPERTY).split(",");
        for (String spath : spaths) {
            File d = new File(spath);
            if (d.exists()) {
                System.out.println(pfx + "Found secret: " + spath);
                File[] items = d.listFiles(item -> item.isFile());
                StringBuffer buf = new StringBuffer();
                for (File item : items) {
                    try {
                        if (item.getName().contains("password"))
                            buf.append(pfx + item.getName()).append("=")
                                    .append("<masked>").append("\n");
                        else
                            buf.append(pfx + item.getName()).append("=")
                                    .append(new String(Files.readAllBytes(Paths.get(item.getPath())))).append("\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println(buf);
            } else {
                System.out.println(pfx + "[ERROR] Missing secret: " + spath);
            }

        }
    }

}