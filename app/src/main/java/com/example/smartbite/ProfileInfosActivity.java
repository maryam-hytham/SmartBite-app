package com.example.smartbite;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileInfosActivity extends AppCompatActivity {

    // UI Components
    private ChipGroup chipGroupHealth, chipGroupAllergies, chipGroupDiet, chipGroupGoals;
    private Button saveContinueBtn;

    private SharedPreferences prefs;

    // Firebase Components
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_infos);

        // Adjust UI for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize SharedPreferences (Optional: kept for local caching if needed)
        prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);

        // Bind Views using the IDs from your XML
        chipGroupHealth = findViewById(R.id.chipGroupHealth);
        chipGroupAllergies = findViewById(R.id.chipGroupAllergies);
        chipGroupDiet = findViewById(R.id.chipGroupDiet);
        chipGroupGoals = findViewById(R.id.chipGroupGoals);
        saveContinueBtn = findViewById(R.id.saveContinueBtn);

        // Set Click Listener for Save Button
        saveContinueBtn.setOnClickListener(v -> saveDataAndContinue());
    }

    private void saveDataAndContinue() {
        // 1. Get the lists of selected chips from each group
        List<String> healthConditions = getSelectedChips(chipGroupHealth);
        List<String> allergies = getSelectedChips(chipGroupAllergies);
        List<String> diet = getSelectedChips(chipGroupDiet);
        List<String> goals = getSelectedChips(chipGroupGoals);

        // 2. Check if user is logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            // Optional: Redirect to LoginActivity
            return;
        }

        // 3. Prepare data for Firestore
        // Note: We save the List<String> directly. Firestore handles arrays natively.
        Map<String, Object> userPreferences = new HashMap<>();
        userPreferences.put("healthConditions", healthConditions);
        userPreferences.put("allergies", allergies);
        userPreferences.put("dietaryPreferences", diet);
        userPreferences.put("goals", goals);

        // Optional: Add a flag that profile setup is complete
        userPreferences.put("isProfileSetup", true);

        // Disable button to prevent double-click while saving
        saveContinueBtn.setEnabled(false);
        saveContinueBtn.setText("Saving...");

        // 4. Update Firestore Document
        // We use SetOptions.merge() so we don't overwrite the existing Name/Email
        db.collection("users").document(currentUser.getUid())
                .set(userPreferences, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {

                    // --- SUCCESS ---

                    // Also save to SharedPreferences (for fast local access without internet)
                    saveToSharedPreferences(healthConditions, allergies, diet, goals);

                    Toast.makeText(ProfileInfosActivity.this, "Preferences Saved!", Toast.LENGTH_SHORT).show();

                    // Navigate to HomeActivity
                    Intent intent = new Intent(ProfileInfosActivity.this, HomeActivity.class);
                    // We can pass data, or let HomeActivity fetch it from Firebase
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // --- FAILURE ---
                    saveContinueBtn.setEnabled(true);
                    saveContinueBtn.setText("Save & Continue");
                    Toast.makeText(ProfileInfosActivity.this, "Error saving data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // Helper to save string versions to SharedPreferences
    private void saveToSharedPreferences(List<String> health, List<String> allergies, List<String> diet, List<String> goals) {
        String conditionsStr = String.join(", ", health);
        String dietStr = String.join(", ", diet);
        String allergiesStr = String.join(", ", allergies);
        String goalsStr = String.join(", ", goals);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("conditions", conditionsStr);
        editor.putString("diet", dietStr);
        editor.putString("allergies", allergiesStr);
        editor.putString("goals", goalsStr);
        editor.apply();
    }

    /**
     * Helper method to iterate through a ChipGroup and return a list of texts
     * from the Chips that are currently checked.
     */
    private List<String> getSelectedChips(ChipGroup chipGroup) {
        List<String> selectedValues = new ArrayList<>();

        // getCheckedChipIds returns a list of the integer IDs of checked chips
        List<Integer> checkedChipIds = chipGroup.getCheckedChipIds();

        for (Integer id : checkedChipIds) {
            Chip chip = chipGroup.findViewById(id);
            if (chip != null) {
                // We assume the chip text matches the value we want to save
                selectedValues.add(chip.getText().toString());
            }
        }

        return selectedValues;
    }
}