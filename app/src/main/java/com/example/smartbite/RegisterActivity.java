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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private Button continueBtn, backBtn;
    private TextView emailError, passError;
    private EditText editEmailAddress, editPassword;
    private Drawable eyeOpen, eyeClosed;
    private boolean isPasswordVisible = false;
    private FirebaseAuth auth;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Firebase Auth instance
        auth = FirebaseAuth.getInstance();

        // Initialize views
        continueBtn = findViewById(R.id.continueBtn);
        backBtn = findViewById(R.id.backBtn);
        emailError = findViewById(R.id.emailError);
        passError = findViewById(R.id.passError);
        editEmailAddress = findViewById(R.id.editEmailAddress);
        editPassword = findViewById(R.id.editPassword);

        // Load eye icons
        eyeOpen = ContextCompat.getDrawable(this, R.drawable.eye);
        eyeClosed = ContextCompat.getDrawable(this, R.drawable.hidden);

        // Resize eye icons to fit EditText height
        int size = (int) (editPassword.getLineHeight() * 0.8);
        eyeOpen.setBounds(0, 0, size, size);
        eyeClosed.setBounds(0, 0, size, size);

        // Disable continue button initially
        continueBtn.setEnabled(false);

        // TextWatcher for validation and eye icon visibility
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

                // Show eye icon if password has text
                if (editPassword.getText().length() > 0) {
                    if (isPasswordVisible) {
                        editPassword.setCompoundDrawables(null, null, eyeClosed, null);
                    } else {
                        editPassword.setCompoundDrawables(null, null, eyeOpen, null);
                    }
                } else {
                    editPassword.setCompoundDrawables(null, null, null, null);
                    isPasswordVisible = false;
                    editPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        };
        editEmailAddress.addTextChangedListener(watcher);
        editPassword.addTextChangedListener(watcher);

        // Continue button: create new user
        continueBtn.setOnClickListener(v -> {
            String email = editEmailAddress.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                            // Delay navigation so toast can be seen
                            editEmailAddress.postDelayed(() -> {
                                Intent intent = new Intent(RegisterActivity.this, ProfileInfosActivity.class);
                                startActivity(intent);
                                finish();
                            }, 500); // 0.5 sec delay
                        } else {
                            String message = "Registration failed";
                            if (task.getException() != null) {
                                message += ": " + task.getException().getMessage();
                            }
                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // Toggle password visibility
        editPassword.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int drawableEnd = 2;
                if (editPassword.getCompoundDrawables()[drawableEnd] != null &&
                        event.getRawX() >= (editPassword.getRight() - editPassword.getCompoundDrawables()[drawableEnd].getBounds().width() - editPassword.getPaddingEnd())) {

                    if (isPasswordVisible) {
                        editPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        editPassword.setCompoundDrawables(null, null, eyeOpen, null);
                        isPasswordVisible = false;
                    } else {
                        editPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        editPassword.setCompoundDrawables(null, null, eyeClosed, null);
                        isPasswordVisible = true;
                    }
                    editPassword.setSelection(editPassword.getText().length());
                    return true;
                }
            }
            return false;
        });

        // Back button
        backBtn.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    // Validate email and password input
    private void validateInput() {
        String emailText = editEmailAddress.getText().toString().trim();
        String passText = editPassword.getText().toString().trim();

        boolean validEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches();
        boolean validPass = passText.length() >= 6;

        if (!validEmail && !emailText.isEmpty()) {
            emailError.setVisibility(View.VISIBLE);
            emailError.setText("Enter a valid email");
        } else {
            emailError.setText("");
            emailError.setVisibility(View.GONE);
        }

        if (!validPass && !passText.isEmpty()) {
            passError.setText("Password must be at least 6 characters");
        } else {
            passError.setText("");
        }

        continueBtn.setEnabled(validEmail && validPass);
    }
}
