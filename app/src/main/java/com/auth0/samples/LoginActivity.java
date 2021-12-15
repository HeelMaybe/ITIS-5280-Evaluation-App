package com.auth0.samples;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationAPIClient;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.callback.Callback;
import com.auth0.android.provider.WebAuthProvider;
import com.auth0.android.result.Credentials;
import com.auth0.android.result.UserProfile;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.io.IOException;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements ImageAnalysis.Analyzer {
    private Auth0 auth0;
    Uri imageUri;

    public static final String EXTRA_CLEAR_CREDENTIALS = "com.auth0.CLEAR_CREDENTIALS";
    public static final String EXTRA_ACCESS_TOKEN = "com.auth0.ACCESS_TOKEN";
    public static final String EXTRA_JWT_TOKEN = "EXTRA_JWT_TOKEN";
    public static final String EXTRA_USER_ID = "EXTRA_USER_ID";
    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 10;
    private static final int PICK_IMAGE = 100;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button loginButtonQRCode = findViewById(R.id.loginButtonQRCode);
        loginButtonQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(gallery, PICK_IMAGE);
                BarcodeScannerOptions options =
                        new BarcodeScannerOptions.Builder()
                                .setBarcodeFormats(
                                        Barcode.FORMAT_QR_CODE,
                                        Barcode.FORMAT_AZTEC)
                                .build();
            }
        });


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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && data != null && resultCode == RESULT_OK) {
            imageUri = data.getData();
            InputImage image;
            try {
                image = InputImage.fromFilePath(this, imageUri);
                BarcodeScanner scanner = BarcodeScanning.getClient();
                Task<List<Barcode>> result = scanner.process(image)
                        .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                            @Override
                            public void onSuccess(List<Barcode> barcodes) {
                                for (Barcode barcode: barcodes) {
                                    Rect bounds = barcode.getBoundingBox();

                                    Point[] corners = barcode.getCornerPoints();

                                    String rawValue = barcode.getRawValue();
                                    Log.d("demo", "onSuccess: read QRCode: " + rawValue);
                                    SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString(EXTRA_ACCESS_TOKEN, rawValue);
                                    //editor.putString(EXTRA_JWT_TOKEN, rawValue);
                                    showNextActivity();

                                    int valueType = barcode.getValueType();
                                    // See API reference for complete list of supported types
                                    switch (valueType) {
                                        case Barcode.TYPE_TEXT:
                                            String rawValue1 = barcode.getRawValue();
                                            Log.d("demo", "onSuccess: read QRCode: " + rawValue1);

                                            break;
                                        case Barcode.TYPE_URL:
                                            String title = barcode.getUrl().getTitle();
                                            String url = barcode.getUrl().getUrl();
                                            break;

                                    }
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                e.printStackTrace();
                                // Task failed with an exception
                                // ...
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }

            //scan uri
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
                        editor.putString(EXTRA_JWT_TOKEN, credentials.getIdToken());

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

    @Override
    public void analyze(ImageProxy imageProxy) {
        @SuppressLint("UnsafeOptInUsageError") Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            // Pass image to an ML Kit Vision API
            // ...

        }
    }
}
