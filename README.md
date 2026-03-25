# Finance Tracker - Catatan Keuangan Pribadi 💰

Aplikasi pencatat keuangan sederhana namun profesional yang dibangun menggunakan Android Studio dengan bahasa pemrograman Kotlin. Aplikasi ini dirancang untuk membantu pengguna mengelola pemasukan dan pengeluaran secara real-time.

## ✨ Fitur Utama
- **Ringkasan Saldo**: Lihat Total Saldo, Pemasukan, dan Pengeluaran secara otomatis di dashboard.
- **Manajemen Transaksi**:
  - Tambah transaksi (Pemasukan/Pengeluaran).
  - Edit transaksi yang sudah ada.
  - Hapus transaksi langsung dari daftar.
- **Real-time Database**: Sinkronisasi instan menggunakan Firebase Firestore.
- **Tampilan Minimalis**: Desain bersih dengan skema warna hijau profesional.
- **Format Mata Uang**: Otomatis dikonversi ke format Rupiah (IDR).
- **Empty State**: Tampilan informatif jika belum ada data transaksi.

## 🛠️ Tech Stack
- **Bahasa**: [Kotlin](https://kotlinlang.org/)
- **Arsitektur**: MVVM (Model-View-ViewModel)
- **Database**: [Firebase Firestore](https://firebase.google.com/products/firestore)
- **UI Framework**: XML Layout (ViewBinding)
- **Library Utama**:
  - Firebase BoM (Firestore)
  - ViewModel & Lifecycle KTX
  - Coroutines Android
  - Material Components (CardView, FAB, Google Fonts)

## 🚀 Cara Pemasangan (Setup)

### 1. Prasyarat
- Android Studio versi Koala atau yang lebih baru.
- Akun Google untuk mengakses Firebase Console.

### 2. Konfigurasi Firebase
Aplikasi ini memerlukan koneksi ke Firebase. Ikuti langkah berikut:
1. Buat proyek baru di [Firebase Console](https://console.firebase.google.com/).
2. Tambahkan aplikasi Android dengan package name: `com.example.financetracker`.
3. Download file `google-services.json` dan letakkan di folder `app/`.
4. Aktifkan **Cloud Firestore** di menu Build.
5. Setel aturan (Rules) Firestore ke mode testing atau publik selama pengembangan:
   ```javascript
   allow read, write: if true;
   ```

### 3. Menjalankan Aplikasi
1. Clone atau buka project ini di Android Studio.
2. Lakukan **Gradle Sync**.
3. Jalankan aplikasi di Emulator atau Perangkat Fisik.

## 📂 Struktur Folder
- `data/model`: Definisi data class `Transaction`.
- `data/repository`: Logika interaksi dengan Firebase Firestore.
- `viewmodel`: Pengolahan data dan logika bisnis aplikasi.
- `ui`: Aktivitas utama dan Adapter untuk RecyclerView.
- `res/layout`: Desain antarmuka (XML).

## 📝 Catatan
Aplikasi ini menggunakan **Firebase BoM** untuk memastikan semua library Firebase berjalan pada versi yang kompatibel tanpa konflik.

---
