package com.kilometer.kilometer.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.tasks.Task;
import com.kilometer.kilometer.R;
import com.kilometer.kilometer.model.EstimationRequest;
import com.kilometer.kilometer.model.EstimationResponse;
import com.kilometer.kilometer.model.StateResponse;
import com.kilometer.kilometer.model.Trip;
import com.kilometer.kilometer.networking.ApiClient;
import com.kilometer.kilometer.networking.ApiInterface;
import com.kilometer.kilometer.util.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MapsActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final float DEFAULT_ZOOM = 15.0f;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136));

    @BindView(R.id.pickUpEditText)
    AutoCompleteTextView pickUpEditText;
    @BindView(R.id.clearPickUpImageView)
    ImageView clearPickUpImageView;
    @BindView(R.id.dropOffEditText)
    AutoCompleteTextView dropOffEditText;
    @BindView(R.id.clearDropOffImageView)
    ImageView clearDropOffImageView;
    @BindView(R.id.dropOffLayout)
    LinearLayout dropOffLayout;
    @BindView(R.id.myLocationImageButton)
    ImageButton myLocationImageButton;
    @BindView(R.id.doneButton)
    Button doneButton;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
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
    @BindView(R.id.bikeImageView)
    ImageView motorCycleImageView;
    @BindView(R.id.carImageView)
    ImageView carImageView;
    @BindView(R.id.suvImageView)
    ImageView microImageView;
    @BindView(R.id.vehicleLayout)
    LinearLayout vehicleLayout;
    @BindView(R.id.bottomLayout)
    LinearLayout bottomLayout;

    private GoogleMap mMap;
    private boolean mLocationPermissionGranted;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;
    private GeoDataClient mGeoDataClient;

    private StateResponse stateResponse;
    private ApiInterface apiInterface;
    private EstimationResponse estimationResponse;

    private String vehicle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        getLocationPermission();

        stateResponse = (StateResponse) getIntent().getSerializableExtra(Constants.STATE_RESPONSE);
        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
    }

    private void init() {
        Log.d(TAG, "init:------------------------------------");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        mGeoDataClient = Places.getGeoDataClient(this);

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this, mGeoDataClient,
                LAT_LNG_BOUNDS, null);

        pickUpEditText.setAdapter(mPlaceAutocompleteAdapter);
        dropOffEditText.setAdapter(mPlaceAutocompleteAdapter);

        pickUpEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                    || keyEvent.getAction() == keyEvent.KEYCODE_ENTER) {
                closeSoftKeyboard();
            }
            return false;
        });

        pickUpEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                    || keyEvent.getAction() == keyEvent.KEYCODE_ENTER) {
                closeSoftKeyboard();
            }
            return false;
        });

        if (stateResponse.isOnTrip()) {
            showOnTripDetails(stateResponse.getTrip());
        }
    }

    private void showOnTripDetails(Trip trip) {
        Log.d(TAG, "showOnTripDetails: user is on a trip");

        pickUpEditText.setText(trip.getFrom());
        dropOffEditText.setText(trip.getTo());

        doneButton.setText(getString(R.string.end));
        setTripDetails(trip);
        infoLayout.setEnabled(false);
        setVehicle(trip.getVehicle());
        vehicleLayout.setEnabled(false);
    }

    private void setTripDetails(Trip trip) {

    }

    private void setVehicle(String vehicle) {
        switch (vehicle) {
            case "bike":
                motorCycleImageView.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                carImageView.setBackgroundColor(getResources().getColor(android.R.color.white));
                microImageView.setBackgroundColor(getResources().getColor(android.R.color.white));
                fareTextView.setText("Fare: " + estimationResponse.getEstimations().get(0).getFare() + " BDT");
                vehicle = "bike";
                break;
            case "car":
                motorCycleImageView.setBackgroundColor(getResources().getColor(android.R.color.white));
                carImageView.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                microImageView.setBackgroundColor(getResources().getColor(android.R.color.white));
                fareTextView.setText("Fare: " + estimationResponse.getEstimations().get(1).getFare() + " BDT");
                vehicle = "car";
                break;
            case "suv":
                motorCycleImageView.setBackgroundColor(getResources().getColor(android.R.color.white));
                carImageView.setBackgroundColor(getResources().getColor(android.R.color.white));
                microImageView.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                fareTextView.setText("Fare: " + estimationResponse.getEstimations().get(0).getFare() + " BDT");
                vehicle = "suv";
                break;
        }
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

        applyCustomMapStyle();

        if (mLocationPermissionGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            init();
        }
    }

    private void applyCustomMapStyle() {
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_style));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
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

    @OnClick({R.id.clearPickUpImageView, R.id.clearDropOffImageView, R.id.doneButton, R.id.bikeImageView, R.id.carImageView, R.id.suvImageView, R.id.myLocationImageButton})
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
            case R.id.bikeImageView:
                setVehicle("bike");
                break;
            case R.id.carImageView:
                setVehicle("car");
                break;
            case R.id.suvImageView:
                setVehicle("suv");
                break;
            case R.id.myLocationImageButton:
                getDeviceLocation();
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
        getEstimations();
    }

    private void getEstimations() {
        String pickUp = pickUpEditText.getText().toString().trim();
        String dropOff = dropOffEditText.getText().toString().trim();

        if (TextUtils.isEmpty(pickUp) || TextUtils.isEmpty(dropOff)) {
            Log.d(TAG, "getEstimations: select pick up and drop off");
            Toast.makeText(this, "Please select pick up and drop off location",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        showProgressBar();
        EstimationRequest estimationRequest = new EstimationRequest(pickUp, dropOff);

        Call<EstimationResponse> estimationResponseCall = apiInterface.getEstimations(estimationRequest);
        estimationResponseCall.enqueue(new Callback<EstimationResponse>() {
            @Override
            public void onResponse(Call<EstimationResponse> call, Response<EstimationResponse> response) {
                hideProgressBar();
                Log.d(TAG, "onResponse: ");
                estimationResponse = response.body();

                Log.d(TAG, "onResponse: =========================================");
                Log.d(TAG, "onResponse: estimationResponse: " + estimationResponse);
                Log.d(TAG, "onResponse: =========================================");

                if (estimationResponse == null) {
                    Log.e(TAG, "onResponse: response == null");
                    Toast.makeText(MapsActivity.this, "Internal server error!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                doneButton.setText(getString(R.string.start));

                showEstimationDetails(estimationResponse);
            }

            @Override
            public void onFailure(Call<EstimationResponse> call, Throwable t) {
                hideProgressBar();
                Log.e(TAG, "onFailure: " + t.getMessage(), t);
                Toast.makeText(MapsActivity.this, "Internal server error!",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEstimationDetails(EstimationResponse estimationResponse) {
        infoLayout.setVisibility(View.VISIBLE);
        separatorView.setVisibility(View.VISIBLE);
        vehicleLayout.setVisibility(View.VISIBLE);

        distanceTextView.setText("Distance: " + estimationResponse.getDistance() + " km");
        timeTextView.setText("Time: " + estimationResponse.getDuration() + " min");
        fareTextView.setText("Fare: " + estimationResponse.getEstimations().get(0).getFare() + " BDT");
        motorCycleImageView.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void closeSoftKeyboard() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }
}
