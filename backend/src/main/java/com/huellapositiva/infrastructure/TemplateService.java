package com.huellapositiva.infrastructure;

import com.huellapositiva.domain.valueobjects.EmailTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

@Service
public class TemplateService {


    public EmailTemplate getEmailConfirmationTemplate() {
        String template = "";
        char[] array = new char[100];
        try{
            FileReader input = new FileReader("/emailConfirmation.txt");
            System.out.println(input.read(array));
            /*BufferedReader in = new BufferedReader(new FileReader("emailConfirmation.txt"));
            String linea = in.readLine();
            while (linea != null){
                template = template + linea + '\n';
                linea = in.readLine();
            }
            in.close();*/
        }
        catch(IOException ex){
            System.out.println(ex);
        }
        return (EmailTemplate.createEmailTemplate(template));
    }
}
