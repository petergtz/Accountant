package p3rg2z.accountant;

import p3rg2z.accountant.AccountancyContentProvider.AccountType;
import p3rg2z.accountant.AccountancyContentProvider.Accounts;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

public class AccountsRepository {
    private ContentResolver resolver;

    public AccountsRepository(ContentResolver resolver) {
        this.resolver = resolver;
    }
    
    public void addBank(String name) {
        ContentValues cv = new ContentValues();
        cv.put(Accounts.NAME, name);
        cv.put(Accounts.TYPE, AccountType.BANK.ordinal());
        resolver.insert(Accounts.CONTENT_URI, cv);
    }

    public void addDestCategory(String name) {
        ContentValues cv = new ContentValues();
        cv.put(Accounts.NAME, name);
        cv.put(Accounts.TYPE, AccountType.BANK.ordinal());
        resolver.insert(Accounts.CONTENT_URI, cv);
    }

    public void addIncomeSource(String name) {
        ContentValues cv = new ContentValues();
        cv.put(Accounts.NAME, name);
        cv.put(Accounts.TYPE, AccountType.BANK.ordinal());
        resolver.insert(Accounts.CONTENT_URI, cv);
    }
    
    public Cursor queryAll() {
        return resolver.query(Accounts.CONTENT_URI, new String[] { Accounts.NAME, Accounts.TYPE }, 
                null, null, null);
    }

}
