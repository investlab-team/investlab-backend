package com.investlab;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import javax.sql.DataSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "investlab.jwt.secret-base64=dGVzdC1vbmx5LWtleS1ub3QtZm9yLXByb2R1Y3Rpb24=")
@ActiveProfiles("local")
@AutoConfigureMockMvc
class InvestLabApplicationTests {
    @Autowired MockMvc mvc;
    @Autowired DataSource dataSource;

    @Test void localDatabaseSupportsTemporaryData() throws Exception {
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            assertThat(connection.getMetaData().getDatabaseProductName()).isEqualTo("H2");
            statement.execute("CREATE LOCAL TEMPORARY TABLE local_db_probe (id INTEGER PRIMARY KEY)");
            try {
                statement.executeUpdate("INSERT INTO local_db_probe (id) VALUES (1)");
                try (var rows = statement.executeQuery("SELECT id FROM local_db_probe")) {
                    assertThat(rows.next()).isTrue();
                    assertThat(rows.getInt("id")).isEqualTo(1);
                    assertThat(rows.next()).isFalse();
                }
            } finally {
                statement.execute("DROP TABLE local_db_probe");
            }
        }
    }
    @Test void healthIsPublic() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }
    @Test void anonymousCannotAccessApi() throws Exception {
        mvc.perform(get("/api/v1/accounts")).andExpect(status().isUnauthorized());
    }
    @Test void malformedJwtIsRejected() throws Exception {
        mvc.perform(get("/api/v1/accounts").header("Authorization", "Bearer invalid"))
            .andExpect(status().isUnauthorized());
    }
}
