package com.kilometer.kilometer.networking;

import com.kilometer.kilometer.model.EstimationRequest;
import com.kilometer.kilometer.model.EstimationResponse;
import com.kilometer.kilometer.model.StateResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET("/services/state?deviceId")
    Call<StateResponse> getState(@Query("device id") String deviceId);

    @POST("/services/estimation")
    Call<EstimationResponse> getEstimations(@Body EstimationRequest estimationRequest);
}
