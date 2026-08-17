"""
AgriShield - Export Risk Model Agronomic Parameters for On-Device Android Engine
"""

import os
import json

def export_parameters(output_path: str = "../../app/src/main/assets/risk_config.json"):
    config = {
        "engine_version": "1.0.0",
        "algorithm": "BLITECAST-Wallin Multi-Factor Agronomic Risk Engine",
        "optimal_fungal_temp_c": 24.0,
        "fungal_temp_std_c": 6.0,
        "high_humidity_threshold": 80.0,
        "leaf_wetness_weight": 0.35,
        "humidity_weight": 0.30,
        "rainfall_weight": 0.20,
        "crop_susceptibility_weight": 0.15,
        "risk_thresholds": {
            "low_to_medium": 0.35,
            "medium_to_high": 0.60
        },
        "crop_susceptibility": {
            "Rice": 0.90,
            "Potato": 0.85,
            "Tomato": 0.80,
            "Apple": 0.75,
            "Pepper": 0.70,
            "Corn": 0.60
        }
    }
    
    os.makedirs(os.path.dirname(os.path.abspath(output_path)), exist_ok=True)
    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(config, f, indent=4)
    print(f"Exported risk configuration to: {output_path}")

if __name__ == "__main__":
    export_parameters()
