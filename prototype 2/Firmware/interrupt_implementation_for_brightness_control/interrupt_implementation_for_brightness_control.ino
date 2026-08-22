
int zerocross = 17;
int optocoupler = 16;
#define LDR_PIN 34  // GPIO 34 (ADC1 channel 6)
int last=1;
volatile int brightness = 0;//start at 12 for visible results
int mapBrightness(int newBrightness) {
    // newBrightness: 0 to 100
    // Map to oldBrightness: 20 to 100
    int oldBrightness = 20 + (newBrightness * 80 / 100);
    return oldBrightness;
}
/*const uint16_t delayTable[101] = {

    10000, 8840, 8531, 8310, 8132, 7980, 7846, 7724, 7612, 7508,
    7411, 7319, 7231, 7147, 7067, 6990, 6915, 6842, 6772, 6704,
    6637, 6572, 6508, 6445, 6384, 6324, 6264, 6206, 6149, 6092,
    6036, 5980, 5926, 5871, 5818, 5765, 5712, 5659, 5607, 5556,
    5504, 5453, 5402, 5351, 5301, 5251, 5200, 5150, 5100, 5050,
    5000, 4950, 4900, 4850, 4800, 4749, 4699, 4649, 4598, 4547,
    4496, 4444, 4393, 4341, 4288, 4235, 4182, 4129, 4074, 4020,
    3964, 3908, 3851, 3794, 3736, 3676, 3616, 3555, 3492, 3428,
    3363, 3296, 3228, 3158, 3085, 3010, 2933, 2853, 2769, 2681,
    2589, 2492, 2388, 2276, 2154, 2020, 1868, 1690, 1469, 1160,
    0
};*/
/*const uint16_t delayTable[101] = {

    10000, 9995, 9979, 9948, 9903, 9841, 9762, 9666, 9552, 9420,
    9268, 9097, 8907, 8802, 8740, 8673, 8601, 8525, 8465, 8401,
    8332, 8269, 8208, 8142, 8082, 8020, 7958, 7898, 7837, 7777,
    7716, 7656, 7596, 7537, 7477, 7418, 7359, 7300, 7240, 7181,
    7121, 7062, 7003, 6944, 6884, 6824, 6764, 6704, 6644, 6584,
    6523, 6462, 6401, 6340, 6277, 6215, 6153, 6090, 6027, 5963,
    5898, 5834, 5768, 5702, 5635, 5568, 5500, 5431, 5361, 5291,
    5219, 5146, 5073, 4998, 4922, 4845, 4766, 4685, 4604, 4520,
    4433, 4346, 4255, 4162, 4066, 3967, 3865, 3759, 3647, 3531,
    3408, 3278, 3140, 2991, 2830, 2651, 2449, 2213, 1921, 1510,
    1000
};*/
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
   }

void loop(){
   if ((brightness<=90)&&(millis()-last>=1000)){
   brightness=brightness+5;
   last=millis();}
}
