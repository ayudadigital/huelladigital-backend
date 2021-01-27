package com.huellapositiva.domain.actions;

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
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ManageNewsletterExcelAction {

    @Autowired
    private final JpaVolunteerRepository jpaVolunteerRepository;

    private final RemoteStorageService remoteStorageService;

    private final EmailCommunicationService communicationService;

    private static final String ROOT = "./newsletterEmails.xlsx";

    public void execute(String email) throws IOException {
        List<JpaVolunteer> subscribedVolunteers = jpaVolunteerRepository.findSubscribedVolunteers();
        if (subscribedVolunteers.isEmpty()){
            throw new IllegalStateException("Can not find any volunteers subscribed to newsletter");
        } else {
            buildExcel(subscribedVolunteers);
            URL url = uploadExcelAndGetUrl();
            communicationService.sendNewsletter(EmailAddress.from(email), url);
            Files.deleteIfExists(Paths.get(ROOT));
        }
    }

    private void buildExcel(List<JpaVolunteer> subscribedVolunteers) throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sh = wb.createSheet("Emails");

        sh.createRow(0).createCell(0).setCellValue("Email");
        for (JpaVolunteer v: subscribedVolunteers){
            sh.createRow(sh.getLastRowNum()+1).createCell(0).setCellValue(v.getCredential().getEmail());
        }
        performChangesInExcel(wb);
    }

    private void performChangesInExcel(XSSFWorkbook wb) throws IOException {
        File excel = new File(ROOT);
        FileOutputStream fos = new FileOutputStream(excel);
        wb.write(fos);
        fos.close();
    }

    private URL uploadExcelAndGetUrl() throws IOException {
        InputStream excel = new FileInputStream(new File(ROOT));
        URL url = remoteStorageService.uploadNewsletterExcel(excel);
        excel.close();
        return url;
    }
}
