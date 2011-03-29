package p3rg2z.accountant;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import p3rg2z.accountant.AccountancyContentProvider.Bookings;

public class BookingsRepository {
    private ContentResolver resolver;

    public BookingsRepository(ContentResolver resolver) {
        this.resolver = resolver;
    }
    
    public void insert(int amount, String text, String bank, String category, String datetime) {
        ContentValues values = new ContentValues();
        values.put(Bookings.AMOUNT, amount);
//        values.put(Bookings.TYPE, type.ordinal());
        values.put(Bookings.TEXT, text);
        values.put(Bookings.SOURCE, bank);
        values.put(Bookings.DEST, category);
        values.put(Bookings.DATETIME, datetime);
        resolver.insert(Bookings.CONTENT_URI, values);
    }
    
    public Cursor getFull(long id) {
        return resolver.query(ContentUris.withAppendedId(Bookings.CONTENT_URI, id), 
                new String[] {Bookings.AMOUNT, Bookings.TEXT, Bookings.SOURCE, Bookings.DEST, Bookings.DATETIME },
                null, null, null);
    }

}
