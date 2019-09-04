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

    @Override
    public void open(Configuration config) throws Exception {
        setUpCache();
    }

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

    @Override
    public List<HashSet<String>> snapshotState(long checkpointId, long timestamp) {
        return Collections.singletonList(new HashSet<String>(this.cache.asMap().keySet()));
    }

    @Override
    public void restoreState(List<HashSet<String>> state) throws Exception {
        setUpCache();
        for (String eventId : state.get(0)) {
            this.cache.put(eventId, true);
        }
    }
}