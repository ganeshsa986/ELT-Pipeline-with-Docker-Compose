# ELT Pipeline with Airflow, Docker, Spark, and PostgreSQL

## Overview

This project implements a containerized ELT (Extract, Load, Transform) pipeline orchestrated by Apache Airflow. The pipeline watches a folder for new CSV files, tracks metadata in a PostgreSQL database, processes data with Apache Spark, and stores results in a production PostgreSQL database. Finally, Apache Superset is used for data visualization.

---

## Architecture

- **Airflow**: Orchestrates ETL jobs with `DockerOperator`.
- **File Watcher**: Monitors new files arriving in a source data folder and inserts metadata into the metadata DB.
- **Spark Job**: Reads pending files from metadata DB, processes data, and writes results to the production DB.
- **PostgreSQL**: Two DBs
  - `metadata_db`: Stores metadata about incoming files and their processing status.
  - `analytics_db`: Stores processed data (e.g., long-term customers, churn summary).
- **Superset**: Visualizes the processed data.

---

## Components

### 1. Docker Compose Services

- `airflow`: Runs Apache Airflow with DAGs for orchestrating tasks.
- `metadata-db`: PostgreSQL instance storing metadata info.
- `prod-db`: PostgreSQL instance for analytics data.
- `file-watcher`: Python app watching for new files and inserting metadata.
- `superset`: Dashboard and visualization tool.

### 2. File Watcher

- Watches `/app/source_data` for new CSV files.
- Inserts file metadata into the `incoming_files` table in the metadata DB with status `Pending`.

### 3. Spark Job (Scala)

- Reads `Pending` files from `incoming_files` table.
- Cleans and transforms data:
  - Filters long-term customers.
  - Flags churned customers.
  - Aggregates churn rates.
- Writes processed data to `long_term_customers` and `churn_summary` tables in production DB.
- Updates file processing status in metadata DB.

### 4. Airflow DAG

- Runs Spark job in a Docker container.
- Mounts local source data folder inside container.