# Keuanganku - Catatan Keuangan Pribadi 💰

Aplikasi pencatat keuangan sederhana namun profesional yang dibangun menggunakan Android Studio dengan bahasa pemrograman Kotlin. Aplikasi ini dirancang untuk membantu pengguna mengelola pemasukan dan pengeluaran secara real-time.

## ✨ Fitur Utama
- **Autentikasi Pengguna**: Login dan Register dengan Firebase Auth.
- **Pemisahan Data**: Setiap pengguna hanya dapat melihat dan mengelola transaksi milik sendiri.
- **Ringkasan Saldo**: Lihat Total Saldo, Pemasukan, dan Pengeluaran secara otomatis di dashboard.
- **Manajemen Transaksi**: Tambah, Edit, dan Hapus transaksi (Pemasukan/Pengeluaran).
- **Real-time Database**: Sinkronisasi instan menggunakan Firebase Firestore.
- **Tampilan Minimalis**: Desain bersih dengan skema warna hijau profesional.
- **Internet Check**: Mendeteksi dan memperingatkan jika tidak ada koneksi internet.

## 🛠️ Tech Stack
- **Bahasa**: [Kotlin](https://kotlinlang.org/)
- **Arsitektur**: MVVM (Model-View-ViewModel)
- **Database**: [Firebase Firestore](https://firebase.google.com/products/firestore)
- **Auth**: [Firebase Authentication](https://firebase.google.com/products/auth)
- **UI Framework**: XML Layout (ViewBinding)

## 🚀 Cara Pemasangan (Setup)

### 1. Prasyarat
- Android Studio Koala atau yang lebih baru.
- Proyek di [Firebase Console](https://console.firebase.google.com/).

### 2. Konfigurasi Firebase
1. Tambahkan aplikasi Android dengan package name: `com.example.financetracker`.
2. Download `google-services.json` dan letakkan di folder `app/`.
3. Aktifkan **Email/Password** di Firebase Authentication.
4. Aktifkan **Cloud Firestore** dan atur **Rules** sebagai berikut:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Rules untuk Transaksi
    match /transactions/{id} {
      allow read, write: if request.auth != null && request.auth.uid == (resource == null ? request.auth.uid : resource.data.userId);
      allow create: if request.auth != null;
    }
    
    // Rules untuk User Profile
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

### 3. Menjalankan Aplikasi
1. Lakukan **Gradle Sync** di Android Studio.
2. Jalankan aplikasi di Emulator atau Perangkat Fisik.

## 📂 Struktur Folder Utama
- `ui`: Aktivitas (Login, Register, Main) dan Adapter.
- `data/model`: Data class `User` dan `Transaction`.
- `data/repository`: Interaksi database Firebase.
- `viewmodel`: Logika bisnis aplikasi.
- `util`: Helper koneksi internet.

---
