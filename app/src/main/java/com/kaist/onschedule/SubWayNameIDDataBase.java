package com.kaist.onschedule;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.SQLException;

/**
 * Created by user on 2016-06-08.
 */
public class SubWayNameIDDataBase extends SQLiteOpenHelper{

    private static final String TAG = "kaist_LocationDataBase";
    private static boolean mIsGetSubwayDBFinish = false;

    private static class COLUMN {
        public static final String ID = "_id";
        public static final String NAME = "name";
        public static final String SUBWAYID = "subwayid";
    }

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "subwaynameid.db";
    public static final String TABLE_SUBWAY = "subwaynameid";

    public static String CREATE_QUERY = "create table " + TABLE_SUBWAY
            + "("
            + COLUMN.ID + " integer primary key autoincrement, "
            + COLUMN.NAME + " text, "//" integer not null, "
            + COLUMN.SUBWAYID + " text "//" integer not null, "
            + ");";

    public SubWayNameIDDataBase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_QUERY);
        Log.i(TAG, "subwaynameid.db is created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXIST " + TABLE_SUBWAY);
        Log.i(TAG, "onUpgrade");
        onCreate(db);
    }

    //Add new row to database
    public void addSubwayNameID(SubwayNameIDInfo info) {
        ContentValues values = new ContentValues();
        values.put(COLUMN.NAME, info.getName());
        values.put(COLUMN.SUBWAYID, info.getSubwayid());
        SQLiteDatabase db = getWritableDatabase();
        Log.i(TAG, info.getName() + " " + info.getSubwayid());
        db.insert(TABLE_SUBWAY, null, values);
        db.close();
    }

    //Delete product from the database
    public void deleteSubwayNameID(String id){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_SUBWAY + " WHERE _id='" + id + "'");
        db.close();
        //db.execSQL("DELETE FROM " + TABLE_LOCATIONS + " WHERE " + FIELD.DATE + "=\"" + date + "\"" + " AND" + FIELD.TIME + "=\"" + time + "\";");
    }

    public void updateSubwayNameID(int id, SubwayNameIDInfo info) {
        ContentValues values = new ContentValues();
        values.put(COLUMN.NAME, info.getName());
        values.put(COLUMN.SUBWAYID, info.getSubwayid());
        SQLiteDatabase db = getWritableDatabase();
        db.update(TABLE_SUBWAY, values, "_id=" + String.valueOf(id), null);
        db.close();
    }

    //Print out the database as a string
    public SubwayNameIDInfo getEachRowOfSubwayNameIDDatabase(int i){

        SubwayNameIDInfo info = new SubwayNameIDInfo();
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_SUBWAY;

        //Cursor point to a location in your result
        Cursor c = db.rawQuery(query, null);
        //Move to first row in your result
        c.moveToFirst();
        if (i > 1) {
            while (i > 1) {
                i--;
                //Log.i(TAG, "row moveToPosition(" + i + ")");
                c.moveToNext();
            }
        }
        if(!c.isAfterLast()) {
            mIsGetSubwayDBFinish = false;
            Log.i(TAG, c.getString(c.getColumnIndex(COLUMN.ID)) + " " + c.getString(c.getColumnIndex(COLUMN.NAME)) + " " + c.getString(c.getColumnIndex(COLUMN.SUBWAYID)));

            if (c.getString(c.getColumnIndex(COLUMN.NAME)) != null) {
                info.setName(c.getString(c.getColumnIndex(COLUMN.NAME)));
            }
            if (c.getString(c.getColumnIndex(COLUMN.SUBWAYID)) != null) {
                info.setSubwayid(c.getString(c.getColumnIndex(COLUMN.SUBWAYID)));
            }
        } else {
            mIsGetSubwayDBFinish = true;
        }
        db.close();

        return info;
    }

    /*
    public long getRowId(String address, String pointx, String pointy)
    {
        // TODO Auto-generated method stub
        String[] columns = new String[]{COLUMN.ID, COLUMN.ADDRESS, COLUMN.PointX, COLUMN.PointY};
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.query(TABLE_LOCATIONS, columns,COLUMN.ADDRESS + "=" + address + " and " + COLUMN.PointX + "=" + pointx + " and "
                + COLUMN.PointY + "=" + pointy , null, null, null, null);

        long rowid = c.getColumnIndex(COLUMN.ID);
        return rowid;
    }
    */

    public String getid(String name, String subwayid) throws SQLException
    {
        //System.out.println("ddbpos="+heading);
        long recc=0;
        String rec=null;
        SQLiteDatabase db = getWritableDatabase();
        Cursor mCursor = db.rawQuery(
                "SELECT " + COLUMN.ID + " FROM " + TABLE_SUBWAY + " WHERE " + COLUMN.NAME + " = '" + name + "' AND " + COLUMN.SUBWAYID + " = '" + subwayid + "'", null);

        if (mCursor != null && mCursor.getCount() > 0)
        {
            mCursor.moveToFirst();
            recc=mCursor.getLong(0);
            rec=String.valueOf(recc);
        }
        return rec;
    }

    /*
    public LocationInfo getPointXYByAddress(String address)
    {
        LocationInfo info = null;
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_LOCATIONS + " WHERE " + COLUMN.ADDRESS + "='" + address + "'", null);

        if (cursor != null)
        {
            if (cursor.moveToFirst())
            {
                info = new LocationInfo();
                info.set_pointx(cursor.getString(cursor.getColumnIndex(COLUMN.PointX)));
                info.set_pointy(cursor.getString(cursor.getColumnIndex(COLUMN.PointY)));
                info.set_address(cursor.getString(cursor.getColumnIndex(COLUMN.ADDRESS)));
            }
            cursor.close();
        }
        return info;

    }
    */

    public boolean isGetDBFinish() {
        return mIsGetSubwayDBFinish;
    }
}
