package com.example.smartwallet;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GoalActivity extends AppCompatActivity implements GoalAdapter.OnGoalActionListener {

    private DatabaseHelper db;
    private RecyclerView recyclerView;
    private GoalAdapter adapter;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals);

        db = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerViewGoals);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Form Views
        EditText etGoalName = findViewById(R.id.etGoalName);
        EditText etTargetAmount = findViewById(R.id.etTargetAmount);
        EditText etSavedAmount = findViewById(R.id.etSavedAmount);
        RadioGroup rgIcons = findViewById(R.id.rgIcons);
        Button btnSaveGoal = findViewById(R.id.btnSaveGoal);

        loadGoals();

        btnSaveGoal.setOnClickListener(v -> {
            String name = etGoalName.getText().toString();
            String targetStr = etTargetAmount.getText().toString();
            String savedStr = etSavedAmount.getText().toString();
            
            if(name.isEmpty() || targetStr.isEmpty() || savedStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double target = Double.parseDouble(targetStr);
            double saved = Double.parseDouble(savedStr);
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            // CORRECTED: Default to a generic type if no specific radio button is selected.
            String icon;
            int selectedId = rgIcons.getCheckedRadioButtonId();
            if (selectedId == R.id.rbCar) {
                icon = "car";
            } else if (selectedId == R.id.rbHouse) {
                icon = "house";
            } else if (selectedId == R.id.rbEducation) {
                icon = "education";
            } else {
                icon = "loan"; // Default for any other goal
            }

            db.addGoal(name, target, saved, date, icon, currentUserId);

            Toast.makeText(this, "Goal Saved!", Toast.LENGTH_SHORT).show();
            
            etGoalName.setText("");
            etTargetAmount.setText("");
            etSavedAmount.setText("");
            rgIcons.clearCheck();
            
            loadGoals();
        });
    }

    private void loadGoals() {
        if (currentUserId != null) {
            List<Goal> list = db.getAllGoals(currentUserId);
            adapter = new GoalAdapter(list, this, this);
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onEdit(Goal goal) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Saved Amount for " + goal.getName());

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Enter new saved amount");
        builder.setView(input);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newAmountStr = input.getText().toString();
            if (!newAmountStr.isEmpty()) {
                double newAmount = Double.parseDouble(newAmountStr);
                db.updateGoal(goal.getId(), newAmount);
                Toast.makeText(this, "Goal Updated!", Toast.LENGTH_SHORT).show();
                loadGoals(); // Refresh the list
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    public void onDelete(Goal goal) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Goal")
            .setMessage("Are you sure you want to delete this goal?")
            .setPositiveButton("Yes", (dialog, which) -> {
                db.deleteGoal(goal.getId());
                Toast.makeText(this, "Goal Deleted", Toast.LENGTH_SHORT).show();
                loadGoals(); // Refresh the list
            })
            .setNegativeButton("No", null)
            .show();
    }
}