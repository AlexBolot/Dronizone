#!/usr/bin/env bash

mvn clean install -DskipTests
cd mock_notification_service
mvn clean install -DskipTests
cd ../dronemock
mvn clean install -DskipTests
cd ../