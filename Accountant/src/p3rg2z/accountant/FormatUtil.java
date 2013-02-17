/*
   Copyright 2013 Peter Goetz

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

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
    // define US as canonical format
    private static final NumberFormat CANONICAL_NUMBER_FORMATTER = NumberFormat.getNumberInstance(Locale.US);
    private static final NumberFormat LOCAL_NUMBER_FORMATTER = NumberFormat.getNumberInstance();
    private static final DateFormat CANONICAL_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final DateFormat LOCAL_LONG_DATE_FORMATTER = DateFormat.getDateInstance(DateFormat.LONG);
    private static final DateFormat LOCAL_SHORT_DATE_FORMATTER = DateFormat.getDateInstance(DateFormat.SHORT);
    private static final DateFormat LOCAL_FULL_DATE_FORMATTER = DateFormat.getDateInstance(DateFormat.FULL);

    private static Number parseAsLocalNumber(String s) throws ParseException {
        return LOCAL_NUMBER_FORMATTER.parse(s);
    }

    private static Number parseAsLocalCurrency(String s) throws ParseException {
        return LOCAL_CURRENCY_FORMATTER.parse(s);
    }

    public static String formatAsLocalCurrency(Number n) {
        return LOCAL_CURRENCY_FORMATTER.format(n);
    }

    public static String formatAsCanonical(Date date) {
        return CANONICAL_DATE_FORMATTER.format(date);
    }

    public static String formatAsLocal(Date date) {
        return LOCAL_LONG_DATE_FORMATTER.format(date);
    }

    public static String reformatNumberAsCanonical(String numberString) throws ParseException {
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
        Number number = CANONICAL_NUMBER_FORMATTER.parse(numberString);
        return LOCAL_NUMBER_FORMATTER.format(number);
    }

    public static String reformatNumberAsLocalCurrency(String numberString) throws ParseException {
        Number number = CANONICAL_NUMBER_FORMATTER.parse(numberString);
        return LOCAL_CURRENCY_FORMATTER.format(number);
    }

    public static Date parseAsCanonicalDate(String s) throws ParseException {
        return CANONICAL_DATE_FORMATTER.parse(s);
    }

    public static String reformatAsLocalDateTime(String dateString) throws ParseException {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).
            format(CANONICAL_DATE_FORMATTER.parse(dateString));
    }

    public static String reformatAsCanonicalDateTime(String dateString) throws ParseException {
        try {
            return CANONICAL_DATE_FORMATTER.format(LOCAL_LONG_DATE_FORMATTER.parse(dateString));
        } catch (ParseException e) {
            try {
                return CANONICAL_DATE_FORMATTER.format(LOCAL_SHORT_DATE_FORMATTER.parse(dateString));
            } catch (ParseException e2) {
                return CANONICAL_DATE_FORMATTER.format(LOCAL_FULL_DATE_FORMATTER.parse(dateString));
            }
        }
    }

}
