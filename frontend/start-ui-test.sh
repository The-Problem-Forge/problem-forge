#!/bin/sh

# Script to start the frontend in UI test mode
# This sets REACT_APP_UI_TEST=true and starts the dev server

echo "Starting frontend in UI test mode..."
echo "UI_TEST flag is enabled - using mock data instead of API calls"

# Ensure we're in the frontend directory
cd "$(dirname "$0")"

# Set the UI test flag and start the dev server
REACT_APP_UI_TEST=true npm start
