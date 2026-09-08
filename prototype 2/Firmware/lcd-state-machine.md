# LCD State Machine

The LCD is managed using a dedicated state machine.

## Display States

The firmware defines five display states:

```text
DISPLAY_MANUAL
DISPLAY_AUTOMATIC
DISPLAY_ENVIRONMENT
DISPLAY_APP_CONNECTED
DISPLAY_APP_DISCONNECTED
```

## State Flow

```text
                  +----------------+
                  |   LCD State    |
                  |    Machine     |
                  +-------+--------+
                          |
          +---------------+---------------+
          |               |               |
          v               v               v
       MANUAL        AUTOMATIC      ENVIRONMENT
          ^               ^               |
          |               |               |
          +---------------+---------------+
                          |
                   Temporary States
                    /            \
                   v              v
             APP CONNECTED   APP DISCONNECTED
```

## Manual Display

Shows:

```text
Manual Mode:
Brightness: XX%
```

When brightness is zero:

```text
Manual Mode:
Lamp is OFF
```

## Automatic Display

Shows:

```text
Automatic Mode:
Brightness: XX%
```

## Environment Display

Shows:

```text
ENVIRONMENT
T:XXC H:XX%
```

## Temporary Connection Displays

`DISPLAY_APP_CONNECTED` and `DISPLAY_APP_DISCONNECTED` are temporary
states.

They remain visible for approximately 1.7 seconds before returning to
the current operating-mode display.

## Display Rotation

During normal operation, the LCD alternates between the current mode
and the environment display.

```text
MANUAL / AUTOMATIC
        |
        v
 ENVIRONMENT
        |
        v
MANUAL / AUTOMATIC
        |
       ...
```
## user interruption feature

the LCD immediately leaves the enviornment screen and goes to the modes one the moment u make a change on the app and doesnt wait for the 2 seconds to end 
so it normally rotates but can be forced to mode the moment u change brightness or the mode on the app