package com.kaist.onschedule;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2016-05-19.
 */
public class SchedDataBase extends SQLiteOpenHelper {

    private static final String TAG = "kaist_SchedDataBase";

    public static class FIELD {
        public static final String ID = "_id";
        public static final String DATE = "date";
        public static final String TIME = "time";
        public static final String DEPARTURE = "departure";
        public static final String DESTINATION = "destination";
        public static final String LEADTIMETOTAL = "totalleadtime";
        public static final String NUMOFCP = "numofcp";
        public static final String ROUTE1 = "cponfoot1";
        public static final String ROUTE2 = "cponfoot2";
        public static final String ROUTE3 = "cponfoot3";
        public static final String ROUTE4 = "cponfoot4";
        public static final String ROUTE5 = "cponfoot5";
        /*
        public static final String NAMEofROUTE1 = "cponfoot1_name";
        public static final String NAMEofROUTE2 = "cponfoot2_name";
        public static final String NAMEofROUTE3 = "cponfoot3_name";
        public static final String NAMEofROUTE4 = "cponfoot4_name";
        public static final String NAMEofROUTE5 = "cponfoot5_name";
        public static final String LEADTIMEofROUTE1 = "cponfoot1_leadtime";
        public static final String LEADTIMEofROUTE2 = "cponfoot2_leadtime";
        public static final String LEADTIMEofROUTE3 = "cponfoot3_leadtime";
        public static final String LEADTIMEofROUTE4 = "cponfoot4_leadtime";
        public static final String LEADTIMEofROUTE5 = "cponfoot5_leadtime";
        public static final String TRANSofROUTE1 = "cponfoot1_transport";
        public static final String TRANSofROUTE2 = "cponfoot2_transport";
        public static final String TRANSofROUTE3 = "cponfoot3_transport";
        public static final String TRANSofROUTE4 = "cponfoot4_transport";
        public static final String TRANSofROUTE5 = "cponfoot5_transport";
        */
    }

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "schedules.db";
    public static final String TABLE_SCHEDULES = "schedules";
    public static boolean mIsGetDBFinish = false;

/*
    String query = "CREATE TABLE " + TABLE_PRODUCTS + "(" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," +
            COLUMN_PRODUCTNAME + " TEXT " +
            ");";
            */
    public static String CREATE_QUERY = "create table " + TABLE_SCHEDULES
            + "("
            + FIELD.ID + " integer primary key autoincrement, "
            + FIELD.DATE + " text, "//" integer not null, "
            + FIELD.TIME + " text, "//" integer not null, "
            + FIELD.DEPARTURE + " text, "
            + FIELD.DESTINATION + " text, "
            + FIELD.ROUTE1 + " text, "
            + FIELD.ROUTE2 + " text, "
            + FIELD.ROUTE3 + " text, "
            + FIELD.ROUTE4 + " text, "
            + FIELD.ROUTE5 + " text, "
            + FIELD.LEADTIMETOTAL + " text "
            + ");";

