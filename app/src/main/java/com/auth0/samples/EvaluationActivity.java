package com.auth0.samples;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EvaluationActivity extends AppCompatActivity {
    private OkHttpClient client = new OkHttpClient();

    ArrayList<Question> questions;
    RadioGroup radioGroup;
    RadioButton btn0;
    RadioButton btn1;
    RadioButton btn2;
    RadioButton btn3;
    RadioButton btn4;
    Button next;
    Button previous;
    Button submit;
    int currentQuestion;
    TextView questionText;
    TextView posterTitle;
    TextView posterParticipants;

    HashMap<Integer, Integer> answers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluation);
        previous = findViewById(R.id.buttonPrevious);
        next = findViewById(R.id.buttonNext);
        submit = findViewById(R.id.buttonSubmit);
        submit.setVisibility(View.INVISIBLE);
        questionText = findViewById(R.id.textViewQuestion);
        radioGroup = findViewById(R.id.radioGroup);
        btn0 = findViewById(R.id.radioButton0);
        btn1 = findViewById(R.id.radioButton1);
        btn2 = findViewById(R.id.radioButton2);
        btn3 = findViewById(R.id.radioButton3);
        btn4 = findViewById(R.id.radioButton4);
        posterTitle = findViewById(R.id.textViewTitle);
        posterParticipants = findViewById(R.id.textViewParticipants);
        questions = new ArrayList<>();
        answers = new HashMap<>();
        setUpQuestions();
        currentQuestion = 0;
        questionText.setText(questions.get(currentQuestion).getQuestionText());

        if (getIntent() != null && getIntent().getExtras() != null && getIntent().hasExtra(MainActivity.POSTER_KEY)) {
            Poster poster = (Poster) getIntent().getSerializableExtra(MainActivity.POSTER_KEY);
            Log.d("demo", "onCreate: PosterId: " + poster.getId() + " Poster Title: " + poster.getTitle());
            posterTitle.setText("Title: " + poster.getTitle());
            posterParticipants.setText("Participants: " + poster.getParticipants());
        }

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (radioGroup.getCheckedRadioButtonId() == -1) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(EvaluationActivity.this, "You must answer before submitting", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Gson gson = new Gson();
                    String json = gson.toJson(answers);
                    //add formBody
                    RequestBody formBody = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));

                    if (getIntent() != null && getIntent().getExtras() != null && getIntent().hasExtra(MainActivity.POSTER_KEY)) {
                        Poster poster = (Poster) getIntent().getSerializableExtra(MainActivity.POSTER_KEY);
                        //add posterID
                        SharedPreferences sh = getSharedPreferences("MySharedPref", MODE_PRIVATE);
                        Request request = new Request.Builder()
                                .url(AuthApiHelper.PosterEndpoint + "/" + String.valueOf(poster.getId()))
                                .header("Authorization", "Bearer " + sh.getString(LoginActivity.EXTRA_ACCESS_TOKEN, ""))
                                .post(formBody)
                                .build();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(EvaluationActivity.this, "Unexpected error occurred.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                String body = response.body().string();
                                if (response.isSuccessful()) {
                                    Log.d("demo", "onResponse: body" + body);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(EvaluationActivity.this, "Unexpected error occurred.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    goBackToMain();
                                } else {
                                    try {
                                        JSONObject json = new JSONObject(body);
                                        String message = json.getString("error");

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(EvaluationActivity.this, message, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    }

                }

            }
        });

        //hide next button if no answer is chosen
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (currentQuestion != 6) {
                    if (checkedId == R.id.radioButton0) {
                        next.setVisibility(View.VISIBLE);
                    } else if (checkedId == R.id.radioButton1) {
                        next.setVisibility(View.VISIBLE);
                    } else if (checkedId == R.id.radioButton2) {
                        next.setVisibility(View.VISIBLE);
                    } else if (checkedId == R.id.radioButton3) {
                        next.setVisibility(View.VISIBLE);
                    } else if (checkedId == R.id.radioButton4) {
                        next.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (checkedId == R.id.radioButton0) {
                        submit.setVisibility(View.VISIBLE);
                    } else if (checkedId == R.id.radioButton1) {
                        submit.setVisibility(View.VISIBLE);
                    } else if (checkedId == R.id.radioButton2) {
                        submit.setVisibility(View.VISIBLE);
                    } else if (checkedId == R.id.radioButton3) {
                        submit.setVisibility(View.VISIBLE);
                    } else if (checkedId == R.id.radioButton4) {
                        submit.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        next.setVisibility(View.INVISIBLE);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check which button is checked for answer
                int checkedId = radioGroup.getCheckedRadioButtonId();
                if (currentQuestion < 6) {
                    if (checkedId == R.id.radioButton0) {
                        //record answer,goto next question, uncheck button, hide next button
                        goToNextQuestion(0);
                    } else if (checkedId == R.id.radioButton1) {
                        goToNextQuestion(1);
                    } else if (checkedId == R.id.radioButton2) {
                        goToNextQuestion(2);
                    } else if (checkedId == R.id.radioButton3) {
                        goToNextQuestion(3);
                    } else if (checkedId == R.id.radioButton4) {
                        goToNextQuestion(4);
                    }
                }
            }
        });
        previous.setVisibility(View.INVISIBLE);
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToPreviousQuestion();
            }
        });
    }

    private void goToNextQuestion(int checked) {
        if (answers.get(questions.get(currentQuestion).getId()) == null || answers.get(questions.get(currentQuestion).getId()) != checked) {
            if (currentQuestion < questions.size() - 1) {
                //record answer
                answers.put(questions.get(currentQuestion).getId(), checked);
                //index to next question
                currentQuestion++;
                if (currentQuestion == 0) {
                    previous.setVisibility(View.INVISIBLE);
                } else {
                    previous.setVisibility(View.VISIBLE);
                }
                //uncheckBtn
                radioGroup.clearCheck();
                //update question text
                updateTextView();
                //hide next button
                next.setVisibility(View.INVISIBLE);
            } else {
                //last question
                answers.put(questions.get(currentQuestion).getId(), checked);
                //submit hashmap
            }
        } else {
            if (currentQuestion == 0) {
                previous.setVisibility(View.INVISIBLE);
            } else {
                previous.setVisibility(View.VISIBLE);
            }
            currentQuestion++;
            resetToPreviousRadioButton();
            //update question text
            updateTextView();
            //hide next button
            //next.setVisibility(View.INVISIBLE);
        }

    }

    private void goToPreviousQuestion() {
        if (currentQuestion > 0) {
            //index to previous question
            currentQuestion--;
            //update question text
            updateTextView();
            //reset the radio button to previous answer
            resetToPreviousRadioButton();
        }
    }

    private void resetToPreviousRadioButton() {
        if (answers.get(questions.get(currentQuestion).getId()) != null) {
            if (answers.get(questions.get(currentQuestion).getId()) == 0) {
                btn0.setChecked(true);
            } else if (answers.get(questions.get(currentQuestion).getId()) == 1) {
                btn1.setChecked(true);
            } else if (answers.get(questions.get(currentQuestion).getId()) == 2) {
                btn2.setChecked(true);
            } else if (answers.get(questions.get(currentQuestion).getId()) == 3) {
                btn3.setChecked(true);
            } else if (answers.get(questions.get(currentQuestion).getId()) == 4) {
                btn4.setChecked(true);
            }
        }
    }

    private void updateTextView() {
        questionText.setText(questions.get(currentQuestion).getQuestionText());
    }

    private void setUpQuestions() {
        Question question1 = new Question("1. Poster content is of professional quality and indicates a mastery of the project subject matter.", 1);
        questions.add(question1);
        Question question2 = new Question("2. The presentation is organized, engaging, and includes a thorough description of the design and the implementation of the design.", 2);
        questions.add(question2);
        Question question3 = new Question("3. All the team members are suitably attired, are polite, demonstrate full knowledge of material, and can answer all relevant questions.", 3);
        questions.add(question3);
        Question question4 = new Question("4. The work product (model, prototype, documentation set or computer simulation) is of professional quality in all respects.", 4);
        questions.add(question4);
        Question question5 = new Question("5. The team implemented novel approaches and/or solutions in the development of the project.", 5);
        questions.add(question5);
        Question question6 = new Question("6. The project has the potential to enhance the reputation of the Innovative Computing Project and/or CCI/DSI", 6);
        questions.add(question6);
        Question question7 = new Question("7. The team successfully explained the scope and results of their project in no more than 5 minutes", 7);
        questions.add(question7);
    }

    private void goBackToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}