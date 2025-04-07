#!/bin/bash

# Check if the user provided a directory
if [ -z "$1" ]; then
  echo "Usage: $0 <directory>"
  exit 1
fi

# Assign the first argument to the directory variable
DIRECTORY="$1"

# Find and remove all .pb files in the specified directory and sub-directories
find "$DIRECTORY" -type f -name '*.pb' -exec rm -v {} \;

echo "All .pb files removed from $DIRECTORY and its sub-directories."