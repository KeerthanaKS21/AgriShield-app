"""
AgriShield - Crop Disease Model Evaluation Script
Computes Classification Report (Precision, Recall, F1-Score) and Confusion Matrix.
"""

import os
import argparse
import json
import numpy as np
import tensorflow as tf
from sklearn.metrics import classification_report, confusion_matrix
import matplotlib.pyplot as plt

IMG_SIZE = (224, 224)
BATCH_SIZE = 32

def evaluate(model_path: str, dataset_dir: str, output_dir: str):
    os.makedirs(output_dir, exist_ok=True)
    print(f"Loading model from: {model_path}")
    model = tf.keras.models.load_model(model_path)

    print(f"Loading test dataset from: {dataset_dir}")
    test_ds = tf.keras.utils.image_dataset_from_directory(
        dataset_dir,
        seed=42,
        image_size=IMG_SIZE,
        batch_size=BATCH_SIZE,
        shuffle=False,
        label_mode='categorical'
    )

    class_names = test_ds.class_names
    y_true = []
    y_pred = []

    print("Running evaluation inference...")
    for images, labels in test_ds:
        preds = model.predict(images, verbose=0)
        y_true.extend(np.argmax(labels.numpy(), axis=1))
        y_pred.extend(np.argmax(preds, axis=1))

    y_true = np.array(y_true)
    y_pred = np.array(y_pred)

    # Generate classification report
    report = classification_report(y_true, y_pred, target_names=class_names, output_dict=True)
    print("\n" + "="*50)
    print("CLASSIFICATION REPORT:")
    print("="*50)
    print(classification_report(y_true, y_pred, target_names=class_names))

    # Save metrics JSON
    metrics_file = os.path.join(output_dir, "evaluation_report.json")
    with open(metrics_file, "w") as f:
        json.dump(report, f, indent=4)
    print(f"Saved evaluation report to: {metrics_file}")

    # Generate Confusion Matrix
    cm = confusion_matrix(y_true, y_pred)
    plt.figure(figsize=(12, 10))
    plt.imshow(cm, interpolation='nearest', cmap=plt.cm.Greens)
    plt.title("AgriShield Crop Disease Confusion Matrix")
    plt.colorbar()
    tick_marks = np.arange(len(class_names))
    plt.xticks(tick_marks, class_names, rotation=90)
    plt.yticks(tick_marks, class_names)
    plt.ylabel('True Disease Label')
    plt.xlabel('Predicted Disease Label')
    plt.tight_layout()

    cm_plot_path = os.path.join(output_dir, "confusion_matrix.png")
    plt.savefig(cm_plot_path, dpi=300)
    plt.close()
    print(f"Saved confusion matrix plot to: {cm_plot_path}")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Evaluate AgriShield Crop Disease Classifier")
    parser.add_argument("--model", type=str, default="output/crop_disease_model.keras", help="Path to trained .keras model")
    parser.add_argument("--dataset", type=str, default="data/dataset", help="Path to test dataset")
    parser.add_argument("--output", type=str, default="output/eval", help="Output directory")
    args = parser.parse_args()

    evaluate(args.model, args.dataset, args.output)
