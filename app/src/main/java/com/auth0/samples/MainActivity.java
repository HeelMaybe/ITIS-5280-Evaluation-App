package com.auth0.samples;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.auth0.samples.databinding.MainActivityBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by lbalmaceda on 5/10/17.
 */

public class MainActivity extends Activity {
    private OkHttpClient client = new OkHttpClient();
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<Poster> availablePosters = new ArrayList<>();
    MainActivityBinding binding;
    public static final String POSTER_KEY = "POSTER_KEY";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = MainActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Button logoutButton = binding.logout;
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sh = getSharedPreferences("MySharedPref", MODE_PRIVATE);
                sh.edit().clear().commit();
                logout();
            }
        });
        getAllPosters();

        //Obtain the token from the Intent's extras
        String accessToken = getIntent().getStringExtra(LoginActivity.EXTRA_ACCESS_TOKEN);
        TextView textView = binding.credentials;
        textView.setText(accessToken);
    }

    private void logout() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(LoginActivity.EXTRA_CLEAR_CREDENTIALS, true);
        startActivity(intent);
        finish();
    }

    private void getAllPosters() {
        binding.recyclerViewPosters.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(MainActivity.this);
        binding.recyclerViewPosters.setLayoutManager(layoutManager);
        mAdapter = new PosterAdapter(availablePosters, new PosterAdapter.IAdapter() {
            @Override
            public void goToEvaluation(Poster poster) {
                evaluate(poster);
            }
        });
        binding.recyclerViewPosters.setAdapter(mAdapter);
        SharedPreferences sh = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        Request request = new Request.Builder()
                .url(AuthApiHelper.PostersEndpoint)
                .header("Authorization", "Bearer " + sh.getString(LoginActivity.EXTRA_ACCESS_TOKEN, ""))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    Log.d("demo", "onResponse: " + body);
                    try {
                        JSONArray postersArray = new JSONArray(body);
                        availablePosters.clear();
                        for (int i = 0; i < postersArray.length(); i++) {
                            JSONObject productJsonObject = postersArray.getJSONObject(i);
                            Poster poster = new Poster();
                            poster.setTitle(productJsonObject.getString("title"));
                            poster.setId(productJsonObject.getString("id"));
                            poster.setParticipants(productJsonObject.getString("participants"));
                            poster.setNFC(productJsonObject.getString("NFC"));
                            availablePosters.add(poster);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.notifyDataSetChanged();
                            }
                        });
                    } catch (JSONException e) {
                        Log.d("demo", "onResponse:  JSONException" + e.getMessage());
                    }
                } else {
                    Log.d("demo", "onResponse: Failed to get all posters");
                }
            }
        });

    }

    private void evaluate(Poster poster) {
        Intent intent = new Intent(this, EvaluationActivity.class);
        intent.putExtra(LoginActivity.EXTRA_CLEAR_CREDENTIALS, true);
        intent.putExtra(POSTER_KEY, poster);
        startActivity(intent);
    }
}
