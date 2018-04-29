package es.eina.tests;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;

public class DateTests {

    private static DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy").withResolverStyle(ResolverStyle.STRICT);

    public static ZonedDateTime getDate(String s){
        try {
            TemporalAccessor x = format.parse(s + " 00:00:00 +0100");
            LocalDate date = LocalDate.from(x);

            return date.atStartOfDay(ZoneId.systemDefault());
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    private static void assertDate(String s, Object value){
        ZonedDateTime date = getDate(s);
        System.out.println(s + " date: " + date);
        assert(date == value);
    }

    public static void main(String[] args) {
        assertDate("18-02-1996", null);
        //assertDate("31-02-1996", null);
        assertDate("29-02-1997", null);
    }

}
