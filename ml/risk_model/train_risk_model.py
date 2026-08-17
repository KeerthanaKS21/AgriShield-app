"""
AgriShield - Crop Disease & Pest Risk ML Model
Trains an XGBoost/RandomForest Classifier on meteorological & agronomic indicators.
"""

import os
import json
import numpy as np
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report, accuracy_score
import xgboost as xgb

def generate_agronomic_dataset(n_samples: int = 5000) -> pd.DataFrame:
    """Generates synthetic agronomic dataset based on epidemiological leaf wetness & temperature curves."""
    np.random.seed(42)
    
    # Weather inputs
    temp = np.random.uniform(10.0, 42.0, n_samples)          # Celsius
    humidity = np.random.uniform(30.0, 100.0, n_samples)     # Relative humidity %
    rainfall = np.random.exponential(scale=8.0, size=n_samples) # mm / 24h
    wind = np.random.uniform(2.0, 35.0, n_samples)           # km/h
    consecutive_wet_hours = np.random.uniform(0.0, 24.0, n_samples)
    crop_susceptibility = np.random.choice([0.6, 0.7, 0.75, 0.8, 0.85, 0.9], n_samples)

    # Epidemiological risk equation (e.g. BLITECAST / Wallin infection index)
    # High fungal risk occurs when temp is between 18-28°C and humidity > 80% with high leaf wetness
    temp_risk = np.exp(-((temp - 24.0) ** 2) / (2 * (6.0 ** 2))) # Peak risk at 24°C
    humidity_risk = np.clip((humidity - 50.0) / 45.0, 0.0, 1.0)
    wetness_risk = np.clip(consecutive_wet_hours / 12.0, 0.0, 1.0)
    rain_risk = np.clip(rainfall / 20.0, 0.0, 1.0)

    composite_risk_score = (
        0.35 * (temp_risk * humidity_risk) +
        0.30 * wetness_risk +
        0.20 * rain_risk +
        0.15 * crop_susceptibility
    )

    # Class labels: 0 = Low Risk, 1 = Medium Risk, 2 = High Risk
    risk_labels = []
    for score in composite_risk_score:
        if score > 0.60:
            risk_labels.append(2) # High
        elif score > 0.35:
            risk_labels.append(1) # Medium
        else:
            risk_labels.append(0) # Low

    df = pd.DataFrame({
        "temperature": temp,
        "humidity": humidity,
        "rainfall_mm": rainfall,
        "wind_kmh": wind,
        "wet_hours": consecutive_wet_hours,
        "crop_susceptibility": crop_susceptibility,
        "risk_level": risk_labels
    })
    return df

def train_risk_model(output_dir: str = "output/risk"):
    os.makedirs(output_dir, exist_ok=True)
    df = generate_agronomic_dataset(6000)
    
    X = df[["temperature", "humidity", "rainfall_mm", "wind_kmh", "wet_hours", "crop_susceptibility"]]
    y = df["risk_level"]

    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42, stratify=y)

    print("Training XGBoost Risk Classifier...")
    model = xgb.XGBClassifier(
        n_estimators=100,
        max_depth=4,
        learning_rate=0.08,
        eval_metric="mlogloss",
        random_state=42
    )
    model.fit(X_train, y_train)

    y_pred = model.predict(X_test)
    acc = accuracy_score(y_test, y_pred)
    print(f"Test Accuracy: {acc * 100:.2f}%")
    print("\nClassification Report:")
    print(classification_report(y_test, y_pred, target_names=["Low Risk", "Medium Risk", "High Risk"]))

    # Save model and feature importances
    model_path = os.path.join(output_dir, "xgb_risk_model.json")
    model.save_model(model_path)
    print(f"Saved XGBoost model to: {model_path}")

    feature_importances = dict(zip(X.columns, [float(x) for x in model.feature_importances_]))
    with open(os.path.join(output_dir, "feature_importance.json"), "w") as f:
        json.dump(feature_importances, f, indent=4)
    print("Feature importances:", feature_importances)

if __name__ == "__main__":
    train_risk_model()
