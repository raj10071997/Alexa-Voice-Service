package com.game.dhanraj.myownalexa.DatabaseForAlarmAndTimer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.game.dhanraj.myownalexa.Alarm.AlarmConstants;
import com.game.dhanraj.myownalexa.R;

import java.util.ArrayList;

/**
 * Created by Dhanraj on 20-06-2017.
 */

public class DataBase extends SQLiteOpenHelper {

    private ArrayList<AlarmConstants> myList = new ArrayList<>();

    private static  final String create_alarm_table ="CREATE TABLE " + "ListofAlarms"+
            " ("+"_id"+" INTEGER PRIMARY KEY AUTOINCREMENT,"+"AlarmOrTimer"+ " TEXT,"+"iconId"+" INTEGER,"+
           "TimeInMilli"+" LONG,"+ "Time"+ " TEXT)";

    public DataBase(Context context) {
        super(context, "DatabaseOfAlarm", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(create_alarm_table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS "+ "ListofAlarms");
        onCreate(db);
    }


    public void addAlarm(String myTime, String type,Long myTimeinmili) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Time",myTime);
        contentValues.put("AlarmOrTimer",type);
        contentValues.put("iconId", R.drawable.ic_alarm_on_black_24dp);
        contentValues.put("TimeInMilli",myTimeinmili);

        db.insert("ListofAlarms",null,contentValues);
        db.close();
    }

    public ArrayList<AlarmConstants> getDetailsOfAlarmAndTime(){
        SQLiteDatabase db = this.getWritableDatabase();
        myList.clear();
        Cursor cursor = db.query("ListofAlarms",new String[]{"_id",
                        "AlarmOrTimer","Time","iconId"},
               null,null,null,null,null);

        if(cursor.moveToFirst()) {
            do {
                AlarmConstants constants = new AlarmConstants();
                constants.setMytime(cursor.getString(cursor.getColumnIndex("Time")));
                constants.setAlarmKeyId(cursor.getInt(cursor.getColumnIndex("_id")));
                constants.setType(cursor.getString(cursor.getColumnIndex("AlarmOrTimer")));
                constants.setIconsIDs(cursor.getInt(cursor.getColumnIndex("iconId")));
                myList.add(constants);
            } while(cursor.moveToNext());
        }

        db.close();
        return  myList;
    }

    public void deleteTheRow(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("ListofAlarms","_id" +" = "+id,null);
        db.close();
    }

    public int getID() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query("ListofAlarms",new String[]{"_id",},
                null,null,null,null,null);
        int id=0;
        if(cursor.moveToFirst())  {
            do{
               id =  cursor.getInt(cursor.getColumnIndex("_id"));

            } while(cursor.moveToNext());
        }
        db.close();
        return  id;
    }

    public int getTime() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query("ListofAlarms",new String[]{"TimeInMilli","_id"},
                null,null,null,null,"TimeInMilli ASC");

        int id=0;
        if (cursor != null && cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndex("_id"));
        }

       /* if(cursor.moveToFirst())
        {
            do{
                if(id==0)
                id =  cursor.getLong(cursor.getColumnIndex("TimeInMilli"));
                else if(id>cursor.getLong(cursor.getColumnIndex("TimeInMilli")))
                    id=cursor.getLong(cursor.getColumnIndex("TimeInMilli"));

            }while(cursor.moveToNext());
        }*/

        db.close();
        return id;
    }

    public void deleteTimeRow(int idtocancelled) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("ListofAlarms","_id" +" = "+idtocancelled,null);
        db.close();
    }

    public int getLastRowID() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT ROWID from MYTABLE order by ROWID DESC limit 1";
        Cursor c = db.query("ListofAlarms",new String[]{"_id"},null,null,null,null,"_id DESC");
        int lastId=0;
        if (c != null && c.moveToFirst()) {
            lastId = c.getInt(0);
        }
        return lastId;
    }
}
