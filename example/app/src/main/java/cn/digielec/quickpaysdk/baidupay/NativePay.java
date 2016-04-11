package cn.digielec.quickpaysdk.baidupay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.android.pay.InitCallBack;
import com.baidu.android.pay.PayCallBack;
import com.baidu.wallet.api.BaiduWallet;
import com.baidu.wallet.api.Constants;
import com.baidu.wallet.core.DebugConfig;
import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import cn.digielec.quickpaysdk.R;
import cn.digielec.quickpaysdk.retrofit.ApiService;
import cn.digielec.quickpaysdk.retrofit.RetrofitHttpUtil;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * 订单支付示例页面
 *
 * @author xionglei01s
 * @since 2015-11-30
 */
public class NativePay extends Activity implements OnClickListener, Callback<String> {

    public static final String TAG = "EbPayDemo";
    private NativePay instance;
    /**
     * 商品名称
     */
    private EditText mGoodsName;
    /**
     * 商品价格
     */
    private EditText mTotalAmount;
    /**
     * 商品描述
     */
    private EditText mGoodsDesc;
    /**
     * 商品地址
     */
    private EditText mGoodsUrl;
    /**
     * 后台通知地址
     */
    private EditText mReturnUrl;
    /**
     * 分润方案，只有涉及分润的商户才需要传入改字段
     */
    private EditText mProfitSolution;

    private Button payBt;

    private ProgressDialog dialog;

    private static final int CREATE_ORDER = 1;

