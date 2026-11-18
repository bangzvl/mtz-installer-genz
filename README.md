# MTZ Installer gen Z 🎨✌️

Aplikasi Android untuk mengimpor dan memasang tema **.mtz** Xiaomi (MIUI / HyperOS).
Gaya UI: dasar desain "Quiz" (latar krem, judul condensed tebal, kartu warna-warni, navigasi
melayang) dengan sentuhan gen Z: garis tepi hitam tegas + bayangan padat ala stiker
(`Modifier.neo()`), aksen lime & pink, stiker miring, dan teks berjalan di Beranda.

## Fitur
- **Onboarding**: judul besar, kolase mockup HP, confetti, dan tombol "Mulai"
- **Beranda**: kartu impor `.mtz`, carousel "Koleksi Terbaru 🔥", dan daftar "Terakhir Dipasang"
- **Koleksi**: podium 3 tema teratas (favorit/sering dipasang) dan daftar semua tema
- **Detail tema** (bottom sheet): pratinjau, versi, ukuran, deskripsi, Pasang, Favorit, Hapus
- **Jelajah**: browser MiuiThemez di dalam aplikasi yang otomatis mengimpor unduhan .mtz (tanpa setup)
- **Info**: status aplikasi Tema bawaan, cara kerja, dan catatan penting
- Membaca `description.xml` (judul, desainer, versi) dan gambar `preview/` dari dalam file .mtz
- File `.mtz` bisa dibuka langsung dari file manager (Buka dengan → MTZ Installer)

## Tab Jelajah (MiuiThemez)
Tab Jelajah langsung membuka
`https://www.miuithemez.com/search` di browser dalam aplikasi. Cari tema seperti biasa, buka
postingannya, lalu tekan tombol unduh (biasanya ke Mediafire). Begitu file `.mtz` mulai diunduh,
aplikasi menangkapnya, mengunduh dengan progres, memvalidasi isinya, lalu memasukkannya ke Koleksi
dan langsung membuka detail tema untuk dipasang.
Situs ditampilkan apa adanya (termasuk iklannya), jadi tidak ada scraping konten. Tema di sana
karya desainer masing-masing; hormati ketentuan situs dan desainernya.

## Cara kerja pemasangan
1. File `.mtz` disalin ke `Download/MtzInstaller/` lewat MediaStore (tanpa izin penyimpanan tambahan).
2. Aplikasi memanggil activity Tema bawaan:
   `com.android.thememanager/.ApplyThemeForScreenshot` dengan extra `theme_file_path`.
3. Jika activity itu diblokir/tidak ada, aplikasi Tema dibuka dan muncul panduan import manual.

## Build
1. Buka folder ini di **Android Studio** (Koala / Ladybug atau lebih baru).
2. Tunggu Gradle sync selesai, lalu **Run ▶** ke HP Xiaomi, atau
   **Build → Build APK(s)**. Bisa juga lewat terminal: `./gradlew assembleDebug`
3. APK ada di `app/build/outputs/apk/debug/app-debug.apk`.

Kebutuhan: JDK 17, Android SDK 35, minSdk 29 (Android 10+).

## Catatan
- Pemasangan otomatis hanya bekerja di perangkat yang punya `com.android.thememanager`.
- Beberapa versi HyperOS terbaru membatasi tema pihak ketiga; jika tema tidak diterapkan,
  gunakan versi aplikasi Tema yang lebih lama atau import manual dari folder Download/MtzInstaller.
- Font judul memakai `sans-serif-condensed` bawaan sistem. Untuk tampilan yang lebih mirip desain,
  taruh font seperti Anton/Bebas Neue di `res/font/` lalu ganti `Condensed` di `ui/theme/Theme.kt`.

## Struktur
```
app/src/main/java/com/mtzinstaller/app/
├── MainActivity.kt          # host, navigasi, picker file, dialog
├── MainViewModel.kt         # impor, pasang, favorit, hapus
├── data/
│   ├── MtzTheme.kt          # model + penyimpanan JSON
│   ├── MtzParser.kt         # baca isi .mtz (zip)
│   ├── CatalogRepository.kt # pengunduh file (progres, redirect, header)
│   └── ThemeInstaller.kt    # salin ke Download + panggil aplikasi Tema
└── ui/
    ├── theme/Theme.kt       # warna & tipografi
    ├── components/          # tombol pil, chip, nav bar, pratinjau, confetti
    └── screens/             # Onboarding, Home, Browse, MiuiThemezBrowser, Collection, Info, DetailSheet
```
