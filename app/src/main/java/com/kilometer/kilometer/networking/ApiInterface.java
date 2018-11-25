package com.kilometer.kilometer.networking;

import com.kilometer.kilometer.model.EstimationRequest;
import com.kilometer.kilometer.model.EstimationResponse;
import com.kilometer.kilometer.model.StartTripRequest;
import com.kilometer.kilometer.model.StartTripResponse;
import com.kilometer.kilometer.model.StateResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET("/services")
    Call<ResponseBody> checkServerStatus();

    @GET("/services/state")
    Call<StateResponse> getState(@Query("deviceId") String deviceId);

    @POST("/services/estimation")
    Call<EstimationResponse> getEstimations(@Body EstimationRequest estimationRequest);

    @POST("/services/trips")
    Call<StartTripResponse> startTrip(@Body StartTripRequest startTripRequest);
}
