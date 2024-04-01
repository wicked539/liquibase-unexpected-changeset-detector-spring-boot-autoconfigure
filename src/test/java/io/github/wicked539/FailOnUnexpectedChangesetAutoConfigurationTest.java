package io.github.wicked539;

import org.junit.jupiter.api.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.context.ApplicationContextException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate;
import org.springframework.test.context.support.DefaultBootstrapContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class FailOnUnexpectedChangesetAutoConfigurationTest {

    @Nested
    @Order(1)
    @SpringBootTest(classes = FailOnUnexpectedChangesetAutoConfiguration.class)
    @SpringBootApplication
    @TestPropertySource(properties = "spring.liquibase.change-log=classpath:db/changelog/db.changelog-with-two-changesets.xml")
    public class FirstLiquibaseRun {
        @Test
        public void sucessfullyUpdatesDbSchema() {

        }
    }

    @Nested
    @Order(2)
    @SpringBootApplication
    @ContextConfiguration(classes = FailOnUnexpectedChangesetAutoConfiguration.class)
    @TestPropertySource(properties = "spring.liquibase.change-log=classpath:db/changelog/db.changelog-with-one-changeset-only.xml")
    public class SecondLiquibaseRun {

        @Test
        public void throwsExceptionOnUnexpectedChangesets() {
            SpringBootTestContextBootstrapper springBootTestContextBootstrapper = new SpringBootTestContextBootstrapper();
            springBootTestContextBootstrapper.setBootstrapContext(new DefaultBootstrapContext(SecondLiquibaseRun.class, new DefaultCacheAwareContextLoaderDelegate()));

            IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () -> {
                springBootTestContextBootstrapper.buildTestContext().getApplicationContext();
            });

            assertThat(illegalStateException.getCause().getCause().getCause().getClass(), is(ApplicationContextException.class));
        }
    }
}
