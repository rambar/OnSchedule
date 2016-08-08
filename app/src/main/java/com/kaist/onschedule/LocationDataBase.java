package com.kaist.onschedule;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

import java.sql.SQLException;

/**
 * Created by user on 2016-06-05.
 */
public class LocationDataBase extends SQLiteOpenHelper {

    private static final String TAG = "kaist_LocationDataBase";
    private static boolean mIsGetLocDBFinish = false;

    public static class COLUMN {
        public static final String ID = "_id";
        public static final String ADDRESS = "address";
        public static final String PointX = "pointx";
        public static final String PointY = "pointy";
    }

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "locations.db";
    public static final String TABLE_LOCATIONS = "locations";

    public static String CREATE_QUERY = "create table " + TABLE_LOCATIONS
            + "("
            + COLUMN.ID + " integer primary key autoincrement, "
            + COLUMN.ADDRESS + " text, "//" integer not null, "
            + COLUMN.PointX + " text, "//" integer not null, "
            + COLUMN.PointY + " text "
            + ");";

    public LocationDataBase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_QUERY);
        Log.i(TAG, "locations.db is created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXIST " + TABLE_LOCATIONS);
        Log.i(TAG, "onUpgrade");
        onCreate(db);
    }

    //Add new row to database
    public void addLocation(LocationInfo info) {
        ContentValues values = new ContentValues();
        values.put(COLUMN.ADDRESS, info.get_address());
        values.put(COLUMN.PointX, info.get_pointx());
        values.put(COLUMN.PointY, info.get_pointy());
        SQLiteDatabase db = getWritableDatabase();
        Log.i(TAG, info.get_address() + " " + info.get_pointx() + " " + info.get_pointy());
        db.insert(TABLE_LOCATIONS, null, values);
        db.close();
    }

    //Delete product from the database
    public void deleteLocation(String id){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_LOCATIONS + " WHERE _id='" + id + "'");
        db.close();
        //db.execSQL("DELETE FROM " + TABLE_LOCATIONS + " WHERE " + FIELD.DATE + "=\"" + date + "\"" + " AND" + FIELD.TIME + "=\"" + time + "\";");
    }

    public void updateLocation(int id, LocationInfo info) {
        ContentValues values = new ContentValues();
        values.put(COLUMN.ADDRESS, info.get_address());
        values.put(COLUMN.PointX, info.get_pointx());
        values.put(COLUMN.PointY, info.get_pointy());
        SQLiteDatabase db = getWritableDatabase();
        db.update(TABLE_LOCATIONS, values, "_id=" + String.valueOf(id), null);
        db.close();
    }

    String getLocationData(String id) {
        String location = "";
        SQLiteDatabase db = getWritableDatabase();
        return location;
    }

    String getCurrentLocationData(String id) {
        String currentlocation = "";
        SQLiteDatabase db = getWritableDatabase();
        return currentlocation;
    }

    boolean getTTSEnable(String id) {
        boolean tts = false;
        SQLiteDatabase db = getWritableDatabase();
        return tts;
    }

    String getDeparture(String id) {
        String departure = "";
        SQLiteDatabase db = getWritableDatabase();
        return departure;
    }

    String getLocationDestination(String id) {
        String destination = "";
        SQLiteDatabase db = getWritableDatabase();
        return destination;
    }

    //Print out the database as a string
    public LocationInfo getEachRowOfLocationDatabase(int i){

        LocationInfo info = new LocationInfo();
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_LOCATIONS;

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
            mIsGetLocDBFinish = false;
            Log.i(TAG, c.getString(c.getColumnIndex(COLUMN.ID)) + " " + c.getString(c.getColumnIndex(COLUMN.ADDRESS)) + " " + c.getString(c.getColumnIndex(COLUMN.PointX)) + " "
                    + c.getString(c.getColumnIndex(COLUMN.PointY)));

            if (c.getString(c.getColumnIndex(COLUMN.ADDRESS)) != null) {
                info.set_address(c.getString(c.getColumnIndex(COLUMN.ADDRESS)));
            }
            if (c.getString(c.getColumnIndex(COLUMN.PointX)) != null) {
                info.set_pointx(c.getString(c.getColumnIndex(COLUMN.PointX)));
            }
            if (c.getString(c.getColumnIndex(COLUMN.PointY)) != null) {
                info.set_pointy(c.getString(c.getColumnIndex(COLUMN.PointY)));
            }
        } else {
            mIsGetLocDBFinish = true;
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

    public String getid(String address, String pointx, String pointy) throws SQLException
    {
        //System.out.println("ddbpos="+heading);
        long recc=0;
        String rec=null;
        SQLiteDatabase db = getWritableDatabase();
        Cursor mCursor = db.rawQuery(
                "SELECT " + COLUMN.ID + " FROM " + TABLE_LOCATIONS + " WHERE " + COLUMN.ADDRESS + " = '" + address + "' AND " + COLUMN.PointX + " = '" + pointx + "' AND "
                        + COLUMN.PointY + " = '" + pointy + "'", null);

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
        return mIsGetLocDBFinish;
    }
}
