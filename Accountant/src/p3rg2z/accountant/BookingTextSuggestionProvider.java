package p3rg2z.accountant;

import p3rg2z.accountant.Tables.Bookings;
import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;

public class BookingTextSuggestionProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        Data.instance().init(this.getContext(), this.getContext().getExternalFilesDir(null));
        return true;
    }
    
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        String searchString = uri.getPath().replace("/" + SearchManager.SUGGEST_URI_PATH_QUERY + "/", "");
        final Cursor c = Data.instance().suggestionsFor(searchString);
        return new CursorWrapper(c) {
            @Override
            public int getColumnIndex(String columnName) {
                if (columnName.equals(SearchManager.SUGGEST_COLUMN_TEXT_1)) {
                    return c.getColumnIndex(Bookings.TEXT);
/*                } else if (columnName.equals(SearchManager.SUGGEST_COLUMN_TEXT_2)) {
                    return 1234;*/
                } else if (columnName.equals(SearchManager.SUGGEST_COLUMN_INTENT_DATA)) {
                    return c.getColumnIndex(Bookings.TEXT);
                }
                return -1;
            }
            
//            @Override
//            public String getString(int columnIndex) {
//                if (columnIndex == 1234) {
//                    return "suggestion";
//                }
//                return super.getString(columnIndex);
//            }
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
