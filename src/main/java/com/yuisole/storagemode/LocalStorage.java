package com.yuisole.storagemode;

import com.yuisole.exception.RateLimitExceededException;
import com.yuisole.storagebase.Storage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Yuisole
 */
public class LocalStorage implements Storage {


    private final Map<String, Long> coolDown = new ConcurrentHashMap<>();

    private final Map<String, Map<String,Long>> imageCodeStorage = new ConcurrentHashMap<>();

    private final Map<String, Map<Long, Integer>> storage = new HashMap<>();


    private static volatile LocalStorage localStorage;

    public static LocalStorage getInstance(){
        if(localStorage == null){
            synchronized (LocalStorage.class){
                if(localStorage == null){
                    localStorage = new LocalStorage();
                }
            }
        }
        return localStorage;
    }

    @Override
    public Integer getRequestCount(String key) {
        Map<Long, Integer> requests = storage.getOrDefault(key, new HashMap<>());
        return requests.values().stream().mapToInt(Integer::intValue).sum();
    }

    @Override
    public void removeRequestCount(String key) {
        Map<Long, Integer> map = storage.getOrDefault(key, new HashMap<>());
        map.clear();
        storage.put(key ,map);
    }

    @Override
    public void setCooldown(String key, long cooldown) {
        coolDown.put(key, System.currentTimeMillis() + cooldown * 1000);
        if(coolDown.size() >60){
            cleanUpOldCoolKeys();
        }
    }

    @Override
    public long getCooldown(String key) {
        return coolDown.getOrDefault(key, 0L);
    }

    @Override
    public void filterRequestsBefore(String key, long timestamp) {
        Map<Long, Integer> requests = storage.getOrDefault(key, new HashMap<>());
        requests.entrySet().removeIf(entry -> entry.getKey() < timestamp);
        storage.put(key, requests);
    }

    @Override
    public void incrementRequestCount(String key) {
        long currentTime = System.currentTimeMillis();
        long currentWindow = currentTime / 1000;
        Map<Long, Integer> requests = storage.getOrDefault(key, new HashMap<>());
        requests.put(currentWindow, requests.getOrDefault(currentWindow, 0) + 1);
        storage.put(key, requests);
    }

    @Override
    public void saveImageCode(String key, String text,long timestamp) {
        Map<String,Long> time =  new HashMap<>();
        time.put(text,System.currentTimeMillis()+timestamp*1000);
        imageCodeStorage.put(key,time);
    }

    @Override
    public boolean validateCaptcha(String key,String uuid,String text) {
        String newKey = key + ":" + uuid;
        Map<String, Long> map = imageCodeStorage.get(newKey);
        if(map == null){
            throw new RateLimitExceededException("no such uuid, please put right uuid!");
        }
        Long aLong = map.get(text);
        boolean b = aLong != null;
        if(b){
            coolDown.remove(key);
            imageCodeStorage.remove(newKey);
        }
        long currentTime = System.currentTimeMillis();

        Iterator<Map.Entry<String, Map<String, Long>>> iterator = imageCodeStorage.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Map<String, Long>> entry = iterator.next();
            Map<String, Long> value = entry.getValue();
            value.entrySet().removeIf(v -> v.getValue() < currentTime);

            if (value.isEmpty()) {
                iterator.remove();
            }
        }

        return b;
    }

    private void cleanUpOldCoolKeys() {
        long currentTime = System.currentTimeMillis();
        coolDown.entrySet().removeIf(entry -> entry.getValue() < currentTime);
    }
}
