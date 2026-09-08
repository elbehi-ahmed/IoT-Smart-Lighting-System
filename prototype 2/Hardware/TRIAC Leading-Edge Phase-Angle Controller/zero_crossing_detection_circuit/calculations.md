# Voltage Divider and Loading Calculation

The transformer takes the **220 V RMS** mains voltage and steps it down to approximately **15.4 V RMS**.

The first voltage divider consists of an unknown resistor $R_x$ and a $1\,\text{k}\Omega$ resistor. At this stage, $R_x$ is intentionally left unknown because the loading effect of the rest of the circuit must first be taken into account.

The target voltage after the divider is approximately:

$$
V_{out} = 0.93\,\text{V RMS}
$$

or, in terms of peak amplitude:

$$
V_{out,peak} = 0.93\sqrt{2} \approx 1.3\,\text{V}
$$

Therefore, the initial divider can be represented as:

$$
15.4\,\text{V RMS} \xrightarrow{R_x \text{ and } 1\,\text{k}\Omega} 0.93\,\text{V RMS}
$$

At this point, $R_x$ is left as an unknown.

              Vx
              │
              │
            ┌─┴─┐
            │ X │
            └─┬─┘
              │
              ●
              │
       ┌──────┴──────┐
       │             │
     ┌─┴─┐          ┌┴─┐
     │1kΩ│          │ Z│
     └─┬─┘          └┬─┘
       │             │
       └──────┬──────┘
              │
              │
             GND
---


## Calculating the Load Impedance Z

The divider is not unloaded. The circuit connected after it consists of the $24\,\text{k}\Omega$ and $12\,\text{k}\Omega$ reference resistors in parallel, followed by the $10\,\mu\text{F}$ coupling capacitor.

First, the equivalent resistance of the reference divider is:

$$
R_{eq} = 24\,\text{k}\Omega \parallel 12\,\text{k}\Omega = \frac{24 \times 12}{24 + 12}\,\text{k}\Omega = 8\,\text{k}\Omega
$$

The impedance of the $10\,\mu\text{F}$ capacitor at $50\,\text{Hz}$ is:

$$
Z_C = \frac{1}{j2\pi fC} = \frac{1}{j2\pi(50)(10 \times 10^{-6})} \approx -j318.3\,\Omega
$$

Therefore, the total impedance seen by the voltage divider is:

$$
Z_{load} = 8\,\text{k}\Omega - j318.3\,\Omega
$$

or equivalently,

$$
\boxed{Z_{load} \approx 8\,\text{k}\Omega - j0.318\,\text{k}\Omega}
$$

Its magnitude is approximately:

$$
|Z_{load}| = \sqrt{(8000)^2 + (318.3)^2} \approx 8006\,\Omega
$$

$$
\boxed{|Z_{load}| \approx 8.01\,\text{k}\Omega}
$$

---

## Thevenin Equivalent

The transformer-side circuit can now be replaced by its Thevenin equivalent. The load seen by the $R_x$-$1\,\text{k}\Omega$ divider is:

$$
Z_{load} = 8\,\text{k}\Omega - j0.318\,\text{k}\Omega
$$

This impedance is effectively in parallel with the $1\,\text{k}\Omega$ resistor of the original divider:

$$
Z_{eq} = 1\,\text{k}\Omega \parallel Z_{load} = \frac{(1000)(8000 - j318.3)}{1000 + (8000 - j318.3)}
$$

which gives approximately:

$$
\boxed{Z_{eq} \approx 889 - j31.5\,\Omega}
$$

---

## Calculating $R_x$

The original $R_x$-$1\,\text{k}\Omega$ divider can therefore be treated as an $R_x$ resistor feeding this loaded equivalent impedance $Z_{eq}$.

The output voltage is given by the voltage divider equation:

$$
V_{out} = V_{in} \times \frac{Z_{eq}}{R_x + Z_{eq}}
$$

We know:

- $V_{in} = 15.4\,\text{V RMS}$
- $V_{out} = 0.93\,\text{V RMS}$
- $Z_{eq} \approx 889 - j31.5\,\Omega$

Solving for $R_x$:

$$
0.93 = 15.4 \times \frac{Z_{eq}}{R_x + Z_{eq}}
$$

$$
\frac{0.93}{15.4} = \frac{Z_{eq}}{R_x + Z_{eq}} = 0.06039
$$

