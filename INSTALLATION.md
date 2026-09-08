# Installation and setup

This guide explains how to run the Smart Relay IoT project locally and how to connect the Android app, ESP32, MQTT broker, sensors, and lighting controller.

## 1. Requirements

### Software

- Android Studio 
- Arduino IDE 2.x or PlatformIO
- ESP32 board support for Arduino IDE
- An MQTT client such as the HiveMQ Cloud Web Client for testing

### Hardware

- ESP32 development board
- DHT11 temperature/humidity sensor
- LDR sensor circuit connected to the ESP32 ADC input
- 16x2 LCD and its wiring
- Optocoupler, TRIAC driver, zero-crossing detector, and a suitable lamp/load for Prototype 2
- USB cable and a separate low-voltage test setup

Do not connect or probe AC mains hardware while learning the software setup. Validate the ESP32 and MQTT behavior with a safe low-voltage load first.

## 2. Clone the repository

```bash
git clone https://github.com/elbehi-ahmed/IoT-Smart-Lighting-System.git
cd Smart-Relay-IoT
```

## 3. Create and test the MQTT broker

Prototype 2 uses HiveMQ Cloud over TLS on port `8883`.

1. Follow [`broker-conf.md`](broker-conf.md) to create a HiveMQ Cloud cluster.
2. Create a dedicated MQTT username and password for the project.
3. Record the cluster hostname and TLS port.
4. Use the HiveMQ Web Client to confirm that the credentials can connect.
5. Subscribe to `home/#` while testing. This lets you see all Prototype 2 messages.

The Android app and ESP32 must use the same hostname, port, username, password, and topic names.

## 4. Configure Prototype 2

Prototype 2 is the complete implementation.

### Android MQTT settings

Open [`RelayMqttClient.kt`](prototype-2/app/app/src/main/java/com/ElbehiAhmed/studytimer/utils/RelayMqttClient.kt) and replace the placeholder values in `HOST`, `USERNAME`, and `PASSWORD` with your broker settings. Keep port `8883` for the TLS connection unless your broker configuration says otherwise.

The Android app requires Internet permission, which is already declared in its manifest. The app publishes commands and subscribes to sensor/status topics from [`MainActivity.kt`](prototype-2/app/app/src/main/java/com/ElbehiAhmed/studytimer/activities/MainActivity.kt).

### ESP32 settings

Open [`prototype-2/esp32/esp32_code.ino`](prototype-2/esp32/esp32_code.ino) and replace:

```cpp
const char* WIFI_SSID = "your-wifi-name";
const char* WIFI_PASSWORD = "your-wifi-password";
const char* MQTT_HOST = "your-cluster.s1.eu.hivemq.cloud";
const int MQTT_PORT = 8883;
const char* MQTT_USERNAME = "your-mqtt-user";
const char* MQTT_PASSWORD = "your-mqtt-password";
```

The current sketch also defines the main hardware pins. Confirm them against your wiring before uploading:

| Function | GPIO in the sketch |
| --- | ---: |
| DHT11 data | 4 |
| Zero-crossing input | 17 |
| TRIAC optocoupler output | 16 |
| LDR ADC input | 34 |
| LCD RS, E, D4, D5, D6, D7 | 22, 21, 19, 18, 5, 23 |

The pin assignments and the hardware documentation must agree. Change the sketch or wiring as a matched pair, never one without the other.

## 5. Build and run the Android app

1. Start Android Studio.
2. Choose **Open** and select `prototype-2/app`, which contains `settings.gradle.kts` and the Gradle wrapper.
3. Allow Gradle sync to finish and install any requested Android SDK components.
4. Select the `app` run configuration and press **Run**.


From a terminal, the same debug build can be started from the project directory:

```powershell
cd 'prototype-2\app'
.\gradlew.bat assembleDebug
```

The generated APK is placed under `app/build/outputs/apk/debug/`.

## 6. Install ESP32 support and libraries

In Arduino IDE:

1. Open **Preferences** and add the Espressif ESP32 board manager URL from the official ESP32 Arduino documentation.
2. Open **Boards Manager**, search for `esp32`, and install the Espressif package.
3. Open **Library Manager** and install libraries matching these includes:
   - `PubSubClient`
   - `DHT11`
   - `LiquidCrystal`
4. Select the correct ESP32 board, usually **ESP32 Dev Module**.
5. Select the USB port for the board.
6. Open the sketch, verify the credentials and pins, and compile it.
7. Upload it, then open Serial Monitor at `115200` baud.

The ESP32 should connect to Wi-Fi, connect to HiveMQ, publish its state, and react to Android commands. If it does not, use the serial output and an MQTT client subscribed to `home/#` to identify whether the failure is Wi-Fi, broker authentication, or topic configuration.

## 7. Test in a safe order

1. Test the broker with the HiveMQ Web Client.
2. Upload the ESP32 sketch with the AC load disconnected.
3. Confirm that the ESP32 connects and that MQTT messages appear on `home/#`.
4. Run the Android app and confirm that it receives `home/state`, `home/temp`, and `home/humidity`.
5. Publish a controlled brightness value and verify the ESP32 behavior with a safe test load.
6. Only after the low-voltage and isolation checks pass should qualified personnel connect the TRIAC and lamp circuit.

## 8. Run Prototype 1 instead

Prototype 1 is the earlier basic relay implementation:

1. Open `prototype-1/mobile_app` in Android Studio.
2. Configure the broker values used by its Android MQTT client.
3. Open `prototype-1/esp32/esp32_code.ino` in Arduino IDE.
4. Replace its Wi-Fi and MQTT values, select the ESP32 board and port, then upload.
5. Use the app and serial monitor to verify basic relay messages.

Prototype 1 does not provide the full Prototype 2 sensor, dimming, LCD, and control-system workflow.

## 9. Troubleshooting

### Gradle sync or build fails

- Open the project root containing `settings.gradle.kts`, not `prototype 2/app/app`.
- Check that Android Studio has downloaded the SDK required by `compileSdk = 37`.
- Use the included `gradlew.bat` so the repository's Gradle version is used.
- Delete only generated `build/` directories if a stale build cache is suspected, then sync again.

### ESP32 cannot connect to Wi-Fi

- Check SSID and password, including capitalization.
- Confirm the network is available to the ESP32 and is suitable for the board's Wi-Fi band.
- Watch Serial Monitor at `115200` baud.

### The app connects but shows no sensor data

- Subscribe to `home/#` with an MQTT client.
- Confirm that the ESP32 sketch publishes the exact topics expected by `MainActivity.kt`.
- Check DHT11 wiring and power.
- Confirm the ESP32 loop continues calling the MQTT client loop function.
