package com.huellapositiva.domain.actions;

import com.huellapositiva.infrastructure.orm.entities.JpaVolunteer;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteerRepository;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;

@Service
@AllArgsConstructor
public class ManageNewsletterExcelAction {

    @Autowired
    private final JpaVolunteerRepository jpaVolunteerRepository;

    private FileInputStream fis;
    private FileOutputStream fos;
    private XSSFSheet sh;
    private XSSFWorkbook wb;
    private List<String> volunteersSubscribed;
    private String root;

    public void execute() throws IOException {
        //Obtener lista con voluntarios suscritos -->
        List<JpaVolunteer> volunteers = jpaVolunteerRepository.findAll();
        for (JpaVolunteer v:volunteers){
            /*
            if(v.subscribed == true){
                volunteersSubscribed.add(v.email);
            }
             */
        }
        buildExcel();

        //Mandar email con el excel
    }

    private void buildExcel() throws IOException {
        fis = new FileInputStream(new File(root));
        wb = new XSSFWorkbook(fis);
        sh = wb.getSheetAt(0);

        int counter = 0;
        for (String v:volunteersSubscribed){
            sh.createRow(counter).createCell(0).setCellValue(v);
            counter++;
        }
        performChangesInExcel();
    }

    private void performChangesInExcel() throws IOException {
        File excel = new File(root);
        fos = new FileOutputStream(excel);
        wb.write(fos);
    }

    public String[] readExcel(){
        String[] content = new String[sh.getLastRowNum()+1];
        int counter = 0;
        for(Row r:sh) {
            if(r != null) {
                String email = r.getCell(0).toString();
                content[counter] = email;
                counter++;
            }
        }
        return content;
    }
}
