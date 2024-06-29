package org.unipi.mpsp2343.project2;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {
    EditText emailText, passwordText, confirmPasswordText;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //<editor-fold desc="App specific code">
        emailText = findViewById(R.id.signUpEmailInput);
        passwordText = findViewById(R.id.signUpPasswordInput);
        confirmPasswordText = findViewById(R.id.signUpPasswordConfirmationInput);
        auth = FirebaseAuth.getInstance();
        //</editor-fold>
    }

    public void signUp(View view) {
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();
        String confirmPassword = confirmPasswordText.getText().toString();

        if(email.isEmpty()){
            Toast.makeText(this, getResources().getString(R.string.signup_error_email_empty), Toast.LENGTH_LONG).show();
            return;
        }
        if(password.isEmpty()){
            Toast.makeText(this, getResources().getString(R.string.signup_error_password_empty), Toast.LENGTH_LONG).show();
            return;
        }
        if(confirmPassword.isEmpty()){
            Toast.makeText(this, getResources().getString(R.string.signup_error_password_confirmation_empty), Toast.LENGTH_LONG).show();
            return;
        }
        if(!password.equals(confirmPassword)){
            Toast.makeText(this, getResources().getString(R.string.signup_error_password_no_match), Toast.LENGTH_LONG).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(
            task -> {
                if (task.isSuccessful()){
                    Toast.makeText(this, getResources().getString(R.string.signup_success), Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    showMessage("Error",task.getException().getLocalizedMessage());
                }
            }
        );
    }

    public void back(View view) {
        finish();
    }

    void showMessage(String title, String message){
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }
}