package org.servicehub.integration.repository.specification;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
public abstract class AbstractPostgresSqlTest {

    @Container
    protected static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:16-alpine")
                    .withDatabaseName("testDb")
                    .withUsername("test")
                    .withPassword("test");

    static {
        POSTGRES.start();
    }

    public static String getJdbcUrl() { return POSTGRES.getJdbcUrl(); }
    public static String getUsername() { return POSTGRES.getUsername(); }
    public static String getPassword() { return POSTGRES.getPassword(); }
}
