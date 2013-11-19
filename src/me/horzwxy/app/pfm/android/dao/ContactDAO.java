package me.horzwxy.app.pfm.android.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by horz on 11/18/13.
 */
public class ContactDAO {

    private SQLiteDatabase db;

    public ContactDAO( Context context ) {
        db = new ContactDBHelper( context ).getWritableDatabase();
    }

    public void addContact( String nickname ) {
        db.execSQL( "INSERT INTO contacts VALUES ('" + nickname + "');" );
    }

    public List<String> getAllContacts() {
        Cursor cursor = db.query( false,
                ContactDBHelper.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        List<String> result = new ArrayList<String>();
        while ( cursor.moveToNext() ) {
            result.add( cursor.getString( cursor.getColumnIndex( ContactDBHelper.KEY_NICKNAME ) ) );
        }
        return result;
    }

    public void closeDAO() {
        db.close();
    }

    private static class ContactDBHelper extends SQLiteOpenHelper {
        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_NAME = "pfm";
        private static final String TABLE_NAME = "contacts";

        private static final String KEY_NICKNAME = "nickname";
        private static final String TABLE_CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        KEY_NICKNAME + " TEXT);";

        ContactDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

        }
    }

}
