# GitHub'da Tek Tik APK Derleme

Bu surum GitHub Actions'ta yaygin ve kararlı Android SDK paketlerini kullanir:

- JDK 17
- Android compileSdk / targetSdk 35
- Android Build Tools 34.0.0 (AGP 8.7 varsayilani)
- Android Gradle Plugin 8.7.3
- Gradle 8.9 (GitHub Actions tarafindan kurulur)
- Kotlin 2.0.21

## Derleme

1. Bu klasorun **icindekileri** GitHub repository ana dizinine yukleyin.
2. GitHub'da **Actions** sekmesini acin.
3. **Android APK Build - Tek Tik** workflow'unu secin.
4. **Run workflow** > **Run workflow** deyin.
5. Islem yesil tamamlandiginda calismanin altindaki **Artifacts** bolumunden **Isim-Analiz-APK** paketini indirin.
6. ZIP icindeki `Isim-Analiz.apk` telefona kurulabilir debug APK'dir.

> Not: Onceki workflow'daki `platforms;android-37` paketi GitHub SDK deposunda bulunamadigi icin SDK kurulumunda hata veriyordu. Bu proje API 35'e sabitlenmistir.
