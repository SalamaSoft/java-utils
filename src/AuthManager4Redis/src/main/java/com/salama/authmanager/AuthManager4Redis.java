package com.salama.authmanager;

import java.io.Closeable;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import MetoXML.XmlSerializer;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

import com.salama.service.clouddata.core.AppAuthUserDataManager;
import com.salama.service.clouddata.core.AppException;
import com.salama.service.clouddata.core.AuthUserInfo;

public class AuthManager4Redis implements AppAuthUserDataManager, Closeable {
	private final static Logger logger = Logger.getLogger(AuthManager4Redis.class);

	private final static String COL_NAME_USER_ID = "userId";
	private final static String COL_NAME_ROLE = "role";
	private final static String COL_NAME_EXPIRING_TIME = "expiringTime";
	
	//private MessageDigest _md5 = null;
	
	//private volatile boolean _isInCleanOldCachedEntry = false;
	private AtomicBoolean _isInCleanOldCachedEntry = new AtomicBoolean(false);
	
	/**
	 * key:authTicket value:AuthUserInfo
	 */
	private ConcurrentHashMap<String, AuthUserInfo> _authTicketMap = new ConcurrentHashMap<String, AuthUserInfo>();
	private LinkedList<String> _authTicketList = new LinkedList<String>();
	
	//private ConcurrentHashMap<String, String> _sessionValueMap = new ConcurrentHashMap<String, String>();
	
	private int _authTicketSeq = 0;
	private Object _lockForAuthTicket = new Object();
    private Random _randomForAuthTicket = new Random(System.currentTimeMillis());
	
	private String _appId = null;
	private String _serverCd = null;

	private int _maxMapCacheCount = 1000000;
	private String _redisHost = null;
	private int _redisPort = 0;

	private JedisPool _jedisPool = null;

	private int _jedisPoolMaxActive = 100;
	private int _jedisPoolMaxIdle = 20;
	private int _jedisPoolMaxWait = 100;
	private int _jedisPoolSoftMinEvictableIdleTimeMillis = 10000;
	private boolean _jedisPoolTestOnBorrow = true;

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
//		try {
//			_md5 = MessageDigest.getInstance("MD5");
//		} catch (NoSuchAlgorithmException e) {
//			throw new RuntimeException(e);
//		}
		
		_serverCd = serverCd;
		_maxMapCacheCount = Integer.parseInt(maxMapCacheCount);
		_redisHost = redisHost;
		_redisPort = Integer.parseInt(redisPort);
		_jedisPoolMaxActive = Integer.parseInt(jedisPoolMaxActive);
		_jedisPoolMaxIdle = Integer.parseInt(jedisPoolMaxIdle);
		_jedisPoolMaxWait = Integer.parseInt(jedisPoolMaxWait);
		_jedisPoolSoftMinEvictableIdleTimeMillis = Integer.parseInt(jedisPoolSoftMinEvictableIdleTimeMillis);

		JedisPoolConfig jedisConfig = new JedisPoolConfig();
		jedisConfig.setMaxIdle(_jedisPoolMaxIdle);
		
		/* old jedis version
		jedisConfig.setMaxActive(_jedisPoolMaxActive);
		jedisConfig.setMaxWait(_jedisPoolMaxWait);
		*/
		jedisConfig.setMaxTotal(_jedisPoolMaxActive);
		jedisConfig.setMaxWaitMillis(_jedisPoolMaxWait);
		
		jedisConfig.setSoftMinEvictableIdleTimeMillis(_jedisPoolSoftMinEvictableIdleTimeMillis);
		jedisConfig.setTestOnBorrow(_jedisPoolTestOnBorrow);

		int iDbNum = 0;
		if(dbNum != null && dbNum.length() > 0) {
			iDbNum = Integer.parseInt(dbNum);
		}
		if(password != null && password.length() == 0) {
			password = null;
		}
		_jedisPool = new JedisPool(
				jedisConfig, _redisHost, _redisPort,
				_jedisPoolMaxWait,
				password,
				iDbNum
				);

