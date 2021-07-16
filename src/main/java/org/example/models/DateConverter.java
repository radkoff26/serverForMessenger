package org.example.models;

import java.util.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DateConverter {

    public static final String DATE_PATTERN = "dd.MM.yyyy H:mm:ss";

    public static String now() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN, new Locale("ru"));
        return sdf.format(date);
    }

    public static Date toDate(String n) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        return sdf.parse(n);
    }
}
