package com.salama.authmanager;

import com.salama.service.clouddata.core.AppAuthUserDataManager;
import com.salama.service.clouddata.core.AppException;
import com.salama.service.clouddata.core.AuthUserInfo;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.Closeable;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class AuthManager4Redis implements AppAuthUserDataManager, Closeable {
    private final static Logger logger = Logger.getLogger(AuthManager4Redis.class);

    private final static String COL_NAME_USER_ID = "userId";
    private final static String COL_NAME_ROLE = "role";
    private final static String COL_NAME_EXPIRING_TIME = "expiringTime";

    //private ConcurrentHashMap<String, AuthUserInfo> _authTicketMap = new ConcurrentHashMap<String, AuthUserInfo>();
    private AtomicInteger _authTicketSeq = new AtomicInteger(0);
    private Random _randomForAuthTicket = new Random(System.currentTimeMillis());
    private LRUCache<String, AuthUserInfo> _cache;

    ///////////////////////////////////////////////////////////////////////////////////
    private long _defaultCacheLifetimeMS = 60 * 1000;
    private int _cacheSizeMax = 100 * 10000;

    private String _appId;

//    private String _host;
//    private int _port;
    private int _timeout = 2000;
//    private String _password = null;
//    private int _database = 0;
//    private String _clientName = null;
//    private JedisPoolConfig _poolConfig;

    private boolean _jedisPoolTestOnBorrow = true;

    private JedisPool _jedisPool;

    public AuthManager4Redis(
            String serverCd,
            String maxMapCacheCount,
            String redisHost,
            String redisPort,
            String jedisPoolMaxActive,
            String jedisPoolMaxIdle,
            String jedisPoolMaxWait,
            String jedisPoolSoftMinEvictableIdleTimeMillis
    ) {
        this(
                serverCd, maxMapCacheCount,
                redisHost, redisPort,
                jedisPoolMaxActive, jedisPoolMaxIdle, jedisPoolMaxWait,
                jedisPoolSoftMinEvictableIdleTimeMillis,
                null,
                null
        );
    }

    public AuthManager4Redis(
            String serverCd,
            String maxMapCacheCount,
            String redisHost,
            String redisPort,
            String jedisPoolMaxActive,
            String jedisPoolMaxIdle,
            String jedisPoolMaxWait,
            String jedisPoolSoftMinEvictableIdleTimeMillis,
            String password
    ) {
        this(
                serverCd, maxMapCacheCount,
                redisHost, redisPort,
                jedisPoolMaxActive, jedisPoolMaxIdle, jedisPoolMaxWait,
                jedisPoolSoftMinEvictableIdleTimeMillis,
                password,
                null
        );
    }

    public AuthManager4Redis(
            String serverCd,
            String maxMapCacheCount,
            String redisHost,
            String redisPort,
            String jedisPoolMaxActive,
            String jedisPoolMaxIdle,
            String jedisPoolMaxWait,
            String jedisPoolSoftMinEvictableIdleTimeMillis,
            String password,
            String dbNum
    ) {
        _appId = serverCd;
        _cacheSizeMax = Integer.parseInt(maxMapCacheCount);

        LRUCache.CachePolicy cachePolicy = new LRUCache.CachePolicy();
        cachePolicy.setCacheSizeMax(_cacheSizeMax);
        cachePolicy.setDefaultCacheLifetimeMS(_defaultCacheLifetimeMS);
        _cache = new LRUCache<String, AuthUserInfo>(cachePolicy);

        {
            JedisPoolConfig jedisConfig = new JedisPoolConfig();
            jedisConfig.setMaxIdle(Integer.parseInt(jedisPoolMaxIdle));

            /* old jedis version
            jedisConfig.setMaxActive(_jedisPoolMaxActive);
            jedisConfig.setMaxWait(_jedisPoolMaxWait);
            */
            jedisConfig.setMaxTotal(Integer.parseInt(jedisPoolMaxActive));
            jedisConfig.setMaxWaitMillis(Long.parseLong(jedisPoolMaxWait));

            jedisConfig.setSoftMinEvictableIdleTimeMillis(Long.parseLong(jedisPoolSoftMinEvictableIdleTimeMillis));
            jedisConfig.setTestOnBorrow(_jedisPoolTestOnBorrow);

            int iDbNum = 0;
            if(dbNum != null && dbNum.length() > 0) {
                iDbNum = Integer.parseInt(dbNum);
            }
            if(password != null && password.length() == 0) {
                password = null;
            }
            _jedisPool = new JedisPool(
                    jedisConfig, redisHost, Integer.parseInt(redisPort),
                    _timeout,
                    password,
                    iDbNum
            );

            logger.info("JedisPoolConfig ->"
                    + " TestOnBorrow:" + _jedisPoolTestOnBorrow
                    + " MaxIdle:" + jedisPoolMaxIdle
                    + " MaxTotal:" + jedisPoolMaxActive
                    + " MaxWaitMillis:" + jedisPoolMaxWait
                    + " SoftMinEvictableIdleTimeMillis:" + jedisPoolSoftMinEvictableIdleTimeMillis
            );        }
    }

    @Override
    public void close() throws IOException {
        try {
            _jedisPool.destroy();
        } catch (Throwable e) {
            logger.error(null, e);
        }

        try {
            _cache.clear();
        } catch (Throwable e) {
            logger.error(null, e);
        }
    }

    @Override
    public void setAppId(String appId) {
        _appId = appId;
    }

    @Override
    public String allocateAuthTicket(String role, String userId, long expiringTime) throws AppException {
        AuthUserInfo authInfo = new AuthUserInfo();

        if(role == null) {
            role = "";
        }
        authInfo.setUserId(userId);
        authInfo.setRole(role);
        authInfo.setExpiringTime(expiringTime);

        //create authTicket
        String authTicket = createNewTicket(role, userId);

        //save
        saveAuthUserInfoToRedis(authTicket, authInfo);
        saveAuthUserInfoToCacheMap(authTicket, authInfo);

        return authTicket;
    }

    @Override
    public boolean isAuthTicketValid(String authTicket) throws AppException {
        final AuthUserInfo authInfo = getAuthUserInfo(authTicket);

        if(authInfo == null) {
            return false;
        } else {
            if(authInfo.getExpiringTime() <= System.currentTimeMillis()) {
                return false;
            } else {
                return true;
            }
        }
    }

    @Override
    public AuthUserInfo getAuthUserInfo(final String authTicket) throws AppException {
        try {
            final AuthUserInfo authInfo = getAuthUserInfoFromCache(authTicket);
            if(authInfo == null) {
                return null;
            }

            //if expire
            if(authInfo.getExpiringTime() <= System.currentTimeMillis()) {
                deleteAuthInfo(authTicket);
                return null;
            } else {
                return authInfo;
            }
        } catch (Throwable e) {
            logger.error("", e);
            return null;
        }
    }

    @Override
    public void backupAllData() throws AppException {
        //do nothing
    }

    @Override
    public void restoreAllData() throws AppException {
    }

    @Override
    public void setBackupDirPath(String backupDirPath) {
        //do nothing
    }

    @Override
    public String removeSessionValue(String authTicket, String key)
            throws AppException {
        throw new AppException("Not support in this server");
    }

    @Override
    public String getSessionValue(String authTicket, String key) throws AppException {
        throw new AppException("Not support in this server");
    }

    @Override
    public String setSessionValue(String authTicket, String key, String value)
            throws AppException {
        throw new AppException("Not support in this server");
    }

    @Override
    public void updateAuthInfo(String authTicket, String role, long expiringTime) throws AppException {
        AuthUserInfo authInfo = updateAuthUserInfoToRedis(authTicket, role, expiringTime);
        if(authInfo == null) {
            return;
        }

        saveAuthUserInfoToCacheMap(authTicket, authInfo);
    }

    @Override
    public void deleteAuthInfo(String authTicket) throws AppException {
        _cache.delete(authTicket);
        deleteAuthUserInfoFromRedis(authTicket);
    }

    private AuthUserInfo getAuthUserInfoFromCache(final String authTicket) throws Exception {
        return _cache.computeIfAbsent(authTicket, new LRUCache.ComputeFunction<String, AuthUserInfo>() {
            @Override
            public AuthUserInfo call(String key) throws Exception {
                AuthUserInfo authInfo = getAuthUserInfoFromRedis(authTicket);
                if(authInfo != null) {
                    saveAuthUserInfoToCacheMap(authTicket, authInfo);
                }

                return authInfo;
            }
        });
    }

    private String getAuthRedisKey(String authTicket) {
        return ("auth").concat(".").concat(authTicket);
    }

    private String createNewTicket(String role, String userId) {
        final String seqHex = HexUtil.toHexString(_authTicketSeq.incrementAndGet());
        long random = _randomForAuthTicket.nextLong();
        String userSign = role.concat(".").concat(userId).concat(".").concat(Long.toString(random));

        final MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] md5bytes = md5.digest(userSign.getBytes());

        return _appId
                .concat(HexUtil.toHexString(System.currentTimeMillis()))
                .concat(seqHex)
                .concat(HexUtil.toHexString(md5bytes))
                ;

    }

    private void saveAuthUserInfoToCacheMap(String authTicket, AuthUserInfo authInfo) {
        _cache.put(authTicket, authInfo);
    }

    private void saveAuthUserInfoToRedis(String authTicket, AuthUserInfo authInfo) {
        Jedis jedis = _jedisPool.getResource();

        try {
            //fields
            String authTicketRedisKey = getAuthRedisKey(authTicket);
            Map<String, String> map = new HashMap<String, String>(3);
            map.put(COL_NAME_USER_ID, authInfo.getUserId());
            map.put(COL_NAME_ROLE, authInfo.getRole());
            map.put(COL_NAME_EXPIRING_TIME, Long.toString(authInfo.getExpiringTime()));

            jedis.hmset(authTicketRedisKey, map);

            //expire time
            int expiredSeconds = (int) ((authInfo.getExpiringTime() - System.currentTimeMillis()) / 1000);
            jedis.expire(authTicketRedisKey, expiredSeconds);
        } finally {
            jedis.close();
        }
    }

    private AuthUserInfo getAuthUserInfoFromRedis(String authTicket) {
        Jedis jedis = _jedisPool.getResource();
        try {
            //get from redis
            String authTicketRedisKey = getAuthRedisKey(authTicket);
            final Map<String, String> fieldMap = jedis.hgetAll(authTicketRedisKey);

            //userId required
            String userId = fieldMap.get(COL_NAME_USER_ID);
            if(userId == null || userId.equals("nil")) {
                return null;
            }

            //result
            AuthUserInfo authInfo = new AuthUserInfo();
            authInfo.setUserId(userId);
            authInfo.setRole(fieldMap.get(COL_NAME_ROLE));
            authInfo.setExpiringTime(Long.parseLong(fieldMap.get(COL_NAME_EXPIRING_TIME)));

            return authInfo;
        } finally {
            jedis.close();
        }
    }

    private AuthUserInfo updateAuthUserInfoToRedis(String authTicket, String role, long expiringTime) {
        Jedis jedis = _jedisPool.getResource();
        try {
            final String authTicketRedisKey = getAuthRedisKey(authTicket);
            String userId = jedis.hget(authTicketRedisKey, COL_NAME_USER_ID);
            if(userId == null || userId.equals("nil")) {
                return null;
            }

            AuthUserInfo authInfo = new AuthUserInfo();
            authInfo.setUserId(userId);
            authInfo.setRole(role);
            authInfo.setExpiringTime(expiringTime);

            //set fields
            Map<String, String> map = new HashMap<String, String>(2);
            map.put(COL_NAME_ROLE, authInfo.getRole());
            map.put(COL_NAME_EXPIRING_TIME, Long.toString(authInfo.getExpiringTime()));

            jedis.hmset(authTicketRedisKey, map);

            //expire
            int expiredSeconds = (int) ((authInfo.getExpiringTime() - System.currentTimeMillis()) / 1000);
            jedis.expire(authTicketRedisKey, expiredSeconds);

            return authInfo;
        } finally {
            jedis.close();
        }
    }

    private void deleteAuthUserInfoFromRedis(String authTicket) {
        Jedis jedis = _jedisPool.getResource();
        try {
            String authTicketRedisKey = getAuthRedisKey(authTicket);
            jedis.del(authTicketRedisKey);
        } finally {
            jedis.close();
        }
    }

    private static class HexUtil {
        private final static String[] ByteHexArray = new String[] {
                "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "0a", "0b", "0c", "0d", "0e", "0f",
                "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "1a", "1b", "1c", "1d", "1e", "1f",
                "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "2a", "2b", "2c", "2d", "2e", "2f",
                "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "3a", "3b", "3c", "3d", "3e", "3f",
                "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "4a", "4b", "4c", "4d", "4e", "4f",
                "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "5a", "5b", "5c", "5d", "5e", "5f",
                "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "6a", "6b", "6c", "6d", "6e", "6f",
                "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "7a", "7b", "7c", "7d", "7e", "7f",
                "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "8a", "8b", "8c", "8d", "8e", "8f",
                "90", "91", "92", "93", "94", "95", "96", "97", "98", "99", "9a", "9b", "9c", "9d", "9e", "9f",
                "a0", "a1", "a2", "a3", "a4", "a5", "a6", "a7", "a8", "a9", "aa", "ab", "ac", "ad", "ae", "af",
                "b0", "b1", "b2", "b3", "b4", "b5", "b6", "b7", "b8", "b9", "ba", "bb", "bc", "bd", "be", "bf",
                "c0", "c1", "c2", "c3", "c4", "c5", "c6", "c7", "c8", "c9", "ca", "cb", "cc", "cd", "ce", "cf",
                "d0", "d1", "d2", "d3", "d4", "d5", "d6", "d7", "d8", "d9", "da", "db", "dc", "dd", "de", "df",
                "e0", "e1", "e2", "e3", "e4", "e5", "e6", "e7", "e8", "e9", "ea", "eb", "ec", "ed", "ee", "ef",
                "f0", "f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8", "f9", "fa", "fb", "fc", "fd", "fe", "ff"
        };

        private final static String[] Zeros = {
                "",
                "0",
                "00",
                "000",
                "0000",
                "00000",
                "000000",
                "0000000",
                "00000000",
                "000000000",
                "0000000000",
                "00000000000",
                "000000000000",
                "0000000000000",
                "00000000000000",
                "000000000000000",
        };


        public static String toHexString(byte val) {
            int index = (val & 0xFF);
            return ByteHexArray[index];
        }

        public static String toHexString(byte[] val) {
            return toHexString(val, 0, val.length);
        }

        public static String toHexString(byte[] val, int offset, int length) {
            StringBuilder str = new StringBuilder();

            int end = offset + length;
            for(int i = offset; i < end; i++) {
                str.append(toHexString(val[i]));
            }

            return str.toString();
        }

        public static String toHexString(int val) {
            String hex = Integer.toHexString(val);
            return Zeros[8 - hex.length()].concat(hex);
        }

        public static String toHexString(long val) {
            String hex = Long.toHexString(val);
            return Zeros[16 - hex.length()].concat(hex);
        }

    }
}
