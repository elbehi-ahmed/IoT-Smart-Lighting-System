#define LDR_PIN 34

const int zerocross = 17;
const int optocoupler = 16;

// ============================================================
// USER SETTINGS
// ============================================================

const unsigned long SAMPLE_INTERVAL = 10;   // ADC sample every 10 ms
const unsigned long NOISE_TIME = 10000;     // 10 seconds
const unsigned long SETTLE_TIME = 3000;     // wait after changing brightness
const unsigned long STEP_TIME = 2000;       // record each step for 2 sec

// ============================================================
// YOUR ORIGINAL BRIGHTNESS MAPPING
// ============================================================

int mapBrightness(int appbrightness) {

  // Map app brightness 0-100 to real brightness 20-100
  int realbrightness = 20 + (appbrightness * 80 / 100);

  return realbrightness;
}

// ============================================================
// YOUR ORIGINAL DELAY TABLE
// ============================================================

const uint16_t delayTable[101] = {
  9867, 9862, 9846, 9815, 9770, 9708, 9629, 9533, 9419, 9287,
  9135, 8964, 8774, 8669, 8607, 8540, 8468, 8392, 8332, 8268,
  8199, 8136, 8075, 8009, 7949, 7887, 7825, 7765, 7704, 7644,
  7583, 7523, 7463, 7404, 7344, 7285, 7226, 7167, 7107, 7048,
  6988, 6929, 6870, 6811, 6751, 6691, 6631, 6571, 6511, 6451,
  6390, 6329, 6268, 6207, 6144, 6082, 6020, 5957, 5894, 5830,
  5765, 5701, 5635, 5569, 5502, 5435, 5367, 5298, 5228, 5158,
  5086, 5013, 4940, 4865, 4789, 4712, 4633, 4552, 4471, 4387,
  4300, 4213, 4122, 4029, 3933, 3834, 3732, 3626, 3514, 3398,
  3275, 3145, 3007, 2858, 2697, 2518, 2316, 2080, 1788, 1377,
  1000
};

// ============================================================
// TRIAC TIMERS
// ============================================================

hw_timer_t *timer = NULL;
hw_timer_t *timer2 = NULL;

volatile int brightness = 0;


// End TRIAC pulse
void IRAM_ATTR pulseISR() {

  digitalWrite(optocoupler, LOW);
}


// Start TRIAC pulse
void IRAM_ATTR timerISR() {

  digitalWrite(optocoupler, HIGH);

  timerWrite(timer2, 0);
  timerAlarm(timer2, 75, false, 0);
}


// Zero crossing
void IRAM_ATTR zcfunction() {

  if (brightness != 0) {

    timerWrite(timer, 0);

    timerAlarm(
      timer,
      delayTable[mapBrightness(brightness)],
      false,
      0
    );
  }
}


// ============================================================
// SET BRIGHTNESS
// ============================================================

void setBrightness(int value) {

  value = constrain(value, 0, 100);

  brightness = value;
}


// ============================================================
// WAIT FOR SERIAL ENTER
// ============================================================

void waitForEnter() {

  Serial.println();
  Serial.println("Press ENTER to start...");

  while (Serial.available()) {
    Serial.read();
  }

  while (!Serial.available()) {
    delay(10);
  }

  while (Serial.available()) {
    Serial.read();
  }
}


// ============================================================
// EXPERIMENT 1
// LDR NOISE
// ============================================================

void experimentNoise() {

  Serial.println();
  Serial.println("=== LDR NOISE EXPERIMENT ===");

  Serial.println("Set the desired brightness using the menu.");
  Serial.println("The lamp will remain constant.");
  Serial.println("ADC will be recorded for 10 seconds.");

  Serial.println();
  Serial.println("Enter brightness 0-100:");

  while (!Serial.available()) {
    delay(10);
  }

  int b = Serial.parseInt();
  setBrightness(b);

  while (Serial.available()) {
    Serial.read();
  }

  delay(3000);

  Serial.println();
  Serial.println("time_ms,brightness,ADC");

  unsigned long start = millis();
  unsigned long lastSample = 0;

  while (millis() - start < NOISE_TIME) {

    if (millis() - lastSample >= SAMPLE_INTERVAL) {

      lastSample = millis();

      int adc = analogRead(LDR_PIN);

      Serial.print(millis() - start);
      Serial.print(",");
      Serial.print(brightness);
      Serial.print(",");
      Serial.println(adc);
    }
  }

  setBrightness(0);

  Serial.println("END");
}


