package it.ucdm.leisure.dinnerplan.features.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import it.ucdm.leisure.dinnerplan.features.user.*;
import it.ucdm.leisure.dinnerplan.features.event.*;
import it.ucdm.leisure.dinnerplan.features.proposal.*;

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
    private UserRepository userRepository;
    @Mock
    private DinnerEventRepository dinnerEventRepository;
    @Mock
    private ProposalRepository proposalRepository;
    @Mock
    private ProposalDateRepository proposalDateRepository;
    @Mock
    private ProposalRatingRepository proposalRatingRepository;
    @Mock
    private VoteRepository voteRepository;
    @Mock
    private DinnerEventMessageRepository messageRepository;
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
