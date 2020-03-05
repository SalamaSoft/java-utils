package com.salama.authmanager;

import java.util.Random;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author XingGu_Liu on 2020/3/4.
 */
class LRUCache<K, V> {
    static {
        System.out.println(LRUCache.class.getName() + " VERSION: " + "1.2.0(20180830)");
    }

    //@FunctionalInterface
    public interface ComputeFunction<K, V> {
        V call(K key) throws Exception;
    }

    public static class CachePolicy {
        private long _defaultCacheLifetimeMS;
        private int _cacheSizeMax;

        public CachePolicy() {
        }

        public CachePolicy(long defaultCacheLifetimeMS, int cacheSizeMax) {
            _defaultCacheLifetimeMS = defaultCacheLifetimeMS;
            _cacheSizeMax = cacheSizeMax;
        }

        public long getDefaultCacheLifetimeMS() {
            return _defaultCacheLifetimeMS;
        }

        public void setDefaultCacheLifetimeMS(long defaultCacheLifetimeMS) {
            _defaultCacheLifetimeMS = defaultCacheLifetimeMS;
        }

        public int getCacheSizeMax() {
            return _cacheSizeMax;
        }
        public void setCacheSizeMax(int cacheSizeMax) {
            _cacheSizeMax = cacheSizeMax;
        }

    }

    private final long _defaultCacheLifetimeMS;
    private final int _cacheSizeMax;
    private final int _cacheSizeMaxThreshold;
    private final int _cacheSizeOldThreshold;

    private final ConcurrentSkipListMap<KeyRef<K>, ValueRef<V>> _cacheMap = new ConcurrentSkipListMap<KeyRef<K>, ValueRef<V>>();
    //deprecated
    private final boolean _enableLock;
    private final Random _rand = new Random(System.currentTimeMillis());

    private final AtomicInteger _cacheSize = new AtomicInteger(0);

    public LRUCache(CachePolicy cachePolicy) {
        this(cachePolicy, false);
    }

    public LRUCache(CachePolicy cachePolicy, boolean enableLock) {
        _defaultCacheLifetimeMS = cachePolicy.getDefaultCacheLifetimeMS();
        _cacheSizeMax = cachePolicy.getCacheSizeMax();

        _enableLock = enableLock;

        _cacheSizeMaxThreshold = (int) (_cacheSizeMax * 1.2);
        _cacheSizeOldThreshold = (int) (_cacheSizeMax * 0.8);
    }

    public void clear() {
        synchronized(this) {
            _cacheMap.clear();
            _cacheSize.set(0);
        }
    }

    public V computeIfAbsent(K key, ComputeFunction<K, V> func) throws InterruptedException {
        return computeIfAbsent(key, func, _defaultCacheLifetimeMS);
    }

    public V get(final K key) {
        KeyRef<K> keyRef = new KeyRef<K>(key, System.currentTimeMillis());
        ValueRef<V> valRef = _cacheMap.get(keyRef);

        if(valRef == null) {
            return null;
        } else {
            return valRef._val;
        }
    }

    public boolean isCacheNullOrExpired(final K key) {
        KeyRef<K> keyRef = new KeyRef<K>(key, System.currentTimeMillis());
        ValueRef<V> valRef = _cacheMap.get(keyRef);

        if(valRef == null) {
            return true;
        } else {
            return valRef.isCacheExpired();
        }
    }

    public V put(final K key, final V v) {
        return put(key, v, _defaultCacheLifetimeMS);
    }


    public V put(final K key, final V v, long cacheLifetimeMS) {
        KeyRef<K> keyRef = new KeyRef<K>(key, System.currentTimeMillis());
        //ValueRef<V> valRefNew = new ValueRef<V>(v, System.currentTimeMillis(), cacheLifetimeMS);
        //ValueRef<V> valueRef = _cacheMap.put(keyRef, valRefNew);

        ValueRef<V> valueRef = _cacheMap.get(keyRef);

        final V oldVal;
        if(valueRef == null) {
            oldVal = null;

            valueRef = new ValueRef<V>(v, System.currentTimeMillis(), cacheLifetimeMS);
        } else {
            oldVal = valueRef._val;

            valueRef._createTime = System.currentTimeMillis();
            valueRef._cacheLifetimeMS = cacheLifetimeMS;
            valueRef._val = v;
        }

        _cacheMap.put(keyRef, valueRef);

        return oldVal;
    }

