# TRIAC Phase-Controlled Light Dimmer

## Overview

To control the brightness of an AC light bulb, I used **TRIAC phase-angle control**. The idea is to control how much of each 50 Hz AC cycle is delivered to the lamp.

Instead of applying the entire sine wave to the bulb, the TRIAC is triggered after a controlled delay following each zero crossing. This cuts off the beginning of each half-cycle, reducing the RMS voltage delivered to the lamp and therefore reducing its brightness.

## Operating Principle

The mains waveform crosses 0 V twice during every 50 Hz cycle, once every half-cycle.

I use the **zero crossing as a timing reference**. When a zero crossing is detected, the controller starts counting a delay. After the desired delay, it sends a short gate pulse to the TRIAC.

The later the TRIAC is triggered within the half-cycle, the smaller the portion of the sine wave delivered to the lamp.


```

The TRIAC then remains conducting for the rest of that half-cycle, until the AC current naturally falls below its holding current at the next zero crossing.

## Hardware

The hardware is divided into two main circuits:

### 1. Zero-Crossing Detection Circuit

This circuit detects when the AC waveform passes through approximately 0 V.

The detected zero crossing is sent to the microcontroller and used as a **timing milestone**. From this event, the controller can measure the required firing delay for the TRIAC.

The detailed zero-crossing detector schematic is provided in this folder.

### 2. TRIAC Firing Circuit

This circuit is responsible for triggering the TRIAC at the desired point in each AC half-cycle.

The microcontroller determines the firing delay based on the requested brightness and generates a gate-control pulse after that delay. An optocoupler is used to provide electrical isolation between the low-voltage control electronics and the mains-side switching circuit.

The detailed TRIAC firing circuit schematic is provided in this folder.

## Control Sequence

For every AC half-cycle:

1. Detect the zero crossing.
2. Start a timer.
3. Calculate/use the required firing delay.
4. Wait for the delay.
5. Send a gate pulse to the TRIAC.
6. The TRIAC conducts for the remainder of the half-cycle.
7. Repeat at the next zero crossing.

At 50 Hz, each complete AC cycle is approximately **20 ms**, giving approximately **10 ms per half-cycle**.


```

## Safety

This project interfaces with **AC mains voltage**, which can be lethal. The mains-side circuitry must be properly isolated from the low-voltage control electronics, and appropriate PCB creepage, clearance, insulation, fusing, enclosure, and testing procedures must be used.
