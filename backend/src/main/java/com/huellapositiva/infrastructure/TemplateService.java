package com.huellapositiva.infrastructure;

import com.huellapositiva.domain.valueobjects.EmailTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@Service
public class TemplateService {


    public EmailTemplate getEmailConfirmationTemplate() {
        String template = "";
        String absolutePath = new File(".").getAbsolutePath();
        absolutePath += "/src/main/resources/templates/emails/emailConfirmation.txt";
        try {
            BufferedReader in = new BufferedReader(new FileReader(absolutePath));
            String linea = in.readLine();
            while (linea != null){
                template = template + linea + '\n';
                linea = in.readLine();
            }
            template = template.substring(0, template.lastIndexOf("\n"));
            in.close();
        } catch(IOException ex){
            System.out.println(ex);
        }
        return (EmailTemplate.createEmailTemplate(template));
    }

}
