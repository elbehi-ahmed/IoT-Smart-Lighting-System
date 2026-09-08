const int ZERO_CROSS_PIN = 17;
int lastState = HIGH;
void setup() {
  Serial.begin(115200);
  pinMode(ZERO_CROSS_PIN, INPUT);
}

void loop() {
  int state = digitalRead(ZERO_CROSS_PIN);

  // Detect HIGH -> LOW transition
  if (lastState == HIGH && state == LOW) {
    Serial.print("Zero crossing at: ");
    Serial.print(micros());
    Serial.println(" us");
  }

  lastState = state;
}
