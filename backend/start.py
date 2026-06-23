import sys
import os
import time
import subprocess
import socket

# Ensure stdout and stderr use UTF-8 encoding to avoid UnicodeEncodeError with ASCII/Unicode banners
if sys.platform.startswith('win'):
    try:
        sys.stdout.reconfigure(encoding='utf-8')
        sys.stderr.reconfigure(encoding='utf-8')
    except AttributeError:
        pass

# Ensure colorama is initialized
try:
    from colorama import init, Fore, Back, Style
    init(autoreset=True)
    HAS_COLORAMA = True
except ImportError:
    HAS_COLORAMA = False

def clear_screen():
    os.system("cls" if os.name == "nt" else "clear")

def get_local_ip() -> str:
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except Exception:
        return "127.0.0.1"

def check_mysql_connection() -> bool:
    try:
        from app.config import settings
        # Attempt a quick socket connection to the MySQL port
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.settimeout(0.5)
        s.connect((settings.DB_HOST, int(settings.DB_PORT)))
        s.close()
        return True
    except Exception:
        return False

def print_banner():
    logo = """
███████╗ ██████╗ ██████╗  ██████╗ ██████╗ ██╗     ██╗     ███████╗ ██████╗████████╗
██╔════╝██╔════╝██╔═══██╗██╔════╝██╔═══██╗██║     ██║     ██╔════╝██╔════╝╚══██╔══╝
█████╗  ██║     ██║   ██║██║     ██║   ██║██║     ██║     █████╗  ██║        ██║   
██╔══╝  ██║     ██║   ██║██║     ██║   ██║██║     ██║     ██╔══╝  ██║        ██║   
███████╗╚██████╗╚██████╔╝╚██████╗╚██████╔╝███████╗███████╗███████╗╚██████╗   ██║   
╚══════╝ ╚═════╝ ╚═════╝  ╚═════╝ ╚═════╝ ╚══════╝╚══════╝╚══════╝ ╚═════╝   ╚═╝   
    """
    
    colors = [Fore.GREEN, Fore.CYAN, Fore.YELLOW, Fore.BLUE, Fore.MAGENTA, Fore.LIGHTGREEN_EX] if HAS_COLORAMA else [""]
    
    for i, line in enumerate(logo.split("\n")):
        if line.strip():
            color = colors[i % len(colors)]
            sys.stdout.write(color + line + "\n")
            sys.stdout.flush()
            time.sleep(0.015)
            
    print("\n" + (Fore.LIGHTBLACK_EX + "═"*90 if HAS_COLORAMA else "═"*90))
    print(f"       {(Fore.GREEN + Style.BRIGHT + 'EcoCollect Smart Waste Management System - CLI Panel') if HAS_COLORAMA else 'EcoCollect Smart Waste Management System - CLI Panel'}")
    print((Fore.LIGHTBLACK_EX + "═"*90 if HAS_COLORAMA else "═"*90) + "\n")


def animate_step(message, duration=0.8, check_func=None):
    frames = ["⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"]
    start_time = time.time()
    i = 0
    
    # Run loop for specified duration
    while time.time() - start_time < duration:
        frame = frames[i % len(frames)]
        if HAS_COLORAMA:
            sys.stdout.write(f"\r  {Fore.MAGENTA}{frame}{Fore.RESET}  {message}")
        else:
            sys.stdout.write(f"\r  {frame}  {message}")
        sys.stdout.flush()
        time.sleep(0.06)
        i += 1
        
    # Run the check function if provided
    result = True
    if check_func:
        result = check_func()
        
    if result:
        symbol = f"{Fore.GREEN}✔{Fore.RESET}" if HAS_COLORAMA else "✔"
        status_msg = f"{Fore.GREEN}Ready{Fore.RESET}" if HAS_COLORAMA else "Ready"
        sys.stdout.write(f"\r  {symbol}  {message:<50} [{status_msg}]\n")
    else:
        symbol = f"{Fore.YELLOW}⚠{Fore.RESET}" if HAS_COLORAMA else "⚠"
        status_msg = f"{Fore.YELLOW}Offline{Fore.RESET}" if HAS_COLORAMA else "Offline"
        sys.stdout.write(f"\r  {symbol}  {message:<50} [{status_msg}]\n")
        
    sys.stdout.flush()
    return result

