package com.example.smartbite;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RecipeActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE = "extra_recipe";
    Button homeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        TextView recipeTextView = findViewById(R.id.recipeTextView);
        homeBtn = findViewById(R.id.homeBtn);

        // Get recipe text from intent
        String recipe = getIntent().getStringExtra(EXTRA_RECIPE);
        if (recipe != null && !recipe.isEmpty()) {
            // Add simple line breaks for readability
            recipe = recipe.replaceAll("\\. ", ".\n");
            recipeTextView.setText(recipe);
        } else {
            recipeTextView.setText("No recipe available.");
        }

        // Navigate back to HomeActivity
        homeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(RecipeActivity.this, HomeActivity.class);
            startActivity(intent);
            finish(); // Optional: close current activity
        });
    }
}
