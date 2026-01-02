package com.example.smartwallet;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("payments") // Corrected Endpoint
    Call<PaymentResponse> processPayment(@Body PaymentRequest request);
}