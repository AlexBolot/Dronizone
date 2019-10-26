from kafka import KafkaConsumer, KafkaProducer
import json
print("hello")
# To consume latest messages and auto-commit offsets
consumer = KafkaConsumer("drones",bootstrap_servers=['localhost:9092'],
                        group_id='tools',auto_offset_reset='earliest',
                        enable_auto_commit=True)
print("consuming")
for message in consumer:
    print("consuming")
    message = message.value
    print(message)