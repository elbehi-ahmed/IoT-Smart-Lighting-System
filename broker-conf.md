# HiveMQ Cloud — Create an MQTT Broker

## 1. What is an MQTT Broker?

An **MQTT broker** is the central server that receives MQTT messages from devices/apps and forwards them to the clients that subscribed to the corresponding topics.

For a remote relay project, the architecture can look like this:

```text
Android App  ──publish──>  HiveMQ Broker  ──publish──>  ESP32
                             │
                             └── MQTT topics
```

Example:

```text
Topic: relay/start
Message: on
```

The Android app publishes `on` to `relay/start`. The HiveMQ broker receives it and sends it to the ESP32 if the ESP32 is subscribed to that topic.

### Why use a broker?

The broker is useful because the Android app and ESP32 do **not** need to connect directly to each other.

It provides:

- **Central communication** between MQTT clients.
- **Publish/Subscribe messaging** using topics.
- **Authentication** so only authorized clients can connect.
- **TLS encryption** for secure communication.
- Communication over the **Internet**, so the ESP32 and phone can be in different networks.
- A place where you can manage MQTT credentials and permissions.

HiveMQ Cloud is a managed MQTT broker, so HiveMQ manages the server infrastructure for you.

---

# 2. Create a HiveMQ Cloud Broker

> HiveMQ's current interface may show either the **HiveMQ Platform** or the **HiveMQ Cloud Console**, depending on when your account was created.

## Step 1 — Open HiveMQ

Go to:

https://www.hivemq.com/

Click **Start Free** or **Try the Platform**.

Create an account using:

- Email
- Google
- GitHub
- LinkedIn

If you register with email, verify your email address.

---

## Step 2 — Choose Cloud

After logging in, choose the **Cloud** option.

You want HiveMQ to host and manage the MQTT broker instead of installing the broker yourself.

For a small IoT project, the **HiveMQ Cloud Serverless** plan is a good option for learning and testing.

---

## Step 3 — Create a Serverless Cluster

In the HiveMQ Cloud area, choose:

**Create Serverless Cluster**

The cluster is your hosted MQTT broker.

HiveMQ will create it for you automatically.

Wait until the cluster status indicates that it is running.

---

## Step 4 — Open Manage Cluster

Click:

**Manage Cluster**

You should see the cluster information, including the connection details.

Save these values:

```text
Host / URL:     <your-cluster-url>
Port:           <your-TLS-port>
Username:       <your-mqtt-username>
Password:       <your-mqtt-password>
```

Do **not** share your MQTT password publicly or commit it to GitHub.

---

# 3. Create MQTT Credentials

Your Android app and ESP32 need credentials to connect to the broker.

Inside your cluster, open the section for:

**Access Management / Credentials**

Create MQTT access credentials.

For example:

```text
Username: remote-relay
Password: ********
```

Use strong credentials in a real project.

---

# 4. Understand Your MQTT Topics

The broker does not normally need to know what your application means by a message.

Your clients communicate through **topics**.

For example, your Remote Relay project can use:

```text
relay/start
relay/brightness
relay/state
relay/temperature
relay/humidity
```

Example:

```text
Android App
    |
    | publish "on"
    v
relay/start
    |
    v
HiveMQ Broker
    |
    | forwards "on"
    v
ESP32
```

The ESP32 subscribes to:

```text
relay/start
```

and receives:

```text
on
```

---

# 5. Test the Broker

HiveMQ Cloud provides a web-based MQTT client that can be used to test your connection.

Open the **Web Client** for your cluster.

Enter your MQTT credentials and connect.

Then test:

### Subscribe

Subscribe to:

```text
relay/test
```

### Publish

Publish to:

```text
relay/test
```

with:

```text
Hello ESP32
```

If the subscription receives `Hello ESP32`, your MQTT broker is working correctly.

---

# 6. Connect the ESP32 and Android App

After the broker works, both of your applications become MQTT clients:

```text
                    HiveMQ Cloud
                  MQTT Broker
                       |
          +------------+------------+
          |                         |
          v                         v
      Android App                 ESP32
       Publisher                Subscriber
          |                         |
          +------ MQTT topics ------+
```

For example:

### Android

```text
publish(
    topic = "relay/start",
    message = "on"
)
```

### ESP32

```text
subscribe("relay/start")
```

When the Android app publishes `on`, the broker forwards the message to the ESP32.

---

# 7. Important Security Notes

HiveMQ Cloud supports secure MQTT connections using **TLS**.

Use the secure TLS connection information provided by your cluster.

Do not put passwords directly in a public GitHub repository.

Instead of:

```kotlin
val password = "mySecretPassword"
```

use a safer configuration approach such as `local.properties`, environment variables, or another secrets-management solution.

Also configure topic permissions so that clients only have the access they actually need.

---

# 8. Quick Summary

```text
1. Go to hivemq.com
2. Create an account
3. Choose HiveMQ Cloud
4. Create a Serverless cluster
5. Open Manage Cluster
6. Create MQTT credentials
7. Copy the cluster URL and TLS port
8. Test using the HiveMQ Web Client
9. Put the connection information in your Android app
10. Put the connection information in your ESP32
11. Use MQTT topics to exchange messages
```

## Your Remote Relay Project

```text
                Internet
                   |
                   v
        +---------------------+
        |   HiveMQ Cloud      |
        |    MQTT Broker      |
        +---------------------+
             ^           |
             |           |
        publish       publish
             |           |
             |           v
     +-------------+  +-------------+
     | Android App |  |    ESP32    |
     +-------------+  +-------------+
             |           |
             |           |
             +---- MQTT -+
               topics
```

The **broker is the middleman** that allows your Android application and ESP32 to communicate reliably without needing a direct connection between them.

## Official HiveMQ Documentation

- HiveMQ Cloud Quick Start:
  https://docs.hivemq.com/hivemq-cloud/quick-start-guide.html

- HiveMQ Cloud documentation:
  https://docs.hivemq.com/hivemq-cloud/

- HiveMQ MQTT Broker beginner guide:
  https://www.hivemq.com/blog/mqtt-brokers-beginners-guide/
