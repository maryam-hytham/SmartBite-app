package com.example.smartbite;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private Button continueBtn, backBtn;
    private TextView emailError, passError;
    private EditText editEmailAddress, editPassword, editName;
    private Drawable eyeOpen, eyeClosed, lockIcon;
    private boolean isPasswordVisible = false;
    private FirebaseAuth auth;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();

        continueBtn = findViewById(R.id.continueBtn);
        backBtn = findViewById(R.id.backBtn);
        emailError = findViewById(R.id.emailError);
        passError = findViewById(R.id.passError);
        editEmailAddress = findViewById(R.id.editEmailAddress);
        editPassword = findViewById(R.id.editPassword);
        editName = findViewById(R.id.editName);

        // Load icons
        eyeOpen = ContextCompat.getDrawable(this, R.drawable.eye);
        eyeClosed = ContextCompat.getDrawable(this, R.drawable.hidden);
        lockIcon = ContextCompat.getDrawable(this, R.drawable.baseline_lock_24);

        int size = (int) (editPassword.getLineHeight() * 0.8);
        if (eyeOpen != null) eyeOpen.setBounds(0, 0, size, size);
        if (eyeClosed != null) eyeClosed.setBounds(0, 0, size, size);
        if (lockIcon != null) lockIcon.setBounds(0, 0, size, size);

        continueBtn.setEnabled(false);

        // Watcher for real-time validation
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateInput();

                // Handle Icon switching logic for password field
                if (editPassword.hasFocus()) {
                    if (editPassword.getText().length() > 0) {
                        editPassword.setCompoundDrawables(lockIcon, null,
                                isPasswordVisible ? eyeClosed : eyeOpen, null);
                    } else {
                        editPassword.setCompoundDrawables(lockIcon, null, null, null);
                        isPasswordVisible = false;
                        editPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    }
                }
            }
        };

        // Add watcher to ALL fields (Name, Email, Password)
        editEmailAddress.addTextChangedListener(watcher);
        editPassword.addTextChangedListener(watcher);
        editName.addTextChangedListener(watcher); // Missing in original code

        continueBtn.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String email = editEmailAddress.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            // Double check (though button should be disabled if empty)
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                return;
            }

            // 1. UI Loading State
            continueBtn.setEnabled(false);
            continueBtn.setText("Creating Account...");
            continueBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.darker_gray)));

            // Clear previous errors
            emailError.setVisibility(View.GONE);
            passError.setVisibility(View.GONE);

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // --- SUCCESS: Save to Firestore ---
                            String uid = auth.getCurrentUser().getUid();
                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                            Map<String, Object> userData = new HashMap<>();
                            userData.put("name", name);
                            userData.put("email", email);

                            db.collection("users").document(uid)
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(RegisterActivity.this, "Account created!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(RegisterActivity.this, ProfileInfosActivity.class);
                                        // Clear stack so user can't go back to register
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        resetButtonState();
                                        Toast.makeText(RegisterActivity.this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });

                        } else {
                            // --- FAILURE: Handle specific errors ---
                            resetButtonState();

                            try {
                                throw task.getException();
                            }
                            // CASE: Email already exists
                            catch (FirebaseAuthUserCollisionException e) {
                                emailError.setText("This email is already registered.");
                                emailError.setVisibility(View.VISIBLE);
                                editEmailAddress.requestFocus();
                                Toast.makeText(RegisterActivity.this, "Email already exists. Please Login.", Toast.LENGTH_LONG).show();
                            }
                            // CASE: Weak Password (Firebase rule, not just local regex)
                            catch (FirebaseAuthWeakPasswordException e) {
                                passError.setText("Password is too weak. Try adding numbers or symbols.");
                                passError.setVisibility(View.VISIBLE);
                                editPassword.requestFocus();
                            }
                            // CASE: Network Error
                            catch (FirebaseNetworkException e) {
                                Toast.makeText(RegisterActivity.this, "No internet connection.", Toast.LENGTH_SHORT).show();
                            }
                            // CASE: Other
                            catch (Exception e) {
                                Toast.makeText(RegisterActivity.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        });

        editPassword.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int drawableEnd = 2;
                if (editPassword.getCompoundDrawables()[drawableEnd] != null &&
                        event.getRawX() >= (editPassword.getRight()
                                - editPassword.getCompoundDrawables()[drawableEnd].getBounds().width()
                                - editPassword.getPaddingEnd())) {

                    isPasswordVisible = !isPasswordVisible;

                    editPassword.setTransformationMethod(isPasswordVisible ?
                            HideReturnsTransformationMethod.getInstance() :
                            PasswordTransformationMethod.getInstance());

                    editPassword.setCompoundDrawables(lockIcon, null,
                            isPasswordVisible ? eyeClosed : eyeOpen, null);

                    editPassword.setSelection(editPassword.getText().length());
                    return true;
                }
            }
            return false;
        });

        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void resetButtonState() {
        continueBtn.setEnabled(true);
        continueBtn.setText("Continue");
        continueBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.primary)));
    }

    private void validateInput() {
        String nameText = editName.getText().toString().trim();
        String emailText = editEmailAddress.getText().toString().trim();
        String passText = editPassword.getText().toString().trim();

        boolean validName = !nameText.isEmpty();
        boolean validEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches();
        boolean validPass = passText.length() >= 6;

        // Email Error Visuals
        if (!validEmail && !emailText.isEmpty()) {
            emailError.setVisibility(View.VISIBLE);
            emailError.setText("Enter a valid email");
            emailError.setTextColor(getResources().getColor(R.color.primary));
        } else {
            emailError.setVisibility(View.GONE);
        }

        // Password Error Visuals
        if (!validPass && !passText.isEmpty()) {
            passError.setVisibility(View.VISIBLE);
            passError.setText("Password must be at least 6 characters");
            passError.setTextColor(getResources().getColor(R.color.primary));
        } else {
            passError.setVisibility(View.GONE);
        }

        // Enable button only if ALL fields are valid
        boolean enableButton = validName && validEmail && validPass;
        continueBtn.setEnabled(enableButton);

        if (enableButton) {
            continueBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.primary)));
        } else {
            continueBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.darker_gray)));
        }
    }
}