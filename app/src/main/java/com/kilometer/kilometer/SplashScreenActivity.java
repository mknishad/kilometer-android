package com.kilometer.kilometer;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class SplashScreenActivity extends AppCompatActivity {

    private static final String TAG = "SplashScreenActivity";
    private static final int ERROR_DIALOG_REQUEST = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isPlayServicesOk()) {
            init();
        }
    }

    private void init() {
        startActivity(new Intent(getApplicationContext(), MapsActivity.class));
        finish();
    }

    private boolean isPlayServicesOk() {
        Log.d(TAG, "isPlayServicesOk: checking google play services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(SplashScreenActivity.this);
        if (available == ConnectionResult.SUCCESS) {
            Log.d(TAG, "isPlayServicesOk: google play services is ok");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Log.d(TAG, "isPlayServicesOk: and error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(SplashScreenActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(SplashScreenActivity.this, "You can't make map requests!", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}
