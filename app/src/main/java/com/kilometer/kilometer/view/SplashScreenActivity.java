package com.kilometer.kilometer.view;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.kilometer.kilometer.model.StateResponse;
import com.kilometer.kilometer.networking.ApiClient;
import com.kilometer.kilometer.networking.ApiInterface;
import com.kilometer.kilometer.util.ApplicationManager;
import com.kilometer.kilometer.util.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashScreenActivity extends AppCompatActivity {

    private static final String TAG = "SplashScreenActivity";
    private static final int ERROR_DIALOG_REQUEST = 1000;

    private String androidId;
    private ApiInterface apiInterface;
    private StateResponse stateResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isPlayServicesOk()) {
            init();
        }
    }

    private boolean isPlayServicesOk() {
        Log.d(TAG, "isPlayServicesOk: checking google play services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(SplashScreenActivity.this);
        if (available == ConnectionResult.SUCCESS) {
            Log.d(TAG, "isPlayServicesOk: google play services is ok");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Log.d(TAG, "isPlayServicesOk: and error occurred but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(SplashScreenActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Log.e(TAG, "isPlayServicesOk: google play services is unavailable!");
            Toast.makeText(SplashScreenActivity.this, "You can't make map requests!", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void init() {
        Log.d(TAG, "init: initializing...");
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d(TAG, "init: androidId: " + androidId);
        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

        if (ApplicationManager.hasInternetConnection(this)) {
            getTripState();
        } else {
            Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "init: no internet connection!");
        }
    }

    private void getTripState() {
        Call<StateResponse> stateResponseCall = apiInterface.getState(androidId);
        stateResponseCall.enqueue(new Callback<StateResponse>() {
            @Override
            public void onResponse(Call<StateResponse> call, Response<StateResponse> response) {
                stateResponse = response.body();

                if (stateResponse == null) {
                    Log.e(TAG, "onResponse: stateResponse == null");
                    Toast.makeText(SplashScreenActivity.this, "Internal server error!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d(TAG, "onResponse:================================");
                Log.d(TAG, "onResponse: stateResponse: " + stateResponse);
                Log.d(TAG, "onResponse:================================");

                openMap();
            }

            @Override
            public void onFailure(Call<StateResponse> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage(), t);
                Toast.makeText(SplashScreenActivity.this, "Internal server error!",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void openMap() {
        Log.d(TAG, "openMap: opening map activity...");
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        intent.putExtra(Constants.STATE_RESPONSE, stateResponse);
        startActivity(intent);
        finish();
    }
}
