package es.eina.utils;

import org.hibernate.type.DateType;

import java.sql.Date;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

public class StringUtils {
    private static DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy").withResolverStyle(ResolverStyle.STRICT);

    public static boolean isValid(String s) {
        return s != null && !s.isEmpty();
    }

    public static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private static Date checkDate(int year, int month, int day) {
        if (year < 1900 || year > 2150) return null;
        if (month <= 0 || month > 12) return null;

        int daysInMonth = daysInMonth(year, month);
        if (day < 0 || day > daysInMonth) return null;

        return new Date(year, month, day);
    }

    private static int daysInMonth(int year, int month) {
        switch (month) {
            case 2:
                return Year.isLeap(year) ? 29 : 28;
            case 4:
            case 6:
            case 9:
            case 11:
                return 30;
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                return 31;
        }

        return -1;
    }

    public static Date isDate(String s) {
        String[] parts = s.split("-");
        if (parts.length != 3) return null;

        int day = parseInt(parts[0]);
        int month = parseInt(parts[1]);
        int year = parseInt(parts[2]);

        return checkDate(year, month, day);
    }
}
