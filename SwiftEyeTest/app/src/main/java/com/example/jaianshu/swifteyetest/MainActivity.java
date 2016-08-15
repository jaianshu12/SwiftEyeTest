package com.example.jaianshu.swifteyetest;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
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

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;


public class MainActivity extends Activity  implements View.OnClickListener
{
    SharedPreferences pref; //Stores your application preferences
    SharedPreferences.Editor editor;
    Button nextImage, prevImage, tempBtn, humidityBtn, cameraBtn, burstBtn, moveLeftBtn, moveRightBtn, moveCenterBtn,saveBtn,motionBtn;
    ImageView image;
    Bitmap bmp;
    TextView txtTemp,txtHumid;
    int count=0;
    static boolean motionFlag=false;
    public static final String TAG = "swiftEyeTest";
    //    String main_path = "http://srinivastech.in/anshul/";
    String main_path = "http://52.35.20.220/rpi/";
    String path = main_path + "uploaded_pics/";
    //    String URL_TO_HIT = "http://maniansh.5gbfree.com/uploaded_pics/";
    String [] imagesList = {};
    ArrayList<Bitmap> imgbmp = new ArrayList<Bitmap>();
    //Creating a broadcast receiver for gcm registration
    private BroadcastReceiver mRegistrationBroadcastReceiver;
//    TouchImageView img = (TouchImageView) findViewById(R.id.img);


    @Override
    protected void onStart() {
        super.onStart();
        motionFlag = pref.getBoolean("motion",false);
        if(motionFlag)
            cameraBtn.setEnabled(false);
        else
            cameraBtn.setEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"11111");
        setContentView(R.layout.activity_main);
        Log.i(TAG,"333333333");
        txtTemp = (TextView)findViewById(R.id.txtTemperature);
        txtHumid = (TextView)findViewById(R.id.txtHumidity);
        prevImage = (Button)findViewById(R.id.prevImageBtn);
        nextImage = (Button)findViewById(R.id.nextImageBtn);
        /*tempBtn = (Button)findViewById(R.id.tempBtn);
        humidityBtn = (Button)findViewById(R.id.humidBtn);*/
        cameraBtn = (Button)findViewById(R.id.takePicBtn);
        //burstBtn = (Button)findViewById(R.id.burstBtn);
        saveBtn = (Button)findViewById(R.id.saveBtn);
        motionBtn = (Button)findViewById(R.id.motionBtn);
        moveLeftBtn = (Button)findViewById(R.id.moveLeftBtn);
        moveRightBtn = (Button)findViewById(R.id.moveRightBtn);
        moveCenterBtn = (Button)findViewById(R.id.moveCenterBtn);
        image = (ImageView) findViewById(R.id.imageView);

        prevImage.setOnClickListener(this);
        nextImage.setOnClickListener(this);
        /*tempBtn.setOnClickListener(this);
        humidityBtn.setOnClickListener(this);*/
        cameraBtn.setOnClickListener(this);
        //burstBtn.setOnClickListener(this);
        saveBtn.setOnClickListener(this);
        motionBtn.setOnClickListener(this);
        moveLeftBtn.setOnClickListener(this);
        moveCenterBtn.setOnClickListener(this);
        moveRightBtn.setOnClickListener(this);

        prevImage.setVisibility(View.GONE);
        nextImage.setVisibility(View.GONE);

        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor=pref.edit();

        /*ActionBar actionBar = getActionBar();
        actionBar.show();*/
        //Initializing our broadcast receiver
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {

            //When the broadcast received
            //We are sending the broadcast from GCMRegistrationIntentService

            @Override
            public void onReceive(Context context, Intent intent) {
                //If the broadcast has received with success
                //that means device is registered successfully
                if(intent.getAction().equals(GCMRegistrationIntentService.REGISTRATION_SUCCESS)){
                    //Getting the registration token from the intent
                    String token = intent.getStringExtra("token");
                    //Displaying the token as toast
                    //Toast.makeText(getApplicationContext(), "Registration token:" + token, Toast.LENGTH_LONG).show();
                    Log.i(TAG, "Token  : "+token);

                    //if the intent is not with success then displaying error messages
                } else if(intent.getAction().equals(GCMRegistrationIntentService.REGISTRATION_ERROR)){
                    Toast.makeText(getApplicationContext(), "GCM registration error!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Error  occurred", Toast.LENGTH_LONG).show();
                }
            }
        };

        //Checking play service is available or not
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

