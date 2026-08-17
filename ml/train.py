"""
AgriShield - Crop Disease Detection Model Training Pipeline
Architecture: MobileNetV2 with Transfer Learning
Target: 15+ Crop Disease Classes across Tomato, Potato, Corn, Rice, Pepper, Apple
"""

import os
import sys
import argparse
import json
import numpy as np
import tensorflow as tf
from tensorflow.keras import layers, models, callbacks, applications, optimizers
from sklearn.metrics import classification_report, confusion_matrix
import matplotlib.pyplot as plt

# Crop disease classes supported by AgriShield
DEFAULT_CLASSES = [
    "Apple___Apple_scab",
    "Apple___Black_rot",
    "Apple___healthy",
    "Corn___Common_rust",
    "Corn___Northern_Leaf_Blight",
    "Corn___healthy",
    "Pepper_bell___Bacterial_spot",
    "Pepper_bell___healthy",
    "Potato___Early_blight",
    "Potato___Late_blight",
    "Potato___healthy",
    "Rice___Brown_Spot",
    "Rice___Leaf_Blast",
    "Rice___healthy",
    "Tomato___Early_blight",
    "Tomato___Late_blight",
    "Tomato___healthy"
]

IMG_SIZE = (224, 224)
BATCH_SIZE = 32
EPOCHS = 25
LEARNING_RATE = 1e-4

def build_model(num_classes: int) -> tf.keras.Model:
    """Builds MobileNetV2 transfer learning model for crop disease classification."""
    base_model = applications.MobileNetV2(
        input_shape=(IMG_SIZE[0], IMG_SIZE[1], 3),
        include_top=False,
        weights='imagenet'
    )
    # Fine-tune the top layers
    base_model.trainable = True
    for layer in base_model.layers[:-30]:
        layer.trainable = False

    data_augmentation = tf.keras.Sequential([
        layers.RandomFlip("horizontal_and_vertical"),
        layers.RandomRotation(0.2),
        layers.RandomZoom(0.2),
        layers.RandomContrast(0.2),
    ], name="data_augmentation")

    inputs = layers.Input(shape=(IMG_SIZE[0], IMG_SIZE[1], 3))
    x = data_augmentation(inputs)
    # Normalization: rescale from [0, 255] to [0, 1]
    x = layers.Rescaling(1.0 / 255.0)(x)
    x = base_model(x, training=False)
    x = layers.GlobalAveragePooling2D()(x)
    x = layers.BatchNormalization()(x)
    x = layers.Dropout(0.3)(x)
    x = layers.Dense(256, activation='relu')(x)
    x = layers.Dropout(0.2)(x)
    outputs = layers.Dense(num_classes, activation='softmax', name="predictions")(x)

    model = tf.keras.Model(inputs, outputs, name="AgriShield_MobileNetV2")
    return model

def train(dataset_dir: str, output_dir: str):
    """Main training routine."""
    os.makedirs(output_dir, exist_ok=True)

    print(f"Loading dataset from: {dataset_dir}")
    train_ds = tf.keras.utils.image_dataset_from_directory(
        dataset_dir,
        validation_split=0.2,
        subset="training",
        seed=1337,
        image_size=IMG_SIZE,
        batch_size=BATCH_SIZE,
        label_mode='categorical'
    )

    val_ds = tf.keras.utils.image_dataset_from_directory(
        dataset_dir,
        validation_split=0.2,
        subset="validation",
        seed=1337,
        image_size=IMG_SIZE,
        batch_size=BATCH_SIZE,
        label_mode='categorical'
    )

    class_names = train_ds.class_names
    num_classes = len(class_names)
    print(f"Detected {num_classes} classes: {class_names}")

    # Save labels.txt
    labels_path = os.path.join(output_dir, "labels.txt")
    with open(labels_path, "w") as f:
        for name in class_names:
            f.write(f"{name}\n")
    print(f"Saved class labels to: {labels_path}")

    # Prefetch datasets for performance
    AUTOTUNE = tf.data.AUTOTUNE
    train_ds = train_ds.prefetch(buffer_size=AUTOTUNE)
    val_ds = val_ds.prefetch(buffer_size=AUTOTUNE)

    # Build and compile
    model = build_model(num_classes)
    model.compile(
        optimizer=optimizers.Adam(learning_rate=LEARNING_RATE),
        loss='categorical_crossentropy',
        metrics=['accuracy', tf.keras.metrics.Precision(name='precision'), tf.keras.metrics.Recall(name='recall')]
    )
    model.summary()

    # Training callbacks
    model_save_path = os.path.join(output_dir, "crop_disease_model.keras")
    cb = [
        callbacks.EarlyStopping(monitor='val_loss', patience=6, restore_best_weights=True, verbose=1),
        callbacks.ReduceLROnPlateau(monitor='val_loss', factor=0.5, patience=3, min_lr=1e-6, verbose=1),
        callbacks.ModelCheckpoint(model_save_path, monitor='val_accuracy', save_best_only=True, verbose=1)
    ]

    print("Starting Model Training...")
    history = model.fit(
        train_ds,
        validation_data=val_ds,
        epochs=EPOCHS,
        callbacks=cb
    )

    # Save final model
    model.save(model_save_path)
    print(f"Model saved to {model_save_path}")

    # Save training metrics
    metrics = {
        "final_train_accuracy": float(history.history['accuracy'][-1]),
        "final_val_accuracy": float(history.history['val_accuracy'][-1]),
        "final_val_precision": float(history.history['val_precision'][-1]),
        "final_val_recall": float(history.history['val_recall'][-1]),
        "epochs_trained": len(history.history['accuracy'])
    }
    with open(os.path.join(output_dir, "training_metrics.json"), "w") as f:
        json.dump(metrics, f, indent=4)
    print("Training metrics saved:", metrics)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Train AgriShield Crop Disease Classifier")
    parser.add_argument("--dataset", type=str, default="data/dataset", help="Path to dataset directory")
    parser.add_argument("--output", type=str, default="output", help="Directory to save trained model")
    args = parser.parse_args()

    if not os.path.exists(args.dataset):
        print(f"Error: Dataset directory '{args.dataset}' does not exist.")
        print("Please download the PlantVillage dataset or run ml/generate_seed_model.py for initial setup.")
        sys.exit(1)

    train(args.dataset, args.output)
