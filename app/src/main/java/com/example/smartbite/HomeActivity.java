package com.example.smartbite;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

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

    private final String apiKey = "AIzaSyD_QYI-zaQshT_B-TWvdY4CYgaUkfwXZjY";
    private final String model = "gemini-2.5-flash-preview-09-2025";
    private final String apiUrlTemplate =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // UI Components - Changed from FlexboxLayout to ChipGroup
    private ChipGroup ingredientsContainer;
    private EditText questionEditText;
    private Button addIngredientBtn, getRecipeBtn;
    private BottomNavigationView bottomNavigationView;

    // Data
    private List<String> ingredientList = new ArrayList<>();
    private String userConditions, userDiet, userAllergies, userGoals;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 1. Initialize Views
        ingredientsContainer = findViewById(R.id.ingredientsContainer);
        questionEditText = findViewById(R.id.questionEditText);
        addIngredientBtn = findViewById(R.id.addIngredientBtn);
        getRecipeBtn = findViewById(R.id.getRecipeBtn);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // 2. Load User Preferences
        prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        loadUserPreferences();

        // 3. Setup Bottom Navigation
        setupBottomNavigation();

        // 4. Add Ingredient Logic
        addIngredientBtn.setOnClickListener(v -> {
            String ing = questionEditText.getText().toString().trim();
            if (!ing.isEmpty() && !ingredientList.contains(ing)) {
                ingredientList.add(ing);
                addIngredientChip(ing); // Function renamed
                questionEditText.setText("");
            }
        });

        // 5. Get Recipe Logic
        getRecipeBtn.setOnClickListener(v -> {
            if (ingredientList.isEmpty()) {
                Toast.makeText(HomeActivity.this, "Please add at least one ingredient.", Toast.LENGTH_SHORT).show();
                return;
            }
            String prompt = buildPrompt();

            getRecipeBtn.setEnabled(false);
            getRecipeBtn.setText("Generating...");

            callGenerateContent(prompt);
        });
    }

    // ... (loadUserPreferences and setupBottomNavigation remain unchanged) ...
    private void loadUserPreferences() {
        userConditions = getIntent().getStringExtra("conditions");
        if (userConditions == null) userConditions = prefs.getString("conditions", "None");

        userDiet = getIntent().getStringExtra("diet");
        if (userDiet == null) userDiet = prefs.getString("diet", "None");

        userAllergies = getIntent().getStringExtra("allergies");
        if (userAllergies == null) userAllergies = prefs.getString("allergies", "None");

        userGoals = getIntent().getStringExtra("goals");
        if (userGoals == null) userGoals = prefs.getString("goals", "None");
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.item_1);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.item_1) {
                return true;
            } else if (id == R.id.item_2) {
                Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    // --- MODIFIED FUNCTION: Creates a Chip instead of a Button ---
    private void addIngredientChip(String ingredient) {
        // Inflate the chip from the XML layout to keep the 'Entry' style
        Chip chip = (Chip) LayoutInflater.from(this)
                .inflate(R.layout.item_chip_entry, ingredientsContainer, false);

        chip.setText(ingredient);

        // Logic to remove chip when the 'X' icon is clicked
        chip.setOnCloseIconClickListener(v -> {
            ingredientList.remove(ingredient);
            ingredientsContainer.removeView(chip);
        });

        ingredientsContainer.addView(chip);
    }

    // ... (buildPrompt and callGenerateContent remain unchanged) ...
    private String buildPrompt() {
        return "You are a helpful recipe assistant.\n" +
                "Generate a recipe based on the following:\n" +
                "Ingredients: " + String.join(", ", ingredientList) + "\n" +
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
            boolean isSuccess = false; // Add a success flag
            HttpURLConnection conn = null;
            try {
                // 1. Setup Connection
                String apiUrl = String.format(apiUrlTemplate, model, apiKey);
                URL url = new URL(apiUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                // 2. Build JSON Payload
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

                // 3. Send Request
                OutputStream os = conn.getOutputStream();
                BufferedWriter w = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                w.write(payload.toString());
                w.flush();
                w.close();
                os.close();

                // 4. Handle Response
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
                    isSuccess = true; // Mark as successful
                } else {
                    // Read the error message from the Error Stream to see WHY it failed
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    StringBuilder errorSb = new StringBuilder();
                    String line;
                    while ((line = errorReader.readLine()) != null) errorSb.append(line);
                    errorReader.close();
                    result = "Error " + code + ": " + errorSb.toString();
                    isSuccess = false;
                }
            } catch (Exception e) {
                result = "Exception: " + e.getMessage();
                isSuccess = false;
            } finally {
                if (conn != null) conn.disconnect();
            }

            // 5. Update UI on Main Thread
            String finalRes = result;
            boolean finalSuccess = isSuccess;

            mainHandler.post(() -> {
                // Re-enable the button
                getRecipeBtn.setEnabled(true);
                getRecipeBtn.setText("Get Recipe");

                if (finalSuccess) {
                    // ONLY open the new activity if it worked
                    Intent intent = new Intent(HomeActivity.this, RecipeActivity.class);
                    intent.putExtra(RecipeActivity.EXTRA_RECIPE, finalRes);
                    startActivity(intent);
                } else {
                    // Show the error in a Toast so you can debug it
                    Toast.makeText(HomeActivity.this, finalRes, Toast.LENGTH_LONG).show();
                    System.out.println("GEMINI_ERROR: " + finalRes); // Log to Logcat
                }
            });
        });
    }
}