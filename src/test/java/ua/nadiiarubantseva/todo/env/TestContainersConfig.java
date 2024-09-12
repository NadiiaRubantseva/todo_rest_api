package ua.nadiiarubantseva.todo.env;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import javax.sql.DataSource;

@Service
@Slf4j
public class TestContainersConfig {

    public static final String POSTGRES_IMAGE = "postgres:14";
    public static final int STARTUP_TIMEOUT_SECONDS = 120;

    @Bean(initMethod = "start", destroyMethod = "stop")
    public JdbcDatabaseContainer<?> jdbcDatabaseContainer() {
        log.info("Init postgres testcontainers");
        try (JdbcDatabaseContainer<?> container = new PostgreSQLContainer<>(POSTGRES_IMAGE)) {
            return container
                    .withStartupTimeoutSeconds(STARTUP_TIMEOUT_SECONDS)
                    .waitingFor(Wait.forListeningPort());
        }
    }

    @Bean
    public DataSource dataSource(JdbcDatabaseContainer<?> jdbcDatabaseContainer) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(jdbcDatabaseContainer.getJdbcUrl());
        hikariConfig.setUsername(jdbcDatabaseContainer.getUsername());
        hikariConfig.setPassword(jdbcDatabaseContainer.getPassword());
        return new HikariDataSource(hikariConfig);
    }
}