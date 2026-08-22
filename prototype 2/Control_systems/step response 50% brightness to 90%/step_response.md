| Parameter                  |              Value | Meaning                                                       |
| -------------------------- | -----------------: | ------------------------------------------------------------- |
| **Sampling period**        |          **10 ms** | Time between consecutive LDR measurements                     |
| **Initial value**          |      **≈2958 ADC** | LDR output before the brightness step                         |
| **Final value**            |      **≈3568 ADC** | LDR output after the response settles                         |
| **Step magnitude**         |       **≈610 ADC** | Change in LDR output: 3568 − 2958                             |
| **10% level**              |      **≈3019 ADC** | 10% of the total response change                              |
| **90% level**              |      **≈3507 ADC** | 90% of the total response change                              |
| **Rise time (10–90%)**     |         **≈60 ms** | Time for the response to rise from 10% to 90%                 |
| **Settling band (±2%)**    | **≈3556–3580 ADC** | Acceptable range around the final value                       |
| **Settling time (±2%)**    |        **≈120 ms** | Time required to enter and remain within the ±2% band         |
| **Steady-state value**     |      **≈3568 ADC** | Final stable LDR output                                       |
| **Steady-state variation** |        **≈±5 ADC** | Small fluctuations around the final value                     |
| **Dead time**              |       **≈0–10 ms** | Approximate delay before a measurable response                |
| **Response type**          |      **Monotonic** | Response increases toward the final value without oscillation |
| **Linearity**              |      **Nonlinear** | LDR output is not proportional to brightness                  |
| **Test configuration**     |      **Open-loop** | No PID/controller feedback was active                         |
