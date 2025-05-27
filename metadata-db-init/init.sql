CREATE TABLE IF NOT EXISTS incoming_files (
    id SERIAL PRIMARY KEY,
    file_name TEXT NOT NULL,
    file_path TEXT NOT NULL,
    arrival_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processing_start_time TIMESTAMP,
    processing_end_time TIMESTAMP,
    status TEXT,
    status_message TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