    public SchedDataBase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_QUERY);
        Log.i(TAG, "schedules.db is created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXIST " + TABLE_SCHEDULES);
        Log.i(TAG, "onUpgrade");
        onCreate(db);
    }

    //Add new row to database
    public void addSchedule(SchedInfo info) {
        ContentValues values = new ContentValues();
        values.put(FIELD.DATE, info.get_date());
        values.put(FIELD.TIME, info.get_time());
        values.put(FIELD.DEPARTURE, info.get_departure());
        values.put(FIELD.DESTINATION, info.get_destination());
        values.put(FIELD.ROUTE1, info.get_cponfoot1());
        values.put(FIELD.ROUTE2, info.get_cponfoot2());
        values.put(FIELD.ROUTE3, info.get_cponfoot3());
        values.put(FIELD.ROUTE4, info.get_cponfoot4());
        values.put(FIELD.ROUTE5, info.get_cponfoot5());
        values.put(FIELD.LEADTIMETOTAL, info.get_leadtimetotal());
        SQLiteDatabase db = getWritableDatabase();
        Log.i(TAG, info.get_date() + " " + info.get_time() + " " + info.get_departure() + " " + info.get_destination() + " " +
                info.get_cponfoot1() + " " + info.get_cponfoot2() + " " + info.get_cponfoot3() + " " + info.get_cponfoot4() + " " + info.get_cponfoot5() + " " + info.get_leadtimetotal());
        db.insert(TABLE_SCHEDULES, null, values);
        db.close();
    }

    //Delete product from the database
    public void deleteSchedule(String id){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_SCHEDULES + " WHERE _id='" + id + "'");
        db.close();
        //db.execSQL("DELETE FROM " + TABLE_SCHEDULES + " WHERE " + FIELD.DATE + "=\"" + date + "\"" + " AND" + FIELD.TIME + "=\"" + time + "\";");
    }

    public void updateSchedule(int id, SchedInfo info) {
        ContentValues values = new ContentValues();
        values.put(FIELD.DATE, info.get_date());
        values.put(FIELD.TIME, info.get_time());
        values.put(FIELD.DEPARTURE, info.get_departure());
        values.put(FIELD.DESTINATION, info.get_destination());
        values.put(FIELD.ROUTE1, info.get_cponfoot1());
        values.put(FIELD.ROUTE2, info.get_cponfoot2());
        values.put(FIELD.ROUTE3, info.get_cponfoot3());
        values.put(FIELD.ROUTE4, info.get_cponfoot4());
        values.put(FIELD.ROUTE5, info.get_cponfoot5());
        values.put(FIELD.LEADTIMETOTAL, info.get_leadtimetotal());
        SQLiteDatabase db = getWritableDatabase();
        db.update(TABLE_SCHEDULES, values, "_id=" + String.valueOf(id), null);
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
    public SchedInfo getEachRowOfSchedDatabase(int i){

        //SchedInfo info = new SchedInfo("","","","");
        SchedInfo info = new SchedInfo();
        SQLiteDatabase db = getWritableDatabase();
        //String query = "SELECT * FROM " + TABLE_SCHEDULES + " WHERE 1";
        String query = "SELECT * FROM " + TABLE_SCHEDULES;

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
            mIsGetDBFinish = false;
            Log.i(TAG, c.getString(c.getColumnIndex(FIELD.ID)) + " " + c.getString(c.getColumnIndex(FIELD.DATE)) + " " + c.getString(c.getColumnIndex(FIELD.TIME)) + " "
            + c.getString(c.getColumnIndex(FIELD.DEPARTURE)) + " " + c.getString(c.getColumnIndex(FIELD.DESTINATION)));

            if (c.getString(c.getColumnIndex(FIELD.DATE)) != null) {
                info.set_date(c.getString(c.getColumnIndex(FIELD.DATE)));
            }
            if (c.getString(c.getColumnIndex(FIELD.TIME)) != null) {
                info.set_time(c.getString(c.getColumnIndex(FIELD.TIME)));
            }
            if (c.getString(c.getColumnIndex(FIELD.DEPARTURE)) != null) {
                info.set_departure(c.getString(c.getColumnIndex(FIELD.DEPARTURE)));
            }
            if (c.getString(c.getColumnIndex(FIELD.DESTINATION)) != null) {
                info.set_destination(c.getString(c.getColumnIndex(FIELD.DESTINATION)));
            }
            if (c.getString(c.getColumnIndex(FIELD.ROUTE1)) != null) {
                info.set_cponfoot1(c.getString(c.getColumnIndex(FIELD.ROUTE1)));
            }
            if (c.getString(c.getColumnIndex(FIELD.ROUTE2)) != null) {
                info.set_cponfoot2(c.getString(c.getColumnIndex(FIELD.ROUTE2)));
            }
            if (c.getString(c.getColumnIndex(FIELD.ROUTE3)) != null) {
                info.set_cponfoot3(c.getString(c.getColumnIndex(FIELD.ROUTE3)));
            }
            if (c.getString(c.getColumnIndex(FIELD.ROUTE4)) != null) {
                info.set_cponfoot4(c.getString(c.getColumnIndex(FIELD.ROUTE4)));
            }
            if (c.getString(c.getColumnIndex(FIELD.ROUTE5)) != null) {
                info.set_cponfoot5(c.getString(c.getColumnIndex(FIELD.ROUTE5)));
            }
            if (c.getString(c.getColumnIndex(FIELD.LEADTIMETOTAL)) != null) {
                info.set_leadtimetotal(c.getString(c.getColumnIndex(FIELD.LEADTIMETOTAL)));
            }
        } else {
            mIsGetDBFinish = true;
        }
        db.close();
        //return entries;
        //Log.i(TAG, info.get_date() + " " + info.get_time() + " " + info.get_departure() + " " + info.get_destination());
        return info;
    }

    /*
    public long getRowId(String date, String time, String departure, String destination)
    {
        // TODO Auto-generated method stub
        String[] columns = new String[]{FIELD.ID, FIELD.DATE, FIELD.TIME, FIELD.DEPARTURE, FIELD.DESTINATION};
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.query(TABLE_SCHEDULES, columns, FIELD.DATE + "=" + date + " and " + FIELD.TIME + "=" + time + " and "
                        + FIELD.DEPARTURE + "=" + departure + " and " + FIELD.DESTINATION + "=" + destination, null, null, null, null);

        long rowid = c.getColumnIndex(FIELD.ID);
        return rowid;
    }
    */

    public String getid(String date, String time, String departure, String destination) throws SQLException
    {
        //System.out.println("ddbpos="+heading);
        long recc=0;
        String rec=null;
        SQLiteDatabase db = getWritableDatabase();
        Cursor mCursor = db.rawQuery(
                "SELECT " + FIELD.ID + " FROM " + TABLE_SCHEDULES + " WHERE " + FIELD.DATE + " = '" + date + "' AND " + FIELD.TIME + " = '" + time + "' AND "
                        + FIELD.DEPARTURE + " = '" + departure + "' AND " + FIELD.DESTINATION + " = '" + destination + "'", null);
        if (mCursor != null && mCursor.getCount() > 0)
        {
            mCursor.moveToFirst();
            recc=mCursor.getLong(0);
            rec=String.valueOf(recc);
        }
        return rec;
    }

    public SchedInfo getSchedInfoById(int id)
    {
        SchedInfo info = null;
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_SCHEDULES + " WHERE " + FIELD.ID + "='" + id + "'", null);

        if (cursor != null)
        {
            if (cursor.moveToFirst())
            {
                info = new SchedInfo();
                info.set_id(cursor.getInt(cursor.getColumnIndex(FIELD.ID)));
                info.set_date(cursor.getString(cursor.getColumnIndex(FIELD.DATE)));
                info.set_time(cursor.getString(cursor.getColumnIndex(FIELD.TIME)));
                info.set_departure(cursor.getString(cursor.getColumnIndex(FIELD.DEPARTURE)));
                info.set_destination(cursor.getString(cursor.getColumnIndex(FIELD.DESTINATION)));
                info.set_cponfoot1(cursor.getString(cursor.getColumnIndex(FIELD.ROUTE1)));
                info.set_cponfoot2(cursor.getString(cursor.getColumnIndex(FIELD.ROUTE2)));
                info.set_cponfoot3(cursor.getString(cursor.getColumnIndex(FIELD.ROUTE3)));
                info.set_cponfoot4(cursor.getString(cursor.getColumnIndex(FIELD.ROUTE4)));
                info.set_cponfoot5(cursor.getString(cursor.getColumnIndex(FIELD.ROUTE5)));
                info.set_leadtimetotal(cursor.getString(cursor.getColumnIndex(FIELD.LEADTIMETOTAL)));
            }
            cursor.close();
        }
        return info;

    }

    public boolean isGetDBFinish() {
        return mIsGetDBFinish;
    }
    /*
    //Print out the database as a string
    public Boolean getEachRowOfSchedDatabase(SchedInfo info, int i){

        Boolean isExist = false;
        SQLiteDatabase db = getWritableDatabase();
        //String query = "SELECT * FROM " + TABLE_SCHEDULES + " WHERE 1";
        String query = "SELECT * FROM " + TABLE_SCHEDULES;

        //Cursor point to a location in your result
        Cursor c = db.rawQuery(query, null);
        //Move to first row in your result
        c.moveToFirst();
        if (i > 1) {
            while (i > 1) {
                i--;
                Log.i(TAG, "row moveToPosition(" + i + ")");
                c.moveToNext();
            }
        }
        if(!c.isAfterLast()) {
            Log.i(TAG, c.getString(c.getColumnIndex(FIELD.ID)));
            Log.i(TAG, c.getString(c.getColumnIndex(FIELD.DATE)));
            Log.i(TAG, c.getString(c.getColumnIndex(FIELD.TIME)));
            Log.i(TAG, c.getString(c.getColumnIndex(FIELD.DEPARTURE)));
            Log.i(TAG, c.getString(c.getColumnIndex(FIELD.DESTINATION)));
            if (c.getString(c.getColumnIndex(FIELD.DATE)) != null) {
                info.set_date(c.getString(c.getColumnIndex(FIELD.DATE)));
            }
            if (c.getString(c.getColumnIndex(FIELD.TIME)) != null) {
                info.set_date(c.getString(c.getColumnIndex(FIELD.TIME)));
            }
            if (c.getString(c.getColumnIndex(FIELD.DEPARTURE)) != null) {
                info.set_date(c.getString(c.getColumnIndex(FIELD.DEPARTURE)));
            }
            if (c.getString(c.getColumnIndex(FIELD.DESTINATION)) != null) {
                info.set_date(c.getString(c.getColumnIndex(FIELD.DESTINATION)));
            }
            isExist = true;
        }
        db.close();
        //return entries;
        return isExist;
    }
    */

}
