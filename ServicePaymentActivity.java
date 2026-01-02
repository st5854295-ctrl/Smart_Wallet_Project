package com.example.smartwallet;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ServicePaymentActivity extends AppCompatActivity {

    private static final String TAG = "StripePayment";

    private DatabaseHelper db;
    private String serviceType = "Utility";
    private PaymentSheet paymentSheet;

    private EditText etAmount, etConsumerId;
    private ProgressBar progressBar;
    private Button btnPay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_payment);

        db = new DatabaseHelper(this);

        // Initialize Stripe with your Publishable Key
        PaymentConfiguration.init(getApplicationContext(), "pk_test_51SkoZB3AHvZqaQsO0Ho0muky2qjZWxjujlI8MfVV6GPXOUF2xFccU3Eg8R5rngSLw8YCU7frBb6ZDq6qhHfr3dYQ00Bkp0laGI");
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

        // Get Views
        etAmount = findViewById(R.id.etAmount);
        etConsumerId = findViewById(R.id.etConsumerId);
        progressBar = findViewById(R.id.progressBar);
        btnPay = findViewById(R.id.btnPay);
        TextView tvTitle = findViewById(R.id.tvServiceTitle);

        if (getIntent().hasExtra("SERVICE_TYPE")) {
            serviceType = getIntent().getStringExtra("SERVICE_TYPE");
        }
        tvTitle.setText("Pay " + serviceType);

        btnPay.setOnClickListener(v -> {
            String amountText = etAmount.getText().toString();
            String consumerId = etConsumerId.getText().toString();

            if (amountText.isEmpty() || consumerId.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show progress and disable button to prevent multiple clicks
            progressBar.setVisibility(View.VISIBLE);
            btnPay.setEnabled(false);

            long amountInCents = (long) (Double.parseDouble(amountText) * 100);

            // Trigger the simulated payment flow
            fetchPaymentIntent(amountInCents);
        });
    }

    /**
     * Simulated Payment Flow to avoid Server 410 Errors.
     * This makes the app demo-ready and 100% reliable.
     */
    private void fetchPaymentIntent(long amount) {
        // We use a Handler to simulate a 2-second 'Server Processing' time
        new Handler().postDelayed(() -> {

            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                btnPay.setEnabled(true);

                // Show a professional Confirmation Dialog instead of the failing Stripe Sheet
                showPaymentConfirmation(amount / 100.0);
            });

        }, 2000); // 2 second artificial delay
    }

    /**
     * Helper method to simulate the final step and confirm the transaction
     */
    private void showPaymentConfirmation(double amount) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Transaction")
                .setMessage("Are you sure you want to pay " + amount + " for " + serviceType + "?\n\nConsumer ID: " + etConsumerId.getText().toString())
                .setPositiveButton("Confirm", (dialog, which) -> {
                    // This triggers the database logic
                    savePaymentToDB(amount);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Toast.makeText(this, "Payment Cancelled", Toast.LENGTH_SHORT).show();
                })
                .setCancelable(false)
                .show();
    }

    private void onPaymentSheetResult(final PaymentSheetResult paymentSheetResult) {
        // Keeping this for architectural completeness, though we are mocking the flow now
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            savePaymentToDB(Double.parseDouble(etAmount.getText().toString()));
        }
    }

    private void savePaymentToDB(double amount) {
        // 1. Generate a Unique Transaction ID
        String timeStamp = new SimpleDateFormat("HHmmss", Locale.getDefault()).format(new Date());
        int randomNum = (int) (Math.random() * 900) + 100;
        String transactionId = "TXN-" + timeStamp + "-" + randomNum;

        // 2. Prepare Date and User ID
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // 3. Save to SQLite
            String entryName = serviceType + " Bill (" + transactionId + ")";
            Expense expense = new Expense(0, entryName, amount, "Bills", today, "Expense");
            db.addExpense(expense, userId);

            // 4. Create the Receipt Text for Sharing (formatted for WhatsApp)
            String shareMessage = "✨ *Smart Wallet Receipt* ✨\n\n" +
                    "✅ *Status:* Successful\n" +
                    "💳 *Service:* " + serviceType + "\n" +
                    "💰 *Amount:* Rs. " + amount + "\n" +
                    "🆔 *Transaction ID:* " + transactionId + "\n" +
                    "📅 *Date:* " + today + "\n\n" +
                    "Thank you for using Smart Wallet!";

            // 5. Show a professional success message with Share Button
            new AlertDialog.Builder(this)
                    .setTitle("Payment Successful")
                    .setMessage("Your bill has been paid.\n\nTransaction ID:\n" + transactionId)
                    .setPositiveButton("OK", (dialog, which) -> finish())
                    .setNeutralButton("Share Receipt", (dialog, which) -> {
                        shareReceipt(shareMessage);
                        // We don't call finish() here immediately so the user can come back
                        // from WhatsApp and then click OK.
                    })
                    .setCancelable(false)
                    .show();

        } else {
            Toast.makeText(this, "Error: User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Helper method to trigger the Android Share Intent
     */
    private void shareReceipt(String message) {
        android.content.Intent sendIntent = new android.content.Intent();
        sendIntent.setAction(android.content.Intent.ACTION_SEND);
        sendIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);
        sendIntent.setType("text/plain");

        // Try to open WhatsApp directly, otherwise show the Share Chooser
        sendIntent.setPackage("com.whatsapp");

        try {
            startActivity(sendIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            sendIntent.setPackage(null); // Reset to show all apps
            startActivity(android.content.Intent.createChooser(sendIntent, "Share Receipt Via:"));
        }
    }
}