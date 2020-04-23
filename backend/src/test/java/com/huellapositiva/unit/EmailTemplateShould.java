package com.huellapositiva.unit;

import com.huellapositiva.domain.exception.EmptyTemplateException;
import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.EmailTemplate;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

/*TO DO
* llega la plantilla, parsea la plantilla
* no se encuentra la plantilla
* se encuentra pero no se puede parsear
 */

class EmailTemplateShould {


    @Test
    void receive_a_empty_text_should_throw_an_exception(){
        //GIVEN

        //WHEN

        //THEN
        assertThrows(EmptyTemplateException.class,()->EmailTemplate.createEmailTemplate(""));
    }


}