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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {
    Button continueBtn, backBtn;
    TextView emailError, passError;
    EditText editEmailAddress, editPassword;
    Drawable eyeOpen, eyeClosed;
    boolean isPasswordVisible = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        continueBtn = (Button) findViewById(R.id.continueBtn);
        backBtn = (Button) findViewById(R.id.backBtn);
        emailError = (TextView) findViewById(R.id.emailError);
        passError = (TextView) findViewById(R.id.passError);
        editEmailAddress = (EditText) findViewById(R.id.editEmailAddress);
        editPassword = (EditText) findViewById(R.id.editPassword);
        eyeOpen = ContextCompat.getDrawable(this, R.drawable.eye__1_);
        eyeClosed = ContextCompat.getDrawable(this, R.drawable.hidden__1_);

        // Resize eye icons to fit EditText
        int size = (int) (editPassword.getLineHeight() * 0.8);
        eyeOpen.setBounds(0, 0, size, size);
        eyeClosed.setBounds(0, 0, size, size);

//        Navigate to create profile activity pressing continue button
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent int1 = new Intent(RegisterActivity.this, ProfileInfosActivity.class);
                startActivity(int1);
            }
        });


        // To disable the button in the start till successful login
        continueBtn.setEnabled(false);
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateInput();
            }
        };
        editEmailAddress.addTextChangedListener(watcher);
        editPassword.addTextChangedListener(watcher);

        // Show eye icon when user types
        editPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    editPassword.setCompoundDrawables(null, null, eyeOpen, null);
                } else {
                    editPassword.setCompoundDrawables(null, null, null, null);
                    isPasswordVisible = false;
                    editPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        // Toggle password visibility when clicking the eye icon
        editPassword.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int drawableEnd = 2; // right drawable
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

        //    Back button to navigate back
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent int2 = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(int2);
            }
        });
    }

    //    Method to validate email and password input
    @SuppressLint("SetTextI18n")
    private void validateInput() {
        String emailText = editEmailAddress.getText().toString().trim();
        String passText = editPassword.getText().toString().trim();

        boolean validEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches();
        boolean validPass = passText.length() >= 6;

        // Show or clear error messages
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

        // Enable the button only if both are valid
        continueBtn.setEnabled(validEmail && validPass);
    }


}