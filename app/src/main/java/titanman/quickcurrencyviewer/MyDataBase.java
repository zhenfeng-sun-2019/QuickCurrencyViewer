package titanman.quickcurrencyviewer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class MyDataBase extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION =1;

    private static  final String SQL_CREATE_TABLE = "CREATE TABLE "
            + MyConstants.MY_DATABASE_TABLE
            + " ("
            + "_id" + " INTEGER PRIMARY KEY,"
            + MyConstants.DATA_KEY_SOURCE_CURRENCY + " TEXT"
            + ","
            + MyConstants.DATA_KEY_TARGET_CURRENCY + " TEXT"
            +","
            + MyConstants.DATA_KEY_RATE + " TEXT"
            + ")";


    public MyDataBase(Context context) {
        super(context, MyConstants.MY_DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_CREATE_TABLE);
        onCreate(db);
    }

    public ArrayList<RateItem> readFromDatabase() {

        ContentValues values = new ContentValues();
        SQLiteDatabase writer = this.getWritableDatabase();
        ArrayList<RateItem> list = new ArrayList<RateItem>();

        SQLiteDatabase db = this.getReadableDatabase();
        String[] columnKey = new String[]{MyConstants.DATA_KEY_SOURCE_CURRENCY,
                MyConstants.DATA_KEY_RATE,
                MyConstants.DATA_KEY_TARGET_CURRENCY};

        Cursor cursor = db.query(MyConstants.MY_DATABASE_TABLE,
                columnKey,
                null,
                null,
                null,
                null,
                null);

        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            list.add(new RateItem(cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2)));
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    public void saveToDataBase(ArrayList<RateItem> list) {
        SQLiteDatabase writer = this.getWritableDatabase();
        writer.delete(MyConstants.MY_DATABASE_TABLE, null, null);

        ContentValues values = new ContentValues();
        for (RateItem item : list) {
            values.put(MyConstants.DATA_KEY_SOURCE_CURRENCY, item.getSourceCurrency());
            values.put(MyConstants.DATA_KEY_TARGET_CURRENCY, item.getTargetCurrency());
            values.put(MyConstants.DATA_KEY_RATE, item.getRateNumber());
            writer.insert(MyConstants.MY_DATABASE_TABLE, null, values);
        }
    }

    public void insertToDataBase(RateItem item) {
        SQLiteDatabase writer = this.getWritableDatabase();
        //writer.delete(MyConstants.MY_DATABASE_TABLE, null, null);

        ContentValues values = new ContentValues();
        values.put(MyConstants.DATA_KEY_SOURCE_CURRENCY, item.getSourceCurrency());
        values.put(MyConstants.DATA_KEY_TARGET_CURRENCY, item.getTargetCurrency());
        values.put(MyConstants.DATA_KEY_RATE, item.getRateNumber());
        writer.insert(MyConstants.MY_DATABASE_TABLE, null, values);
    }

    public void clearDataBase() {
        SQLiteDatabase writer = this.getWritableDatabase();
        writer.delete(MyConstants.MY_DATABASE_TABLE, null, null);
    }
}
