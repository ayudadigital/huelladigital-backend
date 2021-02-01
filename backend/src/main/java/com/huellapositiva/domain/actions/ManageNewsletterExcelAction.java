package com.huellapositiva.domain.actions;

import com.huellapositiva.application.exception.NoVolunteerSubscribedException;
import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.domain.service.RemoteStorageService;
import com.huellapositiva.infrastructure.orm.entities.JpaVolunteer;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteerRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ManageNewsletterExcelAction {

    @Autowired
    private final JpaVolunteerRepository jpaVolunteerRepository;

    private final RemoteStorageService remoteStorageService;

    private final EmailCommunicationService communicationService;

    public void execute(String email) throws IOException {
        List<JpaVolunteer> subscribedVolunteers = jpaVolunteerRepository.findSubscribedVolunteers();
        if (subscribedVolunteers.isEmpty()) {
            throw new NoVolunteerSubscribedException("Could not find any volunteer subscribed to the newsletter");
        }

        File excel = buildExcel(subscribedVolunteers);
        URL url = uploadExcelAndGetUrl(excel);
        communicationService.sendNewsletterSubscriptorsEmail(EmailAddress.from(email), url);
        Files.deleteIfExists(excel.toPath());
    }

    private File buildExcel(List<JpaVolunteer> subscribedVolunteers) throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sh = wb.createSheet("Emails");

        sh.createRow(0).createCell(0).setCellValue("Email");
        for (JpaVolunteer v: subscribedVolunteers){
            sh.createRow(sh.getLastRowNum()+1).createCell(0).setCellValue(v.getCredential().getEmail());
        }

        return performChangesInExcel(wb);
    }

    private File performChangesInExcel(XSSFWorkbook wb) throws IOException {
        File tmpFile = Files.createTempFile("newsletter-users", ".tmp").toFile();
        tmpFile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(tmpFile);
        wb.write(fos);
        fos.close();
        return tmpFile;
    }

    private URL uploadExcelAndGetUrl(File excelFile) throws IOException {
        InputStream excel = new FileInputStream(excelFile);
        URL url = remoteStorageService.uploadNewsletterExcel(excel);
        excel.close();
        return url;
    }
}
