package com.example.smartbite;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private Button loginBtn;
    private TextView emailError, passError;
    private EditText email, password;
    private Drawable eyeOpen, eyeClosed, lockIcon;
    private boolean isPasswordVisible = false;

    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loginBtn = findViewById(R.id.loginBtn);
        Button createAccountBtn = findViewById(R.id.createAccountBtn);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        emailError = findViewById(R.id.emailError);
        passError = findViewById(R.id.passError);

        eyeOpen = ContextCompat.getDrawable(this, R.drawable.eye);
        eyeClosed = ContextCompat.getDrawable(this, R.drawable.hidden);
        lockIcon = ContextCompat.getDrawable(this, R.drawable.baseline_lock_24);

        int iconSize = (int) (password.getLineHeight() * 0.7);
        if (lockIcon != null) lockIcon.setBounds(0, 0, iconSize, iconSize);
        if (eyeOpen != null) eyeOpen.setBounds(0, 0, iconSize, iconSize);
        if (eyeClosed != null) eyeClosed.setBounds(0, 0, iconSize, iconSize);

        password.setCompoundDrawablesRelative(lockIcon, null, null, null);
        loginBtn.setEnabled(false);

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
                if (password.getText().length() > 0) {
                    password.setCompoundDrawablesRelative(lockIcon, null, isPasswordVisible ? eyeClosed : eyeOpen, null);
                } else {
                    password.setCompoundDrawablesRelative(lockIcon, null, null, null);
                    isPasswordVisible = false;
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        };
        email.addTextChangedListener(watcher);
        password.addTextChangedListener(watcher);

        createAccountBtn.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // -------------------------------------------------------------
        // UPDATED LOGIN LOGIC
        // -------------------------------------------------------------
        loginBtn.setOnClickListener(v -> {
            String emailText = email.getText().toString().trim();
            String passText = password.getText().toString().trim();

            // 1. Disable button, show loading
            loginBtn.setEnabled(false);
            loginBtn.setText("Logging in...");
            loginBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.darker_gray)));

            // 2. Clear previous errors visually
            email.setError(null);
            password.setError(null);

            auth.signInWithEmailAndPassword(emailText, passText)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // --- SUCCESS ---
                            if (auth.getCurrentUser() != null) {
                                String uid = auth.getCurrentUser().getUid();
                                retrieveUserPreferences(uid);
                            }
                        } else {
                            // --- FAILURE ---

                            // 1. Reset Button
                            loginBtn.setEnabled(true);
                            loginBtn.setText("Login");
                            loginBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.primary)));

                            // 2. Handle Specific Errors
                            try {
                                throw task.getException();
                            }
                            // CASE A: User does not exist OR Account Disabled
                            // This is the specific block that handles "Email not found"
                            catch (FirebaseAuthInvalidUserException e) {
                                email.setError("Account does not exist");
                                email.requestFocus(); // Force focus to Email
                                password.setText(""); // Optional: Clear password
                            }
                            // CASE B: Wrong Password
                            catch (FirebaseAuthInvalidCredentialsException e) {
                                // Sometimes "Invalid Email format" also falls here
                                String errorCode = e.getErrorCode();
                                if ("ERROR_INVALID_EMAIL".equals(errorCode)) {
                                    email.setError("Invalid email format");
                                    email.requestFocus();
                                } else {
                                    password.setError("Incorrect password");
                                    password.requestFocus(); // Force focus to Password
                                }
                            }
                            // CASE C: Brute force protection
                            catch (FirebaseTooManyRequestsException e) {
                                Toast.makeText(LoginActivity.this, "Too many failed attempts. Try again later.", Toast.LENGTH_LONG).show();
                            }
                            // CASE D: Network
                            catch (FirebaseNetworkException e) {
                                Toast.makeText(LoginActivity.this, "No internet connection.", Toast.LENGTH_SHORT).show();
                            }
                            // CASE E: Unknown
                            catch (Exception e) {
                                Toast.makeText(LoginActivity.this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });

        password.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int drawableEnd = 2;
                Drawable rightDrawable = password.getCompoundDrawablesRelative()[drawableEnd];
                if (rightDrawable != null && event.getRawX() >= (password.getRight() - rightDrawable.getBounds().width() - password.getPaddingEnd())) {
                    isPasswordVisible = !isPasswordVisible;
                    if (isPasswordVisible) {
                        password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        password.setCompoundDrawablesRelative(lockIcon, null, eyeClosed, null);
                    } else {
                        password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        password.setCompoundDrawablesRelative(lockIcon, null, eyeOpen, null);
                    }
                    password.setSelection(password.getText().length());
                    return true;
                }
            }
            return false;
        });
    }

    private void retrieveUserPreferences(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        String name = documentSnapshot.getString("name");
                        if (name != null) editor.putString("name", name);

                        saveListAsString(documentSnapshot, editor, "healthConditions", "conditions");
                        saveListAsString(documentSnapshot, editor, "dietaryPreferences", "diet");
                        saveListAsString(documentSnapshot, editor, "allergies", "allergies");
                        saveListAsString(documentSnapshot, editor, "goals", "goals");

                        editor.apply();

                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Profile incomplete. Please finish setup.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(LoginActivity.this, ProfileInfosActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    loginBtn.setEnabled(true);
                    loginBtn.setText("Login");
                    loginBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.primary)));

                    String errorMsg = "Error loading profile.";
                    if (e instanceof FirebaseNetworkException) {
                        errorMsg = "Network error. Unable to load profile.";
                    }
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                });
    }

    private void saveListAsString(DocumentSnapshot snapshot, SharedPreferences.Editor editor, String firestoreKey, String prefsKey) {
        List<String> list = (List<String>) snapshot.get(firestoreKey);
        if (list != null && !list.isEmpty()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                editor.putString(prefsKey, String.join(", ", list));
            } else {
                editor.putString(prefsKey, TextUtils.join(", ", list));
            }
        } else {
            editor.putString(prefsKey, "");
        }
    }

    private void validateInput() {
        String emailText = email.getText().toString().trim();
        String passText = password.getText().toString().trim();
        boolean validEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches();
        boolean validPass = passText.length() >= 6;

        if (!validEmail && !emailText.isEmpty()) {
            emailError.setText("Enter a valid email");
            emailError.setTextColor(getResources().getColor(R.color.primary));
        } else emailError.setText("");

        if (!validPass && !passText.isEmpty()) {
            passError.setText("Password must be at least 6 characters");
            passError.setTextColor(getResources().getColor(R.color.primary));
        } else passError.setText("");

        boolean enableButton = validEmail && validPass;
        loginBtn.setEnabled(enableButton);

        if (enableButton) {
            loginBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.primary)));
        }
    }
}