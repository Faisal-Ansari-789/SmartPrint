package com.example.SmartPrint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class DocAdapter extends RecyclerView.Adapter<DocAdapter.VH> {
    private final List<PrintItem> items;
    private final Runnable onChange;

    public DocAdapter(List<PrintItem> items, Runnable onChange) {
        this.items = items; this.onChange = onChange;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, pages, copies, price;
        RadioGroup colorGroup; RadioButton rbBw, rbColor;
        CheckBox duplex; Button minus, plus, remove;
        VH(View v) {
            super(v);
            name = v.findViewById(R.id.tvDocName);
            pages = v.findViewById(R.id.tvPages);
            copies = v.findViewById(R.id.tvCopies);
            price = v.findViewById(R.id.tvItemPrice);
            colorGroup = v.findViewById(R.id.colorGroup);
            rbBw = v.findViewById(R.id.rbBw);
            rbColor = v.findViewById(R.id.rbColor);
            duplex = v.findViewById(R.id.cbDuplex);
            minus = v.findViewById(R.id.btnMinus);
            plus = v.findViewById(R.id.btnPlus);
            remove = v.findViewById(R.id.btnRemove);
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_print_doc, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        PrintItem it = items.get(pos);
        h.name.setText(it.name);
        h.pages.setText(it.pages + (it.pages == 1 ? " page" : " pages"));
        h.copies.setText(String.valueOf(it.copies));
        h.price.setText(String.format(Locale.US, "₹%.2f", it.price()));

        h.colorGroup.setOnCheckedChangeListener(null);
        h.rbColor.setChecked(it.color);
        h.rbBw.setChecked(!it.color);
        h.duplex.setOnCheckedChangeListener(null);
        h.duplex.setChecked(it.duplex);

        h.colorGroup.setOnCheckedChangeListener((g, id) -> {
            it.color = (id == R.id.rbColor);
            refresh(h, it);
        });
        h.duplex.setOnCheckedChangeListener((b, checked) -> {
            it.duplex = checked;
            refresh(h, it);
        });
        h.plus.setOnClickListener(v -> {
            if (it.copies < 99) it.copies++;
            refresh(h, it);
        });
        h.minus.setOnClickListener(v -> {
            if (it.copies > 1) it.copies--;
            refresh(h, it);
        });
        h.remove.setOnClickListener(v -> {
            int i = h.getAdapterPosition();
            if (i != RecyclerView.NO_POSITION) {
                items.remove(i);
                notifyItemRemoved(i);
                onChange.run();
            }
        });
    }

    private void refresh(VH h, PrintItem it) {
        h.copies.setText(String.valueOf(it.copies));
        h.price.setText(String.format(Locale.US, "₹%.2f", it.price()));
        onChange.run();
    }

    @Override public int getItemCount() { return items.size(); }
}