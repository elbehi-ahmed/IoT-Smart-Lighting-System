# Automatic Brightness Control

Automatic mode uses the LDR as feedback to maintain a target light
level.

## Control Architecture

```text
          TARGET_LIGHT
               |
               v
        +--------------+
        | Error        |
        | Target - LDR |
        +------+-------+
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
              LDR
               |
               +-------> Feedback
```

## Automatic Mode Initialization

When automatic mode is selected:

```text
Current Brightness
       |
       v
brightnessmanu
```

The current LDR reading is then used as the initial target:

```text
LDR Reading
    |
    v
TARGET_LIGHT
```

The integral term is reset before automatic control begins.

## Control Update

The PI controller is executed periodically while:

```text
mode == automatic
```

The current implementation uses a 20 ms control interval.

# PI Controller

The automatic lighting controller uses incremental proportional and integral
control.

The current parameters are:

```text
Kp = 0.02
Ki = 0.002
```

No derivative term is implemented, so the current controller is a
**PI controller**, not a complete PID controller.

## Measurement

```text
light = analogRead(LDR_PIN)
```

## Error

```text
error = TARGET_LIGHT - light
```

## Integral

```text
integral += error * dt
```

The integral is constrained to:

```text
-50 <= integral <= 50
```

## Controller Output

```text
correction =
    (Kp * error) +
    (Ki * integral)
```

The correction is applied to the current brightness:

```text
brightness += correction
```

The resulting brightness is constrained to:

```text
0 <= brightness <= 99
```

## Execution

The controller runs every:

```text
20 ms
```

## Control Loop

```text
LDR Measurement
      |
      v
    Error
      |
      v
 Proportional Term
      |
      +------+
      |      |
      v      v
 Integral  Kp*Error
      |
      v
   Ki*Integral
      |
      +------+
             |
             v
       PI Correction
             |
             v
         Brightness
             |
             v
       TRIAC Control
```
