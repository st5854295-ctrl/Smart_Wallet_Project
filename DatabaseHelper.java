package com.example.smartwallet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SmartWallet.db";
    private static final int DATABASE_VERSION = 8;

    // Expenses Table
    private static final String TABLE_EXPENSES = "expenses";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_TYPE = "type";

    // Goals Table
    private static final String TABLE_GOALS = "goals";
    private static final String COLUMN_GOAL_ID = "id";
    private static final String COLUMN_GOAL_NAME = "name";
    private static final String COLUMN_GOAL_TARGET = "target";
    private static final String COLUMN_GOAL_SAVED = "saved";
    private static final String COLUMN_GOAL_DATE = "date";
    private static final String COLUMN_GOAL_ICON = "icon";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createExpensesTable = "CREATE TABLE " + TABLE_EXPENSES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_ID + " TEXT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_AMOUNT + " REAL, " +
                COLUMN_CATEGORY + " TEXT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_TYPE + " TEXT)";
        db.execSQL(createExpensesTable);

        String createGoalsTable = "CREATE TABLE " + TABLE_GOALS + " (" +
                COLUMN_GOAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_ID + " TEXT, " +
                COLUMN_GOAL_NAME + " TEXT, " +
                COLUMN_GOAL_TARGET + " REAL, " +
                COLUMN_GOAL_SAVED + " REAL, " +
                COLUMN_GOAL_DATE + " TEXT, " +
                COLUMN_GOAL_ICON + " TEXT)";
        db.execSQL(createGoalsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GOALS);
        onCreate(db);
    }

    // --- USER-AWARE EXPENSE METHODS ---
    public void addExpense(Expense expense, String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_TITLE, expense.getTitle());
        values.put(COLUMN_AMOUNT, expense.getAmount());
        values.put(COLUMN_CATEGORY, expense.getCategory());
        values.put(COLUMN_DATE, expense.getDate());
        values.put(COLUMN_TYPE, expense.getType());
        db.insert(TABLE_EXPENSES, null, values);
        db.close();
    }

    public List<Expense> getAllExpenses(String userId) {
        List<Expense> expenseList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_EXPENSES + " WHERE " + COLUMN_USER_ID + " = ? ORDER BY " + COLUMN_ID + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{userId});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                String type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE));
                expenseList.add(new Expense(id, title, amount, category, date, type));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return expenseList;
    }

    public void deleteExpense(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("expenses", "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public double getTotalBalance(String userId) {
        double totalIncome = 0;
        double totalExpense = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        
        String[] userIdArg = {userId};
        Cursor cursorIncome = db.rawQuery("SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_EXPENSES + " WHERE " + COLUMN_TYPE + " = 'Income' AND " + COLUMN_USER_ID + " = ?", userIdArg);
        if (cursorIncome.moveToFirst()) {
            totalIncome = cursorIncome.getDouble(0);
        }
        cursorIncome.close();

        Cursor cursorExpense = db.rawQuery("SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_EXPENSES + " WHERE " + COLUMN_TYPE + " = 'Expense' AND " + COLUMN_USER_ID + " = ?", userIdArg);
        if (cursorExpense.moveToFirst()) {
            totalExpense = cursorExpense.getDouble(0);
        }
        cursorExpense.close();
        return totalIncome - totalExpense;
    }

    // --- USER-AWARE GOALS METHODS ---
    public void addGoal(String name, double target, double saved, String date, String icon, String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_GOAL_NAME, name);
        values.put(COLUMN_GOAL_TARGET, target);
        values.put(COLUMN_GOAL_SAVED, saved);
        values.put(COLUMN_GOAL_DATE, date);
        values.put(COLUMN_GOAL_ICON, icon);
        db.insert(TABLE_GOALS, null, values);
        db.close();
    }

    public List<Goal> getAllGoals(String userId) {
        List<Goal> goalList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_GOALS + " WHERE " + COLUMN_USER_ID + " = ? ORDER BY " + COLUMN_GOAL_ID + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{userId});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_GOAL_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GOAL_NAME));
                double target = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_GOAL_TARGET));
                double saved = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_GOAL_SAVED));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GOAL_DATE));
                String icon = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GOAL_ICON));
                goalList.add(new Goal(id, name, target, saved, date, icon));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return goalList;
    }

    // New methods for updating and deleting goals
    public void updateGoal(int goalId, double newSavedAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GOAL_SAVED, newSavedAmount);
        db.update(TABLE_GOALS, values, COLUMN_GOAL_ID + " = ?", new String[]{String.valueOf(goalId)});
        db.close();
    }

    public void deleteGoal(int goalId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_GOALS, COLUMN_GOAL_ID + " = ?", new String[]{String.valueOf(goalId)});
        db.close();
    }
}