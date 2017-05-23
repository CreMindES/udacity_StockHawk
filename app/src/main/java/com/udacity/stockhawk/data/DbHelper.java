package com.udacity.stockhawk.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.common.collect.ImmutableMap;
import com.udacity.stockhawk.data.Contract.Quote;

import java.util.Map;


class DbHelper extends SQLiteOpenHelper {


    private static final String NAME = "StockHawk.db";
    private static final int VERSION = 3;

    private static final Map< String, String > tableUpgradeMap = ImmutableMap.<String, String>builder()
        .put("1_2", "ALTER TABLE " + NAME + " ADD COLUMN " + Quote.COLUMN_NAME + " TEXT NOT NULL;" )
        .put("2_3", "ALTER TABLE " + NAME + " ADD COLUMN " + Quote.COLUMN_STOCKEXCHANGE + " TEXT NOT NULL;" )
        .build();


    DbHelper(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String builder = "CREATE TABLE " + Quote.TABLE_NAME + " ("
                + Quote._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Quote.COLUMN_NAME + " TEXT NOT NULL, "
                + Quote.COLUMN_STOCKEXCHANGE + " TEXT NOT NULL, "
                + Quote.COLUMN_SYMBOL + " TEXT NOT NULL, "
                + Quote.COLUMN_PRICE + " REAL NOT NULL, "
                + Quote.COLUMN_ABSOLUTE_CHANGE + " REAL NOT NULL, "
                + Quote.COLUMN_PERCENTAGE_CHANGE + " REAL NOT NULL, "
                + Quote.COLUMN_HISTORY + " TEXT NOT NULL, "
                + "UNIQUE (" + Quote.COLUMN_SYMBOL + ") ON CONFLICT REPLACE);";

        db.execSQL(builder);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        for( int i = oldVersion; i < newVersion; ++i ) {
            final String key = String.valueOf( i ) + "_" + String.valueOf( i+1 );
            db.execSQL( tableUpgradeMap.get( key ) );
        }
        onCreate(db);
    }
}