    private Handler mDopayHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CREATE_ORDER:
                    if (msg.obj != null) {
                        if (msg.obj instanceof String) {
                            String str = (String) msg.obj;
                            if (!TextUtils.isEmpty(str)
                                    && str.contains("service_code")) {
                                realPay(str);
                            } else {
                                Toast.makeText(NativePay.this, "订单创建失败",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.layout_nativepay);
        instance = this;
        // 构造初始化参数
        Map<String, String> params = new HashMap<String, String>();
        // 设定商户号
        // params.put("sp", PartnerConfig.PARTNER_ID);

        BaiduWallet.getInstance().init(this, params, new InitCallBack() {
            @Override
            public void onComplete(boolean result) {
            }
        });
        mGoodsName = (EditText) findViewById(R.id.goods_name);
        mTotalAmount = (EditText) findViewById(R.id.total_amount);
        mGoodsDesc = (EditText) findViewById(R.id.goods_desc);
        mGoodsUrl = (EditText) findViewById(R.id.goods_url);
        mReturnUrl = (EditText) findViewById(R.id.return_url);
        mProfitSolution = (EditText) findViewById(R.id.profit_solution);

        payBt = (Button) findViewById(R.id.pay);
        payBt.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if (view == payBt) {
            pay();
        }
    }

    /**
     * 该方法中是对所有支付参数进行拼接后签名得到sign值，然后将所有参数外加sign值用&拼接传入doPay接口的orderInfo中
     * 参数的拼接和签名是放到我们自己的测试服务器来进行的，商户必须也将此步骤放到自己的server端来做
     */
    private void pay() {
        final String name = mGoodsName.getText().toString();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "商品名字不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        String price = mTotalAmount.getText().toString();
        if (TextUtils.isEmpty(price)) {
            Toast.makeText(this, "商品价格不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        BigDecimal bigPrice = new BigDecimal(price);
        if (bigPrice.compareTo(new BigDecimal(0)) != 1) {
            Toast.makeText(this, "商品价格必须大于0", Toast.LENGTH_SHORT).show();
            return;
        }

        String goodsDesc = mGoodsDesc.getText().toString();
        String goodsUrl = mGoodsUrl.getText().toString();
        String returnUrl = mReturnUrl.getText().toString();
        String profitSolution = mProfitSolution.getText().toString();

        final List<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
        params.add(new Pair<>("goods_name", name)); //
        params.add(new Pair<>("total_amount", price));//
        params.add(new Pair<>("goods_desc", goodsDesc)); //
        params.add(new Pair<>("goods_url", goodsUrl)); //
        params.add(new Pair<>("return_url", returnUrl));//
        params.add(new Pair<>("unit_amount", "")); //
        params.add(new Pair<>("unit_count", "")); //
        params.add(new Pair<>("transport_amount", ""));//
        params.add(new Pair<>("page_url", "")); //
        params.add(new Pair<>("buyer_sp_username", "")); //
        params.add(new Pair<>("pay_type", "2")); //
        params.add(new Pair<>("extra", ""));

        String env = DebugConfig.getInstance(this).getEnvironment();
        if (!TextUtils.isEmpty(env)) {
            if (env.equals(DebugConfig.ENVIRONMENT_QA)) {
                params.add(new Pair<>("environment", "qa"));
            } else if (env.equals(DebugConfig.ENVIRONMENT_RD)) {
                params.add(new Pair<>("environment", "rd"));
            } else {
                params.add(new Pair<>("environment", "online"));
            }
        } else {
            params.add(new Pair<>("environment", "online"));
        }


        if (!TextUtils.isEmpty(profitSolution)) {
            params.add(new Pair<>("profit_solution", profitSolution));
        }

        doPost(instance, params);


    }

    /**
     * 支付结果处理
     *
     * @param stateCode
     * @param payDesc
     */
    private void handlepayResult(int stateCode, String payDesc) {

        switch (stateCode) {
            case Constants.PAY_STATUS_SUCCESS:// 需要到服务端验证支付结果
                Toast.makeText(NativePay.this, "支付成功\n " + payDesc, Toast.LENGTH_SHORT).show();
                break;
            case Constants.PAY_STATUS_PAYING:// 需要到服务端验证支付结果
                Toast.makeText(NativePay.this, "支付处理中\n " + payDesc, Toast.LENGTH_SHORT).show();
                break;
            case Constants.PAY_STATUS_CANCEL:
                Toast.makeText(NativePay.this, "支付取消\n " + payDesc, Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }

        //TODO  宿主可以让自己的服务端主动查询支付结果，如何主动查询支付结果请参照文档中的 “服务器通知支付结果”章节

    }

    private void realPay(String orderInfo) {
        /**
         * orderInfo是订单信息，建议在宿主server端生成并完成签名以确保安全性
         * 具体签名规则请参照接入文档中的 签名机制 章节
         */
        BaiduWallet.getInstance().doPay(this, orderInfo, new PayCallBack() {
            public void onPayResult(int stateCode, String payDesc) {
                handlepayResult(stateCode, payDesc);
            }

            public boolean isHideLoadingDialog() {
                return true;
            }
        });

    }

    /**
     * 客户端发送参数给server进行签名，建议宿主集成时需要宿主后端签名完成后返给客户端orderInfo，然后客户端调用doPay
     *
     * @param postParameters
     * @return
     */
    private void doPost(Context context, List<Pair<String, String>> postParameters) {
        dialog = ProgressDialog.show(context, "提示：", "正在生成预付款单ing...");
        Gson gson = new Gson();
        String entity = gson.toJson(postParameters);
        RetrofitHttpUtil retrofitHttpUtil = new RetrofitHttpUtil(context, "http://bdwallet.duapp.com/");
        //retrofitHttpUtil.setLogLevel(true, HttpLoggingInterceptor.Level.BODY);
        ApiService apiService = retrofitHttpUtil.getRetrofit().create(ApiService.class);
        Call<String> userInfoCall = apiService.createBaiDuPayOrder(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), entity));
        userInfoCall.enqueue(this);


//        String resultStr = "";
//        BufferedReader in = null;
//        try {
//            HttpClient client = new DefaultHttpClient();
//            HttpPost request = new HttpPost(url);
//            // 实例化UrlEncodedFormEntity对象
//            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(
//                    postParameters, "gbk");
//
//            // 使用HttpPost对象来设置UrlEncodedFormEntity的Entity
//            request.setEntity(formEntity);
//            HttpResponse response = client.execute(request);
//            in = new BufferedReader(new InputStreamReader(response.getEntity()
//                    .getContent()));
//
//            StringBuffer string = new StringBuffer("");
//            String lineStr = "";
//            while ((lineStr = in.readLine()) != null) {
//                // Log.i(TAG,"lineStr="+lineStr);
//                string.append(lineStr + "\n");
//                resultStr = lineStr;
//            }
//            in.close();
//            Log.i(TAG, "result=" + string.toString());
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (in != null) {
//                try {
//                    in.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return resultStr;
    }

    @Override
    public void onResponse(Response<String> response, Retrofit retrofit) {
        if (dialog != null) {
            dialog.dismiss();
        }
        if (response.isSuccess()) {
            String orderinfo = response.body();
            if (orderinfo != null && orderinfo.length() > 0) {
                Log.i(TAG, "orderinfo=" + orderinfo);
                mDopayHandler.sendMessage(mDopayHandler.obtainMessage(
                        CREATE_ORDER, orderinfo));
            }
        }
        if (response.errorBody() != null) {
            try {
                Log.d(TAG, "onResponse Error " + response.errorBody().string());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onFailure(Throwable t) {
        if (dialog != null) {
            dialog.dismiss();
        }
        Log.i(TAG, "onFailure " + t.getMessage());
    }
}