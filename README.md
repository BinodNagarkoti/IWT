# Interval Walk Tracker

Interval Walk Tracker is a production-grade Android application designed to guide users through the **Japanese Interval Walking Training (IWT)** method.

## 🚶‍♂️ How it Works
The workout follows a simple but effective pattern:
- **3 Minutes FAST Walking**
- **3 Minutes SLOW Walking**
- Repeated for multiple sets.

## ✨ Key Features
- **Real-time Workout Tracking:** Monitor your current set, interval mode, and countdown timer.
- **Audio Coaching:** Voice guidance using Android Text-to-Speech (TTS) to notify you when to switch modes or when a set is completed.
- **Background Support:** Uses a Foreground Service to ensure the timer and audio coaching remain active even when the screen is locked or the app is in the background.
- **Step Counting:** Accurate step tracking using the device's hardware step counter sensor.
- **Detailed Statistics:** View your daily, monthly, and yearly progress on a clean Dashboard.
- **Workout History:** Review all your previous sessions with summarized data.

## 🛠 Tech Stack
- **UI:** Jetpack Compose (Declarative UI)
- **Language:** Kotlin
- **Architecture:** MVVM (Model-View-ViewModel) + Repository Pattern
- **Dependency Injection:** Hilt
- **Database:** Room (SQLite) for persistent session storage
- **Navigation:** Jetpack Compose Navigation
- **Lifecycle:** Lifecycle-aware components and StateFlow for reactive UI updates

## 📂 Project Structure
- `di/`: Hilt modules for Dependency Injection.
- `data/`: Room database entities, DAOs, and the Repository layer.
- `service/`: Foreground Service for background workout management.
- `timer/`: Business logic for interval timing.
- `sensors/`: Step sensor management.
- `audio/`: Text-to-Speech audio coaching manager.
- `ui/`: Compose screens, navigation, and reusable components.
- `viewmodel/`: ViewModels handling UI state and business logic integration.

## 🚀 Getting Started
1. Clone the repository.
2. Open in Android Studio (Ladybug or newer).
3. Build and run on a physical device (recommended for step sensor testing).
4. **Permissions:** Ensure you grant *Activity Recognition* and *Notification* permissions for the best experience.

## 📄 License
This project is licensed under the MIT License - see the LICENSE file for details.
