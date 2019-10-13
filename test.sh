docker-compose up --build
echo "We simulate an order rpc call with a post call"
curl -d "@./data_sets/order.json"  -H "Content-Type: application/json"  -X POST http://localhost:8082/order
echo "we see the order 1 preparË©e it's items, and notify the server that it is ready for pickup"
curl -X PUT http://localhost:8081/warehouse/orders/1
curl -X GET http://localhost:8083/drone/fleet/command/callback
echo "Elena called the drones back"
sudo docker logs dronazone-notification-mock-server | tail -n 10
echo "Thoses are the docker notification logs to show that a customer notification was trigger on the drone approching"
docker-compose down