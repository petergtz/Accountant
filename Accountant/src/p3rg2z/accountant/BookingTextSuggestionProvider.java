package p3rg2z.accountant;

import java.io.File;

import p3rg2z.accountant.Tables.Bookings;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class BookingTextSuggestionProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        Data.instance().init(this.getContext(), this.getContext().getExternalFilesDir(null));
        
        return true;
    }
    
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
//        String subtext = uri.getPath().replace("/" + SearchManager.SUGGEST_URI_PATH_QUERY + "/", "");
//        MatrixCursor c = new MatrixCursor(new String[] {BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_INTENT_DATA});
//        c.addRow(new Object[] {1, subtext + "A", subtext + "A"});
//        c.addRow(new Object[] {2, subtext + "B", subtext + "B"});
//        c.addRow(new Object[] {3, subtext + "C", subtext + "C"});
//        return c;
        final Cursor c = Data.instance().suggestions();
        c.moveToFirst();
        String t1 = c.getString(1);
        return new CursorWrapper(c) {

                    @Override
            public int getColumnIndexOrThrow(String columnName)
                    throws IllegalArgumentException {
                        Log.i("ACCOUNTANT", "getColumnIndexorthrow was called with " + columnName);
                return super.getColumnIndexOrThrow(columnName);
            }

                    @Override
            public int getColumnCount() {
                Log.i("ACCOUNTANT", "getColumnCount" );
                return super.getColumnCount();
            }

            @Override
            public int getColumnIndex(String columnName) {
                Log.i("ACCOUNTANT", "getColumnIndex was called with " + columnName);
                if (columnName.equals(SearchManager.SUGGEST_COLUMN_TEXT_1)) {
                    Log.i("ACCOUNTANT", "return 1");
                    return c.getColumnIndex(Bookings.TEXT);
                } else if (columnName.equals(SearchManager.SUGGEST_COLUMN_INTENT_DATA)) {
                    Log.i("ACCOUNTANT", "return 1");
                    return 1;
                }
                Log.i("ACCOUNTANT", "return -1");
                return -1;
            }

            @Override
            public String getColumnName(int columnIndex) {
                Log.i("ACCOUNTANT", "getColumnName" );
                return super.getColumnName(columnIndex);
            }

            @Override
            public String[] getColumnNames() {
                Log.i("ACCOUNTANT", "getColumnNames" );
                return super.getColumnNames();
            }

            
        };
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        return 0;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }



}
