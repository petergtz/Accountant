package p3rg2z.accountant;

import java.io.File;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class BookingTextSuggestionProvider extends ContentProvider {
    public static final Uri CONTENT_URI = 
        Uri.parse("content://p3rg2z.accountant.BookingTextSuggestionProvider");
    
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        String subtext = uri.getPath().replace("/" + SearchManager.SUGGEST_URI_PATH_QUERY + "/", "");
        MatrixCursor c = new MatrixCursor(new String[] {BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_INTENT_DATA});
        c.addRow(new Object[] {1, subtext + "A", subtext + "A"});
        c.addRow(new Object[] {2, subtext + "B", subtext + "B"});
        c.addRow(new Object[] {3, subtext + "C", subtext + "C"});
        return c;
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

    @Override
    public boolean onCreate() {
//        Data.create(this.getContext(), new File("jkhkjh"));
        return true;
    }


}
