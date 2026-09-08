# Closed-Loop Control Study

## Closed-Loop Performance Specifications

For a step change in the light setpoint:

| Specification | Target |
|---------------|--------|
| Rise time (10% → 90%) | < 500 ms |
| Settling time (±5% of final value) | < 3 s |
| Overshoot | < 10% |
| Steady-state error | 0-2 |
| Stability margin | No sustained oscillations |

## 1. First-Order Approximation of the Plant

The open-loop step response of the lighting system was measured experimentally. The response was **monotonic**, with no significant oscillation or overshoot, and can therefore be approximated as a **first-order system** for controller-design purposes.

The plant can be approximated locally as:

```math
G(s)=\frac{K_{\text{plant}}}{\tau s+1}
```

However, experimental measurements showed that the plant parameters are **not constant over the operating range**. The static gain varies significantly with the brightness level, so a single fixed plant model cannot accurately represent the entire system.

For this reason, the controller gains were not calculated from a single fixed plant model. Instead, the PI parameters were **experimentally adjusted until a satisfactory closed-loop response was obtained**.

---

# 2. Incremental PI Controller Design

A **PI controller** was selected rather than a full PID controller. The integral action eliminates steady-state error, while the proportional term provides the main correction in response to changes in illumination.

The implemented controller uses an **incremental (velocity-form) PI algorithm**. Instead of directly calculating the complete control output from the current error, the controller calculates the change in the control output:

```math
\Delta u[k]
=
K_p\left(e[k]-e[k-1]\right)
+
K_i T_s e[k]
```

The control output is then updated according to:

```math
u[k]=u[k-1]+\Delta u[k]
```

where:

* $e[k]$ = current control error
* $e[k-1]$ = previous control error
* $u[k]$ = current TRIAC brightness command
* $u[k-1]$ = previous TRIAC brightness command
* $T_s$ = controller update period

## Proportional Gain

The proportional gain was selected experimentally:

```math
\boxed{K_p=0.02}
```

Rather than being obtained from a single theoretical plant model, the gain was adjusted experimentally while observing the response of the real lighting system.

## Integral Gain

The integral gain was also selected experimentally:

```math
\boxed{K_i=0.002}
```

The values were adjusted until the controller provided a stable response with acceptable tracking and without excessive brightness fluctuations.

---

# 3. Final PI Controller

The implemented controller therefore uses:

| Parameter               |              Value |
| ----------------------- | -----------------: |
| Plant                   |      **Nonlinear** |
| Proportional gain $K_p$ |           **0.02** |
| Integral gain $K_i$     |          **0.002** |
| Controller type         | **Incremental PI** |
| PID update period       |          **20 ms** |

The controller output is constrained to the available TRIAC brightness range:

```math
0\leq u\leq99
```

where $u$ represents the commanded brightness level.

---

# 4. Control Principle

The closed-loop system operates according to:

```math
e[k]=r[k]-y[k]
```

where:

* $r[k]$ = desired LDR value (setpoint)
* $y[k]$ = measured LDR ADC value
* $e[k]$ = control error

The incremental PI controller calculates the change in the control output:

```math
\Delta u[k]
=
K_p\left(e[k]-e[k-1]\right)
+
K_iT_s e[k]
```

The output is then updated:

```math
u[k]=u[k-1]+\Delta u[k]
```

The resulting $u[k]$ determines the commanded brightness of the TRIAC dimmer.

The complete feedback loop is:

```text
Desired illumination
        │
        ▼
   Setpoint r[k]
        │
        ▼
     ┌───────┐
     │ Error │◄──────────────────────┐
     │ r - y │                       │
     └───┬───┘                       │
         │                           │
         ▼                           │
   Incremental PI                    │
      Controller                     │
         │                           │
         ▼                           │
  TRIAC Brightness Command           │
         │                           │
         ▼                           │
       Lamp                          │
         │                           │
         ▼                           │
       Light                         │
         │                           │
         ▼                           │
        LDR                          │
         │                           │
         ▼                           │
       ADC ──────────────────────────┘
```

## Engineering Interpretation

When the measured illumination is below the desired level, the error becomes positive and the incremental PI controller increases the TRIAC brightness command. As the measured illumination approaches the setpoint, the change in the control output decreases. The integral action continues accumulating the error, allowing the controller to eliminate steady-state error.

The experimental system identification showed that the plant is **strongly nonlinear**: the measured static gain varies from approximately **6.5 to 24.8 ADC/%**, while the time constant varies from approximately **250 ms to 60 ms** depending on the operating range.

Therefore, the final PI gains were **selected experimentally on the actual hardware rather than derived from a single fixed plant model**. This was necessary because the system dynamics change significantly with the operating point.
