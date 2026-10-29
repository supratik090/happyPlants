package com.hos.happy.plants.data.remote

import android.graphics.Bitmap
import com.google.gson.Gson
import com.hos.happy.plants.data.remote.model.PlantDiseaseResult
import com.hos.happy.plants.data.remote.model.PlantNetDiseaseResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Service for the PlantNet Diseases API.
 * Endpoint: POST https://my-api.plantnet.org/v2/diseases/identify
 * Up to 5 images per request; all images must be of the same individual plant.
 */
class PlantNetDiseaseService {

    private val gson = Gson()

    /**
     * Identify probable diseases from up to 3 plant photos.
     *
     * @param bitmaps  1–3 bitmaps of the sick plant (same individual, different angles/parts)
     * @param apiKey   PlantNet API key
     * @return         Result wrapping a list of [PlantDiseaseResult] sorted by confidence (desc)
     */
    suspend fun identifyDiseases(
        bitmaps: List<Bitmap>,
        apiKey: String
    ): Result<List<PlantDiseaseResult>> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException("Pl@ntNet API Key is missing.")
            )
        }
        if (bitmaps.isEmpty()) {
            return@withContext Result.failure(
                IllegalArgumentException("Please add at least one photo before scanning.")
            )
        }

        try {
            val boundary = "---PlantNetDiseaseBoundary${System.currentTimeMillis()}"
            val lineEnd = "\r\n"
            val twoHyphens = "--"

            val urlStr = "https://my-api.plantnet.org/v2/diseases/identify" +
                    "?api-key=${apiKey.trim()}" +
                    "&include-related-images=true" +
                    "&no-reject=false" +
                    "&nb-results=5" +
                    "&lang=en"

            val url = URL(urlStr)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 20_000
            conn.readTimeout = 30_000
            conn.doInput = true
            conn.doOutput = true
            conn.useCaches = false
            conn.setRequestProperty("Connection", "Keep-Alive")
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

            DataOutputStream(conn.outputStream).use { output ->
                bitmaps.forEachIndexed { index, bitmap ->
                    // Scale down if oversized (max 1600px to keep upload fast)
                    val scaledBitmap = if (bitmap.width > 1600 || bitmap.height > 1600) {
                        val ratio = 1600f / maxOf(bitmap.width, bitmap.height)
                        Bitmap.createScaledBitmap(
                            bitmap,
                            (bitmap.width * ratio).toInt(),
                            (bitmap.height * ratio).toInt(),
                            true
                        )
                    } else bitmap

                    val stream = ByteArrayOutputStream()
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                    val imageBytes = stream.toByteArray()

                    // --- image field ---
                    output.writeBytes(twoHyphens + boundary + lineEnd)
                    output.writeBytes(
                        "Content-Disposition: form-data; name=\"images\"; filename=\"disease_${index}.jpg\"$lineEnd"
                    )
                    output.writeBytes("Content-Type: image/jpeg$lineEnd")
                    output.writeBytes(lineEnd)
                    output.write(imageBytes)
                    output.writeBytes(lineEnd)

                    // --- organs field (one per image) ---
                    output.writeBytes(twoHyphens + boundary + lineEnd)
                    output.writeBytes("Content-Disposition: form-data; name=\"organs\"$lineEnd")
                    output.writeBytes(lineEnd)
                    output.writeBytes("auto$lineEnd")
                }

                // End boundary
                output.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)
                output.flush()
            }

            val responseCode = conn.responseCode
            if (responseCode in 200..299) {
                val responseText =
                    BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                val diseaseResponse =
                    gson.fromJson(responseText, PlantNetDiseaseResponse::class.java)

                if (diseaseResponse.results.isEmpty()) {
                    return@withContext Result.failure(
                        Exception(
                            "No diseases detected. The plant may be healthy, or try photos showing " +
                                    "affected leaves, spots, or discoloration more clearly."
                        )
                    )
                }

                val domainResults = diseaseResponse.results.map { result ->
                    val descriptionText = result.description?.name
                    val eppoCode = result.name
                    val humanName = descriptionText ?: eppoCodeToHumanName(eppoCode)
                    val confidence = (result.score * 100).toInt().coerceIn(1, 99)
                    val imageUrl = result.images.firstOrNull()?.url?.bestUrl

                    PlantDiseaseResult(
                        eppoCode = eppoCode,
                        diseaseName = humanName,
                        confidencePercent = confidence,
                        description = result.description?.name,
                        infoUrl = result.description?.url,
                        referenceImageUrl = imageUrl,
                        treatmentAdvice = inferTreatmentAdvice(eppoCode, humanName)
                    )
                }

                Result.success(domainResults)
            } else {
                val errorText = try {
                    BufferedReader(InputStreamReader(conn.errorStream ?: conn.inputStream))
                        .use { it.readText() }
                } catch (e: Exception) {
                    "HTTP $responseCode"
                }

                val errorMessage = when {
                    // Out of quota or rate limited
                    responseCode == 429 -> "This feature is currently not available."
                    errorText.contains("quota", ignoreCase = true) ||
                    errorText.contains("limit", ignoreCase = true) ||
                    errorText.contains("credit", ignoreCase = true) -> "This feature is currently not available."

                    // Server down or error
                    responseCode in 500..599 -> "This feature is currently not available."
                    errorText.contains("unavailable", ignoreCase = true) ||
                    errorText.contains("down", ignoreCase = true) ||
                    errorText.contains("server error", ignoreCase = true) -> "This feature is currently not available."

                    // Auth failure vs general failure
                    responseCode in listOf(401, 403) -> {
                        if (errorText.contains("quota", ignoreCase = true) || errorText.contains("limit", ignoreCase = true)) {
                            "This feature is currently not available."
                        } else {
                            "Invalid or unauthorized Pl@ntNet API Key. Please verify your key at my.plantnet.org."
                        }
                    }
                    responseCode == 404 -> "No disease matched. Try a clearer, closer photo of the affected area (leaf spot, stem, or root)."
                    else -> "This feature is currently not available."
                }

                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // In case Pl@ntNet API is down, connection fails, or network is unavailable
            val message = when (e) {
                is java.net.ConnectException,
                is java.net.SocketTimeoutException,
                is java.net.UnknownHostException,
                is java.io.IOException -> "This feature is currently not available."
                else -> {
                    val raw = e.message ?: ""
                    if (raw.contains("quota", ignoreCase = true) ||
                        raw.contains("limit", ignoreCase = true) ||
                        raw.contains("timeout", ignoreCase = true) ||
                        raw.contains("connect", ignoreCase = true) ||
                        raw.contains("unreachable", ignoreCase = true) ||
                        raw.contains("unavailable", ignoreCase = true) ||
                        raw.contains("503") || raw.contains("502") || raw.contains("500")
                    ) {
                        "This feature is currently not available."
                    } else {
                        raw.ifBlank { "This feature is currently not available." }
                    }
                }
            }
            Result.failure(Exception(message))
        }
    }

    /**
     * Converts EPPO codes or partial names into more readable labels for common plant diseases.
     */
    private fun eppoCodeToHumanName(eppoCode: String): String {
        return when {
            eppoCode.contains("APHISP", ignoreCase = true) -> "Aphids (Aphididae)"
            eppoCode.contains("TETRAN", ignoreCase = true) -> "Spider Mites (Tetranychus)"
            eppoCode.contains("COCCF", ignoreCase = true) -> "Scale Insects (Coccidae)"
            eppoCode.contains("BOTRCI", ignoreCase = true) -> "Grey Mould (Botrytis cinerea)"
            eppoCode.contains("PHTEIN", ignoreCase = true) -> "Phytophthora Root Rot"
            eppoCode.contains("FUSARI", ignoreCase = true) -> "Fusarium Wilt / Root Rot"
            eppoCode.contains("POWERY", ignoreCase = true) || eppoCode.contains("ERYSIP", ignoreCase = true) -> "Powdery Mildew"
            eppoCode.contains("ALTERN", ignoreCase = true) -> "Alternaria Leaf Spot"
            eppoCode.contains("XANTHO", ignoreCase = true) -> "Bacterial Leaf Spot (Xanthomonas)"
            eppoCode.contains("SEPTRI", ignoreCase = true) -> "Septoria Leaf Spot"
            eppoCode.contains("THRIPS", ignoreCase = true) || eppoCode.contains("THYSANOP", ignoreCase = true) -> "Thrips Infestation"
            eppoCode.contains("ALEYRO", ignoreCase = true) -> "Whiteflies (Aleyrodidae)"
            eppoCode.contains("PSEUDO", ignoreCase = true) -> "Bacterial Blight (Pseudomonas)"
            eppoCode.contains("SCLER", ignoreCase = true) -> "Sclerotinia Stem Rot"
            eppoCode.contains("MEALYBUG", ignoreCase = true) || eppoCode.contains("PSEUDO", ignoreCase = true) -> "Mealybugs"
            else -> eppoCode.replace(Regex("[0-9]"), "").replace("_", " ").trim().ifBlank { eppoCode }
        }
    }

    /**
     * Infers practical treatment advice based on disease name or EPPO code.
     * Since the API does not provide structured treatment data, we use curated heuristics.
     */
    private fun inferTreatmentAdvice(eppoCode: String, humanName: String): String {
        val combined = "$eppoCode $humanName".lowercase()
        return when {
            combined.contains("aphid") ->
                "🌿 Wipe leaves with neem oil solution (5 ml per 1L water). Spray underside of leaves. Repeat weekly for 3 weeks. Introduce beneficial ladybirds if outdoors."

            combined.contains("spider mite") || combined.contains("tetran") ->
                "💦 Mist leaves with water daily to raise humidity. Apply diluted neem oil or insecticidal soap spray. Isolate the plant from others. Remove severely infested leaves."

            combined.contains("scale") || combined.contains("cocciд") || combined.contains("coccf") ->
                "🪶 Scrape off visible scales with a soft toothbrush soaked in rubbing alcohol. Spray with horticultural oil every 10 days. Isolate the plant."

            combined.contains("grey mould") || combined.contains("botryt") ->
                "✂️ Remove all infected leaves and flowers immediately. Improve air circulation and reduce humidity. Avoid overhead watering. Apply a copper-based fungicide if severe."

            combined.contains("root rot") || combined.contains("phytophth") || combined.contains("fusari") ->
                "🪴 Unpot the plant and trim all black/mushy roots. Let roots air-dry for 1–2 hours. Repot in fresh, well-draining soil (add perlite). Water sparingly and only when top 3 cm of soil is dry."

            combined.contains("powdery mildew") || combined.contains("erysip") ->
                "🍃 Remove affected leaves. Spray with a baking soda solution (1 tsp per 1L water + few drops of liquid soap). Ensure good airflow and avoid wetting foliage. Repeat every 5 days."

            combined.contains("leaf spot") || combined.contains("altern") || combined.contains("septor") ->
                "🌱 Remove infected leaves promptly. Avoid wetting foliage when watering. Apply a copper or sulfur-based fungicide. Ensure the plant has good air circulation."

            combined.contains("bacterial") || combined.contains("xantho") || combined.contains("pseudo") ->
                "🧴 Remove infected parts with sterilized scissors. Avoid overhead watering and leaf contact. Apply copper-based bactericide. Disinfect tools after use."

            combined.contains("thrip") ->
                "🪲 Use sticky blue/yellow traps. Spray with spinosad or neem oil solution. Remove severely affected buds and flowers. Repeat treatment every 5–7 days."

            combined.contains("whitefl") || combined.contains("aleyro") ->
                "🔆 Use yellow sticky traps. Spray undersides of leaves with insecticidal soap. Introduce beneficial insects (Encarsia) if available. Neem oil spray weekly."

            combined.contains("mealybug") ->
                "🩹 Dab individual mealybugs with a cotton swab soaked in 70% isopropyl alcohol. Spray the whole plant with diluted neem oil. Repeat every 7 days for 4 weeks. Isolate immediately."

            else ->
                "🔬 Isolate the plant from your other plants to prevent spread. Remove visibly damaged leaves with sterilized scissors. Check roots for rot (dark/mushy roots). Apply a general-purpose neem oil spray as a first measure. If the issue persists, consult a local nursery or plant vet."
        }
    }
}