def start_server():
    clear_screen()
    print_banner()
    
    # ── ANIMATED SYSTEM STARTUP SEQUENCES ──
    print(f"  {(Fore.CYAN + 'System Diagnostic Checks:' if HAS_COLORAMA else 'System Diagnostic Checks:')}\n")
    
    # 1. Config Check
    animate_step("Loading environment parameters (.env)...", duration=0.6)
    
    # 2. MySQL Port Connection Check
    db_ok = animate_step("Verifying XAMPP MySQL database service...", duration=0.8, check_func=check_mysql_connection)
    if not db_ok:
        print(f"\n  {Fore.YELLOW}⚠ [Notice] MySQL server is not reachable on port 3306.{Fore.RESET}")
        print(f"    Please start MySQL in XAMPP Control Panel to activate database integrations.")
        print(f"    FastAPI will continue to start, but will report connection warnings.\n")
        time.sleep(1.5)
        
    # 3. Static directory check
    def create_dir_check():
        os.makedirs(os.path.join(os.path.dirname(os.path.abspath(__file__)), "uploads"), exist_ok=True)
        return True
    animate_step("Checking directory structures and uploads path...", duration=0.5, check_func=create_dir_check)
    
    # 4. Starting server
    animate_step("Spawning Uvicorn worker process...", duration=0.6)
    time.sleep(0.4)
    
    clear_screen()
    print_banner()
    
    # ── BEAUTIFUL BOXED SUMMARY ──
    local_ip = get_local_ip()
    
    box_width = 88
    top = f"┌{'─' * (box_width - 2)}┐"
    bottom = f"└{'─' * (box_width - 2)}┘"
    empty = f"│{' ' * (box_width - 2)}│"
    
    # Color variables
    c_cyan = Fore.CYAN if HAS_COLORAMA else ""
    c_green = Fore.GREEN if HAS_COLORAMA else ""
    c_magenta = Fore.MAGENTA if HAS_COLORAMA else ""
    c_yellow = Fore.YELLOW if HAS_COLORAMA else ""
    c_reset = Style.RESET_ALL if HAS_COLORAMA else ""
    c_gray = Fore.LIGHTBLACK_EX if HAS_COLORAMA else ""
    c_white = Fore.LIGHTWHITE_EX if HAS_COLORAMA else ""
    
    print(c_magenta + top + c_reset)
    
    # Server title
    title_text = "🚀  E C O C O L L E C T   F A S T A P I   S E R V E R"
    left_padding = (box_width - 2 - len(title_text)) // 2
    right_padding = box_width - 2 - len(title_text) - left_padding
    print(f"│{' ' * left_padding}{c_cyan + Style.BRIGHT + title_text + c_reset}{' ' * right_padding}│")
    
    print(c_magenta + f"├{'─' * (box_width - 2)}┤" + c_reset)
    print(empty)
    
    # Server stats
    print(f"│  {c_white}• Status:{c_reset}      Active (Server listening on {c_green}http://0.0.0.0:8000{c_reset}){' ' * 19}│")
    print(f"│  {c_white}• Local IP:{c_reset}    {c_green}{local_ip:<15}{c_reset} (Use this for Android Emulator / Physical Phones){' ' * 9}│")
    print(empty)
    
    # Documentation resources
    print(f"│  {c_cyan + Style.BRIGHT}📚 API Documentation & Interactive Sandboxes:{c_reset}{' ' * (box_width - 49)}│")
    print(f"│  {c_white}• Local Swagger:{c_reset}   http://127.0.0.1:8000/docs{' ' * 45}│")
    print(f"│  {c_white}• Local ReDoc:{c_reset}     http://127.0.0.1:8000/redoc{' ' * 44}│")
    print(f"│  {c_white}• Network Docs:{c_reset}    http://{local_ip}:8000/docs{' ' * (box_width - 19 - len(local_ip) - 12)}│")
    print(empty)
    
    # Shut down instructions
    instructions = "📢  Logs will stream below. Press Ctrl+C inside the terminal to stop the server."
    left_padding = (box_width - 2 - len(instructions)) // 2
    right_padding = box_width - 2 - len(instructions) - left_padding
    print(f"│{' ' * left_padding}{c_gray + instructions + c_reset}{' ' * right_padding}│")
    
    print(c_magenta + bottom + c_reset)
    print()
    
    try:
        # Run uvicorn process
        subprocess.run([sys.executable, "-m", "uvicorn", "app.main:app", "--host", "0.0.0.0", "--reload"])
    except KeyboardInterrupt:
        print(f"\n\n  {c_green}✔ Server stopped successfully.{c_reset}\n")
        time.sleep(1)

def run_test():
    clear_screen()
    print_banner()
    print(f"  {(Fore.YELLOW + '🧪 Running backend package validation checks...') if HAS_COLORAMA else 'Running backend package validation checks...'}\n")
    
    try:
        res = subprocess.run([sys.executable, "test_backend.py"])
        if res.returncode == 0:
            print(f"\n  {(Fore.GREEN + Style.BRIGHT + '✔ All tests passed successfully!') if HAS_COLORAMA else 'All tests passed successfully!'}")
        else:
            print(f"\n  {(Fore.RED + Style.BRIGHT + '✘ Validation tests failed. Please review error above.') if HAS_COLORAMA else 'Validation tests failed.'}")
    except Exception as e:
         print(f"\n  {(Fore.RED + f'✘ Error running tests: {e}') if HAS_COLORAMA else f'Error running tests: {e}'}")

