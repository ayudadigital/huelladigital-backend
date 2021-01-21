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
import org.springframework.web.multipart.MultipartFile;

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

    private List<JpaVolunteer> subscribedVolunteers;
    private final String root = "./newsletterEmails.xlsx";

    public void execute(String email) throws IOException {
        subscribedVolunteers = jpaVolunteerRepository.findSubscribedVolunteers();
        buildExcel();
        URL url = UploadExcelAndGetUrl();
        communicationService.sendNewsletter(EmailAddress.from(email), url);
        Files.deleteIfExists(Paths.get(root));
    }

    private void buildExcel() throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sh = wb.createSheet("Emails");

        sh.createRow(0).createCell(0).setCellValue("Email");
        for (JpaVolunteer v:subscribedVolunteers){
            sh.createRow(sh.getLastRowNum()+1).createCell(0).setCellValue(v.getCredential().getEmail());
        }
        performChangesInExcel(wb);
    }

    private void performChangesInExcel(XSSFWorkbook wb) throws IOException {
        File excel = new File(root);
        FileOutputStream fos = new FileOutputStream(excel);
        wb.write(fos);
        fos.close();
    }

    private URL UploadExcelAndGetUrl() throws IOException {
        InputStream excel = new FileInputStream(new File(root));
        URL url = remoteStorageService.uploadNewsletterExcel(excel);
        excel.close();
        return url;
    }
}
