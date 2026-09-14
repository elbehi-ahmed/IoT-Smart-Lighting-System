# IoT-Smart-Lighting-System

An end-to-end smart-lighting project that connects an Android application to an ESP32 over MQTT. The system supports relay control, brightness control, environmental monitoring, local usage history, and automatic brightness control using an LDR sensor.

## Video Demos

| Prototype | Preview / Watch |
| --- | --- |
| **Prototype 1** (Basic Relay) | [![Proto 1 Demo](https://images.unsplash.com/photo-1558346490-a72e53ae2d4f?w=600)](assets/demos/proto-1-demo.mp4) |
| **Prototype 2** (Full System) | [![Proto 2 Demo](https://images.unsplash.com/photo-1558346490-a72e53ae2d4f?w=600)](assets/demos/proto-2-demo.mp4) |


## Choose a prototype

| Prototype | Purpose | Android project | ESP32 entry point |
| --- | --- | --- | --- |
| Prototype 1 | Basic MQTT relay proof of concept | [`prototype-1/mobile_app`](prototype-1/mobile_app) | [`prototype-1/esp32/esp32_code.ino`](prototype-1/esp32/esp32_code.ino) |
| Prototype-2 | Integrated dimming, DHT11 monitoring, LCD, LDR feedback, history, and statistics | [`prototype-2/app`](prototype-2/app) | [`prototype-2/esp32/esp32_code.ino`](prototype-2/esp32/esp32_code.ino) |

Prototype-2 is the recommended version for a complete demonstration. Prototype 1 is retained as the earlier, simpler relay implementation.

## Project map

```text
Smart-Relay-IoT/
├── README.md                         This overview
├── INSTALLATION.md                   Setup, flashing, configuration, and troubleshooting
├── broker-conf.md                    HiveMQ Cloud broker setup and MQTT concepts
├── prototype-1/
│   ├── demo.md                       Prototype 1 notes
│   ├── esp32/                        Basic ESP32 code
│   └── mobile_app/                   Prototype 1 Android/Gradle project
└── prototype 2/
    ├── app/                          Prototype 2 Android/Gradle project
    │   └── app/src/main/              Kotlin activities, MQTT client, Room database
    ├── esp32/                         Basic ESP32 code
    ├── Firmware/                      Firmware experiments and implementation notes
    ├── Hardware/                      Wiring, circuit, and simulation documentation
    └── Control_systems/               Open-loop tests and closed-loop control analysis
```

### Prototype 2 supporting folders

- `Firmware/`: firmware architecture, MQTT behavior, LCD state machine, interrupt-based brightness control, and TRIAC delay-table notes. The `.ino` files in this folder are focused experiments or implementation slices, not all-in-one replacements for the main ESP32 sketch.
- `Hardware/`: wiring and circuit documentation for the LCD/DHT11, LDR signal conditioning, TRIAC phase-angle controller, and zero-crossing detector. The Proteus project is kept with the zero-crossing documentation.
- `Control_systems/open_loop/`: plant-identification sketches, ADC logs, and step-response results used to understand the lamp and LDR response.
- `Control_systems/closed_loop/`: analysis of the feedback controller and its limitations across the brightness range.


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

## Further documentation

- [Installation guide](INSTALLATION.md)
- [HiveMQ broker configuration](broker-conf.md)
- [Firmware architecture](prototype-2/Firmware/read%20me%20(firmware_architecture).md)
- [MQTT communication notes](prototype-2/Firmware/mqtt-communication.md)
- [Automatic control](prototype-2/Firmware/automatic-control..md)
- [Closed-loop analysis](prototype-2/Control_systems/closed_loop/closed_loop.md)
- [Hardware architecture](prototype-2/Hardware/TRIAC%20Leading-Edge%20Phase-Angle%20Controller/readme%20(principal_used%26architecture).md)

--
created by Bouhlel Ahmed & Elbehi Ahmed
If you encounter any bugs, have questions, or want to suggest improvements:
Bug Reports: Open an issue on the GitHub Issues page or contact us .


