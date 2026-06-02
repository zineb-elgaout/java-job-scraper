# Java Job Scraper & Machine Learning Platform

A complete Java-based job scraping and analytics application built for Moroccan job portals. This project collects job offers, cleans and enriches data, exports datasets for Weka, and provides both a Swing GUI and a command-line ML workflow.

## 🚀 Project Overview

This repository contains:

- Web scrapers for job portals: `Rekrute`, `Emploi.ma`, and `MarocAnnonces`
- Data cleaning, feature extraction, and category balancing pipeline
- Weka-compatible export for classification and analysis
- A GUI dashboard with login, site selection, job listings, statistics, and ML recommendations
- A CLI-based machine learning engine using J48, Naive Bayes, Random Forest, and k-NN

## 📁 Key Modules

- `src/scraping/` — web scrapers for Moroccan job sites
- `src/processing/` — data cleaning and transformation pipeline
- `src/ml/` — training, evaluation and model persistence
- `src/ui/` — Swing interface and dashboard application
- `src/dao/` — database access objects
- `lib/` — third-party JAR dependencies
- `models/` — trained Weka models and saved classifier files
- `data/` — cleaned dataset examples and ARFF exports

## ✅ Features

- Scrapes job listings with site-specific parsers
- Normalizes and extracts job attributes from raw text
- Balances label categories for better ML performance
- Generates clean CSV and ARFF files for Weka
- Trains and evaluates multiple classifiers
- Provides job recommendation logic in the GUI
- Supports model saving and loading

## 🛠️ Prerequisites

- Java 11+ installed
- MySQL server available
- Git clone of this repository
- Required JARs are included in `lib/`

### Database configuration

The default database connection is configured in `src/util/DBConnection.java`:

- URL: `jdbc:mysql://localhost:3307/job_scraper`
- User: `root`
- Password: `` (empty)

Update the connection parameters if your MySQL server uses a different port, database name, or credentials.

## 💡 Build & Run

### Compile all source files

From the repository root:

```powershell
javac -cp "lib/*" -d bin src\**\*.java
```

### Run the GUI application

```powershell
java -cp "bin;lib/*" main.Main
```

### Run the machine learning CLI

```powershell
java -cp "bin;lib/*" ml.MLMain
```

### Run the full ETL pipeline

```powershell
java -cp "bin;lib/*" processing.RunCompletePipelineFixed
```

## 📊 Recommended Workflow

1. Run `processing.RunCompletePipelineFixed` to create cleaned datasets and Weka-ready files.
2. Open `data/06_weka_ready_fixed.arff` in Weka or use `ml.MLMain` to train models.
3. Use the GUI via `main.Main` for scraping, job browsing, and recommendations.

## 🧪 Included Artifacts

The project already contains useful output files such as:

- `data/01_données_nettoyées.csv`
- `data/06_weka_ready_fixed.arff`
- `04_dataset_complet_fixed.csv`
- `05_dataset_simplifié_fixed.csv`
- `07_guide_weka_fixed.txt`

## 📦 Dependencies

The application depends on the following libraries located in `lib/`:

- `weka.jar`
- `jsoup-1.21.2.jar`
- `mysql-connector-j-9.5.0.jar`
- `opencsv-5.9.jar`
- `jfreechart`, `jcommon`, `orsonpdf`, `orsoncharts`, and others for charts and UI utilities

## 📌 Notes

- The `models/` directory is created automatically when saving trained classifiers.
- The GUI uses JDBC and requires the MySQL server to be running before launch.
- If you change the database port or credentials, adjust `src/util/DBConnection.java` and the processing scripts accordingly.

## ✨ Contribution

Contributions, bug fixes, and improvements are welcome. For major changes, please create a branch and submit a pull request.
