# Using Appsody in Spring Microservice Development for Kubernetes

## Introduction

This is a sample application motivated by the following [IBM Cloud Architectures](https://www.ibm.com/cloud/architecture/architectures/microservices) article, which references an archetype "Storefront Shopping" application and its supporting "Inventory" microservice.

[Microservices with Kubernetes (Spring)](https://www.ibm.com/cloud/architecture/architectures/microservices-with-kubernetes-spring-solution)

A reference implementation of the architecture described in that article is available here:  
[ibm-cloud-architecture/refarch-cloudnative-kubernetes](https://github.com/ibm-cloud-architecture/refarch-cloudnative-kubernetes/tree/spring)

The present sample is an implementation of the "inventory" microservice identified from that solution architecture, and originates from that earlier sample implementation. 

Similarities of note:
* leverages [Spring Boot](https://projects.spring.io/spring-boot/) framework to build a micro-service application
* uses [Spring Data JPA](http://projects.spring.io/spring-data-jpa/) to persist data to an inventory database

This alternative implementation differs as follows. 

* makes use of [Appsody](https://appsody.dev) to assist development for cloud-native deployment
* uses `spring-kafka` to simplify integration as a Kafka Consumer
* provides Kafka Consumer configuration via Spring application properties
* uses `spring-kafka-test` to provide an embedded Kafka implementation for `mvn test`
* uses `h2` to provide an embedded relational database implementation for `mvn test`
* uses `spring-hateos` and `spring-data-common` to provide a pageable "items" query response and "next link" response header
* represents a "solution component developer" role bias, assuming that the solution CI/CD pipeline and integration test cluster setup are the responsibility of some other role  

This sample was bootstrapped into existence with Appsody, using the Spring Boot Appsody stack.

    appsody init incubator/java-spring-boot2 

That bootstrapping produces all of the source supporting a minimal (or "starter") Spring Boot application, which can than be deployed to a Kubernetes cluster with `appsody deploy`. That deployment involves use of the [Appsody operator](https://github.com/appsody/appsody-operator/blob/master/doc/user-guide.md) for K8s, and the `AppsodyApplication` K8s custom resource definition. In other words, the sample application is deployed to K8s as a K8s Custom Resource of type **AppsodyApplication**. 

## REST API

There is a simple REST API available to exercise the running application. An example `curl` session is shown below, where we a) query current stock level for a selected inventory item, b) simulate order fulfillment for a count of 1 item, and c) query stock level again to verify stock level change.   

    $ curl -w "\nSTATUS: %{http_code}\n" http://localhost:8080/demo/inventory/item/13401
    {"id":13401,"name":"Dayton Meat Chopper",..."stock":998,...
    STATUS: 200

    $ curl -w "STATUS: %{http_code}\n" -X POST 'http://localhost:8080/demo/util/order?itemId=13401&count=1'
    STATUS: 200

    $ curl -w "\nSTATUS: %{http_code}\n" http://localhost:8080/demo/inventory/item/13401
    {"id":13401,"name":"Dayton Meat Chopper",..."stock":997,...
    STATUS: 200

A script is provided to automate this request sequence, given a `hostport` parameter: `scripts/do_order_item.sh`.

## Messaging API

Step (b) above, where we simulate order fulfillment, actually involves the posting of a `order completed` message to a `orders` Kafka topic. As a Kafka listener, then, our sample here receives an `order completed` message. The change in current stock level is performed by the `order completed` notification handler. 

## Running the sample

The sample is built for execution within a local development scenario, component test, and integration test. The expectation is that DB2 and Kafka service integration configuration may be different for development and test. The default configuration is for integration test. To run the development configuration, use the Spring `dev` configuration profile (e.g.: run with `--spring.profiles.active=dev`). A template configuration file is provided to assist setup for `dev` overrides: `application-dev-template.yml`. Copy this file to `application-dev.yml` and replace variable references with your service integration configuration parameters.

## Test data setup

With the `dev` configuration profile active, a TopicConfiguration bean will cause the required `orders` topic to be created by `spring-kafka`. 

The sample requires a single 'items' table for the persisted inventory model. A `db2_ddl.sql` script is provided for creation of this table. A `insert_sample_data.sql` script is also provided for seeding this `items` table with sample data. With the `dev` configuration profile active, however, an ItemsBuilder bean will create the `items` table data on startup if needed - and recreate that data if it already exists. 

## Unit Test

The Appsody starter application includes implementations for a K8s `livenessProbe` and a K8s `readinessProbe`. These probes are implemented using the Spring Boot `actuator` framework. The starter application also includes a set of unit tests for the actuator endpoints, in: `MainTests.java`. 

There are a small set of tests for the application's `RestController` and `KafkaListener` as well. 

## Running the sample in a Kubernetes cluster

Solution *integration test* is expected to occur in a shared Kubernetes (K8s) cluster. However, it's a good practice for the cloud native microservice developer to perform functional test of the component, as deployed within a K8s cluster, before delivery of code to the shared code repository - and thereby triggering CI/CD. This is consistent with "Out of Process Component Test", as described here:

    https://martinfowler.com/articles/microservice-testing/#testing-component-out-of-process-diagram

This article presumes that solution *component test* occurs in a local K8s cluster, such as the cluster within a local Docker Desktop installation. 

The Appsody default, or "starter", K8s configuration can be produced as a file - `app-deploy.yaml` - using `appsody deploy --generate-only`. To support this sample, there are a few defaults in this configuration that we change or add here. 

1. The AppsodyApplication name, which appears when the command `kubectl get AppsodyApplication` is run: `metatdata.name`.
1. The docker image name: `spec.applicationImage`
1. The K8s docker image "pull policy": `spec.pullPolicy`
1. The service account used by K8s for pulling the docker image: `spec.serviceAccountName`
1. The volume mounts used for K8s Secrets acquisition
1. The context path for Spring Boot actuator endpoints

### Specifying the AppsodyApplication name

By default, Appsody uses your project name (the name of the directory containing your `app-deploy.yaml` file) as the AppsodyApplication name. We decouple those names here, by changing the value of `metadata.name`.

### Specifying docker image properties

The default docker image name is also derived from your project name, and that is decoupled here as well. In addition, to support deployment to Docker Desktop Kubernetes, we specify `pullPolicy: Never`.  This will ensure that K8s always (and only) uses the local docker image registry. 

When deploying to a remote K8s cluster, some alternatives to this configuration are required and are described below.  

### Service integration configuration acquisition for Kubernetes

When deploying for component test and integration test, the service integration configurations would likely be different in each case - but the method of accessing these configurations (K8s Secrets) is the same in both cases. 

A K8s `kustomization-template.yml` file is provided (as illustration) to assist in creation of the required Secrets. Copy this to `kustomization.yml` and fill in the configuration values. Then create the Secrets using `kubectl apply -k`. 

This sample application acquires the Secrets via K8s volume mounts. The AppsodyApplication configuration produced by `appsody init` does not include the K8s volume mounts required to acquire the K8s Secrets needed by this sample, but that version of `app-deploy.yaml` has been modified here to include that information.

The individual Secret data items are used to resolve configuration placeholders found within `application.yml`.  This is all done by Spring Cloud Kubernetes (SCK) and Spring Boot. 

The application enables SCK K8s Secrets mapping, and declares a set of Secrets paths (consistent with defined Secrets and volumes), as shown here (from `bootstrap.yml`):

    spring:
      cloud:
        kubernetes:
          secrets:
            enabled: true
            paths: /etc/secrets/messagebroker,/etc/secrets/inventorydb

SCK then maps the contents of each file under these parent directories into a configuration property. And Spring Boot uses those values to resolve placeholders found within `application.yml`.

For example. Suppose our `application.yml` declares:

    spring:
      datasource:
        username: ${inventorydb.username}

And our `kustomization.yml` declares a Secret and data item as shown here:

    secretGenerator:
    - name: inventory-db-access
      literals:
      - inventorydb.username=vsv20760

And then our `app-deploy.yaml` specifies:

    volumes:
    - name: db-secrets
      secret:
        secretName: inventory-db-access-9b2fk8th2d

and also:

    volumeMounts:
    - mountPath: /etc/secrets/inventorydb
      name: db-secrets

Then K8s will ensure that the file `/etc/secrets/inventorydb/inventorydb.username` exists and contains the value `vsv20760`. And SCK will create a Spring environment property named `inventorydb.username` with this value. And Spring Boot will resolve `spring.datasource.username` as `vsv20760`.

For verification of intended configuration, this sample implements a Spring ApplicationReady event listener which logs those Secrets items which specify service implementation locations. 

    [main] inventory.Main: [ApplicationReady] spring.datasource.url: jdbc:db2://dashdb-txn-sbox-yp-dal09...
    [main] inventory.Main: [ApplicationReady] spring.kafka.bootstrap-servers: broker-0-1cz91wl3680d247s.kafka....

### Using Appsody deploy

All of the changes for the starter application's AppsodyApplication configuration mentioned above are in place in this sample's version of `app-deploy.yaml`. 
  
With Docker Desktop Kubernetes enabled, run:

    appsody deploy --tag inventory-svc:latest
    
Appsody will perform a local `docker build` and Docker Desktop Kubernetes will use the image from the local Docker registry.

As shown in this example, note that the image `tag` specified (for `appsody deploy`) must match the value used in the AppsodyApplication configuration. The former will be applied to the docker image in the Docker registry, and the latter is used by the Appsody operator to locate that image. Near the end of execution, you should see the following message.

    Built docker image inventory-svc:latest
 
On completion make note of the `hostport` part of the informational message produced, as in this example:

    Deployed project running at http://localhost:30804

The application within the K8s pod may not have completed initialization immediately. Verify the pod status is `Running` before proceeding, as in this example:

    $ kubectl get pods
    NAME                                READY   STATUS    RESTARTS   AGE
    appsody-operator-586db784fc-6zvh7   1/1     Running   2          6d22h
    inventory-svc-555f84748d-jpm9v      1/1     Running   0          25s
     
Follow the instructions above for exercising the sample app's REST API and KafkaListener with `do_order_item.sh`.

To remove the deployed AppsodyApplication from your K8s cluster, run:

    appsody deploy delete

## Run in IBM Cloud Kubernetes Services (IKS)

First, ensure that your local system is setup to use IKS.
 
1. Ensure that the Docker engine is running
1. Login to IBM Cloud: `ibmcloud login --sso`
1. Configure the `kubectl` environment: `ibmcloud ks cluster config --cluster mycluster`
1. Login to IBM Cloud Container Registry (ICR): `ibmcloud cr login`

For more information, see: [Accessing Kubernetes clusters](https://cloud.ibm.com/docs/containers?topic=containers-access_cluster).

### AppsodyApplication configuration changes for IKS

Do NOT use `latest` in the image tag (`spec.applicationImage`), as this likely will not behave as you imagine. Rather, use a semantic version ID. For example: `0.1.0`. 

Ensure that you include the appropriate IBM Container Registry location and namespace. Run `ibmcloud cr info` and select the value of `Container Registry`. Run `ibmcloud cr namespaces` and select your preferred `Namespace`. Example:

    applicationImage: us.icr.io/ns-mine/inventory-svc:0.1.2

Also, specify `spec.pullPolicy: Always`, indicating that a configuration update should always pull the image from ICR. This is typically what is wanted by a developer, but an alternative may be more appropriate for other use cases.  

The ICR for IKS is configured by default to allow the `default` service account to pull images. To ensure that this is the service account in effect for the Appsody `deploy`, we've added the following to the AppsodyApplication configuration.

    serviceAccountName: default

### Deploy to IKS

Run `deploy` with the `push` and `tag` options as in this example (where the *tag* aligns with your *applicationImage*): 

    appsody deploy --push --tag us.icr.io/ns-mine/inventory-svc:0.1.2

Note that the `deploy` fails to discover the external IP address for the IKS NodePort service. To determine this IP address. Run:

    kubectl get node -o wide
    
Make note of the EXTERNAL-IP value.

To determine the mapped service port. Run:

    kubectl get service -o wide inventory-svc 

Under the PORT heading, make note of the mapping for port 8080.

Using these two values, exercise the deployed AppsodyApplication as in the local K8s scenario.

## References

1. IBM Developer [Spring Tutorials](https://developer.ibm.com/tutorials/category/spring/)
1. Programming with Java on IBM Cloud: [Configuring the Spring environment](https://cloud.ibm.com/docs/java?topic=java-spring-configuration)
1. [IBM Cloud Kubernetes Service](https://cloud.ibm.com/docs/containers?topic=containers-getting-started)

--- 
