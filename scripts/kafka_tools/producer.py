from kafka import KafkaConsumer, KafkaProducer
import json
# To consume latest messages and auto-commit offsets
consumer = KafkaProducer(bootstrap_servers=['localhost:9092'])
print("sending")
msg = '{"id":1, "name":"oguz"}'
consumer.send("drones",json.dumps(msg).encode('utf-8'))
consumer.flush()
print("sent")