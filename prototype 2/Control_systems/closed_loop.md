# Closed-Loop Control Study — ESP32 Smart Lighting

## 1. Objective

I wanted to control the brightness of the AC lamp automatically using an LDR as the feedback sensor.

The goal was to maintain a target light level despite changes in the surrounding lighting.

The control loop is:

LDR → ADC → PI controller → brightness command → TRIAC → lamp → light → LDR

I used the ESP32 to read the LDR and control the TRIAC firing angle.

---

# 2. Open-Loop Characterization

Before implementing the controller, I tested the system in open loop.

I changed the lamp brightness manually and recorded the LDR ADC response.

For one of the step tests:

| Parameter | Measured value |
|---|---:|
| Initial ADC value | ≈2958 |
| Final ADC value | ≈3568 |
| Step size | ≈610 ADC |
| Sampling period | 10 ms |
| Rise time (10–90%) | ≈60 ms |
| 2% settling time | ≈120 ms |
| Dead time | ≈0–10 ms |
| Overshoot | ≈7 ADC ≈1.1% |
| Steady-state variation | ≈±5 ADC |

The response was mostly monotonic and did not show significant oscillation during the open-loop test.

This indicated that the lighting/sensor system could be approximated as a relatively slow first-order plant for initial controller design.

---

# 3. Plant Gain

I initially calculated the plant gain from the change in output divided by the change in brightness command.

For the tested step:

Δy ≈ 610 ADC

Δu = 40 brightness units

Therefore:

K_plant = Δy / Δu

K_plant = 610 / 40

K_plant ≈ 15.25 ADC/brightness unit

The important point is that K_plant is the **change in output produced by a change in input**.

It is not simply the final ADC value.

In the time domain, this means approximately:

Δy ≈ K_plant Δu

For my system:

ΔADC ≈ 15.25 × Δbrightness

This gain describes the sensitivity of the plant to a brightness command.

---

# 4. Time Constant

For a first-order system, the time constant τ is the time required for the response to reach approximately 63.2% of its total change.

For the open-loop step:

Initial value:

2958 ADC

Total change:

610 ADC

63.2% of the change:

0.632 × 610 ≈ 385 ADC

Therefore the 63.2% level is:

2958 + 385 ≈ 3343 ADC

From the measured response, the system reached approximately this level around 40 ms after the step.

After accounting for approximately 10 ms of sensor/response delay, I estimated:

τ ≈ 35 ms

This gave the simplified plant model:

G(s) ≈ 15.25 / (0.035s + 1)

This model is only an approximation of the real lighting system.

The real system contains nonlinearities, sensor filtering, TRIAC phase-angle control, lamp dynamics, and environmental light.

---

# 5. LDR Filtering

The LDR is connected at the top of the voltage divider.

Therefore:

More light → LDR resistance decreases → ADC voltage increases

So my feedback has the following relationship:

More light → higher ADC value

Less light → lower ADC value

I also added a capacitor to filter the LDR signal.

Initially I used approximately 1 µF.

I later tested a 10 µF capacitor to obtain more filtering.

The larger capacitor made the response slower and caused the closed-loop oscillation to become worse.

After returning to 1 µF, the oscillation became smaller but did not disappear.

This showed that the capacitor was not the fundamental cause of the oscillation.

Instead, the larger capacitor increased the delay of the feedback signal and therefore made the existing control-loop problem worse.

---

# 6. Why the Feedback Delay Matters

The controller does not immediately see the effect of a brightness change.

The actual process is:

PI controller
↓
Brightness command
↓
TRIAC
↓
Lamp
↓
Light output
↓
LDR
↓
RC filtering
↓
ADC
↓
PI controller

Therefore, the controller is always reacting to a measurement that is delayed relative to its previous command.

If the controller is too aggressive, it can increase the brightness again before the LDR has had enough time to respond.

For example:

Brightness increases
↓
LDR has not responded yet
↓
Controller still sees low light
↓
Brightness increases again
↓
LDR finally responds
↓
Measured light becomes too high
↓
Controller decreases brightness
↓
The same process happens in the opposite direction

This creates hunting/oscillation.

---

# 7. Initial PI Controller

I first implemented the controller using:

Kp = 0.10

Ki = 0.025

PID update interval:

20 ms

The controller calculated:

error = TARGET_LIGHT - light

dt = 0.020 s

The integral was calculated as:

integral += error × dt

I also added integral anti-windup:

integral = constrain(integral, -50, 50)

The original controller calculated an absolute brightness command:

output = Kp × error + Ki × integral

However, I changed this approach after realizing that my controller should behave as an incremental controller for the way I was modifying the TRIAC brightness command.

---

# 8. Incremental PI Controller

The current controller uses the PI output as a correction to the existing brightness value.

The basic implementation is:

```cpp
void updatePID()
{
    int light = analogRead(LDR_PIN);

    float error = TARGET_LIGHT - light;
    float dt = PID_INTERVAL / 1000.0;

    integral += error * dt;
    integral = constrain(integral, -50.0, 50.0);

    float correction = (Kp * error) + (Ki * integral);

    brightness += (int)correction;

    brightness = constrain(brightness, 0, 99);
}
