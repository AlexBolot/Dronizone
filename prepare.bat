call mvn clean install -DskipTests
cd mock_notification_service
call mvn clean install -DskipTests
call cd ../dronemock
call mvn clean install -DskipTests
call cd ../