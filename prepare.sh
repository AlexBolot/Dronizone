mvn clean install
cd mock_notification_service
mvn clean install
cd ../dronemock
mvn clean install
cd ../integration-tester
mvn clean install
cd ../
docker-compose up -d --build
