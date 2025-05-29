import os
from datetime import datetime
from airflow import DAG
from airflow.providers.docker.operators.docker import DockerOperator
from docker.types import Mount
import logging

default_args = {
    'start_date': datetime(2025, 5, 26),
}

with DAG(
    dag_id='customer-etl-pipeline',
    default_args=default_args,
    schedule_interval=None,
    catchup=False,
) as dag:

    run_spark_job = DockerOperator(
        task_id='spark_job',
        image='spark-runner',
        container_name='spark-job-runner',
        api_version='auto',
        auto_remove=True,
        docker_url='unix://var/run/docker.sock',
        network_mode='default',
        mounts=[
            Mount(
                source= '/Users/ganeshsangle986/Documents/Projects/elt-pipeline/data',
                target='/app/source_data',
                type='bind'
            )
        ],
    )
