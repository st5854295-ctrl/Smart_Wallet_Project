Smart Wallet - Android Management System
Smart Wallet is a professional financial ledger application built for Android. It allows users to manage utility bills, track expenses, and visualize spending patterns through interactive charts. The app is integrated with major payment platforms like Easypaisa and JazzCash via deep-linking.

🚀 Features
User Authentication: Secure Login/Signup using Firebase Authentication.

Utility Bill Integration: Quick access and deep-linking to Easypaisa and JazzCash for Electricity, Water, Internet, and Mobile bills.

Expense Tracking: Full CRUD (Create, Read, Update, Delete) operations using a local SQLite database.

Data Visualization: Real-time spending analysis using a Pie Chart (MPAndroidChart).

Advanced Search: Real-time filtering of transactions by name or category.

PDF Reports: Generate and export professional monthly transaction summaries as PDF files.

Social Sharing: Share formatted transaction receipts directly to WhatsApp.

🛠️ Tech Stack
Language: Java

Local Database: SQLite

Backend: Firebase (Auth)

UI Framework: Material Design

Libraries:

MPAndroidChart for data visualization.

iText / PdfDocument for report generation.

📂 Project Structure
Plaintext

app/src/main/java/com/yourpackage/smartwallet/
├── MainActivity.java           # Dashboard logic & Search implementation
├── ServicePaymentActivity.java  # Bill payment logic & Intent handling
├── DatabaseHelper.java         # SQLite database management
├── ExpenseAdapter.java         # RecyclerView adapter with Search Filter
├── Expense.java                # Data Model for transactions
└── PdfGenerator.java           # Logic for PDF creation and export
⚙️ Installation
Clone the Repository:

Bash

git clone https://github.com/yourusername/Smart-Wallet-Android.git
Firebase Setup:

Create a project in Firebase Console.

Add an Android App and download google-services.json.

Place google-services.json in the app/ directory.

Build:

Open the project in Android Studio.

Sync Gradle and click Run.

📊 How to Use
Login/Register: Use your email to create a secure account.

Pay Bills: Click on a service (e.g., Electricity). The app will generate a Transaction ID and ask to launch your preferred payment app.

Search: Use the search bar on the home screen to find specific transactions by typing the category or service name.

Export: Click the "Report" icon to generate a PDF of all your monthly transactions.

🤝 Contributing
