package p3rg2z.accountant;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;


public class AccountancyContentProvider extends ContentProvider {

    public static final String AUTHORITY = "p3rg2z.accountant.accountancycontentprovider";
    
    private static final String BOOKINGS = "bookings";
    private static final String ACCOUNTS = "accounts";
    
    public static final class Bookings implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BOOKINGS);
        
        public static final String AMOUNT = "amount";
        public static final String TEXT = "text";
        public static final String SOURCE = "source";
        public static final String DEST = "dest";
        public static final String DATETIME = "datetime";
        
        public static final int AMOUNT_INDEX = 0;
        public static final int TEXT_INDEX = 1;
        public static final int SOURCE_INDEX = 2;
        public static final int DEST_INDEX = 3;
        public static final int DATETIME_INDEX = 4;
        
        private Bookings() {}
    }

    public static enum AccountType {
        BANK, INCOME_SOURCE, DEST_CATEGORY
    }

    public static final class Accounts implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + ACCOUNTS);
        
        public static final String NAME = "name";
        public static final String TYPE = "type";
        
        public static final int NAME_INDEX = 0;
        public static final int TYPE_INDEX = 1;
        
        private Accounts() {}
    }

    private SQLiteOpenHelper _openHelper;
    
    @Override
    public boolean onCreate() {
        _openHelper = new SQLiteOpenHelper(getContext(), getContext().getExternalFilesDir(null)+"/" +"accountant.db", null, 2) {
            @Override
            public void onCreate(SQLiteDatabase db) {
                db.execSQL("CREATE TABLE " + BOOKINGS + " ("
                        + Bookings._ID + " INTEGER PRIMARY KEY,"
                        + Bookings.AMOUNT + " INTEGER,"
                        + Bookings.TEXT + " TEXT,"
                        + Bookings.SOURCE + " TEXT,"
                        + Bookings.DEST + " TEXT,"
                        + Bookings.DATETIME + " TEXT);");
                db.execSQL("CREATE TABLE " + ACCOUNTS
                        + " (" + Accounts._ID +" INTEGER PRIMARY KEY,"
                        + Accounts.NAME + " TEXT, "
                        + Accounts.TYPE + " INTEGER);");
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                db.execSQL("DROP TABLE IF EXISTS " + BOOKINGS);
                db.execSQL("DROP TABLE IF EXISTS" + ACCOUNTS);
                onCreate(db);
            }
        };
        return true;
    }

    private static final int BOOKINGS_QUERY = 1;
    private static final int BOOKINGS_ID_QUERY = 2;
    private static final int ACCOUNTS_QUERY = 3;
    private static final int ACCOUNTS_ID_QUERY = 4;
    
    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, BOOKINGS, BOOKINGS_QUERY);
        uriMatcher.addURI(AUTHORITY, BOOKINGS + "/#", BOOKINGS_ID_QUERY);
        uriMatcher.addURI(AUTHORITY, ACCOUNTS, ACCOUNTS_QUERY);
        uriMatcher.addURI(AUTHORITY, ACCOUNTS + "/#", ACCOUNTS_ID_QUERY);
    }
    
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        switch (uriMatcher.match(uri)) {
        case BOOKINGS_QUERY:
            throw new UnsupportedOperationException();
        case BOOKINGS_ID_QUERY:
            return _openHelper.getReadableDatabase().query(
                    BOOKINGS, projection, "_ID = ?", new String[] { uri.getLastPathSegment() },
                    null, null, null);
        case ACCOUNTS_QUERY:
            return _openHelper.getReadableDatabase().query(ACCOUNTS, projection, 
                    null, null, null, null, null);
        case ACCOUNTS_ID_QUERY:
            throw new UnsupportedOperationException();
        default:
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (uriMatcher.match(uri)) {
        case BOOKINGS_QUERY:
            long id = _openHelper.getWritableDatabase().insert(BOOKINGS, "", values);
            return ContentUris.withAppendedId(Bookings.CONTENT_URI, id);
        case ACCOUNTS_QUERY:
            long accountsId = _openHelper.getWritableDatabase().insert(ACCOUNTS, "", values);
            return ContentUris.withAppendedId(Accounts.CONTENT_URI, accountsId);
        default:
            throw new UnsupportedOperationException();
        }
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
        switch (uriMatcher.match(uri)) {
        case BOOKINGS_QUERY:
            return AUTHORITY + "/" + BOOKINGS;
        case BOOKINGS_ID_QUERY:
            return AUTHORITY + "/" + BOOKINGS;
        case ACCOUNTS_QUERY:
            return AUTHORITY + "/" + ACCOUNTS;
        case ACCOUNTS_ID_QUERY:
            return AUTHORITY + "/" + ACCOUNTS;
        default:
            return null;
        }
    }

}
