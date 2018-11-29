package com.kilometer.kilometer.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.kilometer.kilometer.model.StateResponse;
import com.kilometer.kilometer.networking.ApiClient;
import com.kilometer.kilometer.networking.ApiInterface;
import com.kilometer.kilometer.util.ApplicationManager;
import com.kilometer.kilometer.util.Constants;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashScreenActivity extends AppCompatActivity {

    private static final String TAG = "SplashScreenActivity";
    private static final int ERROR_DIALOG_REQUEST = 1000;
    private static final int REQUEST_CHECK_SETTINGS = 1001;

    private String androidId;
    private ApiInterface apiInterface;
    private StateResponse stateResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

        checkServerStatus();

        if (isPlayServicesOk()) {
            createLocationRequest();
        } else {
            finish();
        }
    }

    private void checkServerStatus() {
        Call<ResponseBody> call = apiInterface.checkServerStatus();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "onResponse: " + response.body());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage(), t);
            }
        });
    }

    private boolean isPlayServicesOk() {
        Log.d(TAG, "isPlayServicesOk: checking google play services version");

        int available = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(SplashScreenActivity.this);
        if (available == ConnectionResult.SUCCESS) {
            Log.d(TAG, "isPlayServicesOk: google play services is ok");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Log.d(TAG, "isPlayServicesOk: and error occurred but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance()
                    .getErrorDialog(SplashScreenActivity.this,
                            available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Log.e(TAG, "isPlayServicesOk: google play services is unavailable!");
            Toast.makeText(SplashScreenActivity.this, "You can't make map requests!",
                    Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    protected void createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, locationSettingsResponse -> {
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
            init();
        });

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(SplashScreenActivity.this,
                            REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                }
            }
        });
    }

    private void init() {
        Log.d(TAG, "init: initializing...");
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d(TAG, "init: androidId: " + androidId);

        getTripState();
    }

    private void getTripState() {
        if (!ApplicationManager.hasInternetConnection(this)) {
            Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "getTripState: No internet connection!");
            openMap();
        } else {
            Call<StateResponse> stateResponseCall = apiInterface.getState(androidId);
            stateResponseCall.enqueue(new Callback<StateResponse>() {
                @Override
                public void onResponse(Call<StateResponse> call, Response<StateResponse> response) {
                    stateResponse = response.body();
                    Log.d(TAG, "onResponse:================================");
                    Log.d(TAG, "onResponse: stateResponse: " + stateResponse);
                    Log.d(TAG, "onResponse:================================");

                    if (stateResponse == null) {
                        Log.e(TAG, "onResponse: stateResponse == null");
                        Toast.makeText(SplashScreenActivity.this,
                                "Internal server error!",
                                Toast.LENGTH_SHORT).show();
                    }

                    openMap();
                }

                @Override
                public void onFailure(Call<StateResponse> call, Throwable t) {
                    Log.e(TAG, "onFailure: " + t.getMessage(), t);
                    Toast.makeText(SplashScreenActivity.this,
                            "Internal server error!",
                            Toast.LENGTH_SHORT).show();
                    openMap();
                }
            });
        }
    }

    private void openMap() {
        Log.d(TAG, "openMap: opening map activity...");
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        intent.putExtra(Constants.STATE_RESPONSE, stateResponse);
        intent.putExtra(Constants.DEVICE_ID, androidId);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        init();
                        break;
                    case Activity.RESULT_CANCELED:
                        finish();
                }
        }
    }
}
