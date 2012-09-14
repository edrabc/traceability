# traceability

## Introduction

A simple Traceability module that offers out-of-the-box interactions with MVC, JMS and other Java related technologies, so a unique transaction ID could be shared and traced across the logs on different systems.

## Features

TODO

## Build it!

* Clone the git repository:

        $ git clone git@github.com:edrabc/traceability.git

* Install the JAR artifact in your local repository:

        $ cd traceability
        $ mvn clean install

## Usage

In order to use traceability module feature, **pick your poison** and follow the instructions:

### Servlet Filter + Logback MDC

TODO

### Spring MVC + Logback MDC

If you prefer to use Spring MVC interceptors to set the Transaction ID, first of all you will need an explicit declaration of the dependency:

```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-webmvc</artifactId>
    <version>${org.springframework-version}</version>
    <scope>compile</scope>
    <exclusions>
        <exclusion>
            <artifactId>commons-logging</artifactId>
            <groupId>commons-logging</groupId>
        </exclusion>
    </exclusions>
</dependency>
```

Depending on the desired behaviour, the Transaction ID could be traced from several sources:

- HTTP Header: if every request includes an special header with a unique session or transaction ID, add the following configuration to your context file:

```xml
<bean class="traceability.logback.spring.mvc.HttpHeaderSpringInterceptor">
    <property name="headerName" value="x-transaction" />
    <property name="mdcKey" value="x-transaction" />
</bean>
```

- Authorized User: if every request requires a previous authorization step, simply add the following configuration to your context file, so the username is automatically injected in the MDC:

```xml
<bean class="traceability.logback.spring.mvc.PrincipalSpringInterceptor">
    <property name="mdcKey" value="transaction" />
</bean>
```

**Heads Up**: all the logs and code that runs before Spring Dispatcher Servlet (e.g. Spring Security or Servlet Filters) will not be traced, because the real injection of the transaction ID is done once the request hits the Controller layer.

### Spring JMS + Logback MDC

Messages sent to a JMS server could be easily traced, including the Transaction ID in the message headers. If you are using Spring JMS Framework, add the following dependencies in your project:

```xml
<dependency>
    <groupId>org.apache.geronimo.specs</groupId>
    <artifactId>geronimo-jms_1.1_spec</artifactId>
    <version>1.1</version>
    <scope>compile</scope>
</dependency>

<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-jms</artifactId>
    <version>${org.springframework-version}</version>
    <scope>compile</scope>
</dependency>
```

To inject the MDC Transaction ID when using `JmsTemplate`, set a `TraceableMessagePostProcessor` when sending the message:

```java
jmsTemplate.convertAndSend(destination, body, new TraceableMessagePostProcessor());
```

### Apache CXF + Logback MDC

Requests to SOAP webservices using Apache CXF could be easily traced, including the Transaction ID in the envelope header. If you are using Apache CXF, add the following dependencies in your project:

```xml
<dependency>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-rt-bindings-soap</artifactId>
    <version>${org.apache.cxf-version}</version>
    <scope>compile</scope>
</dependency>
```

**Note**: As pointed out in http://cxf.apache.org/docs/interceptors.html, there are several ways to configure interceptors in CXF. One of the most common is to configure traceability through configuration:

```xml
<bean id="traceableHeader" class="traceability.logback.cxf.TraceableHeaderSoapInterceptor">
    <property name="soapKey" value="ConsumerTransactionID" />
    <property name="namespace" value="http://webservice/core_1" />
</bean>

<cxf:bus>
    ...
    <cxf:outInterceptors>
        <ref bean="traceableHeader"/>
    </cxf:outInterceptors>
</cxf:bus>
```

### Jersey + Logback MDC

TODO

### ... Log4J MDC

TODO

## Troubleshooting

Nothing so far...

## How to Contribute

Found a bug? Use the GitHub Issue tracker to let us know them.

Want to contribute? You are very much encouraged and invited to contribute back your modifications to the API, preferably in a Github fork, of course. 

But, please keep the following in mind:

- Contributions will not be accepted without tests.
- If you are creating a small fix or patch to an existing feature, just a simple test will do. Please stay in the confines of the current test suite.
- If it is a brand new feature, make sure to create a new test suite. Also, whipping up some documentation in README.md files would be appreciated.

Have questions? Soon...
