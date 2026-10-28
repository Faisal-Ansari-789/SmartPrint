package com.example.SmartPrint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class OrderDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "smartprint.db";
    private static final int DB_VERSION = 1;

    public static class Row {
        public long id;
        public long createdAt;
        public double total;
        public String status;
        public List<String> docs = new ArrayList<>();
    }

    public OrderDbHelper(Context c) { super(c, DB_NAME, null, DB_VERSION); }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE orders (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id TEXT, payment_id TEXT, total REAL, status TEXT, created_at INTEGER)");
        db.execSQL("CREATE TABLE order_items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "order_id INTEGER, name TEXT, pages INTEGER, color INTEGER, duplex INTEGER, copies INTEGER, " +
                "FOREIGN KEY(order_id) REFERENCES orders(id) ON DELETE CASCADE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int o, int n) {
        db.execSQL("DROP TABLE IF EXISTS order_items");
        db.execSQL("DROP TABLE IF EXISTS orders");
        onCreate(db);
    }

    public long insertOrder(String uid, String paymentId, double total, String status,
                            long ts, List<PrintItem> items) {
        SQLiteDatabase db = getWritableDatabase();
        long orderId = -1;
        db.beginTransaction();
        try {
            ContentValues v = new ContentValues();
            v.put("user_id", uid);
            v.put("payment_id", paymentId);
            v.put("total", total);
            v.put("status", status);
            v.put("created_at", ts);
            orderId = db.insert("orders", null, v);
            for (PrintItem it : items) {
                ContentValues iv = new ContentValues();
                iv.put("order_id", orderId);
                iv.put("name", it.name);
                iv.put("pages", it.pages);
                iv.put("color", it.color ? 1 : 0);
                iv.put("duplex", it.duplex ? 1 : 0);
                iv.put("copies", it.copies);
                db.insert("order_items", null, iv);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return orderId;
    }

    public List<Row> getOrders(String uid) {
        List<Row> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT id, total, status, created_at FROM orders " +
                "WHERE user_id = ? ORDER BY created_at DESC", new String[]{uid})) {
            while (c.moveToNext()) {
                Row r = new Row();
                r.id = c.getLong(0);
                r.total = c.getDouble(1);
                r.status = c.getString(2);
                r.createdAt = c.getLong(3);
                try (Cursor ic = db.rawQuery("SELECT name, pages, color, duplex, copies " +
                        "FROM order_items WHERE order_id = ?", new String[]{String.valueOf(r.id)})) {
                    while (ic.moveToNext()) {
                        r.docs.add(ic.getString(0) + " (" + ic.getInt(1) + "p, "
                                + (ic.getInt(2) == 1 ? "Colour" : "B&W") + ", "
                                + (ic.getInt(3) == 1 ? "Back-to-back" : "Single") + ", x"
                                + ic.getInt(4) + ")");
                    }
                }
                list.add(r);
            }
        }
        return list;
    }

    public void updateStatus(long id, String status) {
        ContentValues v = new ContentValues();
        v.put("status", status);
        getWritableDatabase().update("orders", v, "id = ?", new String[]{String.valueOf(id)});
    }

    public void deleteOrder(long id) {
        getWritableDatabase().delete("orders", "id = ?", new String[]{String.valueOf(id)});
    }
}