# IoT Smart Lighting System

An end-to-end IoT smart solution utilizing an Android app to remotely control and monitor physical hardware appliances via ESP32 and lightweight MQTT communication protocols.

## Features & Prototypes

The repository is divided into two working prototypes. Each prototype contains an `app` folder (Android source code) and an `esp32` folder (C++ hardware code). Both versions feature local history logging and usage statistics calculations.

*   **Prototype 1: Basic Relay Control**
    Allows users to remotely toggle a physical relay on and off directly from the mobile interface.
*   **Prototype 2: Advanced Monitoring & Dimming**
    Expands functionality by streaming real-time environmental data (temperature, humidity) from the ESP32 to the app, and provides dynamic brightness control for a bedside lamp.

## Demos

*   [Demo Video 1 (Basic) - *e*](#)
*   [Demo Video 2 (Advanced) - *ere*](#)

## Installation & Setup

### Android Application
1. **Clone the repository:**
   ```bash
   git clone https://github.com/elbehi-ahmed/IoT-Smart-Lighting-System.git
   ```
2. **Open the project:** Launch **Android Studio** (Hedgehog or newer recommended) and open the `app` directory of your chosen prototype.
3. **Build and Deploy:** Allow the Gradle sync to finish, then click **Run ▶** to deploy the app to an emulator or physical device.

### ESP32 Hardware
1. Open the source files inside the `esp32` directory using the **Arduino IDE** or **PlatformIO**.
2. Configure your Wi-Fi credentials and MQTT broker settings within the code.
3. Compile and flash the code to your ESP32 board.
