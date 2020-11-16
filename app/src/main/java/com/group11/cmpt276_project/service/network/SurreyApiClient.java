package com.group11.cmpt276_project.service.network;

import com.group11.cmpt276_project.utils.Constants;

import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class SurreyApiClient {

    private static class SurreyApiClientHolder {
        private static final Retrofit INSTANCE = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
    }

    public static Retrofit getInstance() {
        return SurreyApiClientHolder.INSTANCE;
    }
}
