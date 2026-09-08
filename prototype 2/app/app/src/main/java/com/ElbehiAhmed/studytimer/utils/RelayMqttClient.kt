package com.ElbehiAhmed.studytimer.utils

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.MqttClientState
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import android.util.Log

object RelayMqttClient {

    private const val TAG = "RelayMqttClient"
    private const val HOST = "81135964782e485cb30707f9520dc6f6.s1.eu.hivemq.cloud"
    private const val PORT = 8883 // Use 8883 for TLS
    private const val USERNAME = "esp32_project"
    private const val PASSWORD = "YourStrongPassword123"
    private const val CLIENT_ID = "android_relay_app"

    // Last Will topic/payload: the broker publishes this itself the moment it notices this
    // client is gone (crash, force-close, network drop, swipe from recents, keep-alive
    // timeout) — this is what actually guarantees "off" gets published, since it doesn't
    // depend on any app code running during shutdown.
    private const val TOPIC_START = "home/start"
    private const val WILL_PAYLOAD = "off"

    // Lower than the HiveMQ default (60s) so the broker detects a dead connection and fires
    // the will message sooner. Tune this: lower = faster "off" detection, higher = fewer
    // PINGREQ packets / less battery use. 20s is a reasonable middle ground.
    private const val KEEP_ALIVE_SECONDS = 20

    // Tracks which topics we've already subscribed to so we don't re-subscribe on every call.
    private val subscribedTopics = mutableSetOf<String>()

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
                    .keepAlive(KEEP_ALIVE_SECONDS)
                    .simpleAuth()
                    .username(USERNAME)
                    .password(PASSWORD.toByteArray())
                    .applySimpleAuth()
                    .willPublish()
                    .topic(TOPIC_START)
                    .payload(WILL_PAYLOAD.toByteArray())
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .retain(true)
                    .applyWillPublish()
                    .send()
                Log.d(TAG, "Connect result: $connectResult")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to MQTT broker", e)
            throw e
        }
    }

    /** Publish [message] to [topic]. */
    @Synchronized
    fun publish(topic: String, message: String) {
        try {
            connect()
            Log.d(TAG, "Publishing message: $message to topic: $topic")
            client.publishWith()
                .topic(topic)
                .payload(message.toByteArray())
                .qos(MqttQos.AT_LEAST_ONCE)
                .send()
            Log.d(TAG, "Message published successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error publishing message", e)
            throw e
        }
    }


    /**
     * Subscribe to [topic]; [onMessage] is invoked with the payload as a String every time a
     * message arrives. The callback runs on the MQTT client's internal Netty thread, NOT the
     * calling thread — hop back to the main thread yourself if you touch views directly
     * (MainActivity already does this via runOnUiThread).
     */
    @Synchronized
    fun subscribe(topic: String, onMessage: (String) -> Unit) {
        try {
            connect()
            if (topic in subscribedTopics) {
                Log.d(TAG, "Already subscribed to topic: $topic")
                return
            }
            Log.d(TAG, "Subscribing to topic: $topic")
            // The blocking client has no callback-accepting subscribe overload; the async
            // view (same underlying connection) is what exposes the callback API.
            client.toAsync().subscribeWith()
                .topicFilter(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback { publish: Mqtt3Publish ->
                    val payload = String(publish.payloadAsBytes)
                    Log.d(TAG, "Message received on $topic: $payload")
                    onMessage(payload)
                }
                .send()
            subscribedTopics += topic
            Log.d(TAG, "Subscribed successfully to: $topic")
        } catch (e: Exception) {
            Log.e(TAG, "Error subscribing to topic: $topic", e)
            throw e
        }
    }

    /**
     * Disconnects cleanly, e.g. from Activity#onDestroy after the final "off" publish.
     *
     * Note: a clean disconnect() intentionally does NOT trigger the will message (that's
     * normal MQTT behavior — the will only fires on an *unexpected* disconnect). That's fine
     * here because MainActivity still explicitly publishes "off" itself before calling this
     * on a graceful close; the will message is purely the safety net for ungraceful ones.
     */
    @Synchronized
    fun disconnect() {
        try {
            if (client.state == MqttClientState.CONNECTED) {
                client.disconnect()
                subscribedTopics.clear()
                Log.d(TAG, "Disconnected from MQTT broker.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting from MQTT broker", e)
        }
    }
}