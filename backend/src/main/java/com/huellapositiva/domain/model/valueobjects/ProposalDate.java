package com.huellapositiva.domain.model.valueobjects;

import lombok.Getter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Calendar.DAY_OF_MONTH;

@Getter
public class ProposalDate {

    private final Date date;

    public ProposalDate(Date date) {
        this.date = date;
    }

    public static ProposalDate createStartingProposalDate(String date) throws ParseException {
        return new ProposalDate(new SimpleDateFormat("dd-MM-yyyy").parse(date));
    }

    public static ProposalDate createClosingProposalDate(String date) throws ParseException {
        return new ProposalDate(new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").parse(date + " 23:55:00"));
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
        if (proposalLocalDate.isBefore(targetLocalDate)) {
            return 0;
        }
        Predicate<LocalDate> isWeekend = d -> d.getDayOfWeek() == DayOfWeek.SATURDAY
                || d.getDayOfWeek() == DayOfWeek.SUNDAY;
        long daysBetween = ChronoUnit.DAYS.between(targetLocalDate,proposalLocalDate);
        return Stream.iterate(proposalLocalDate, d -> d.plusDays(1)).limit(daysBetween)
                .filter(isWeekend.negate()).count();
    }

    @Override
    public String toString() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return String.format("%d-%d-%d", calendar.get(DAY_OF_MONTH), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
    }

    public boolean isNotBeforeStipulatedDeadline() {
        int stipulatedTimeInMonthsForDeadline = 6 * 30;
        Date dateSixMonthsFromNow = Date.from(LocalDate.now().plusDays(stipulatedTimeInMonthsForDeadline).atStartOfDay().toInstant(OffsetDateTime.now().getOffset()));
        return !this.date.before(dateSixMonthsFromNow);
    }
}
