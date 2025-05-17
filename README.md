# 🚀 TaskBoard Pro

A real-time collaborative task management Android app built with **Kotlin**, **Jetpack Compose**, and **Firebase**.

---

## 📱 Features

- 🔐 **Firebase Authentication**
  - Email/password signup and login
  - Google Sign-In support

- 📁 **Projects**
  - Create projects with teammates
  - Invite members by email (via `mailto:` link)
  - Team-specific project visibility

- 📋 **Task Board**
  - Add tasks with due date, assignee, and description
  - Kanban-style status: `To Do`, `In Progress`, `Done`
  - Move tasks between statuses dynamically

- 💬 **Task Comments**
  - Real-time discussion section under every task
  - Comment posting and display

- 🏅 **Badges & Profile**
  - Earn badges for completed tasks
  - Profile screen showing personal stats

- 🏆 **Leaderboard**
  - Displays users ranked by badge count
  - Dynamic UI with gold/silver/bronze styling

- 🎨 **Colorful Material Design UI**
  - Responsive and clean layout with Jetpack Compose

---

## 🛠️ Tech Stack

| Tech            | Purpose                          |
|-----------------|----------------------------------|
| Kotlin + Compose| UI development (modern Android)  |
| Firebase Auth   | Authentication (email + Google)  |
| Firebase Realtime DB | Real-time storage & sync      |
| Material3       | UI components and layout styling |
| Jetpack Libraries| State, navigation, composables  |

---

## 🧑‍💻 Setup Instructions

1. Clone the repository:
```bash
git clone https://github.com/yourusername/TaskBoardPro.git
Open in Android Studio

2.Add your google-services.json file to the /app directory

3.Enable:

Firebase Auth (Email & Google)
Firebase Realtime Database

4.Add your SHA-1 key to Firebase project settings

5.Run the project on emulator or device 🎯
