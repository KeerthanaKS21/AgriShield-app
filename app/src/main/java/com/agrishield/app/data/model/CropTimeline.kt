package com.agrishield.app.data.model

data class CropTimeline(
    val id: String = "",
    val cropName: String = "Tomato",
    val cropNameTa: String = "தக்காளி",
    val variety: String = "Hybrid US-440",
    val fieldPlotName: String = "Plot 1",
    val areaAcres: Double = 1.5,
    val sowingDateEpoch: Long = System.currentTimeMillis() - (25L * 24 * 60 * 60 * 1000), // 25 days ago
    val currentStage: GrowthStage = GrowthStage.VEGETATIVE,
    val expectedHarvestDateEpoch: Long = System.currentTimeMillis() + (65L * 24 * 60 * 60 * 1000),
    val locationName: String = "Coimbatore",
    val latitude: Double? = 11.0168,
    val longitude: Double? = 76.9558,
    val tasks: List<CareTask> = emptyList()
)

enum class GrowthStage {
    SOWING,
    SEEDLING,
    VEGETATIVE,
    FLOWERING,
    FRUITING,
    HARVEST
}

data class CareTask(
    val id: String = "",
    val title: String = "",
    val titleTa: String = "",
    val category: TaskCategory = TaskCategory.FERTILIZATION,
    val dueDateEpoch: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val notes: String = ""
)

enum class TaskCategory {
    IRRIGATION,
    FERTILIZATION,
    PEST_INSPECTION,
    DISEASE_SPRAY,
    WEEDING,
    HARVESTING
}

object CropTimelineTemplates {

