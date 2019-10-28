#!/usr/bin/env bash

mvn clean install
cd mock_notification_service
mvn clean install
cd ../dronemock
mvn clean install
cd ../