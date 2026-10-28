package com.example.SmartPrint;
import android.database.Cursor;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import org.json.JSONObject;
import java.util.*;

public class PrintActivity extends AppCompatActivity implements PaymentResultListener {

    private static final String RAZORPAY_KEY = "rzp_test_TslcRo2TolCz8Z";

    private final List<PrintItem> items = new ArrayList<>();
    private DocAdapter adapter;
    private TextView tvTotal;
    private Button btnPay;
    private FirebaseUser user;

    private final ActivityResultLauncher<String[]> picker =
            registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(), uris -> {
                if (uris == null) return;
                for (Uri u : uris) items.add(new PrintItem(u, fileName(u), pageCount(u)));
                adapter.notifyDataSetChanged();
                updateTotal();
            });

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_print);
        user = FirebaseAuth.getInstance().getCurrentUser();
        Checkout.preload(getApplicationContext());

        tvTotal = findViewById(R.id.tvTotal);
        btnPay = findViewById(R.id.btnPay);
        RecyclerView rv = findViewById(R.id.rvDocs);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DocAdapter(items, this::updateTotal);
        rv.setAdapter(adapter);

        findViewById(R.id.btnAdd).setOnClickListener(v ->
                picker.launch(new String[]{"application/pdf", "image/*"}));
        btnPay.setOnClickListener(v -> startPayment());
        updateTotal();
    }

    private double total() {
        double t = 0;
        for (PrintItem i : items) t += i.price();
        return t;
    }

    private void updateTotal() {
        tvTotal.setText(String.format(Locale.US, "Total: ₹%.2f", total()));
        btnPay.setEnabled(!items.isEmpty());
    }

    private String fileName(Uri u) {
        String n = "document";
        try (Cursor c = getContentResolver().query(u, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (i >= 0) n = c.getString(i);
            }
        }
        return n;
    }

    private int pageCount(Uri u) {
        String type = getContentResolver().getType(u);
        if (type != null && type.equals("application/pdf")) {
            try (ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(u, "r");
                 PdfRenderer r = new PdfRenderer(pfd)) {
                return Math.max(1, r.getPageCount());
            } catch (Exception e) { }
        }
        return 1;
    }

    private void startPayment() {
        if (total() < 1) { Toast.makeText(this, "Nothing to pay", Toast.LENGTH_SHORT).show(); return; }
        try {
            Checkout co = new Checkout();
            co.setKeyID(RAZORPAY_KEY);
            JSONObject o = new JSONObject();
            o.put("name", "Smart Print");
            o.put("description", "Print order");
            o.put("currency", "INR");
            o.put("amount", Math.round(total() * 100));
            o.put("prefill", new JSONObject().put("email", user.getEmail()));
            co.open(this, o);
        } catch (Exception e) {
            Toast.makeText(this, "Payment error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPaymentSuccess(String paymentId) {
        btnPay.setEnabled(false);
        saveOrder(paymentId);
    }

    @Override
    public void onPaymentError(int code, String msg) {
        Toast.makeText(this, "Payment failed: " + msg, Toast.LENGTH_LONG).show();
    }

    private void saveOrder(String paymentId) {
        long now = System.currentTimeMillis();

        // 1) Save locally in SQLite
        new OrderDbHelper(this).insertOrder(user.getUid(), paymentId, total(),
                "Paid - Pending print", now, items);

        // 2) Save to Firestore (cloud copy)
        List<Map<String, Object>> docs = new ArrayList<>();
        for (PrintItem it : items) {
            Map<String, Object> m = new HashMap<>();
            m.put("name", it.name);
            m.put("pages", it.pages);
            m.put("color", it.color);
            m.put("duplex", it.duplex);
            m.put("copies", it.copies);
            docs.add(m);
        }
        Map<String, Object> order = new HashMap<>();
        order.put("userId", user.getUid());
        order.put("email", user.getEmail());
        order.put("paymentId", paymentId);
        order.put("total", total());
        order.put("status", "Paid - Pending print");
        order.put("createdAt", now);
        order.put("docs", docs);

        FirebaseFirestore.getInstance().collection("orders").add(order)
                .addOnCompleteListener(t -> {
                    Toast.makeText(this, "Order placed!", Toast.LENGTH_LONG).show();
                    finish();
                });
    }
}