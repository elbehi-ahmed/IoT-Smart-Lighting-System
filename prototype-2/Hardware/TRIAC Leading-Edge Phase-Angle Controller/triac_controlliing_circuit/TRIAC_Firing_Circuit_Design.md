# TRIAC Firing Circuit Design

## 1. TRIAC Selection

The load used in the project is a **100 W incandescent lamp** supplied from a nominal **220 V RMS** mains voltage.

The maximum load current is therefore approximately:

$$
I_{load} = \frac{P}{V} = \frac{100}{220} \approx 0.455\ \text{A}
$$

Since the load current is only about **455 mA**, I wanted a TRIAC with a comfortable safety margin rather than selecting a device close to the calculated operating current.

I selected the **BT136**, which has a maximum RMS on-state current of approximately **4 A**. This is significantly higher than the expected load current:

$$
4\ \text{A} \gg 0.455\ \text{A}
$$

Therefore, the BT136 provides a substantial current margin for the 100 W lamp.

The TRIAC is controlled through a **MOC3021 optocoupler**, which provides electrical isolation between the ESP32 control circuitry and the mains-side TRIAC circuit.

---

## 2. MOC3021 Interface

The **MOC3021** is a random-phase optotriac driver. This is important for a phase-angle dimmer because it allows the TRIAC to be triggered at a controlled point during each AC half-cycle.

The ESP32 drives the LED inside the MOC3021. When the LED is activated, the phototriac on the mains side conducts and supplies current to the BT136 gate, causing the main TRIAC to turn on.

Because the MOC3021 is an **optically isolated random-phase driver**, the ESP32 does not need to directly handle the mains voltage or generate a gate signal with the correct positive/negative polarity. The mains-side circuit naturally follows the polarity of the AC waveform.

The ESP32 therefore only needs to control **when the MOC3021 is triggered**. This makes it possible to implement phase-angle control by changing the delay between the zero crossing and the MOC3021 trigger.

> **Important:** The MOC3021 does not automatically synchronize the firing with the AC waveform. The circuit is synchronized through the separate zero-crossing detection system, while the ESP32 determines the firing delay.

---

## 3. MOC3021 LED Resistor

The resistor on the ESP32 side limits the current through the MOC3021 LED.

According to the MOC3021 datasheet, the maximum specified LED trigger current $I_{FT}$ is:

$$
I_{FT(max)} = 15\ \text{mA}
$$

The LED forward voltage is approximately:

$$
V_F \approx 1.1\ \text{V}
$$

I selected a **150 Ω** series resistor as a compromise between providing sufficient LED current and limiting the current drawn from the ESP32 GPIO.

The approximate LED current is:

$$
I_{LED} = \frac{V_{GPIO} - V_F}{R} = \frac{3.3 - 1.1}{150} \approx 14.7\ \text{mA}
$$

This places the LED current close to the MOC3021's specified maximum trigger-current requirement, providing enough current to reliably activate the optotriac.

The resistor also limits the current so that the ESP32 GPIO is not directly overloaded.
 The 15mA is not the absolute maximum current rating (the absolute max continuous forward current is 60mA, as shown in the datasheet).

15mA is the maximum required trigger current which means 14 ma is safe
​
 

---

## 4. BT136 Gate Triggering

The BT136 can operate in four different triggering quadrants depending on the polarity of the gate and the two main terminals.

The required gate current depends strongly on the operating quadrant:

| Quadrant | MT2 Polarity | Gate Polarity | Minimum $I_{GT}$ | Maximum $I_{GT}$ |
| :------: | :----------: | :------------: | ----------------: | ----------------: |
|    I     | Positive (+) |  Positive (+)  |               5 mA |               35 mA |
|    II    | Positive (+) |  Negative (−)  |               8 mA |               35 mA |
|    III   | Negative (−) |  Negative (−)  |              11 mA |               35 mA |
|    IV    | Negative (−) |  Positive (+)  |              30 mA |               70 mA |

Quadrants **I and III** were preferred because they require significantly less gate current than Quadrant IV, particularly compared with the much higher trigger requirement of approximately 30 mA minimum in Quadrant IV.

This is useful because the MOC3021 also has a limited phototriac current capability. Designing around Quadrants I and III therefore gives a more practical operating condition for the optocoupler and allows a wider usable phase-angle range.

---

## 5. Gate Resistor Selection

A **560 Ω resistor** was selected for the mains-side gate circuit.

The resistor has two main purposes:

1. Limit the current flowing through the MOC3021 output and the BT136 gate.
2. Provide sufficient gate current to reliably trigger the BT136 while remaining within the MOC3021's output-current capability.

The resistor was therefore selected as a compromise between:

- providing enough gate current to exceed the BT136's required $I_{GT}$,
- limiting the current through the MOC3021,
- and maintaining reliable triggering over the desired phase-angle range.

The final value was verified experimentally in simulation and during circuit testing to ensure reliable TRIAC triggering throughout the intended dimming range.

---

## 6. Overall Operation

The firing sequence can therefore be summarized as:

1. The **zero-crossing detector** detects an AC zero crossing.
2. The ESP32 starts a controlled delay corresponding to the desired brightness.
3. After the delay, the ESP32 activates the **MOC3021 LED**.
4. The MOC3021 phototriac conducts on the mains side.
5. Current flows through the **560 Ω gate resistor** and into the BT136 gate.
6. The BT136 switches on and remains conducting for the remainder of that AC half-cycle.
7. At the next current zero crossing, the TRIAC naturally turns off.
8. The process repeats on the next half-cycle.

By changing the delay after each zero crossing, the ESP32 controls the **firing angle** of the BT136 and therefore the average power delivered to the lamp.

This forms the basis of the project's **phase-angle AC dimming system**.




## Heatsink and Snubber Omission

- Heatsink not required: Dissipation ~0.8W, junction temp ~88°C (well below 125°C max).
- Snubber not needed: Load is resistive incandescent, natural dv/dt ~0.1 V/µs, far below MOC3021's 10 V/µs rating.
- Both parts are omitted to reduce cost and board space without compromising reliability.
- If future use includes inductive loads or >200W lamps, they can be added easily.