    public V delete(final K key) {
        KeyRef<K> keyRef = new KeyRef<K>(key, System.currentTimeMillis());
        ValueRef<V> valueRef = _cacheMap.remove(keyRef);
        if(valueRef == null) {
            return null;
        } else {
            return valueRef._val;
        }
    }

    /**
     * Return the value in cache if key exists. Computing and return new Value if key does not exist.
     * @param key
     * @param func
     * @return
     * @throws Exception
     */
    public V computeIfAbsent(final K key, final ComputeFunction<K, V> func, long cacheLifetimeMS) throws InterruptedException {
        KeyRef<K> keyRef = new KeyRef<K>(key, System.currentTimeMillis());

        ValueRef<V> valRef = _cacheMap.get(keyRef);
        if(isNeedCompute(valRef) == 0) {
            return valRef._val;
        }

        boolean isNewRef = false;
        if(valRef == null) {
            synchronized (_cacheMap) {
                valRef = _cacheMap.get(keyRef);
                if(valRef == null) {
                    //make it expired to prevent other threads from using it
                    valRef = new ValueRef<V>(null, 0, cacheLifetimeMS);
                    _cacheMap.put(keyRef, valRef);

                    isNewRef = true;
                }
            }
        }

        final V v;
        synchronized (valRef) {
            if(isNeedCompute(valRef) == 0) {
                return valRef._val;
            }

            try {
                v = func.call(key);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            valRef._createTime = System.currentTimeMillis();
            valRef._val = v;
            valRef._cacheLifetimeMS = cacheLifetimeMS;
        }

        if(isNewRef) {
            checkCacheSizeWhenAddNew();
        } else {
            //checkCacheLifetime();
        }

        return v;
    }

    /**
     *
     * @param valRef
     * @return 0:no need computing         1:need computing(not exist)      2:need computing(lifetime is over)
     */
    private int isNeedCompute(ValueRef<V> valRef) {
        if(valRef != null) {
            //lifetime
            if(valRef.isCacheExpired()) {
                //lifetime is over
                return 2;
            } else {
                //no need compute
                return 0;
            }
        } else {
            //not exists
            return 1;
        }
    }

    private void checkCacheSizeWhenAddNew() {
        if(_cacheSize.get() > _cacheSizeMax) {
            _cacheSize.incrementAndGet();

            long beginTime = System.currentTimeMillis();
            int cleanCount = 0;
            //pop the oldest ones
            final int cacheSizeDownTo = (int) (_cacheSizeMax * 0.9f);
            while(_cacheSize.get() > cacheSizeDownTo) {
                if(_cacheMap.pollFirstEntry() == null) {
                    break;
                }

                _cacheSize.decrementAndGet();
                cleanCount ++;
            }

            System.out.println(LRUCache.class.getName() + " checkCacheSizeWhenAddNew() -> clean old ones."
                    + " cost(ms):" + (System.currentTimeMillis() - beginTime)
                    + " cleanCount:" + cleanCount
                    + " _cacheSize:" + _cacheSize.get()
            );
        } else {
            _cacheSize.incrementAndGet();
        }
    }

    private static class KeyRef<K> implements Comparable<K> {
        private K _key;
        private long _accessTime;

        public KeyRef(K key, long accessTime) {
            _key = key;
            _accessTime = accessTime;
        }

        @Override
        public int hashCode() {
            return _key.hashCode();
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean equals(Object obj) {
            return _key.equals(((KeyRef<K>)obj)._key);
        }

        @SuppressWarnings("unchecked")
        @Override
        public int compareTo(K o) {
            //ascending (the minimal accessTime is the the eldest)
            if(this._key.equals(((KeyRef<K>)o)._key)) {
                return 0;
            } else {
                long diff = this._accessTime - ((KeyRef<K>)o)._accessTime;
                if(diff > 0) {
                    return 1;
                } else {
                    return -1;
                }
            }
        }

    }

    private static class ValueRef<V> {
        private V _val;
        private long _createTime;
        private long _cacheLifetimeMS;

        public ValueRef(V val, long createTime, long cacheLifetimeMS) {
            _val = val;
            _createTime = createTime;
            _cacheLifetimeMS = cacheLifetimeMS;
        }

        public boolean isCacheExpired() {
            return (System.currentTimeMillis() - _createTime) > _cacheLifetimeMS;
        }
    }


}
