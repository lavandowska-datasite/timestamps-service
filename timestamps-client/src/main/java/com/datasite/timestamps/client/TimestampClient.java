package com.datasite.timestamps.client;

import com.datasite.timestamps.client.api.TimestampsControllerApi;

/**
 * Use the {@link com.datasite.retrofit.client.DatasiteRetrofitClientBuilder} to instantiate a client
 *
 * <pre>{@code
 *     timestampClient = new DatasiteRetrofitClientBuilder<>(TimestampClient.class)
 *      .setBaseUrl(getConfig().targetHost)
 *      .setHttpLoggingInterceptorLevel(HttpLoggingInterceptor.Level.BODY)
 *      .setHitIdPrefix("acc-test-")
 *      .setTimeoutSeconds(30)
 *      .setMeterRegistry(new SimpleMeterRegistry())
 *      .build()
 * }</pre>
 */
public interface TimestampClient extends TimestampsControllerApi {
}
