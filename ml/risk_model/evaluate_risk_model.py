"""
AgriShield - Crop Disease Risk Model Evaluation
"""

import os
import json
import xgboost as xgb
import numpy as np
import pandas as pd
from train_risk_model import generate_agronomic_dataset
from sklearn.metrics import classification_report, accuracy_score

def evaluate(model_path: str = "output/risk/xgb_risk_model.json"):
    if not os.path.exists(model_path):
        print(f"Model {model_path} not found. Please train first.")
        return

    booster = xgb.Booster()
    booster.load_model(model_path)

    test_df = generate_agronomic_dataset(1500)
    features = ["temperature", "humidity", "rainfall_mm", "wind_kmh", "wet_hours", "crop_susceptibility"]
    X_test = test_df[features]
    y_test = test_df["risk_level"]

    dtest = xgb.DMatrix(X_test)
    preds_prob = booster.predict(dtest)
    preds = np.argmax(preds_prob, axis=1)

    acc = accuracy_score(y_test, preds)
    print(f"Risk Model Evaluation Accuracy: {acc * 100:.2f}%")
    print("\nRisk Classification Report:")
    print(classification_report(y_test, preds, target_names=["Low", "Medium", "High"]))

if __name__ == "__main__":
    evaluate()
