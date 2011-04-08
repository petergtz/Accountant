package p3rg2z.accountant.test;

import java.io.File;

import p3rg2z.accountant.AccountancyContentProvider.Accounts;
import p3rg2z.accountant.AccountancyContentProvider.Bookings;
import p3rg2z.accountant.Data;
import android.database.Cursor;
import android.test.AndroidTestCase;

public class AccountancyRepositoriesTest extends AndroidTestCase {

    
    @Override
    public void setUp() {
        new File(getContext().getExternalFilesDir(null)+"/" +"test.db").delete();
    }
    
    private Data createMockData() {
        return new Data(getContext(), getContext().getExternalFilesDir(null)+"/" +"test.db");
    }

    public void testIfCanInsertAndQueryBooking() {
        Data bookings = createMockData();
        bookings.insert("123", "Lidl", "VB", "Essen", "");
        
        Cursor cursor = bookings.getFull(1);
        assertEquals(1, cursor.getCount());
        cursor.moveToNext();
        assertEquals(123, cursor.getInt(Bookings.AMOUNT_INDEX));
        assertEquals("Lidl", cursor.getString(Bookings.TEXT_INDEX));
        assertEquals("VB", cursor.getString(Bookings.SOURCE_INDEX));
        assertEquals("Essen", cursor.getString(Bookings.DEST_INDEX));
        assertEquals("", cursor.getString(Bookings.DATETIME_INDEX));
    }

    
    public void testIfCanInsertAndQueryAllAccounts() {
        Data accounts = createMockData();
        accounts.addBank("Volksbank");
        accounts.addBank("DKB");
        accounts.addBank("Commerzbank");
        Cursor c = accounts.queryAll();
        assertEquals(3, c.getCount());
        c.moveToNext();
        assertEquals("Volksbank", c.getString(Accounts.NAME_INDEX));
        c.moveToNext();
        assertEquals("DKB", c.getString(Accounts.NAME_INDEX));
        c.moveToNext();
        assertEquals("Commerzbank", c.getString(Accounts.NAME_INDEX));
    }

    
    public void testIfCanInsertAndQuerySpecificAccountTypes() {
        Data accounts = createMockData();
        accounts.addBank("Volksbank");
        accounts.addBank("DKB");
        accounts.addBank("Commerzbank");
        accounts.addDestCategory("Essen");
        accounts.addDestCategory("Zug");
        accounts.addIncomeSource("Gehalt");
        Cursor c = accounts.queryBanks();
        assertEquals(3, c.getCount());
        c.moveToNext();
        assertEquals("Volksbank", c.getString(Accounts.NAME_INDEX));
        c.moveToNext();
        assertEquals("DKB", c.getString(Accounts.NAME_INDEX));
        c.moveToNext();
        assertEquals("Commerzbank", c.getString(Accounts.NAME_INDEX));

        c = accounts.queryDestCategory();
        assertEquals(2, c.getCount());
        c.moveToNext();
        assertEquals("Essen", c.getString(Accounts.NAME_INDEX));
        c.moveToNext();
        assertEquals("Zug", c.getString(Accounts.NAME_INDEX));

        c = accounts.queryIncomeSources();
        assertEquals(1, c.getCount());
        c.moveToNext();
        assertEquals("Gehalt", c.getString(Accounts.NAME_INDEX));
    }
}
