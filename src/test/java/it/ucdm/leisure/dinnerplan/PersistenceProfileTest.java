package it.ucdm.leisure.dinnerplan;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import it.ucdm.leisure.dinnerplan.persistence.VoteRepositoryPort;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("mongo")
public class PersistenceProfileTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void whenMongoProfileActive_contextShouldLoadSuccessfully() {
        assertNotNull(applicationContext, "Context should load successfully");
        assertNotNull(applicationContext.getBean(VoteRepositoryPort.class), "VoteRepositoryPort should be present");
    }
}
