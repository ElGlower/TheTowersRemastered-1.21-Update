import os
import sys
import time
import json
import ssl
import urllib.request
import paramiko

sys.stdout.reconfigure(encoding='utf-8', line_buffering=True)

SFTP_HOST = "na-va-04.holy.gg"
SFTP_PORT = 2022
SFTP_USER = "bl_33565.14ce6597"
SFTP_PASS = "p6t90rfx^003"

PANEL_URL = "https://panel.holy.gg"
SERVER_ID = "14ce6597"
API_KEY = "ptlc_HL4MiRjv1aW85qIwNBWD0Yklf95hnIR7Apxj7NVv19T"

ctx = ssl.create_default_context()

JARS = [
    (r"C:\Users\javif\Documentos\destinywars\target\destinywars-1.0.0.jar", "plugins/destinywars-1.0.0.jar"),
    (r"C:\Users\javif\Documentos\destinylobby\target\destinylobby-1.0.0.jar", "plugins/destinylobby-1.0.0.jar"),
    (r"C:\Users\javif\Documents\DestinyTowers\target\DestinyTowers-1.0.0.jar", "plugins/DestinyTowers-1.0.0.jar"),
    (r"C:\Users\javif\Documentos\destinyperms\target\destinyperms-1.0.0.jar", "plugins/destinyperms-1.0.0.jar"),
    (r"C:\Users\javif\Documentos\destinycosmetics\target\destinycosmetics-1.0.0.jar", "plugins/destinycosmetics-1.0.0.jar"),
    (r"C:\Users\javif\Documentos\destinylobby\src\main\resources\config.yml", "plugins/DestinyLobby/config.yml"),
    (r"C:\Users\javif\Documentos\destinyperms\editor.html", "plugins/DestinyPerms/editor.html"),
    (r"C:\Users\javif\Documentos\destinywars\src\main\resources\chests.yml", "plugins/DestinyWars/chests.yml"),
    (r"C:\Users\javif\Documentos\destinywars\src\main\resources\config.yml", "plugins/DestinyWars/config.yml"),
]

def upload_jars():
    print("🚀 Conectando a HolyHosting SFTP...")
    transport = paramiko.Transport((SFTP_HOST, SFTP_PORT))
    try:
        transport.connect(username=SFTP_USER, password=SFTP_PASS)
        sftp = paramiko.SFTPClient.from_transport(transport)
        for local, remote in JARS:
            if not os.path.exists(local):
                print(f"⚠️ Archivo no encontrado: {local}")
                continue
            size = os.path.getsize(local)
            print(f"📦 Subiendo {os.path.basename(local)} ({size} bytes) -> {remote}...")
            sftp.put(local, remote)
            print(f"✔ {os.path.basename(local)} subido correctamente.")
        sftp.close()
        transport.close()
        print("🎉 Todos los JARs han sido subidos exitosamente.")
        return True
    except Exception as e:
        print(f"❌ Error en SFTP: {e}")
        return False

def restart_server():
    print("🔄 Reiniciando servidor en panel.holy.gg...")
    url = f"{PANEL_URL}/api/client/servers/{SERVER_ID}/power"
    data = json.dumps({"signal": "restart"}).encode('utf-8')
    req = urllib.request.Request(url, data=data, method="POST")
    req.add_header("Authorization", f"Bearer {API_KEY}")
    req.add_header("Content-Type", "application/json")
    req.add_header("Accept", "application/json")
    req.add_header("User-Agent", "Mozilla/5.0")
    try:
        with urllib.request.urlopen(req, context=ctx) as resp:
            print("✔ Señal de reinicio enviada correctamente.")
            return True
    except Exception as e:
        print(f"❌ Error enviando señal de reinicio: {e}")
        return False

def check_status():
    url = f"{PANEL_URL}/api/client/servers/{SERVER_ID}/resources"
    req = urllib.request.Request(url)
    req.add_header("Authorization", f"Bearer {API_KEY}")
    req.add_header("Accept", "application/json")
    req.add_header("User-Agent", "Mozilla/5.0")
    try:
        with urllib.request.urlopen(req, context=ctx) as resp:
            data = json.loads(resp.read().decode('utf-8'))
            state = data.get("attributes", {}).get("current_state", "unknown")
            return state
    except Exception as e:
        return str(e)

if __name__ == "__main__":
    if upload_jars():
        restart_server()
        print("⏳ Monitoreando estado de arranque...")
        time.sleep(5)
        for i in range(12):
            state = check_status()
            print(f"[{i*5}s] Estado actual: {state}")
            if state == "running":
                print("✅ ¡Servidor en línea y operativo!")
                break
            time.sleep(5)