		logger.info("JedisPoolConfig ->"
						+ " TestOnBorrow:" + _jedisPoolTestOnBorrow
						+ " MaxIdle:" + _jedisPoolMaxIdle
						+ " MaxTotal:" + _jedisPoolMaxActive
						+ " MaxWaitMillis:" + _jedisPoolMaxWait
						+ " SoftMinEvictableIdleTimeMillis:" + _jedisPoolSoftMinEvictableIdleTimeMillis
				);
	}
	
	@Override
	public void close() throws IOException {
		_jedisPool.destroy();
	}
	
	private String getAuthRedisKey(String authTicket) {
		return ("auth").concat(".").concat(authTicket);
	}
	
	private String createNewTicket(String role, String userId) {
		String seqHex = null;
		
		synchronized (_lockForAuthTicket) {
			if(_authTicketSeq == Integer.MAX_VALUE) {
				_authTicketSeq = 1;
			} else {
				_authTicketSeq ++;
			}
		}
		
		seqHex = toHexString(_authTicketSeq);
		
		long random = _randomForAuthTicket.nextLong();
		String userSign = role.concat(".").concat(userId).concat(".").concat(Long.toString(random));
		
		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		byte[] md5bytes = md5.digest(userSign.getBytes());
		
		return _serverCd.concat(Long.toString(System.currentTimeMillis()))
				.concat(seqHex).concat(toHexString(md5bytes));
		
	}
	
	@Override
	public String allocateAuthTicket(String role, String userId, long expiringTime)
			throws AppException {
		AuthUserInfo authInfo = new AuthUserInfo();
		
		authInfo.setUserId(userId);
		authInfo.setRole(role);
		authInfo.setExpiringTime(expiringTime);
		
		String authTicket = createNewTicket(role, userId);
		saveAuthUserInfoToRedis(authTicket, authInfo);
		saveAuthUserInfoToCacheMap(authTicket, authInfo);
		
		return authTicket;
	}
	
	@Override
	public void backupAllData() throws AppException {
		//do nothing
	}

	@Override
	public void deleteAuthInfo(String authTicket) throws AppException {
		_authTicketMap.remove(authTicket);
		deleteAuthUserInfoFromRedis(authTicket);
	}

	@Override
	public AuthUserInfo getAuthUserInfo(String authTicket) throws AppException {
		AuthUserInfo authInfo = _authTicketMap.get(authTicket);
		
		if(authInfo == null) {
			authInfo = getAuthUserInfoFromRedis(authTicket);
			if(authInfo == null) {
				return null;
			}
			
			saveAuthUserInfoToCacheMap(authTicket, authInfo);
			
			return authInfo;
		} else {
			if(authInfo.getExpiringTime() <= System.currentTimeMillis()) {
				deleteAuthInfo(authTicket);
				return null;
			} else {
				return authInfo;
			}
		}
	}

	@Override
	public String getSessionValue(String authTicket, String key) throws AppException {
		throw new AppException("Not support in this server");
	}

	@Override
	public boolean isAuthTicketValid(String authTicket) throws AppException {
		AuthUserInfo authInfo = getAuthUserInfo(authTicket);
		
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
	public String removeSessionValue(String authTicket, String key)
			throws AppException {
		throw new AppException("Not support in this server");
	}

	@Override
	public void restoreAllData() throws AppException {
	}

	@Override
	public void setAppId(String appId) {
		_appId = appId;
	}

	@Override
	public void setBackupDirPath(String backupDirPath) {
		//do nothing
	}

	@Override
	public String setSessionValue(String authTicket, String key, String value)
			throws AppException {
		throw new AppException("Not support in this server");
	}

	@Override
	public void updateAuthInfo(String authTicket, String role, long expiringTime)
			throws AppException {
	    /*
		AuthUserInfo authInfo = _authTicketMap.get(authTicket);
		
		if(authInfo == null) {
			authInfo = updateAuthUserInfoToRedis(authTicket, role, expiringTime);
			
			if(authInfo == null) {
				return;
			}
			
			saveAuthUserInfoToCacheMap(authTicket, authInfo);
		} else {
			authInfo.setRole(role);
			authInfo.setExpiringTime(expiringTime);
			
			updateAuthUserInfoToRedis(authTicket, role, expiringTime);
		}
		*/
        AuthUserInfo authInfo = updateAuthUserInfoToRedis(authTicket, role, expiringTime);
        if(authInfo == null) {
            return;
        }
        saveAuthUserInfoToCacheMap(authTicket, authInfo);
	}
		
	private void saveAuthUserInfoToCacheMap(String authTicket, AuthUserInfo authInfo) {
		_authTicketMap.put(authTicket, authInfo);
		_authTicketList.add(authTicket);
		
		if(_authTicketList.size() >= _maxMapCacheCount) {
			startCleanOldCachedEntry();
		}
	}
	
	private void startCleanOldCachedEntry() {
		if(_isInCleanOldCachedEntry.get()) {
			return;
		}
		
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				/*
				if(_isInCleanOldCachedEntry) {
					return;
				}
				_isInCleanOldCachedEntry = true;
				*/
				if(!_isInCleanOldCachedEntry.compareAndSet(false, true)) {
					return;
				}
				
				try {
					int deleteCount = _authTicketList.size() - _maxMapCacheCount;
					if(deleteCount > 0) {
						String authTicket = null;
						
						for(int i = 0; i < deleteCount; i++) {
							authTicket = _authTicketList.poll();
							
							if(authTicket == null) {
								break;
							} else {
								_authTicketMap.remove(authTicket);
							}
						}
					}
				} finally {
					//_isInCleanOldCachedEntry = false;
					_isInCleanOldCachedEntry.set(false);
				}
			}
		});
		
		t.start();
	}
	
	private AuthUserInfo updateAuthUserInfoToRedis(String authTicket, String role, long expiringTime) {
		Jedis jedis = null;
		
		try {
			jedis = _jedisPool.getResource();
			
			String authTicketRedisKey = getAuthRedisKey(authTicket);
			
			AuthUserInfo authInfo = new AuthUserInfo();
			
			String userId = jedis.hget(authTicketRedisKey, COL_NAME_USER_ID);
			if(userId == null || userId.equals("nil")) {
				return null;
			}
			
			authInfo.setUserId(userId);
			authInfo.setRole(role);
			authInfo.setExpiringTime(expiringTime);

			jedis.hset(authTicketRedisKey, COL_NAME_ROLE, authInfo.getRole());
			jedis.hset(authTicketRedisKey, COL_NAME_EXPIRING_TIME, Long.toString(authInfo.getExpiringTime()));
			
			int expiredSeconds = (int) ((authInfo.getExpiringTime() - System.currentTimeMillis()) / 1000); 
			jedis.expire(authTicketRedisKey, expiredSeconds);
			
			return authInfo;
		} catch(JedisConnectionException e) {
			_jedisPool.returnBrokenResource(jedis);
			return null;
		} finally {
			_jedisPool.returnResource(jedis);
		}
	}
	
	private void deleteAuthUserInfoFromRedis(String authTicket) {
		Jedis jedis = null;
		
		try {
			jedis = _jedisPool.getResource();
			
			String authTicketRedisKey = getAuthRedisKey(authTicket);
			
			//jedis.hdel(authTicketRedisKey, COL_NAME_USER_ID, COL_NAME_ROLE, COL_NAME_EXPIRING_TIME);
			jedis.del(authTicketRedisKey);
			
		} catch(JedisConnectionException e) {
			_jedisPool.returnBrokenResource(jedis);
		} finally {
			_jedisPool.returnResource(jedis);
		}
	}
	
	private AuthUserInfo getAuthUserInfoFromRedis(String authTicket) {
		Jedis jedis = null;
		
		try {
			jedis = _jedisPool.getResource();
			
			String authTicketRedisKey = getAuthRedisKey(authTicket);
			
			AuthUserInfo authInfo = new AuthUserInfo();
			
			String userId = jedis.hget(authTicketRedisKey, COL_NAME_USER_ID);
			if(userId == null || userId.equals("nil")) {
				return null;
			}
			
			authInfo.setUserId(userId);
			authInfo.setRole(jedis.hget(authTicketRedisKey, COL_NAME_ROLE));
			authInfo.setExpiringTime(Long.parseLong(jedis.hget(authTicketRedisKey, COL_NAME_EXPIRING_TIME)));
			
			return authInfo;
		} catch(JedisConnectionException e) {
			_jedisPool.returnBrokenResource(jedis);
			return null;
		} finally {
			_jedisPool.returnResource(jedis);
		}
	}
	
	private void saveAuthUserInfoToRedis(String authTicket, AuthUserInfo authInfo) {
		Jedis jedis = null;
		
		try {
			jedis = _jedisPool.getResource();
			
			String authTicketRedisKey = getAuthRedisKey(authTicket);
			
			jedis.hset(authTicketRedisKey, COL_NAME_USER_ID, authInfo.getUserId());
			jedis.hset(authTicketRedisKey, COL_NAME_ROLE, authInfo.getRole());
			jedis.hset(authTicketRedisKey, COL_NAME_EXPIRING_TIME, Long.toString(authInfo.getExpiringTime()));
			
			int expiredSeconds = (int) ((authInfo.getExpiringTime() - System.currentTimeMillis()) / 1000); 
			jedis.expire(authTicketRedisKey, expiredSeconds);
		} catch(JedisConnectionException e) {
			_jedisPool.returnBrokenResource(jedis);
		} finally {
			_jedisPool.returnResource(jedis);
		}
	}

	

	private static String toHexString(int val) {
		StringBuilder hexStr = new StringBuilder(Integer.toHexString(val));

		for(int i = hexStr.length(); i < 8; i++) {
			hexStr.insert(0, '0');
		}
		
		return hexStr.toString();
	}
	
	private static String toHexString(byte[] val) {
		return toHexString(val, 0, val.length);
	}

	private static String toHexString(long val) {
		StringBuilder hexStr = new StringBuilder(Long.toHexString(val));

		for(int i = hexStr.length(); i < 16; i++) {
			hexStr.insert(0, '0');
		}
		
		return hexStr.toString();
	}
	
	private static String toHexString(byte[] val, int offset, int length) {
		long lVal = 0;
		int cnt = length / 8;
		int startIndex = offset;
		StringBuilder hexStr = new StringBuilder();
		
		for(int i = 0; i < cnt; i++) {
			
			lVal = 
				((((long)val[startIndex]) << 56) & 0xFF00000000000000L) + 
				((((long)val[startIndex + 1]) << 48) & 0x00FF000000000000L) +
				((((long)val[startIndex + 2]) << 40) & 0x0000FF0000000000L) +
				((((long)val[startIndex + 3]) << 32) & 0x000000FF00000000L) +
				((((long)val[startIndex + 4]) << 24) & 0x00000000FF000000L) +
				((((long)val[startIndex + 5]) << 16) & 0x0000000000FF0000L) +
				((((long)val[startIndex + 6]) << 8) &  0x000000000000FF00L) +
				((((long)val[startIndex + 7]) ) & 0x00000000000000FFL) ;
			hexStr.append(toHexString(lVal));
			
			startIndex += 8;
		}
		
		for(; startIndex < length; startIndex++) {
			hexStr.append(toHexString(val[startIndex]));
		}
		
		return hexStr.toString();
	}
	
}
