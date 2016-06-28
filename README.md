Profiler V6.1.8-SNAPSHOT

[![Build Status](https://drone.io/github.com/arrahtec/profiler/status.png)](https://drone.io/github.com/arrahtec/profiler/latest)   [![Download Open Source Data Quality and Profiling](https://img.shields.io/sourceforge/dm/dataquality.svg)](https://sourceforge.net/projects/dataquality/files/latest/download)   [![Download Open Source Data Quality and Profiling](https://img.shields.io/sourceforge/dw/dataquality.svg)](https://sourceforge.net/projects/dataquality/files/latest/download)

[![Download sub-keenes](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/dataquality/files/latest/download)

**Description**

This project is dedicated to open source data quality and data preparation solutions. Data Quality includes profiling, filtering, governance, similarity check, data enrichment/alteration, real time alerting, basket analysis, bubble chart Warehouse validation, single customer view etc.
This product is developing high performance integrated data management platform which will seamlessly do Data Integration, Data Profiling, Data Quality, Data Preparation, Dummy Data Creation, Meta Data Discovery, Anomaly Discovery, Reporting and Analytic.

It also had Hadoop ( Big data ) support to move files to/from Hadoop Grid, Create, Load and Profile Hive Tables. This project is also known as "Aggregate Profiler"

**Changes**

6.1.6 Changes:

a) Training, Validation and Test data preparation added
b) File Loader added
c) Rounding methods added for data preparation
d) Unspecified bug fixes


**Features**

- Mysql, Oracle,Postgres,Access,Db2,SQL Server certified Big data support - HIVE
- Create Hive table, Profile Hive table, Move file to/from Profiler System and Hadoop Grid
- Fuzzy Logic based similarity check, Cardinailty check between tables and files
- Export and import from XML, XLS or CSV format, PDF export
- File Analysis, Regex search, Standardization, DB search
- Complete DB Scan, SQL interface, Data Dictionary, Schema Comparison
- Statistical Analysis, Reporting ( dimension and measure based), Ad Hoc reports and Analytics
- Pattern Matching , DeDuplication, Case matching, Basket Analysis, Distribution Chart
- Data generation, Data Preparation and Data masking features
- Meta Data Information, Reverse engineering of Data Model
- Timeliness analysis , String length analysis, KMean, Prediction, Regression
- Address Correction, Single View of Customer, Product, Golden merge for records
- Record Match, Linkage and Merge added based on fuzzy logic
- Format Creation, Format Matching ( Phone, Date, String and Number), Format standardization


Getting Started
---------------

>Using pre-compiled distribution - you can directly download the pre-compiled binaries from sourceforge

[![Download Open Source Data Quality and Profiling](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/dataquality/files/latest/download)

1. Unzip the download release file
2. Go to bin directory
3. run `runprofiler.[sh][bat]`


>Building and Running

1. git clone git@github.com:arrahtec/profiler.git
2. cd profiler
3. mvn package
4. cd profiler-ui/target
5. unzip the ProfilerV<version>.zip
6. cd ProfilerV<version>
7. cd bin
8. runprofiler.[sh|bat]





