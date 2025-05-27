FROM python:3.10-slim

WORKDIR /app

COPY file_watcher.py .

RUN pip install watchdog psycopg2-binary

CMD ["python", "file_watcher.py"]