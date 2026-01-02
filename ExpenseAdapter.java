package com.example.smartwallet;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenseList;
    private List<Expense> expenseListFull; // Backup copy for filtering
    private Context context;
    private DatabaseHelper db;
    private Runnable onUpdateListener;
    private String currencySymbol;

    public ExpenseAdapter(List<Expense> expenseList, Context context, DatabaseHelper db, Runnable onUpdateListener, String currencySymbol) {
        this.expenseList = expenseList;
        this.expenseListFull = new ArrayList<>(expenseList); // Create a copy
        this.context = context;
        this.db = db;
        this.onUpdateListener = onUpdateListener;
        this.currencySymbol = currencySymbol;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);
        holder.tvTitle.setText(expense.getTitle());
        holder.tvCategory.setText(expense.getCategory());
        
        if ("Income".equals(expense.getType())) {
            holder.tvAmount.setText(String.format(Locale.getDefault(), "+ %s%.2f", currencySymbol, expense.getAmount()));
            holder.tvAmount.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            holder.tvAmount.setText(String.format(Locale.getDefault(), "- %s%.2f", currencySymbol, expense.getAmount()));
            holder.tvAmount.setTextColor(Color.parseColor("#F44336"));
        }
        
        holder.tvDate.setText(expense.getDate());

        String fullText = (expense.getTitle() + " " + expense.getCategory()).toLowerCase();
        
        int iconRes;
        int iconTint;

        if ("Income".equals(expense.getType())) {
            iconRes = R.drawable.income_logo;
            iconTint = Color.parseColor("#E8F5E9");
        } else {
            iconRes = R.drawable.bill;
            iconTint = Color.parseColor("#EEEEEE");

            if (fullText.contains("grocery") || fullText.contains("market")) {
                iconRes = R.drawable.grocery_logo;
                iconTint = Color.parseColor("#E8F5E9");
            } else if (fullText.contains("food") || fullText.contains("restaurant")) {
                iconRes = R.drawable.food_logo;
                iconTint = Color.parseColor("#FFF3E0");
            } else if (fullText.contains("transport") || fullText.contains("fuel")) {
                iconRes = R.drawable.transport;
                iconTint = Color.parseColor("#E3F2FD");
            } else if (fullText.contains("electric")) {
                iconRes = R.drawable.electricity;
                iconTint = Color.parseColor("#FFEBEE");
            } else if (fullText.contains("shop")) {
                iconRes = R.drawable.shopping;
                iconTint = Color.parseColor("#FCE4EC");
            }
        }

        holder.imgIcon.setImageResource(iconRes);
        holder.imgIcon.clearColorFilter();
        holder.iconContainer.setCardBackgroundColor(iconTint);

        holder.itemView.setOnLongClickListener(v -> {
            if (db != null && onUpdateListener != null) {
                db.deleteExpense(expense.getId());
                expenseList.remove(position);
                expenseListFull.remove(expense); // Ensure consistency
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, expenseList.size());
                onUpdateListener.run();
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public void filterList(String query) {
        expenseList.clear();
        if (query.isEmpty()) {
            expenseList.addAll(expenseListFull);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Expense item : expenseListFull) {
                if (item.getTitle().toLowerCase().contains(lowerCaseQuery) || 
                    item.getCategory().toLowerCase().contains(lowerCaseQuery)) {
                    expenseList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void updateList(List<Expense> newList) {
        this.expenseList = newList;
        notifyDataSetChanged();
    }

    public List<Expense> getCurrentList() {
        return this.expenseList;
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvAmount, tvDate;
        ImageView imgIcon;
        CardView iconContainer;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDate = itemView.findViewById(R.id.tvDate);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            iconContainer = itemView.findViewById(R.id.iconContainer);
        }
    }
}