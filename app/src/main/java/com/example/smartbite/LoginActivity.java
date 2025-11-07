package com.example.smartbite;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {
    Button loginBtn;
    TextView emailError , passError;
    EditText email , password;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
//        Navigate between activities


        loginBtn = (Button) findViewById(R.id.loginBtn);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        emailError = (TextView) findViewById(R.id.emailError);
        passError = (TextView) findViewById(R.id.passErorr);
// To disable the button in the start
        loginBtn.setEnabled(false);

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
    email.addTextChangedListener(watcher);
    password.addTextChangedListener(watcher);

      loginBtn.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              Intent int1= new Intent(LoginActivity.this , HomeActivity.class);
              startActivity(int1);
          }
      });
    }
    @SuppressLint("SetTextI18n")
//    Method to validate email and password input
    private void validateInput() {
        String emailText = email.getText().toString().trim();
        String passText = password.getText().toString().trim();

        boolean validEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches();
        boolean validPass = passText.length() >= 6;

        // Show or clear error messages
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

        // Enable the button only if both are valid
        loginBtn.setEnabled(validEmail && validPass);
    }
}

