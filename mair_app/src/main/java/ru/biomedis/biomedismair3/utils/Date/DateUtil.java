/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.biomedis.biomedismair3.utils.Date;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * Helper functions for handling dates.
 * 
 * @author Marco Jakob
 */
public class DateUtil {
    private static Calendar calendar = Calendar.getInstance();
    /** The date pattern that is used for conversion. Change as you wish. */
    private static final String DATE_PATTERN = "dd.MM.yyyy";
    private static final String DATE_TIME_PATTERN = "dd.MM.yyyy HH:mm:ss";
    private static final String DATE_TIME_PATTERN_WITHOUT_SEC = "dd.MM.yyyy HH:mm";

    /** The date formatter. */
    private static final DateTimeFormatter DATE_FORMATTER = 
            DateTimeFormatter.ofPattern(DATE_PATTERN);

    private static final DateFormat DATE_TIME_FORMATTER = new SimpleDateFormat(DATE_TIME_PATTERN);

    private static final DateFormat DATE_TIME_FORMATTER_WITHOUT_SEC = new SimpleDateFormat(DATE_TIME_PATTERN_WITHOUT_SEC);
    /**
     * Returns the given date as a well formatted String. The above defined 
     * {@link DateUtil#DATE_PATTERN} is used.
     * 
     * @param date the date to be returned as a string
     * @return formatted string
     */
    public static String format(LocalDate date) {
        if (date == null) {
            return null;
        }
        return DATE_FORMATTER.format(date);
    }

    public static String timeStampToStringDateTime(long timeStamp, boolean viewSeconds){

         if(viewSeconds) return DATE_TIME_FORMATTER.format(new Date(timeStamp));
         else  return DATE_TIME_FORMATTER_WITHOUT_SEC.format(new Date(timeStamp));
    }

    /**
     * Converts a String in the format of the defined {@link DateUtil#DATE_PATTERN} 
     * to a {@link LocalDate} object.
     * 
     * Returns null if the String could not be converted.
     * 
     * @param dateString the date as String
     * @return the date object or null if it could not be converted
     */
    public static LocalDate parse(String dateString) {
        try {
            return DATE_FORMATTER.parse(dateString, LocalDate::from);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Checks the String whether it is a valid date.
     * 
     * @param dateString
     * @return true if the String is a valid date
     */
    public static boolean validDate(String dateString) {
        // Try to parse the String.
        return DateUtil.parse(dateString) != null;
    }

    public static String convertSecondsToHMmSs(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60));
        return String.format("%02d:%02d:%02d", h,m,s);

    }

    public static String replaceTime(String time, ResourceBundle res)
    {
        StringBuilder strb=new StringBuilder();

        String[] split = time.split(":");
        if(split.length==3)
        {
            strb.append(split[0]); strb.append(res.getString("app.hour"));strb.append(" ");
            strb.append(split[1]); strb.append(res.getString("app.minute"));strb.append(" ");
            strb.append(split[2]); strb.append(res.getString("app.secunde"));
            return strb.toString();
        }else   return time.replace(":","_");

    }

}