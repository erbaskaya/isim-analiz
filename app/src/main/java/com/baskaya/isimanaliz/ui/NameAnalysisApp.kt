package com.baskaya.isimanaliz.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.baskaya.isimanaliz.data.*
import com.baskaya.isimanaliz.domain.AnalysisEngine

private enum class AppSection(val label: String, val mark: String) {
    ANALYSIS("Analiz", "◇"),
    NAMES("İsimler", "ا"),
    ESMA("Esma", "✦"),
    ABOUT("Bilgi", "i")
}

private sealed interface QuranState {
    data object Idle : QuranState
    data object Loading : QuranState
    data class Success(val surah: QuranSurah) : QuranState
    data class Error(val message: String) : QuranState
}

@Composable
fun NameAnalysisApp() {
    val context = LocalContext.current
    val repo = remember { LocalDataRepository(context) }
    val engine = remember { AnalysisEngine(repo) }
    val quranRepo = remember { QuranRepository(context) }
    var section by remember { mutableStateOf(AppSection.ANALYSIS) }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar {
                AppSection.entries.forEach { item ->
                    NavigationBarItem(
                        selected = section == item,
                        onClick = { section = item },
                        icon = { Text(item.mark, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (section) {
                AppSection.ANALYSIS -> AnalysisSection(repo, engine, quranRepo)
                AppSection.NAMES -> NamesSection(repo)
                AppSection.ESMA -> EsmaSection(repo)
                AppSection.ABOUT -> AboutSection()
            }
        }
    }
}

@Composable
private fun AnalysisSection(repo: LocalDataRepository, engine: AnalysisEngine, quranRepo: QuranRepository) {
    var result by remember { mutableStateOf<AnalysisResult?>(null) }
    if (result == null) {
        AnalysisForm(repo = repo, onResult = { result = engine.analyze(it) })
    } else {
        ResultScreen(result = result!!, quranRepo = quranRepo, onBack = { result = null })
    }
}

@Composable
private fun HeroHeader(subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .8f))
                )
            )
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            AppLogo(64.dp)
            Column {
                Text("İsim Analiz", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AppLogo(size: androidx.compose.ui.unit.Dp) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val surface = MaterialTheme.colorScheme.surface
    Canvas(Modifier.size(size)) {
        drawCircle(surface)
        drawCircle(primary, style = Stroke(width = size.toPx() * .055f))
        val cx = this.size.width / 2
        val top = this.size.height * .22f
        val bottom = this.size.height * .72f
        drawLine(primary, Offset(cx, top), Offset(cx, bottom), strokeWidth = this.size.width * .09f)
        drawCircle(primary, radius = this.size.width * .07f, center = Offset(cx, this.size.height * .16f))
        drawCircle(secondary, radius = this.size.width * .055f, center = Offset(this.size.width * .28f, this.size.height * .56f))
        drawCircle(secondary, radius = this.size.width * .055f, center = Offset(this.size.width * .72f, this.size.height * .56f))
        drawCircle(secondary, radius = this.size.width * .055f, center = Offset(cx, this.size.height * .82f))
    }
}

@Composable
private fun AnalysisForm(repo: LocalDataRepository, onResult: (AnalysisRequest) -> Unit) {
    var mode by remember { mutableStateOf(CalculationMode.CORRECTED) }

    var father1 by remember { mutableStateOf("") }; var father1Ar by remember { mutableStateOf("") }
    var father2 by remember { mutableStateOf("") }; var father2Ar by remember { mutableStateOf("") }
    var mother1 by remember { mutableStateOf("") }; var mother1Ar by remember { mutableStateOf("") }
    var mother2 by remember { mutableStateOf("") }; var mother2Ar by remember { mutableStateOf("") }
    var child1 by remember { mutableStateOf("") }; var child1Ar by remember { mutableStateOf("") }
    var child2 by remember { mutableStateOf("") }; var child2Ar by remember { mutableStateOf("") }
    var day by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    fun auto(value: String): String = repo.findName(value)?.arabic.orEmpty()
    fun nameInput(latin: String, arabic: String): NameInput = NameInput(
        latin = latin.trim(),
        arabic = arabic.trim(),
        registeredEbced = repo.findName(latin)?.registeredEbced
    )

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { HeroHeader("Ebced • Element • Fikir • Akıl • Burç • Sûre") }
        item {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Hesaplama modu", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = mode == CalculationMode.CORRECTED,
                        onClick = { mode = CalculationMode.CORRECTED },
                        label = { Text("Düzeltilmiş") }
                    )
                    FilterChip(
                        selected = mode == CalculationMode.EXCEL_COMPATIBLE,
                        onClick = { mode = CalculationMode.EXCEL_COMPATIBLE },
                        label = { Text("Excel uyumlu") }
                    )
                }
                Text(
                    if (mode == CalculationMode.CORRECTED)
                        "Tüm harfleri hesaplar, Arapça harekeleri normalize eder ve 12. burç hatasını düzeltir."
                    else
                        "Mevcut XLSM'deki formül/makro davranışını mümkün olduğunca aynen tekrarlar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            PersonInputCard(
                title = "Baba",
                firstLatin = father1, firstArabic = father1Ar,
                secondLatin = father2, secondArabic = father2Ar,
                repo = repo,
                onFirstLatin = { father1 = it; auto(it).takeIf { a -> a.isNotBlank() }?.let { a -> father1Ar = a } },
                onFirstArabic = { father1Ar = it },
                onSecondLatin = { father2 = it; auto(it).takeIf { a -> a.isNotBlank() }?.let { a -> father2Ar = a } },
                onSecondArabic = { father2Ar = it }
            )
        }
        item {
            PersonInputCard(
                title = "Anne",
                firstLatin = mother1, firstArabic = mother1Ar,
                secondLatin = mother2, secondArabic = mother2Ar,
                repo = repo,
                onFirstLatin = { mother1 = it; auto(it).takeIf { a -> a.isNotBlank() }?.let { a -> mother1Ar = a } },
                onFirstArabic = { mother1Ar = it },
                onSecondLatin = { mother2 = it; auto(it).takeIf { a -> a.isNotBlank() }?.let { a -> mother2Ar = a } },
                onSecondArabic = { mother2Ar = it }
            )
        }
        item {
            PersonInputCard(
                title = "Çocuk",
                firstLatin = child1, firstArabic = child1Ar,
                secondLatin = child2, secondArabic = child2Ar,
                repo = repo,
                onFirstLatin = { child1 = it; auto(it).takeIf { a -> a.isNotBlank() }?.let { a -> child1Ar = a } },
                onFirstArabic = { child1Ar = it },
                onSecondLatin = { child2 = it; auto(it).takeIf { a -> a.isNotBlank() }?.let { a -> child2Ar = a } },
                onSecondArabic = { child2Ar = it }
            )
        }
        item {
            Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Doğum tarihi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NumberField("Gün", day, { day = it.filter(Char::isDigit).take(2) }, Modifier.weight(1f))
                        NumberField("Ay", month, { month = it.filter(Char::isDigit).take(2) }, Modifier.weight(1f))
                        NumberField("Yıl", year, { year = it.filter(Char::isDigit).take(4) }, Modifier.weight(1.6f))
                    }
                }
            }
        }
        item {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
                Button(
                    onClick = {
                        val d = day.toIntOrNull(); val m = month.toIntOrNull(); val y = year.toIntOrNull()
                        val requiredOk = father1.isNotBlank() && father1Ar.isNotBlank() && mother1.isNotBlank() && mother1Ar.isNotBlank() && child1.isNotBlank() && child1Ar.isNotBlank()
                        if (!requiredOk) {
                            error = "Baba, anne ve çocuk için en az bir isim ve Arapça karşılığı gerekli. Sözlükte olmayan ismin Arapçasını elle yazabilirsiniz."
                        } else if (d == null || d !in 1..31 || m == null || m !in 1..12 || y == null || y !in 1..9999) {
                            error = "Geçerli bir doğum tarihi girin."
                        } else {
                            error = null
                            onResult(
                                AnalysisRequest(
                                    father = PersonInput(nameInput(father1, father1Ar), father2.takeIf { it.isNotBlank() || father2Ar.isNotBlank() }?.let { nameInput(father2, father2Ar) }),
                                    mother = PersonInput(nameInput(mother1, mother1Ar), mother2.takeIf { it.isNotBlank() || mother2Ar.isNotBlank() }?.let { nameInput(mother2, mother2Ar) }),
                                    child = PersonInput(nameInput(child1, child1Ar), child2.takeIf { it.isNotBlank() || child2Ar.isNotBlank() }?.let { nameInput(child2, child2Ar) }),
                                    birthDate = BirthDateInput(d, m, y),
                                    mode = mode
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp)
                ) { Text("Analizi Hesapla", fontWeight = FontWeight.Bold) }
                Text("Software by Baskaya", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun PersonInputCard(
    title: String,
    firstLatin: String,
    firstArabic: String,
    secondLatin: String,
    secondArabic: String,
    repo: LocalDataRepository,
    onFirstLatin: (String) -> Unit,
    onFirstArabic: (String) -> Unit,
    onSecondLatin: (String) -> Unit,
    onSecondArabic: (String) -> Unit
) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            SmartNameField("1. isim", firstLatin, firstArabic, repo, onFirstLatin, onFirstArabic)
            SmartNameField("2. isim (isteğe bağlı)", secondLatin, secondArabic, repo, onSecondLatin, onSecondArabic)
        }
    }
}

@Composable
private fun SmartNameField(
    label: String,
    latin: String,
    arabic: String,
    repo: LocalDataRepository,
    onLatin: (String) -> Unit,
    onArabic: (String) -> Unit
) {
    val suggestions = remember(latin) { if (repo.findName(latin) == null) repo.suggestNames(latin, 4) else emptyList() }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(
            value = latin,
            onValueChange = onLatin,
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )
        if (suggestions.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(suggestions) { n ->
                    AssistChip(onClick = { onLatin(n.latin); onArabic(n.arabic) }, label = { Text(n.latin) })
                }
            }
        }
        OutlinedTextField(
            value = arabic,
            onValueChange = onArabic,
            label = { Text("Arapça") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(fontSize = 22.sp, textAlign = TextAlign.End),
            shape = RoundedCornerShape(14.dp),
            supportingText = {
                Text(if (repo.findName(latin) != null) "Yerel isim sözlüğünden" else "Sözlükte yoksa elle düzeltilebilir")
            }
        )
    }
}

@Composable
private fun NumberField(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
        shape = RoundedCornerShape(14.dp)
    )
}

