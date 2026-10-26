package com.hos.happy.plants.data.remote

import android.graphics.Bitmap
import com.google.gson.Gson
import com.hos.happy.plants.data.model.MasterPlant
import com.hos.happy.plants.data.model.PlantIdentificationResult
import com.hos.happy.plants.data.remote.model.PlantNetResponse
import com.hos.happy.plants.data.remote.model.WikipediaSummaryResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class PlantNetApiService {

    private val gson = Gson()

    suspend fun identifyPlant(
        bitmap: Bitmap,
        apiKey: String,
        localCatalog: List<MasterPlant> = emptyList()
    ): Result<PlantIdentificationResult> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException("Pl@ntNet API Key is missing. Please add it to your .env file or enter it in settings.")
            )
        }

        try {
            // Compress bitmap to JPEG for upload
            val stream = ByteArrayOutputStream()
            // Scale down if oversized to ensure fast and lightweight upload (< 2 MB)
            val scaledBitmap = if (bitmap.width > 1600 || bitmap.height > 1600) {
                val ratio = 1600f / maxOf(bitmap.width, bitmap.height)
                Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true)
            } else {
                bitmap
            }
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
            val imageBytes = stream.toByteArray()

            val boundary = "---PlantNetBoundary${System.currentTimeMillis()}"
            val lineEnd = "\r\n"
            val twoHyphens = "--"

            val urlStr = "https://my-api.plantnet.org/v2/identify/all?api-key=${apiKey.trim()}&include-related-images=true&no-reject=false&lang=en"
            val url = URL(urlStr)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 15000
            conn.readTimeout = 20000
            conn.doInput = true
            conn.doOutput = true
            conn.useCaches = false
            conn.setRequestProperty("Connection", "Keep-Alive")
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

            DataOutputStream(conn.outputStream).use { output ->
                // Field: organs
                output.writeBytes(twoHyphens + boundary + lineEnd)
                output.writeBytes("Content-Disposition: form-data; name=\"organs\"$lineEnd")
                output.writeBytes(lineEnd)
                output.writeBytes("auto$lineEnd")

                // Field: images
                output.writeBytes(twoHyphens + boundary + lineEnd)
                output.writeBytes("Content-Disposition: form-data; name=\"images\"; filename=\"plant.jpg\"$lineEnd")
                output.writeBytes("Content-Type: image/jpeg$lineEnd")
                output.writeBytes(lineEnd)
                output.write(imageBytes)
                output.writeBytes(lineEnd)

                // End boundary
                output.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)
                output.flush()
            }

            val responseCode = conn.responseCode
            if (responseCode in 200..299) {
                val responseText = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                val plantNetResponse = gson.fromJson(responseText, PlantNetResponse::class.java)

                val bestResult = plantNetResponse.results.firstOrNull()
                    ?: return@withContext Result.failure(Exception("No plant matches found in the image. Please try a clearer close-up of a leaf or flower."))

                val scientificName = bestResult.species.scientificNameWithoutAuthor.trim()
                val confidence = (bestResult.score * 100).toInt().coerceIn(1, 99)
                val genus = bestResult.species.genus?.scientificNameWithoutAuthor
                val family = bestResult.species.family?.scientificNameWithoutAuthor
                val primaryCommonName = bestResult.species.commonNames.firstOrNull()?.replaceFirstChar { it.uppercase() }
                    ?: scientificName

                // Image resolution: try PlantNet reference image first
                var referenceImageUrl = bestResult.images.firstOrNull()?.url?.bestUrl

                // Check if this identified plant matches our local catalog (20 indoor species in India)
                val catalogMatch = localCatalog.find { catalogPlant ->
                    catalogPlant.botanicalName.equals(scientificName, ignoreCase = true) ||
                            catalogPlant.commonName.equals(primaryCommonName, ignoreCase = true) ||
                            catalogPlant.botanicalName.contains(genus ?: "---", ignoreCase = true) &&
                            catalogPlant.botanicalName.split(" ").firstOrNull().equals(genus, ignoreCase = true)
                }

                var wikiSummary: WikipediaSummaryResponse? = null
                if (referenceImageUrl.isNullOrBlank() || catalogMatch == null) {
                    wikiSummary = fetchWikipediaInfo(scientificName)
                    if (referenceImageUrl.isNullOrBlank()) {
                        referenceImageUrl = wikiSummary?.thumbnail?.source ?: wikiSummary?.originalimage?.source
                    }
                }

                // If matched to our catalog, use catalog's tailored image, light conditions, and watering!
                val finalImageUrl = catalogMatch?.displayImageUrl ?: referenceImageUrl
                val lightCondition = if (catalogMatch != null && catalogMatch.lightLux != null) {
                    "Ideal: ${catalogMatch.lightLux.idealMin?.toInt() ?: 1500} – ${catalogMatch.lightLux.idealMax?.toInt() ?: 3500} lx (tolerates ${catalogMatch.lightLux.minTolerated?.toInt() ?: 500} – ${catalogMatch.lightLux.maxTolerated?.toInt() ?: 5000} lx)"
                } else {
                    inferLightCondition(family, genus)
                }

                val moistureNeed = if (catalogMatch != null && catalogMatch.watering != null) {
                    val hotDays = catalogMatch.watering.hotDryMonths
                    val coolDays = catalogMatch.watering.monsoonAndWinter
                    "Summer: every ${hotDays?.min ?: 7}–${hotDays?.max ?: 10} days. Winter/Monsoon: every ${coolDays?.min ?: 12}–${coolDays?.max ?: 20} days. ${catalogMatch.watering.note ?: ""}"
                } else {
                    inferMoistureCondition(family, genus)
                }

                val soilAdvice = catalogMatch?.soil?.inSimpleWords
                    ?: inferSoilAdvice(family, genus)

                val result = PlantIdentificationResult(
                    commonName = catalogMatch?.commonName ?: primaryCommonName,
                    botanicalName = scientificName,
                    family = family,
                    genus = genus,
                    confidencePercent = confidence,
                    referenceImageUrl = finalImageUrl,
                    userCapturedBitmap = bitmap,
                    lightCondition = lightCondition,
                    moistureNeed = moistureNeed,
                    soilAdvice = soilAdvice,
                    localNames = catalogMatch?.localNames,
                    masterPlant = catalogMatch,
                    description = wikiSummary?.extract
                )

                Result.success(result)
            } else {
                val errorText = try {
                    BufferedReader(InputStreamReader(conn.errorStream ?: conn.inputStream)).use { it.readText() }
                } catch (e: Exception) {
                    "HTTP $responseCode"
                }

                val errorMessage = when (responseCode) {
                    401, 403 -> "Invalid or unauthorized Pl@ntNet API Key. Please verify your key at my.plantnet.org."
                    404 -> "Pl@ntNet could not identify this plant. Please try taking a closer, well-lit photo of the leaf or flower."
                    else -> "Pl@ntNet identification failed ($responseCode): $errorText"
                }

                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun fetchWikipediaInfo(botanicalName: String): WikipediaSummaryResponse? {
        var conn: HttpURLConnection? = null
        return try {
            val encodedTitle = URLEncoder.encode(botanicalName.replace(" ", "_"), "UTF-8")
            val url = URL("https://en.wikipedia.org/api/rest_v1/page/summary/$encodedTitle")
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 6000
            conn.readTimeout = 6000
            conn.setRequestProperty("User-Agent", "HappyPlants/1.0 (Android Plant Care Assistant)")
            conn.setRequestProperty("Accept", "application/json")

            if (conn.responseCode in 200..299) {
                BufferedReader(InputStreamReader(conn.inputStream)).use { reader ->
                    gson.fromJson(reader.readText(), WikipediaSummaryResponse::class.java)
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    private fun inferLightCondition(family: String?, genus: String?): String {
        val f = family?.lowercase() ?: ""
        val g = genus?.lowercase() ?: ""

        return when {
            f.contains("cact") || f.contains("crassul") || g.contains("sansevieria") || g.contains("dracaena") ->
                "Bright indirect to direct light (2,500 – 6,000 lx). Very tolerant of varying indoor light."
            f.contains("araceae") || g.contains("monstera") || g.contains("epipremnum") || g.contains("philodendron") ->
                "Medium to bright indirect light (1,500 – 3,500 lx). Shield from harsh midday direct sun."
            f.contains("arecaceae") || g.contains("dypsis") || g.contains("chamaedorea") ->
                "Filtered indirect light (1,200 – 3,000 lx). Thrives near north or east-facing windows."
            f.contains("marantaceae") || g.contains("calathea") ->
                "Gentle dappled shade or medium indirect light (800 – 2,000 lx). Direct sun scorches foliage."
            else ->
                "Moderate to bright indirect sunlight (1,500 – 3,500 lx). Avoid harsh scorching direct afternoon sun."
        }
    }

    private fun inferMoistureCondition(family: String?, genus: String?): String {
        val f = family?.lowercase() ?: ""
        val g = genus?.lowercase() ?: ""

        return when {
            f.contains("cact") || f.contains("crassul") || g.contains("zamioculcas") || g.contains("dracaena") ->
                "Low moisture. Allow the soil to dry out completely between waterings (every 10–18 days). Highly prone to root rot if overwatered."
            f.contains("marantaceae") || f.contains("pteridaceae") || f.contains("polypodiaceae") ->
                "Consistent light moisture. Water when the top 1–2 cm begins to dry (every 3–5 days). Avoid letting roots sit in soggy water."
            else ->
                "Moderate moisture. The golden rule: water thoroughly only when top 2–3 cm of soil is dry (every 6–10 days in summer, 10–16 days in winter)."
        }
    }

    private fun inferSoilAdvice(family: String?, genus: String?): String {
        val f = family?.lowercase() ?: ""
        return when {
            f.contains("cact") || f.contains("crassul") ->
                "Fast-draining gritty succulent mix: 40% coarse river sand / perlite, 40% garden soil, 20% compost."
            else ->
                "Well-draining, airy indoor potting mix: 40% garden soil, 30% cocopeat for moisture retention, and 30% perlite or vermicompost."
        }
    }
}
