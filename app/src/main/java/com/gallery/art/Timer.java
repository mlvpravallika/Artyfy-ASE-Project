package com.gallery.art;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.widget.Toast;

public class Timer {


    private CountDownTimer CDT;
    ProgressDialog progressDialog;
    Context c;
    int i = 0;

    public Timer(ProgressDialog progressDialog, Context c) {
        this.progressDialog = progressDialog;
        this.c = c;
    }

    public void count() {
        if (!(new CheckNetwork(c).isNetworkAvailable())) {
            progressDialog.dismiss();
            Toast.makeText(c, "check connection", Toast.LENGTH_LONG).show();
            /*if((Activity) c instanceof LoginActivity){
                ((Activity) c).finish();
                ((Activity) c).startActivity(new Intent(c, LoginActivity.class));
            }
            else {
                ((Activity) c).finish();
                ((Activity) c).startActivity(new Intent(c, DashBoardActivity.class));
            }*/
        }
        else {
            CDT = new CountDownTimer(30000, 1000) {
                public void onTick(long m) {
                    i++;
                    if (i == 10) {
                        progressDialog.setMessage("processing");
                    }
                    if (i == 20) {
                        progressDialog.setMessage("slow network connection");
                    }
                }

                public void onFinish() {
                    i = 0;
                    progressDialog.dismiss();
                    Toast.makeText(c, "check connection", Toast.LENGTH_LONG).show();
                    if((Activity) c instanceof LoginActivity){
                        ((Activity) c).finish();
                        ((Activity) c).startActivity(new Intent(c, UserActivity.class));
                    }
                    else {

                        ((Activity) c).finish();
                        ((Activity) c).startActivity(new Intent(c, RegisterActivity.class));
                    }
                }
            }
            //.start()
            ;
        }
    }
}