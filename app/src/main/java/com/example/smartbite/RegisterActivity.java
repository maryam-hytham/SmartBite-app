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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private Button continueBtn, backBtn;
    private TextView emailError, passError;
    private EditText editEmailAddress, editPassword;
    private EditText editName;
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
        eyeOpen.setBounds(0, 0, size, size);
        eyeClosed.setBounds(0, 0, size, size);
        lockIcon.setBounds(0, 0, size, size);

        continueBtn.setEnabled(false);

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

                if (editPassword.getText().length() > 0) {
                    editPassword.setCompoundDrawables(lockIcon, null,
                            isPasswordVisible ? eyeClosed : eyeOpen, null);
                } else {
                    editPassword.setCompoundDrawables(lockIcon, null, null, null);
                    isPasswordVisible = false;
                    editPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        };

        editEmailAddress.addTextChangedListener(watcher);
        editPassword.addTextChangedListener(watcher);

        continueBtn.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String email = editEmailAddress.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            String uid = auth.getCurrentUser().getUid();

                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                            Map<String, Object> userData = new HashMap<>();
                            userData.put("name", name);
                            userData.put("email", email);

                            db.collection("users").document(uid)
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(RegisterActivity.this,
                                                "Account created!", Toast.LENGTH_SHORT).show();

                                        startActivity(new Intent(RegisterActivity.this,
                                                ProfileInfosActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(RegisterActivity.this,
                                                "Failed to save data: " + e.getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    });

                        } else {
                            Toast.makeText(RegisterActivity.this,
                                    "Registration failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
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
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void validateInput() {
        String emailText = editEmailAddress.getText().toString().trim();
        String passText = editPassword.getText().toString().trim();

        boolean validEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches();
        boolean validPass = passText.length() >= 6;

        if (!validEmail && !emailText.isEmpty()) {
            emailError.setVisibility(View.VISIBLE);
            emailError.setText("Enter a valid email");
            emailError.setTextColor(getResources().getColor(R.color.primary));
        } else {
            emailError.setVisibility(View.GONE);
        }

        if (!validPass && !passText.isEmpty()) {
            passError.setVisibility(View.VISIBLE);
            passError.setText("Password must be at least 6 characters");
            passError.setTextColor(getResources().getColor(R.color.primary));
        } else {
            passError.setVisibility(View.GONE);
        }

        continueBtn.setEnabled(validEmail && validPass);
        if (continueBtn.isEnabled()) {
            continueBtn.setBackgroundColor(getResources().getColor(R.color.primary));
        }

    }
}