$$
R_x + Z_{eq} = \frac{Z_{eq}}{0.06039}
$$

$$
R_x = Z_{eq}\left(\frac{1}{0.06039} - 1\right) = Z_{eq} \times (16.56 - 1) = Z_{eq} \times 15.56
$$

Now substitute:

$$
Z_{eq} \approx 889 - j31.5\,\Omega
$$

$$
R_x \approx (889 - j31.5) \times 15.56 \approx 13833 - j490\,\Omega
$$

Since $R_x$ is a resistor, we take the real part (the imaginary component is negligible, only 3.5% of the real part):

$$
\boxed{R_x \approx 13.83\,\text{k}\Omega}
$$

---

## Final Design Value

Using the nearest resistor value:

$$
\boxed{R_x \approx 14\,\text{k}\Omega}
$$

This value provides the required voltage division while accounting for the loading effect of the reference divider and coupling capacitor. The small imaginary component indicates that the capacitor's reactance has minimal effect on the total impedance.

---

## Hysteresis Calculation

A $1\,\text{M}\Omega$ feedback resistor is added to the comparator to introduce hysteresis. The resistor feeds a small portion of the comparator output back to the input node.

The $24\,\text{k}\Omega$ and $12\,\text{k}\Omega$ resistors form the reference divider. Their equivalent resistance is:

$$
R_{div} = 24\,\text{k}\Omega \parallel 12\,\text{k}\Omega = \frac{24 \times 12}{24 + 12}\,\text{k}\Omega
$$

$$
\boxed{R_{div} = 8\,\text{k}\Omega}
$$

The hysteresis resistor is:

$$
R_H = 1\,\text{M}\Omega
$$

The hysteresis voltage is determined by the fraction of the comparator output voltage that is fed back through the $1\,\text{M}\Omega$ resistor.

Therefore:

$$
V_H = V_{out} \times \frac{R_{div}}{R_H + R_{div}}
$$

Substituting the values:

$$
V_H = V_{out} \times \frac{8\,\text{k}\Omega}{1\,\text{M}\Omega + 8\,\text{k}\Omega} = V_{out} \times \frac{8000}{1{,}008{,}000} \approx 0.00794 \times V_{out}
$$

For a comparator output voltage of approximately $4.16\,\text{V}$:

$$
V_H \approx 4.16 \times 0.00794
$$

$$
\boxed{V_H \approx 33\,\text{mV}}
$$

Thus, the $1\,\text{M}\Omega$ feedback resistor introduces approximately **33 mV of hysteresis** at the comparator input.

---

## Input Impedance with the Hysteresis Resistor

Without the hysteresis resistor, the input node sees the $24\,\text{k}\Omega$ and $12\,\text{k}\Omega$ resistors in parallel:

$$
R_{in} = 24\,\text{k}\Omega \parallel 12\,\text{k}\Omega
$$

$$
\boxed{R_{in} = 8\,\text{k}\Omega}
$$

When the $1\,\text{M}\Omega$ hysteresis resistor is added, it appears in parallel with the existing $8\,\text{k}\Omega$ resistance.

Therefore:

$$
R_{in} = 8\,\text{k}\Omega \parallel 1\,\text{M}\Omega = \frac{8000 \times 1{,}000{,}000}{8000 + 1{,}000{,}000} \approx 7936.5\,\Omega
$$

$$
\boxed{R_{in} \approx 7.94\,\text{k}\Omega}
$$

The input impedance changes only slightly:

$$
8\,\text{k}\Omega \rightarrow 7.94\,\text{k}\Omega
$$

This shows that the $1\,\text{M}\Omega$ resistor has a **negligible effect on the input impedance**, while still providing approximately $33\,\text{mV}$ of hysteresis.

---

## Final Result

The addition of the $1\,\text{M}\Omega$ feedback resistor therefore gives:

$$
\boxed{R_H = 1\,\text{M}\Omega}
$$

$$
\boxed{V_H \approx 33\,\text{mV}}
$$

$$
\boxed{R_{in} \approx 7.94\,\text{k}\Omega}
$$

The $1\,\text{M}\Omega$ resistor therefore provides a small amount of positive feedback, creating approximately **33 mV of hysteresis** while only slightly reducing the effective input impedance of the reference network.