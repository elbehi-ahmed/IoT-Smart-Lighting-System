## Zero-Crossing Detection

This was one of the most interesting parts of the entire project in my opinion. I knew I needed a way to pass the information **"the mains voltage is at 0 V right now"** down to the ESP32, since this zero crossing would be my timing reference for firing the TRIAC.

First of all, I obviously can't work directly with the 220 V mains. I therefore used a **transformer** to step the voltage down to a safer AC voltage while also providing **galvanic isolation** from the mains.

The transformer I used was rated for **12 V**, but I quickly found out that this was its voltage at its nominal load, not its no-load voltage. With no load connected, I measured approximately **15.4 V RMS** on the secondary.

Now that I had a much safer AC waveform to work with, I had to decide how to detect when it crossed 0 V.

I considered two approaches:

* **Using the ESP32 ADC:** I could sample the AC waveform and determine experimentally which ADC value corresponded to 0 V, for example **598**. Values below that would represent the negative half-cycle, while values above it would represent the positive half-cycle.
* **Using an analog comparator:** The comparator could directly compare the AC waveform against 0 V and produce a digital signal that the ESP32 could read through a GPIO.

The **comparator solution was ultimately the better choice**. The ADC approach would introduce ADC nonlinearity and would require the firmware to continuously sample and interpret the waveform. With the comparator, the analog problem is handled in hardware, and the ESP32 only needs to detect when the comparator switches state.

This also greatly simplifies the firmware: instead of continuously analyzing ADC measurements, I can simply use a **GPIO interrupt** to detect the zero-crossing event and use that event as the starting point for the TRIAC firing delay.

### Designing the Circuit

Now that I had a proper logical architecture, I had to design the actual circuit. I used the **LM393** as my comparator, mainly because of its **open-collector output**, which makes interfacing its output with the ESP32 much easier. This also fits my implementation, where the LM393 and ESP32 operate at different voltages: **5 V and 3.3 V** respectively.

According to the LM393 datasheet, its input voltage needs to remain within its specified common-mode input range. With the comparator powered from a positive supply and ground, this meant I could not simply feed the full **15.4 V RMS** transformer output directly into its input.

More importantly, I couldn't directly compare the AC signal to **0 V** the way I originally imagined. The comparator inputs need to remain within their allowed voltage range, which in my implementation is approximately **0 to VCC − 1.5 V**. With a 5 V supply, this gives an input range of approximately **0 to 3.5 V**.

This meant I needed to **scale and shift the AC waveform** before feeding it to the comparator. After scaling, the original 0 V crossing of the AC waveform would no longer sit at 0 V electrically. Instead, the entire waveform would be shifted upward so that the actual zero crossing would occur somewhere around **1–2 V**.

That shifted voltage would then become my comparator reference. When the scaled AC waveform crossed this reference, the LM393 would switch its output state, giving the ESP32 a clean digital indication of the zero crossing.

Now I could actually draw the schematic.

I first used a **voltage divider** to reduce the 15.4 V RMS transformer output down to approximately **0.92 V RMS**, corresponding to approximately **1.3 V peak amplitude**. I initially kept the upper resistor as a variable **X kΩ** value, with a **1 kΩ** resistor on the lower side.

I deliberately kept **X** as a variable at this stage because I wanted to compensate for the **loading effect** that the rest of the circuit would have on the voltage divider. The final value could therefore be calculated after taking the circuit's input impedance into account, rather than assuming that the divider would behave as an ideal unloaded divider.

After the voltage divider, I added a **10 µF coupling capacitor**. Its purpose was to block the DC component while allowing the 50 Hz AC signal to pass. This allowed the AC signal to ride on top of a DC reference voltage, effectively shifting the entire waveform upward instead of having it swing around 0 V.

I therefore needed two identical reference voltages: one for the sine wave to ride on, and another for the comparator to compare it against. I chose the reference voltage to be approximately **1.66 V**, which gives the sine wave enough room to swing while remaining safely away from both the **0 V** and **5 V** rails.

To generate the two identical reference voltages, I used another voltage divider powered from the same **5 V supply** used by the LM393. The divider consists of **24 kΩ and 12 kΩ** resistors, producing approximately **1.66 V**.

I also deliberately chose resistor values for this reference divider that were higher than those used in the initial transformer voltage divider, in order to **minimize the loading effect** on the circuit.

### Hysteresis and Final Calculations

I also considered adding a small amount of **hysteresis** to prevent the comparator from double-triggering around the zero crossing due to noise or small fluctuations in the signal. After calculating the input impedance, I found that using a **1 MΩ feedback resistor** from the output to the positive input would provide approximately **33 mV of hysteresis**. This seemed like a good compromise: enough hysteresis to provide some protection against double triggering without significantly affecting the zero-crossing detection precision.

A **pull-up resistor to 3.3 V** was then used on the LM393's open-collector output to interface it with the ESP32. This allowed the LM393 to operate from **5 V** while keeping the digital signal seen by the ESP32 at **3.3 V**.

At this point, the remaining issues were the **phase shift introduced by the coupling capacitor** and the **X value** that I had left open earlier.

Because the capacitor has imaginary impedance, the signal seen by the LM393 is not exactly the same waveform as the original transformer output. There is a small phase shift, meaning that the comparator's switching point is slightly displaced from the actual zero crossing.

After performing a **Thevenin equivalent analysis** of the circuit, I found that **X ≈ 14 kΩ** was required to still deliver approximately **0.92 V RMS** to the coupling stage while accounting for the loading of the circuit.

The resulting phase shift was approximately **2.73°**, which at **50 Hz** corresponds to approximately **151 µs**. This value is small enough to be taken into account by the firmware as a fixed timing offset.

With this, the zero-crossing detection circuit was complete: the transformer provides isolation and a safer voltage, the divider scales the waveform, the coupling capacitor shifts it around the reference voltage, the LM393 converts the zero crossing into a clean digital transition, and the ESP32 receives that transition through a GPIO interrupt.
all the calculations r in the file 0cross calculations check them out, also the enitre circuit is simulated on proteus as shown