// ============================================================
// EXPERIMENT 2
// FILTERING / RESPONSE
// ============================================================

void experimentFiltering() {

  Serial.println();
  Serial.println("=== LDR RESPONSE / FILTERING EXPERIMENT ===");

  Serial.println("This records the sensor response when brightness changes.");
  Serial.println("Start: 20%");
  Serial.println("Final: 80%");

  waitForEnter();

  setBrightness(20);

  delay(3000);

  Serial.println();
  Serial.println("time_ms,brightness,ADC");

  unsigned long start = millis();
  unsigned long lastSample = 0;

  setBrightness(80);

  while (millis() - start < 2000) {

    if (millis() - lastSample >= SAMPLE_INTERVAL) {

      lastSample = millis();

      int adc = analogRead(LDR_PIN);

      Serial.print(millis() - start);
      Serial.print(",");
      Serial.print(brightness);
      Serial.print(",");
      Serial.println(adc);
    }
  }

  setBrightness(0);

  Serial.println("END");
}


// ============================================================
// EXPERIMENT 3
// BRIGHTNESS CURVE
// ============================================================

void experimentBrightnessCurve() {

  Serial.println();
  Serial.println("=== BRIGHTNESS -> LDR CURVE ===");

  Serial.println("Each brightness level will be applied.");
  Serial.println("Wait for settling, then ADC is averaged.");

  Serial.println();
  Serial.println("brightness,real_brightness,delay_us,average_ADC");

  for (int b = 0; b <= 100; b += 5) {

    setBrightness(b);

    delay(SETTLE_TIME);

    long sum = 0;

    const int samples = 100;

    for (int i = 0; i < samples; i++) {

      sum += analogRead(LDR_PIN);

      delay(10);
    }

    float average = (float)sum / samples;

    int realBrightness = mapBrightness(b);

    unsigned int delayValue = delayTable[realBrightness];

    Serial.print(b);
    Serial.print(",");
    Serial.print(realBrightness);
    Serial.print(",");
    Serial.print(delayValue);
    Serial.print(",");
    Serial.println(average);
  }

  setBrightness(0);

  Serial.println("END");
}


// ============================================================
// EXPERIMENT 4
// HYSTERESIS
// ============================================================

void experimentHysteresis() {

  Serial.println();
  Serial.println("=== HYSTERESIS EXPERIMENT ===");

  Serial.println("Increasing brightness:");

  Serial.println();
  Serial.println("direction,brightness,average_ADC");

  for (int b = 0; b <= 100; b += 5) {

    setBrightness(b);

    delay(SETTLE_TIME);

    long sum = 0;

    for (int i = 0; i < 100; i++) {

      sum += analogRead(LDR_PIN);

      delay(10);
    }

    Serial.print("UP,");
    Serial.print(b);
    Serial.print(",");
    Serial.println((float)sum / 100.0);
  }


  Serial.println();
  Serial.println("Decreasing brightness:");

  for (int b = 100; b >= 0; b -= 5) {

    setBrightness(b);

    delay(SETTLE_TIME);

    long sum = 0;

    for (int i = 0; i < 100; i++) {

      sum += analogRead(LDR_PIN);

      delay(10);
    }

    Serial.print("DOWN,");
    Serial.print(b);
    Serial.print(",");
    Serial.println((float)sum / 100.0);
  }

  setBrightness(0);

  Serial.println("END");
}


// ============================================================
// ONE STEP RESPONSE
// ============================================================

