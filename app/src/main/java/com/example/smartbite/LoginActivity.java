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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
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

        // Firebase Auth instance
        auth = FirebaseAuth.getInstance();

        // Initialize views
        loginBtn = findViewById(R.id.loginBtn);
        Button createAccountBtn = findViewById(R.id.createAccountBtn);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        emailError = findViewById(R.id.emailError);
        passError = findViewById(R.id.passError);

        // Load drawables
        eyeOpen = ContextCompat.getDrawable(this, R.drawable.eye);
        eyeClosed = ContextCompat.getDrawable(this, R.drawable.hidden);
        lockIcon = ContextCompat.getDrawable(this, R.drawable.baseline_lock_24);

        // Resize icons
        int iconSize = (int) (password.getLineHeight() * 0.7);
        lockIcon.setBounds(0, 0, iconSize, iconSize);
        eyeOpen.setBounds(0, 0, iconSize, iconSize);
        eyeClosed.setBounds(0, 0, iconSize, iconSize);

        // Initial password drawable (lock left)
        password.setCompoundDrawablesRelative(lockIcon, null, null, null);

        // Disable login button initially
        loginBtn.setEnabled(false);


        // TextWatcher for validation and eye icon
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
                    if (isPasswordVisible) {
                        password.setCompoundDrawablesRelative(lockIcon, null, eyeClosed, null);
                    } else {
                        password.setCompoundDrawablesRelative(lockIcon, null, eyeOpen, null);
                    }
                } else {
                    password.setCompoundDrawablesRelative(lockIcon, null, null, null);
                    isPasswordVisible = false;
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        };
        email.addTextChangedListener(watcher);
        password.addTextChangedListener(watcher);

        // Navigate to create account activity
        createAccountBtn.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Login button: sign in existing user
        loginBtn.setOnClickListener(v -> {
            String emailText = email.getText().toString().trim();
            String passText = password.getText().toString().trim();

            auth.signInWithEmailAndPassword(emailText, passText)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, ProfileInfosActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // Toggle password visibility
        password.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int drawableEnd = 2;
                Drawable rightDrawable = password.getCompoundDrawablesRelative()[drawableEnd];
                if (rightDrawable != null &&
                        event.getRawX() >= (password.getRight() - rightDrawable.getBounds().width() - password.getPaddingEnd())) {

                    if (isPasswordVisible) {
                        password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        password.setCompoundDrawablesRelative(lockIcon, null, eyeOpen, null);
                        isPasswordVisible = false;
                    } else {
                        password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        password.setCompoundDrawablesRelative(lockIcon, null, eyeClosed, null);
                        isPasswordVisible = true;
                    }
                    password.setSelection(password.getText().length());
                    return true;
                }
            }
            return false;
        });
    }

    // Validate input and change button color
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

        // Change color dynamically
        if (enableButton) {
            loginBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.primary)));
        }
    }
}

