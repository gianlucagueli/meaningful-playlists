package com.meaningfulplaylists.infrastructure.retrofit;

import com.meaningfulplaylists.infrastructure.interceptors.SpotifyAuthInterceptor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
public class RetrofitUtils {
    private final SpotifyAuthInterceptor spotifyAuthInterceptor;
    private final HttpLoggingInterceptor httpLoggingInterceptor;

    RetrofitUtils(SpotifyAuthInterceptor spotifyAuthInterceptor,
                  @Value("${spring.config.environment}") String environment) {
        this.spotifyAuthInterceptor = spotifyAuthInterceptor;
        this.httpLoggingInterceptor = createHttpLoggingInterceptor(environment);
    }

    public Retrofit buildRetrofit(String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(buildOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public Retrofit buildRetrofitWithAuthInterceptor(String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(buildOkHttpClientWithAuthInterceptor())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static <T> Optional<T> safeExecute(Call<T> call) {
        try {
            Response<T> response = call.execute();
            if (response.isSuccessful()) {
                 return Optional.ofNullable(response.body());
            }
            log.error("Unsuccessful Retrofit response from call [{}]: code={}, error={}", call.request().url(), response.code(), response.message());
        } catch (IOException e) {
            log.error("IOException executing call: {}", call, e);
        }

        return Optional.empty();
    }

    private OkHttpClient buildOkHttpClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .build();
    }

    private OkHttpClient buildOkHttpClientWithAuthInterceptor() {
        return new OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor(spotifyAuthInterceptor)
                .build();
    }

    private HttpLoggingInterceptor createHttpLoggingInterceptor(String environment) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel("prod".equals(environment)
                ? HttpLoggingInterceptor.Level.BASIC
                : HttpLoggingInterceptor.Level.BODY
        );
        return interceptor;
    }

}
