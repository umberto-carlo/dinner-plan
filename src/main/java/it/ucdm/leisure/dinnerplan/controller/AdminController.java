package it.ucdm.leisure.dinnerplan.controller;

import it.ucdm.leisure.dinnerplan.service.AdminDataService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminDataService adminDataService;

    public AdminController(AdminDataService adminDataService) {
        this.adminDataService = adminDataService;
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/export")
    @SuppressWarnings("null")
    public ResponseEntity<ByteArrayResource> exportData() throws IOException {
        byte[] data = adminDataService.exportData();
        ByteArrayResource resource = new ByteArrayResource(data);

        String filename = "dinner-plan-backup-"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")) + ".zip";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(data.length)
                .body(resource);
    }

    @PostMapping("/import")
    public String importData(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a file to import.");
            return "redirect:/admin/dashboard";
        }

        try {
            adminDataService.importData(file.getInputStream());
            redirectAttributes.addFlashAttribute("success",
                    "Database imported successfully. All previous data has been overwritten.");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to import data: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}
