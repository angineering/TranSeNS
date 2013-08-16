#!/bin/bash

HOST=http://app_user:Android0.1@angela.computicake.co.uk:5986

curl -X PUT $HOST/$1/_design/views --data-binary @designdoc.json

curl -X GET $HOST/$1/_design/views/_list/csv_acc/z?group=true > data/z-$1\.csv

curl -X GET $HOST/$1/_design/views/_list/csv_acc/xyzAll?group=true > data/all-$1\.csv

curl -X GET $HOST/$1/_design/views/_list/csv_acc/y?group=true > data/y-$1\.csv
