package cn.digielec.quickpaysdk.retrofit;

import android.content.Context;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import java.util.concurrent.TimeUnit;

import cn.digielec.quickpaysdk.retrofit.convertors.StringConverterFactory;
import cn.digielec.quickpaysdk.retrofit.convertors.ToStringConverterFactory;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * http://square.github.io/retrofit/
 * https://github.com/mutsinghua/Retrofit2.0FullExample
 * Created by dw on 2016/3/31.
 */
public class RetrofitHttpUtil {

    public static final String TAG = RetrofitHttpUtil.class.getSimpleName();

    public static final int CACHE_SIZE = 4 * 1024 * 1024; //cache size
    public static final int NETWORK_TIME_OUT = 20; //network time out

    private static OkHttpClient sDefaultHttpClient;

    private Context mContext;

    private Retrofit mRetrofit;

    private OkHttpClient mOKHttpClient;

    private String mServerUrl; //server url

    private boolean mEnableLog;

    private HttpLoggingInterceptor.Level mLogLevel;

    public RetrofitHttpUtil(Context context) {
        mContext = context.getApplicationContext();
    }

    public RetrofitHttpUtil(Context context, String serverUrl) {
        mContext = context.getApplicationContext();
        mServerUrl = serverUrl;
    }

    public RetrofitHttpUtil(Context context, String serverUrl, OkHttpClient okHttpClient) {
        mContext = context.getApplicationContext();
        mServerUrl = serverUrl;
        mOKHttpClient = okHttpClient;
    }

    public Retrofit getRetrofit() {
        if (mRetrofit == null) {
            synchronized (RetrofitHttpUtil.class) {
                if (mRetrofit == null) {
                    mRetrofit = initDefault();
                }
            }
        }
        return mRetrofit;
    }

    /**
     * Set your own component, normally you don't need to set intently unless default function can not reach your requirement.
     *
     * @param retrofit
     */
    public void setRetrofit(Retrofit retrofit) {
        if (retrofit != null) {
            mRetrofit = retrofit;
            mServerUrl = mRetrofit.baseUrl().toString();
        }
    }

    /**
     * 设置日志级别
     *
     * @param enable
     * @param logLevel
     */
    public void setLogLevel(boolean enable, HttpLoggingInterceptor.Level logLevel) {
        mEnableLog = enable;
        mLogLevel = logLevel;
    }


    private Retrofit initDefault() {
        Retrofit.Builder builder = new Retrofit.Builder();
        if (mOKHttpClient == null) {
            OkHttpClient okHttpClient = buildDefalutClient(mContext);
            if (mEnableLog) {
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(mLogLevel);
                okHttpClient.interceptors().add(logging);
                mOKHttpClient = okHttpClient;
            } else {
                mOKHttpClient = getDefaultOkHttpClient(okHttpClient);
            }

        }
        builder.client(mOKHttpClient);
        builder.addConverterFactory(new StringConverterFactory());
        builder.addConverterFactory(new ToStringConverterFactory());
        builder.addConverterFactory(GsonConverterFactory.create());
        builder.baseUrl(mServerUrl);
        return builder.build();
    }

    /**
     * Generate a default OKHttpClient with default parameter.
     * If you have special requirements, please create by yourself.
     *
     * @return
     */
    public static synchronized OkHttpClient getDefaultOkHttpClient(OkHttpClient okHttpClient) {
        if (sDefaultHttpClient == null) {
            sDefaultHttpClient = okHttpClient;
        }
        return sDefaultHttpClient;
    }

    private static OkHttpClient buildDefalutClient(Context context) {
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setCache(new Cache(context.getCacheDir(), CACHE_SIZE));
        okHttpClient.interceptors().add(new DefaultCacheInterceptor(context));
        okHttpClient.setConnectTimeout(NETWORK_TIME_OUT, TimeUnit.SECONDS);
        okHttpClient.setReadTimeout(NETWORK_TIME_OUT, TimeUnit.SECONDS);
        okHttpClient.setWriteTimeout(NETWORK_TIME_OUT, TimeUnit.SECONDS);
        return okHttpClient;
    }
}
