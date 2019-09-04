import os
import time

from google.cloud import pubsub_v1

from example_events import NewPlay
from example_events.NewPlay import Data

project_id = os.environ.get('GCP_PROJECT')
topic = os.environ.get('PUBSUB_TOPIC')

publisher = pubsub_v1.PublisherClient()
topic_path = publisher.topic_path(project_id, topic)

counts = {
    '1': 20,
    '2': 30,
    '3': 10
}

for k in counts:
    for i in range(counts.get(k)):
        data = Data(videoId=k)
        event = NewPlay(data=data)
        payload = event.to_JSON().encode()
        
        future = publisher.publish(topic_path, data=payload)

print("Waiting for Cloud Pub/Sub to complete publishing events (20s)...")
time.sleep(20)
