package com.huellapositiva.domain.model.valueobjects;

import lombok.Getter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Getter
public class ProposalDate {

    private final Date date;

    public ProposalDate(Date date) {
        this.date = date;
    }

    public static ProposalDate createStartingProposalDate(String date) throws ParseException {
        return new ProposalDate(new SimpleDateFormat("dd-M-yyyy").parse(date));
    }

    public static ProposalDate createClosingProposalDate(String date) throws ParseException {
        return new ProposalDate(new SimpleDateFormat("dd-M-yyyy hh:mm:ss").parse(date + " 23:55:00"));
    }

    public static ProposalDate createStartingVolunteeringDate(String date) throws ParseException {
        return new ProposalDate(new SimpleDateFormat("dd-MM-yyyy").parse(date));
    }

    public boolean isBefore(ProposalDate date){
        return this.date.before(date.getDate());
    }

    public boolean isBeforeNow(){
        return this.date.before(new Date());
    }

    public long getBusinessDaysFrom(Date targetDate){
        LocalDate proposalLocalDate = LocalDate.ofInstant(this.date.toInstant(), ZoneId.systemDefault());
        LocalDate targetLocalDate = LocalDate.ofInstant(targetDate.toInstant(), ZoneId.systemDefault());
        Predicate<LocalDate> isWeekend = date -> date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY;
        long daysBetween = ChronoUnit.DAYS.between(targetLocalDate,proposalLocalDate);
        return Stream.iterate(proposalLocalDate, date -> date.plusDays(1)).limit(daysBetween)
                .filter(isWeekend.negate()).count();
    }
}