    fun generateDefaultTasksForCrop(cropName: String, sowingDate: Long): List<CareTask> {
        val dayMs = 24L * 60 * 60 * 1000
        val cropLower = cropName.lowercase()

        return when {
            cropLower.contains("rice") || cropLower.contains("paddy") || cropLower.contains("நெல்") -> listOf(
                CareTask(
                    id = "task_paddy_1",
                    title = "Basal Application: DAP (50 kg) + Zinc Sulphate (10 kg)",
                    titleTa = "அடி உரம் இடுதல்: டி.ஏ.பி (50 கிலோ) + துத்தநாக சல்பேட் (10 கிலோ)",
                    category = TaskCategory.FERTILIZATION,
                    dueDateEpoch = sowingDate + (3 * dayMs),
                    isCompleted = true
                ),
                CareTask(
                    id = "task_paddy_2",
                    title = "Maintain 2-3 cm Standing Water & Weed Inspection",
                    titleTa = "2-3 செ.மீ நீர் மட்டம் பராமரித்தல் மற்றும் களை மேலாண்மை",
                    category = TaskCategory.IRRIGATION,
                    dueDateEpoch = sowingDate + (15 * dayMs),
                    isCompleted = true
                ),
                CareTask(
                    id = "task_paddy_3",
                    title = "Tillering Stage: Top-dress Urea (30 kg/acre)",
                    titleTa = "தூர்கட்டும் பருவம்: மேலுரமாக யூரியா (30 கிலோ/ஏக்கர்)",
                    category = TaskCategory.FERTILIZATION,
                    dueDateEpoch = sowingDate + (30 * dayMs),
                    isCompleted = false
                ),
                CareTask(
                    id = "task_paddy_4",
                    title = "Scout for Leaf Folder & Stem Borer + Pheromone Traps",
                    titleTa = "இலை சுருட்டு புழு & தண்டு துளைப்பான் கண்காணிப்பு + இனக்கவர்ச்சி பொறி",
                    category = TaskCategory.PEST_INSPECTION,
                    dueDateEpoch = sowingDate + (45 * dayMs),
                    isCompleted = false
                ),
                CareTask(
                    id = "task_paddy_5",
                    title = "Panicle Initiation: Potassium (MOP 20 kg) & Blast Prevention",
                    titleTa = "கதிர் உருவாகும் பருவம்: பொட்டாஷ் (20 கிலோ) & குலைநோய் தடுப்பு",
                    category = TaskCategory.DISEASE_SPRAY,
                    dueDateEpoch = sowingDate + (65 * dayMs),
                    isCompleted = false
                ),
                CareTask(
                    id = "task_paddy_6",
                    title = "Drain Field 10 Days Before Grain Harvest",
                    titleTa = "அறுவடைக்கு 10 நாட்களுக்கு முன் வயல் நீரை வடித்தல்",
                    category = TaskCategory.HARVESTING,
                    dueDateEpoch = sowingDate + (105 * dayMs),
                    isCompleted = false
                )
            )

            cropLower.contains("chilli") || cropLower.contains("மிளகாய்") -> listOf(
                CareTask(
                    id = "task_chilli_1",
                    title = "Basal Fertilizer: FYM 5 tons + Neem Cake 100 kg",
                    titleTa = "அடி உரம்: தொழுவுரம் 5 டன் + வேப்பம் புண்ணாக்கு 100 கிலோ",
                    category = TaskCategory.FERTILIZATION,
                    dueDateEpoch = sowingDate + (2 * dayMs),
                    isCompleted = true
                ),
                CareTask(
                    id = "task_chilli_2",
                    title = "Set up Yellow & Blue Sticky Traps for Thrips & Whiteflies",
                    titleTa = "இலைப்பேன் மற்றும் வெள்ளை ஈக்களுக்கு மஞ்சள் & நீல நிற ஒட்டும் பொறிகள் அமைத்தல்",
                    category = TaskCategory.PEST_INSPECTION,
                    dueDateEpoch = sowingDate + (14 * dayMs),
                    isCompleted = true
                ),
                CareTask(
                    id = "task_chilli_3",
                    title = "Foliar Spray: Pseudomonas fluorescens @ 5g/L",
                    titleTa = "இலைவழி தெளிப்பு: சூடோமோனாஸ் புளோரசன்ஸ் 5 கிராம்/லிட்டர்",
                    category = TaskCategory.DISEASE_SPRAY,
                    dueDateEpoch = sowingDate + (28 * dayMs),
                    isCompleted = false
                ),
                CareTask(
                    id = "task_chilli_4",
                    title = "Flowering Spray: Boron (20%) @ 1g/L + 13:0:45 @ 5g/L",
                    titleTa = "பூக்கும் பருவம்: போரான் (1 கிராம்/லி) + பொட்டாசியம் நைட்ரேட் (5 கிராம்/லி)",
                    category = TaskCategory.FERTILIZATION,
                    dueDateEpoch = sowingDate + (45 * dayMs),
                    isCompleted = false
                ),
                CareTask(
                    id = "task_chilli_5",
                    title = "First Green Chilli Picking & Mild Irrigation",
                    titleTa = "முதல் பச்சை மிளகாய் அறுவடை & மிதமான பாசனம்",
                    category = TaskCategory.HARVESTING,
                    dueDateEpoch = sowingDate + (75 * dayMs),
                    isCompleted = false
                )
            )

            cropLower.contains("cotton") || cropLower.contains("பருத்தி") -> listOf(
                CareTask(
                    id = "task_cotton_1",
                    title = "Basal Dressing: NPK (20:20:0:13) + Trichoderma viride",
                    titleTa = "அடி உரம்: என்.பி.கே உரம் + டிரைக்கோடெர்மா விரிடி விதை நேர்த்தி",
                    category = TaskCategory.FERTILIZATION,
                    dueDateEpoch = sowingDate + (3 * dayMs),
                    isCompleted = true
                ),
                CareTask(
                    id = "task_cotton_2",
                    title = "Square Formation: Gap filling & Thinning to 1 plant/hill",
                    titleTa = "சதுர அரும்பு பருவம்: பயிர் கலைத்தல் மற்றும் இடைவெளி பராமரிப்பு",
                    category = TaskCategory.WEEDING,
                    dueDateEpoch = sowingDate + (25 * dayMs),
                    isCompleted = true
                ),
                CareTask(
                    id = "task_cotton_3",
                    title = "Bollworm Monitoring: Install 5 Pheromone Traps/acre",
                    titleTa = "காய்ப்புழு கண்காணிப்பு: ஏக்கருக்கு 5 இனக்கவர்ச்சி பொறி அமைத்தல்",
                    category = TaskCategory.PEST_INSPECTION,
                    dueDateEpoch = sowingDate + (50 * dayMs),
                    isCompleted = false
                ),
                CareTask(
                    id = "task_cotton_4",
                    title = "Foliar Spray: NAA (Planofix) 4.5ml/10L to prevent boll dropping",
                    titleTa = "பூ மற்றும் பிஞ்சு உதிர்வதைத் தடுக்க பிளனோபிக்ஸ் தெளித்தல்",
                    category = TaskCategory.FERTILIZATION,
                    dueDateEpoch = sowingDate + (70 * dayMs),
                    isCompleted = false
                ),
                CareTask(
                    id = "task_cotton_5",
                    title = "First Boll Bursting & Clean Cotton Picking",
                    titleTa = "முதல் காய் வெடித்தல் & சுத்தமான பருத்தி அறுவடை",
                    category = TaskCategory.HARVESTING,
                    dueDateEpoch = sowingDate + (120 * dayMs),
                    isCompleted = false
                )
            )

            cropLower.contains("banana") || cropLower.contains("வாழை") -> listOf(
                CareTask(
                    id = "task_banana_1",
                    title = "Pit Preparation: FYM (10 kg) + Neem Cake (250g) + Carbofuran",
                    titleTa = "குழி தயார் செய்தல்: தொழுவுரம் 10 கிலோ + வேப்பம் புண்ணாக்கு 250 கிராம்",
                    category = TaskCategory.FERTILIZATION,
                    dueDateEpoch = sowingDate + (5 * dayMs),
                    isCompleted = true
                ),
                CareTask(
                    id = "task_banana_2",
                    title = "3rd Month: Nitrogen & Potash Dose (50g Urea + 75g MOP/plant)",
                    titleTa = "3-ஆம் மாதம்: 50 கிராம் யூரியா + 75 கிராம் பொட்டாஷ் இடுதல்",
                    category = TaskCategory.FERTILIZATION,
                    dueDateEpoch = sowingDate + (90 * dayMs),
                    isCompleted = false
                ),
                CareTask(
                    id = "task_banana_3",
                    title = "De-suckering (Remove side shoots) & Earthing Up",
                    titleTa = "பக்கக் கன்றுகளை நீக்குதல் மற்றும் மண் அணைத்தல்",
                    category = TaskCategory.WEEDING,
                    dueDateEpoch = sowingDate + (120 * dayMs),
                    isCompleted = false
                ),
                CareTask(
                    id = "task_banana_4",
                    title = "Sigatoka Leaf Spot Prevention: Copper Oxychloride 2.5g/L",
                    titleTa = "சிகாடோகா இலைப்புள்ளி நோய் தடுப்பு: காப்பர் ஆக்ஸிகுளோரைடு தெளித்தல்",
                    category = TaskCategory.DISEASE_SPRAY,
                    dueDateEpoch = sowingDate + (180 * dayMs),
                    isCompleted = false
                ),
                CareTask(
                    id = "task_banana_5",
                    title = "Bunch Emergence: Denavelling & Propping with Casuarina poles",
                    titleTa = "குலை தள்ளும் பருவம்: ஆண் பூ மொட்டு நீக்குதல் & முட்டுக் கொடுத்தல்",
                    category = TaskCategory.HARVESTING,
                    dueDateEpoch = sowingDate + (240 * dayMs),
                    isCompleted = false
                )
            )

            cropLower.contains("sugarcane") || cropLower.contains("கரும்பு") -> listOf(
                CareTask(
                    id = "task_cane_1",
                    title = "Furrow Basal Application: Single Super Phosphate (150 kg/acre)",
                    titleTa = "பார் சால் அடி உரம்: சூப்பர் பாஸ்பேட் (150 கிலோ/ஏக்கர்)",
                    category = TaskCategory.FERTILIZATION,
                    dueDateEpoch = sowingDate + (5 * dayMs),
                    isCompleted = true
                ),
                CareTask(
                    id = "task_cane_2",
                    title = "Early Shoot Borer Management: Sealer spray & Granulosis Virus",
                    titleTa = "இடைக்குருத்து புழு மேலாண்மை மற்றும் தழைக்கூளம் இடுதல்",
                    category = TaskCategory.PEST_INSPECTION,
                    dueDateEpoch = sowingDate + (35 * dayMs),
                    isCompleted = true
                ),
                CareTask(
                    id = "task_cane_3",
                    title = "90 Days: Final Earthing Up & Urea + Potash Top-dressing",
                    titleTa = "90-ஆம் நாள்: பெரும் மண் அணைத்தல் & யூரியா + பொட்டாஷ் மேலுரம்",
                    category = TaskCategory.FERTILIZATION,
                    dueDateEpoch = sowingDate + (90 * dayMs),
                    isCompleted = false
                ),
                CareTask(
                    id = "task_cane_4",
                    title = "Trash Mulching between Rows to Conserve Soil Moisture",
                    titleTa = "கரும்பு சோகைகளை வரிசைகளுக்கு இடையே பரப்பி ஈரம் காத்தல்",
                    category = TaskCategory.IRRIGATION,
                    dueDateEpoch = sowingDate + (150 * dayMs),
                    isCompleted = false
                ),
                CareTask(
                    id = "task_cane_5",
                    title = "Brix Refractometer Testing & Sugar Mill Harvest Scheduling",
                    titleTa = "சர்க்கரை அளவு (Brix) பரிசோதித்தல் & அறுவடை திட்டமிடல்",
                    category = TaskCategory.HARVESTING,
                    dueDateEpoch = sowingDate + (330 * dayMs),
                    isCompleted = false
                )
            )

            // Default Tomato / General Horticultural Crops
            else -> listOf(
                CareTask(
                    id = "task_gen_1",
                    title = "Apply Basal Fertilizer (DAP + FYM + Bio-fertilizer)",
                    titleTa = "அடி உரம் இடுதல் (டி.ஏ.பி + மண்புழு உரம் + உயிர் உரம்)",
                    category = TaskCategory.FERTILIZATION,
                    dueDateEpoch = sowingDate + (3 * dayMs),
                    isCompleted = true
                ),
                CareTask(
                    id = "task_gen_2",
                    title = "First Weeding & Root-zone Earthing Up",
                    titleTa = "முதல் களை எடுத்தல் மற்றும் வேர் பகுதியில் மண் அணைத்தல்",
                    category = TaskCategory.WEEDING,
                    dueDateEpoch = sowingDate + (18 * dayMs),
                    isCompleted = true
                ),
                CareTask(
                    id = "task_gen_3",
                    title = "Preventive Spray: Neem Oil (1500 ppm) @ 4 ml/L",
                    titleTa = "முன்னெச்சரிக்கை இயற்கை தெளிப்பு: வேப்பெண்ணெய் 4 மி.லி/லி",
                    category = TaskCategory.PEST_INSPECTION,
                    dueDateEpoch = sowingDate + (30 * dayMs),
                    isCompleted = false
                ),
                CareTask(
                    id = "task_gen_4",
                    title = "Flowering Booster: 19:19:19 (5g/L) + Micronutrients (1g/L)",
                    titleTa = "பூக்கும் பருவம்: 19:19:19 நீரில் கரையும் உரம் (5 கிராம்/லி) தெளிப்பு",
                    category = TaskCategory.FERTILIZATION,
                    dueDateEpoch = sowingDate + (45 * dayMs),
                    isCompleted = false
                ),
                CareTask(
                    id = "task_gen_5",
                    title = "Fruiting Stage: Potassium Nitrate (13:0:45) @ 5g/L",
                    titleTa = "காய்க்கும் பருவம்: பொட்டாசியம் நைட்ரேட் (5 கிராம்/லி) தெளிப்பு",
                    category = TaskCategory.FERTILIZATION,
                    dueDateEpoch = sowingDate + (60 * dayMs),
                    isCompleted = false
                ),
                CareTask(
                    id = "task_gen_6",
                    title = "First Crop Harvest & Grading for Market",
                    titleTa = "முதல் கட்ட பயிர் அறுவடை & சந்தைக்கான தரம் பிரித்தல்",
                    category = TaskCategory.HARVESTING,
                    dueDateEpoch = sowingDate + (75 * dayMs),
                    isCompleted = false
                )
            )
        }
    }
}
