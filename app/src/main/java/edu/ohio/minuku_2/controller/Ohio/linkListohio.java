package edu.ohio.minuku_2.controller.Ohio;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import edu.ohio.minuku.DBHelper.DBHelper;
import edu.ohio.minuku.config.Constants;
import edu.ohio.minuku.logger.Log;
import edu.ohio.minuku.manager.DBManager;
import edu.ohio.minuku_2.R;
//import edu.ohio.minuku_2.R;

/**
 * Created by Lawrence on 2017/9/16.
 */

public class linkListohio extends Activity {

    private final static String TAG = "linkListohio";

    private ListView listview;
    private ArrayList<String> data;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.linklist_ohio);

        data = new ArrayList<String>();
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG,"onResume");

        data = new ArrayList<String>();
        data = getData();

        initlinkListohio();

    }

    private void initlinkListohio(){

        Log.d(TAG,"initlinkListohio");

        listview = (ListView) findViewById(R.id.linkList);
        listview.setEmptyView(findViewById(R.id.emptyView));

        OhioLinkListAdapter ohioLinkListAdapter = new OhioLinkListAdapter(
                this,
                R.id.recording_list,
                data
        );

        listview.setAdapter(ohioLinkListAdapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String link = OhioLinkListAdapter.data.get(position);

                String posTime = getLinkTime(position);

                //TODO update the value in the DB.
                SQLiteDatabase db = DBManager.getInstance().openDatabase();

                String strSQL = "UPDATE "+ OhioLinkListAdapter.taskTable +" SET "+DBHelper.clickornot_col
                        +" = "+ 1 +" WHERE "+DBHelper.TIME+ " = "+ posTime;

                db.execSQL(strSQL);

                if(OhioLinkListAdapter.dataPos.contains(position)) {

                    Intent resultIntent = new Intent(Intent.ACTION_VIEW);
                    resultIntent.setData(Uri.parse(link)); //get the link from adapter

                    startActivity(resultIntent);
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            // do something on back.
            Log.d(TAG, " onKeyDown");

            Intent intent = getIntent();
            Bundle bundle = new Bundle();
//        bundle.putBoolean("TripStatus", sharedPrefs.getBoolean("TripStatus", false));
            intent.putExtras(bundle);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public ArrayList<String> getData(){

        Log.d(TAG, " getData");

        ArrayList<String> data = new ArrayList<String>();

        long startTime = -9999;
        long endTime = -9999;
        String startTimeString = "";
        String endTimeString = "";

        Calendar cal = Calendar.getInstance();
        Date date = new Date();
        cal.setTime(date);
        int Year = cal.get(Calendar.YEAR);
        int Month = cal.get(Calendar.MONTH)+1;
        int Day = cal.get(Calendar.DAY_OF_MONTH);

        startTimeString = makingDataFormat(Year, Month, Day);
        endTimeString = makingDataFormat(Year, Month, Day+1);
        startTime = getSpecialTimeInMillis(startTimeString);
        endTime = getSpecialTimeInMillis(endTimeString);

        /*String taskTable = null;

        if(Day%2==0)
            taskTable = DBHelper.checkFamiliarOrNotLinkList_table;
        else
            taskTable = DBHelper.intervalSampleLinkList_table;
        */

        String taskTable = DBHelper.surveyLinkList_table;

        try {
            SQLiteDatabase db = DBManager.getInstance().openDatabase();

            Cursor tripCursor = db.rawQuery("SELECT "+ DBHelper.clickornot_col+", "+ DBHelper.link_col +" FROM " + taskTable + " WHERE " //+ DBHelper.Trip_id + " ='" + position + "'" +" AND "
                    +DBHelper.TIME+" BETWEEN"+" '"+startTime+"' "+"AND"+" '"+endTime+"' ORDER BY "+DBHelper.TIME+" DESC", null);

            Log.d(TAG, "SELECT "+ DBHelper.clickornot_col+", "+ DBHelper.link_col +" FROM " + taskTable + " WHERE " //+ DBHelper.Trip_id + " ='" + position + "'" +" AND "
                    +DBHelper.TIME+" BETWEEN"+" '"+startTime+"' "+"AND"+" '"+endTime+"' ORDER BY "+DBHelper.TIME+" DESC");

            //get all data from cursor
            int i = 0;
            if(tripCursor.moveToFirst()){
                do{
                    int eachdataInCursor = tripCursor.getInt(0);
                    String link = tripCursor.getString(1);

                    Log.d(TAG, " 0 : "+ eachdataInCursor+ ", 1 : "+ link);

                    /*if(eachdataInCursor==0){ //0 represent they haven't click into it.
                        dataPos.add(i);
                        i++;
                    }*/
                    data.add(link);
                    Log.d(TAG, " link : "+ link);

                    Log.d(TAG, " tripCursor.moveToFirst()");
                }while(tripCursor.moveToNext());
            }else
                Log.d(TAG, " tripCursor.moveToFirst() else");
            tripCursor.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return data;
    }

    public String getLinkTime(int position){

        String eachdataInCursor = "NA";

        try {
            SQLiteDatabase db = DBManager.getInstance().openDatabase();

            Cursor tripCursor = db.rawQuery("SELECT "+ DBHelper.TIME +" FROM " + OhioLinkListAdapter.taskTable + " WHERE " //+ DBHelper.Trip_id + " ='" + position + "'" +" AND "
                    +DBHelper.TIME+" BETWEEN"+" '"+OhioLinkListAdapter.startTime+"' "+"AND"+" '"+OhioLinkListAdapter.endTime+"' ORDER BY "+DBHelper.TIME+" DESC", null);

            Log.d(TAG, "SELECT "+ DBHelper.TIME +" FROM " + OhioLinkListAdapter.taskTable + " WHERE " //+ DBHelper.Trip_id + " ='" + position + "'" +" AND "
                    +DBHelper.TIME+" BETWEEN"+" '"+OhioLinkListAdapter.startTime+"' "+"AND"+" '"+OhioLinkListAdapter.endTime+"' ORDER BY "+DBHelper.TIME+" DESC");

            //get all data from cursor
            if(tripCursor.moveToPosition(position))
                eachdataInCursor = tripCursor.getString(0);
            else
                Log.d(TAG, " tripCursor.moveToPosition() else");
            tripCursor.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        return eachdataInCursor;
    }

    private long getSpecialTimeInMillis(String givenDateFormat){
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_NOW);
        long timeInMilliseconds = 0;
        try {
            Date mDate = sdf.parse(givenDateFormat);
            timeInMilliseconds = mDate.getTime();
            Log.d(TAG,"Date in milli :: " + timeInMilliseconds);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeInMilliseconds;
    }

    public String makingDataFormat(int year,int month,int date){
        String dataformat= "";

//        dataformat = addZero(year)+"-"+addZero(month)+"-"+addZero(date)+" "+addZero(hour)+":"+addZero(min)+":00";
        dataformat = addZero(year)+"/"+addZero(month)+"/"+addZero(date)+" "+"00:00:00";
        Log.d(TAG,"dataformat : " + dataformat);

        return dataformat;
    }

    private String addZero(int date){
        if(date<10)
            return String.valueOf("0"+date);
        else
            return String.valueOf(date);
    }

}