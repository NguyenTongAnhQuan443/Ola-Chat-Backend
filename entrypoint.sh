#!/bin/sh
# filepath: d:\IUH\HK8\NewTechnologyInITApplicationDevelopment\Project\Ola-Chat-Backend\entrypoint.sh

echo "Starting Ola-Chat Backend..."

# Check environment
if [ "${RENDER}" = "true" ]; then
  echo "Running in Render environment"
else
  echo "Running in non-Render environment"
fi

# Handle Firebase credentials
if [ -f "/etc/secrets/serviceAccountKey.json" ]; then
  echo "Using Firebase credentials from /etc/secrets/serviceAccountKey.json"
  export GOOGLE_APPLICATION_CREDENTIALS="/etc/secrets/serviceAccountKey.json"
else
  echo "WARNING: Firebase credentials not found at /etc/secrets/serviceAccountKey.json"
fi

# Handle other secrets if needed
if [ -d "/etc/secrets" ]; then
  echo "Secrets directory exists"
  ls -la /etc/secrets
fi

# Start the application
echo "Launching application..."
exec java ${JAVA_OPTS} -jar ola-chat-backend.jar