package cn.digielec.quickpaysdk.retrofit;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;


/**
 * 用于默认的缓存策略,需要权限android.permission.ACCESS_NETWORK_STATE,会判断的请求的Header中是否包含有CusCache:enable，如果包括，则有缓存，否则不缓存
 * 示例，需要缓存 Cus-Cache:enable, online=30, offline=864000
 * 或使用默认时间 Cus-Cache:enable
 * 不需要缓存 不写Header或Cus-Cache:disable
 */
public class DefaultCacheInterceptor implements Interceptor {

    private static final String TAG = "DefaultCacheInterceptor";
    private static final long DEFAULT_ONLINE_TIMEOUT = 60;  //60秒
    private static final long DEFAULT_OFFLINE_TIMEOUT = 60 * 60 * 24 * 28;  //一个月
    public static final String Cus_CACHE = "Cus-Cache";
    private final Context mContext;
    private final long mOnlineTimeout;
    private final long mOfflineTimout;

    public DefaultCacheInterceptor(Context context) {
        this(context, DEFAULT_ONLINE_TIMEOUT, DEFAULT_OFFLINE_TIMEOUT);
    }

    /**
     * @param context
     * @param onlineTimeout  有网络时的缓存有效时间
     * @param offlineTimeout 无网络时的缓存有效时间
     */
    public DefaultCacheInterceptor(final Context context, final long onlineTimeout, final long offlineTimeout) {
        mContext = context.getApplicationContext();
        mOnlineTimeout = onlineTimeout;
        mOfflineTimout = offlineTimeout;
    }

    public static Boolean isNetworkReachable(final Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo current = cm.getActiveNetworkInfo();
        if (current == null) {
            return false;
        }
        return (current.isAvailable());
    }

    private long[] parseCache(final String cache) {
        String[] result = cache.split(",");
        long[] ret = new long[2];
        for (int i = 0; i < result.length; i++) {
            final String text = result[i].trim();
            if (text.startsWith("online")) {
                long index = parseNumber(text);
                ret[0] = index;
            } else if (text.startsWith("offline")) {
                long index = parseNumber(text);
                ret[1] = index;
            }
        }
        if (ret[0] < 0) {
            ret[0] = mOnlineTimeout;
        }
        if (ret[1] < 0) {
            ret[1] = mOfflineTimout;
        }
        return ret;
    }

    private long parseNumber(final String text) {
        String numberText = text.trim();
        int index = numberText.indexOf("=");
        try {
            String number = numberText.substring(index + 1).trim();
            return Long.parseLong(number);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String cusCache = request.header(Cus_CACHE);
        Log.d(TAG, "Cus-cache:" + cusCache);
        //不需要缓存
        if (TextUtils.isEmpty(cusCache) || cusCache.trim().startsWith("disable")) {
            request = request.newBuilder().removeHeader(Cus_CACHE).build();
            return chain.proceed(request);
        }

        //需要缓存，解析字符串

        long[] time = parseCache(cusCache);
        long maxAge = time[0];
        long maxStale = time[1];
        Log.d(TAG, "Cus-cache time:%d" + maxAge + " ," + maxStale);


        Request.Builder builder = request.newBuilder();
        builder.removeHeader(Cus_CACHE);
        if (!isNetworkReachable(mContext)) {
            builder.cacheControl(CacheControl.FORCE_CACHE);
        }
        request = builder.build();
        Response response = chain.proceed(request);
        if (isNetworkReachable(mContext)) {

            // 有网络时
            response.newBuilder()
                    .header("Cache-Control", "public, max-age=" + maxAge)
                    .removeHeader("Pragma")// 清除头信息，因为服务器如果不支持，会返回一些干扰信息，不清除下面无法生效
                    .build();
        } else {
            // 无网络时

            response.newBuilder()
                    .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                    .removeHeader("Pragma")
                    .build();
        }
        return response;
    }
}
