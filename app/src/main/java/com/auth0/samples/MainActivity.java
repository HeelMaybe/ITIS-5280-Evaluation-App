package com.auth0.samples;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.auth0.samples.databinding.MainActivityBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends Activity {

    public static final String ERROR_DETECTED = "No NFC Tag Detected";

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

        binding.logout.setOnClickListener(new View.OnClickListener() {
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
    }

    private void logout() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(LoginActivity.EXTRA_CLEAR_CREDENTIALS, true);
        startActivity(intent);
        finish();
    }

    private void getAllPosters() {
        binding.recyclerViewPosters.setHasFixedSize(true);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(binding.recyclerViewPosters.getContext(), DividerItemDecoration.VERTICAL);
        binding.recyclerViewPosters.addItemDecoration(dividerItemDecoration);
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
        //Log.d("demo", "token: "+ "appSession=" + sh.getString(LoginActivity.EXTRA_JWT_TOKEN, ""));
        Request request = new Request.Builder()
                .url(AuthApiHelper.PostersEndpoint)
                .header("Authorization", "Bearer " + sh.getString(LoginActivity.EXTRA_ACCESS_TOKEN, ""))
                //.header("Cookie", "appSession=" + sh.getString(LoginActivity.EXTRA_JWT_TOKEN, ""))
                .header("Cookie", "appSession=eyJhbGciOiJkaXIiLCJlbmMiOiJBMjU2R0NNIiwiaWF0IjoxNjM5MDEwOTA2LCJ1YXQiOjE2MzkwMTEyMzgsImV4cCI6MTYzOTA5NzYzOH0..MIiOSJfMywsvOCmO.rWwo9vOoTD_5f6W-cwB4I8qnGMSfcq9YFif7EHO_E5mrETUz7UPJb7pComhs92HQDYoBi8_RhC_zbA2vQcJyss-uaawhzBkswb_sKqZKBw_dIalHj4RJWubpY-OgbsEFeGhoLUroFxsfQkapU_lMaLoRIJ2phg3SRY8DLF1SPF77_0wHxlEbENyEOO6YmURZrLfL-YHg3VlZcmwiIFstPfc-8ZdYWKbBwvlCC24gXLzUOypFRRIjGrYzMAMWIy930rR2STkxqU2YuieKgzDtvlO0PavE0ub0AtoGFKEyKidibEIwjX3Yu8sdlG4upT53aacw2q7-DRs7Gsahg3kv33rhgFz12duzls1RAf_3zynDArGLX0gAIAhX0-XnA2eMfOh0er1pH9bPeCpM8kHyHHXbhVyIzP1n5dSSSdnaHXIefqusL_RhKY-SN0wHNTFuOSZZ9Lm-cnDXCEcO1BvWs_chiKHVm2JmsIlhn7N_dbvLVLEpwhFuprpiKnijQUUPRIJeInyvrBVE0k3FAUcJwdSD_Y_fPyDcxY2MtAnkADjP-6_UEouXUak95kxxo9_v69y7B7scfbbkq5lsiIWl6Eml_fErfzZf5uz5W1gLUBglpW1TbVxaPyyMo4lHxD0_FLa0x-BxtfLVVdkvFEr5A4-m7VK2IOOajmcu_v-d3IXPNWsx5kupMOejk58nXabBnZ06r4TLRVaPLUbUA2HwjBkxtEFI_j3_xFm_sIv72xGFI_tNjZZc5YWqLxNFvyyKHWLmL7mekwWfNDVZxvmekM44SXFMinV32hi8DHX9QEOoTkbGV9hVDOJUrgWUR3zvn3dAWA2G5yqkT8GIW5AaDbisV2u5EXEbQKwf9gSO_8kpMHEQpkdNkIgJ6v8-ADFeY-GNzU9_rsNfexEcGPxCQLCEolC-Zwo5cHkPAMrvOB00CbQdSPsb7fe14ZLlxBB9jKamTdjgzpDa1GveZN2rvTxVB_v4sL1QocCtCr9DnqbsTAy0s631YOT7UyomjLEntaL4RYLqlpsbnkvBsVZMljuqFTgK3ZGcAF1j7HP64NuHhFwBCwb7wtL0OP7qkQyt5MHYQJsJk8as89nSAXTNHGj8UT_SSqOYL7D27GT96NXvqmZkZvFBL_20jl4ptVcZNVo_sPgtWbV8ywzikVnHew1Ue5SNVKz3-sOzyqnnaN6jdyQHcEpDJf1fBNWGTLeoxKDJjtPFASWeBXa2KW7hDHGWstQvJtMycse_qPWwCKz6_FwvA-gV9RH2V8ok4pPcDhbsCL5rlu1cvxY6jXm0FowcdfBJrtIjyIwoyD99jyPfEuPowqLYopXvadQBhKpRARDTghyUJt_11ue92i0yJL4TuZnaor-NSVkWCK6w--Tb7a99etMlGkaqGB42_-_FOCxAO2jCjIpKgg7ig89hDhEefKkAG0fU6q0TiwW-tgzHy_Ji5ZDTaqAFbErS_0er2xGjsC3Bb2mWxZXVIW1tjkNbJ_4ZdHX_3NKXfR36_1bg_H9wSa-5bDicy3G1ZBQK9YenROC1GS2e8b8yiZRG6RIa.wpH4MKTfkjy-YND_zNFEXQ")

                .header("userid", sh.getString(LoginActivity.EXTRA_USER_ID, ""))
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
                    String body = response.body().string();
                    Log.d("demo", "onResponse: Failed to get all posters: \n" + body);
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
