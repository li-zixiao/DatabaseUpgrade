package com.huangzj.databaseupgrade.dao.ormlite;

import android.support.v4.util.LruCache;
import android.text.TextUtils;

import com.huangzj.databaseupgrade.util.JSONUtil;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by huangzj on 2016/3/2.
 * <p/>
 * 数据库查询后的缓存数据
 */
public class DbCache {

    /**
     * 最大缓存的查询记录条数
     */
    private static final int CACHE_SIZE = 100;

    /**
     * 所有缓存的总集，以表名为key，value为对应数据表下，所有查询条件的数据总集，每条查询对应的数据以json格式保存
     */
    private static LruCache<String, Map<String, String>> mLruCache;

    private static void initCache() {
        Timber.i("--------init cache----");
        mLruCache = new LruCache<String, Map<String, String>>(CACHE_SIZE) {
            @Override
            protected int sizeOf(String key, Map<String, String> value) {
                return value.size();
            }
        };
    }

    public static <T> void addCache(String tableName, String query, T value) {
        if (mLruCache == null) {
            initCache();
        }
        if (inValid(tableName) || inValid(query) || value == null) {
            Timber.i("--------value is null----");
            return;
        }
        Map<String, String> tableCache = mLruCache.get(tableName);
        if (tableCache == null) {
            Timber.i("--------add cache");
            tableCache = new HashMap<>();
            tableCache.put(query, JSONUtil.toJSON(value));
            mLruCache.put(tableName, tableCache);
        } else {
            Timber.i("--------update cache");
            tableCache.put(query, JSONUtil.toJSON(value));
        }
    }

    public static String getCache(String tableName, String query) {
        if (inValid(tableName) || inValid(query)) {
            Timber.i("--------mLruCache is empty");
            return null;
        }

        Map<String, String> tableCache = mLruCache.get(tableName);
        if (tableCache == null || tableCache.isEmpty()) {
            Timber.i("----------------tableCache is empty---");
            return null;
        }
        return tableCache.get(query);
    }

    public static void clearByQuery(String tableName, String query) {
        if (inValid(tableName) || inValid(query)) {
            return;
        }
        Map<String, String> tableCache = mLruCache.get(tableName);
        if (tableCache != null) {
            tableCache.remove(query);
        }
    }

    public static void clearByTable(String tableName) {
        if (inValid(tableName)) {
            return;
        }
        Timber.i("--------clear table cache---");
        mLruCache.remove(tableName);
    }

    public void clearAll() {
        if (mLruCache != null) {
            if (mLruCache.size() > 0) {
                mLruCache.evictAll();
            }
        }
        System.gc();
    }

    private static boolean inValid(String key) {
        if (mLruCache == null) {
            return true;
        }
        if (TextUtils.isEmpty(key)) {
            return true;
        }
        return false;
    }

}
