package org.unipi.mpsp2343.project2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    EditText emailText, passwordText;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //<editor-fold desc="App specific code">
        emailText = findViewById(R.id.signInEmailInput);
        passwordText = findViewById(R.id.signInPasswordInput);
        auth = FirebaseAuth.getInstance();
        //</editor-fold>
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(auth.getCurrentUser() != null){
            goToHomeActivity();
        }
    }

    public void login(View view){
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();
        if(email.isEmpty()){
            Toast.makeText(this, getResources().getString(R.string.login_error_empty_email), Toast.LENGTH_LONG).show();
            return;
        }
        if(password.isEmpty()){
            Toast.makeText(this, getResources().getString(R.string.login_error_empty_pwd), Toast.LENGTH_LONG).show();
            return;
        }
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(
            task -> {
                if (task.isSuccessful()){
                    Toast.makeText(this, getResources().getString(R.string.login_success), Toast.LENGTH_LONG).show();
                    goToHomeActivity();
                } else {
                    Toast.makeText(this, getResources().getString(R.string.login_error_invalid_credentials), Toast.LENGTH_LONG).show();
                }
            }
        );
    }

    public void signUp(View view) {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    void showMessage(String title, String message){
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }

    private void goToHomeActivity(){
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}