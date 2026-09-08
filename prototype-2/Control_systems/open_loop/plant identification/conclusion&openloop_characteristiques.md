

### Final System Identification Summary – Statistical Table

| Operating Range (Command %) | Static Gain, \(K\) (ADC/%) | Time Constant, \(\tau\) (ms) | Dead Time, \(L\) (ms) | Rise Time (10→90%) (ms) | Settling Time (±2%) (ms) | Overshoot (%) |
|-----------------------------|---------------------------|-----------------------------|-----------------------|-------------------------|--------------------------|---------------|
| **5 → 20**                  | 6.53                      | 250                         | 10                    | 1460                    | >2000                    | 0             |
| **20 → 40**                 | 15.10                     | 170                         | 10                    | 445                     | 1800                     | 0             |
| **40 → 60**                 | 24.51                     | 110                         | 10                    | 280                     | 1200                     | 0             |
| **60 → 80**                 | 24.77                     | 80                          | 10                    | 165                     | 400                      | 0             |
| **80 → 100**                | 20.05                     | 60                          | 10                    | 100                     | 200                      | 0             |

---

### Repeatability (40→60 Step, 3 repetitions)

| Parameter | Mean ± Std. Dev. |
|-----------|------------------|
| Gain, \(K\) | \(24.11 \pm 0.13\) ADC/% |
| Time Constant, \(\tau\) | \(111.7 \pm 2.9\) ms |
| Rise Time | \(375 \pm 25\) ms |

---

### Verdict on Plant Linearity

- **\(K\)** varies from **6.5** to **24.8** (factor of ~4x).  
- **\(\tau\)** varies from **250** ms down to **60** ms (factor of ~4x).

**Conclusion:** The plant is **strongly nonlinear**. A single fixed-gain PI controller will not perform optimally across the whole range. **Gain scheduling** or **static non-linear mapping** is experimentally justified and required for consistent closed-loop performance.