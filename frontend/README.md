# EcoCollect Smart Waste Management - Frontend Application

This is the Android mobile application for the EcoCollect Smart Waste Management System. It is built using **Kotlin**, **Jetpack Compose**, and **Ktor Client** for network requests to the FastAPI backend.

---

## ⚙️ Connecting Frontend to the Backend (IP Address Setup)

To allow your Android Emulator or physical device to communicate with the FastAPI backend, you must configure the backend server's local IP address.

### Step 1: Find Your Local IP Address

Run the command corresponding to your operating system to find your local IP address:

* **Windows (Command Prompt / PowerShell)**:
  ```cmd
  ipconfig
  ```
  Look for the `IPv4 Address` under your active network adapter (e.g., `192.168.X.X` or `10.0.X.X`).

* **macOS / Linux (Terminal)**:
  ```bash
  ifconfig
  # OR
  ip a
  ```
  Look for your active connection interface (usually `en0` or `eth0` / `wlan0`) to locate the `inet` address.

---

### Step 2: Configure the API Base URL

1. Open the following Kotlin network service file:
   [ApiService.kt](src/main/java/com/wastereporting/network/ApiService.kt)
2. Locate the `BASE_URL` constant near the top of the file (around line 51):
   ```kotlin
   private const val BASE_URL = "http://<YOUR_IP_ADDRESS>:8000/api"
   ```
3. Replace `<YOUR_IP_ADDRESS>` with your actual IPv4 address retrieved in Step 1.
   * *Example*: `http://192.168.1.100:8000/api`

> [!NOTE]
> * **Android Emulator**: If you are using the standard Android emulator on the same machine running the backend, you can optionally use `http://10.0.2.2:8000/api`. However, using your actual machine IP is recommended as it works for both Emulators and Physical test devices connected to the same Wi-Fi network.

---

## 🛠️ Build and Run Requirements

1. **Java Development Kit (JDK)**: Ensure JDK 17 or higher is installed and set in your environment variables.
2. **Android Studio**: Open the `frontend` folder as a project in Android Studio (Ladybug or newer recommended).
3. **Gradle**: Ensure Gradle Sync completes successfully.
