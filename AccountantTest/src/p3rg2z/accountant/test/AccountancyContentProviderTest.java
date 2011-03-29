package p3rg2z.accountant.test;

import p3rg2z.accountant.AccountancyContentProvider;
import p3rg2z.accountant.AccountancyContentProvider.AccountType;
import p3rg2z.accountant.AccountancyContentProvider.Accounts;
import p3rg2z.accountant.AccountancyContentProvider.Bookings;
import p3rg2z.accountant.AccountsRepository;
import p3rg2z.accountant.BookingType;
import p3rg2z.accountant.BookingsRepository;
import android.database.Cursor;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

public class AccountancyContentProviderTest extends ProviderTestCase2<AccountancyContentProvider> {

    public AccountancyContentProviderTest() {
        super(AccountancyContentProvider.class, AccountancyContentProvider.AUTHORITY);
    }
    
    public void testIfCanInsertAndQueryBooking() {
        MockContentResolver mockResolver = getMockContentResolver();
        BookingsRepository bookings = new BookingsRepository(mockResolver);
        bookings.insert(123, "Lidl", "VB", "Essen", "");
        
        Cursor cursor = bookings.getFull(1);
        assertEquals(1, cursor.getCount());
        cursor.moveToNext();
        assertEquals(123, cursor.getInt(Bookings.AMOUNT_INDEX));
        assertEquals("Lidl", cursor.getString(Bookings.TEXT_INDEX));
        assertEquals("VB", cursor.getString(Bookings.SOURCE_INDEX));
        assertEquals("Essen", cursor.getString(Bookings.DEST_INDEX));
        assertEquals("", cursor.getString(Bookings.DATETIME_INDEX));
    }
    
    
    
    public void testIfCanInsertAndQueryAccount() {
        MockContentResolver mockResolver = getMockContentResolver();
        AccountsRepository accounts = new AccountsRepository(mockResolver);
        accounts.addBank("Volksbank");
        accounts.addBank("DKB");
        accounts.addBank("Commerzbank");
        Cursor c = accounts.queryAll();
        assertEquals(3, c.getCount());
        c.moveToNext();
        assertEquals("Volksbank", c.getString(Accounts.NAME_INDEX));
        assertEquals(AccountType.BANK.ordinal(),c.getInt(Accounts.TYPE_INDEX));
        c.moveToNext();
        assertEquals("DKB", c.getString(Accounts.NAME_INDEX));
        assertEquals(AccountType.BANK.ordinal(),c.getInt(Accounts.TYPE_INDEX));
        c.moveToNext();
        assertEquals("Commerzbank", c.getString(Accounts.NAME_INDEX));
        assertEquals(AccountType.BANK.ordinal(),c.getInt(Accounts.TYPE_INDEX));
    }
    
    
    

}
