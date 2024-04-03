# liquibase-unexpected-changeset-detector-spring-boot-autoconfigure

Small Spring Boot autoconfiguration library that checks for unexpected Liquibase changesets in the database.

An unexpected changeset is one that ran according to the ```databasechangelog``` table, but is unknown to the current version of the application. This typically happens when a more recent version of the application was previously deployed somewhere, e.g. on a test system.

Out of the box, Liquibase will only detect differences in known changesets (e.g., different checksum).

Upon detection of an unexpected changeset, this library throws an ```ApplicationContextException```, causing the Spring application context initialization to fail.

### Usage
Simply add this library as a dependency to your Spring Boot project:

```
<dependency>
    <groupId>io.github.wicked539</groupId>
    <artifactId>liquibase-unexpected-changeset-detector-spring-boot-autoconfigure</artifactId>
    <version>0.2.0</version>
</dependency>
```

When Liquibase is on the classpath, this library will perform it's check during application context initialization.