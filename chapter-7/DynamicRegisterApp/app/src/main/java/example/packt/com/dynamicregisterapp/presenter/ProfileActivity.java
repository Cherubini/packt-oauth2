package example.packt.com.dynamicregisterapp.presenter;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import example.packt.com.dynamicregisterapp.client.oauth2.registration.ClientCredentials;
import example.packt.com.dynamicregisterapp.client.oauth2.registration.ClientCredentialsRepository;
import example.packt.com.dynamicregisterapp.client.oauth2.registration.OnClientRegistrationResult;
import retrofit2.Call;

import example.packt.com.dynamicregisterapp.R;
import example.packt.com.dynamicregisterapp.client.ClientAPI;
import example.packt.com.dynamicregisterapp.client.oauth2.AccessToken;
import example.packt.com.dynamicregisterapp.client.oauth2.AccessTokenRequestData;
import example.packt.com.dynamicregisterapp.client.oauth2.OAuth2StateManager;
import example.packt.com.dynamicregisterapp.client.oauth2.TokenResponseCallback;
import example.packt.com.dynamicregisterapp.client.profile.ProfileAuthorizationListener;
import example.packt.com.dynamicregisterapp.client.profile.UserProfile;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private TextView textName;
    private TextView textEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        String accessToken = (String) getIntent().getExtras().get("access_token");

        textName = (TextView) findViewById(R.id.profile_name);
        textEmail = (TextView) findViewById(R.id.profile_email);

        Call<UserProfile> call = ClientAPI.userProfile().token(accessToken);
        call.enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                UserProfile profile = response.body();
                textName.setText(profile.getName());
                textEmail.setText(profile.getEmail());
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                Log.e("ProfileActivity", "error retrieving user profile", t);
            }
        });

    }


}