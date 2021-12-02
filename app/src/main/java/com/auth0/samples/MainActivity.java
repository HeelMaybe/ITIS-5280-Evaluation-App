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
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    Tag myTag;
    Context context;
    Button scanNfc;

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

        scanNfc = binding.scanNfc;
        context = this;
        scanNfc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((myTag == null)) {
                    Toast.makeText(context, ERROR_DETECTED, Toast.LENGTH_LONG).show();
                } else {
                    //connected to nfctag
                }
            }
        });
        nfcAdapter = NfcAdapter.getDefaultAdapter(context);
        if(nfcAdapter == null){
            Toast.makeText(context, "This device does no support NFC", Toast.LENGTH_LONG).show();
            scanNfc.setVisibility(View.GONE);
        }else{
            readFromIntent(getIntent());
            pendingIntent = PendingIntent.getActivity(context,0,new Intent(context,getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),0);
            IntentFilter tagDectected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
            tagDectected.addCategory(Intent.CATEGORY_DEFAULT);
        }
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
    }

    public void readFromIntent(Intent intent){
        String action = intent.getAction();
        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs = null;
            if(rawMsgs != null){
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++){
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            buildTagViews(msgs);
        }
    }

    public void  buildTagViews(NdefMessage[] msgs){
        if (msgs == null || msgs.length == 0)return;

        String text = "";
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
        int languageCodeLength = payload[0] & 0063;

        try{
            text = new String(payload,languageCodeLength+1,payload.length - languageCodeLength -1,textEncoding);
        }catch (UnsupportedEncodingException e){
            Log.d("demo", "UnsupportedEncodingException: " + e.getMessage() );
        }
        Log.d("demo", "NFC text: "+ text);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        readFromIntent(intent);
        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        }

    }

    private void logout() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(LoginActivity.EXTRA_CLEAR_CREDENTIALS, true);
        startActivity(intent);
        finish();
    }

    private void getAllPosters() {
        binding.recyclerViewPosters.setHasFixedSize(true);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(binding.recyclerViewPosters.getContext(),DividerItemDecoration.VERTICAL);
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
        Request request = new Request.Builder()
                .url(AuthApiHelper.PostersEndpoint)
                .header("Authorization", "Bearer " + sh.getString(LoginActivity.EXTRA_ACCESS_TOKEN, ""))
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
