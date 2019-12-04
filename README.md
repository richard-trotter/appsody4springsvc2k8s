# Using Appsody in Spring Microservice Development for Kubernetes

## Introduction

This is a sample application motivated by the following [IBM Cloud Architectures](https://www.ibm.com/cloud/garage/architectures) article.

**Microservices with Kubernetes (Spring)**    
[https://www.ibm.com/cloud/garage/architectures/microservices/microservices-kubernetes](https://www.ibm.com/cloud/garage/architectures/microservices/microservices-kubernetes)

A reference implementation of the architecture described in that article is available here:
[https://github.com/ibm-cloud-architecture/refarch-cloudnative-kubernetes/tree/spring](https://github.com/ibm-cloud-architecture/refarch-cloudnative-kubernetes/tree/spring)

The present sample is an implementation of the "inventory" microservice identified from that solution architecture, and originates from that earlier sample implementation. 

Similarities of note:
* leverages [Spring Boot](https://projects.spring.io/spring-boot/) framework to build a micro-service application
* uses [Spring Data JPA](http://projects.spring.io/spring-data-jpa/) to persist data to an inventory database

This alternative implementation differs as follows. 

* makes use of [Appsody](https://appsody.dev) to assist development for cloud-native deployment
* illustrates the use of an IBM Event Streams Spring Boot "starter" to assist Event Streams service integration 
* uses IBM DB2 database, as an alternative to MySQL
* represents a "solution component developer" role bias, assuming that the solution CI/CD pipeline and integration test cluster setup are the responsibility of some other role  

This sample was bootstrapped into existence with Appsody, using the Spring Boot Appsody stack.

    appsody init java-spring-boot2 

This produces all of the source supporting a minimal (or "starter") Spring Boot application, which can than be deployed to a Kubernetes cluster with `appsody deploy`. That deployment involves use of the [Appsody operator](https://github.com/appsody/appsody-operator/blob/master/doc/user-guide.md) for K8s, and the `AppsodyApplication` K8s custom resource definition. In other words, the sample application is deployed to K8s as a K8s Custom Resource of type AppsodyApplication. 

**NOTE: THIS IS A PLAIN SPRING BOOT BASELINE VERSION** 

## REST API

There is a simple REST API available to exercise the running application. An example `curl` session is shown below, where we a) query current stock level for a selected inventory item, b) simulate order fulfillment for a count of 1 item, and c) query stock level again to verify stock level change.   

    $ curl -w "\nSTATUS: %{http_code}\n" http://localhost:8080/demo/inventory/item/13401
    {"id":13401,"name":"Dayton Meat Chopper",..."stock":998,...
    STATUS: 200

    $ curl -w "STATUS: %{http_code}\n" -X POST 'http://localhost:8080/demo/inventory/order?itemId=13401&count=1'
    STATUS: 200

    $ curl -w "\nSTATUS: %{http_code}\n" http://localhost:8080/demo/inventory/item/13401
    {"id":13401,"name":"Dayton Meat Chopper",..."stock":997,...
    STATUS: 200

A script is provided to automate this request sequence, given a `hostport` parameter: `scripts/do_order_item.sh`.

## Messaging API

Step (b) above, where we simulate order fulfillment, actually involves the posting of a `order request` message to a `orders` Kafka topic. As a Kafka listener, then, our sample here receives an `order request` message. The change in current stock level is performed by the `order request` notification handler. 

## Running the sample

The sample is built for execution within a local development scenario, component test, and integration test. The expectation is that DB2 and Event Streams service integration configuration may be different for development and test. The default configuration is for integration test. To run the development configuration, use the Spring `dev` configuration profile (e.g.: run with `--spring.profiles.active=dev`). A template configuration file is provided to assist setup for `dev` overrides: `application-dev-template.yml`. Copy this file to `application-dev.yml` and replace variable references with your service integration configuration parameters.

## Test data setup

With the `dev` configuration profile active, a TopicConfiguration bean will create the required `orders` topic as needed. 

The sample requires a single 'items' table for the persisted inventory model. A `db2_ddl.sql` script is provided for creation of this table. A `db2_data.sql` script is also provided for seeding this `items` table with sample data. With the `dev` configuration profile active, however, an ItemsBuilder bean will create the `items` table data on startup if needed - and recreate that data if it already exists. 

## Unit Test

The Appsody starter application includes implementations for a K8s `livenessProbe` and a K8s `readinessProbe`. These probes are implemented using the Spring Boot `actuator` framework. The starter application also includes a set of unit tests for the actuator endpoints, in: `MainTests.java`. For unit test, we don't want to include any external service integration dependencies. A `unittest` Spring profile is used to enable configuration for unit test. When unit test is run, the external Event Streams and DB2 dependencies are disabled by configuration.

## References

1. IBM Developer [Spring Tutorials](https://developer.ibm.com/tutorials/category/spring/)
1. Programming with Java on IBM Cloud: [Configuring the Spring environment](https://cloud.ibm.com/docs/java?topic=java-spring-configuration)

--- 
