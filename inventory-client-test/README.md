
This module contains a set of JUnit tests that constitute a "live api test" suite. They require that the Java system property `spring.profiles.active` is set to the value `live-api-test`, and that an instance of inventory-service is already running at: `http://localhost:8080/demo`.

To run the service under test locally: 

``` bash
% cd inventory-service
% mvn spring-boot:run
```

or:

``` bash
% cd inventory-service
% appsody run
```

To run the live api test locally against a local service under test:

``` bash
% cd inventory-client-test
% mvn test | grep -E 'INFO|Sending request.*\/inventory\/item'
```
Sample output:

```bash
% mvn test | grep -E 'INFO..(Tests|Results)|Sending request'
20:05:49.752 [main] DEBUG org.apache.http.impl.conn.DefaultClientConnection - Sending request: POST /demo/inventory/item HTTP/1.1
20:05:49.846 [main] DEBUG org.apache.http.impl.conn.DefaultClientConnection - Sending request: GET /demo/inventory/item/13414 HTTP/1.1
20:05:49.922 [main] DEBUG org.apache.http.impl.conn.DefaultClientConnection - Sending request: DELETE /demo/inventory/item/13414 HTTP/1.1
20:05:49.980 [main] DEBUG org.apache.http.impl.conn.DefaultClientConnection - Sending request: GET /demo/inventory/item?page=1&size=2 HTTP/1.1
20:05:50.021 [main] DEBUG org.apache.http.impl.conn.DefaultClientConnection - Sending request: GET /demo/inventory/item HTTP/1.1
20:05:50.405 [main] DEBUG org.apache.http.impl.conn.DefaultClientConnection - Sending request: GET /demo/inventory/item?page=1&size=2 HTTP/1.1
20:05:50.549 [main] DEBUG org.apache.http.impl.conn.DefaultClientConnection - Sending request: GET /demo/inventory/item/13402 HTTP/1.1
20:05:50.578 [main] DEBUG org.apache.http.impl.conn.DefaultClientConnection - Sending request: GET /demo/inventory/item?page=1&size=7 HTTP/1.1
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.119 s - in demo.inventory.client.InventoryControllerLiveTest
[INFO] Results:
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
```

To run the live api test locally against a remote service, specify the `serverUri` system property:

``` bash
% cd inventory-client-test
% mvn test -DserverUri=http://192.168.1.66:8080
```



