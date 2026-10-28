package com.example.SmartPrint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import androidx.appcompat.app.AppCompatDelegate;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private EditText etEmail, etPass;

    @Override
    protected void onCreate(Bundle b) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(b);
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) { goHome(); return; }
        setContentView(R.layout.activity_login);
        etEmail = findViewById(R.id.etEmail);
        etPass = findViewById(R.id.etPassword);

        findViewById(R.id.btnLogin).setOnClickListener(v -> {
            String e = etEmail.getText().toString().trim(), p = etPass.getText().toString();
            if (e.isEmpty() || p.isEmpty()) { toast("Enter email and password"); return; }
            auth.signInWithEmailAndPassword(e, p)
                    .addOnSuccessListener(r -> goHome())
                    .addOnFailureListener(x -> toast(x.getMessage()));
        });
        findViewById(R.id.btnCreate).setOnClickListener(v -> {
            String e = etEmail.getText().toString().trim(), p = etPass.getText().toString();
            if (e.isEmpty() || p.length() < 6) { toast("Email required, password min 6 chars"); return; }
            auth.createUserWithEmailAndPassword(e, p)
                    .addOnSuccessListener(r -> goHome())
                    .addOnFailureListener(x -> toast(x.getMessage()));
        });
    }

    private void goHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
    private void toast(String m) { Toast.makeText(this, m, Toast.LENGTH_LONG).show(); }
}