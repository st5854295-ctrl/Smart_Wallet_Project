package com.example.smartwallet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder> {

    private List<Goal> goalList;
    private Context context;
    private OnGoalActionListener listener;

    public interface OnGoalActionListener {
        void onEdit(Goal goal);
        void onDelete(Goal goal);
    }

    public GoalAdapter(List<Goal> goalList, Context context, OnGoalActionListener listener) {
        this.goalList = goalList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_goal, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        Goal goal = goalList.get(position);

        holder.tvGoalName.setText(goal.getName());
        holder.tvSaved.setText("$" + String.format("%.0f", goal.getSavedAmount()));
        holder.tvTarget.setText(" of $" + String.format("%.0f", goal.getTargetAmount()));

        int progress = 0;
        if (goal.getTargetAmount() > 0) { // Avoid division by zero
            progress = (int) ((goal.getSavedAmount() / goal.getTargetAmount()) * 100);
        }
        holder.progressBar.setProgress(progress);
        holder.tvGoalProgress.setText(progress + "%");

        // CORRECTED ICON LOGIC
        String iconName = goal.getIcon();
        if ("car".equalsIgnoreCase(iconName)) {
            holder.imgGoalIcon.setImageResource(R.drawable.ic_car);
        } else if ("house".equalsIgnoreCase(iconName)) {
            holder.imgGoalIcon.setImageResource(R.drawable.ic_house);
        } else if ("education".equalsIgnoreCase(iconName)) {
            holder.imgGoalIcon.setImageResource(R.drawable.ic_education);
        } else {
            // Default to the loan icon for any other goal type
            holder.imgGoalIcon.setImageResource(R.drawable.ic_loan);
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(goal));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(goal));
    }

    @Override
    public int getItemCount() {
        return goalList.size();
    }

    public static class GoalViewHolder extends RecyclerView.ViewHolder {
        TextView tvGoalName, tvSaved, tvTarget, tvGoalProgress;
        ProgressBar progressBar;
        ImageView imgGoalIcon;
        ImageButton btnEdit, btnDelete;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGoalName = itemView.findViewById(R.id.tvGoalName);
            tvSaved = itemView.findViewById(R.id.tvSaved);
            tvTarget = itemView.findViewById(R.id.tvTarget);
            tvGoalProgress = itemView.findViewById(R.id.tvGoalProgress);
            progressBar = itemView.findViewById(R.id.progressBarGoal);
            imgGoalIcon = itemView.findViewById(R.id.imgGoalIcon);
            btnEdit = itemView.findViewById(R.id.btnEditGoal);
            btnDelete = itemView.findViewById(R.id.btnDeleteGoal);
        }
    }
}