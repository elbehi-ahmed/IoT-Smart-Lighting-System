[read me (firmware_architecture).md](https://github.com/user-attachments/files/31705762/read.me.firmware_architecture.md)
# Firmware Architecture

## 1. Overview

The ESP32 firmware controls an AC lighting system using TRIAC phase-angle control.

The firmware integrates:

* Wi-Fi connectivity
* MQTT communication
* AC zero-cross detection
* TRIAC phase-angle control
* Manual brightness control
* Automatic brightness control using a PI controller
* LDR light sensing
* DHT11 temperature and humidity sensing
* 16x2 LCD interface
* Brightness smoothing
* Application/system state management
* Manual/automatic operating modes
* Hardware timers and interrupts

---

## 2. High-Level Architecture

```text
                         +-------------------+
                         |    Android App    |
                         +---------+---------+
                                   |
                                   | MQTT
                                   v
                         +-------------------+
                         |    MQTT Broker    |
                         +---------+---------+
                                   |
                                   | Wi-Fi
                                   v
                    +--------------------------------+
                    |              ESP32              |
                    |                                |
                    |  +--------------------------+  |
                    |  | MQTT / Wi-Fi             |  |
                    |  | Communication            |  |
                    |  +------------+-------------+  |
                    |               |                |
                    |               v                |
                    |  +--------------------------+  |
                    |  | Application State        |  |
                    |  | ON / OFF                 |  |
                    |  +------------+-------------+  |
                    |               |                |
                    |               v                |
                    |  +--------------------------+  |
                    |  | Operating Mode           |  |
                    |  | MANUAL / AUTOMATIC       |  |
                    |  +------------+-------------+  |
                    |               |                |
                    |        +------+-------+        |
                    |        |              |        |
                    |        v              v        |
                    |    MANUAL          AUTOMATIC   |
                    |    CONTROL           PI        |
                    |        |           CONTROL     |
                    |        |              |        |
                    |        +------+-------+        |
                    |               |                |
                    |               v                |
                    |       Brightness Control       |
                    |               |                |
                    |               v                |
                    |        TRIAC Phase Control     |
                    |               ^                |
                    |               |                |
                    |       Zero-Cross Interrupt     |
                    |               |                |
                    |          AC Zero Cross         |
                    |                                |
                    |  +--------------------------+  |
                    |  | LCD State Machine        |  |
                    |  +--------------------------+  |
                    |                                |
                    |  +--------------------------+  |
                    |  | DHT11 Sensor             |  |
                    |  +--------------------------+  |
                    +--------------------------------+
                                   |
                                   v
                                AC Lamp
```

---

# 3. Firmware Layers

The firmware can be divided into five logical layers.

### Layer 1 — Hardware Interface

Handles direct interaction with:

* GPIO
* ADC
* LCD
* DHT11
* Zero-cross detector
* Optocoupler/TRIAC interface
* Hardware timers

### Layer 2 — Real-Time Control

Handles:

* Zero-cross interrupts
* TRIAC phase-angle control
* Hardware-timed firing
* Gate pulse generation
* Brightness control
* PI control

### Layer 3 — System Logic

Handles:

* Application/system ON/OFF state
* Manual/automatic mode
* Brightness storage
* Brightness smoothing
* Target light level

### Layer 4 — Communication

Handles:

* Wi-Fi
* MQTT connection
* Application commands
* Status messages
* Sensor data publishing

### Layer 5 — User Interface

Handles:

* LCD state machine
* Manual display
* Automatic display
* Environment display
* Application connection messages
* Display rotation

---

# 4. Application State

The `state` variable represents the **application/system ON/OFF state**.

```text
                 Application State
                        |
                 +------+------+
                 |             |
                 v             v
                ON            OFF
```

The MQTT application controls this state through:

```text
"on"  -> state = on
"off" -> state = off
```

When the application sends `on`, the firmware enters the `ON` state.

When the application sends `off`, the firmware enters the `OFF` state.

The application state is separate from the lighting operating mode.

---

# 5. Lighting Operating Mode

The `mode` variable determines how brightness is controlled.

```text
                 Lighting Mode
                      |
               +------+------+
               |             |
               v             v
            MANUAL       AUTOMATIC
               |             |
               v             v
        App brightness      PI control
               |             |
               |             v
               |            LDR
               |             |
               +------+------+
                      |
                      v
                 Brightness
                      |
                      v
                TRIAC Control
```

The two modes are:

* `manual`
* `automatic`

---

# 6. Manual Mode

In manual mode, the Android application determines the requested
brightness.

```text
Android App
     |
     | brightness value
     v
 MQTT Broker
     |
     v
 MQTT Callback
     |
     v
brightness_before_smoothing
     |
     v
Brightness Smoothing
     |
     v
brightness
     |
     v
TRIAC Control
```

The requested brightness is not immediately applied when it changes.

Instead, the firmware gradually moves the current brightness toward the
requested brightness.

The smoothing mechanism changes the brightness by one step at a time,
approximately every 10 ms.

---

# 7. Automatic Mode

Automatic mode uses the LDR as feedback.

```text
                  TARGET LIGHT
                       |
                       v
                  +---------+
                  |  Error  |
                  | Target  |
                  | - LDR   |
                  +----+----+
                       |
                       v
                  PI Controller
                       |
                       v
                 Brightness
                       |
                       v
                 TRIAC Control
                       |
                       v
                     Lamp
                       |
                       v
                  Light Level
                       |
                       v
                      LDR
                       |
                       +-----------> Feedback
```

When automatic mode is activated:

1. The current manual brightness is stored.
2. The LDR is read.
3. The current LDR value becomes `TARGET_LIGHT`.
4. The PI integral is reset.
5. The firmware switches to automatic mode.

The PI  controller then updates the brightness every 20 ms.

---

# 8. incremental PI Controller

The current implementation uses proportional and integral control.

```text
Kp = 0.02
Ki = 0.002
```

There is no derivative term in the current implementation.

Therefore, the implemented controller is a ** incremental PI controller**, rather than
a full PID controller.

The control process is:

```text
LDR Measurement
      |
      v
Error = TARGET_LIGHT - Measurement
      |
      v
Integral Update
      |
      v
PI Correction
      |
      v
Brightness Update
      |
      v
TRIAC Control
```

The controller runs at:

```text
PID_INTERVAL = 20 ms
```

The integral term is limited to:

```text
-50 <= integral <= 50
```

The resulting brightness is constrained to:

```text
0 <= brightness <= 99
```

---

# 9. Brightness Mapping

The Android application uses a brightness range of:

```text
0 - 100 %
```

The firmware maps this into an internal range:

```text
Application brightness
        |
        v
     0 - 100 %
        |
        v
  Internal brightness
      20 - 100 %
```

This mapping is performed by `mapBrightness()`.

The resulting value is used to select the TRIAC firing delay.

"The minimum brightness of 20 corresponds to the minimum TRIAC firing angle that produces visible light from the lamp. Values below 20 produce no perceptible illumination due to the lamp's non-linear response, so they are mapped to the same effective off-state." where do i add this

---

# 10. TRIAC Phase-Angle Control

The TRIAC is synchronized with the AC waveform using zero-cross
detection.

```text
AC Waveform
     |
     v
Zero-Cross Detection
     |
     v
Zero-Cross Interrupt
     |
     v
Brightness Value
     |
     v
delayTable[]
     |
     v
Hardware Timer
     |
     v
TRIAC Gate HIGH
     |
     v
Gate Pulse Timer
     |
     v
TRIAC Gate LOW
```

The firing delay determines how much of each AC half-cycle is delivered
to the lamp.

```text
Higher brightness
       |
       v
Smaller firing delay
       |
       v
Earlier TRIAC firing
       |
       v
More power to lamp  
also the zero cross hardware adds a delay of 2,6 degrees due to the capacitor that i corrected in the delay table (the signal seen bvy the compatrator is a slightly shiftewd one from the mains) 
```

---

# 11. Zero-Cross Interrupt

The zero-cross detector is connected to GPIO 17.

The firmware attaches an interrupt to this input.

When a zero-crossing event occurs:

```text
Zero Cross
    |
    v
zcfunction()
    |
    v
Check brightness
    |
    +---- brightness == 0
    |          |
    |          v
    |       No firing
    |
    +---- brightness != 0
               |
               v
         Reset timer
               |
               v
       Load firing delay
               |
               v
        Start timer
```

This keeps TRIAC firing synchronized with the AC waveform.

---

# 12. Hardware Timer Architecture

Two hardware timers are used for TRIAC control.

### Timer 1 — Firing Delay

Responsible for waiting between the zero-crossing event and TRIAC
activation.

```text
Zero Cross
    |
    v
Timer 1
    |
    | calculated delay
    v
timerISR()
    |
    v
TRIAC Gate HIGH
```

### Timer 2 — Gate Pulse

Responsible for ending the TRIAC gate pulse.

```text
TRIAC Gate HIGH
       |
       v
    Timer 2
       |
       v
pulseISR()
       |
       v
TRIAC Gate LOW
```

This separates AC synchronization from the short gate-pulse timing.

---

# 13. TRIAC Delay Table

The firmware contains a lookup table containing 101 firing-delay values.

Conceptually:

```text
Brightness
    |
    v
delayTable[brightness]
    |
    v
Firing Delay
    |
    v
TRIAC
```

The relationship is approximately:

```text
Low brightness
      |
      v
Large firing delay
      |
      v
Less conduction
```

and:

```text
High brightness
      |
      v
Small firing delay
      |
      v
More conduction
```

Using a lookup table allows the firmware to use predefined firing delays
rather than calculating the phase-angle relationship during every
zero-crossing event.

---

# 14. MQTT Architecture

The ESP32 communicates with the Android application through an MQTT
broker.

```text
Android App
     |
     v
MQTT Broker
     |
     v
ESP32
```

### Command Topics

```text
home/mode
home/brightness
home/start
```

### Published Topics

```text
home/state
home/temp
home/humidity
```

---

# 15. MQTT Command Processing

Incoming MQTT messages are processed by the MQTT callback.

```text
MQTT Message
     |
     v
callback()
     |
     v
Command Interpretation
     |
     +----------------+----------------+----------------+
     |                |                |                |
     v                v                v                v
  "manual"       "automatic"          "on"            "off"
     |                |                |                |
     v                v                v                v
Manual Mode      Automatic Mode       ON State       OFF State
```

Numerical messages are interpreted as brightness commands:

```text
Brightness Message
       |
       v
message.toInt()
       |
       v
brightness_before_smoothing
       |
       v
Brightness Smoothing
```

---

# 16. MQTT Status and Sensor Data

When the application/system state is ON, the firmware periodically
publishes system and sensor information.

### System Status

Approximately every 5 seconds:

```text
home/state
    |
    v
"online"
```

### Temperature

Approximately every 4 seconds:

```text
DHT11
  |
  v
Temperature
  |
  v
home/temp
```

### Humidity

Approximately every 4 seconds:

```text
DHT11
  |
  v
Humidity
  |
  v
home/humidity
```

---

# 17. LDR Feedback Architecture

The LDR is connected to GPIO 34.

```text
LDR
 |
 v
GPIO 34 ADC
 |
 v
analogRead()
 |
 v
Light Measurement
 |
 +--------------------+
 |                    |
 v                    v
PI Controller      LCD / Logic
```

The LDR measurement is the feedback signal used by automatic lighting
control.

---

# 18. DHT11 Architecture

The DHT11 is connected to GPIO 4.

```text
             DHT11
             /   \
            /     \
           v       v
    Temperature   Humidity
          |          |
          +----+-----+
               |
               v
             ESP32
               |
               v
             MQTT
```

The sensor is read periodically rather than continuously.

---

# 19. LCD State Machine

The LCD uses a dedicated state machine.

States:

```text
DISPLAY_MANUAL
DISPLAY_AUTOMATIC
DISPLAY_ENVIRONMENT
DISPLAY_APP_CONNECTED
DISPLAY_APP_DISCONNECTED
```

Architecture:

```text
                    LCD State
                       |
       +---------------+---------------+
       |               |               |
       v               v               v
    MANUAL        AUTOMATIC      ENVIRONMENT
       ^
       |
       |
       +-----------------------+
                               |
                        Temporary States
                               |
                  +------------+------------+
                  |                         |
                  v                         v
           APP CONNECTED            APP DISCONNECTED
```

---

# 20. LCD Manual Display

The manual state displays:

```text
Manual Mode:
Brightness: XX%
```

If brightness is zero:

```text
Manual Mode:
Lamp is OFF
```

---

# 21. LCD Automatic Display

The automatic state displays:

```text
Automatic Mode:
Brightness: XX%
```

---

# 22. LCD Environment Display

The environment state displays:

```text
ENVIRONMENT
T:XXC H:XX%
```

---

# 23. Temporary LCD Messages

The application connection states are temporary display states.

When the application sends `on`:

```text
APP CONNECTED
     |
     v
Temporary LCD message
     |
     v
Approximately 1.7 s
     |
     v
Return to current operating-mode display
```

When the application sends `off`:

```text
APP DISCONNECTED
     |
     v
Temporary LCD message
     |
     v
Approximately 1.7 s
     |
     v
Return to current operating-mode display
```

---

# 24. LCD Display Rotation

During normal operation, the LCD alternates between the current
operating mode and the environmental information.

```text
MANUAL / AUTOMATIC
        |
        v
 ENVIRONMENT
        |
        v
MANUAL / AUTOMATIC
        |
        v
 ENVIRONMENT
        |
       ...
```

The environment display is shown for approximately 1.7 seconds, while
the operating-mode display remains active for approximately 4 seconds.

---

# 25. Main Loop Architecture

The main loop continuously services the non-interrupt-driven parts of
the firmware.

```text
                    loop()
                      |
          +-----------+-----------+
          |           |           |
          v           v           v
        MQTT      Brightness     System
        Loop      Smoothing      Logic
                                  |
                          +-------+-------+
                          |               |
                          v               v
                       DHT11             PI
                     if required    if automatic
                          |               |
                          +-------+-------+
                                  |
                                  v
                              updateLCD()
```

---

# 26. Timing Architecture

The firmware uses several periodic software timers based on `millis()`.

| Task                          | Period |
| ----------------------------- | -----: |
| Brightness smoothing          | ~10 ms |
| PI control                    |  20 ms |
| LCD update limiting           | 100 ms |
| DHT11 reading                 |   ~4 s |
| MQTT online status            |   ~5 s |
| Temporary LCD message         | ~1.7 s |
| LCD mode/environment rotation |   ~4 s |

TRIAC timing is handled separately using hardware timers and interrupts.

---

# 27. Interrupt vs Main Loop Architecture

The firmware separates normal application processing from time-critical
AC control.

### Main Loop

The main loop handles:

* MQTT communication
* Brightness smoothing
* System/application logic
* DHT11 measurements
* PI control
* LCD state machine

### Interrupt/Timer Layer

Interrupts and hardware timers handle:

* Zero-cross detection
* TRIAC firing delay
* TRIAC gate pulse generation

```text
                ESP32 Firmware
                      |
             +--------+--------+
             |                 |
             v                 v
        Main Loop        Interrupt Layer
             |                 |
             v                 v
          MQTT          Zero Cross ISR
          Sensors              |
          PI                   v
          LCD             Timer 1
          Smoothing            |
                               v
                         TRIAC Gate HIGH
                               |
                               v
                            Timer 2
                               |
                               v
                         TRIAC Gate LOW
```

---

# 28. Initialization Sequence

The `setup()` function initializes the firmware in the following
general sequence:

```text
Start
  |
  v
Initialize LCD
  |
  v
Initialize Serial
  |
  v
Configure Wi-Fi
  |
  v
Connect to Wi-Fi
  |
  v
Configure MQTT
  |
  v
Connect to MQTT Broker
  |
  v
Configure MQTT Callback
  |
  v
Subscribe to MQTT Topics
  |
  v
Configure GPIO
  |
  v
Configure Zero-Cross Interrupt
  |
  v
Initialize Hardware Timers
  |
  v
Configure ADC
  |
  v
Read Initial LDR Value
  |
  v
Initialize LCD State
  |
  v
Publish Initial System Status
  |
  v
Enter Main Loop
```

---

# 29. Complete Control Data Flow

## Manual Control

```text
Android App
    |
    v
MQTT Broker
    |
    v
ESP32 MQTT Callback
    |
    v
Brightness Request
    |
    v
Brightness Smoothing
    |
    v
Current Brightness
    |
    v
Brightness Mapping
    |
    v
TRIAC Delay Table
    |
    v
Zero-Cross Synchronization
    |
    v
Hardware Timer
    |
    v
TRIAC Gate Pulse
    |
    v
AC Lamp
```

## Automatic Control

```text
Android App
    |
    v
Automatic Mode
    |
    v
Target Light Level
    |
    v
LDR Measurement
    |
    v
PI Controller
    |
    v
Brightness Correction
    |
    v
TRIAC Delay Table
    |
    v
Zero-Cross Synchronization
    |
    v
TRIAC Gate Pulse
    |
    v
AC Lamp
    |
    v
Light Level
    |
    v
LDR
    |
    +-----------------------> Feedback
```

---

# 30. Overall Firmware Architecture

```text
                         ANDROID APP
                              |
                              | MQTT
                              v
                         MQTT BROKER
                              |
                              v
                    +--------------------+
                    |       ESP32        |
                    +--------------------+
                              |
             +----------------+----------------+
             |                |                |
             v                v                v
        APPLICATION       OPERATING        COMMUNICATION
          STATE             MODE             SERVICES
        ON / OFF       MANUAL / AUTO       Wi-Fi / MQTT
             |                |                |
             |          +-----+-----+          |
             |          |           |          |
             |          v           v          |
             |       MANUAL     AUTOMATIC      |
             |       CONTROL       PI          |
             |          |           |          |
             |          +-----+-----+          |
             |                |                |
             |                v                |
             |          BRIGHTNESS             |
             |             CONTROL             |
             |                |                |
             |                v                |
             |        TRIAC PHASE CONTROL      |
             |                ^                |
             |                |                |
             |        ZERO-CROSS ISR           |
             |                |                |
             |             AC ZERO             |
             |                |                |
             |                v                |
             |              LAMP               |
             |                |                |
             |                v                |
             |               LDR               |
             |                |                |
             |                +------> PI      |
             |                                 |
             +---------------------------------+
                              |
             +----------------+----------------+
             |                                 |
             v                                 v
        LCD STATE MACHINE                  DHT11
             |                                 |
             v                                 v
          16x2 LCD                     Temperature/Humidity
                                               |
                                               v
                                             MQTT
```

---

# 31. Firmware Design Summary

The firmware is structured around three independent concepts:

### Application State

```text
ON / OFF
```

This represents the state commanded by the application.

### Lighting Mode

```text
MANUAL / AUTOMATIC
```

This determines how brightness is controlled.

### LCD State

```text
MANUAL
AUTOMATIC
ENVIRONMENT
APP CONNECTED
APP DISCONNECTED
```

This determines what information is currently presented to the user.

These states serve different purposes and are intentionally separated.

The real-time TRIAC control is further separated from the main firmware
logic through hardware interrupts and timers, allowing AC synchronization
to operate independently of MQTT, LCD, sensor, and control-processing
tasks.
