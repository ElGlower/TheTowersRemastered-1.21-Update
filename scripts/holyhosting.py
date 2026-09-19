import urllib.request
import json
import ssl
import sys
import os
import paramiko

sys.stdout.reconfigure(encoding='utf-8')

# Configuración de HolyHosting
SFTP_HOST = "na-va-04.holy.gg"
SFTP_PORT = 2022
SFTP_USER = "bl_33565.14ce6597"
SFTP_PASS = "p6t90rfx^003"

PANEL_URL = "https://panel.holy.gg"
SERVER_ID = "14ce6597"
API_KEY = "ptlc_HL4MiRjv1aW85qIwNBWD0Yklf95hnIR7Apxj7NVv19T"

LOCAL_JAR = r"C:\Users\javif\Documents\compilados\DestinyTowers-1.0.0.jar"
REMOTE_JAR = "plugins/DestinyTowers-1.0.0.jar"

ctx = ssl.create_default_context()

def upload_jar():
    if not os.path.exists(LOCAL_JAR):
        print(f"Error: {LOCAL_JAR} no existe.")
        return False
    size = os.path.getsize(LOCAL_JAR)
    print(f"📦 Subiendo {LOCAL_JAR} ({size} bytes) a HolyHosting ({REMOTE_JAR})...")
    transport = paramiko.Transport((SFTP_HOST, SFTP_PORT))
    try:
        transport.connect(username=SFTP_USER, password=SFTP_PASS)
        sftp = paramiko.SFTPClient.from_transport(transport)
        sftp.put(LOCAL_JAR, REMOTE_JAR)
        sftp.close()
        transport.close()
        print("✔ ¡JAR subido exitosamente a la carpeta /plugins!")
        return True
    except Exception as e:
        print(f"❌ Error al subir: {e}")
        return False

def send_command(cmd):
    url = f"{PANEL_URL}/api/client/servers/{SERVER_ID}/command"
    data = json.dumps({"command": cmd}).encode('utf-8')
    req = urllib.request.Request(url, data=data, method="POST")
    req.add_header("Authorization", f"Bearer {API_KEY}")
    req.add_header("Content-Type", "application/json")
    req.add_header("Accept", "application/json")
    req.add_header("User-Agent", "Mozilla/5.0")
    try:
        with urllib.request.urlopen(req, context=ctx) as resp:
            print(f"✔ Comando enviado a la consola: '{cmd}'")
            return True
    except Exception as e:
        print(f"❌ Error al enviar comando: {e}")
        return False

def get_status():
    url = f"{PANEL_URL}/api/client/servers/{SERVER_ID}/resources"
    req = urllib.request.Request(url)
    req.add_header("Authorization", f"Bearer {API_KEY}")
    req.add_header("Accept", "application/json")
    req.add_header("User-Agent", "Mozilla/5.0")
    try:
        with urllib.request.urlopen(req, context=ctx) as resp:
            res = json.loads(resp.read().decode('utf-8')).get("attributes", {})
            state = res.get("current_state", "unknown")
            r = res.get("resources", {})
            ram = r.get("memory_bytes", 0) / (1024 * 1024)
            cpu = r.get("cpu_absolute", 0)
            disk = r.get("disk_bytes", 0) / (1024 * 1024)
            print("=== ESTADO DEL SERVIDOR ===")
            print(f"Estado: {state.upper()}")
            print(f"RAM: {ram:.1f} MB | CPU: {cpu:.1f}% | Disco: {disk:.1f} MB")
            return state
    except Exception as e:
        print(f"❌ Error al obtener estado: {e}")
        return None

def set_power(signal):
    url = f"{PANEL_URL}/api/client/servers/{SERVER_ID}/power"
    data = json.dumps({"signal": signal}).encode('utf-8')
    req = urllib.request.Request(url, data=data, method="POST")
    req.add_header("Authorization", f"Bearer {API_KEY}")
    req.add_header("Content-Type", "application/json")
    req.add_header("Accept", "application/json")
    req.add_header("User-Agent", "Mozilla/5.0")
    try:
        with urllib.request.urlopen(req, context=ctx) as resp:
            print(f"✔ Señal de energía '{signal}' enviada al servidor.")
            return True
    except Exception as e:
        print(f"❌ Error al enviar señal: {e}")
        return False

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Uso: python holyhosting.py [deploy | status | cmd <comando> | restart | stop | start]")
        sys.exit(1)
    action = sys.argv[1].lower()
    if action == "deploy":
        upload_jar()
    elif action == "status":
        get_status()
    elif action == "cmd":
        if len(sys.argv) < 3:
            print("Debes especificar el comando a enviar.")
        else:
            send_command(" ".join(sys.argv[2:]))
    elif action in ["restart", "stop", "start"]:
        set_power(action)
    else:
        print("Acción no reconocida.")
