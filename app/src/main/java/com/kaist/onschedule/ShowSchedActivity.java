package com.kaist.onschedule;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShowSchedActivity extends AppCompatActivity {

    private static final String TAG = "kaist_ShowSchedActivity";
    private String[] mRoute = {"", "", "", "", ""};
    private String mleadTimeTotal = "";
    public static String strSeparator = "__,__";
    private CalendarView cal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_sched);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final ListView lv = (ListView) findViewById(R.id.schedLv);
        cal = (CalendarView) findViewById(R.id.calendarView1);

        if (lv != null) {
            lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
                    AlertDialog.Builder b = new AlertDialog.Builder(ShowSchedActivity.this);
                    b.setMessage("Are you sure \n to delete this schedule?");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        b.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                // Setup the list view
                                final ListView newsEntryListView = (ListView) findViewById(R.id.schedLv);
                                final ShowSchedListEntryAdapter showSchedListEntryAdapter = new ShowSchedListEntryAdapter(getBaseContext(), R.layout.news_entry_list_item);
                                newsEntryListView.setAdapter(showSchedListEntryAdapter);
                                newsEntryListView.setItemsCanFocus(false);
                                newsEntryListView.setTextFilterEnabled(true);

                                // Populate the list, through the adapter
                                for(final SchedInfo entry : getNewsEntries()) {
                                    showSchedListEntryAdapter.add(entry);
                                }
                            }
                        });
                    }
                    b.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            TextView txt = (TextView) view.findViewById(R.id.news_entry_title);
                            String keyword = txt.getText().toString();
                            TextView subtxt = (TextView) view.findViewById(R.id.news_entry_subtitle);
                            String subkeyword = subtxt.getText().toString();
                            String[] date = keyword.split("-");
                            String[] time = {"",};
                            String departure = "";
                            String destination = "";
                            String[] pattern = {"By ", " From ", " To "};
                            Pattern p = Pattern.compile(pattern[0] + "(.*?)" + pattern[1] + "(.*?)" + pattern[2]);
                            Matcher m = p.matcher(subkeyword);
                            while (m.find()) {
                                time = m.group(1).split(":");
                                departure = m.group(2);
                                destination = subkeyword.substring(subkeyword.lastIndexOf(pattern[2]) + 4);
                                Log.i(TAG, m.group(1) + m.group(2) + time[0] + time[1] + departure + destination);
                            }

                            String dbRowId = getRowId(keyword, time, departure, destination);
                            deleteSchedFromDataBase(String.valueOf(dbRowId));
                        }
                    });
                    b.setPositiveButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    b.show();

                    return true;
                }
            });
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView txt = (TextView) view.findViewById(R.id.news_entry_title);
                    String keyword = txt.getText().toString();
                    setDate(keyword);
                    TextView subtxt = (TextView) view.findViewById(R.id.news_entry_subtitle);
                    String subkeyword = subtxt.getText().toString();
                    String[] date = keyword.split("-");
                    String[] time = {"",};
                    String departure = "";
                    String destination = "";
                    String[] pattern = {"By ", " From ", " To "};
                    Pattern p = Pattern.compile(pattern[0] + "(.*?)" + pattern[1] + "(.*?)" + pattern[2]);
                    Matcher m = p.matcher(subkeyword);
                    while (m.find()) {
                        time = m.group(1).split(":");
                        departure = m.group(2);
                        destination = subkeyword.substring(subkeyword.lastIndexOf(pattern[2]) + 4);
                        Log.i(TAG, m.group(1) + m.group(2) + time[0] + time[1] + departure + destination);
                    }

                    String dbRowId = getRowId(keyword, time, departure, destination);
                    Log.i(TAG, "id=" + dbRowId);
                    getRouteInfo(dbRowId);
                    Log.i(TAG, "mRoute[0]" + mRoute[0] + "mRoute[1]" + mRoute[1] + "mRoute[2]" + mRoute[2] + "mRoute[3]" + mRoute[3] + "mRoute[4]" + mRoute[4]);
                    //String[] time = m.group(1).split(":");
                    //Toast.makeText(getBaseContext(),date[0] + " " + date[1] + " " + date[2] + " " + destination, Toast.LENGTH_LONG).show();

                    Intent i = new Intent(ShowSchedActivity.this, com.kaist.onschedule.RegisterSchedDateActivity.class);
                    i.putExtra("year", date[0]);
                    i.putExtra("month", date[1]);
                    i.putExtra("day", date[2]);
                    i.putExtra("hour", time[0]);
                    i.putExtra("minute", time[1]);
                    i.putExtra("departure", departure);
                    i.putExtra("destination", destination);
                    i.putExtra("id", Integer.valueOf(dbRowId));
                    int k = 0;
                    for(String buf : mRoute) {
                        String route = "route";
                        if (!mRoute[k].isEmpty()) {
                            route = route + String.valueOf(k+1);
                            i.putExtra(route, mRoute[k]);
                        } /*else {
                            i.putExtra(route, "");
                        }*/
                        k++;
                    }
                    i.putExtra("leadtimetotal", mleadTimeTotal);
                    Toast.makeText(getBaseContext(), "Update Schedule", Toast.LENGTH_SHORT).show();
                    i.addFlags(i.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);

                }
            });
        }

        setSupportActionBar(toolbar);

        // Setup the list view
        final ListView newsEntryListView = (ListView) findViewById(R.id.schedLv);
        final ShowSchedListEntryAdapter showSchedListEntryAdapter = new ShowSchedListEntryAdapter(this, R.layout.news_entry_list_item);
        newsEntryListView.setAdapter(showSchedListEntryAdapter);
        newsEntryListView.setItemsCanFocus(false);
        newsEntryListView.setTextFilterEnabled(true);

        // Populate the list, through the adapter
        for(final SchedInfo entry : getNewsEntries()) {
            showSchedListEntryAdapter.add(entry);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Setup the list view
        final ListView newsEntryListView = (ListView) findViewById(R.id.schedLv);
        final ShowSchedListEntryAdapter showSchedListEntryAdapter = new ShowSchedListEntryAdapter(this, R.layout.news_entry_list_item);
        newsEntryListView.setAdapter(showSchedListEntryAdapter);
        newsEntryListView.setItemsCanFocus(false);
        newsEntryListView.setTextFilterEnabled(true);

        // Populate the list, through the adapter
        for(final SchedInfo entry : getNewsEntries()) {
            showSchedListEntryAdapter.add(entry);
        }
    }

    private List<SchedInfo> getNewsEntries() {

        // Let's setup some test data.
        // Normally this would come from some asynchronous fetch into a data source
        // such as a sqlite database, or an HTTP request

        final List<SchedInfo> entries = new ArrayList<SchedInfo>();
        Boolean isExist = true;
        int i = 0;

        SchedDataBase db = new SchedDataBase(this, null, null, 1);
/*
        do {
            i++;
            SchedInfo info = new SchedInfo();
            isExist = db.getEachRowOfSchedDatabase(info, i);
            //entries.add(new SchedInfo());
            entries.add(info);
        } while (isExist);

*/
        SchedInfo info = new SchedInfo();
        do {
            i++;
            info = db.getEachRowOfSchedDatabase(i);
            Log.i(TAG, info.get_date() + " " + info.get_time() + " " + info.get_departure() + " " + info.get_destination() + " " + info.get_cponfoot1() + " " +  info.get_leadtimetotal());

            //entries.add(new SchedInfo());
            if (info.get_date() != null && info.get_time() != null && info.get_departure() != null && info.get_destination() != null) {
                entries.add(info);
            }
        } while (!db.isGetDBFinish());

        Log.i(TAG, entries.toString());
        return entries;
    }

    void getRouteInfo(String id) {
        //int i = 0;
        SchedDataBase db = new SchedDataBase(this, null, null, 1);
        SchedInfo info = new SchedInfo();

        //do {
        //    i++;
        info = db.getSchedInfoById(Integer.valueOf(id));
        /*
            info = db.getEachRowOfSchedDatabase(Integer.valueOf(id));
            Log.i(TAG, info.get_date() + " " + info.get_time() + " " + info.get_departure() + " " + info.get_destination() + " " + info.get_cponfoot1() + " " +  info.get_leadtimetotal());
            info = db.getEachRowOfSchedDatabase(Integer.valueOf(id) + 1);
            Log.i(TAG, info.get_date() + " " + info.get_time() + " " + info.get_departure() + " " + info.get_destination() + " " + info.get_cponfoot1() + " " +  info.get_leadtimetotal());
            info = db.getEachRowOfSchedDatabase(Integer.valueOf(id) - 1);
            Log.i(TAG, info.get_date() + " " + info.get_time() + " " + info.get_departure() + " " + info.get_destination() + " " + info.get_cponfoot1() + " " +  info.get_leadtimetotal());
            */

            //entries.add(new SchedInfo());
            Log.i(TAG, "check Database ID" + " " + info.get_id() + " " + id);
            if (info.get_id() >= 0 && id.equals(String.valueOf(info.get_id()))) {
                Log.i(TAG, "db id is equal");
                if (!info.get_cponfoot1().isEmpty()) {
                    mRoute[0] = info.get_cponfoot1();
                }
                if (!info.get_cponfoot2().isEmpty()) {
                    mRoute[1] = info.get_cponfoot2();
                }
                if (!info.get_cponfoot3().isEmpty()) {
                    mRoute[2] = info.get_cponfoot3();
                }
                if (!info.get_cponfoot4().isEmpty()) {
                    mRoute[3] = info.get_cponfoot4();
                }
                if (!info.get_cponfoot5().isEmpty()) {
                    mRoute[4] = info.get_cponfoot5();
                }
                if(!info.get_leadtimetotal().isEmpty()) {
                    mleadTimeTotal = info.get_leadtimetotal();
                }
            }
       // } while (!db.isGetDBFinish());
    }

    public static String[] convertStringToArray(String str){
        String[] arr = str.split(strSeparator);
        return arr;
    }

    private void showSchedList() {

    }

    private void deleteSchedFromDataBase (String id) {
        SchedDataBase db = new SchedDataBase(this, null, null, 1);
        db.deleteSchedule(id);
    }

    private String getRowId(String date, String[] time, String departure, String destination) {
        SchedDataBase db = new SchedDataBase(getBaseContext(), null, null, 1);
        String dbRowId = null;
        try {
            dbRowId = db.getid(date, time[0]+":"+time[1], departure, destination);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dbRowId;
    }

    private void setDate(String date) {
        String parts[] = date.split("-");

        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int day = Integer.parseInt(parts[2]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);

        long milliTime = calendar.getTimeInMillis();
        cal.setDate(milliTime, true, true);
    }
}
