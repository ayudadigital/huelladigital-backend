package com.huellapositiva.infrastructure;

import com.huellapositiva.domain.exception.TemplateNotAvailableException;
import com.huellapositiva.domain.valueobjects.EmailTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class TemplateService {

    private String getContentFile(String absolutePath) {
        Logger logger = Logger.getLogger("TemplateService");
        try (
                FileReader fileReader = new FileReader(absolutePath);
                BufferedReader bufferedReader = new BufferedReader(fileReader)
        ) {
            String line = bufferedReader.readLine();
            StringBuilder template = new StringBuilder();
            while (line != null) {
                template.append(line).append("\n");
                line = bufferedReader.readLine();
            }
            int lastLineIndex = template.toString().lastIndexOf('\n');
            template = new StringBuilder(template.substring(0, lastLineIndex));
            return template.toString();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error: " + ex);
            throw new TemplateNotAvailableException(ex);
        }
    }

    public EmailTemplate getEmailConfirmationTemplate() {
        String absolutePath = new File(".").getAbsolutePath();
        absolutePath += "/src/main/resources/templates/emails/emailConfirmation.txt";
        String template = getContentFile(absolutePath);
        return new EmailTemplate(template);
    }
}