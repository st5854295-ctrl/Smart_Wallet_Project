package com.example.smartwallet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DatabaseHelper db;
    private TextView tvTotalBalance;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private PieChart pieChart;
    private String currentUserId;
    private String selectedCurrency = "Rs.";
    private ExpenseAdapter adapter;
    private List<Expense> expenseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        selectedCurrency = prefs.getString("SelectedCurrency", "Rs.");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        currentUserId = user.getUid();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    finish();
                }
            }
        });

        tvTotalBalance = findViewById(R.id.tvTotalBalance);
        pieChart = findViewById(R.id.pieChart);
        FloatingActionButton fab = findViewById(R.id.fabAdd);

        // Search and Sort UI
        SearchView searchView = findViewById(R.id.searchView);
        ImageView btnSort = findViewById(R.id.btnSort);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) {
                    adapter.filterList(newText);
                }
                return true;
            }
        });

        btnSort.setOnClickListener(v -> showSortDialog());

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // RESTORED: Service Card Setup
        setupServiceCard(findViewById(R.id.cardRecharge), "Mobile", R.drawable.bill);
        setupServiceCard(findViewById(R.id.cardElectric), "Electricity", R.drawable.bill);
        setupServiceCard(findViewById(R.id.cardInternet), "Internet", R.drawable.bill);
        setupServiceCard(findViewById(R.id.cardWater), "Water", R.drawable.bill);

        updateNavHeader();
        loadData();

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
            startActivity(intent);
        });
    }

    private void showSortDialog() {
        final String[] options = {"Date (Newest First)", "Amount (High to Low)", "Amount (Low to High)"};

        new AlertDialog.Builder(this)
                .setTitle("Sort By")
                .setItems(options, (dialog, which) -> {
                    List<Expense> listToSort = new ArrayList<>(adapter.getCurrentList());
                    
                    switch (which) {
                        case 0: // Date
                            Collections.sort(listToSort, (o1, o2) -> o2.getDate().compareTo(o1.getDate()));
                            break;
                        case 1: // Amount High to Low
                            Collections.sort(listToSort, (o1, o2) -> Double.compare(o2.getAmount(), o1.getAmount()));
                            break;
                        case 2: // Amount Low to High
                            Collections.sort(listToSort, Comparator.comparingDouble(Expense::getAmount));
                            break;
                    }
                    adapter.updateList(listToSort);
                    Toast.makeText(this, "Sorted by " + options[which], Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_currency_selector) {
            showCurrencySelectorDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showCurrencySelectorDialog() {
        final String[] currencies = {"PKR (Rs.)", "USD ($)"};
        final String[] currencySymbols = {"Rs.", "$"};

        new AlertDialog.Builder(this)
                .setTitle("Select Currency")
                .setItems(currencies, (dialog, which) -> {
                    selectedCurrency = currencySymbols[which];
                    
                    SharedPreferences.Editor editor = getSharedPreferences("AppPrefs", MODE_PRIVATE).edit();
                    editor.putString("SelectedCurrency", selectedCurrency);
                    editor.apply();

                    updateTotalBalance();
                    loadData();
                    Toast.makeText(this, "Currency set to " + currencies[which], Toast.LENGTH_SHORT).show();
                })
                .show();
    }
    
    // --- RESTORED: Service Card and Payment Logic ---
    private void setupServiceCard(View view, String title, int iconRes) {
        ImageView icon = view.findViewById(R.id.imgServiceIcon);
        TextView text = view.findViewById(R.id.tvServiceName);
        icon.setImageResource(iconRes);
        icon.clearColorFilter();
        text.setText(title);

        view.setOnClickListener(v -> {
            Log.d("MainActivity", "Service Clicked: " + title);
            showPaymentOptions(title);
        });
    }

    private void showPaymentOptions(String serviceType) {
        final CharSequence[] options = {"EasyPaisa", "JazzCash", "Card / Manual Entry", "Share Bill (WhatsApp)"};

        new AlertDialog.Builder(this)
                .setTitle("Select Payment Method")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // EasyPaisa
                            openAppByPackage("com.telco.unf.easypaisa");
                            break;
                        case 1: // JazzCash
                            openAppByPackage("com.techlogix.mobilinkcustomer");
                            break;
                        case 2: // Manual / Stripe
                            Intent intent = new Intent(MainActivity.this, ServicePaymentActivity.class);
                            intent.putExtra("SERVICE_TYPE", serviceType);
                            startActivity(intent);
                            break;
                        case 3: // Share via WhatsApp
                            shareToWhatsApp("Hey, please pay the " + serviceType + " bill.");
                            break;
                    }
                })
                .show();
    }

    private void openAppByPackage(String pkg) {
        Toast.makeText(this, "Launching app...", Toast.LENGTH_SHORT).show();
        PackageManager pm = getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage(pkg);
        if (launchIntent != null) {
            startActivity(launchIntent);
        } else {
            Toast.makeText(this, "App not installed", Toast.LENGTH_SHORT).show();
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pkg)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + pkg)));
            }
        }
    }

    private void shareToWhatsApp(String message) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.setPackage("com.whatsapp");
        intent.putExtra(Intent.EXTRA_TEXT, message);
        try {
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "WhatsApp not installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateNavHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView tvNavEmail = headerView.findViewById(R.id.tvNavEmail);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            tvNavEmail.setText(user.getEmail());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        expenseList = db.getAllExpenses(currentUserId);
        adapter = new ExpenseAdapter(expenseList, this, db, this::updateTotalBalance, selectedCurrency);
        ((RecyclerView)findViewById(R.id.recyclerView)).setAdapter(adapter);

        updateTotalBalance();
        setupPieChart(expenseList);
    }

    private void updateTotalBalance() {
        double total = db.getTotalBalance(currentUserId);
        tvTotalBalance.setText(String.format(Locale.getDefault(), "%s%.2f", selectedCurrency, total));
    }

    private void setupPieChart(List<Expense> list) {
        Map<String, Float> categoryMap = new HashMap<>();
        for (Expense expense : list) {
            if ("Expense".equals(expense.getType())) {
                String category = expense.getCategory();
                float amount = (float) expense.getAmount();
                Float currentTotal = categoryMap.get(category);
                categoryMap.put(category, (currentTotal == null ? 0 : currentTotal) + amount);
            }
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : categoryMap.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Expenses by Category");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Expenses");
        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Already on Home
        } else if (id == R.id.nav_goals) {
            Intent intent = new Intent(this, GoalActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_report) {
            List<Expense> list = db.getAllExpenses(currentUserId);
            double totalBalance = db.getTotalBalance(currentUserId);
            PdfGenerator.generateExpenseReport(this, list, totalBalance, selectedCurrency);
        } else if (id == R.id.nav_theme) {
            int currentMode = AppCompatDelegate.getDefaultNightMode();
            if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        } else if (id == R.id.nav_settings) {
             Intent intent = new Intent(this, ProfileActivity.class); 
             startActivity(intent);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}