package com.auth0.samples;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationAPIClient;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.callback.Callback;
import com.auth0.android.provider.WebAuthProvider;
import com.auth0.android.result.Credentials;
import com.auth0.android.result.UserProfile;

public class LoginActivity extends AppCompatActivity {

    private Auth0 auth0;

    public static final String EXTRA_CLEAR_CREDENTIALS = "com.auth0.CLEAR_CREDENTIALS";
    public static final String EXTRA_ACCESS_TOKEN = "com.auth0.ACCESS_TOKEN";
    public static final String EXTRA_USER_ID = "EXTRA_USER_ID";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        auth0 = new Auth0(this);
        //Check if the activity was launched to log the user out
        if (getIntent().getBooleanExtra(EXTRA_CLEAR_CREDENTIALS, false)) {
            logout();
        }
        SharedPreferences sh = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String s1 = sh.getString(LoginActivity.EXTRA_ACCESS_TOKEN, "");
        if (!s1.equals("")) {
            showNextActivity();
        }
    }

    private void showUserProfile(String accessToken) {
        AuthenticationAPIClient client = new AuthenticationAPIClient(auth0);

        // With the access token, call `userInfo` and get the profile from Auth0.
        client.userInfo(accessToken)
                .start(new Callback<UserProfile, AuthenticationException>() {
                    @Override
                    public void onSuccess(UserProfile userProfile) {
                        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(EXTRA_USER_ID, userProfile.getId());
                        editor.commit();


                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra(EXTRA_ACCESS_TOKEN, accessToken);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailure(@NonNull AuthenticationException e) {

                    }
                });

    }

    private void login() {
        WebAuthProvider.login(auth0)
                .withScheme("demo")
                .withAudience(String.format("https://%s/userinfo", getString(R.string.com_auth0_domain)))
                .start(this, new Callback<Credentials, AuthenticationException>() {
                    @Override
                    public void onFailure(@NonNull final AuthenticationException exception) {
                        Toast.makeText(LoginActivity.this, "Error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onSuccess(@Nullable final Credentials credentials) {
                        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(EXTRA_ACCESS_TOKEN, credentials.getAccessToken());
                        editor.commit();
                        Log.d("demo", credentials.getAccessToken());

                        showUserProfile(credentials.getAccessToken());
                    }
                });
    }

    private void logout() {
        WebAuthProvider.logout(auth0)
                .withScheme("demo")
                .start(this, new Callback<Void, AuthenticationException>() {
                    @Override
                    public void onSuccess(@Nullable Void payload) {
                        // The user has been logged out!
                        Log.d("demo", "onSuccess: logged Out");
                    }

                    @Override
                    public void onFailure(@NonNull AuthenticationException error) {
                        //Log out canceled, keep the user logged in
                        showNextActivity();
                        Log.d("demo", "onFailure: not logged out");
                    }
                });
    }

    private void showNextActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
