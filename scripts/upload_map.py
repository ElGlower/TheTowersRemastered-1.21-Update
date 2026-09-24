import sys
import os
import zipfile
import tempfile
import urllib.request
import json
import ssl
import paramiko

# Configuración de HolyHosting
SFTP_HOST = "na-va-04.holy.gg"
SFTP_PORT = 2022
SFTP_USER = "bl_33565.14ce6597"
SFTP_PASS = "p6t90rfx^003"

PANEL_URL = "https://panel.holy.gg"
SERVER_ID = "14ce6597"
API_KEY = "ptlc_HL4MiRjv1aW85qIwNBWD0Yklf95hnIR7Apxj7NVv19T"

ctx = ssl.create_default_context()

def api_request(endpoint, method="GET", payload=None):
    url = f"{PANEL_URL}/api/client/servers/{SERVER_ID}/{endpoint}"
    data = json.dumps(payload).encode('utf-8') if payload else None
    req = urllib.request.Request(url, data=data, method=method)
    req.add_header("Authorization", f"Bearer {API_KEY}")
    req.add_header("Content-Type", "application/json")
    req.add_header("Accept", "application/json")
    req.add_header("User-Agent", "Mozilla/5.0")
    with urllib.request.urlopen(req, context=ctx) as resp:
        if resp.status in (200, 201):
            return json.loads(resp.read().decode('utf-8'))
        return True

def zip_directory(folder_path, output_zip):
    print(f"📦 Comprimiendo '{folder_path}' en zip temporal...")
    with zipfile.ZipFile(output_zip, 'w', zipfile.ZIP_DEFLATED) as zipf:
        for root, dirs, files in os.walk(folder_path):
            for file in files:
                # Omitir archivos de sesión o locks locales si existen
                if file in ("session.lock", "uid.dat"):
                    continue
                file_path = os.path.join(root, file)
                arcname = os.path.relpath(file_path, folder_path)
                zipf.write(file_path, arcname)

def upload_and_extract_map(local_path, target_name):
    if not os.path.exists(local_path):
        print(f"❌ Error: La ruta local '{local_path}' no existe.")
        return False

    temp_zip = None
    if os.path.isdir(local_path):
        fd, temp_zip = tempfile.mkstemp(suffix=".zip")
        os.close(fd)
        # Crear subcarpeta interna con el nombre target para que se extraiga directo
        print(f"📦 Empaquetando mundo en estructura '{target_name}/'...")
        with zipfile.ZipFile(temp_zip, 'w', zipfile.ZIP_DEFLATED) as zipf:
            for root, dirs, files in os.walk(local_path):
                for file in files:
                    if file in ("session.lock", "uid.dat"):
                        continue
                    file_path = os.path.join(root, file)
                    rel = os.path.relpath(file_path, local_path)
                    arcname = os.path.join(target_name, rel).replace("\\", "/")
                    zipf.write(file_path, arcname)
        upload_file = temp_zip
        remote_zip = f"{target_name}_temp.zip"
    else:
        upload_file = local_path
        remote_zip = os.path.basename(local_path)

    # 1. Subir vía SFTP
    print(f"🚀 Subiendo '{remote_zip}' a HolyHosting...")
    transport = paramiko.Transport((SFTP_HOST, SFTP_PORT))
    try:
        transport.connect(username=SFTP_USER, password=SFTP_PASS)
        sftp = paramiko.SFTPClient.from_transport(transport)
        sftp.put(upload_file, remote_zip)
        sftp.close()
        transport.close()
        print("✔ Subida completada con éxito.")
    except Exception as e:
        print(f"❌ Error al subir por SFTP: {e}")
        if temp_zip and os.path.exists(temp_zip):
            os.remove(temp_zip)
        return False

    # 2. Descomprimir en el servidor vía API Pterodactyl
    print(f"⚡ Descomprimiendo en el servidor como '{target_name}'...")
    try:
        api_request("files/decompress", method="POST", payload={"root": "/", "file": remote_zip})
        print(f"✔ Mundo '{target_name}' descomprimido en la raíz del servidor.")
    except Exception as e:
        print(f"❌ Error al descomprimir por API: {e}")

    # 3. Eliminar archivo zip remoto
    try:
        api_request("files/delete", method="POST", payload={"root": "/", "files": [remote_zip]})
        print(f"✔ Archivo temporal remoto '{remote_zip}' eliminado.")
    except Exception as e:
        pass

    # 4. Limpiar zip local si fue generado
    if temp_zip and os.path.exists(temp_zip):
        os.remove(temp_zip)

    print(f"\n🎉 ¡Mundo '{target_name}' instalado y listo en HolyHosting!")
    return True

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Uso: python upload_map.py <ruta_carpeta_o_zip_local> <nombre_mundo_servidor>")
        print("Ejemplo: python upload_map.py \"C:\\Mapas\\sw_islas\" sw_mapa1")
        sys.exit(1)
    
    upload_and_extract_map(sys.argv[1], sys.argv[2])
