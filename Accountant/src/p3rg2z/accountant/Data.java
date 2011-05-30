package p3rg2z.accountant;

import static p3rg2z.accountant.Tables.*;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Data {
    private SQLiteOpenHelper db;
    
    public static Data create(Context context, File dir) {
        return  new Data(context, dir + "/" + "accountant.db");
    }

    public static Data createForTesting(Context context, File dir) {
        return  new Data(context, dir + "/" + "test-accountant.db");
    }

    private Data(Context context, String name) {
        db = new SQLiteOpenHelper(context, name, null, 1) {
            
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

    public void insert(String amount, String text, String bank, String category, String datetime) {
        ContentValues values = new ContentValues();
        values.put(Bookings.AMOUNT, amount);
        values.put(Bookings.TEXT, text);
        values.put(Bookings.SOURCE, bank);
        values.put(Bookings.DEST, category);
        values.put(Bookings.DATE, datetime);
        db.getWritableDatabase().insert(BOOKINGS, "", values);
    }
    
    public Cursor getFull(long id) {
        return db.getReadableDatabase().query(BOOKINGS, 
                new String[] {Bookings.AMOUNT, Bookings.TEXT, Bookings.SOURCE, Bookings.DEST, Bookings.DATE },
                Bookings._ID+ "=?", new String[] { String.valueOf(id) }, null, null, null);
    }
    
    public Cursor queryAllBookings() {
        return db.getReadableDatabase().query(BOOKINGS, new String[] { Bookings._ID, Bookings.TEXT }, 
                null, null, null, null, null);
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
    
    public Cursor queryAllAccounts() {
        return db.getReadableDatabase().query(ACCOUNTS, new String[] { Accounts._ID, Accounts.NAME }, 
                null, null, null, null, null);
    }

    public Cursor queryBanks() {
        return queryType(AccountType.BANK);
    }

    public Cursor queryIncomeSources() {
        return queryType(AccountType.INCOME_SOURCE);
    }

    public Cursor queryDestCategory() {
        return queryType(AccountType.DEST_CATEGORY);
    }

    public Cursor queryType(AccountType type) {
        return db.getReadableDatabase().query(ACCOUNTS, 
                new String[] { Accounts._ID, Accounts.NAME }, 
                Accounts.TYPE + "= ?", new String[] { String.valueOf(type.ordinal()) }, 
                null, null, null);
    }

}
