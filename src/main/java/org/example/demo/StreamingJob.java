package org.example.demo;

import java.util.Map;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.gcp.pubsub.PubSubSource;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.streaming.api.windowing.time.Time;

public class StreamingJob {

	public static void main(String[] args) throws Exception {
		// Set up the streaming execution environment
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.enableCheckpointing(4000);

		// Set up a Cloud Pub/Sub topic as input
		
		// deserializer controls how Apache Flink deserializes the message
		// returns from Cloud Pub/Sub (a byte string) to a PubSubEvent
		// (custom Java class). See PubSubEvent.java for definition.
		PlayCountEventDeserializationSchema deserializer = new PlayCountEventDeserializationSchema();

		Map<String, String> envVars = System.getenv();
		String projectId = envVars.get("GCP_PROJECT");
		String subscription = envVars.get("PUBSUB_SUBSCRIPTION");

		SourceFunction<PubSubEvent> pubSubSource = PubSubSource.newBuilder()
															   .withDeserializationSchema(deserializer)
															   .withProjectName(projectId)
															   .withSubscriptionName(subscription)
															   .build();
		
		DataStream<PubSubEvent> dataStream = env.addSource(pubSubSource);

		// Start processing data
		DataStream<Tuple2<String, Long>> plays = dataStream
			// Partitions the incoming events in the stream using its specified
			// key (videoId). getKeySelector() here tells Flink which attribute
			// to use as the key.
			.keyBy(PubSubEvent.getKeySelector())
			.filter(new CustomFilter())
			.flatMap(new FlatMapFunction<PubSubEvent, Tuple2<String, Long>>() {
				static final long serialVersionUID = 1L;

				@Override
				public void flatMap(PubSubEvent event, Collector<Tuple2<String, Long>> out) {
					Tuple2<String, Long> t = new Tuple2<String, Long>();
					t.f0 = event.videoId;
					t.f1 = 1L;
					out.collect(t);
				};
			})
			.keyBy(0)
			.timeWindow(Time.seconds(10L))
			.reduce(new ReduceFunction<Tuple2<String, Long>>() {
				static final long serialVersionUID = 1L;

				@Override
				public Tuple2<String, Long> reduce(Tuple2<String, Long> value1, Tuple2<String, Long> value2) {
					Tuple2<String, Long> t = new Tuple2<String, Long>();
					t.f0 = value1.f0;
					t.f1 = value1.f1 + value2.f1;
					return t;
				}
			});
		
		plays.print();

		env.execute("Collecting play counts");
	}
}
