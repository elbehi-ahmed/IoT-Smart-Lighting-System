#include <WiFi.h>
#include <WiFiClientSecure.h>
#include <PubSubClient.h>
#include <DHT11.h>
#include <LiquidCrystal.h>

LiquidCrystal lcd(22, 21, 19, 18, 5, 23); // RS, E, D4, D5, D6, D7
DHT11 dht11(4); //sensor humidity&temperature

int temperature;
int humidity;

int zerocross = 17;
int optocoupler = 16;
int duration=4000;

#define LDR_PIN 34  // GPIO 34 (ADC1 channel 6)

volatile int brightness = 0;
volatile int brightnessmanu = 0;
volatile int brightness_before_smoothing = 0;

float Kp = 0.02;
float Ki = 0.002;
float integral = 0;

unsigned long lastPID = 0;
unsigned long lastdht = 0;
unsigned long laststatus = 0;
unsigned long lastsmoothing = 0;

unsigned long lastlcd = 0;
unsigned long lastflip = 0;

unsigned long displayStart = 0;

const unsigned long PID_INTERVAL = 20;
float TARGET_LIGHT = 0.0;

const char* WIFI_SSID = "Ooredoo1EEC87_plus";
const char* WIFI_PASSWORD = "KAEX9S6G#&?EZ";

const char* MQTT_HOST = "81135964782e485cb30707f9520dc6f6.s1.eu.hivemq.cloud";
const int MQTT_PORT = 8883;

const char* MQTT_USERNAME = "esp32_project";
const char* MQTT_PASSWORD = "YourStrongPassword123";

WiFiClientSecure espClient;
PubSubClient mqttClient(espClient);

String message;


// SYSTEM STATES


enum systemstate {
    on,
    off
};

enum LightMode {
    manual,
    automatic
};

volatile LightMode mode = manual;
volatile systemstate state = off;


// LCD STATE MACHINE


enum DisplayState {
    DISPLAY_MANUAL,
    DISPLAY_AUTOMATIC,
    DISPLAY_ENVIRONMENT,
    DISPLAY_APP_CONNECTED,
    DISPLAY_APP_DISCONNECTED
};

DisplayState displayState = DISPLAY_MANUAL;

bool displayTemporary = false;

unsigned long displayDuration = 1700;
unsigned long displayLastChange = 0;

// BRIGHTNESS

int mapBrightness(int appbrightness) {
    // Map 0-100 to 20-100
    int realbrightness = 20 + (appbrightness * 80 / 100);
    return realbrightness;
}

void brightnesssmoother(int brightness_before_smoothing) {
    if (brightness < brightness_before_smoothing) {
        brightness++;
    }
    else if (brightness > brightness_before_smoothing) {
        brightness--;
    }
}



//            TRIAC DELAY TABLE

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


//           HARDWARE TIMERS


hw_timer_t *timer = NULL;
hw_timer_t *timer2 = NULL;

void IRAM_ATTR pulseISR() {
    digitalWrite(optocoupler, LOW);
}

void IRAM_ATTR timerISR() {
    digitalWrite(optocoupler, HIGH);

    timerWrite(timer2, 0);
    timerAlarm(timer2, 75, false, 0);
}

void IRAM_ATTR zcfunction() {
    if (brightness != 0) {
        timerWrite(timer, 0);
        timerAlarm(timer, delayTable[mapBrightness(brightness)], false, 0);
    }
}

// PID


void updatePID() {
    int light = analogRead(LDR_PIN);
    float error = TARGET_LIGHT - light;
    float dt = PID_INTERVAL / 1000.0;
    integral += error * dt;
    integral = constrain(integral, -50.0, 50.0);
    float correction =(Kp * error) + (Ki * integral);
    int newBrightness =brightness + (int)correction;
    newBrightness =constrain(newBrightness, 0, 99);
    brightness = newBrightness;
}


void changeDisplayState(DisplayState newState) {

    displayState = newState;

    displayLastChange = millis();


    // Temporary messages
    if (newState == DISPLAY_APP_CONNECTED ||
        newState == DISPLAY_APP_DISCONNECTED) {

        displayTemporary = true;
    }
    else {
        displayTemporary = false;
    }

    lastlcd = 0;
}


// MQTT CALLBACK


void callback(char* topic, byte* payload, unsigned int length) {

    message = "";

    for (unsigned int i = 0; i < length; i++) {
        message += (char)payload[i];
    }

    if (message == "manual") {

        mode = manual;

        brightness = brightnessmanu;

        changeDisplayState(DISPLAY_MANUAL);
    }

    else if (message == "automatic") {

        brightnessmanu = brightness;

        TARGET_LIGHT = analogRead(LDR_PIN);

        integral = 0;

        mode = automatic;

        changeDisplayState(DISPLAY_AUTOMATIC);
    }


    else if (message == "on") {

        state = on;

        changeDisplayState(DISPLAY_APP_CONNECTED);
    }

    else if (message == "off") {
        state=off;
        changeDisplayState(DISPLAY_APP_DISCONNECTED);
    }


    else {
        brightness_before_smoothing = message.toInt();
        changeDisplayState(DISPLAY_MANUAL);
    }
}



//                 LCD STATE MACHINE


