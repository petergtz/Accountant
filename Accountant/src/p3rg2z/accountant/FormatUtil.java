package p3rg2z.accountant;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FormatUtil {

    private FormatUtil() {}

    private static final NumberFormat LOCAL_CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance();
    private static final NumberFormat US_NUMBER_FORMATTER = NumberFormat.getNumberInstance(Locale.US);
    private static final NumberFormat LOCAL_NUMBER_FORMATTER = NumberFormat.getNumberInstance();
    private static final DateFormat ISO_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final DateFormat LOCAL_DATE_FORMATTER = DateFormat.getDateInstance(DateFormat.LONG);

    private static Number parseAsLocalNumber(String s) throws ParseException {
        return LOCAL_NUMBER_FORMATTER.parse(s);
    }

    private static Number parseAsLocalCurrency(String s) throws ParseException {
        return LOCAL_CURRENCY_FORMATTER.parse(s);
    }

    public static String formatAsLocalCurrency(Number n) {
        return LOCAL_CURRENCY_FORMATTER.format(n);
    }

    public static String formatAsISO(Date date) {
        return ISO_DATE_FORMATTER.format(date);
    }

    public static String formatAsLocal(Date date) {
        return LOCAL_DATE_FORMATTER.format(date);
    }

    public static String reformatNumberAsISO(String numberString) throws ParseException {
        Number number = parseAsLocalNumber(numberString);
        String currencyString = formatAsLocalCurrency(number);
        Number currencyNumber = parseAsLocalCurrency(currencyString);
        if (currencyNumber instanceof Long) {
            return String.format(Locale.US, "%d.00", currencyNumber);
        } else {
            return String.format(Locale.US, "%.2f", currencyNumber);
        }
    }

    public static String reformatNumberAsLocal(String numberString) throws ParseException {
        Number number = US_NUMBER_FORMATTER.parse(numberString);
        return LOCAL_NUMBER_FORMATTER.format(number);
    }

    public static String reformatNumberAsLocalCurrency(String numberString) throws ParseException {
        Number number = US_NUMBER_FORMATTER.parse(numberString);
        return LOCAL_CURRENCY_FORMATTER.format(number);
    }

    public static Date parseAsISODate(String s) throws ParseException {
        return ISO_DATE_FORMATTER.parse(s);
    }

    public static String reformatAsLocalDateTime(String dateString) throws ParseException {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).
            format(ISO_DATE_FORMATTER.parse(dateString));
    }

    public static String reformatAsISODateTime(String dateString) throws ParseException {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.US).
            format(LOCAL_DATE_FORMATTER.parse(dateString));
    }

}
