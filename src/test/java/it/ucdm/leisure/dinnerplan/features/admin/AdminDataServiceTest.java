package it.ucdm.leisure.dinnerplan.features.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import it.ucdm.leisure.dinnerplan.persistence.sql.JpaDinnerEventMessageRepository;
import it.ucdm.leisure.dinnerplan.persistence.sql.JpaDinnerEventRepository;
import it.ucdm.leisure.dinnerplan.persistence.sql.JpaProposalDateRepository;
import it.ucdm.leisure.dinnerplan.persistence.sql.JpaProposalRatingRepository;
import it.ucdm.leisure.dinnerplan.persistence.sql.JpaProposalRepository;
import it.ucdm.leisure.dinnerplan.persistence.sql.JpaUserRepository;
import it.ucdm.leisure.dinnerplan.persistence.sql.JpaVoteRepository;

import java.io.ByteArrayInputStream;

import java.io.IOException;
import java.util.Collections;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminDataServiceTest {

    @Mock
    private JpaUserRepository userRepository;
    @Mock
    private JpaDinnerEventRepository dinnerEventRepository;
    @Mock
    private JpaProposalRepository proposalRepository;
    @Mock
    private JpaProposalDateRepository proposalDateRepository;
    @Mock
    private JpaProposalRatingRepository proposalRatingRepository;
    @Mock
    private JpaVoteRepository voteRepository;
    @Mock
    private JpaDinnerEventMessageRepository messageRepository;
    @Mock
    private BackupMapper backupMapper;

    @InjectMocks
    private AdminDataService adminDataService;

    @Test
    void exportData_CreatesZipWithEntries() throws IOException {
        // Setup empty data for simplicity, just checking zip structure
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        when(dinnerEventRepository.findAll()).thenReturn(Collections.emptyList());
        when(proposalRepository.findAll()).thenReturn(Collections.emptyList());
        when(proposalDateRepository.findAll()).thenReturn(Collections.emptyList());
        when(proposalRatingRepository.findAll()).thenReturn(Collections.emptyList());
        when(voteRepository.findAll()).thenReturn(Collections.emptyList());
        when(messageRepository.findAll()).thenReturn(Collections.emptyList());

        byte[] zipBytes = adminDataService.exportData();

        assertNotNull(zipBytes);
        assertTrue(zipBytes.length > 0);

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            int entryCount = 0;
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                entryCount++;
                assertTrue(entry.getName().endsWith(".json"));
            }
            assertEquals(7, entryCount, "Should contain 7 JSON files");
        }
    }

    // Note: detailed import/export content testing would require more complex
    // object setup
    // This basic test ensures the plumbing works.
}
