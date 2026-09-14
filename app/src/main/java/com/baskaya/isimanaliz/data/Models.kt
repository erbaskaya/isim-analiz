package com.baskaya.isimanaliz.data

data class NameEntry(
    val latin: String,
    val arabic: String,
    val registeredEbced: Int?
)

data class ElementEntry(
    val arabic: String,
    val ebced: Int,
    val element: String,
    val gender: String
)

data class BurcEntry(
    val index: Int,
    val name: String,
    val trait: String,
    val mindValue: Int?,
    val mindMeaning: String,
    val ideaValue: Int?,
    val ideaMeaning: String
)

data class BurcData(
    val rows: List<BurcEntry>,
    val masculineMeaning: String,
    val feminineMeaning: String,
    val practicalMeaning: String
)

data class EsmaEntry(
    val count: Int?,
    val name: String,
    val description: String
)

data class RevelationEntry(
    val revelationOrder: Int,
    val surahNumber: Int,
    val surahName: String,
    val period: String
)

enum class CalculationMode {
    EXCEL_COMPATIBLE,
    CORRECTED
}

enum class PersonRole { FATHER, MOTHER, CHILD }

data class NameInput(
    val latin: String,
    val arabic: String,
    val registeredEbced: Int? = null
)

data class PersonInput(
    val first: NameInput,
    val second: NameInput? = null
)

data class BirthDateInput(
    val day: Int,
    val month: Int,
    val year: Int
)

data class AnalysisRequest(
    val father: PersonInput,
    val mother: PersonInput,
    val child: PersonInput,
    val birthDate: BirthDateInput,
    val mode: CalculationMode
)

data class LetterAnalysis(
    val char: String,
    val ebced: Int?,
    val element: String?,
    val gender: String?,
    val known: Boolean
)

data class NamePartAnalysis(
    val input: NameInput,
    val letters: List<LetterAnalysis>,
    val ebcedUsed: Int,
    val fullMappedEbced: Int,
    val unknownChars: List<String>
)

data class PersonAnalysis(
    val role: PersonRole,
    val first: NamePartAnalysis,
    val second: NamePartAnalysis?,
    val ebcedTotal: Int,
    val registeredEbcedTotal: Int?,
    val elementCounts: Map<String, Int>,
    val genderCounts: Map<String, Int>,
    val unknownChars: List<String>
)

data class AnalysisResult(
    val mode: CalculationMode,
    val father: PersonAnalysis,
    val mother: PersonAnalysis,
    val child: PersonAnalysis,
    val fikir: Int,
    val fikirMeaning: String,
    val akil: Int,
    val akilMeaning: String,
    val burcIndex: Int,
    val burcName: String,
    val burcMeaning: String,
    val birthNumber: Int,
    val frequencyOrder: Int,
    val revelation: RevelationEntry?,
    val masculineMeaning: String,
    val feminineMeaning: String,
    val practicalMeaning: String
)

data class QuranAyah(
    val numberInSurah: Int,
    val arabic: String,
    val turkish: String
)

data class QuranSurah(
    val number: Int,
    val name: String,
    val ayahs: List<QuranAyah>,
    val fromCache: Boolean
)
