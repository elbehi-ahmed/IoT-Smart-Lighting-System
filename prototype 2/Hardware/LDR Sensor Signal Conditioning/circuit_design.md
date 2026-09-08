# LDR Light Intensity Sensor

## Sensor Selection

For the light intensity sensor, I chose a **generic 5 mm LDR (Light-Dependent Resistor)**.

This was sufficient for the application because the objective was to measure **general light intensity**, rather than distinguish between specific wavelengths or colors of light. Therefore, the spectral characteristics of the sensor were not a primary concern.

## Characterizing the LDR

Before designing the interface circuit, I needed to characterize the LDR's resistance over the range of light conditions expected in the application. This allowed me to select an appropriate **voltage divider** for interfacing the sensor with the ESP32's ADC.

I measured the LDR's resistance using a multimeter under several different lighting conditions:

* **Complete darkness**
* **Ambient room lighting**
* **The controlled lamp at different brightness levels**

The measured resistance ranged approximately from **3.5 kΩ to 170 kΩ**.

By varying the lamp brightness, I was able to determine the approximate resistance range that the sensor would need to cover during operation. This was important for selecting the voltage-divider resistor and ensuring that the ADC would be able to measure the entire useful range without saturating.

## Physical Placement

To make the measurements representative of the lamp's actual illumination, I fixed the LDR **to the side of the lamp** at a consistent position relative to the light source.

I also added a small **cover around the LDR** to prevent it from being directly exposed to the lamp's light source. This reduces the influence of the lamp's direct light and allows the sensor to respond more to the **surrounding environmental light**.

Keeping the sensor in a fixed position also ensures consistent geometry and distance from the lamp during measurements and operation.

## Voltage Divider Design

Based on the measured resistance range of approximately **3.5 kΩ to 170 kΩ**, I selected a **34.7 kΩ resistor** as the lower resistor of the voltage divider, with the LDR acting as the upper resistor.

The resulting circuit is:

```text
              Vcc = 3.3 V
                 │
                LDR
                 │
                 ●────── ADC
                 │
              34.7 kΩ
                 │
                GND
```

The ADC voltage is given by:

$$
V_{ADC} = V_{CC} \cdot \frac{34.7\ \text{k}\Omega}{R_{LDR} + 34.7\ \text{k}\Omega}
$$

### Maximum LDR Resistance

At the darkest measured condition, $R_{LDR} = 170\ \text{k}\Omega$, so:

$$
V_{ADC,min} = 3.3 \cdot \frac{34.7}{170 + 34.7} \approx 0.559\ \text{V}
$$

### Minimum LDR Resistance

At the brightest measured condition, $R_{LDR} = 3.5\ \text{k}\Omega$, so:

$$
V_{ADC,max} = 3.3 \cdot \frac{34.7}{3.5 + 34.7} \approx 3.00\ \text{V}
$$

Thus, over the measured LDR resistance range, the ADC input spans approximately:

$$
0.56\ \text{V} \leq V_{ADC} \leq 3.00\ \text{V}
$$

This keeps the signal within the ESP32's **3.3 V supply range** while making use of a large portion of the ADC's measurable range.

## Signal Conditioning

A **1 µF capacitor** was added at the ADC input to provide hardware filtering of the LDR signal.

```text
                 LDR
                  │
                  ●──────── ADC
                  │
               ┌──┴──┐
               │ 1 µF│
               └──┬──┘
                  │
               34.7 kΩ
                  │
                 GND
```

The capacitor and the Thevenin equivalent resistance of the voltage divider form a **first-order RC low-pass filter**. The resistance seen by the capacitor is:

$$
R_{TH} = R_{LDR} \parallel 34.7\ \text{k}\Omega
$$

Since the LDR resistance changes with illumination, the filter's time constant also changes.

### At Maximum LDR Resistance

For $R_{LDR} = 170\ \text{k}\Omega$:

$$
R_{TH} = 170\ \text{k}\Omega \parallel 34.7\ \text{k}\Omega \approx 28.78\ \text{k}\Omega
$$

$$
\tau = R_{TH} \cdot C = (28.78\ \text{k}\Omega)(1\ \mu\text{F}) \approx 28.8\ \text{ms}
$$

The corresponding cutoff frequency is:

$$
f_c = \frac{1}{2\pi\tau} \approx 5.53\ \text{Hz}
$$

### At Minimum LDR Resistance

For $R_{LDR} = 3.5\ \text{k}\Omega$:

$$
R_{TH} = 3.5\ \text{k}\Omega \parallel 34.7\ \text{k}\Omega \approx 3.18\ \text{k}\Omega
$$

$$
\tau = (3.18\ \text{k}\Omega)(1\ \mu\text{F}) \approx 3.18\ \text{ms}
$$

$$
f_c = \frac{1}{2\pi(3.18\ \text{ms})} \approx 50.1\ \text{Hz}
$$

### Summary

Because the LDR resistance changes with illumination, the RC filter has an approximate operating range of:

$$
3.18\ \text{ms} \leq \tau \leq 28.8\ \text{ms}
$$

$$
5.53\ \text{Hz} \leq f_c \leq 50.1\ \text{Hz}
$$

The capacitor therefore attenuates the rapid variations in the LDR signal caused by the **100 Hz AC lamp waveform**. The TRIAC repeats the same phase-control pattern every half-cycle, and from the perspective of the light intensity, the positive and negative half-cycles produce the same effect.

Therefore, the optical signal can be considered approximately proportional to the **absolute value of the phase-controlled AC waveform**. Its Fourier series contains a **DC component**, a **100 Hz component**, and higher harmonics.

With the filter's cutoff frequency reaching approximately **50 Hz at the lowest LDR resistance**, the 100 Hz component and higher-frequency harmonics are significantly attenuated, leaving a much smoother signal that is closer to the **average light intensity**.

This hardware filtering is particularly useful because the lamp is controlled using **AC phase control**. During each half-cycle, the TRIAC is fired at a specific point in the waveform, meaning that the lamp's instantaneous light output varies significantly within each cycle. Without filtering, these variations would appear directly in the ESP32 ADC measurements.

Using the capacitor allows the sensor signal to be smoothed **before it reaches the ADC**, reducing the amount of averaging that needs to be performed in software.
