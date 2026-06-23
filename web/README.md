# EcoCollect Smart Waste Management - Web Portal

This is the central web dashboard for the EcoCollect Smart Waste Management System. It is built using **React**, **TypeScript**, **Tailwind CSS**, and **Vite**.

---

## 🚀 Running the Web Portal

### Step 1: Install Dependencies
Run the following command inside the `web` directory:
```bash
npm install
```

### Step 2: Start Development Server
Start the local development server:
```bash
npm run dev
```
By default, the server runs at: 👉 **http://localhost:5173/** (or another port if port 5173 is occupied).

---

## ⚙️ Connecting to the FastAPI Backend

The web portal connects to the backend using either environment variables or dynamic hostname resolution:

### Option A: Using Environment Variables (`.env`)
You can configure the backend URL using the `.env` file in the `web` folder. A template [web/.env.example](.env.example) is provided.
1. Create/edit `web/.env`:
   ```env
   VITE_BACKEND_URL=http://localhost:8000
   ```
2. The React codebase reads this variable across the project via Vite's environment helper:
   ```typescript
   const backendHost = import.meta.env.VITE_BACKEND_URL;
   ```

### Option B: Dynamic Resolution Fallback
If `VITE_BACKEND_URL` is not specified, the application will fallback to dynamic resolution based on the browser's address bar:
* If accessed via `http://localhost:5173`, the API calls target `http://localhost:8000`.
* If accessed via your local IP address (e.g. `http://192.168.1.100:5173`), the API calls target `http://192.168.1.100:8000`.

To view the portal from another device on the same network (e.g., mobile phone or tablet):
1. **Find Your Local IP Address**:
   * **Windows**: Run `ipconfig` and look for the `IPv4 Address`.
   * **macOS / Linux**: Run `ifconfig` or `ip a` and look for the `inet` address.
2. **Access the Portal**: Open the browser on your device and navigate to `http://<YOUR_IP_ADDRESS>:5173/`. The backend host will automatically resolve to `http://<YOUR_IP_ADDRESS>:8000`.


---

## 🛠️ Build for Production

To compile and optimize the assets for production deployment:
```bash
npm run build
```
The output files will be generated in the `dist/` directory.
