package p3rg2z.accountant.test;

import java.io.File;

import p3rg2z.accountant.Data;
import p3rg2z.accountant.Tables.*;
import android.database.Cursor;
import android.test.AndroidTestCase;

public class AccountancyRepositoriesTest extends AndroidTestCase {

    Data data = Data.instance();
    
    @Override
    public void setUp() {
        new File(getContext().getExternalFilesDir(null)+"/" +"test.db").delete();
        data.initForTesting(getContext(), new File(getContext().getExternalFilesDir(null)+"/" +"test.db"));
    }
    
    public void ignore_testIfCanInsertAndQueryBooking() {
        data.addNewBooking("123", "Lidl", "VB", "Essen", "");
        
        Cursor cursor = data.bookingFor(1);
        assertEquals(1, cursor.getCount());
        cursor.moveToNext();
        assertEquals(123, cursor.getInt(Bookings.AMOUNT_INDEX));
        assertEquals("Lidl", cursor.getString(Bookings.TEXT_INDEX));
        assertEquals("VB", cursor.getString(Bookings.SOURCE_INDEX));
        assertEquals("Essen", cursor.getString(Bookings.DEST_INDEX));
        assertEquals("", cursor.getString(Bookings.DATETIME_INDEX));
    }

    
    public void ignore_testIfCanInsertAndQueryAllAccounts() {
        data.addBank("Volksbank");
        data.addBank("DKB");
        data.addBank("Commerzbank");
        Cursor c = data.allAccounts();
        assertEquals(3, c.getCount());
        c.moveToNext();
        assertEquals("Volksbank", c.getString(Accounts.NAME_INDEX));
        c.moveToNext();
        assertEquals("DKB", c.getString(Accounts.NAME_INDEX));
        c.moveToNext();
        assertEquals("Commerzbank", c.getString(Accounts.NAME_INDEX));
    }

    
    public void ignore_testIfCanInsertAndQuerySpecificAccountTypes() {
        data.addBank("Volksbank");
        data.addBank("DKB");
        data.addBank("Commerzbank");
        data.addDestCategory("Essen");
        data.addDestCategory("Zug");
        data.addIncomeSource("Gehalt");
        Cursor c = data.allBanks();
        assertEquals(3, c.getCount());
        c.moveToNext();
        assertEquals("Volksbank", c.getString(Accounts.NAME_INDEX));
        c.moveToNext();
        assertEquals("DKB", c.getString(Accounts.NAME_INDEX));
        c.moveToNext();
        assertEquals("Commerzbank", c.getString(Accounts.NAME_INDEX));

        c = data.allDestCategories();
        assertEquals(2, c.getCount());
        c.moveToNext();
        assertEquals("Essen", c.getString(Accounts.NAME_INDEX));
        c.moveToNext();
        assertEquals("Zug", c.getString(Accounts.NAME_INDEX));

        c = data.allIncomeSources();
        assertEquals(1, c.getCount());
        c.moveToNext();
        assertEquals("Gehalt", c.getString(Accounts.NAME_INDEX));
    }
}
