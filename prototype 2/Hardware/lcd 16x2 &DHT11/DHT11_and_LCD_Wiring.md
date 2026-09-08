# DHT11 and 16×2 LCD Wiring

## DHT11

The DHT11 is powered from the ESP32's **5V pin**. A **10 kΩ pull-up resistor** is connected between the 5V supply and the DHT11 data line.

## 16×2 LCD

The 16×2 LCD is connected to the ESP32 using **4-bit mode**, using the RS, E, D4, D5, D6, and D7 control/data lines.
theres also a 20 k pot resistor controlling the contrast of the screen for tuning
