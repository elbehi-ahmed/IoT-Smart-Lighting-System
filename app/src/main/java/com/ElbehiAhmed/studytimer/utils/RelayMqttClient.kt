package com.ElbehiAhmed.studytimer.utils

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient

object RelayMqttClient {

    private const val HOST = "81135964782e485cb30707f9520dc6f6.s1.eu.hivemq.cloud"
    private const val PORT = 8884
    private const val USERNAME = "esp32_project"
    private const val PASSWORD = "YourStrongPassword123"
    private const val CLIENT_ID = "android_relay_app"
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

    @Synchronized
    fun publish(message: String) {
        client.connectWith()
            .simpleAuth()
            .username(USERNAME)
            .password(PASSWORD.toByteArray())
            .applySimpleAuth()
            .send()

        client.publishWith()
            .topic(TOPIC)
            .payload(message.toByteArray())
            .qos(MqttQos.AT_LEAST_ONCE)
            .send()
    }
}