package org.ezek.bible;

import java.util.HashMap;
import java.util.Locale;

import org.ezek.bible.BibleProvider.NewTest;
import org.ezek.bible.BibleProvider.OldTest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.admob.android.ads.AdView;

public class BookActivity extends ListActivity {

    private static final int TTS_CHECK = 0;

    private static final int MENU_VOICE_START = 1;
    private static final int MENU_VOICE_STOP = 2;
    private static final int MENU_CHAPTER_JUMP = 3;

    private static final int DIALOG_CHAPTER_JUMP = 1;

    private SimpleCursorAdapter mBookAdapter;
    private AdView mAdmobAdView;
    private TextToSpeech mTts;

    private String mStartVoice;
    private String mStopVoice;
    private String mBook;
    private Uri mBaseUri;

    class mOnInitListener implements OnInitListener, OnUtteranceCompletedListener {
        HashMap<String, String> options = new HashMap<String, String>();
        Cursor cursor;
        String[] projection = new String[] { NewTest.Columns._ID, NewTest.Columns.LINE };
        int cap = 0;

        public Uri getNextCursorUri() {
            cap++;
            return mBaseUri.buildUpon()
                .appendPath(mBook)
                .appendPath(OldTest.Columns.CAP)
                .appendPath(Integer.toString(cap))
                .build();
        }

        @Override
        public void onInit(int status) {
            options.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "utterId");

            mTts.setLanguage(Locale.US);
            mTts.setOnUtteranceCompletedListener(this);

            cursor = managedQuery(getNextCursorUri(), projection, null, null, null);
            cursor.moveToFirst();
            String line = cursor.getString(cursor.getColumnIndexOrThrow(NewTest.Columns.LINE));
            mTts.speak(line, TextToSpeech.QUEUE_FLUSH, options);
        }

        @Override
        public void onUtteranceCompleted(String utteranceId) {
            cursor.moveToNext();

            if (cursor.isAfterLast()) {
                cursor = managedQuery(getNextCursorUri(), projection, null, null, null);
                cursor.moveToFirst();
                if (cursor.getCount() == 0) {
                    mTts.stop();
                    mTts.shutdown();
                }
            }

            String line = cursor.getString(cursor.getColumnIndexOrThrow(NewTest.Columns.LINE));
            mTts.speak(line, TextToSpeech.QUEUE_ADD, options);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, TTS_CHECK);

        mStopVoice = getResources().getString(R.string.menu_options_stop_voice);
        mStartVoice = getResources().getString(R.string.menu_options_start_voice);
        
        Intent i = getIntent();
        mBook = i.getStringExtra(NewTest.Columns.BOOK);

        LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        mAdmobAdView = (AdView) li.inflate(R.layout.admob_adview, null);
        mAdmobAdView.setKeywords(mBook +" bible testament religion jesus god");
        getListView().addHeaderView(mAdmobAdView);

        getListView().setFastScrollEnabled(true);

        if(i.getStringExtra("version").equals(OldTest.TABLE)) {
            mBaseUri = OldTest.BOOK_URI;
        } else {
            mBaseUri = NewTest.BOOK_URI;
        }

        String[] projection = new String[] {
            NewTest.Columns._ID, NewTest.Columns.CAP, NewTest.Columns.VERSE, NewTest.Columns.LINE
        };
        Uri uri = mBaseUri.buildUpon()
            .appendPath(mBook)
            .build();
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

    @Override
    protected void onDestroy() {
        if(mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id) {
            case DIALOG_CHAPTER_JUMP:
                Uri uri = mBaseUri.buildUpon()
                    .appendPath(mBook)
                    .appendPath("chapters")
                    .appendPath("unique")
                    .build();
                Cursor c = managedQuery(uri, null, null, null, null);

                int chapterCount = c.getCount() + 1;
                String[] chapters = new String[chapterCount];
                chapters[0] = "All";
                for(int x = 1;x<chapterCount;x++) {
                    chapters[x] = "Chapter "+ x;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getResources().getString(R.string.menu_options_chapter_jump));
                builder.setItems(chapters, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        Uri uri = mBaseUri.buildUpon()
                            .appendPath(mBook)
                            .build();

                        /** Show all chapters or only one? **/
                        if(item != 0) {
                            uri = uri .buildUpon()
                                .appendPath("cap")
                                .appendPath(Integer.toString(item))
                                .build();
                        }

                        Cursor c = managedQuery(uri, null, null, null, null);
                        mBookAdapter.changeCursor(c);
                    }
                });
                return builder.create();
            default:
                return null;
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Cursor c = mBookAdapter.getCursor();
        c.moveToPosition(position-1);
        final String line = c.getString(c.getColumnIndexOrThrow(NewTest.Columns.LINE));
        mTts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                mTts.speak(line, TextToSpeech.QUEUE_FLUSH, null);
            }
        });
        super.onListItemClick(l, v, position, id);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if(mTts != null) {
            menu.add(0, MENU_VOICE_STOP, 0, mStopVoice);
        } else {
            menu.add(0, MENU_VOICE_START, 0, mStartVoice);
        }
        menu.add(0, MENU_CHAPTER_JUMP, 0, "Chapter");
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case MENU_VOICE_START:
                mTts = new TextToSpeech(this, new mOnInitListener());
                break;
            case MENU_VOICE_STOP:
                if(mTts != null) {
                    mTts.stop();
                    mTts.shutdown();
                    mTts = null;
                }
                break;
            case MENU_CHAPTER_JUMP:
                showDialog(DIALOG_CHAPTER_JUMP);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TTS_CHECK) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
            } else {
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    }
}
