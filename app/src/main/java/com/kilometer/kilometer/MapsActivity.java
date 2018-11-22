package com.kilometer.kilometer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final float DEFAULT_ZOOM = 15.0f;

    @BindView(R.id.pickUpEditText)
    EditText pickUpEditText;
    @BindView(R.id.clearPickUpImageView)
    ImageView clearPickUpImageView;
    @BindView(R.id.dropOffEditText)
    EditText dropOffEditText;
    @BindView(R.id.clearDropOffImageView)
    ImageView clearDropOffImageView;
    @BindView(R.id.dropOffLayout)
    LinearLayout dropOffLayout;
    @BindView(R.id.doneButton)
    Button doneButton;
    @BindView(R.id.distanceTextView)
    TextView distanceTextView;
    @BindView(R.id.timeTextView)
    TextView timeTextView;
    @BindView(R.id.fareTextView)
    TextView fareTextView;
    @BindView(R.id.infoLayout)
    LinearLayout infoLayout;
    @BindView(R.id.separatorView)
    View separatorView;
    @BindView(R.id.motorCycleImageView)
    ImageView motorCycleImageView;
    @BindView(R.id.carImageView)
    ImageView carImageView;
    @BindView(R.id.microImageView)
    ImageView microImageView;
    @BindView(R.id.vehicleLayout)
    LinearLayout vehicleLayout;
    @BindView(R.id.bottomLayout)
    LinearLayout bottomLayout;

    private GoogleMap mMap;
    private boolean mLocationPermissionGranted;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);

        // prevent keyboard opening on activity startup
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        getLocationPermission();
    }

    private void initEditTexts() {
        Log.d(TAG, "initEditTexts: ");

        pickUpEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                    || keyEvent.getAction() == keyEvent.KEYCODE_ENTER) {
                // execute method for searching
                //geoLocate();
            }
            return false;
        });
    }

    private void geoLocate() {
        Log.d(TAG, "geoLocate: geolocating...");

        String searchString = pickUpEditText.getText().toString();
        Geocoder geocoder = new Geocoder(MapsActivity.this);
        List<Address> addressList = new ArrayList<>();

        try {
            addressList = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException e) {
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage(), e);
        }

        if (addressList.size() > 0) {
            Address address = addressList.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());
        }
    }

    private void initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: Map is ready");
        mMap = googleMap;

        if (mLocationPermissionGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            initEditTexts();
        }
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting devices current location...");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionGranted) {
                Task locationTask = mFusedLocationProviderClient.getLastLocation();
                locationTask.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: location fount");
                        Location currentLocation = (Location) task.getResult();
                        moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                DEFAULT_ZOOM);
                    } else {
                        Log.e(TAG, "onComplete: current location is null");
                        Toast.makeText(MapsActivity.this, "Unable to get current location!",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage(), e);
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCamera: moving the camera to lat: " + latLng.latitude + ", lng: "
                + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            Log.e(TAG, "onRequestPermissionsResult: permission failed!");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted!");
                    mLocationPermissionGranted = true;
                    initMap();
                }
        }
    }

    @OnClick({R.id.clearPickUpImageView, R.id.clearDropOffImageView, R.id.doneButton, R.id.motorCycleImageView, R.id.carImageView, R.id.microImageView})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.clearPickUpImageView:
                clearPickUp();
                break;
            case R.id.clearDropOffImageView:
                clearDropOff();
                break;
            case R.id.doneButton:
                done();
                break;
            case R.id.motorCycleImageView:
                break;
            case R.id.carImageView:
                break;
            case R.id.microImageView:
                break;
        }
    }

    private void clearPickUp() {
        pickUpEditText.setText("");
    }

    private void clearDropOff() {
        dropOffEditText.setText("");
    }

    private void done() {
        Log.d(TAG, "done: ");
        doneButton.setText("Start");
        infoLayout.setVisibility(View.VISIBLE);
        separatorView.setVisibility(View.VISIBLE);
        vehicleLayout.setVisibility(View.VISIBLE);
    }
}
