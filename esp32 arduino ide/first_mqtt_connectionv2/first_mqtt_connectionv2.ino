#include <WiFi.h>
#include <WiFiClientSecure.h>
#include <PubSubClient.h>

const char* WIFI_SSID = "Ooredoo1EEC87";
const char* WIFI_PASSWORD = "**********";
// ur home wifi credentials
const char* MQTT_HOST = "***********.s1.eu.hivemq.cloud";
const int MQTT_PORT = 8883;
// mqtt URL&PORT

const char* MQTT_USERNAME = "esp32_project";
const char* MQTT_PASSWORD = "YourStrongPassword123";
// mqtt user credential (configured in the hivemqweb)

WiFiClientSecure espClient;
PubSubClient mqttClient(espClient);

String message;

void callback(char* topic, byte* payload, unsigned int length) {
  message = "";

  for (unsigned int i = 0; i < length; i++) {
    message += (char)payload[i];
  }

  Serial.println(message);
}

void setup() {
  Serial.begin(115200);

  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
  }
  Serial.println("wificonnected");

  espClient.setInsecure();

  mqttClient.setServer(MQTT_HOST, MQTT_PORT);

  mqttClient.connect("ESP32_Relay", MQTT_USERNAME, MQTT_PASSWORD);
  if(mqttClient.connected()){Serial.println("clientconnected");}
  mqttClient.subscribe("home/relay/");
  mqttClient.setCallback(callback);
}

void loop() {
  mqttClient.loop();
}
