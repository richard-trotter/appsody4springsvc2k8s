
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
% mvn test
```

To run the live api test locally against a remote service, specify the `serverUri` system property:

``` bash
% cd inventory-client-test
% mvn test -DserverUri=http://192.168.1.66:8080
```


