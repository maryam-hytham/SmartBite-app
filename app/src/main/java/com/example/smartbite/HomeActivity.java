package com.example.smartbite;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.flexbox.FlexboxLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeActivity extends AppCompatActivity {

    private final String apiKey = "AIzaSyBQwYQmPXNsh_hnY0FRyxPk79c_gxaEKs8";
    private final String model = "gemini-2.5-flash-preview-09-2025";
    private final String apiUrlTemplate =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private FlexboxLayout ingredientsContainer;
    private EditText questionEditText;
    private Button addIngredientBtn, getRecipeBtn;
    private TextView answerTextView;
    private List<String> ingredientList = new ArrayList<>();
    private String userConditions;
    private String userDiet;
    private String userAllergies;
    private String userGoals;
    private SharedPreferences prefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ingredientsContainer = findViewById(R.id.ingredientsContainer);
        questionEditText = findViewById(R.id.questionEditText);
        addIngredientBtn = findViewById(R.id.addIngredientBtn);
        getRecipeBtn = findViewById(R.id.getRecipeBtn);


        prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);

        // Load preferences: Use Intent extras if available, otherwise fall back to SharedPreferences
        userConditions = getIntent().getStringExtra("conditions");
        if (userConditions == null || userConditions.isEmpty()) {
            userConditions = prefs.getString("conditions", "None specified");
        }

        userDiet = getIntent().getStringExtra("diet");
        if (userDiet == null || userDiet.isEmpty()) {
            userDiet = prefs.getString("diet", "None specified");
        }

        userAllergies = getIntent().getStringExtra("allergies");
        if (userAllergies == null || userAllergies.isEmpty()) {
            userAllergies = prefs.getString("allergies", "None specified");
        }

        userGoals = getIntent().getStringExtra("goals");
        if (userGoals == null || userGoals.isEmpty()) {
            userGoals = prefs.getString("goals", "None specified");
        }

        addIngredientBtn.setOnClickListener(v -> {
            String ing = questionEditText.getText().toString().trim();
            if (!ing.isEmpty() && !ingredientList.contains(ing)) {
                ingredientList.add(ing);
                addIngredientButton(ing);
                questionEditText.setText("");
            }
        });

        getRecipeBtn.setOnClickListener(v -> {
            if (ingredientList.isEmpty()) {
                answerTextView.setText("Please add at least one ingredient.");
                return;
            }
            String prompt = buildPrompt();
            callGenerateContent(prompt);
            getRecipeBtn.setEnabled(false);
        });
    }

    private void addIngredientButton(String ingredient) {
        Button btn = new Button(this);
        btn.setText(ingredient + " ✕");
        btn.setOnClickListener(v -> {
            ingredientList.remove(ingredient);
            ingredientsContainer.removeView(btn);
        });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT

        );
        params.setMargins(8, 8, 8, 8);
        btn.setLayoutParams(params);
        ingredientsContainer.addView(btn);
    }

    private String buildPrompt() {
        return "You are a helpful recipe assistant.\n" +
                "Generate a recipe based on the following:\n" +
                "Ingredients: (its ok if not using all)" + String.join(", ", ingredientList) + "\n" +
                "User conditions: " + userConditions + "\n" +
                "Diet: " + userDiet + "\n" +
                "Allergies: " + userAllergies + "\n" +
                "Goals: " + userGoals + "\n\n" +
                "The response should start with: 'Here is a recipe that matches your needs:'\n" +
                "Then, write clearly:\n" +
                "Recipe name\n" +
                "How to make it (numbered step by step, short and clear instructions)\n" +
                "Do not use bold and explain shortly how it fits the user in the end.\n" +
                "Keep it concise and easy to follow.";
    }

    private void callGenerateContent(String prompt) {
        executor.execute(() -> {
            String result = "";
            HttpURLConnection conn = null;
            try {
                String apiUrl = String.format(apiUrlTemplate, model, apiKey);
                URL url = new URL(apiUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                JSONObject part = new JSONObject();
                part.put("text", prompt);

                JSONArray parts = new JSONArray();
                parts.put(part);

                JSONObject content = new JSONObject();
                content.put("parts", parts);

                JSONArray contents = new JSONArray();
                contents.put(content);

                JSONObject payload = new JSONObject();
                payload.put("contents", contents);

                OutputStream os = conn.getOutputStream();
                BufferedWriter w = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                w.write(payload.toString());
                w.flush();
                w.close();
                os.close();

                int code = conn.getResponseCode();
                if (code == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) sb.append(line);
                    in.close();

                    JSONObject json = new JSONObject(sb.toString());
                    JSONArray candidates = json.getJSONArray("candidates");
                    JSONObject first = candidates.getJSONObject(0);
                    JSONObject cont = first.getJSONObject("content");
                    JSONArray parts2 = cont.getJSONArray("parts");
                    result = parts2.getJSONObject(0).getString("text");

                } else {
                    result = "HTTP Error: " + code;
                }
            } catch (Exception e) {
                result = "Error: " + e.getMessage();
            } finally {
                if (conn != null) conn.disconnect();
            }

            String finalRes = result;
            mainHandler.post(() -> {
                getRecipeBtn.setEnabled(true);

                // Instead of setting TextView directly, open RecipeActivity
                Intent intent = new Intent(HomeActivity.this, RecipeActivity.class);
                intent.putExtra(RecipeActivity.EXTRA_RECIPE, finalRes);
                startActivity(intent);
            });

        });
    }
}