package org.ezek.bible;

import org.ezek.bible.BibleProvider.NewTest;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

public class BookActivity extends ListActivity {

    private SimpleCursorAdapter mBookAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        String book = i.getStringExtra(NewTest.Columns.BOOK);

        Uri uri = NewTest.BOOK_URI.buildUpon()
            .appendPath(book)
            .build();
        String[] projection = new String[] {
            NewTest.Columns._ID, NewTest.Columns.CAP, NewTest.Columns.VERSE, NewTest.Columns.LINE
        };
        Cursor cursor = managedQuery(uri, projection, null, null, null);

        String[] from = new String[] {
            NewTest.Columns.CAP, NewTest.Columns.VERSE, NewTest.Columns.LINE
        };
        int[] to = new int[] {
            R.id.book_row_cap_view, R.id.book_row_verse_view, R.id.book_row_line_view
        };
        mBookAdapter = new SimpleCursorAdapter(this, R.layout.book_row, cursor, from, to);
        setListAdapter(mBookAdapter);
	}
}
