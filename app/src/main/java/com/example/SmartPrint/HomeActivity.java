package com.example.SmartPrint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_home);
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        ((TextView) findViewById(R.id.tvHello)).setText("Hello, " + email);

        findViewById(R.id.cardNew).setOnClickListener(v -> startActivity(new Intent(this, PrintActivity.class)));
        findViewById(R.id.cardOrders).setOnClickListener(v -> startActivity(new Intent(this, OrdersActivity.class)));
        findViewById(R.id.cardLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}