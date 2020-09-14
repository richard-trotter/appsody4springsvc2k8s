
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

To run the live api test locally:

``` bash
% cd inventory-client-test
% mvn -Dspring.profiles.active=live-api-test surefire:test
```



