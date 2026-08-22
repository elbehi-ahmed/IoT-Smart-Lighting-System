
int zerocross = 17;
int optocoupler = 16;
#define LDR_PIN 34  // GPIO 34 (ADC1 channel 6)
int last=0;
volatile int brightness = 50;//start at 12 for visible results
int mapBrightness(int appbrightness) {
    // newBrightness: 0 to 100
    // Map to oldBrightness: 20 to 100
    int realbrightness = 20 + (appbrightness * 80 / 100);
    return realbrightness;
}

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
hw_timer_t *timer = NULL;
hw_timer_t *timer2 = NULL;

void IRAM_ATTR pulseISR(){
    digitalWrite(optocoupler, LOW);
}

void IRAM_ATTR timerISR()
{
    digitalWrite(optocoupler, HIGH);

    timerWrite(timer2, 0);
    timerAlarm(timer2, 75, false, 0);
}


void IRAM_ATTR zcfunction()
{
    if (brightness != 0)
    {
        timerWrite(timer, 0);
        timerAlarm(timer, delayTable[mapBrightness(brightness)], false, 0);
    }
}

void setup()
   { 
    pinMode(zerocross, INPUT);
    pinMode(optocoupler, OUTPUT);
    digitalWrite(optocoupler, LOW);
    attachInterrupt(digitalPinToInterrupt(zerocross),zcfunction, CHANGE);
    timer = timerBegin(1000000);
    timer2 = timerBegin(1000000);
    timerAttachInterrupt(timer, timerISR);
    timerAttachInterrupt(timer2, pulseISR);
    timerStart(timer);
    timerStart(timer2);
    Serial.begin(115200);
  
  // Set ADC attenuation to 11dB to measure up to ~3.9V (full 0-3.3V range)
  analogSetAttenuation(ADC_11db);
  
  // Optional: Set ADC resolution (default is 12-bit, 0-4095)
  // analogReadResolution(12);
   }

void loop(){
    /*if ((brightness<=90)&&(millis()-last>=1000)){
   brightness=brightness+5;
   last=millis();}*/
   if (millis()>=4000){
    brightness=90;}
    if (millis()-last>=10){int rawValue = analogRead(LDR_PIN);
  Serial.println(rawValue);
  last=millis();}

}
