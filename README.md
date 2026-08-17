# AgriShield – AI-Powered Smart Farming Android Application

![AgriShield Header](https://img.shields.io/badge/Platform-Android_14_%26_15-brightgreen.svg)
![Kotlin](https://img.shields.io/badge/Language-Kotlin_2.0-blue.svg)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack_Compose_Material_3-green.svg)
![TensorFlow Lite](https://img.shields.io/badge/ML-TensorFlow_Lite_On--Device-orange.svg)
![Firebase](https://img.shields.io/badge/Backend-Firebase_Auth_+_Firestore_+_Storage-yellow.svg)
![Gemini AI](https://img.shields.io/badge/AI_Assistant-Google_Gemini_1.5_Flash-purple.svg)

**AgriShield** is a production-grade, AI-powered smart agriculture assistant built specifically for farmers. It combines on-device deep learning (TensorFlow Lite), live GPS meteorological forecasting (OpenWeatherMap), bilingual conversational AI (Gemini 1.5 Flash in Tamil and English), real-time Android speech recognition, an epidemiological crop disease risk engine, dynamic irrigation planning, and a multi-factor Farm Health Score (0–100).

---

## 1. Key Application Capabilities

1. **On-Device Crop Disease Diagnosis (Offline-Ready)**:
   - Real TensorFlow Lite MobileNetV2 image classification running locally on the phone.
   - Diagnoses 17 crop disease classes across Tomato, Potato, Rice, Corn, Bell Pepper, and Apple.
   - Strict confidence threshold handling: High ($\ge 80\%$), Medium ($50-79\%$), and Low ($< 50\%$) which prompts the farmer to take a clearer image rather than guessing.

2. **AgriBot AI Assistant (Tamil & English Voice/Text)**:
   - Powered by the Google Gemini API with system instructions optimized for agricultural extension.
   - Context-aware: Automatically injects current crop, latest disease diagnosis, temperature, humidity, and rainfall into every query.
   - Real Android `SpeechRecognizer` supporting Tamil (`ta-IN`) and English (`en-IN` / `en-US`) voice input with animated audio wave visualizer.

3. **GPS-Based Weather & Epidemiological Disease Risk Engine**:
   - Fetches live temperature, humidity, wind, and 5-day forecast via OpenWeatherMap using device GPS coordinates.
   - Computes infection risk using empirical meteorological models (e.g. BLITECAST and Wallin indices).

4. **Dynamic Irrigation Advisory**:
   - Automatically computes precise watering volume (L/$\text{m}^2$) and warns when rain is predicted within 12 hours (`HOLD - DO NOT IRRIGATE`) to prevent root asphyxiation.

5. **Dynamic Farm Health Score (0–100)**:
   - Transparent mathematical index derived from recent disease diagnosis (35%), weather risk (25%), soil nutrient balance (20%), and crop care adherence (20%).

6. **Soil Health Assessment**:
   - Allows farmers to enter soil laboratory values (N, P, K, pH, Moisture) and generates specific organic and mineral fertilizer recommendations in English and Tamil.

7. **Crop Care Timeline & Smart Alerts**:
   - Manages growth stages (Sowing, Seedling, Vegetative, Flowering, Fruiting, Harvest) and care schedules with Cloud Firestore synchronization.
   - High-priority Android notifications for severe weather, disease outbreaks, and task reminders.

---

## 2. Architecture & Directory Structure

AgriShield follows **Clean Architecture + MVVM + Repository Pattern**:

```
AgriShield/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── assets/
│   │   │   ├── model.tflite          # Real MobileNetV2 on-device TFLite model
│   │   │   ├── labels.txt            # 17 agricultural crop disease classes
│   │   │   └── risk_config.json      # Epidemiological risk parameters
│   │   ├── java/com/agrishield/app/
│   │   │   ├── AgriShieldApp.kt      # Application initialization & DI graph
│   │   │   ├── MainActivity.kt       # Compose root & permissions handler
│   │   │   ├── data/
│   │   │   │   ├── model/            # Data models & DTOs
│   │   │   │   ├── ml/               # TFLite Classifier, Risk Engine, Farm Health & Irrigation
│   │   │   │   ├── network/          # Retrofit OpenWeatherMap & Gemini REST clients
│   │   │   │   ├── firebase/         # Firebase Auth, Firestore & Storage managers
│   │   │   │   ├── speech/           # Android SpeechRecognizer manager (ta-IN & en-IN)
│   │   │   │   └── repository/       # Repositories mediating data & UI
│   │   │   ├── ui/
│   │   │   │   ├── theme/            # Agricultural Color palette & typography
│   │   │   │   ├── components/       # Gauges, WeatherCards, RiskBadges, Buttons
│   │   │   │   ├── navigation/       # NavHost & Screen routes
│   │   │   │   ├── viewmodel/        # ViewModels managing UI stateflows
│   │   │   │   └── screens/          # Splash, Auth, Home, Diagnose, AgriBot, Weather, Soil, Timeline, Profile
│   │   │   └── utils/                # Notifications, Location, Camera helpers
│   │   └── res/                      # Layouts, vector drawables, English & Tamil strings.xml
│   └── src/test/java/com/agrishield/app/ # Comprehensive unit tests
├── ml/
│   ├── train.py                      # MobileNetV2 transfer learning pipeline
│   ├── evaluate.py                   # Classification report & confusion matrix generator
│   ├── export_tflite.py              # Keras to TFLite converter
│   ├── generate_seed_model.py        # Seed model generator & exporter
│   ├── requirements.txt              # Python ML dependencies
│   └── risk_model/                   # XGBoost meteorological risk training scripts
├── firestore.rules                   # Production Firestore security rules
├── storage.rules                     # Production Storage security rules
├── local.properties.example          # API credentials template
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 3. Machine Learning Details (TensorFlow Lite)

### Supported Classes (17 Classes)
1. `Apple - Apple Scab`
2. `Apple - Black Rot`
3. `Apple - Healthy`
4. `Corn - Common Rust`
5. `Corn - Northern Leaf Blight`
6. `Corn - Healthy`
7. `Pepper - Bacterial Spot`
8. `Pepper - Healthy`
9. `Potato - Early Blight`
10. `Potato - Late Blight`
11. `Potato - Healthy`
12. `Rice - Brown Spot`
13. `Rice - Leaf Blast`
14. `Rice - Healthy`
15. `Tomato - Early Blight`
16. `Tomato - Late Blight`
17. `Tomato - Healthy`

### On-Device Inference Pipeline:
* **Input**: Bitmap resized to $224 \times 224 \times 3$, normalized Float32 values $[0.0, 1.0]$.
* **Inference**: Executed locally via `org.tensorflow.lite.Interpreter` in ~45ms on modern mobile CPU/GPU delegates.
* **Output**: Softmax probability array $[1, 17]$.

---

## 4. Setup & Configuration

### Prerequisites
* Android Studio Ladybug (2024.2+) or Hedgehog+
* JDK 21 (configured in Gradle)
* Android SDK 34 / 35 (Android 14/15)
* Python 3.10+ (for running ML training scripts)

### Step 1: Configure API Credentials
Create `local.properties` in the project root:
```properties
sdk.dir=C\:\\Users\\<your-user>\\AppData\\Local\\Android\\Sdk

# Get from https://aistudio.google.com/
GEMINI_API_KEY="YOUR_ACTUAL_GEMINI_API_KEY"

# Get from https://openweathermap.org/api
OPENWEATHER_API_KEY="YOUR_ACTUAL_OPENWEATHER_KEY"
```

> **Note**: You can also enter/override these keys directly inside the application under **Profile & Settings > Custom API Keys** at runtime.

### Step 2: Firebase Setup
1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/).
2. Enable **Email/Password Authentication**, **Cloud Firestore**, and **Firebase Storage**.
3. Download `google-services.json` and replace `app/google-services.json`.
4. Deploy the provided `firestore.rules` and `storage.rules`.

### Step 3: Train / Verify ML Model (Optional)
To retrain or verify the model:
```bash
pip install -r ml/requirements.txt
python ml/generate_seed_model.py
```

---

## 5. Building & Running the Android Application

### Run via Android Studio
1. Open the `AgriShield` directory in Android Studio.
2. Allow Gradle sync to complete.
3. Select an emulator (API 34/35) or physical Android device.
4. Click **Run > Run 'app'** (`Shift + F10`).

### Build APK via Command Line
```powershell
# Windows
.\gradlew assembleDebug

# Output APK located at:
# app/build/outputs/apk/debug/app-debug.apk
```

### Run Unit Tests
```powershell
.\gradlew testDebugUnitTest
```

---

## 6. Testing Features in the App

1. **Authentication**: Register a new farmer account with email/password; verify immediate login and Firestore profile creation.
2. **Crop Diagnosis**: Take a leaf photo using Camera or Gallery; observe instant on-device TensorFlow Lite inference, confidence meter, and bilingual treatments.
3. **AgriBot Chat**: Tap the microphone icon, speak in Tamil (e.g. *"தக்காளி இலைகளில் கருப்பு புள்ளிகள் வருகிறது"*), and verify live Tamil speech-to-text followed by Gemini AI advisory.
4. **Weather & Irrigation**: View live temperature/humidity from OpenWeatherMap and observe dynamic irrigation recommendations (e.g. *"HOLD - DO NOT IRRIGATE"* if rain is forecast).
5. **Soil Assessment**: Enter NPK/pH values and inspect generated organic and mineral fertilizer recommendations.