        //if play service is not available
        if(ConnectionResult.SUCCESS != resultCode) {
            //If play service is supported but not installed
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                //Displaying message that play service is not installed
                Toast.makeText(getApplicationContext(), "Google Play Service is not install/enabled in this device!", Toast.LENGTH_LONG).show();
                GooglePlayServicesUtil.showErrorNotification(resultCode, getApplicationContext());

                //If play service is not supported
                //Displaying an error message
            } else {
                Toast.makeText(getApplicationContext(), "This device does not support for Google Play Service!", Toast.LENGTH_LONG).show();
            }

            //If play service is available
        } else {
            //Starting intent to register device
            Intent itent = new Intent(this, GCMRegistrationIntentService.class);
            startService(itent);
        }

        final GestureDetector gdt = new GestureDetector(new GestureListener());
        image.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                gdt.onTouchEvent(motionEvent);
                return true;
            }
        });
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
                    Log.i(TAG,"Null Exception");
                }
//                Log.i(TAG,"image lst :"+ imagesList[0] + imagesList[1] + imagesList[2] + "size" + imagesList.length);
                count = imagesList.length-1;
                if(count>=0) {
                    if(imgbmp.get(count)==null)
                    {
                        loadImage(path + imagesList[count]);
                        imgbmp.set(count,bmp);
                    }
                    else
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                image.setImageBitmap(imgbmp.get(count));
                            }
                        });
                    }
                    /*try {
                        imgbmp.get( count );
                        image.setImageBitmap(imgbmp.get(count));
                    } catch ( IndexOutOfBoundsException e ) {
                        loadImage(path + imagesList[count]);
                    }*/

                }
            }
        }.start();
        // Implement Zoom functionality

        //For humidity and temperature
        new Thread()
        {
            public void run()
            {
                while(true) {
                    try {
                        getDHT();
                        Thread.sleep(5000);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    catch (Exception e)
                    {

                    }
                }
//                Log.i(TAG,"image lst :"+ imagesList[0] + imagesList[1] + imagesList[2] + "size" + imagesList.length);
                //loadImage(path+imagesList[count]);
            }
        }.start();

    }

    //Registering receiver on activity resume
    @Override
    protected void onResume() {
        super.onResume();
        Log.w("MainActivity", "onResume");
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntentService.REGISTRATION_SUCCESS));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntentService.REGISTRATION_ERROR));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,new IntentFilter("picuploaded"));
    }


    //Unregistering receiver on activity paused
    @Override
    protected void onPause() {
        super.onPause();
        Log.w("MainActivity", "onPause");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    public void onStop()
    {
        super.onStop();
    }

    private void runTask() throws ExecutionException, InterruptedException, NullPointerException {
        Log.i(TAG, "inside runTask");
        String mL = new MyTask().execute(path).get();
        String [] lst = processValue(mL);
        imagesList = lst;
        for(int i=0;i<imagesList.length;i++)
        {
            imgbmp.add(null);
        }
        Log.i(TAG, " total pics: " + imagesList.length + " last pic : " + imagesList[imagesList.length-1] );
    }

    private void getDHT() throws ExecutionException, InterruptedException, NullPointerException {
        Log.i(TAG, "inside DHT11");
        //String mL = new MyTask().execute(main_path + "getDHT.php").get();
        final String mL = new MyTask().execute(main_path + "flags/dht11.txt").get();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String temp[] = mL.split("-");
                    Log.i(TAG, temp[0] + " " + temp[1]);
                    final String DEGREE = "\u00b0";
                    txtTemp.setText(" " + temp[0] + " " + DEGREE + "C   ");
                    txtHumid.setText(" " + temp[1] + " %   ");
                }
                catch (Exception r)
                {

                }
            }
        });
    }

    private String [] processValue(String result)
    {
        Log.i(TAG, result);
        List<String> allMatches = new ArrayList<String>();
        Matcher m = Pattern.compile("a href=\"(\\w+\\.jpg)\">").matcher(result);
        while (m.find()) {
            allMatches.add(m.group(1));
        }
        Log.i(TAG, "All matches - " + allMatches.toString());
        String mystr = allMatches.toString();
        mystr = mystr.replaceAll("\\[", "").replaceAll("\\]","");
        String [] myList = mystr.split(", ");
        Log.i(TAG, myList.toString() );
        return myList;
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
//            finally {
//                if(connection != null) {
//                    connection.disconnect();
//                }
//                try {
//                    if(reader != null) {
//                        reader.close();
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
//            processValue(result);
//            tvData.setText(result);
        }
    }

    public void loadImage(String imageURL)
    {
        try {
            URL url = new URL(imageURL);
            bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
//            InputStream.res
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    image.setImageBitmap(bmp);
                }
            });
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view)
    {
        if(view.getId()==R.id.prevImageBtn)
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
                    if(imgbmp.get(count)==null)
                    {
                        loadImage(path + imagesList[count]);
                        imgbmp.set(count,bmp);
                    }
                    else
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                image.setImageBitmap(imgbmp.get(count));
                            }
                        });
                    }

                }
            }.start();
        }
        else if(view.getId()==R.id.nextImageBtn)
        {
            new Thread()
            {
                public void run()
                {
                    count++;
                    count = (count)%imagesList.length;
                    if(imgbmp.get(count)==null)
                    {
                        loadImage(path + imagesList[count]);
                        imgbmp.set(count,bmp);
                    }
                    else
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                image.setImageBitmap(imgbmp.get(count));
                            }
                        });
                    }

                }
            }.start();
        }
        /*else if(view.getId()==R.id.tempBtn)
        {

        }
        else if(view.getId()==R.id.humidBtn)
        {

        }*/
        else if(view.getId()==R.id.takePicBtn)
        {
            new Thread()
            {
                public void run()
                {
                    executeUrl(main_path+"takepic.php");
                    //updateImageView();
                }
            }.start();
        }
        /*else if(view.getId()==R.id.burstBtn)
        {

        }*/
        else if(view.getId()==R.id.saveBtn)
        {
            String filename = imagesList[count];
            File sd = Environment.getExternalStorageDirectory();
            File dest = new File(sd, filename);

            Bitmap bitmap = imgbmp.get(count);
            try {
                FileOutputStream out = new FileOutputStream(dest);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();
                Toast.makeText(this, "Image Saved", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        else if(view.getId()==R.id.motionBtn)
        {
            if(!motionFlag) {
                new Thread()
                {
                    public void run()
                    {
                        Log.i(TAG, "to do: setMotion");
                        executeUrl(main_path+"setMotion.php");
                        //updateImageView();
                    }
                }.start();
                cameraBtn.setEnabled(false);
                motionFlag = true;
                editor.putBoolean("motion",true);
                editor.commit();
                Toast.makeText(this, "Motion Detection turned on", Toast.LENGTH_SHORT).show();
                //Launching new Activity
            }
            else
            {
                new Thread()
                {
                    public void run()
                    {
                        Log.i(TAG, "to do: updateMotion");
                        executeUrl(main_path+"updateMotion.php");
                        //updateImageView();
                    }
                }.start();
                motionFlag = false;
                cameraBtn.setEnabled(true);
                editor.putBoolean("motion",false);
                editor.commit();
                Toast.makeText(this, "Motion Detection turned off", Toast.LENGTH_SHORT).show();
            }
        }

        else if(view.getId()==R.id.moveLeftBtn)
        {
            new Thread()
            {
                public void run()
                {
                    executeUrl(main_path+"updateServo.php?servo=L");
                    //updateImageView();
                }
            }.start();

        }
        else if(view.getId()==R.id.moveRightBtn)
        {
            new Thread()
            {
                public void run()
                {
                    executeUrl(main_path+"updateServo.php?servo=R");
                    //updateImageView();
                }
            }.start();
        }
        else if(view.getId()==R.id.moveCenterBtn)
        {
            new Thread()
            {
                public void run()
                {
                    executeUrl(main_path+"updateServo.php?servo=C");
                    //updateImageView();
                }
            }.start();
        }
    }

    public void executeUrl(String takeurl)
    {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(takeurl);
        try {
            HttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                throw new Exception("HTTP status code: " + statusCode + " != " + HttpStatus.SC_OK);
            }
            //-------- Response received successfully. Here play with the data received ---------
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateImageView(){
//        count = 0 ;
        new Thread()
        {
            public void run()
            {
                try {
                    Log.i(TAG, "executing runtask()" );
                    //Thread.sleep(5000);
                    //Thread.sleep(100);
                    runTask();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "inside update view" );
                loadImage(path+imagesList[imagesList.length-1]);
                imgbmp.add(bmp);
                count = imagesList.length-1;
                Log.i(TAG, imagesList[0]+ " " + imagesList[imagesList.length-1]+" inside update view - load image called....tot image =" + imagesList.length );
            }
        }.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_settings:
                Toast.makeText(this, "Settings selected", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_live:
                Intent myIntent = new Intent(MainActivity.this, MotionActivity.class);
                //myIntent.putExtra("key", value); //Optional parameters
                startActivity(myIntent);
                break;
            default:
                break;
        }
        return true;
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            Log.i(TAG, "Pic uploaded");
            String message = intent.getStringExtra("message");
            Log.d("receiver", "Got message: " + message);
            updateImageView();
        }
    };

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                nextImage.callOnClick();
                return false; // Right to left
            }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                prevImage.callOnClick();
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

