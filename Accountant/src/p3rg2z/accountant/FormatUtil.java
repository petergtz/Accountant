package p3rg2z.accountant;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FormatUtil {

    private FormatUtil() {}

    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance();
    private static final NumberFormat US_NUMBER_FORMATTER = NumberFormat.getNumberInstance(Locale.US);
    private static final NumberFormat NUMBER_FORMATTER = NumberFormat.getNumberInstance();
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final DateFormat LOCAL_DATE_FORMATTER = DateFormat.getDateInstance(DateFormat.LONG);

    public static Number parseAsLocalNumber(String s) throws ParseException {
        return NUMBER_FORMATTER.parse(s);
    }

    public static Number parseAsLocalCurrency(String s) throws ParseException {
        return CURRENCY_FORMATTER.parse(s);
    }

    public static String formatAsLocalCurrency(Number n) {
        return CURRENCY_FORMATTER.format(n);
    }

    public static String formatDate(Date date) {
        return DATE_FORMATTER.format(date);
    }

    public static String formatDateAsLocal(Date date) {
        return LOCAL_DATE_FORMATTER.format(date);
    }

    public static String reformatAsUS(String amount) throws ParseException {
        Number number = parseAsLocalNumber(amount);
        String currencyString = formatAsLocalCurrency(number);
        Number currencyNumber = parseAsLocalCurrency(currencyString);
        if (currencyNumber instanceof Long) {
            return String.format(Locale.US, "%d.00", currencyNumber);
        } else {
            return String.format(Locale.US, "%.2f", currencyNumber);
        }
    }

    public static String reformatAsLocal(String amount) throws ParseException {
        Number number = US_NUMBER_FORMATTER.parse(amount);
        return NUMBER_FORMATTER.format(number);
    }

    public static Date parseAsUSDate(String s) throws ParseException {
        return DATE_FORMATTER.parse(s);
    }

}
