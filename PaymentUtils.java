package com.example.smartwallet;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

public class PaymentUtils {

    public static final String EASYPAISA_PACKAGE = "com.telco.unf.easypaisa";
    public static final String JAZZCASH_PACKAGE = "com.techlogix.mobilinkcustomer";

    public static void openPaymentApp(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            // Check if app is installed
            Intent intent = pm.getLaunchIntentForPackage(packageName);
            if (intent != null) {
                context.startActivity(intent);
            } else {
                // If not installed, redirect to Play Store
                context.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + packageName)));
            }
        } catch (Exception e) {
            Toast.makeText(context, "Could not open the app", Toast.LENGTH_SHORT).show();
        }
    }
}