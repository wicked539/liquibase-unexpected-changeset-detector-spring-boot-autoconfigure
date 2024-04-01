package io.github.wicked539;

import liquibase.Liquibase;
import liquibase.changelog.RanChangeSet;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

@Configuration
@ConditionalOnClass(SpringLiquibase.class)
public class FailOnUnexpectedChangesetAutoConfiguration {

    public FailOnUnexpectedChangesetAutoConfiguration(SpringLiquibase springLiquibase, DataSource dataSource) throws NoSuchMethodException, SQLException, InvocationTargetException, IllegalAccessException, LiquibaseException {
        Method createLiquibase = SpringLiquibase.class.getDeclaredMethod("createLiquibase", Connection.class);
        createLiquibase.setAccessible(true);

        Liquibase liquibase;

        try (Connection connection = dataSource.getConnection()) {
            liquibase = (Liquibase) createLiquibase.invoke(springLiquibase, connection);
            Collection<RanChangeSet> changeSets = liquibase.listUnexpectedChangeSets(null, null);

            if (!changeSets.isEmpty()) {
                Optional<String> changeSetIds = changeSets.stream().map(c -> c.getId() + " ").reduce(String::concat);
                throw new ApplicationContextException("Unexpexted Liquibase Changesets found in db: " + changeSetIds.get());
            }
        }
    }
}