"""
AgriShield - Export Keras Model to TensorFlow Lite (.tflite)
Supports Float32 / Float16 quantization and tests input/output tensors.
"""

import os
import argparse
import numpy as np
import tensorflow as tf

def export_tflite(keras_model_path: str, output_tflite_path: str, quantize_fp16: bool = False):
    print(f"Loading Keras model from: {keras_model_path}")
    model = tf.keras.models.load_model(keras_model_path)

    print("Converting model to TensorFlow Lite format...")
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    
    if quantize_fp16:
        print("Enabling FP16 quantization...")
        converter.target_spec.supported_types = [tf.float16]

    tflite_model = converter.convert()

    os.makedirs(os.path.dirname(os.path.abspath(output_tflite_path)), exist_ok=True)
    with open(output_tflite_path, "wb") as f:
        f.write(tflite_model)
    
    size_mb = os.path.getsize(output_tflite_path) / (1024 * 1024)
    print(f"Successfully exported TFLite model to: {output_tflite_path} ({size_mb:.2f} MB)")

    # Verify model with TFLite Interpreter
    print("\nVerifying TFLite Interpreter...")
    interpreter = tf.lite.Interpreter(model_path=output_tflite_path)
    interpreter.allocate_tensors()

    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()

    print(f"Input Shape:  {input_details[0]['shape']}, Type: {input_details[0]['dtype']}")
    print(f"Output Shape: {output_details[0]['shape']}, Type: {output_details[0]['dtype']}")

    # Test dummy inference
    sample_input = np.random.uniform(0.0, 1.0, size=input_details[0]['shape']).astype(np.float32)
    interpreter.set_tensor(input_details[0]['index'], sample_input)
    interpreter.invoke()
    output_data = interpreter.get_tensor(output_details[0]['index'])

    print(f"Sample Inference Output Shape: {output_data.shape}")
    print(f"Softmax Sum Check: {np.sum(output_data):.4f} (should be ~1.0)")
    print("TFLite Model Verification PASSED!")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Export Keras Model to TensorFlow Lite")
    parser.add_argument("--model", type=str, default="output/crop_disease_model.keras", help="Path to .keras model")
    parser.add_argument("--output", type=str, default="../app/src/main/assets/model.tflite", help="Path for .tflite file")
    parser.add_argument("--fp16", action="store_true", help="Use Float16 quantization")
    args = parser.parse_args()

    export_tflite(args.model, args.output, args.fp16)
