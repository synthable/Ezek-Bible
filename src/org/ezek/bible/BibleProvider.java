package org.ezek.bible;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;

public class BibleProvider extends ContentProvider {
    public static final String AUTHORITY = "org.ezek.bible.bibleprovider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final class NewTest {
        public static final Uri URI = Uri.parse(CONTENT_URI + "/newtest");
        public static final Uri BOOKS_URI = Uri.parse(CONTENT_URI + "/newtest/books");
        public static final Uri BOOK_URI = Uri.parse(CONTENT_URI + "/newtest/book");
        public static final String TABLE = "new_test";
        public static final String SCHEMA =
            "CREATE TABLE " + TABLE + "("
                + Columns._ID + " integer primary key autoincrement,"
                + Columns.CAP + " integer,"
                + Columns.VERSE + " integer,"
                + Columns.LINE + " varchar(255),"
                + Columns.BOOK + " varchar(32)"
            + ");";
        public static final String DEFAULT_SORT_ORDER = "name ASC";
        public static final String[] PROJECTION = {
            Columns._ID, Columns.CAP, Columns.VERSE, Columns.LINE, Columns.BOOK
        };

        public static final String TYPE_DIR = "vnd.android.cursor.dir/vnd.ezek.bible.category";
        public static final String TYPE_ITEM = "vnd.android.cursor.item/vnd.ezek.bible.category";
        
        public static final class Columns implements BaseColumns {
            public static final String CAP = "cap";
            public static final String VERSE = "verse";
            public static final String LINE = "line";
            public static final String BOOK = "book";
        }
    }

    public class BibleProviderDbHelper extends SQLiteOpenHelper {

        public static final String NAME = "bible.db";
        public static final int VERSION = 1;

        public BibleProviderDbHelper(Context context) {
            super(context, NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        }

        @Override
        public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
        }
    }

    public BibleProviderDbHelper dbHelper;

    private static final UriMatcher sUriMatcher;

    private static final int NEW_BOOKS = 100;
    private static final int NEW_BOOK = 101;
    private static final int NEW_BOOK_VERSE = 102;
    private static final int NEW_BOOK_VERSE_RANDOM = 103;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        sUriMatcher.addURI(AUTHORITY, "newtest/books", NEW_BOOKS);
        sUriMatcher.addURI(AUTHORITY, "newtest/book/*", NEW_BOOK);
        sUriMatcher.addURI(AUTHORITY, "newtest/book/*/verse", NEW_BOOK_VERSE);
        sUriMatcher.addURI(AUTHORITY, "newtest/book/*/verse/random", NEW_BOOK_VERSE_RANDOM);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new BibleProviderDbHelper(getContext());
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Put your code here
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case NEW_BOOKS:
                return NewTest.TYPE_DIR;
            case NEW_BOOK:
                return NewTest.TYPE_ITEM;
            case NEW_BOOK_VERSE:
                return NewTest.TYPE_ITEM;
            case NEW_BOOK_VERSE_RANDOM:
                return NewTest.TYPE_ITEM;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Put your code here
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

//Log.v("query()", uri.toString() +" AND "+ sUriMatcher.match(uri));

        String limit = null;

        switch (sUriMatcher.match(uri)) {
            case NEW_BOOKS:
                String sql = "SELECT _id, DISTINCT book FROM new_test";
                SQLiteDatabase mDb = dbHelper.getReadableDatabase();
                Cursor c = mDb.rawQuery(sql, null);
                c.setNotificationUri(getContext().getContentResolver(), uri);
                return c;
            case NEW_BOOK:
                qb.setTables(NewTest.TABLE);
                if (selection == null) {
                    selection = NewTest.Columns.BOOK + " = '" + uri.getLastPathSegment() +"'";
                }
                break;
            case NEW_BOOK_VERSE:
                qb.setTables(NewTest.TABLE);
                if (selection == null) {
                    selection = NewTest.Columns.VERSE + " = " + uri.getPathSegments().get(1);
                }
                break;
            case NEW_BOOK_VERSE_RANDOM:
                qb.setTables(NewTest.TABLE);
                if (selection == null) {
                    selection = NewTest.Columns.VERSE + " = " + uri.getPathSegments().get(1);
                }
                if(sortOrder == null) {
                    sortOrder = "RANDOM()";
                }
                limit = "1";
                break;
            default:
                throw new IllegalArgumentException("Unknown query().URI: " + uri);
        }

        SQLiteDatabase mDb = dbHelper.getReadableDatabase();
        Cursor c = qb.query(mDb, projection, selection, selectionArgs, null, null, sortOrder, limit);
        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO Put your code here
        return 0;
    }
}
