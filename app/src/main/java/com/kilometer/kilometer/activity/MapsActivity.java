package com.kilometer.kilometer.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
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

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import com.kilometer.kilometer.R;
import com.kilometer.kilometer.model.EndTripRequest;
import com.kilometer.kilometer.model.EndTripResponse;
import com.kilometer.kilometer.model.EstimationRequest;
import com.kilometer.kilometer.model.EstimationResponse;
import com.kilometer.kilometer.model.Passenger;
import com.kilometer.kilometer.model.StartTripRequest;
import com.kilometer.kilometer.model.StartTripResponse;
import com.kilometer.kilometer.model.StateResponse;
import com.kilometer.kilometer.model.Trip;
import com.kilometer.kilometer.networking.ApiClient;
import com.kilometer.kilometer.networking.ApiInterface;
import com.kilometer.kilometer.util.ApplicationManager;
import com.kilometer.kilometer.util.Constants;
import com.kilometer.kilometer.adapter.PlaceAutocompleteAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements RoutingListener, OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MapsActivity";
    private static final float DEFAULT_ZOOM = 15.0f;
    private static final float ON_TRIP_ZOOM = 14.0f;
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
    @BindView(R.id.estimatedFareTextView)
    TextView estimatedFareTextView;
    @BindView(R.id.infoLayout)
    LinearLayout infoLayout;
    @BindView(R.id.separatorView)
    View separatorView;
    @BindView(R.id.bikeImageView)
    ImageView bikeImageView;
    @BindView(R.id.carImageView)
    ImageView carImageView;
    @BindView(R.id.suvImageView)
    ImageView suvImageView;
    @BindView(R.id.vehicleLayout)
    LinearLayout vehicleLayout;
    @BindView(R.id.bottomLayout)
    LinearLayout bottomLayout;

    private GoogleMap mMap;
    private boolean mLocationPermissionGranted;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GeoDataClient mGeoDataClient;

    private StateResponse stateResponse;
    private ApiInterface apiInterface;
    private EstimationResponse estimationResponse;
    private StartTripResponse startTripResponse;
    private EndTripResponse endTripResponse;

    private String deviceId;
    private String from;
    private String to;
    private String vehicle;
    private String name;
    private String phone;
    private Passenger passenger;
    private com.kilometer.kilometer.model.Location pickUpLocation;
    private com.kilometer.kilometer.model.Location dropOffLocation;
    private Trip trip;

    private String appState = Constants.NORMAL;

    private List<Polyline> polylines = new ArrayList<>();
    private static final int[] COLORS = new int[]{R.color.colorAccent};

    private Marker startMarker;
    private Marker endMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }*/

        //getLocationPermission();

        stateResponse = (StateResponse) getIntent().getSerializableExtra(Constants.STATE_RESPONSE);
        trip = stateResponse.getTrip();
        deviceId = getIntent().getStringExtra(Constants.DEVICE_ID);
        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        mLocationPermissionGranted = getIntent().getBooleanExtra(Constants.LOCATION_PERMISSION,
                false);

        initMap();
    }

    private void initPlacesService() {
        Log.d(TAG, "init:------------------------------------");

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

        if (stateResponse != null && stateResponse.isOnTrip()) {
            showOnTripDetails(stateResponse.getTrip());
        }
    }

    private void showOnTripDetails(Trip trip) {
        Log.d(TAG, "showOnTripDetails: user is on a trip");

        pickUpEditText.setText(trip.getFrom());
        dropOffEditText.setText(trip.getTo());
        disableEditTextAndMyLocation();

        doneButton.setText(getString(R.string.end));
        infoLayout.setVisibility(View.GONE);
        separatorView.setVisibility(View.GONE);
        vehicleLayout.setVisibility(View.GONE);
        appState = Constants.ON_TRIP;

        pickUpLocation = getLocationFromAddress(trip.getFrom());
        dropOffLocation = getLocationFromAddress(trip.getTo());

        drawPath();
    }

    private void setVehicle(String v) {
        switch (v) {
            case "bike":
                bikeImageView.setBackgroundColor(getResources().getColor(android.R.color.white));
                carImageView.setBackgroundColor(getResources().getColor(
                        android.R.color.darker_gray));
                suvImageView.setBackgroundColor(getResources().getColor(
                        android.R.color.darker_gray));
                estimatedFareTextView.setText("Fare: " + estimationResponse.getEstimations()
                        .get(0).getFare() + " Tk");
                vehicle = "bike";
                break;
            case "car":
                bikeImageView.setBackgroundColor(getResources().getColor(
                        android.R.color.darker_gray));
                carImageView.setBackgroundColor(getResources().getColor(
                        android.R.color.white));
                suvImageView.setBackgroundColor(getResources().getColor(
                        android.R.color.darker_gray));
                estimatedFareTextView.setText("Fare: " + estimationResponse.getEstimations()
                        .get(1).getFare() + " Tk");
                vehicle = "car";
                break;
            case "suv":
                bikeImageView.setBackgroundColor(getResources().getColor(
                        android.R.color.darker_gray));
                carImageView.setBackgroundColor(getResources().getColor(
                        android.R.color.darker_gray));
                suvImageView.setBackgroundColor(getResources().getColor(android.R.color.white));
                estimatedFareTextView.setText("Fare: " + estimationResponse.getEstimations()
                        .get(2).getFare() + " Tk");
                vehicle = "suv";
                break;
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

            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setCompassEnabled(false);

            initPlacesService();
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
                        Log.d(TAG, "getDeviceLocation: =============================");
                        Log.d(TAG, "getDeviceLocation: currentLocation: " +
                                currentLocation.toString());
                        Log.d(TAG, "getDeviceLocation: =============================");
                        setCurrentAddress(currentLocation.getLatitude(),
                                currentLocation.getLongitude());
                        moveCamera(new LatLng(currentLocation.getLatitude(),
                                currentLocation.getLongitude()),
                                DEFAULT_ZOOM);
                        pickUpLocation = new com.kilometer.kilometer.model.Location(
                                currentLocation.getLatitude(), currentLocation.getLongitude());
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

    public void setCurrentAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address address = addresses.get(0);
            Log.d(TAG, "setCurrentAddress: address: " + address.toString());
            String currentAddress = address.getAddressLine(0);
            Log.d(TAG, "setCurrentAddress: add: " + currentAddress);

            pickUpEditText.setText(currentAddress);
        } catch (IOException e) {
            Log.e(TAG, "setCurrentAddress: IOException: " + e.getMessage(), e);
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCamera: moving the camera to lat: " + latLng.latitude + ", lng: "
                + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    @OnClick({R.id.clearPickUpImageView, R.id.clearDropOffImageView, R.id.doneButton,
            R.id.bikeImageView, R.id.carImageView, R.id.suvImageView, R.id.myLocationImageButton})
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
        ApplicationManager.hideKeyboard(this);

        switch (appState) {
            case Constants.NORMAL:
                getEstimations();
                break;
            case Constants.ESTIMATIONS:
                showStartDialog();
                break;
            case Constants.ON_TRIP:
                endTrip();
                break;
        }
    }

    private void getEstimations() {
        String pickUp = pickUpEditText.getText().toString().trim();
        String dropOff = dropOffEditText.getText().toString().trim();

        if (TextUtils.isEmpty(pickUp)) {
            Log.d(TAG, "getEstimations: select pick up");
            Toast.makeText(this, "Please enter pick up location",
                    Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(dropOff)) {
            Log.d(TAG, "getEstimations: select drop off");
            Toast.makeText(this, "Please enter drop off location",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!ApplicationManager.hasInternetConnection(this)) {
            Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "getEstimations: no internet connection!");
            return;
        }

        showProgressBar();
        EstimationRequest estimationRequest = new EstimationRequest(pickUp, dropOff);

        Call<EstimationResponse> estimationResponseCall = apiInterface.
                getEstimations(estimationRequest);
        estimationResponseCall.enqueue(new Callback<EstimationResponse>() {
            @Override
            public void onResponse(Call<EstimationResponse> call,
                                   Response<EstimationResponse> response) {
                hideProgressBar();
                Log.d(TAG, "onResponse: ");
                estimationResponse = response.body();

                Log.d(TAG, "onResponse: =========================================");
                Log.d(TAG, "onResponse: estimationResponse: " + estimationResponse);
                Log.d(TAG, "onResponse: =========================================");

                if (estimationResponse == null) {
                    Log.e(TAG, "onResponse: estimationResponse == null");
                    Toast.makeText(MapsActivity.this, "Internal server error!",
                            Toast.LENGTH_SHORT).show();
                } else if (estimationResponse.getStatus().equals(Constants.SUCCESS)) {
                    doneButton.setText(getString(R.string.start));
                    disableEditTextAndMyLocation();
                    appState = Constants.ESTIMATIONS;
                    vehicle = "bike";
                    showEstimationDetails(estimationResponse);
                    dropOffLocation = getLocationFromAddress(dropOffEditText.getText()
                            .toString().trim());
                    drawPath();
                } else if (estimationResponse.getStatus().equals(Constants.ERROR)) {
                    Toast.makeText(MapsActivity.this, estimationResponse.getError(),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MapsActivity.this, "Internal server error!",
                            Toast.LENGTH_SHORT).show();
                }
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
        showInfoLayout();

        distanceTextView.setText("Distance: " + estimationResponse.getDistance() + " km");
        timeTextView.setText("Time: " + estimationResponse.getDuration() + " min");
        estimatedFareTextView.setText("Fare: " + estimationResponse.getEstimations()
                .get(0).getFare() + " Tk");
        bikeImageView.setBackgroundColor(getResources().getColor(android.R.color.white));
    }

    private void showStartDialog() {
        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_dialog_start_trip, null);

        TextInputLayout nameWrapper = dialogView.findViewById(R.id.nameWrapper);
        TextInputLayout phoneWrapper = dialogView.findViewById(R.id.phoneWrapper);
        Button startButton = dialogView.findViewById(R.id.startButton);

        startButton.setOnClickListener(view -> {
            name = nameWrapper.getEditText().getText().toString().trim();
            phone = phoneWrapper.getEditText().getText().toString().trim();

            ApplicationManager.hideKeyboard(this);

            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this, "Please enter passenger name",
                        Toast.LENGTH_SHORT).show();
                return;
            } else if (TextUtils.isEmpty(phone)) {
                Toast.makeText(this, "Please enter passenger phone number",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            startTrip();
            ApplicationManager.hideKeyboard(this);
            dialogBuilder.dismiss();
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    private void startTrip() {
        Log.d(TAG, "startTrip: ");

        if (!ApplicationManager.hasInternetConnection(this)) {
            Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "startTrip: no internet connection!");
            return;
        }

        showProgressBar();

        from = pickUpEditText.getText().toString().trim();
        to = dropOffEditText.getText().toString().trim();
        passenger = new Passenger(name, phone);
        StartTripRequest startTripRequest = new StartTripRequest(deviceId, from, to, vehicle,
                passenger, pickUpLocation);

        Log.d(TAG, "startTrip: =======================================");
        Log.d(TAG, "startTrip: startTripRequest: " + startTripRequest);
        Log.d(TAG, "startTrip: =======================================");

        Call<StartTripResponse> responseCall = apiInterface.startTrip(startTripRequest);
        responseCall.enqueue(new Callback<StartTripResponse>() {
            @Override
            public void onResponse(Call<StartTripResponse> call,
                                   Response<StartTripResponse> response) {
                hideProgressBar();
                startTripResponse = response.body();

                Log.d(TAG, "onResponse: =======================================");
                Log.d(TAG, "onResponse: startTripResponse: " + startTripResponse);
                Log.d(TAG, "onResponse: =======================================");

                if (startTripResponse == null) {
                    Log.e(TAG, "onResponse: startTripResponse == null");
                    Toast.makeText(MapsActivity.this, "Internal server error!",
                            Toast.LENGTH_SHORT).show();
                } else if (startTripResponse.getStatus().equals(Constants.SUCCESS)) {
                    doneButton.setText(getResources().getString(R.string.end));
                    hideInfoLayout();
                    appState = Constants.ON_TRIP;
                    trip = startTripResponse.getTrip();
                } else if (startTripResponse.getStatus().equals(Constants.ERROR)) {
                    Toast.makeText(MapsActivity.this, startTripResponse.getError(),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MapsActivity.this, "Internal server error!",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<StartTripResponse> call, Throwable t) {
                hideProgressBar();
                Log.e(TAG, "onFailure: " + t.getMessage(), t);
                Toast.makeText(MapsActivity.this, "Internal server error!",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void drawPath() {
        showProgressBar();

        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(pickUpLocation.getLat(), pickUpLocation.getLng()),
                        new LatLng(dropOffLocation.getLat(), dropOffLocation.getLng()))
                .key(getString(R.string.server_key))
                .build();
        routing.execute();
    }

    private void hideInfoLayout() {
        infoLayout.setVisibility(View.GONE);
        separatorView.setVisibility(View.GONE);
        vehicleLayout.setVisibility(View.GONE);
    }

    private void showInfoLayout() {
        infoLayout.setVisibility(View.VISIBLE);
        separatorView.setVisibility(View.VISIBLE);
        vehicleLayout.setVisibility(View.VISIBLE);
    }

    private void endTrip() {
        Log.d(TAG, "endTrip: ");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        showProgressBar();

        try {
            if (mLocationPermissionGranted) {
                Task locationTask = mFusedLocationProviderClient.getLastLocation();
                locationTask.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: location found");
                        Location currentLocation = (Location) task.getResult();
                        Log.d(TAG, "getDeviceLocation: =====================================");
                        Log.d(TAG, "getDeviceLocation: currentLocation: " +
                                currentLocation.toString());
                        Log.d(TAG, "getDeviceLocation: =====================================");
                        com.kilometer.kilometer.model.Location endLocation =
                                new com.kilometer.kilometer.model.Location(
                                        currentLocation.getLatitude(),
                                        currentLocation.getLongitude());
                        if (ApplicationManager.hasInternetConnection(this)) {
                            callEndTripService(endLocation);
                        } else {
                            Log.e(TAG, "endTrip: No internet connection!");
                            Toast.makeText(this, "No internet connection!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "onComplete: current location is null");
                        Toast.makeText(MapsActivity.this,
                                "Unable to get current location!",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage(), e);
        }
    }

    private void callEndTripService(com.kilometer.kilometer.model.Location endLocation) {
        Log.d(TAG, "callEndTripService: ");
        EndTripRequest endTripRequest = new EndTripRequest(deviceId, endLocation);

        Log.d(TAG, "callEndTripService: ====================================================");
        Log.d(TAG, "callEndTripService: endTripRequest: " + endTripRequest);
        Log.d(TAG, "callEndTripService: ====================================================");

        Call<EndTripResponse> responseCall = apiInterface.endTrip(trip.getId(), endTripRequest);
        responseCall.enqueue(new Callback<EndTripResponse>() {
            @Override
            public void onResponse(Call<EndTripResponse> call, Response<EndTripResponse> response) {
                Log.d(TAG, "onResponse: ");
                hideProgressBar();

                endTripResponse = response.body();

                Log.d(TAG, "onResponse: ====================================================");
                Log.d(TAG, "onResponse: endTripResponse: " + endTripResponse);
                Log.d(TAG, "onResponse: ====================================================");

                if (endTripResponse == null) {
                    Log.e(TAG, "onResponse: endTripResponse == null");
                    Toast.makeText(MapsActivity.this, "Internal server error!",
                            Toast.LENGTH_SHORT).show();
                } else if (endTripResponse.getStatus().equals(Constants.SUCCESS)) {
                    showEndDialog();
                } else if (endTripResponse.getStatus().equals(Constants.ERROR)) {
                    Log.e(TAG, "onResponse: endTripResponse.getError() == " +
                            endTripResponse.getError());
                    Toast.makeText(MapsActivity.this, endTripResponse.getError(),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "onResponse: else");
                    Toast.makeText(MapsActivity.this, "Internal server error!",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<EndTripResponse> call, Throwable t) {
                hideProgressBar();
                Log.e(TAG, "onFailure: " + t.getMessage(), t);
                Toast.makeText(MapsActivity.this, "Internal server error!",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEndDialog() {
        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_dialog_end_trip, null);

        TextView nameTextView = dialogView.findViewById(R.id.nameTextView);
        TextView phoneTextView = dialogView.findViewById(R.id.phoneTextView);
        TextView fareTextView = dialogView.findViewById(R.id.fareTextView);
        Button doneTripButton = dialogView.findViewById(R.id.doneTripButton);

        nameTextView.setText(trip.getPassenger().getName());
        phoneTextView.setText(trip.getPassenger().getPhone());
        fareTextView.setText("Fare: " + endTripResponse.getFare());

        doneTripButton.setOnClickListener(view -> {
            ApplicationManager.hideKeyboard(this);
            setViewsToInitialLook();
            appState = Constants.NORMAL;
            getDeviceLocation();
            dialogBuilder.dismiss();
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    private void setViewsToInitialLook() {
        enableEditTextAndMyLocation();
        bikeImageView.setBackgroundColor(getResources().getColor(android.R.color.white));
        carImageView.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        suvImageView.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        removePath();
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
        progressBar.setVisibility(View.INVISIBLE);
    }

    private com.kilometer.kilometer.model.Location getLocationFromAddress(String address) {
        Log.d(TAG, "getLocationFromAddress: ");
        Geocoder coder = new Geocoder(this);
        com.kilometer.kilometer.model.Location location =
                new com.kilometer.kilometer.model.Location();
        try {
            ArrayList<Address> addresses = (ArrayList<Address>) coder.getFromLocationName(
                    address, 5);
            Address foundAddress = addresses.get(0);
            location = new com.kilometer.kilometer.model.Location(foundAddress.getLatitude(),
                    foundAddress.getLongitude());
            Log.d(TAG, "getLocationFromAddress: =============================");
            Log.d(TAG, "getLocationFromAddress: foundAddress: " + foundAddress);
            Log.d(TAG, "getLocationFromAddress: =============================");
        } catch (IOException e) {
            Log.e(TAG, "getLocationFromAddress: IOException: " + e.getMessage(), e);
        }

        return location;
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        hideProgressBar();
        Log.e(TAG, "onRoutingFailure: RouteException: " + e.getMessage(), e);
        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRoutingStart() {
        Log.d(TAG, "onRoutingStart: ");
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        hideProgressBar();
        /*CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(pickUpLocation.getLat(),
                pickUpLocation.getLng()));*/

        LatLng source = new LatLng(pickUpLocation.getLat(),
                pickUpLocation.getLng());
        moveCamera(source, ON_TRIP_ZOOM);

        if (polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i < route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Log.d(TAG, "Route " + (i + 1) + ": distance - " + route.get(i).getDistanceValue() +
                    ": duration - " + route.get(i).getDurationValue());
        }

        // Start marker
        MarkerOptions options = new MarkerOptions();
        options.position(new LatLng(pickUpLocation.getLat(), pickUpLocation.getLng()));
        options.icon(ApplicationManager.bitmapDescriptorFromVector(this,
                R.drawable.ic_pick_up));
        startMarker = mMap.addMarker(options);

        // End marker
        options = new MarkerOptions();
        options.position(new LatLng(dropOffLocation.getLat(), dropOffLocation.getLng()));
        options.icon(ApplicationManager.bitmapDescriptorFromVector(this,
                R.drawable.ic_drop_off));
        endMarker = mMap.addMarker(options);
    }

    @Override
    public void onRoutingCancelled() {
        Log.e(TAG, "onRoutingCancelled: ");
        hideProgressBar();
    }

    private void removePath() {
        for (Polyline line : polylines) {
            line.remove();
        }
        polylines.clear();
        if (startMarker != null) startMarker.remove();
        if (endMarker != null) endMarker.remove();
    }

    @Override
    public void onBackPressed() {
        if (appState.equals(Constants.ESTIMATIONS)) {
            appState = Constants.NORMAL;
            enableEditTextAndMyLocation();
            hideInfoLayout();
            doneButton.setText(getString(R.string.done));
            removePath();
            getDeviceLocation();
        } else {
            super.onBackPressed();
        }
    }

    private void enableEditTextAndMyLocation() {
        doneButton.setText(getResources().getString(R.string.done));
        pickUpEditText.setText("");
        pickUpEditText.setEnabled(true);
        dropOffEditText.setText("");
        dropOffEditText.setEnabled(true);
        clearPickUpImageView.setEnabled(true);
        clearDropOffImageView.setEnabled(true);
        myLocationImageButton.setEnabled(true);
    }

    private void disableEditTextAndMyLocation() {
        pickUpEditText.setEnabled(false);
        dropOffEditText.setEnabled(false);
        clearPickUpImageView.setEnabled(false);
        clearDropOffImageView.setEnabled(false);
        myLocationImageButton.setEnabled(false);
    }
}
