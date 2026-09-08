package com.ElbehiAhmed.studytimer.utils

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.MqttClientState
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient
import android.util.Log

object RelayMqttClient {
    // put here your credentials
    private const val TAG = ""
    private const val HOST = ""
    private const val PORT = 8883 // Use 8883 for TLS
    private const val USERNAME = ""
    private const val PASSWORD = ""
    private const val CLIENT_ID = ""
    private const val TOPIC = "home/relay/"

    private val client: Mqtt3BlockingClient by lazy {
        MqttClient.builder()
            .useMqttVersion3()
            .identifier(CLIENT_ID)
            .serverHost(HOST)
            .serverPort(PORT)
            .sslWithDefaultConfig()
            .buildBlocking()
    }

    private fun connect() {
        try {
            Log.d(TAG, "Current client state: ${client.state}")
            if (client.state != MqttClientState.CONNECTED) {
                Log.d(TAG, "Connecting to MQTT broker: $HOST:$PORT")
                val connectResult = client.connectWith()
                    .simpleAuth()
                    .username(USERNAME)
                    .password(PASSWORD.toByteArray())
                    .applySimpleAuth()
                    .send()
                Log.d(TAG, "Connect result: $connectResult")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to MQTT broker", e)
            throw e
        }
    }

    @Synchronized
    fun publish(message: String) {
        try {
            connect()
            Log.d(TAG, "Publishing message: $message to topic: $TOPIC")
            client.publishWith()
                .topic(TOPIC)
                .payload(message.toByteArray())
                .qos(MqttQos.AT_LEAST_ONCE)
                .send()
            Log.d(TAG, "Message published successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error publishing message", e)
            throw e
        }
    }
}