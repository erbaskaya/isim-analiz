package com.baskaya.isimanaliz.domain

import com.baskaya.isimanaliz.data.*
import java.text.Normalizer

class AnalysisEngine(private val repo: LocalDataRepository) {

    fun analyze(request: AnalysisRequest): AnalysisResult {
        val father = analyzePerson(PersonRole.FATHER, request.father, request.mode)
        val mother = analyzePerson(PersonRole.MOTHER, request.mother, request.mode)
        val child = analyzePerson(PersonRole.CHILD, request.child, request.mode)

        val fikir = reduceToSingleDigitLikeWorkbook(father.ebcedTotal + child.ebcedTotal)
        val akil = reduceToSingleDigitLikeWorkbook(mother.ebcedTotal + child.ebcedTotal)

        val burcBase = mother.ebcedTotal + child.ebcedTotal
        val rawBurc = if (burcBase == 0) 0 else burcBase % 12
        val burcIndex = when (request.mode) {
            CalculationMode.EXCEL_COMPATIBLE -> rawBurc
            CalculationMode.CORRECTED -> if (rawBurc == 0 && burcBase > 0) 12 else rawBurc
        }
        val burcRow = repo.burcData.rows.firstOrNull { it.index == burcIndex }
        val fikirMeaning = repo.burcData.rows.firstOrNull { it.ideaValue == fikir }?.ideaMeaning.orEmpty()
        val akilMeaning = repo.burcData.rows.firstOrNull { it.mindValue == akil }?.mindMeaning.orEmpty()

        val frequencyOrder = (mother.genderCounts["E"] ?: 0) + (mother.genderCounts["D"] ?: 0) +
            (child.genderCounts["E"] ?: 0) + (child.genderCounts["D"] ?: 0)

        return AnalysisResult(
            mode = request.mode,
            father = father,
            mother = mother,
            child = child,
            fikir = fikir,
            fikirMeaning = fikirMeaning,
            akil = akil,
            akilMeaning = akilMeaning,
            burcIndex = burcIndex,
            burcName = burcRow?.name ?: if (burcIndex == 0) "Excel'de eşleşmiyor" else "—",
            burcMeaning = burcRow?.trait.orEmpty(),
            birthNumber = calculateBirthNumber(request.birthDate),
            frequencyOrder = frequencyOrder,
            revelation = repo.revelationByOrder[frequencyOrder],
            masculineMeaning = repo.burcData.masculineMeaning,
            feminineMeaning = repo.burcData.feminineMeaning,
            practicalMeaning = repo.burcData.practicalMeaning
        )
    }

    private fun analyzePerson(role: PersonRole, input: PersonInput, mode: CalculationMode): PersonAnalysis {
        val firstLimit = when {
            mode == CalculationMode.CORRECTED -> null
            else -> 14
        }
        val secondLimit = if (mode == CalculationMode.CORRECTED) null else 14

        val first = analyzePart(input.first, mode, firstLimit, role == PersonRole.FATHER && mode == CalculationMode.EXCEL_COMPATIBLE)
        val second = input.second?.takeIf { it.latin.isNotBlank() || it.arabic.isNotBlank() }
            ?.let { analyzePart(it, mode, secondLimit, false) }

        val allLetters = buildList {
            addAll(first.letters)
            if (second != null) addAll(second.letters)
        }
        val elementCounts = allLetters.mapNotNull { it.element }.groupingBy { it }.eachCount()
        val genderCounts = allLetters.mapNotNull { it.gender }.groupingBy { it }.eachCount()
        val registeredValues = listOfNotNull(first.input.registeredEbced, second?.input?.registeredEbced)
        val registeredTotal = if (registeredValues.isEmpty()) null else registeredValues.sum()

        return PersonAnalysis(
            role = role,
            first = first,
            second = second,
            ebcedTotal = first.ebcedUsed + (second?.ebcedUsed ?: 0),
            registeredEbcedTotal = registeredTotal,
            elementCounts = elementCounts,
            genderCounts = genderCounts,
            unknownChars = (first.unknownChars + (second?.unknownChars ?: emptyList())).distinct()
        )
    }

    private fun analyzePart(
        input: NameInput,
        mode: CalculationMode,
        limit: Int?,
        fatherFirstSevenBug: Boolean
    ): NamePartAnalysis {
        val source = if (mode == CalculationMode.CORRECTED) normalizeArabic(input.arabic) else input.arabic
        val characters = source.codePoints().toArray().map { String(Character.toChars(it)) }
            .filterNot { it.isBlank() || it == "ـ" }
            .let { if (limit == null) it else it.take(limit) }

        val letters = characters.map { ch ->
            val e = repo.elementMap[ch]
            LetterAnalysis(
                char = ch,
                ebced = e?.ebced,
                element = e?.element,
                gender = e?.gender,
                known = e != null
            )
        }

        val fullMapped = letters.sumOf { it.ebced ?: 0 }
        val ebcedUsed = if (fatherFirstSevenBug) letters.take(7).sumOf { it.ebced ?: 0 } else fullMapped
        return NamePartAnalysis(
            input = input,
            letters = letters,
            ebcedUsed = ebcedUsed,
            fullMappedEbced = fullMapped,
            unknownChars = letters.filterNot { it.known }.map { it.char }.distinct()
        )
    }

    private fun normalizeArabic(value: String): String {
        val normalized = Normalizer.normalize(value, Normalizer.Form.NFKC)
        return normalized.filterNot { ch ->
            Character.getType(ch) == Character.NON_SPACING_MARK.toInt() ||
                Character.getType(ch) == Character.COMBINING_SPACING_MARK.toInt()
        }
    }

    private fun reduceToSingleDigitLikeWorkbook(value: Int): Int {
        if (value <= 0) return 0
        val first = digitSum(value)
        return if (first >= 10) digitSum(first) else first
    }

    private fun digitSum(value: Int): Int = value.toString().filter { it.isDigit() }.sumOf { it.digitToInt() }

    private fun calculateBirthNumber(date: BirthDateInput): Int {
        val dayDigits = date.day.coerceIn(1, 31).toString().padStart(2, '0').sumOf { it.digitToInt() }
        val dayReduced = if (dayDigits >= 10) digitSum(dayDigits) else dayDigits

        val monthSum = date.month.coerceIn(1, 12).toString().padStart(2, '0').sumOf { it.digitToInt() }

        val yearDigits = date.year.coerceIn(1, 9999).toString().padStart(4, '0').sumOf { it.digitToInt() }
        val yearReduced = if (yearDigits >= 10) digitSum(yearDigits) else yearDigits

        val total = dayReduced + monthSum + yearReduced
        return if (total > 11) digitSum(total) else total
    }
}
