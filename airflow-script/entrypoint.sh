#!/bin/bash
set -e

pip install apache-airflow-providers-docker

# Initialize the DB
airflow db init

# Create admin user, ignore error if user already exists
airflow users create \
    --username admin \
    --password admin \
    --firstname Admin \
    --lastname User \
    --role Admin \
    --email admin@example.com || echo "User admin already exists"

# Start scheduler in background
airflow scheduler &

# Start webserver as the main container process
exec airflow webserver