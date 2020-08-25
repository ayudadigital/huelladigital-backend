package com.huellapositiva.domain.model.valueobjects;

import lombok.Getter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
public class ProposalDate {

    private final Date date;

    public ProposalDate(Date date) {
        this.date = date;
    }

    public static ProposalDate createExpirationDate(String date) throws ParseException {
        return new ProposalDate(new SimpleDateFormat("dd-M-yyyy hh:mm:ss").parse(date + " 23:55:00"));
    }

    public static ProposalDate createStartingDate(String date) throws ParseException {
        return new ProposalDate(new SimpleDateFormat("dd-MM-yyyy").parse(date));
    }

    public boolean isBefore(ProposalDate date){
        return this.date.before(date.getDate());
    }

    public boolean isBeforeNow(){
        return this.date.before(new Date());
    }
}
