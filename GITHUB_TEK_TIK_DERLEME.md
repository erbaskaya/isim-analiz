# GitHub'da Tek Tikla APK Derleme

Bu proje GitHub Actions ile bilgisayarda Android Studio/SDK kurmadan APK uretmek icin hazirdir.

## Ilk kurulum

1. GitHub'da bos bir repository acin.
2. Bu ZIP'in **icindeki dosyalari** repository'nin kokune yukleyin. `build.gradle.kts`, `settings.gradle.kts`, `app` ve `.github` klasorleri repository kokunde gorunmelidir.
3. GitHub'da repository icinde **Actions** sekmesine girin.
4. Soldan **Android APK Build - Tek Tik** workflow'unu acin.
5. **Run workflow** > **Run workflow** dugmesine basin.

## APK'yi indirme

Derleme yesil tik olduktan sonra ayni calisma sayfasinin en altindaki **Artifacts** bolumunde **Isim-Analiz-APK** gorunur.

Paketi indirip acin:

- `Isim-Analiz.apk` -> telefona kurulacak dosya
- `Isim-Analiz.apk.sha256` -> dosya butunluk kontrolu

## GitHub'un kurdugu ortam

Workflow otomatik olarak sunlari kurar:

- JDK 17
- Android API 37
- Android Build Tools 36.0.0
- Gradle 9.6.0
- Android Gradle Plugin 9.4.0

Bu surumler AGP 9.4.0 resmi uyumluluk tablosuna gore eslestirilmistir.

## Not

Tek tik derleme ek ayar gerektirmesin diye `assembleDebug` kullanilir. Uretilen APK Android tarafindan debug anahtariyla imzalanir ve test/kurulum icin uygundur. Google Play'e yayinlanacak kalici release surumunde ayni imza anahtarinin korunmasi gerekir; bunun icin daha sonra GitHub Secrets ile release signing eklenebilir.
