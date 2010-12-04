package org.ezek.bible;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;

class BibleImportThread extends AsyncTask<Void, String, Void> {

    private Activity activity;
    private SharedPreferences settings;

    public BibleImportThread(Activity activity, SharedPreferences settings) {
        this.activity = activity;
        this.settings = settings;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        activity.showDialog(BooksList.DIALOG_IMPORTING_BIBLE);
    }

    @Override
    protected Void doInBackground(Void... unused) {
        try {
            String mDbDir = "/data/data/org.ezek.bible/databases/";
            new File(mDbDir).mkdirs();

            OutputStream mOutputStream = new FileOutputStream(mDbDir + BibleProvider.BibleProviderDbHelper.NAME);
            InputStream mInputStream = activity.getResources().openRawResource(R.raw.bible);

            byte[] buffer = new byte[1028];
            while (mInputStream.read(buffer) > 0) {
                mOutputStream.write(buffer);
            }

            mOutputStream.close();
            mInputStream.close();

            /**
             * TODO: Actually do something with this exception.
             */
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void unused) {
        settings.edit().putBoolean("bible_installed", true).commit();
        activity.removeDialog(BooksList.DIALOG_IMPORTING_BIBLE);
    }
}