@Composable
private fun ResultScreen(result: AnalysisResult, quranRepo: QuranRepository, onBack: () -> Unit) {
    var quranState by remember(result.revelation?.surahNumber) { mutableStateOf<QuranState>(QuranState.Idle) }
    val surahNumber = result.revelation?.surahNumber
    LaunchedEffect(surahNumber) {
        if (surahNumber != null) {
            quranState = QuranState.Loading
            quranState = runCatching { quranRepo.getSurah(surahNumber) }
                .fold(onSuccess = { QuranState.Success(it) }, onFailure = { QuranState.Error(it.message ?: "Bağlantı hatası") })
        }
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { HeroHeader(if (result.mode == CalculationMode.CORRECTED) "Düzeltilmiş hesaplama sonucu" else "Excel uyumlu hesaplama sonucu") }
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryCard("Fikir", result.fikir.toString(), Modifier.weight(1f))
                SummaryCard("Akıl", result.akil.toString(), Modifier.weight(1f))
                SummaryCard("Burç", result.burcName, Modifier.weight(1f))
                SummaryCard("Doğum", result.birthNumber.toString(), Modifier.weight(1f))
            }
        }
        item {
            Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Yorumlar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    MeaningRow("Fikir", result.fikirMeaning)
                    MeaningRow("Akıl", result.akilMeaning)
                    MeaningRow("Burç", result.burcMeaning)
                }
            }
        }
        item { PersonResultCard("Baba", result.father) }
        item { PersonResultCard("Anne", result.mother) }
        item { PersonResultCard("Çocuk", result.child) }
        item {
            Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Dişil / Eril değerlendirmesi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Eril", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    Text(result.masculineMeaning, style = MaterialTheme.typography.bodyMedium)
                    Text("Dişil", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.SemiBold)
                    Text(result.feminineMeaning, style = MaterialTheme.typography.bodyMedium)
                    Text("Pratik anlam", fontWeight = FontWeight.SemiBold)
                    Text(result.practicalMeaning, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Çocuk + Anne Frekans Sûresi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Nüzul sırası: ${result.frequencyOrder}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val r = result.revelation
                    if (r != null) {
                        Text("${r.surahName} Sûresi", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text("Mushaf sırası: ${r.surahNumber} • ${if (r.period == "mekki") "Mekkî" else "Medenî"}")
                    } else {
                        Text("Bu frekans için 1–114 aralığında sûre eşleşmesi yok.", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
        when (val qs = quranState) {
            QuranState.Idle -> Unit
            QuranState.Loading -> item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
            is QuranState.Error -> item {
                Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(18.dp)) {
                    Text("Meal alınamadı: ${qs.message}\nİnternet bağlantısını kontrol edin. Daha önce açılan sûreler yerel önbellekten çalışır.", Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error)
                }
            }
            is QuranState.Success -> {
                item {
                    Text(
                        "Ayetler ve Diyanet Meali${if (qs.surah.fromCache) " • Yerel önbellek" else " • İnternetten alındı"}",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(qs.surah.ayahs, key = { it.numberInSurah }) { ayah -> AyahCard(ayah) }
                item {
                    Text(
                        "Kaynak: Al Quran Cloud API • quran-uthmani / tr.diyanet. Çeviri metni otomatik olarak değiştirilmez.",
                        Modifier.padding(horizontal = 20.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        item {
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(52.dp), shape = RoundedCornerShape(16.dp)) {
                Text("Yeni Analiz")
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, maxLines = 1)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MeaningRow(label: String, value: String) {
    if (value.isNotBlank()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("$label:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(value.trim(), modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun PersonResultCard(title: String, p: PersonAnalysis) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Ebced: ${p.ebcedTotal}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                p.registeredEbcedTotal?.let { registered ->
                    if (registered != p.ebcedTotal) {
                        SuggestionBadge("İsim tablosu: $registered")
                    }
                }
            }
            Text("Elementler: ${formatElements(p.elementCounts)}", style = MaterialTheme.typography.bodyMedium)
            Text("Eril: ${p.genderCounts["E"] ?: 0} • Dişil: ${p.genderCounts["D"] ?: 0}", style = MaterialTheme.typography.bodyMedium)
            if (p.unknownChars.isNotEmpty()) {
                Text("Element tablosunda bulunmayan karakterler: ${p.unknownChars.joinToString(" ")}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            NamePartLetters(p.first)
            p.second?.let { NamePartLetters(it) }
        }
    }
}

@Composable
private fun NamePartLetters(part: NamePartAnalysis) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("${part.input.latin}  ${part.input.arabic}", fontWeight = FontWeight.SemiBold)
        if (part.ebcedUsed != part.fullMappedEbced) {
            Text("Excel uyumu: kullanılan toplam ${part.ebcedUsed}; tüm eşleşen harfler ${part.fullMappedEbced}.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(part.letters) { l ->
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Column(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(l.char, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text(l.ebced?.toString() ?: "?", style = MaterialTheme.typography.labelMedium)
                        Text(listOfNotNull(l.element, l.gender).joinToString("/"), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionBadge(text: String) {
    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.secondary.copy(alpha = .16f)) {
        Text(text, Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
    }
}

private fun formatElements(map: Map<String, Int>): String {
    val names = mapOf("A" to "Ateş", "T" to "Toprak", "H" to "Hava", "S" to "Su")
    return listOf("A", "T", "H", "S").joinToString(" • ") { "${names[it]} ${map[it] ?: 0}" }
}

@Composable
private fun AyahCard(ayah: QuranAyah) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("${ayah.numberInSurah}. ayet", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(ayah.arabic, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End, fontSize = 25.sp, lineHeight = 40.sp)
            HorizontalDivider()
            Text(ayah.turkish, style = MaterialTheme.typography.bodyLarge, lineHeight = 25.sp)
        }
    }
}

@Composable
private fun NamesSection(repo: LocalDataRepository) {
    var query by remember { mutableStateOf("") }
    val list = remember(query) {
        val q = query.trim().lowercase()
        if (q.isBlank()) repo.names else repo.names.filter { it.latin.lowercase().contains(q) || it.arabic.contains(query.trim()) }
    }
    Column(Modifier.fillMaxSize()) {
        HeroHeader("Yerel isim ve Arapça yazım sözlüğü")
        OutlinedTextField(query, { query = it }, label = { Text("İsim ara") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(14.dp))
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(list, key = { it.latin + it.arabic }) { n ->
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(n.latin, fontWeight = FontWeight.Bold)
                            n.registeredEbced?.let { Text("Kayıtlı Ebced: $it", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                        Text(n.arabic, fontSize = 24.sp, textAlign = TextAlign.End)
                    }
                }
            }
        }
    }
}

@Composable
private fun EsmaSection(repo: LocalDataRepository) {
    var query by remember { mutableStateOf("") }
    val list = remember(query) {
        val q = query.trim().lowercase()
        if (q.isBlank()) repo.esma else repo.esma.filter { it.name.lowercase().contains(q) || it.description.lowercase().contains(q) }
    }
    Column(Modifier.fillMaxSize()) {
        HeroHeader("Excel'deki Esma Zikr Sayısı tablosu")
        OutlinedTextField(query, { query = it }, label = { Text("Esma veya niyet ara") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(14.dp))
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(list) { e ->
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(e.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(e.count?.toString() ?: "—", fontWeight = FontWeight.ExtraBold)
                        }
                        Text(e.description, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutSection() {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { HeroHeader("XLSM çalışma kitabının Android uyarlaması") }
        item {
            InfoCard("Yerel veriler", "526 isim ve Arapça karşılığı, 32 harf/ebced/element kuralı, 12 burç kaydı, 98 Esma kaydı ve 114 nüzul-sûre eşlemesi uygulamanın içinde tutulur.")
        }
        item {
            InfoCard("İnternetten alınan veri", "Kur’an’ın 6.236 ayeti uygulamaya gömülmez. Sonuçta gereken sûre, Al Quran Cloud üzerinden quran-uthmani ve tr.diyanet baskılarıyla alınır ve cihazın özel depolamasında önbelleğe yazılır.")
        }
        item {
            InfoCard("Gizlilik", "Baba, anne, çocuk isimleri ve doğum tarihi internet servisine gönderilmez. Ağ isteğinde yalnızca sonuçta belirlenen sûre numarası kullanılır; kişisel girişler cihazda kalır.")
        }
        item {
            InfoCard("Excel uyumlu mod", "Orijinal VBA/formül davranışını korur. Baba ilk adında ilk 7 harfin Ebced toplamına girmesi ve 12’ye tam bölünmede burç değerinin 0 kalması gibi mevcut dosya davranışları görünür hale gelir.")
        }
        item {
            InfoCard("Düzeltilmiş mod", "İsimdeki tüm harfleri hesaba katar, Unicode Arapça sunum biçimlerini/harekeleri normalize eder ve burçta 0 sonucunu 12 olarak yorumlar. Tanımsız harflere değer uydurmaz; bunları sonuçta uyarı olarak gösterir.")
        }
        item {
            InfoCard("Not", "Bu uygulama çalışma kitabındaki geleneksel Ebced/numeroloji yöntemini dijitalleştirir. Sonuçlar bilimsel veya dinî hüküm olarak sunulmaz. Esma tablosu Excel’de hiçbir makroya bağlı olmadığı için ayrı rehber olarak korunmuştur.")
        }
        item {
            Text("Software by Baskaya", Modifier.fillMaxWidth().padding(24.dp), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun InfoCard(title: String, text: String) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 7.dp), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text, style = MaterialTheme.typography.bodyMedium, lineHeight = 23.sp)
        }
    }
}
