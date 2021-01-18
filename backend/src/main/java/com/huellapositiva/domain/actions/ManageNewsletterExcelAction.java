package com.huellapositiva.domain.actions;

import com.huellapositiva.infrastructure.orm.entities.JpaVolunteer;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteerRepository;
import lombok.AllArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Service
@AllArgsConstructor
public class ManageNewsletterExcelAction {

    @Autowired
    private final JpaVolunteerRepository jpaVolunteerRepository;

    private FileOutputStream fos;
    private XSSFSheet sh;
    private XSSFWorkbook wb;
    private List<JpaVolunteer> subscribedVolunteers;
    private String root = "./testdata.xlsx";

    public void execute() throws IOException {
        //Obtener lista con voluntarios suscritos -->
        //subscribedVolunteers = jpaVolunteerRepository.findSubscribedVolunteers();
        buildExcel();
        //Mandar email con el excel

        //Borrar excel
        Files.deleteIfExists(Paths.get(root));
    }

    private void buildExcel() throws IOException {
        wb = new XSSFWorkbook();
        sh = wb.createSheet("Emails");

        sh.createRow(0).createCell(0).setCellValue("Email");
        for (JpaVolunteer v:subscribedVolunteers){
            sh.createRow(sh.getLastRowNum()+1).createCell(0).setCellValue(v.getCredential().getEmail());
        }
        performChangesInExcel();
    }

    private void performChangesInExcel() throws IOException {
        File excel = new File(root);
        fos = new FileOutputStream(excel);
        wb.write(fos);
    }
}
