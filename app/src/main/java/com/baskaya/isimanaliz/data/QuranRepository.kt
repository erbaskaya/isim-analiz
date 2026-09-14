package com.baskaya.isimanaliz.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class QuranRepository(private val context: Context) {
    private val cacheDir = File(context.filesDir, "quran_cache").apply { mkdirs() }

    suspend fun getSurah(number: Int): QuranSurah = withContext(Dispatchers.IO) {
        require(number in 1..114)
        val cacheFile = File(cacheDir, "surah_$number.json")
        if (cacheFile.exists()) {
            runCatching { parseCombined(cacheFile.readText(Charsets.UTF_8), true) }.getOrNull()?.let { return@withContext it }
        }

        val arabic = fetch("https://api.alquran.cloud/v1/surah/$number/quran-uthmani")
        val turkish = fetch("https://api.alquran.cloud/v1/surah/$number/tr.diyanet")
        val combined = combine(number, arabic, turkish)
        cacheFile.writeText(combined, Charsets.UTF_8)
        parseCombined(combined, false)
    }

    private fun fetch(url: String): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 12_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "IsimAnaliz-Android/1.0")
        }
        try {
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            if (code !in 200..299) error("Kur'an servisi HTTP $code")
            return body
        } finally {
            connection.disconnect()
        }
    }

    private fun combine(number: Int, arabicJson: String, turkishJson: String): String {
        val arRoot = JSONObject(arabicJson).getJSONObject("data")
        val trRoot = JSONObject(turkishJson).getJSONObject("data")
        val arAyahs = arRoot.getJSONArray("ayahs")
        val trAyahs = trRoot.getJSONArray("ayahs")
        val trByNo = mutableMapOf<Int, String>()
        for (i in 0 until trAyahs.length()) {
            val a = trAyahs.getJSONObject(i)
            trByNo[a.getInt("numberInSurah")] = a.getString("text")
        }
        val outAyahs = JSONArray()
        for (i in 0 until arAyahs.length()) {
            val a = arAyahs.getJSONObject(i)
            val no = a.getInt("numberInSurah")
            outAyahs.put(JSONObject().apply {
                put("number", no)
                put("arabic", a.getString("text"))
                put("turkish", trByNo[no].orEmpty())
            })
        }
        return JSONObject().apply {
            put("number", number)
            put("name", arRoot.optString("englishName", number.toString()))
            put("ayahs", outAyahs)
        }.toString()
    }

    private fun parseCombined(json: String, fromCache: Boolean): QuranSurah {
        val root = JSONObject(json)
        val arr = root.getJSONArray("ayahs")
        val ayahs = List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            QuranAyah(o.getInt("number"), o.getString("arabic"), o.getString("turkish"))
        }
        return QuranSurah(root.getInt("number"), root.getString("name"), ayahs, fromCache)
    }
}
