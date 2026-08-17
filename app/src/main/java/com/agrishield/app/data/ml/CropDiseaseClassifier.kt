package com.agrishield.app.data.ml

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import com.agrishield.app.data.model.ConfidenceLevel
import com.agrishield.app.data.model.Diagnosis
import com.agrishield.app.utils.Constants
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class CropDiseaseClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null
    private val labels = mutableListOf<String>()
    private val inputImageSize = Constants.MODEL_INPUT_SIZE // 224

    init {
        loadModelAndLabels()
    }

    private fun loadModelAndLabels() {
        try {
            // Load labels
            context.assets.open(Constants.LABELS_FILE).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line = reader.readLine()
                    while (line != null) {
                        if (line.isNotBlank()) {
                            labels.add(line.trim())
                        }
                        line = reader.readLine()
                    }
                }
            }

            // Load TFLite Model
            val assetFileDescriptor: AssetFileDescriptor = context.assets.openFd(Constants.MODEL_FILE)
            val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
            val fileChannel = fileInputStream.channel
            val startOffset = assetFileDescriptor.startOffset
            val declaredLength = assetFileDescriptor.declaredLength
            val modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

            val options = Interpreter.Options().apply {
                setNumThreads(4)
            }
            interpreter = Interpreter(modelBuffer, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isModelReady(): Boolean = interpreter != null && labels.isNotEmpty()

    fun getSupportedClasses(): List<String> = labels.toList()

    fun classifyImage(bitmap: Bitmap, imageUriString: String = ""): Diagnosis {
        val interp = interpreter
        if (interp == null || labels.isEmpty()) {
            return Diagnosis(
                id = java.util.UUID.randomUUID().toString(),
                crop = "Unknown",
                disease = "Model Not Initialized",
                confidence = 0f,
                confidenceLevel = ConfidenceLevel.LOW,
                explanation = "TensorFlow Lite engine is not initialized.",
                imageUrl = imageUriString
            )
        }

        // 1. Resize Bitmap to 224x224
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputImageSize, inputImageSize, true)

        // 2. Preprocess into Float32 ByteBuffer (Shape: [1, 224, 224, 3], Values: [0.0, 1.0])
        val inputBuffer = ByteBuffer.allocateDirect(1 * inputImageSize * inputImageSize * 3 * 4)
        inputBuffer.order(ByteOrder.nativeOrder())
        inputBuffer.rewind()

        val intValues = IntArray(inputImageSize * inputImageSize)
        resizedBitmap.getPixels(intValues, 0, resizedBitmap.width, 0, 0, resizedBitmap.width, resizedBitmap.height)

        var pixel = 0
        for (i in 0 until inputImageSize) {
            for (j in 0 until inputImageSize) {
                val value = intValues[pixel++]
                // Normalize RGB to [0.0, 1.0]
                val r = ((value shr 16) and 0xFF) / 255.0f
                val g = ((value shr 8) and 0xFF) / 255.0f
                val b = (value and 0xFF) / 255.0f

                inputBuffer.putFloat(r)
                inputBuffer.putFloat(g)
                inputBuffer.putFloat(b)
            }
        }

        // 3. Prepare Output Array (Shape: [1, num_classes])
        val numClasses = labels.size
        val outputArray = Array(1) { FloatArray(numClasses) }

        // 4. Run Inference
        interp.run(inputBuffer, outputArray)

        // 5. Extract top prediction
        val probabilities = outputArray[0]
        var maxIndex = 0
        var maxConfidence = 0.0f

        for (i in probabilities.indices) {
            if (probabilities[i] > maxConfidence) {
                maxConfidence = probabilities[i]
                maxIndex = i
            }
        }

        val rawLabel = labels[maxIndex]
        val parts = rawLabel.split(" - ", "___")
        val cropName = if (parts.size >= 2) parts[0].replace("_", " ").trim() else "Crop"
        val diseaseName = if (parts.size >= 2) parts[1].replace("_", " ").trim() else rawLabel

        // 6. Confidence Threshold Enforcement
        val confidenceLevel = when {
            maxConfidence >= Constants.CONFIDENCE_HIGH_THRESHOLD -> ConfidenceLevel.HIGH
            maxConfidence >= Constants.CONFIDENCE_MEDIUM_THRESHOLD -> ConfidenceLevel.MEDIUM
            else -> ConfidenceLevel.LOW
        }

        val isHealthy = diseaseName.contains("healthy", ignoreCase = true)
        val severity = when {
            isHealthy -> "None (Healthy)"
            confidenceLevel == ConfidenceLevel.HIGH -> "Severe/Active"
            confidenceLevel == ConfidenceLevel.MEDIUM -> "Moderate"
            else -> "Uncertain"
        }

        // Treatment details based on real agronomy
        val (treatmentEn, treatmentTa, explanation) = getAgronomicTreatment(cropName, diseaseName, isHealthy)

        return Diagnosis(
            id = java.util.UUID.randomUUID().toString(),
            crop = cropName,
            disease = if (confidenceLevel == ConfidenceLevel.LOW) "Low Confidence Detection ($diseaseName)" else diseaseName,
            confidence = maxConfidence * 100f,
            confidenceLevel = confidenceLevel,
            severity = severity,
            explanation = explanation,
            treatmentEn = treatmentEn,
            treatmentTa = treatmentTa,
            imageUrl = imageUriString,
            timestamp = System.currentTimeMillis()
        )
    }

    private fun getAgronomicTreatment(crop: String, disease: String, isHealthy: Boolean): Triple<String, String, String> {
        if (isHealthy) {
            return Triple(
                "Plant shows healthy vigorous foliage. Maintain regular drip irrigation schedule and apply balanced micronutrient spray every 15 days.",
                "பயிர் ஆரோக்கியமாக உள்ளது. வழக்கமான சொட்டு நீர்ப்பாசனத்தை தொடரவும் மற்றும் 15 நாட்களுக்கு ஒருமுறை நுண்ணூட்டச்சத்து தெளிக்கவும்.",
                "No active pathogens detected on the examined leaf sample."
            )
        }

        val diseaseLower = disease.lowercase()
        return when {
            diseaseLower.contains("early blight") -> Triple(
                "1. Spray Mancozeb 75% WP (2g/L) or Chlorothalonil (2g/L) on affected foliage.\n2. Remove lower infected leaves and avoid overhead sprinkler watering.\n3. Organic control: Spray 5% Neem Seed Kernel Extract (NSKE) or Trichoderma viride.",
                "1. மேன்கோசெப் 75% WP (2 கிராம்/லிட்டர்) அல்லது குளோரோதலோனில் தெளிக்கவும்.\n2. பாதிக்கப்பட்ட கீழ் இலைகளை அகற்றி அழிக்கவும்.\n3. இயற்கை முறை: 5% வேப்பங்கொட்டை சாறு அல்லது டிரைக்கோடெர்மா விரிடி தெளிக்கவும்.",
                "Alternaria solani fungal infection causing concentric brown/black spots with yellow halo on leaves."
            )
            diseaseLower.contains("late blight") -> Triple(
                "1. Immediate emergency spray with Metalaxyl + Mancozeb (2.5g/L) or Cymoxanil.\n2. Improve field drainage and destroy infected plant residues.\n3. Organic control: Copper Oxychloride (2.5g/L) as preventive spray.",
                "1. மெட்டலாக்சில் + மேன்கோசெப் (2.5 கிராம்/லிட்டர்) உடனடியாக தெளிக்கவும்.\n2. பண்ணையில் தண்ணீர் தேங்குவதை தவிர்த்து வடிகால் வசதி செய்யவும்.\n3. இயற்கை முறை: காப்பர் ஆக்ஸிகுளோரைடு (2.5 கிராம்/லிட்டர்) தெளிக்கவும்.",
                "Phytophthora infestans water mold infection causing rapid water-soaked dark lesions in humid conditions."
            )
            diseaseLower.contains("leaf blast") || diseaseLower.contains("blast") -> Triple(
                "1. Spray Tricyclazole 75% WP (0.6g/L) or Isoprothiolane (1.5ml/L).\n2. Avoid excessive nitrogen fertilizer application during cloudy weather.\n3. Maintain 2-3 cm shallow water level in the paddy field.",
                "1. டிரைசைக்ளசோல் 75% WP (0.6 கிராம்/லிட்டர்) தெளிக்கவும்.\n2. மேகமூட்டமான காலங்களில் அதிகப்படியான யூரியா இடுவதை தவிர்க்கவும்.\n3. வயலில் 2-3 செ.மீ சீரான நீர் அளவை பராமரிக்கவும்.",
                "Magnaporthe oryzae fungal blast causing spindle-shaped lesions with grey center."
            )
            diseaseLower.contains("bacterial spot") -> Triple(
                "1. Spray Streptomycin sulphate + Tetracycline (0.5g/10L) mixed with Copper Oxychloride (2g/L).\n2. Avoid working in the field when crop leaves are wet to prevent bacterial spread.\n3. Use certified disease-free seeds.",
                "1. ஸ்ட்ரெப்டோமைசின் சல்பேட் (0.5 கிராம்/10 லிட்டர்) உடன் காப்பர் ஆக்ஸிகுளோரைடு சேர்த்து தெளிக்கவும்.\n2. இலைகள் ஈரமாக இருக்கும்போது வேலை செய்வதைத் தவிர்க்கவும்.\n3. நோய் தாக்காத தரமான விதைகளைப் பயன்படுத்தவும்.",
                "Xanthomonas bacterial pathogen causing small water-soaked circular lesions."
            )
            diseaseLower.contains("rust") -> Triple(
                "1. Spray Propiconazole 25% EC (1ml/L) or Mancozeb (2g/L).\n2. Ensure proper crop spacing for aeration to reduce leaf surface wetness.",
                "1. புரோபிகோனசோல் 25% EC (1 மி.லி/லிட்டர்) அல்லது மேன்கோசெப் தெளிக்கவும்.\n2. பயிர்களுக்கு இடையே நல்ல காற்றோட்டம் இருக்குமாறு இடைவெளி விடவும்.",
                "Puccinia fungal rust causing raised reddish-brown or golden-orange powdery pustules."
            )
            diseaseLower.contains("scab") || diseaseLower.contains("black rot") -> Triple(
                "1. Spray Difenoconazole 25% EC (0.5ml/L) or Captan (2g/L).\n2. Prune dead twigs and remove fallen mummified fruits from orchard floor.",
                "1. டைபினோகோனசோல் (0.5 மி.லி/லிட்டர்) அல்லது கேப்டான் தெளிக்கவும்.\n2. காய்ந்த கிளைகளை கவாத்து செய்து கீழே விழுந்த பழங்களை அப்புறப்படுத்தவும்.",
                "Fungal pathogen causing dark olive-green or velvety lesions on foliage and fruits."
            )
            else -> Triple(
                "1. Apply broad-spectrum fungicide (Mancozeb 2g/L) or bio-fungicide (Pseudomonas fluorescens 5g/L).\n2. Isolate severely affected plants and consult AgriBot for tailored guidance.",
                "1. மேன்கோசெப் (2 கிராம்/லிட்டர்) அல்லது சூடோமோனாஸ் (5 கிராம்/லிட்டர்) தெளிக்கவும்.\n2. பாதிக்கப்பட்ட பயிர்களை தனிமைப்படுத்தி அக்ரிபாட்டிடம் கூடுதல் வழிகாட்டல் பெறவும்.",
                "Crop leaf pathogen detected. Prompt treatment recommended."
            )
        }
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}
