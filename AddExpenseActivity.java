package com.example.smartwallet;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddExpenseActivity extends AppCompatActivity {

    private EditText etTitle, etAmount, etCategory;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        etTitle = findViewById(R.id.etTitle);
        etAmount = findViewById(R.id.etAmount);
        etCategory = findViewById(R.id.etCategory);
        RadioButton rbIncome = findViewById(R.id.rbIncome);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnScan = findViewById(R.id.btnScanReceipt);
        DatabaseHelper db = new DatabaseHelper(this);

        // Initialize the camera launcher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        processImageForText(imageBitmap);
                    }
                });

        // Initialize the permission launcher
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        launchCamera();
                    } else {
                        Toast.makeText(this, "Camera permission is required to scan receipts", Toast.LENGTH_SHORT).show();
                    }
                });

        btnScan.setOnClickListener(v -> checkCameraPermissionAndLaunch());

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString();
            String amountStr = etAmount.getText().toString();
            String category = etCategory.getText().toString();
            String type = rbIncome.isChecked() ? "Income" : "Expense";

            if(title.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (userId == null) {
                Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            Expense expense = new Expense(title, amount, category, date, type);
            db.addExpense(expense, userId);

            Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(takePictureIntent);
    }

    private void processImageForText(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        Task<Text> result = recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    parseTextFromReceipt(visionText);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "OCR Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("OCR", "Error processing image", e);
                });
    }

    private void parseTextFromReceipt(Text visionText) {
        // --- How the OCR Parsing Works ---
        // We assume the first line of text on a receipt is the store name.
        // For the total, we find all numbers with decimal points in the entire text.
        // Then, we assume the largest number found is the final total.

        List<String> allLines = new ArrayList<>();
        for (Text.TextBlock block : visionText.getTextBlocks()) {
            for (Text.Line line : block.getLines()) {
                allLines.add(line.getText());
            }
        }

        if (allLines.isEmpty()) {
            Toast.makeText(this, "No text found on receipt", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Find the Title (Store Name)
        // We make a simple assumption that the first non-trivial line is the store name.
        String title = "";
        for (String line : allLines) {
            if (line.length() > 2) { // Ignore very short lines
                title = line;
                break;
            }
        }

        // 2. Find the Total Amount
        double maxAmount = -1.0;
        // This regex looks for numbers like: 123.45, 45.99, 1,234.56, etc.
        Pattern pattern = Pattern.compile("[0-9,]+\\.[0-9]{2}");
        
        for (String line : allLines) {
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()) {
                try {
                    // Remove commas for safe parsing
                    String numberStr = matcher.group().replace(",", "");
                    double currentAmount = Double.parseDouble(numberStr);
                    if (currentAmount > maxAmount) {
                        maxAmount = currentAmount;
                    }
                } catch (NumberFormatException e) {
                    // Ignore if parsing fails
                }
            }
        }

        // 3. Auto-fill the EditText fields
        etTitle.setText(title);
        if (maxAmount != -1.0) {
            etAmount.setText(String.format(Locale.US, "%.2f", maxAmount));
        }

        Toast.makeText(this, "Scanned! Please verify details.", Toast.LENGTH_LONG).show();
    }
}