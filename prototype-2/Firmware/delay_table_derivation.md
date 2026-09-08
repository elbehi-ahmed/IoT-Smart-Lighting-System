### Delay Table — Brightness Control

For the `delayTable`, which controls the brightness percentage, I initially tried to make the firing delay from **0–10,000 µs proportional to the voltage delivered to the resistive load**.

The idea was that **half the average voltage would produce half the brightness**.

I therefore tried to calculate the firing angle $\alpha$ corresponding to a desired brightness percentage using:

```math
V_{\mathrm{avg}} = \frac{1}{\pi-\alpha} \int_{\alpha}^{\pi} \sin(t)\,dt
```

I normalized this voltage to the full-conduction case:

```math
V_{\mathrm{rel}} = \frac{ \displaystyle\frac{1}{\pi-\alpha}\int_{\alpha}^{\pi}\sin(t)\,dt }{ \displaystyle\frac{1}{\pi}\int_{0}^{\pi}\sin(t)\,dt }
```

and set:

```math
V_{\mathrm{rel}} = \text{desired percentage}
```

The problem was that I needed to solve this equation **backwards**: given a brightness percentage, I needed to find the corresponding firing angle $\alpha$ and therefore the required delay.

However, the resulting equation has no convenient analytical inverse, making it impractical to calculate the firing angle for every percentage. (no $f^{-1}(t)$)

I therefore switched to a **precomputed lookup table (`delayTable`)**, directly mapping each brightness percentage to a firing delay.

Experimentation showed that voltage proportionality did not produce the desired brightness response. The better approach was to use **power proportionality**, since for a resistive load:

```math
P(t) \propto V^2(t)
```

Using the normalized mains waveform:

```math
V(t)=\sin(t)
```

the relative power delivered after firing at $\alpha$ is:

```math
P_{\mathrm{rel}}=
\frac{
\displaystyle\int_{\alpha}^{\pi}\sin^2(t)\,dt
}{
\displaystyle\int_{0}^{\pi}\sin^2(t)\,dt
}
```

Using:

```math
\sin^2(t)=\frac{1-\cos(2t)}{2}
```

gives:

```math
P_{\mathrm{rel}}=
\frac{
\displaystyle\frac{\pi-\alpha}{2}+\frac{\sin(2\alpha)}{4}
}{
\displaystyle\frac{\pi}{2}
}
```

Since **power is proportional to brightness** for the resistive lamp:

```math
\mathrm{Brightness}=P_{\mathrm{rel}}
```

This relationship was used to generate the `delayTable`.

### Comparator Delay Compensation

A final correction was required because the comparator does not detect the actual zero crossing of the mains waveform. Its output is slightly **shifted in time** due to the detection circuit.

The measured phase shift was approximately **2.5°**, which corresponds to:

```math
\Delta t = \frac{2.5^\circ}{360^\circ}\times20\,\mathrm{ms}
```

```math
\Delta t \approx 139\,\mu s
```

Therefore, a constant delay correction was applied to the values in the `delayTable` to compensate for this hardware-induced timing shift.
