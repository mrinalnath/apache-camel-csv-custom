# apache-camel-csv-custom

apache-camel-csv-custom is derived from apache-camel-csv 
to add dymanic ability to define CsvDataFormat
using the exchange header CSVDATAFORMAT_OVERRIDE


#how to build

Merge the code into main branch

mvn clean install -Pfastinstall

mvn clean install -Pfastinstall -pl :camel-csv-custom
