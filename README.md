Test Task Description
==================================

Initial Data:
-
Several CSV files. The number of files can be quite large (up to 100,000).

The number of rows within each file can reach up to several million.

Each file contains 5 columns: product ID (integer), Name (string), Condition (string), State (string), Price (float).

The same product IDs may occur more than once in different CSV files and in the same CSV file.

To do:
-
Write a console utility using Java programming language that allows getting a selection of the cheapest 1000 products from the input CSV files, but no more than 20 products with the same ID. Use parallel processing to increase performance.

Utility Result:
-
Output CSV file that meets the following criteria:
no more than 1000 products sorted by Price from all files;
no more than 20 products with the same ID.


Getting
==================
For getting clone source code:  
```
git clone https://github.com/molokovskikh/ao1SolutionTestTask.git
```

Build
==================
If you just would like build the application run to follow command from project directory:
```
./gradlew installDist
```

The result will be present in **$projectDir/build/install/csv-selection**.


Run
==================
After building you can move to **$projectDir/build/install/csv-selection/bin** and run there ***csv-selection*** shell (for *Windows OS* **csv-selection.bat**):
```
cd build/install/csv-selection/bin
./csv-selection --dir /full_path_to_csv_files_dir --output /full_path_to_result_csv_file
``` 

You can also point spring application config file use command like under:

```
cd build/install/csv-selection/bin
./csv-selection --dir /full_path_to_csv_files_dir --output /full_path_to_result_csv_file --spring.config.location=file:./config/
``` 
In directory **projectDir/build/install/csv-selection/config** placed config file by named **application.yml** 

In this config file you can change parameters (top most cheap 1000 products, and 20 products with the same ID) to run application:
```yaml
app:
  same-product-id-retry-limit: 20
  records-total: 1000
```

Test
==================
For run acceptance test do following:
```
./gradlew test
``` 
Before, ensure what you have enough disk space in temporary folder your OS.
