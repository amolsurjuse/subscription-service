package com.electrahub.subscription;

import liquibase.changelog.ChangeLogParameters;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class LiquibaseChangelogTest {

    @Test
    void masterChangelogParses() {
        assertDoesNotThrow(() -> {
            try (ClassLoaderResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor()) {
                ChangeLogParserFactory.getInstance()
                        .getParser("db/changelog/db.changelog-master.yaml", resourceAccessor)
                        .parse(
                                "db/changelog/db.changelog-master.yaml",
                                new ChangeLogParameters(),
                                resourceAccessor
                        );
            }
        });
    }
}
