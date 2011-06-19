package p3rg2z.accountant;

import static p3rg2z.accountant.Tables.ACCOUNTS;
import static p3rg2z.accountant.Tables.BOOKINGS;

import java.io.File;

import p3rg2z.accountant.NewBookingActivity.BookingMode;
import p3rg2z.accountant.Tables.AccountType;
import p3rg2z.accountant.Tables.Accounts;
import p3rg2z.accountant.Tables.Bookings;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Data {
    private SQLiteOpenHelper db;
    private boolean mIsTestData;

    private static Data instance = new Data();

    private static String amount;

    public void init(Context context, File dir) {
        if (db == null) {
            db = createOpenHelper(context, dir + "/" + "accountant.db");
            mIsTestData = false;
        }
    }

    public void initForTesting(Context context, File dir) {
        if (db == null) {
            db = createOpenHelper(context, dir + "/" + "test-accountant.db");
            mIsTestData = true;
            deleteAllRows();
            addNewBooking("1.00", "Some trans", "Bank1", "Bank2", "2009-01-01 00:00:00");
            addNewBooking("1.00", "One", "Bank1", "Cat1", "2009-01-01 00:00:00");
            addNewBooking("2.00", "Two", "Bank1", "Cat1", "2009-01-01 00:00:00");
            addNewBooking("2.00", "Another Two", "Bank1", "Cat1", "2009-01-01 00:00:00");
            addNewBooking("3.00", "Three", "Bank1", "Cat1", "2009-01-01 00:00:00");
            addNewBooking("3.00", "Three", "Bank2", "Cat2", "2009-01-01 00:00:00");
            addNewBooking("3.00", "Three", "Bank2", "Cat2", "2009-01-01 00:00:00");
            addNewBooking("3.00", "Three", "Bank1", "Cat2", "2009-01-01 00:00:00");
            addNewBooking("4.00", "Four", "Bank1", "Cat1", "2009-01-01 00:00:00");
            addNewBooking("5.00", "Five", "Bank1", "Cat1", "2011-01-01 00:00:00");
            addBank("Bank1");
            addBank("Bank2");
            addDestCategory("Cat1");
            addDestCategory("Cat2");
        }
    }

    public boolean isTestData() {
        return mIsTestData;
    }

    private void deleteAllRows() {
        db.getWritableDatabase().delete(BOOKINGS, null, null);
        db.getWritableDatabase().delete(ACCOUNTS, null, null);

    }

    public static Data instance() {
        return instance;
    }

    private Data() {
    }

    private SQLiteOpenHelper createOpenHelper(Context context, String name) {
        return new SQLiteOpenHelper(context, name, null, 1) {

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void onCreate(SQLiteDatabase db) {
                db.execSQL("CREATE TABLE IF NOT EXISTS " + BOOKINGS + " ("
                        + Bookings._ID + " INTEGER PRIMARY KEY,"
                        + Bookings.AMOUNT + " INTEGER,"
                        + Bookings.TEXT + " TEXT,"
                        + Bookings.SOURCE + " TEXT,"
                        + Bookings.DEST + " TEXT,"
                        + Bookings.DATE + " TEXT);");
                db.execSQL("CREATE TABLE IF NOT EXISTS " + ACCOUNTS
                        + " (" + Accounts._ID +" INTEGER PRIMARY KEY,"
                        + Accounts.NAME + " TEXT, "
                        + Accounts.TYPE + " INTEGER);");
            }
        };
    }

    public void close() {
        db.close();
    }

    public void addNewBooking(String amount, String text, String bank, String category, String datetime) {
        ContentValues values = new ContentValues();
        values.put(Bookings.AMOUNT, amount);
        values.put(Bookings.TEXT, text);
        values.put(Bookings.SOURCE, bank);
        values.put(Bookings.DEST, category);
        values.put(Bookings.DATE, datetime);
        db.getWritableDatabase().insert(BOOKINGS, "", values);
    }

    public void updateBooking(String id, String amount, String text, String bank, String category, String datetime) {
        ContentValues values = new ContentValues();
        values.put(Bookings.AMOUNT, amount);
        values.put(Bookings.TEXT, text);
        values.put(Bookings.SOURCE, bank);
        values.put(Bookings.DEST, category);
        values.put(Bookings.DATE, datetime);
        db.getWritableDatabase().update(BOOKINGS, values, Bookings._ID + " = ?", new String[] { id });
    }

    public void deleteBooking(String id) {
        db.getWritableDatabase().delete(BOOKINGS, Bookings._ID + " = ?", new String[] { id });
    }

    public Cursor bookingFor(String id) {
        Cursor c = db.getReadableDatabase().query(BOOKINGS,
                new String[] {Bookings.AMOUNT, Bookings.TEXT, Bookings.SOURCE, Bookings.DEST, Bookings.DATE },
                Bookings._ID+ "=?", new String[] { id }, null, null, null);
        c.moveToFirst();
        return c;
    }

    public static String textFrom(Cursor booking) {
        return booking.getString(Bookings.TEXT_INDEX);
    }

    public static String amountFrom(Cursor booking) {
        return booking.getString(Bookings.AMOUNT_INDEX);
    }

    public BookingMode bookingTypeFrom(Cursor booking) {
        Cursor sourceC = db.getReadableDatabase().query(ACCOUNTS,
                new String[] { Accounts.TYPE },
                Accounts.NAME + "= ?", new String[] { sourceFrom(booking) },
                null, null, null);
        sourceC.moveToFirst();
        Cursor destC = db.getReadableDatabase().query(ACCOUNTS,
                new String[] { Accounts.TYPE },
                Accounts.NAME + "= ?", new String[] { destFrom(booking) },
                null, null, null);
        destC.moveToFirst();
        if (sourceC.getInt(0) == AccountType.BANK.ordinal() &&
              destC.getInt(0) == AccountType.DEST_CATEGORY.ordinal()) {
            return NewBookingActivity.BookingMode.EXPENSE;
        } else if (sourceC.getInt(0) == AccountType.INCOME_SOURCE.ordinal() &&
              destC.getInt(0) == AccountType.BANK.ordinal()) {
            return NewBookingActivity.BookingMode.INCOME;
        } else {
            return NewBookingActivity.BookingMode.TRANSACTION;
        }
    }

    public static String sourceFrom(Cursor booking) {
        return booking.getString(Bookings.SOURCE_INDEX);
    }

    public static String destFrom(Cursor booking) {
        return booking.getString(Bookings.DEST_INDEX);
    }

    public static String dateFrom(Cursor booking) {
        return booking.getString(Bookings.DATETIME_INDEX);
    }

    public Cursor allBookings() {
        return db.getReadableDatabase().query(BOOKINGS,
                new String[] { Bookings._ID,
                Bookings.TEXT,
                Bookings.DATE,
                Bookings.AMOUNT,
                Bookings.SOURCE,
                Bookings.DEST},
                null, null, null, null, Bookings.DATE + " DESC");
    }

    public void addBank(String name) {
        ContentValues cv = new ContentValues();
        cv.put(Accounts.NAME, name);
        cv.put(Accounts.TYPE, AccountType.BANK.ordinal());
        db.getWritableDatabase().insert(ACCOUNTS, "", cv);
    }

    public void addDestCategory(String name) {
        ContentValues cv = new ContentValues();
        cv.put(Accounts.NAME, name);
        cv.put(Accounts.TYPE, AccountType.DEST_CATEGORY.ordinal());
        db.getWritableDatabase().insert(ACCOUNTS, "", cv);
    }

    public void addIncomeSource(String name) {
        ContentValues cv = new ContentValues();
        cv.put(Accounts.NAME, name);
        cv.put(Accounts.TYPE, AccountType.INCOME_SOURCE.ordinal());
        db.getWritableDatabase().insert(ACCOUNTS, "", cv);
    }

    public Cursor allAccounts() {
        return db.getReadableDatabase().query(ACCOUNTS, new String[] { Accounts._ID, Accounts.NAME },
                null, null, null, null, null);
    }

    public Cursor allBanks() {
        return allAccountsOf(AccountType.BANK);
    }

    public Cursor allIncomeSources() {
        return allAccountsOf(AccountType.INCOME_SOURCE);
    }

    public Cursor allDestCategories() {
        return allAccountsOf(AccountType.DEST_CATEGORY);
    }

    public Cursor allAccountsOf(AccountType type) {
        return db.getReadableDatabase().query(ACCOUNTS,
                new String[] { Accounts._ID, Accounts.NAME },
                Accounts.TYPE + "= ?", new String[] { String.valueOf(type.ordinal()) },
                null, null, null);
    }

    public Cursor textSuggestionsFor(String searchString) {
        return new MergeCursor(new Cursor[] {
                db.getReadableDatabase().query(BOOKINGS,
                        new String[] { Bookings._ID, Bookings.TEXT },
                        Bookings.AMOUNT + " = ? AND " + Bookings.TEXT + " LIKE ?",
                        new String[] { amount, "%" + searchString + "%"},
                        Bookings.TEXT, null,
                        Bookings.DATE + " DESC", "0,3"),
                db.getReadableDatabase().query(BOOKINGS,
                        new String[] { Bookings._ID, Bookings.TEXT },
                        Bookings.AMOUNT + " <> ? AND " + Bookings.TEXT + " LIKE ?",
                        new String[] { amount, "%" + searchString + "%"},
                        Bookings.TEXT, null,
                        Bookings.DATE + " DESC", "0,10")});
    }

    public static class SourceAndDest {
        public String source;
        public String dest;
    }

    public SourceAndDest suggestedSourceAndDestFor(String text) {
        SourceAndDest result = new SourceAndDest();

        Cursor c = db.getReadableDatabase().query(BOOKINGS,
                new String[] { Bookings.SOURCE, Bookings.DEST, "count(*)"},
                Bookings.TEXT + " = ?", new String[] { text },
                Bookings.SOURCE + ", " + Bookings.DEST, null,
                Bookings.DATE + " DESC", "0,30");

        int count = -1;
        c.moveToNext();
        while (!c.isAfterLast()) {
            if (c.getInt(2) > count) {
                result.source = c.getString(0);
                result.dest = c.getString(1);
                count = c.getInt(2);
            }
            c.moveToNext();
        }
        return result;
    }

    public static void setAmountForSuggestions(String string) {
        amount = string;
    }

    public boolean accountExists(String accountName) {
        return db.getReadableDatabase().query(ACCOUNTS, new String[] { Accounts.NAME },
                Accounts.NAME + " = ?", new String[] { accountName }, null, null, null).getCount() > 0;
    }


}
