package inventory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * If the 'dev' profile is active, enable auto init on startup of the required 'items' table.
 * The contents of the table is recreated on startup as a convenience, to restore any items
 * removed during a previous test run.
 */
@Component
@Profile("dev")
public class ItemsBuilder {
  static final Logger logger = LoggerFactory.getLogger(ItemsBuilder.class);

  private JdbcTemplate jdbcTemplate;

  public ItemsBuilder(DataSource dataSource) {
      this.jdbcTemplate = new JdbcTemplate(dataSource);
  }
  
  @PostConstruct
  public void createItems() throws IOException {
    
    Long itemsCount = jdbcTemplate.queryForObject("select count(*) from SYSCAT.TABLES where TABNAME='ITEMS'", Long.class);
    if( itemsCount > 0 ) {
      logger.info("Found 'items' database table.");
      logger.info("Dropping 'items' database table.");
      jdbcTemplate.execute("drop table items");
    }
    
    logger.info("Creating database table 'items'");

    String createTable = "create table items ("
            +"id int not null GENERATED ALWAYS AS IDENTITY (START WITH 13401) primary key,"
            +"stock int not null,"
            +"name varchar(100) not null,"
            +"description varchar(2048) not null,"
            +"price decimal(8,2) not null,"
            +"img_alt varchar(75),"
            +"img varchar(50) not null)";

    jdbcTemplate.execute(createTable);
    
    try(InputStream inputStream = ItemsBuilder.class.getClassLoader().getResourceAsStream("db2_data.sql")) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
      reader.lines().forEach(s -> jdbcTemplate.execute(s));
    }
    
    itemsCount = jdbcTemplate.queryForObject("select count(*) from ITEMS", Long.class);
    logger.info("Found "+itemsCount+" 'items'");
  }
}