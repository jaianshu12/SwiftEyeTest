package com.example.jaianshu.swifteyetest;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jaianshu on 8/13/2016.
 */
public class MotionActivity extends Activity {
    CalendarView calendar;
    Dialog dialog;
    EditText txthourofday;
    Button dialogButton;
    ImageView motionimage;
    Bitmap motionbmp;
    String [] imagesList = {};
    int count=0;
    Spinner dynamicSpinner;
    String main_path = "http://52.35.20.220/rpi/motion_pics/";
    String path = main_path;
    String TAG = "Motion Activity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motion);
        motionimage = (ImageView) findViewById(R.id.motionImageView);
        Log.i(MainActivity.TAG, "In MotionActivity");
        //initializes the calendarview
        initializeCalendar();
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_settime);
        dialog.setTitle("Hour Of Day");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
//        txthourofday = (EditText)dialog.findViewById(R.id.txthourofday);
        dialogButton = (Button) dialog.findViewById(R.id.btnsettime);
        dynamicSpinner = (Spinner) dialog.findViewById(R.id.timespinner);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this,R.array.time_array,android.R.layout.simple_spinner_item);
        dynamicSpinner.setAdapter(adapter);

        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subdir ="";
                int n = dynamicSpinner.getSelectedItemPosition();
                String item = dynamicSpinner.getSelectedItem().toString();
                Log.i(TAG,"selected = "  + n +"  : " + item );
//                Toast.makeText(getApplicationContext(),txthourofday.getText().toString() , Toast.LENGTH_SHORT).show();
//                path=path+txthourofday.getText().toString()+"/";
                  if(n<10){
                      subdir = "0" + (n);
                  }
                  else{
                      subdir = ""+ (n);
                  }
                  path=path+ subdir +"/";
                Log.i(TAG, "path : " + path);
                new Thread()
                {
                    public void run()
                    {
                        try {
                            runTask();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        catch (NullPointerException n)
                        {

                            Log.i(MainActivity.TAG,"Null Exception");
                            displayExceptionMessage();
                        }
//                Log.i(MainActivity.TAG,"image lst :"+ imagesList[0] + imagesList[1] + imagesList[2] + "size" + imagesList.length);
                        count = imagesList.length-1;
                        if(count>=0) {
                            loadImage(path + imagesList[count]);
                        }
                    }
                }.start();

                dialog.dismiss();

            }
        });

        final GestureDetector gdt = new GestureDetector(new GestureListener());
        motionimage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                gdt.onTouchEvent(motionEvent);
                return true;
            }
        });
    }

    public void displayExceptionMessage(){
        MotionActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                motionimage.setImageResource(R.drawable.noimagefound);
//                Toast.makeText(getApplicationContext(),"No Pics taken in this time period." , Toast.LENGTH_LONG).show();
            }
        });
    }
    private String [] processValue(String result)
    {
        Log.i(MainActivity.TAG, result);
        List<String> allMatches = new ArrayList<String>();
        Matcher m = Pattern.compile("a href=\"(\\w+\\.jpg)\">").matcher(result);
        while (m.find()) {
            allMatches.add(m.group(1));
        }
        Log.i(MainActivity.TAG, "All matches - " + allMatches.toString());
        String mystr = allMatches.toString();
        mystr = mystr.replaceAll("\\[", "").replaceAll("\\]","");
        String [] myList = mystr.split(", ");
        Log.i(MainActivity.TAG, myList.toString() );
        return myList;
    }

    public void loadImage(String imageURL)
    {
        try {
            URL url = new URL(imageURL);
            motionbmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
//            InputStream.res
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    motionimage.setImageBitmap(motionbmp);
                }
            });
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void runTask() throws ExecutionException, InterruptedException, NullPointerException {
        Log.i(MainActivity.TAG, "inside runTask");
        String mL = new MyTask().execute(path).get();
        String [] lst = processValue(mL);
        imagesList = lst;
        Log.i(MainActivity.TAG, " total pics: " + imagesList.length + " last pic : " + imagesList[imagesList.length-1] );
    }

    private class MyTask extends AsyncTask<String, Void,  String > {
        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line ="";
                while ((line = reader.readLine()) != null){
                    buffer.append(line);
                }
                return (buffer.toString());
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
//            processValue(result);
//            tvData.setText(result);
        }
    }

    public void initializeCalendar() {
        calendar = (CalendarView) findViewById(R.id.calendarView);

        // sets whether to show the week number.
        calendar.setShowWeekNumber(false);

        // sets the first day of week according to Calendar.
        // here we set Monday as the first day of the Calendar
        calendar.setFirstDayOfWeek(2);

        //The background color for the selected week.
        calendar.setSelectedWeekBackgroundColor(getResources().getColor(R.color.green));

        //sets the color for the dates of an unfocused month.
        calendar.setUnfocusedMonthDateColor(getResources().getColor(R.color.transparent));

        //sets the color for the separator line between weeks.
        calendar.setWeekSeparatorLineColor(getResources().getColor(R.color.transparent));

        //sets the color for the vertical bar shown at the beginning and at the end of the selected date.
        calendar.setSelectedDateVerticalBar(R.color.darkgreen);

        //sets the listener to be notified upon selected date change.
        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            //show the selected date as a toast
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int day) {
                month=month+1;
                Toast.makeText(getApplicationContext(), day + "-" + month + "-" + year, Toast.LENGTH_SHORT).show();
                String numberAsString = String.valueOf(month);
                StringBuilder sb = new StringBuilder();
                while(sb.length()+numberAsString.length()<2) {
                    sb.append('0');
                }
                sb.append(month);
                String finalmonth = sb.toString();

                String dayAsString = String.valueOf(day);
                StringBuilder sb1 = new StringBuilder();
                while(sb1.length()+dayAsString.length()<2) {
                    sb1.append('0');
                }
                sb1.append(day);
                String finalday = sb1.toString();

                path=main_path+year+"-"+finalmonth+"-"+finalday+"/";
                dialog.show();
            }
        });
    }

    private void nextImage()
    {
        new Thread()
        {
            public void run()
            {
                count++;
                count = (count)%imagesList.length;
                loadImage(path + imagesList[count]);

            }
        }.start();
    }

    private void prevImage()
    {
        new Thread()
        {
            public void run()
            {
                count--;
                if(count < 0)
                {
                    count = imagesList.length-1;
                }
                else {
                    count = (count) % imagesList.length;
                }
                loadImage(path + imagesList[count]);
            }
        }.start();
    }

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                nextImage();
                return false; // Right to left
            }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                prevImage();
                return false; // Left to right
            }

            /*if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                return false; // Bottom to top
            }  else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                return false; // Top to bottom
            }*/
            return false;
        }
    }
}
