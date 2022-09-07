package com.datasite.timestamps.client.api;

import com.datasite.timestamps.model.TimestampRequest;
import com.datasite.timestamps.model.TimestampResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface TimestampsControllerApi {

    /**
     *
     * @param authorization    Authorization token (required)
     * @param timestampRequest timestampRequest (required)
     * @return Returns a TimestampResponse with a timestamp denoting when the requested context was last updated
     */
    @Headers({
        "Content-Type:application/json"
    })
    @POST("/search")
    Call<TimestampResponse> search(@Header("Authorization") String authorization, @Body TimestampRequest timestampRequest);
}
