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
