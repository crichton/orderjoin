#!/bin/bash

# Check if a filename is provided as an argument
if [ $# -eq 0 ]; then
  echo "Usage: $0 <filename>"
  exit 1
fi

# Store the filename provided as an argument
filename="$1"

# Check if the file exists
if [ ! -f "$filename" ]; then
  echo "File '$filename' not found."
  exit 1
fi

# Read and echo each line from the file
while IFS= read -r topic; do
  echo "creating $topic ..."
  ./kafka-topics --bootstrap-server localhost:9092 --create --topic "$topic" --partitions 1 --replication-factor 1
done < "$filename"

