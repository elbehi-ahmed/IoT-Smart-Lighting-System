# MQTT Communication

MQTT provides communication between the Android application and the
ESP32 through the MQTT broker.

## Architecture

```text
Android App
     |
     v
MQTT Broker
     |
     v
ESP32 MQTT Client
     |
     v
MQTT Callback
```

## Subscribed Topics

The ESP32 subscribes to:

```text
home/mode
home/brightness
home/start
```

### `home/mode`

Receives:

```text
manual
automatic
```

### `home/brightness`

Receives a numerical brightness value.

### `home/start`

Receives APP STATE:

```text
on
off
```

## Published Topics

The firmware publishes:

```text
home/state
home/temp
home/humidity
```

## Command Processing

Incoming messages are processed inside the MQTT callback.

```text
MQTT Message
     |
     v
callback()
     |
     +---- "manual" ----> Manual Mode
     |
     +---- "automatic" -> Automatic Mode
     |
     +---- "on" --------> System State ON
     |
     +---- "off" -------> System State OFF
     |
     +---- Number ------> Brightness Request
```

## Sensor Publishing

When the system is ON:

* Temperature is periodically published to `home/temp`.
* Humidity is periodically published to `home/humidity`.
* The ESP32 periodically publishes `online` to `home/state`and 'connecting' when powered on (used to reconnect after power cuts).
