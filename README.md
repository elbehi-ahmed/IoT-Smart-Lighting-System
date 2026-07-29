# Study Timer

A simple Android app to track study sessions with start/pause/resume/stop timing, session history, and statistics.

## Features
- Start, pause, resume, and stop a study timer (HH:MM:SS)
- Automatically saves each completed session (date, start/end time, duration)
- Session history with sort (newest/oldest), search by date, and delete 
- Statistics

## Tech Stack
- Kotlin, MVVM architecture
- Room (SQLite) database
- Repository pattern
- ViewModel + StateFlow / Flow
- ViewBinding
- RecyclerView

## Installation

### Run from source (Android Studio)
1. Clone the repo:
   ```
   git clone https://github.com/elbehi-ahmed/StudyTimer.git
   ```
2. Open the project in **Android Studio** (Hedgehog or newer recommended).
3. Let Gradle sync finish.
4. Click **Run ▶** and select an emulator or a physical device .

### Install a pre-built APK
1. Download the APK from the [Releases](../../releases) page.
2. Transfer it to your Android device.
3. Enable **Install unknown apps** for the file manager/browser you're using.
4. Tap the APK file to install.

## Author
**Ahmed Elbehi**