void updateLCD() {

    unsigned long now = millis();

    //            TEMPORARY APP MESSAGE


    if (displayTemporary) {

        if (now - displayLastChange >= displayDuration) {

            displayTemporary = false;


                if (mode == manual) {
                    changeDisplayState(DISPLAY_MANUAL);
                }
                else {
                    changeDisplayState(DISPLAY_AUTOMATIC);
                }
            
        }
    }
   
    else{

    // IDLE DISPLAY ROTATION

        if (now - lastflip >= duration) {

            lastflip = now;

            if (displayState == DISPLAY_ENVIRONMENT) {
                duration=4000;
                if (mode == manual) {
                    changeDisplayState(DISPLAY_MANUAL);
                }
                else {
                    changeDisplayState(DISPLAY_AUTOMATIC);
                }
            }

            else {

                changeDisplayState(DISPLAY_ENVIRONMENT);
                duration=1700;
            }
        }
    }
    

    // PRINT CURRENT STATE
   

    if (now - lastlcd < 100) {
        return;
    }
    lastlcd = now;
    switch (displayState) {
        
        case DISPLAY_MANUAL:
            lcd.setCursor(0, 0);
            lcd.print("Manual Mode:    ");
            lcd.setCursor(0, 1);
            if (brightness == 0) {
                lcd.print("Lamp is OFF     ");
                }

            else {

                lcd.print("Brightness: ");
                lcd.print(brightness);
                lcd.print("%    ");
            }

            break;
        

        case DISPLAY_AUTOMATIC:

            lcd.setCursor(0, 0);
            lcd.print("Automatic Mode: ");

            lcd.setCursor(0, 1);

            lcd.print("Brightness:");
            lcd.print(brightness);
            lcd.print("%  ");
            break;
        

        case DISPLAY_ENVIRONMENT:

            lcd.setCursor(0, 0);
            lcd.print("ENVIRONMENT     ");

            lcd.setCursor(0, 1);

            lcd.print("T:");
            lcd.print(temperature);
            lcd.print("C H:");
            lcd.print(humidity);
            lcd.print("%       ");

            break;


     
        case DISPLAY_APP_CONNECTED:

            lcd.setCursor(0, 0);
            lcd.print("App Connected   ");

            lcd.setCursor(0, 1);
            lcd.print("Ready           ");

            break;
     
        case DISPLAY_APP_DISCONNECTED:

            lcd.setCursor(0, 0);
            lcd.print("App Disconnected");

            lcd.setCursor(0, 1);
            lcd.print("Idle            ");

            break;
    }
}

void setup() {
    lcd.begin(16, 2);
    Serial.begin(115200);
    WiFi.mode(WIFI_STA);
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

    while (WiFi.status() != WL_CONNECTED) {
        delay(500);}

    lcd.setCursor(0, 0);
    lcd.print("wifi connected  ");

    Serial.println("wificonnected");

    delay(1000);

    espClient.setInsecure();

    mqttClient.setServer(MQTT_HOST, MQTT_PORT);

    mqttClient.connect("ESP32_Relay",MQTT_USERNAME,MQTT_PASSWORD);


    if (mqttClient.connected()) {

        lcd.setCursor(0, 1);
        lcd.print("MQTT Connected  ");

        Serial.println("client connected");
    }

    delay(1000);

    mqttClient.setCallback(callback);

    mqttClient.subscribe("home/mode");
    mqttClient.subscribe("home/brightness");
    mqttClient.subscribe("home/start");


    pinMode(zerocross, INPUT);

    pinMode(optocoupler, OUTPUT);

    digitalWrite(optocoupler, LOW);

    attachInterrupt(
digitalPinToInterrupt(zerocross),zcfunction,CHANGE);
    timer = timerBegin(1000000);

    timer2 = timerBegin(1000000);


    timerAttachInterrupt(timer, timerISR);

    timerAttachInterrupt(timer2, pulseISR);


    timerStart(timer);

    timerStart(timer2);


    // ADC attenuation
    analogSetAttenuation(ADC_11db);

    lcd.clear();
    delay(300);
    TARGET_LIGHT = analogRead(LDR_PIN);
    changeDisplayState(DISPLAY_MANUAL);
    mqttClient.publish("home/state","connecting");
}




void loop() {

    mqttClient.loop();


    // BRIGHTNESS SMOOTHING
    if ((millis() - lastsmoothing >= 10) &&(brightness != brightness_before_smoothing)) {
        brightnesssmoother(brightness_before_smoothing);
        lastsmoothing = millis();}
        
    // SYSTEM ON


    if (state == on) {
    // MQTT STATUS


        if ((millis() - laststatus >= 5000) &&
            mqttClient.connected()) {

            laststatus = millis();

            mqttClient.publish("home/state","online");
        }
 
    //               DHT11

        if (millis() - lastdht >= 4000) {
            lastdht = millis();
            int result =dht11.readTemperatureHumidity(temperature,humidity);
            if (result == 0) {
                mqttClient.publish("home/temp",String(temperature).c_str());
                mqttClient.publish("home/humidity",String(humidity).c_str());}
        }}

    //                    PID

        if (mode == automatic) {
            if (millis() - lastPID >= PID_INTERVAL) {
                updatePID();
                lastPID = millis();}}
    
    //            LCD STATE MACHINE
    updateLCD();
}