def display_config():
    clear_screen()
    print_banner()
    print(f"  {(Fore.CYAN + Style.BRIGHT + '⚙ Active Configuration Details:') if HAS_COLORAMA else 'Active Configuration Details:'}\n")
    
    try:
        from app.config import settings
        local_ip = get_local_ip()
        print(f"    • Project Name:  {Fore.GREEN + settings.PROJECT_NAME if HAS_COLORAMA else settings.PROJECT_NAME}")
        print(f"    • Local IP:      {Fore.GREEN + local_ip if HAS_COLORAMA else local_ip}")
        print(f"    • DB Host:      {Fore.YELLOW + settings.DB_HOST if HAS_COLORAMA else settings.DB_HOST}")
        print(f"    • DB Port:      {Fore.YELLOW + settings.DB_PORT if HAS_COLORAMA else settings.DB_PORT}")
        print(f"    • DB Name:      {Fore.YELLOW + settings.DB_NAME if HAS_COLORAMA else settings.DB_NAME}")
        print(f"    • DB Username:  {Fore.YELLOW + settings.DB_USER if HAS_COLORAMA else settings.DB_USER}")
        print(f"    • Database URL: {Fore.LIGHTBLACK_EX + settings.DATABASE_URL if HAS_COLORAMA else settings.DATABASE_URL}")
        print(f"    • JWT Secret:   {Fore.RED + '******' if HAS_COLORAMA else '******'} (Hidden for security)")
    except Exception as e:
        print(f"    {(Fore.RED + f'Error reading config: {e}') if HAS_COLORAMA else f'Error reading config: {e}'}")

def display_docs():
    clear_screen()
    print_banner()
    local_ip = get_local_ip()
    print(f"  {(Fore.CYAN + Style.BRIGHT + '📚 API Documentation Resources:') if HAS_COLORAMA else 'API Documentation Resources:'}\n")
    print("    Once the backend server is running, you can access the following pages:")
    print(f"\n    • Local Swagger UI:   {Fore.GREEN + 'http://127.0.0.1:8000/docs' if HAS_COLORAMA else 'http://127.0.0.1:8000/docs'}")
    print(f"    • External Swagger UI: {Fore.GREEN + f'http://{local_ip}:8000/docs' if HAS_COLORAMA else f'http://{local_ip}:8000/docs'}")
    print(f"    • Local ReDoc UI:     {Fore.GREEN + 'http://127.0.0.1:8000/redoc' if HAS_COLORAMA else 'http://127.0.0.1:8000/redoc'}")
    print(f"    • External ReDoc UI:   {Fore.GREEN + f'http://{local_ip}:8000/redoc' if HAS_COLORAMA else f'http://{local_ip}:8000/redoc'}")
    print(f"    • Root Status Link:   {Fore.GREEN + 'http://127.0.0.1:8000/' if HAS_COLORAMA else 'http://127.0.0.1:8000/'}")

def print_help():
    clear_screen()
    print_banner()
    print(f"  {(Fore.CYAN + Style.BRIGHT + '💡 Usage Instructions:') if HAS_COLORAMA else 'Usage Instructions:'}\n")
    print("    By default, running this script starts the server directly.")
    print("    You can pass the following command line flags to access other features:")
    print("\n    Flags:")
    print(f"      • {Fore.GREEN + '--test' if HAS_COLORAMA else '--test'} or {Fore.GREEN + '-t' if HAS_COLORAMA else '-t'}        : Run validation test script")
    print(f"      • {Fore.GREEN + '--config' if HAS_COLORAMA else '--config'} or {Fore.GREEN + '-c' if HAS_COLORAMA else '-c'}      : Display configuration settings")
    print(f"      • {Fore.GREEN + '--docs' if HAS_COLORAMA else '--docs'} or {Fore.GREEN + '-d' if HAS_COLORAMA else '-d'}        : Display API docs documentation links")
    print(f"      • {Fore.GREEN + '--help' if HAS_COLORAMA else '--help'} or {Fore.GREEN + '-h' if HAS_COLORAMA else '-h'}        : Show this usage guide")

def main():
    if len(sys.argv) == 1:
        # Default action: run server directly
        start_server()
    else:
        arg = sys.argv[1].lower()
        if arg in ["--test", "-t"]:
            run_test()
        elif arg in ["--config", "-c"]:
            display_config()
        elif arg in ["--docs", "-d"]:
            display_docs()
        elif arg in ["--help", "-h"]:
            print_help()
        else:
            print(f"  {(Fore.RED + f'✘ Unknown option: {sys.argv[1]}') if HAS_COLORAMA else f'Unknown option: {sys.argv[1]}'}")
            print(f"  Run {Fore.GREEN + 'python start.py --help' if HAS_COLORAMA else 'python start.py --help'} to see all options.")

if __name__ == "__main__":
    main()
