package com.baskaya.isimanaliz.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

class LocalDataRepository(private val context: Context) {
    val names: List<NameEntry> by lazy { loadNames() }
    val elements: List<ElementEntry> by lazy { loadElements() }
    val burcData: BurcData by lazy { loadBurc() }
    val esma: List<EsmaEntry> by lazy { loadEsma() }
    val revelationMap: List<RevelationEntry> by lazy { loadRevelationMap() }

    val elementMap: Map<String, ElementEntry> by lazy { elements.associateBy { it.arabic } }
    private val nameMap: Map<String, NameEntry> by lazy { names.associateBy { normalizeLatin(it.latin) } }
    val revelationByOrder: Map<Int, RevelationEntry> by lazy { revelationMap.associateBy { it.revelationOrder } }

    fun findName(value: String): NameEntry? = nameMap[normalizeLatin(value)]

    fun suggestNames(query: String, limit: Int = 5): List<NameEntry> {
        val q = normalizeLatin(query)
        if (q.length < 2) return emptyList()
        return names.asSequence()
            .filter { normalizeLatin(it.latin).startsWith(q) }
            .take(limit)
            .toList()
    }

    private fun normalizeLatin(value: String): String =
        value.trim().lowercase(Locale("tr", "TR"))

    private fun readAsset(name: String): String =
        context.assets.open(name).bufferedReader(Charsets.UTF_8).use { it.readText() }

    private fun loadNames(): List<NameEntry> {
        val array = JSONArray(readAsset("names.json"))
        return List(array.length()) { i ->
            val o = array.getJSONObject(i)
            NameEntry(
                latin = o.getString("latin"),
                arabic = o.getString("arabic"),
                registeredEbced = if (o.isNull("registeredEbced")) null else o.getInt("registeredEbced")
            )
        }
    }

    private fun loadElements(): List<ElementEntry> {
        val array = JSONArray(readAsset("elements.json"))
        return List(array.length()) { i ->
            val o = array.getJSONObject(i)
            ElementEntry(o.getString("arabic"), o.getInt("ebced"), o.getString("element"), o.getString("gender"))
        }
    }

    private fun loadBurc(): BurcData {
        val root = JSONObject(readAsset("burc.json"))
        val rowsJson = root.getJSONArray("rows")
        val rows = List(rowsJson.length()) { i ->
            val o = rowsJson.getJSONObject(i)
            BurcEntry(
                index = o.getInt("index"),
                name = o.getString("name"),
                trait = o.getString("trait"),
                mindValue = if (o.isNull("mindValue")) null else o.getInt("mindValue"),
                mindMeaning = o.optString("mindMeaning"),
                ideaValue = if (o.isNull("ideaValue")) null else o.getInt("ideaValue"),
                ideaMeaning = o.optString("ideaMeaning")
            )
        }
        return BurcData(
            rows = rows,
            masculineMeaning = root.optString("masculineMeaning"),
            feminineMeaning = root.optString("feminineMeaning"),
            practicalMeaning = root.optString("practicalMeaning")
        )
    }

    private fun loadEsma(): List<EsmaEntry> {
        val array = JSONArray(readAsset("esma.json"))
        return List(array.length()) { i ->
            val o = array.getJSONObject(i)
            EsmaEntry(
                count = if (o.isNull("count")) null else o.getInt("count"),
                name = o.getString("name"),
                description = o.getString("description")
            )
        }
    }

    private fun loadRevelationMap(): List<RevelationEntry> {
        val array = JSONArray(readAsset("revelation_map.json"))
        return List(array.length()) { i ->
            val o = array.getJSONObject(i)
            RevelationEntry(
                revelationOrder = o.getInt("revelationOrder"),
                surahNumber = o.getInt("surahNumber"),
                surahName = o.getString("surahName"),
                period = o.getString("period")
            )
        }
    }
}
