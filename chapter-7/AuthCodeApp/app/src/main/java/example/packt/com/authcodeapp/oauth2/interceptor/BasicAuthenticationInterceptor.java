package example.packt.com.authcodeapp.oauth2.interceptor;

import android.util.Base64;

import java.io.IOException;

import example.packt.com.authcodeapp.BuildConfig;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class BasicAuthenticationInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        Request authenticatedRequest = request.newBuilder()
            .addHeader("Authorization", getEncodedAuthorization())
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .method(request.method(), request.body())
            .build();

        return chain.proceed(authenticatedRequest);
    }

    private String getEncodedAuthorization() {
        String clientId = BuildConfig.CLIENT_ID;
        String clientSecret = BuildConfig.CLIENT_SECRET;

        String credentials = clientId + ":" + clientSecret;
        return "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
    }
}
