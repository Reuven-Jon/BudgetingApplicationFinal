# 💰 Spend Cents – Gamify Your Finances

**Spend Cents** is an Android finance tracking application that makes managing money fun and interactive. Built in Android Studio and integrated with Firebase, 
this app allows users to record income and expenses, visualize their financial trends, and track their savings progress using a board game–themed interface. 
It’s designed to motivate users through gamified financial tracking while providing essential budgeting tools.

---

## 🧩 Features

### 1. 💵 Income Page
- View a list of all income entries.
- Income entries include amount, date, and description.
- Stored securely in Firebase Firestore.

### 2. 🧾 Expense Page
- Displays all expenses with date, amount, and category.
- View uploaded images (receipts or proof) for each entry.

### 3. ➕ Enter Income
- Users can add new income entries with date and description.
- Data is saved to the Firebase Firestore under the logged-in user.

### 4. ➖ Enter Expense with Picture Upload
- Users can log new expenses by category.
- Optional image upload for receipts or related documents.
- Images are stored in Firebase Storage; references are saved in Firestore.

---

## 🎮 Board Game Page (Core Visual & Interactive Feature)

The **boardgame page** is the heart of the application, turning savings tracking into an engaging journey:

- A **gameboard-style interface** displays the user's progress visually.
- Each tile on the board represents a step toward the user's **monthly savings goal**.
- The player is motivated to continue tracking their finances to reach the final tile.

#### 🧍 Custom Feature 1: Moving Game Piece
- A **player piece** moves across the board based on the user's progress toward their **savings goal**.
- The more the user saves, the further the piece moves — a fun, visual indicator of success.

#### 🎯 Custom Feature 2: Goal Tracking & Feedback
- **Minimum and maximum spending goals** are set by the user.
- The board game provides **live feedback** based on these goals:
  - 0% Minimum Goal
  - 100% Maximum Goal
- This replaces traditional boring charts with a **motivational, gamified experience**.

---

## 📈 Graphs & Charts Page

Users can view graphical insights into their financial habits:

### 📊 Category-Based Expense Chart
- View expenses broken down by category (e.g. food, transport, bills).
- Helps users identify spending patterns and areas to cut back.

### 📅 Custom Time Range Selection
- Users can select a start and end date to filter expenses shown in the graph.

### 🚦 Goal Overlay
- The graph displays user-defined **minimum and maximum spending thresholds**.
- Users can visually assess if spending is within their target range.

---

## 🔐 Firebase Integration

- **Firebase Authentication**: Each user logs in securely using email/password.
- **Firebase Firestore**: Stores all income, expenses, goals, and user progress.
- **Firebase Storage**: Handles uploaded images for expenses (e.g. receipts).
  
## 🚀 Getting Started

Follow these simple steps to download the code from GitHub and run it in Android Studio:

1. **Clone the repository**  
   Open your terminal (or Git Bash) and run:  
   ```bash
(https://github.com/Reuven-Jon/BudgetingApplicationFinal.git)

2. Open in Android Studio

  Launch Android Studio.

3. Select File → Open (or Open an existing project).

4. Navigate to the Spend-Cents folder you just cloned and click OK.

5. Let Gradle sync
   Android Studio will automatically download all dependencies.
   If you see a prompt to “Update Gradle” or “Accept Licenses,” click OK or Accept.

6. Configure Firebase

7. Download your google-services.json from the Firebase console.

8. Place it in your project at:

css
Copy
Edit
app/src/main/
Run the app

9. Connect an Android device via USB (with USB debugging enabled) or start an Android emulator.

10. Click the green Run ▶️ button in the toolbar.

11. Choose your target device and press OK. (Preferably Medium Phone API 36 or 35)

12. You should now see Spend Cents launch on your device/emulator. 🎉


### 🔧 Firestore Data Structure (Simplified)

```plaintext
users
└── UID
    ├── minGoal: 1000
    ├── maxGoal: 2000
    ├── income (subcollection)
    │   └── incomeID
    │       ├── amount: 1500
    │       ├── date: Timestamp
    │       └── description: "Salary"
    ├── expenses (subcollection)
    │   └── expenseID
    │       ├── amount: 250
    │       ├── category: "Food"
    │       ├── date: Timestamp
    │       └── imageUrl: "https://firebase..." 



