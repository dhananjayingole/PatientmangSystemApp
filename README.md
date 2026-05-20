# 🏥 Patient Management System — Android App

A full-featured healthcare management Android application built with **Jetpack Compose**, **Firebase Realtime Database**, and **Firebase Authentication**. The app supports two roles — **Admin (Doctor/Staff)** and **Patient (User)** — with real-time data sync, appointment booking, prescription management, and emergency SOS alerts.

## ✨ Features

### 👨‍⚕️ Admin Panel
- **Dashboard** — live stats: total patients, appointments today, pending emergency alerts
- **Patient Management** — add, edit, delete patient records with full medical profile
- **Prescription Management** — create and update prescriptions with multiple medicines (name, dosage, frequency, duration, timing)
- **Appointment Management** — view all appointments, confirm, cancel, or mark as completed with optional notes
- **Emergency Alerts** — view incoming SOS alerts with GPS location, update response status (responding / resolved)
- **User Management** — view all registered users, delete accounts
- **Auto account linking** — when a patient is added via the appointment flow, their Firebase Auth UID is silently linked to the patient record so prescriptions sync automatically to their app

### 👤 Patient (User) Panel
- **Dashboard** — upcoming appointments, recent prescriptions, quick actions
- **Book Appointment** — select doctor, specialty, date, time, and reason
- **My Appointments** — view all past and upcoming appointments with status
- **My Prescriptions** — view prescriptions issued by doctors, including all medicine details
- **Notifications** — real-time push notifications for appointment confirmations, prescription updates, and emergency responses
- **Emergency SOS** — one-tap alert with live GPS location sent to admin
- **Yoga & Exercises** — browse yoga poses with detail screens
- **Profile** — update name and phone number

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM (ViewModel + StateFlow) |
| Navigation | Jetpack Navigation Compose |
| Backend / Database | Firebase Realtime Database |
| Authentication | Firebase Authentication |
| Location | Google Play Services — FusedLocationProviderClient |
| Async | Kotlin Coroutines + `kotlinx.coroutines.tasks.await` |
| State Management | `StateFlow` / `collectAsState` |

---

## 🗂️ Project Structure

```
app/
└── src/main/java/eu/tutorials/patientmanagementapp/
    ├── MainActivity.kt
    ├── Model/
    │   ├── User.kt
    │   ├── Patient.kt
    │   ├── Prescription.kt
    │   ├── Medicine.kt
    │   ├── Appointment.kt
    │   ├── Notification.kt
    │   └── EmergencyAlert.kt
    ├── Navigation/
    │   ├── NavGraphSetup.kt
    │   └── Routes.kt
    ├── viewmodels/
    │   ├── AdminViewModel.kt
    │   └── UserViewModel.kt
    ├── auth/
    │   ├── AuthScreen.kt
    │   ├── AuthViewModel.kt
    │   └── AuthViewModelFactory.kt
    ├── admin/
    │   ├── AdminDashboard.kt
    │   ├── AdminPatientsScreen.kt
    │   ├── AdminAddEditPatientScreen.kt
    │   ├── AdminPrescriptionsScreen.kt
    │   ├── AdminAddEditPrescriptionScreen.kt
    │   ├── AdminAppointmentsScreen.kt
    │   ├── AdminEmergencyAlertsScreen.kt
    │   ├── AdminUsersScreen.kt
    │   └── AdminProfileScreen.kt
    └── user/
        ├── UserDashboard.kt
        ├── UserPrescriptionsScreen.kt
        ├── MyAppointmentsScreen.kt
        ├── BookAppointmentScreen.kt
        ├── NotificationsScreen.kt
        ├── EmergencyScreen.kt
        ├── YogaExercisesScreen.kt
        ├── YogaDetailScreen.kt
        └── UserProfileScreen.kt
```

---

## 🔥 Firebase Database Structure

