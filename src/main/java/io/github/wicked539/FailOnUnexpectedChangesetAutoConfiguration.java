package io.github.wicked539;

import liquibase.Liquibase;
import liquibase.changelog.RanChangeSet;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.context.ApplicationContextException;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

@AutoConfiguration(after = { LiquibaseAutoConfiguration.class })
@ConditionalOnClass(SpringLiquibase.class)
public class FailOnUnexpectedChangesetAutoConfiguration {

    public FailOnUnexpectedChangesetAutoConfiguration(SpringLiquibase springLiquibase, DataSource dataSource)
            throws NoSuchMethodException, SQLException, InvocationTargetException,
            IllegalAccessException, LiquibaseException {

        Method createLiquibase = SpringLiquibase.class.getDeclaredMethod("createLiquibase", Connection.class);
        createLiquibase.setAccessible(true);

        try (Liquibase liquibase = (Liquibase) createLiquibase.invoke(springLiquibase, dataSource.getConnection())) {
            Collection<RanChangeSet> unexpectedChangeSets =
                    liquibase.listUnexpectedChangeSets(null, null);

            if (!unexpectedChangeSets.isEmpty()) {
                String changeSetIds = unexpectedChangeSets.stream()
                        .map(c -> c.getId() + " ").reduce(String::concat).get().trim();
                throw new ApplicationContextException("Unexpexted Liquibase Changesets found in db: " + changeSetIds);
            }
        }
    }
}