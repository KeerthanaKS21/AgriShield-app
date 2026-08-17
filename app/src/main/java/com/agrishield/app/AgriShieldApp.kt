package com.agrishield.app

import android.app.Application
import com.agrishield.app.data.firebase.FirebaseAuthManager
import com.agrishield.app.data.firebase.FirestoreManager
import com.agrishield.app.data.firebase.FirebaseStorageManager
import com.agrishield.app.data.ml.CropDiseaseClassifier
import com.agrishield.app.data.ml.CropRiskEngine
import com.agrishield.app.data.ml.FarmHealthCalculator
import com.agrishield.app.data.ml.IrrigationAdvisor
import com.agrishield.app.data.ml.SoilHealthEvaluator
import com.agrishield.app.data.repository.AgriBotRepository
import com.agrishield.app.data.repository.AuthRepository
import com.agrishield.app.data.repository.DiagnosisRepository
import com.agrishield.app.data.repository.FarmHealthRepository
import com.agrishield.app.data.repository.SoilRepository
import com.agrishield.app.data.repository.TimelineRepository
import com.agrishield.app.data.repository.WeatherRepository
import com.agrishield.app.data.speech.AgriSpeechRecognizer
import com.agrishield.app.utils.AgriNotificationHelper
import com.agrishield.app.utils.AppLanguageManager
import com.agrishield.app.utils.LocationHelper

class AgriShieldApp : Application() {

    // Firebase Managers
    lateinit var authManager: FirebaseAuthManager
    lateinit var firestoreManager: FirestoreManager
    lateinit var storageManager: FirebaseStorageManager

    // Machine Learning & Agronomic Engines
    lateinit var cropClassifier: CropDiseaseClassifier
    lateinit var riskEngine: CropRiskEngine
    lateinit var healthCalculator: FarmHealthCalculator
    lateinit var soilEvaluator: SoilHealthEvaluator
    lateinit var irrigationAdvisor: IrrigationAdvisor

    // Helpers
    lateinit var locationHelper: LocationHelper
    lateinit var notificationHelper: AgriNotificationHelper
    lateinit var speechRecognizer: AgriSpeechRecognizer

    // Repositories
    lateinit var authRepository: AuthRepository
    lateinit var diagnosisRepository: DiagnosisRepository
    lateinit var weatherRepository: WeatherRepository
    lateinit var agriBotRepository: AgriBotRepository
    lateinit var farmHealthRepository: FarmHealthRepository
    lateinit var soilRepository: SoilRepository
    lateinit var timelineRepository: TimelineRepository

    override fun onCreate() {
        super.onCreate()

        // 1. Initialize Helpers & Language
        AppLanguageManager.init(this)
        locationHelper = LocationHelper(this)
        notificationHelper = AgriNotificationHelper(this)
        speechRecognizer = AgriSpeechRecognizer(this)

        // 2. Initialize Firebase
        authManager = FirebaseAuthManager()
        firestoreManager = FirestoreManager()
        storageManager = FirebaseStorageManager()

        // 3. Initialize ML & Engines
        cropClassifier = CropDiseaseClassifier(this)
        riskEngine = CropRiskEngine()
        healthCalculator = FarmHealthCalculator()
        soilEvaluator = SoilHealthEvaluator()
        irrigationAdvisor = IrrigationAdvisor()

        // 4. Initialize Repositories
        authRepository = AuthRepository(authManager, firestoreManager)
        diagnosisRepository = DiagnosisRepository(cropClassifier, firestoreManager, storageManager)
        weatherRepository = WeatherRepository()
        agriBotRepository = AgriBotRepository()
        farmHealthRepository = FarmHealthRepository(healthCalculator, riskEngine, irrigationAdvisor)
        soilRepository = SoilRepository(soilEvaluator, firestoreManager)
        timelineRepository = TimelineRepository(firestoreManager)
    }
}
