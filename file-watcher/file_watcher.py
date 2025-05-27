import os
import time
from datetime import datetime
import psycopg2
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler

# === CONFIG ===
WATCH_FOLDER = "/app/source_data"  # This is the path inside the container


DB_CONFIG = {
    'dbname': 'metadata_db',
    'user': 'metadata_user',
    'password': 'metadata_pass',
    'host': 'metadata-db',   # Use Docker Compose service name
    'port': 5432             # default Postgres port inside Docker
}

# === EVENT HANDLER ===
class NewFileHandler(FileSystemEventHandler):
    def on_created(self, event):
        if event.is_directory:
            return
        
        file_path = event.src_path
        file_name = os.path.basename(file_path)
        arrival_time = datetime.now()

        print(f"[+] New file detected: {file_name} at {arrival_time}")

        try:
            conn = psycopg2.connect(**DB_CONFIG)
            cur = conn.cursor()
            cur.execute("""
                INSERT INTO incoming_files (file_name, file_path, arrival_time, status)
                VALUES (%s, %s, %s, %s)
            """, (file_name, file_path, arrival_time, 'Pending'))

            conn.commit()
            cur.close()
            conn.close()

            print(f"[✓] Task inserted for: {file_name}")

        except Exception as e:
            print(f"[✗] Error inserting task for {file_name}: {e}")

# === WATCH FUNCTION ===
def start_watching():
    event_handler = NewFileHandler()
    observer = Observer()
    observer.schedule(event_handler, path=WATCH_FOLDER, recursive=False)
    observer.start()
    print(f"🔍 Watching for new files in: {WATCH_FOLDER}")

    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print("✋ Stopping watcher...")
        observer.stop()
    observer.join()

# === ENTRY POINT ===
if __name__ == "__main__":
    start_watching()
