package org.example.demo;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.checkpoint.ListCheckpointed;

public class CustomFilter extends RichFilterFunction<PubSubEvent> implements ListCheckpointed<HashSet<String>> {
    static final long serialVersionUID = 1L;
    private LoadingCache<String, Boolean> cache;
    private long cacheExpirationTime = 10L;

    public CustomFilter() {}

    public CustomFilter(long cacheExpirationTime) {
        this.cacheExpirationTime = cacheExpirationTime;
    }

    // Set up the cache.
    public void setUpCache() {
        this.cache = CacheBuilder
        .newBuilder()
        .expireAfterWrite(this.cacheExpirationTime, TimeUnit.MINUTES)
        .build(new CacheLoader<String, Boolean>() {
            @Override
            public Boolean load(String s) throws Exception {
                return false;
            }
        });
    }

    // Call setUpCache when the filter initiates.
    @Override
    public void open(Configuration config) throws Exception {
        setUpCache();
    }

    // Specify how this custom filter works. The system queries the cache
    // with the ID of the incoming event every time it arrives; if it hits,
    // the filter returns false; otherwise it adds the ID to the cache and
    // returns true.
    @Override
    public boolean filter(PubSubEvent event) throws Exception {
        String eventId = event.eventId;
        boolean ok = this.cache.get(eventId);

        if (!ok) {
            this.cache.put(eventId, true);
            return true;
        } else {
            return false;
        }
    }

    // snapshotState and restoreStore help restore the contents of the
    // cache if any partition of Flink crashs.

    // snapshotState checks in the contents of the cache at Flink checkpoints.
    @Override
    public List<HashSet<String>> snapshotState(long checkpointId, long timestamp) {
        return Collections.singletonList(new HashSet<String>(this.cache.asMap().keySet()));
    }

    // If the system crashes, restoreState loads the contents checked in at
    // the last checkpoint back to the cache.
    @Override
    public void restoreState(List<HashSet<String>> state) throws Exception {
        setUpCache();
        for (String eventId : state.get(0)) {
            this.cache.put(eventId, true);
        }
    }
}