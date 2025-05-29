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

---

## Prerequisites

- Docker & Docker Compose installed on your machine
- Java & Scala build tools (SBT) if you want to build/modify the Spark job
- DBeaver (or any SQL client) installed to connect and manage PostgreSQL databases visually

| Service       | Host                       | Port | Database      | User           | Password       |
| ------------- | -------------------------- | ---- | ------------- | -------------- | -------------- |
| Metadata DB   | localhost (or Docker host) | 5434 | metadata\_db  | metadata\_user | metadata\_pass |
| Production DB | localhost (or Docker host) | 5433 | analytics\_db | prod\_user     | prod\_pass     |

In DBeaver, create new connections using the above credentials to browse tables like incoming_files, long_term_customers, and churn_summary.

### Update Data Folder Path in Airflow DAG
  - Before running the pipeline, you need to update the Airflow DAG to mount your local data folder correctly inside the Docker container.

1. Locate your project folder and data directory.
  - Your project is cloned under a folder named ELT-Pipeline-with-Docker-Compose. The data files should be inside the data subfolder:

2. Find the absolute path to the `data` folder.

  - On **macOS/Linux**, open a terminal, navigate to your project folder, and run: pwd
  - The output is the absolute path to your project folder. Append /data to get the full path to your data folder.

3. Update the source path in the Airflow DAG file (customer_etl_pipeline.py):

Mount(
    source='/Users/ganeshsangle986/Documents/Projects/ELT-Pipeline-with-Docker-Compose/data',  # <-- Replace this with your absolute path
    target='/app/source_data',
    type='bind'
)

4. Keep the target path as /app/source_data (path inside the container).

This step allows the Docker container to access your local data files during the Spark job execution.

---

## How to Run

1. **Clone the repository:**
- git clone https://github.com/yourusername/ELT-Pipeline-with-Docker-Compose.git
- cd ELT-Pipeline-with-Docker-Compose

2. **Update the data folder path in the Airflow DAG as described above.**

3. **Start the Docker Compose stack:**
- docker-compose -f dockerfile.yml up -d

4. **Connect to the Metadata PostgreSQL Database and confirm the incoming_files table exists:**
- SELECT * FROM incoming_files;

5. **Add Input File**
- Copy the file customer_churn_data.csv from your input dataset into the data/ folder.

6. **Watch File Detection:**
- Once the file is added, the watcher service will insert its metadata into the incoming_files table with status Pending.

7. **Verify Metadata Entry**
- SELECT * FROM incoming_files;

8. **Build Spark Runner Image**
- Navigate to the spark-runner folder and run:
- docker build -t spark-runner .

9. **Verify the image exists:**
- docker images

10. **Access Airflow Environment:**
- Go to http://localhost:8090/login/
- Username: admin
- Password: admin
- Run the customer-etl-pipeline DAG. It will:
  - Read pending files from the metadata DB
  - Transform the data
  - Write results to long_term_customers and churn_summary in the analytics DB
  - Verify output tables:
    - SELECT * FROM long_term_customers;
    - SELECT * FROM churn_summary;

11. **Access Superset**
- Open http://localhost:8088/login/
  - Username: admin
  - Password: admin
- It will not include any datasources, charts, or dashboards. I have already created and exported a dashboard—just import it, and you should be able to visualize the data.
  - Click on import Dashboards
  - Select the zip from superset/pre-configured-template
  - Enter password for:
    - AnalyticsDB: prod_pass
    - MetadataDB: metadata_pass