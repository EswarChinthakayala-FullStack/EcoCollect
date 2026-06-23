# EcoCollect Smart Waste Management - Backend

Welcome to the backend service for the **EcoCollect Smart Waste Management** application. This service is built using **Python FastAPI** and connects to a **MySQL** database (compatible with XAMPP phpMyAdmin).

---

## Features

- **Relational MySQL Database**: Relational schema including users (citizens, supervisors, admins), issues, status audits, and notifications.
- **JWT Authentication**: Secure login/registration flows with password hashing (`bcrypt`) and token verification.
- **Media Support**: Built-in image upload router (`POST /api/upload`) saving media files to a local `uploads` directory served statically.
- **Roles-based API Routing**: Fine-grained access control for citizen, supervisor, and administrator requests.
- **Auto-migration**: Tables are automatically initialized in the database upon launching the server if they don't exist.

---

## Getting Started

### Prerequisites

- **Python 3.10+** (Python 3.13 is fully supported)
- **XAMPP Control Panel** (MySQL/MariaDB database)

---

### Step 1: Database Setup in XAMPP

1. Start **XAMPP Control Panel**.
2. Start the **Apache** and **MySQL** services.
3. Open browser and navigate to **phpMyAdmin**: `http://localhost/phpmyadmin`
4. Click **New** in the sidebar and create a database named `wastereporting` with collation `utf8mb4_general_ci`.
5. *(Optional)* If you wish to import the database structure manually, you can run the SQL queries inside the [schema.sql](schema.sql) file. Otherwise, the backend will auto-generate the tables on the first launch.

---

### Step 2: Activating the Virtual Environment

Open a terminal or command prompt inside the `backend` folder and run the appropriate command for your operating system:

#### Windows PowerShell:
```powershell
.\venv\Scripts\Activate.ps1
```

#### Windows Command Prompt (CMD):
```cmd
.\venv\Scripts\activate
```

#### Linux / macOS:
```bash
source venv/bin/activate
```

---

### Step 3: Installing Dependencies (Completed)

All dependencies have already been installed. If you ever need to restore or update dependencies, make sure your virtual environment is active and run:
```bash
pip install -r requirements.txt
```

---

### Step 4: Starting the Backend Server

To start the server manually inside the activated virtual environment:

#### Localhost only:
```bash
uvicorn app.main:app --reload
```

#### Supporting External Devices & Emulators (Recommended):
To allow connection from external devices (like an actual Android phone or an emulator running on a local Wi-Fi network), bind the host to `0.0.0.0`:
```bash
uvicorn app.main:app --host 0.0.0.0 --reload
```

Alternatively, you can run our CLI manager script, which automatically resolves your local network IP and starts the server publicly by default:
```bash
python start.py
```

To run other tools via the CLI manager, pass the appropriate command line flags:
- **Run validation tests**: `python start.py --test` (or `-t`)
- **Show active configurations**: `python start.py --config` (or `-c`)
- **Access API documentation links**: `python start.py --docs` (or `-d`)
- **View CLI usage guide**: `python start.py --help` (or `-h`)

Once running:
- **API Server Address**: `http://127.0.0.1:8000`
- **Interactive Swagger Documentation**: `http://127.0.0.1:8000/docs`
- **Alternative ReDoc Documentation**: `http://127.0.0.1:8000/redoc`

---

## Default Administrator Credentials

A default administrator account has been initialized in the database:
- **Email/Username**: `admin@ecocollect.city`
- **Password**: `Admin@123`
- **Role**: `admin`

---

## Directory Structure

```
backend/
├── app/
│   ├── models/       # SQLAlchemy ORM Models
│   ├── routes/       # API Route controllers (auth, citizen, admin, etc.)
│   ├── schemas/      # Pydantic schemas for data serialization and validation
│   ├── utils/        # JWT & security utilities
│   ├── config.py     # Environment parameters & configurations
│   ├── database.py   # SQL database connection pool
│   └── main.py       # FastAPI application initiation
├── uploads/          # Saved media and image uploads
├── venv/             # Local Python virtual environment
├── requirements.txt  # Python packages list
├── schema.sql        # Database tables structures DDL
├── test_backend.py   # Code import verification script
└── start.py          # Animated start CLI manager script
```
