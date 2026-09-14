# İnternet veri kaynakları

## Kur'an / Meal

Uygulama çalışma kitabındaki 6236 satırlık Kur'an verisini APK içine taşımak yerine yalnız gereken sûreyi ağ üzerinden alır.

- API: https://alquran.cloud/api
- Arapça: `quran-uthmani`
- Türkçe: `tr.diyanet`
- Önbellek: `filesDir/quran_cache/surah_{N}.json`

Ağ başarısız olursa daha önce indirilen sûreler yerel önbellekten açılır.

Not: Diyanet'in kendi açık kaynak projelerinde kullandığı bazı servislerin bağımsız üçüncü taraf ürünlerde kullanım şartları farklı olabilir. Bu projede doğrudan Diyanet servis anahtarı kullanılmamıştır; Al Quran Cloud edisyon uç noktası kullanılmıştır.
