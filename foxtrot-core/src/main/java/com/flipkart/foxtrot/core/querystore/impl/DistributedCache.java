package com.flipkart.foxtrot.core.querystore.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.foxtrot.core.common.ActionResponse;
import com.flipkart.foxtrot.core.common.Cache;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.IMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * User: Santanu Sinha (santanu.sinha@flipkart.com)
 * Date: 25/03/14
 * Time: 7:43 PM
 */
public class DistributedCache implements Cache {
    private static final Logger logger = LoggerFactory.getLogger(DistributedCache.class.getSimpleName());
    private IMap<String, String> distributedMap;
    private ObjectMapper mapper;

    public DistributedCache(HazelcastConnection hazelcastConnection, String name, ObjectMapper mapper) {
        MapConfig mapConfig = hazelcastConnection.getHazelcast().getConfig().getMapConfig(name);
        mapConfig.setInMemoryFormat(InMemoryFormat.BINARY);
        mapConfig.setTimeToLiveSeconds(30);
        mapConfig.setEvictionPolicy(MapConfig.EvictionPolicy.LRU);
        mapConfig.setBackupCount(0);
        this.distributedMap = hazelcastConnection.getHazelcast().getMap(name);
        this.mapper = mapper;
    }

    @Override
    public ActionResponse put(String key, ActionResponse data) {
        try {
            distributedMap.put(key, mapper.writeValueAsString(data));
        } catch (JsonProcessingException e) {
            logger.error("Error saving value to map: ", e);
        }
        return data;
    }

    @Override
    public ActionResponse get(String key) {
        if(null == key) {
            return null; //Hazelcast map throws NPE if key is null
        }
        String data = distributedMap.get(key);
        try {
            return mapper.readValue(data, ActionResponse.class);
        } catch (IOException e) {
            logger.error("Error deserializing: ", e);
        }
        return null;
    }

    @Override
    public boolean has(String key) {
        return null != key && distributedMap.containsKey(key);
    }
}