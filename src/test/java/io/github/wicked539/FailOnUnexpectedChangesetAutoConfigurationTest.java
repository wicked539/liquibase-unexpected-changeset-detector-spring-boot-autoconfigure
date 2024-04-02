package io.github.wicked539;

import org.junit.jupiter.api.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.context.ApplicationContextException;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate;
import org.springframework.test.context.support.DefaultBootstrapContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class FailOnUnexpectedChangesetAutoConfigurationTest {

    @Nested
    @Order(1)
    @SpringBootTest(classes = FailOnUnexpectedChangesetAutoConfiguration.class)
    @SpringBootApplication
    @TestPropertySource(properties = "spring.liquibase.change-log=" +
            "classpath:db/changelog/db.changelog-with-two-changesets.xml")
    public class FirstLiquibaseRun {
        @Test
        public void sucessfullyUpdatesDbSchema() {

        }
    }

    @Nested
    @Order(2)
    @SpringBootApplication
    @ContextConfiguration(classes = FailOnUnexpectedChangesetAutoConfiguration.class)
    @TestPropertySource(properties = "spring.liquibase.change-log=" +
            "classpath:db/changelog/db.changelog-with-one-changeset-only.xml")
    public class SecondLiquibaseRun {

        @Test
        public void throwsExceptionOnUnexpectedChangesets() {
            // since we want to assert an exception during application context initialization,
            // we cannot use @SpringBootTest, but instead create the application context programmatically
            SpringBootTestContextBootstrapper ctxBootstrapper = new SpringBootTestContextBootstrapper();
            ctxBootstrapper.setBootstrapContext(new DefaultBootstrapContext(SecondLiquibaseRun.class,
                    new DefaultCacheAwareContextLoaderDelegate()));

            IllegalStateException illegalStateException = assertThrows(IllegalStateException.class,
                    () -> ctxBootstrapper.buildTestContext().getApplicationContext());

            Throwable rootCause = NestedExceptionUtils.getRootCause(illegalStateException);
            assertThat(rootCause, isA(ApplicationContextException.class));
            assertThat(rootCause.getMessage(),
                    is("Unexpexted Liquibase Changesets found in db: 2-add-size-column"));
        }
    }
}
