package com.example.smartbite;

import android.annotation.SuppressLint;
import android.content.Intent;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class LoginActivity extends AppCompatActivity {

    private Button loginBtn;
    private TextView emailError, passError;
    private EditText email, password;
    private Drawable eyeOpen, eyeClosed;
    private boolean isPasswordVisible = false; // single boolean instead of array

    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        loginBtn = findViewById(R.id.loginBtn);
        Button createAccountBtn = findViewById(R.id.createAccountBtn);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        emailError = findViewById(R.id.emailError);
        passError = findViewById(R.id.passError);
        eyeOpen = ContextCompat.getDrawable(this, R.drawable.eye__1_);
        eyeClosed = ContextCompat.getDrawable(this, R.drawable.hidden__1_);

        // Resize eye icons to fit EditText
        int size = (int) (password.getLineHeight() * 0.8);
        eyeOpen.setBounds(0, 0, size, size);
        eyeClosed.setBounds(0, 0, size, size);

        // Navigate to create account activity
        createAccountBtn.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Initially disable login button
        loginBtn.setEnabled(false);

        // Watch for changes in email and password to validate input
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
            }
        };
        email.addTextChangedListener(watcher);
        password.addTextChangedListener(watcher);

        // Navigate to next activity on login
        loginBtn.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
        });

        // Show eye icon when user types
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    password.setCompoundDrawables(null, null, eyeOpen, null);
                } else {
                    password.setCompoundDrawables(null, null, null, null);
                    isPasswordVisible = false;
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        // Toggle password visibility when clicking the eye icon
        password.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int drawableEnd = 2; // right drawable
                if (password.getCompoundDrawables()[drawableEnd] != null &&
                        event.getRawX() >= (password.getRight() - password.getCompoundDrawables()[drawableEnd].getBounds().width() - password.getPaddingEnd())) {

                    if (isPasswordVisible) {
                        password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        password.setCompoundDrawables(null, null, eyeOpen, null);
                        isPasswordVisible = false;
                    } else {
                        password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        password.setCompoundDrawables(null, null, eyeClosed, null);
                        isPasswordVisible = true;
                    }
                    password.setSelection(password.getText().length());
                    return true;
                }
            }
            return false;
        });
    }

    @SuppressLint("SetTextI18n")
    private void validateInput() {
        String emailText = email.getText().toString().trim();
        String passText = password.getText().toString().trim();

        boolean validEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches();
        boolean validPass = passText.length() >= 6;

        // Show error messages
        if (!validEmail && !emailText.isEmpty()) {
            emailError.setText("Enter a valid email");
        } else {
            emailError.setText("");
        }

        if (!validPass && !passText.isEmpty()) {
            passError.setText("Password must be at least 6 characters");
        } else {
            passError.setText("");
        }

        // Enable login button only if both are valid
        loginBtn.setEnabled(validEmail && validPass);
    }
}