void runStep(int startBrightness, int finalBrightness) {

  Serial.println();
  Serial.print("STEP ");
  Serial.print(startBrightness);
  Serial.print(" -> ");
  Serial.println(finalBrightness);

  setBrightness(startBrightness);

  delay(3000);

  Serial.println("time_ms,brightness,ADC");

  unsigned long start = millis();
  unsigned long lastSample = 0;

  setBrightness(finalBrightness);

  while (millis() - start < STEP_TIME) {

    if (millis() - lastSample >= SAMPLE_INTERVAL) {

      lastSample = millis();

      int adc = analogRead(LDR_PIN);

      Serial.print(millis() - start);
      Serial.print(",");
      Serial.print(brightness);
      Serial.print(",");
      Serial.println(adc);
    }
  }

  setBrightness(0);

  delay(2000);
}


// ============================================================
// EXPERIMENT 5
// STEP RESPONSES
// ============================================================

void experimentSteps() {

  Serial.println();
  Serial.println("=== STEP RESPONSE EXPERIMENTS ===");

  Serial.println("You will get:");
  Serial.println("5 -> 20");
  Serial.println("20 -> 40");
  Serial.println("40 -> 60");
  Serial.println("60 -> 80");
  Serial.println("80 -> 100");

  waitForEnter();

  runStep(5, 20);
  runStep(20, 40);
  runStep(40, 60);
  runStep(60, 80);
  runStep(80, 100);

  Serial.println("ALL STEP RESPONSES COMPLETE");
}


// ============================================================
// EXPERIMENT 6
// REPEATABILITY
// ============================================================

void experimentRepeatability() {

  Serial.println();
  Serial.println("=== STEP REPEATABILITY ===");

  Serial.println("Repeating 40 -> 60 three times.");

  waitForEnter();

  for (int i = 1; i <= 3; i++) {

    Serial.println();
    Serial.print("REPEAT ");
    Serial.println(i);

    runStep(40, 60);
  }

  Serial.println("END");
}


// ============================================================
// EXPERIMENT 7
// AMBIENT DISTURBANCE
// ============================================================

void experimentAmbient() {

  Serial.println();
  Serial.println("=== AMBIENT LIGHT DISTURBANCE ===");

  Serial.println("Lamp will be fixed at 50%.");
  Serial.println("You will have 10 seconds to disturb the ambient light.");
  Serial.println("For example: turn room light on/off or block/unblock room light.");

  waitForEnter();

  setBrightness(50);

  delay(3000);

  Serial.println();
  Serial.println("START DISTURBANCE NOW");
  Serial.println("time_ms,brightness,ADC");

  unsigned long start = millis();
  unsigned long lastSample = 0;

  while (millis() - start < 10000) {

    if (millis() - lastSample >= SAMPLE_INTERVAL) {

      lastSample = millis();

      int adc = analogRead(LDR_PIN);

      Serial.print(millis() - start);
      Serial.print(",");
      Serial.print(brightness);
      Serial.print(",");
      Serial.println(adc);
    }
  }

  setBrightness(0);

  Serial.println("END");
}


// ============================================================
// EXPERIMENT 8
// SENSOR SHIELD TEST
// ============================================================

void experimentShield() {

  Serial.println();
  Serial.println("=== SENSOR SHIELD TEST ===");

  Serial.println("Lamp fixed at 50%.");
  Serial.println("First measure WITHOUT shield.");
  Serial.println("Then repeat with your black cone.");

  waitForEnter();

  setBrightness(50);

  delay(3000);

  Serial.println();
  Serial.println("WITHOUT_SHIELD");
  Serial.println("time_ms,ADC");

  unsigned long start = millis();
  unsigned long lastSample = 0;

  while (millis() - start < 5000) {

    if (millis() - lastSample >= SAMPLE_INTERVAL) {

      lastSample = millis();

      Serial.print(millis() - start);
      Serial.print(",");
      Serial.println(analogRead(LDR_PIN));
    }
  }

  Serial.println();
  Serial.println("Now install the black cone.");
  Serial.println("Press ENTER when ready.");

  waitForEnter();

  Serial.println();
  Serial.println("WITH_SHIELD");
  Serial.println("time_ms,ADC");

  start = millis();
  lastSample = 0;

  while (millis() - start < 5000) {

    if (millis() - lastSample >= SAMPLE_INTERVAL) {

      lastSample = millis();

      Serial.print(millis() - start);
      Serial.print(",");
      Serial.println(analogRead(LDR_PIN));
    }
  }

  setBrightness(0);

  Serial.println("END");
}


