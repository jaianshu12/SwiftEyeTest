package com.example.jaianshu.swifteyetest;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class LogoutActivity extends Activity {
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);
        tv = (TextView)findViewById(R.id.textLogout);
        signout();
    }

    public void signout(){
        Log.i("LogoutAct", "inside logout");
        SharedPreferences preferences =getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
        tv.setText("Logout Successful !!");
        finish();
    }
}
