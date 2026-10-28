package com.example.SmartPrint;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.*;

public class OrdersActivity extends AppCompatActivity {
    private OrderDbHelper db;
    private ListView lv;
    private String uid;
    private List<OrderDbHelper.Row> rows = new ArrayList<>();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_orders);
        lv = findViewById(R.id.lvOrders);
        lv.setEmptyView(findViewById(R.id.tvEmpty));
        db = new OrderDbHelper(this);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        lv.setOnItemLongClickListener((p, v, pos, id) -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete order?")
                    .setMessage("This removes it from this device.")
                    .setPositiveButton("Delete", (d, w) -> {
                        db.deleteOrder(rows.get(pos).id);
                        load();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });
        load();
    }

    private void load() {
        rows = db.getOrders(uid);
        SimpleDateFormat f = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        List<String> text = new ArrayList<>();
        for (OrderDbHelper.Row r : rows) {
            StringBuilder sb = new StringBuilder();
            sb.append(f.format(new Date(r.createdAt))).append("\n");
            for (String d : r.docs) sb.append("• ").append(d).append("\n");
            sb.append("Total: ₹").append(String.format(Locale.US, "%.2f", r.total))
                    .append("   |   ").append(r.status);
            text.add(sb.toString());
        }
        lv.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, text));
    }
}