package com.example.smartbite;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class ProfileInfosActivity extends AppCompatActivity {

    // Lists to store the user's selections
    private final List<String> selectedHealthConditions = new ArrayList<>();
    private final List<String> selectedDiet = new ArrayList<>();
    private final List<String> selectedAllergies = new ArrayList<>();
    private final List<String> selectedGoals = new ArrayList<>();

    Button saveContinueBtn;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_infos);

        // Adjust UI for system bars (status/navigation)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);

        saveContinueBtn = findViewById(R.id.saveContinueBtn);

        // Attach toggle logic for all button groups
        setupToggleButtons();

        // Go to HomeActivity with all selected preferences
        saveContinueBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileInfosActivity.this, HomeActivity.class);

            // Convert lists → comma separated text
            String conditionsStr = String.join(", ", selectedHealthConditions);
            String dietStr = String.join(", ", selectedDiet);
            String allergiesStr = String.join(", ", selectedAllergies);
            String goalsStr = String.join(", ", selectedGoals);

            intent.putExtra("conditions", conditionsStr);
            intent.putExtra("diet", dietStr);
            intent.putExtra("allergies", allergiesStr);
            intent.putExtra("goals", goalsStr);

            // Save to SharedPreferences for persistence
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("conditions", conditionsStr);
            editor.putString("diet", dietStr);
            editor.putString("allergies", allergiesStr);
            editor.putString("goals", goalsStr);
            editor.apply();

            startActivity(intent);
        });
    }

    /**
     * Helper method to turn any button into a toggle button.
     * When clicked → highlight it and add the value to the list.
     * When clicked again → remove from list.
     */
    private void enableToggle(Button btn, String value, List<String> list) {
        btn.setOnClickListener(v -> {
            if (list.contains(value)) {
                // Unselect
                list.remove(value);
                btn.setBackgroundColor(getColor(android.R.color.holo_purple));
            } else {
                // Select
                list.add(value);
                btn.setBackgroundColor(getColor(android.R.color.holo_green_dark));
            }
        });
    }

    /**
     * Attach toggle behavior to all buttons.
     * All IDs are taken from your XML exactly.
     */
    private void setupToggleButtons() {

        // -------------------------------
        // HEALTH CONDITIONS
        // -------------------------------
        enableToggle(findViewById(R.id.button3), "Diabetes", selectedHealthConditions);
        enableToggle(findViewById(R.id.button4), "Heart Disease", selectedHealthConditions);
        enableToggle(findViewById(R.id.button6), "High Cholesterol", selectedHealthConditions);
        enableToggle(findViewById(R.id.button5), "Thyroid Issues", selectedHealthConditions);
        enableToggle(findViewById(R.id.button7), "High Blood Pressure", selectedHealthConditions);

        // -------------------------------
        // GOALS
        // -------------------------------
        enableToggle(findViewById(R.id.button30), "Build Muscle", selectedGoals);
        enableToggle(findViewById(R.id.button29), "Increase Fiber", selectedGoals);
        enableToggle(findViewById(R.id.button28), "Increase Protein", selectedGoals);
        enableToggle(findViewById(R.id.button14), "Lower Cholesterol", selectedGoals);
        enableToggle(findViewById(R.id.button26), "Manage Diabetes", selectedGoals);

        // -------------------------------
        // ALLERGIES
        // -------------------------------
        enableToggle(findViewById(R.id.button15), "Dairy", selectedAllergies);
        enableToggle(findViewById(R.id.button16), "Eggs", selectedAllergies);
        enableToggle(findViewById(R.id.button25), "Nuts", selectedAllergies);
        enableToggle(findViewById(R.id.button24), "Shellfish", selectedAllergies);
        enableToggle(findViewById(R.id.button19), "Soy", selectedAllergies);
        enableToggle(findViewById(R.id.button31), "Wheat", selectedAllergies);

        // -------------------------------
        // DIETARY RESTRICTIONS
        // -------------------------------
        enableToggle(findViewById(R.id.button8), "Dairy-Free", selectedDiet);
        enableToggle(findViewById(R.id.button9), "Gluten-Free", selectedDiet);
        enableToggle(findViewById(R.id.button10), "Keto", selectedDiet);
        enableToggle(findViewById(R.id.button11), "Low-Carb", selectedDiet);
        enableToggle(findViewById(R.id.button12), "Vegan", selectedDiet);
        enableToggle(findViewById(R.id.button13), "Vegetarian", selectedDiet);
    }
}