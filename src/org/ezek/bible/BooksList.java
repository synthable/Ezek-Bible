package org.ezek.bible;

import org.ezek.bible.BibleProvider.NewTest;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.admob.android.ads.AdView;

public class BooksList extends ListActivity {

    public static final int DIALOG_IMPORTING_BIBLE = 0;

    private ArrayAdapter<CharSequence> mBooksArrayAdapter;
    private AdView mAdmobAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
        if (!settings.getBoolean("bible_installed", false)) {
            new BibleImportThread(this, settings).execute();
        }

        LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        mAdmobAdView = (AdView) li.inflate(R.layout.admob_adview, null);
        mAdmobAdView.setBackgroundColor(R.color.adviewBackgroundColor);
        mAdmobAdView.setPrimaryTextColor(R.color.adviewPrimaryTextColor);
        mAdmobAdView.setSecondaryTextColor(R.color.adviewSecondaryTextColor);
        getListView().addHeaderView(mAdmobAdView);

        mBooksArrayAdapter = ArrayAdapter.createFromResource(this, R.array.new_books, R.layout.books_row);
        setListAdapter(mBooksArrayAdapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        TextView view = (TextView) v;
        Intent intent = new Intent(this, BookActivity.class);
        intent.putExtra(NewTest.Columns.BOOK, view.getText().toString());
        startActivity(intent);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_IMPORTING_BIBLE:
                ProgressDialog mProgressDialog;
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage(getResources().getString(R.string.dialog_importing_bible));
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                return mProgressDialog;
            default:
                return null;
        }
    }
}
