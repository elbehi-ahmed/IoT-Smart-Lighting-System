# Smart Relay IoT

An end-to-end smart-lighting project that connects an Android application to an ESP32 over MQTT. The system supports relay control, brightness control, environmental monitoring, local usage history, and automatic brightness control using an LDR sensor.

## Choose a prototype

| Prototype | Purpose | Android project | ESP32 entry point |
| --- | --- | --- | --- |
| Prototype 1 | Basic MQTT relay proof of concept | [`prototype-1/mobile_app`](prototype-1/mobile_app) | [`prototype-1/esp32/esp32_code.ino`](prototype-1/esp32/esp32_code.ino) |
| Prototype 2 | Integrated dimming, DHT11 monitoring, LCD, LDR feedback, history, and statistics | [`prototype 2/app`](prototype%202/app) | [`prototype 2/esp32/esp32_code.ino`](prototype%202/esp32/esp32_code.ino) |

Prototype 2 is the recommended version for a complete demonstration. Prototype 1 is retained as the earlier, simpler relay implementation.

## Project map

```text
Smart-Relay-IoT/
├── README.md                         This overview
├── INSTALLATION.md                   Setup, flashing, configuration, and troubleshooting
├── broker-conf.md                    HiveMQ Cloud broker setup and MQTT concepts
├── prototype-1/
│   ├── demo.md                       Prototype 1 notes
│   ├── esp32/                        Basic ESP32 MQTT relay sketch
│   └── mobile_app/                   Prototype 1 Android/Gradle project
└── prototype 2/
    ├── app/                          Prototype 2 Android/Gradle project
    │   └── app/src/main/              Kotlin activities, MQTT client, Room database, and UI
    ├── esp32/                         Integrated ESP32 sketch
    ├── Firmware/                      Firmware experiments and implementation notes
    ├── Hardware/                      Wiring, circuit, and simulation documentation
    └── Control_systems/               Open-loop tests and closed-loop control analysis
```

### Prototype 2 supporting folders

- `Firmware/`: firmware architecture, MQTT behavior, LCD state machine, interrupt-based brightness control, and TRIAC delay-table notes. The `.ino` files in this folder are focused experiments or implementation slices, not all-in-one replacements for the main ESP32 sketch.
- `Hardware/`: wiring and circuit documentation for the LCD/DHT11, LDR signal conditioning, TRIAC phase-angle controller, and zero-crossing detector. The Proteus project is kept with the zero-crossing documentation.
- `Control_systems/open_loop/`: plant-identification sketches, ADC logs, and step-response results used to understand the lamp and LDR response.
- `Control_systems/closed_loop/`: analysis of the feedback controller and its limitations across the brightness range.

### Prototype 2 Android folders

- `activities/`: Main control screen, history, and statistics screens.
- `viewmodel/`: Timer and statistics state used by the UI.
- `database/`, `dao/`, `entity/`, and `repository/`: Room storage for lighting sessions and sensor values.
- `utils/`: MQTT connection and time/statistics helpers.
- `res/`: Layouts, strings, themes, icons, and other Android resources.
- `app/build.gradle.kts`: Android SDK settings and dependencies, including HiveMQ MQTT and Room.
- `gradle/libs.versions.toml`: Central dependency and plugin versions.

## How the system works

The Android app and ESP32 are MQTT clients. HiveMQ Cloud is the broker between them.

| Topic | Direction | Payload examples | Use |
| --- | --- | --- | --- |
| `home/start` | App -> ESP32 | `on`, `off` | Start/stop relay session |
| `home/brightness` | App -> ESP32 | `0` to `100` | Manual brightness command |
| `home/mode` | App -> ESP32 | `manual`, `automatic` | Select control mode |
| `home/temp` | ESP32 -> App | numeric value | DHT11 temperature |
| `home/humidity` | ESP32 -> App | numeric value | DHT11 humidity |
| `home/state` | ESP32 -> App | `online`, `connecting`, or state | Device status heartbeat |

## Quick start

Read [`INSTALLATION.md`](INSTALLATION.md) for the complete procedure. In short:

1. Create a HiveMQ Cloud broker and credentials using [`broker-conf.md`](broker-conf.md).
2. Configure the broker, Wi-Fi, and MQTT credentials in the selected Android and ESP32 sources.
3. Open the correct Android project directory in Android Studio and run it on an Android 8.0+ device or emulator.
4. Open the matching `.ino` sketch in Arduino IDE, install its libraries, select the ESP32 board and port, then upload it.
5. Test the MQTT connection before connecting the mains-voltage TRIAC circuit.

## Safety and credentials

Prototype 2 controls AC mains hardware through a TRIAC. Do not handle or test mains wiring without appropriate electrical safety procedures, isolation, fusing, and supervision. Begin with the ESP32 and low-voltage test loads.

Credentials are currently stored in source files, which is convenient for the prototype but unsafe for a public repository. Replace any exposed Wi-Fi or MQTT password immediately, and move credentials to a local, ignored configuration mechanism before deployment. Never commit `local.properties`, passwords, or production broker credentials.

## Further documentation

- [Installation guide](INSTALLATION.md)
- [HiveMQ broker configuration](broker-conf.md)
- [Firmware architecture](prototype%202/Firmware/read%20me%20(firmware_architecture).md)
- [MQTT communication notes](prototype%202/Firmware/mqtt-communication.md)
- [Automatic control](prototype%202/Firmware/automatic-control..md)
- [Closed-loop analysis](prototype%202/Control_systems/closed_loop/closed_loop.md)
- [Hardware architecture](prototype%202/Hardware/TRIAC%20Leading-Edge%20Phase-Angle%20Controller/readme%20(principal_used%26architecture).md)
