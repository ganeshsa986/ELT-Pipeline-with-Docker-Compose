# 🚀 Modern Data Lakehouse Platform with Apache Iceberg & Apache Polaris

> Production-inspired Data Lakehouse built using Apache Spark, Apache
> Iceberg, Apache Polaris, PostgreSQL, MinIO, and Apache Airflow.

## Overview

This project demonstrates how to build a modern Data Lakehouse by
separating compute, storage, metadata, governance, and orchestration.

## Architecture

``` mermaid
flowchart TB
Airflow[Apache Airflow] --> Spark[Apache Spark]
Spark --> Analytics[(Analytics PostgreSQL)]
Spark --> Polaris[Apache Polaris]
Polaris --> Gov[(Polaris PostgreSQL)]
Polaris --> Iceberg[Apache Iceberg]
Iceberg --> MinIO[MinIO Warehouse]
```

## Components

### Apache Spark

-   ETL
-   Iceberg writes
-   PostgreSQL writes

### Apache Polaris

-   REST Catalog
-   OAuth Authentication
-   Catalog & Namespace Management
-   Governance

### PostgreSQL

Two databases: - Analytics Database - Polaris Governance Database

### MinIO

Stores Iceberg metadata and Parquet files.

## Run

``` bash
docker compose up -d
cd spark-scala-project
sbt clean assembly
```
