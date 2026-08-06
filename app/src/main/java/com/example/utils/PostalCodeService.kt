package com.example.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object PostalCodeService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    // Offline database for instant suggestion feedback
    private val offlineZipMap = mapOf(
        "60290" to listOf("Rantigny", "Cauffry", "Laigneville", "Cambronne-lès-Clermont"),
        "60000" to listOf("Beauvais", "Allonne", "Goincourt"),
        "75001" to listOf("Paris 1er Arrondissement"),
        "75002" to listOf("Paris 2e Arrondissement"),
        "75008" to listOf("Paris 8e Arrondissement"),
        "75015" to listOf("Paris 15e Arrondissement"),
        "75000" to listOf("Paris"),
        "69001" to listOf("Lyon 1er Arrondissement"),
        "69002" to listOf("Lyon 2e Arrondissement"),
        "69000" to listOf("Lyon"),
        "13001" to listOf("Marseille 1er Arrondissement"),
        "13000" to listOf("Marseille"),
        "31000" to listOf("Toulouse"),
        "33000" to listOf("Bordeaux"),
        "44000" to listOf("Nantes"),
        "59000" to listOf("Lille"),
        "06000" to listOf("Nice"),
        "67000" to listOf("Strasbourg"),
        "34000" to listOf("Montpellier"),
        "35000" to listOf("Rennes"),
        "83000" to listOf("Toulon"),
        "38000" to listOf("Grenoble"),
        "76000" to listOf("Rouen"),
        "51000" to listOf("Reims"),
        "84000" to listOf("Avignon"),
        "78000" to listOf("Versailles"),
        "92100" to listOf("Boulogne-Billancourt"),
        "92000" to listOf("Nanterre"),
        "93000" to listOf("Bobigny"),
        "93100" to listOf("Montreuil"),
        "94000" to listOf("Créteil"),
        "95000" to listOf("Cergy")
    )

    private val departmentMap = mapOf(
        "01" to "Bourg-en-Bresse", "02" to "Laon", "03" to "Moulins", "06" to "Nice",
        "13" to "Marseille", "14" to "Caen", "21" to "Dijon", "29" to "Brest",
        "31" to "Toulouse", "33" to "Bordeaux", "34" to "Montpellier", "35" to "Rennes",
        "38" to "Grenoble", "44" to "Nantes", "45" to "Orléans", "49" to "Angers",
        "54" to "Nancy", "57" to "Metz", "59" to "Lille", "60" to "Beauvais",
        "62" to "Calais", "67" to "Strasbourg", "68" to "Mulhouse", "69" to "Lyon",
        "75" to "Paris", "76" to "Rouen", "77" to "Melun", "78" to "Versailles",
        "80" to "Amiens", "83" to "Toulon", "84" to "Avignon", "91" to "Évry",
        "92" to "Nanterre", "93" to "Bobigny", "94" to "Créteil", "95" to "Cergy"
    )

    /**
     * Look up cities corresponding to a French 5-digit postal code.
     * Tries official Government Geo API first, then falls back to local database.
     */
    suspend fun fetchCities(zipCode: String): List<String> = withContext(Dispatchers.IO) {
        val cleanZip = zipCode.trim().filter { it.isDigit() }
        if (cleanZip.length != 5) return@withContext emptyList()

        try {
            val url = "https://geo.api.gouv.fr/communes?codePostal=$cleanZip&fields=nom"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonStr = response.body?.string() ?: ""
                val array = JSONArray(jsonStr)
                val cities = mutableListOf<String>()
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    cities.add(item.getString("nom"))
                }
                if (cities.isNotEmpty()) {
                    return@withContext cities.distinct()
                }
            }
        } catch (e: Exception) {
            // Network failure or offline mode, fall back to offline dictionary
        }

        // Offline Fallback
        offlineZipMap[cleanZip]?.let { return@withContext it }

        // Department Fallback
        val dept = cleanZip.take(2)
        departmentMap[dept]?.let { return@withContext listOf(it) }

        return@withContext emptyList()
    }

    /**
     * Formats the standard French AFNOR location and date header string: "Fait à [Ville], le [Date]"
     */
    fun formatCityDate(city: String, date: Date = Date()): String {
        val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale.FRENCH)
        val formattedDate = dateFormat.format(date)
        val cityName = if (city.isBlank()) "..." else city
        return "Fait à $cityName, le $formattedDate"
    }
}