// ============================================================
// EXPERIMENT 9
// ACTUATOR RESOLUTION
// ============================================================

void experimentResolution() {

  Serial.println();
  Serial.println("=== ACTUATOR RESOLUTION ===");

  Serial.println("Testing small changes around 50%.");

  Serial.println();
  Serial.println("brightness,average_ADC");

  for (int b = 45; b <= 55; b++) {

    setBrightness(b);

    delay(SETTLE_TIME);

    long sum = 0;

    for (int i = 0; i < 100; i++) {

      sum += analogRead(LDR_PIN);

      delay(10);
    }

    Serial.print(b);
    Serial.print(",");
    Serial.println((float)sum / 100.0);
  }

  setBrightness(0);

  Serial.println("END");
}


// ============================================================
// EXPERIMENT 10
// SATURATION
// ============================================================

void experimentSaturation() {

  Serial.println();
  Serial.println("=== SATURATION TEST ===");

  Serial.println("Testing 0% and 100%.");

  Serial.println();
  Serial.println("brightness,average_ADC");

  setBrightness(0);

  delay(3000);

  long sum0 = 0;

  for (int i = 0; i < 100; i++) {

    sum0 += analogRead(LDR_PIN);

    delay(10);
  }

  Serial.print("0,");
  Serial.println((float)sum0 / 100.0);


  setBrightness(100);

  delay(3000);

  long sum100 = 0;

  for (int i = 0; i < 100; i++) {

    sum100 += analogRead(LDR_PIN);

    delay(10);
  }

  Serial.print("100,");
  Serial.println((float)sum100 / 100.0);

  setBrightness(0);

  Serial.println("END");
}


// ============================================================
// MENU
// ============================================================

void printMenu() {

  Serial.println();
  Serial.println("================================");
  Serial.println("       PLANT EXPERIMENTS");
  Serial.println("================================");

  Serial.println("1 - LDR noise");
  Serial.println("2 - LDR response / filtering");
  Serial.println("3 - Brightness -> LDR curve");
  Serial.println("4 - Hysteresis");
  Serial.println("5 - Step responses");
  Serial.println("6 - Step repeatability");
  Serial.println("7 - Ambient disturbance");
  Serial.println("8 - Black cone comparison");
  Serial.println("9 - Actuator resolution");
  Serial.println("10 - Saturation");

  Serial.println();
  Serial.println("Enter experiment number:");
}


// ============================================================
// SETUP
// ============================================================

void setup() {

  Serial.begin(115200);

  pinMode(zerocross, INPUT);
  pinMode(optocoupler, OUTPUT);

  digitalWrite(optocoupler, LOW);

  attachInterrupt(
    digitalPinToInterrupt(zerocross),
    zcfunction,
    CHANGE
  );

  timer = timerBegin(1000000);
  timer2 = timerBegin(1000000);

  timerAttachInterrupt(timer, timerISR);
  timerAttachInterrupt(timer2, pulseISR);

  timerStart(timer);
  timerStart(timer2);

  analogSetAttenuation(ADC_11db);

  setBrightness(0);

  Serial.println();
  Serial.println("READY");
}


// ============================================================
// LOOP
// ============================================================

void loop() {

  if (Serial.available()) {

    int experiment = Serial.parseInt();

    while (Serial.available()) {
      Serial.read();
    }

    switch (experiment) {

      case 1:
        experimentNoise();
        break;

      case 2:
        experimentFiltering();
        break;

      case 3:
        experimentBrightnessCurve();
        break;

      case 4:
        experimentHysteresis();
        break;

      case 5:
        experimentSteps();
        break;

      case 6:
        experimentRepeatability();
        break;

      case 7:
        experimentAmbient();
        break;

      case 8:
        experimentShield();
        break;

      case 9:
        experimentResolution();
        break;

      case 10:
        experimentSaturation();
        break;

      default:
        Serial.println("Invalid experiment.");
        break;
    }

    printMenu();
  }
}