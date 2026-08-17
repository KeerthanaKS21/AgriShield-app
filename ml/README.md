# AgriShield Machine Learning Subsystem

This directory contains the training, evaluation, and export pipeline for the **AgriShield Crop Disease Detection & Risk Prediction Engine**.

---

## 1. Supported Crop Disease Classes (17 Classes)

The model is trained on 17 agricultural crop disease classes across major staple and cash crops:

| No. | Crop | Disease / Condition | Classification Class |
|---|---|---|---|
| 1 | Apple | Apple Scab (*Venturia inaequalis*) | `Apple - Apple Scab` |
| 2 | Apple | Black Rot (*Botryosphaeria obtusa*) | `Apple - Black Rot` |
| 3 | Apple | Healthy Leaf | `Apple - Healthy` |
| 4 | Corn (Maize) | Common Rust (*Puccinia sorghi*) | `Corn - Common Rust` |
| 5 | Corn (Maize) | Northern Leaf Blight (*Exserohilum*) | `Corn - Northern Leaf Blight` |
| 6 | Corn (Maize) | Healthy Leaf | `Corn - Healthy` |
| 7 | Pepper Bell | Bacterial Spot (*Xanthomonas*) | `Pepper - Bacterial Spot` |
| 8 | Pepper Bell | Healthy Leaf | `Pepper - Healthy` |
| 9 | Potato | Early Blight (*Alternaria solani*) | `Potato - Early Blight` |
| 10 | Potato | Late Blight (*Phytophthora infestans*) | `Potato - Late Blight` |
| 11 | Potato | Healthy Leaf | `Potato - Healthy` |
| 12 | Rice | Brown Spot (*Bipolaris oryzae*) | `Rice - Brown Spot` |
| 13 | Rice | Leaf Blast (*Magnaporthe oryzae*) | `Rice - Leaf Blast` |
| 14 | Rice | Healthy Leaf | `Rice - Healthy` |
| 15 | Tomato | Early Blight (*Alternaria solani*) | `Tomato - Early Blight` |
| 16 | Tomato | Late Blight (*Phytophthora infestans*) | `Tomato - Late Blight` |
| 17 | Tomato | Healthy Leaf | `Tomato - Healthy` |

---

## 2. Directory Structure

```
ml/
├── train.py                  # MobileNetV2 transfer learning training pipeline
├── evaluate.py               # Evaluates model metrics and generates confusion matrix
├── export_tflite.py          # Exports .keras to optimized .tflite format
├── generate_seed_model.py    # Generates initial verified model.tflite for Android assets
├── requirements.txt          # Python dependencies
├── model_metrics.json        # Evaluation metrics for Model Info screen
├── labels.txt                # Exported class labels
└── risk_model/
    ├── train_risk_model.py   # Trains XGBoost climate-disease risk model
    ├── evaluate_risk_model.py# Feature importance & evaluation
    └── export_model.py       # Exports agronomic risk parameters
```

---

## 3. Training Instructions

### Step 1: Install Dependencies
```bash
pip install -r ml/requirements.txt
```

### Step 2: Prepare Dataset
Download the PlantVillage dataset or place your cropped leaf images in `ml/data/dataset/` organized by class:
```
data/dataset/
├── Tomato___Early_blight/
├── Tomato___Late_blight/
├── Tomato___healthy/
...
```

### Step 3: Run Training Pipeline
```bash
python ml/train.py --dataset ml/data/dataset --output ml/output
```

### Step 4: Evaluate Model
```bash
python ml/evaluate.py --model ml/output/crop_disease_model.keras --dataset ml/data/dataset --output ml/output/eval
```

### Step 5: Export to Android TFLite
```bash
python ml/export_tflite.py --model ml/output/crop_disease_model.keras --output app/src/main/assets/model.tflite --fp16
```

---

## 4. On-Device TFLite Model Specifications

* **Input Tensor**: `[1, 224, 224, 3]`, `float32`, Normalized to `[0.0, 1.0]` (RGB)
* **Output Tensor**: `[1, 17]`, `float32`, Softmax probabilities
* **Confidence Thresholds**:
  * High: $\ge 80\%$
  * Medium: $50\% - 79\%$
  * Low: $< 50\%$ (Prompts farmer to retake image with better lighting)
