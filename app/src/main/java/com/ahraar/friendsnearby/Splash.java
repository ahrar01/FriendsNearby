package com.ahraar.friendsnearby;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;

import com.ahraar.friendsnearby.Activity.Login;

public class Splash extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 3000;
    private boolean InternetCheck = true;
    private boolean flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        flag = false;

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {

                boolean InternetResult = checkConnection();
                // This method will be executed once the timer is over
                // Start your app main activity
                if (InternetResult) {
                    Intent intent = new Intent(getApplicationContext(), Login.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    DialogAppear();
                }

            }
        }, SPLASH_TIME_OUT);

    }

    //DialogBox Main Function
    public void DialogAppear() {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                Splash.this);

        builder.setTitle("Network Error");   //Title
        builder.setMessage("No Internet Connectivity");//Message
        builder.setCancelable(false);


        //Negative Message
        builder.setNegativeButton("Exit",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int which) {

                        /* close this activity
                         *  When Exit is clicked
                         */
                        finish();

                    }
                });

        builder.show();
    }


    //Check Internet status of the mobile
    protected boolean isOnline() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    //Return Internet Status of the Mobile
    public boolean checkConnection() {
        if (isOnline()) {
            return InternetCheck;
        } else {
            InternetCheck = false;
            return InternetCheck;

        }

    }

}
