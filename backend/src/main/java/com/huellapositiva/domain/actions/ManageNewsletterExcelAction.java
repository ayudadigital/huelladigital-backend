package com.huellapositiva.domain.actions;

import com.huellapositiva.infrastructure.orm.entities.JpaVolunteer;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteerRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ManageNewsletterExcelAction {

    @Autowired
    private final JpaVolunteerRepository jpaVolunteerRepository;

    private List<JpaVolunteer> subscribedVolunteers;
    private final String root = "./testdata.xlsx";

    public void execute() throws IOException {
        subscribedVolunteers = jpaVolunteerRepository.findSubscribedVolunteers();
        buildExcel();
        //Mandar email con el excel

        //Borrar excel
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
}