```
root/
├── users/
│   └── {uid}/
│       ├── name
│       ├── email
│       └── phoneNumber
│
├── patients/
│   └── {patientId}/
│       ├── id
│       ├── name
│       ├── age
│       ├── gender
│       ├── bloodGroup
│       ├── phoneNumber
│       ├── address
│       ├── emergencyContact
│       ├── assignedDoctor
│       ├── medicalHistory
│       └── userId          ← Firebase Auth UID (links to user account)
│
├── prescriptions/
│   └── {prescriptionId}/
│       ├── id
│       ├── patientId
│       ├── patientName
│       ├── doctorName
│       ├── diagnosis
│       ├── notes
│       ├── date
│       ├── userId          ← copied from patient, used for user-side queries
│       └── medicines/
│           └── []/
│               ├── name
│               ├── dosage
│               ├── frequency
│               ├── duration
│               └── timing
│
├── appointments/
│   └── {appointmentId}/
│       ├── id
│       ├── userId          ← Firebase Auth UID of the patient user
│       ├── userName
│       ├── doctorName
│       ├── doctorSpecialty
│       ├── date
│       ├── time
│       ├── reason
│       ├── status          ← pending | confirmed | cancelled | completed
│       └── notes
│
├── notifications/
│   └── {notificationId}/
│       ├── id
│       ├── userId
│       ├── title
│       ├── message
│       ├── type            ← appointment | prescription | emergency
│       ├── isRead
│       └── timestamp
│
└── emergency_alerts/
    └── {alertId}/
        ├── id
        ├── userId
        ├── userName
        ├── userPhone
        ├── latitude
        ├── longitude
        ├── address
        ├── status          ← pending | responding | resolved
        ├── respondedBy
        ├── responseTime
        └── timestamp
```

---

## 🔗 Patient ↔ User Account Linking Flow

This is a key design decision that makes prescriptions automatically appear on the patient's phone:

```
1. Patient registers / books appointment via the app
   └── Appointment saved with userId = Firebase Auth UID

2. Admin confirms appointment

3. When patient arrives, admin taps "Add as Patient" on the appointment card
   └── AdminAddEditPatientScreen opens with:
       - prefillName  = appointment.userName
       - prefillUserId = appointment.userId   ← silently carried

4. Admin fills in age, blood group, etc. and saves
   └── Patient record saved with userId = the linked UID

5. Admin creates a prescription for that patient
   └── AdminViewModel.savePrescription() copies userId from the patient record
       into the prescription automatically

6. Prescription now visible on the patient's "My Prescriptions" screen
   └── UserViewModel queries prescriptions WHERE userId == currentUser.uid
```

No manual UID entry required at any step.

---

## ⚙️ Setup & Installation

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 17
- A Firebase project

### Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/dhananjayingole/PatientmangSystemApp.git
   cd PatientmangSystemApp
   ```

2. **Create a Firebase project**
   - Go to [Firebase Console](https://console.firebase.google.com)
   - Create a new project
   - Add an Android app with package name `eu.tutorials.patientmanagementapp`
   - Download `google-services.json` and place it in the `app/` folder

3. **Enable Firebase services**
   - Authentication → Email/Password sign-in
   - Realtime Database → Start in test mode (then add rules below)

4. **Set Firebase Realtime Database rules**
   ```json
   {
     "rules": {
       ".read": "auth != null",
       ".write": "auth != null"
     }
   }
   ```

5. **Set admin role**
   In Firebase Realtime Database, manually set the role for your admin account:
   ```
   users/{adminUid}/role = "admin"
   ```
   Regular users have `role = "user"` (set automatically on registration).

6. **Build and run**
   Open in Android Studio → Sync Gradle → Run on device or emulator (API 26+)

---

## 🔑 User Roles

| Role | How to set | Access |
|---|---|---|
| `admin` | Manually set `role: "admin"` in Firebase for that user's UID | Full admin dashboard |
| `user` | Set automatically on registration | Patient dashboard |

On login, the app reads the role from Firebase and routes to the correct dashboard automatically.

---

## 📦 Key Dependencies

```kotlin
// Jetpack Compose
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.navigation:navigation-compose")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose")

// Firebase
implementation("com.google.firebase:firebase-auth-ktx")
implementation("com.google.firebase:firebase-database-ktx")

// Location
implementation("com.google.android.gms:play-services-location")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services")
```

---

## 🐛 Known Issues Fixed

- **Patients not showing in prescription screen** — caused by each screen creating its own `AdminViewModel` instance via `= viewModel()` default. Fixed by creating one shared instance in `NavGraphSetup` and passing it explicitly to all admin screens.
- **Auth race condition on cold start** — users were landing on the login screen briefly even when already authenticated because `userRole` was `null` while Firebase was still loading. Fixed by showing a `CircularProgressIndicator` while `isAuthenticated == true` but `userRole == null`.
- **Prescriptions not appearing for patients** — fixed by ensuring `userId` is copied from the patient record into every prescription at save time inside `AdminViewModel.savePrescription()`.

---

## 🤝 Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you'd like to change.

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

---

## 👨‍💻 Author

**Dhananjay Ingole**
- GitHub: [@dhananjayingole](https://github.com/dhananjayingole)

---

> Built with ❤️ using Jetpack Compose + Firebase
