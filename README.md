# Keuanganku - Catatan Keuangan Pribadi 💰

Aplikasi pencatat keuangan profesional yang dibangun menggunakan Android Studio dengan Kotlin. Membantu pengguna mengelola keuangan dengan fitur cerdas seperti analisis pengeluaran harian dan mingguan secara real-time.

## ✨ Fitur Utama
- **Autentikasi Pengguna**: Login & Register aman menggunakan Firebase Authentication.
- **Data Terisolasi**: Setiap pengguna memiliki penyimpanan data pribadi yang terpisah.
- **Dashboard Interaktif**: Ringkasan saldo, pemasukan, dan pengeluaran yang intuitif.
- **Smart Analytics**:
  - **Analisis Harian**: Peringatan otomatis "Boros" jika pengeluaran harian melebihi Rp 500.000.
  - **Analisis Pekanan**: Perbandingan tren pengeluaran minggu ini vs minggu lalu.
  - **Pola Pengeluaran**: Grafik persentase pengeluaran berdasarkan kategori.
- **Real-time Sync**: Database sinkron instan menggunakan Firebase Firestore.
- **Keamanan Lanjutan**: Proteksi level database dengan Rules Firestore.
- **UI Modern**: Desain minimalis dengan tema Hijau-Putih yang bersih dan profesional.
- **Cek Koneksi**: Deteksi otomatis dan peringatan jika internet terputus.

## 🛠️ Tech Stack
- **Bahasa**: [Kotlin](https://kotlinlang.org/)
- **Arsitektur**: MVVM (Model-View-ViewModel) dengan Fragments.
- **Backend**: Firebase (Auth & Firestore Database).
- **UI**: XML Layout, Material Design 3, ViewBinding.
- **Asynchrony**: Kotlin Coroutines & Flow.

## 🚀 Panduan Setup

### 1. Konfigurasi Firebase
1. Buat proyek di [Firebase Console](https://console.firebase.google.com/).
2. Daftarkan aplikasi Android (`com.example.financetracker`).
3. Download `google-services.json` ke folder `app/`.
4. Aktifkan **Email/Password Auth** dan **Cloud Firestore**.
5. Salin **Security Rules** berikut ke tab Rules Firestore:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /transactions/{id} {
      allow read, write: if request.auth != null && request.auth.uid == (resource == null ? request.auth.uid : resource.data.userId);
      allow create: if request.auth != null;
    }
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

### 2. Menjalankan Aplikasi
1. Clone repository ini.
2. Buka di Android Studio.
3. Tunggu Gradle Sync selesai.
4. Klik **Run** pada Emulator atau Device fisik.

## 📂 Struktur Project
- `ui`: Aktivitas Utama, Fragments (Dashboard, Insights, Profile), dan Adapters.
- `data`: Model data (`Transaction`, `User`) dan `FirebaseRepository`.
- `viewmodel`: Logika bisnis dan pemrosesan data analisis.
- `util`: Utility untuk koneksi internet.

---
