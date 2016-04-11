package cn.digielec.quickpaysdk.wechatpay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.util.Pair;
import android.util.Xml;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cn.digielec.quickpaysdk.interfaces.onPayResultListener;
import cn.digielec.quickpaysdk.retrofit.ApiService;
import cn.digielec.quickpaysdk.retrofit.RetrofitHttpUtil;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by dw on 2016/4/7.
 */
public class WeChatPayAPI implements Callback<String> {
    private final String TAG = WeChatPayAPI.class.getSimpleName();
    private Activity activity;
    public static onPayResultListener mPayResultListener;
    private StringBuffer sb;
    private PayReq req;
    private final IWXAPI msgApi;
    private Map<String, String> resultunifiedorder;

    public WeChatPayAPI(Activity activity, onPayResultListener onPayResultListener) {
        this.activity = activity;
        this.mPayResultListener = onPayResultListener;
        req = new PayReq();
        sb = new StringBuffer();
        msgApi = WXAPIFactory.createWXAPI(activity, null);
    }

    public void doPay(String productName, String amount) {
        String entity = genProductArgs(productName, amount);
        RetrofitHttpUtil retrofitHttpUtil = new RetrofitHttpUtil(activity, "https://api.mch.weixin.qq.com/");
        //retrofitHttpUtil.setLogLevel(true, HttpLoggingInterceptor.Level.BODY);
        ApiService apiService = retrofitHttpUtil.getRetrofit().create(ApiService.class);
        Call<String> userInfoCall = apiService.createWeChatPayOrder(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), entity));
        userInfoCall.enqueue(this);
    }

    @Override
    public void onResponse(Response<String> response, Retrofit retrofit) {
        if (response.isSuccess()) {
            String body = response.body();
            if (body != null && body.length() > 0) {
                Map<String, String> xml = decodeXml(body);
                if (xml.get("return_code").equals("FAIL")) {
                    new AlertDialog.Builder(activity).setTitle("支付失败").setMessage(xml.get("return_msg").toString())
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {
                                    dialoginterface.dismiss();
                                }
                            }).show();
                    return;
                }
                sb.append("prepay_id\n" + xml.get("prepay_id") + "\n\n");
                resultunifiedorder = xml;
                genPayReq();
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
        Log.i(TAG, "onFailure " + t.getMessage());
    }

    public Map<String, String> decodeXml(String content) {

        try {
            Map<String, String> xml = new HashMap<String, String>();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(content));
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {

                String nodeName = parser.getName();
                switch (event) {
                    case XmlPullParser.START_DOCUMENT:

                        break;
                    case XmlPullParser.START_TAG:

                        if ("xml".equals(nodeName) == false) {
                            xml.put(nodeName, parser.nextText());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                event = parser.next();
            }

            return xml;
        } catch (Exception e) {
            Log.e("--- Exception ---", e.toString());
        }
        return null;

    }


    private String genNonceStr() {
        Random random = new Random();
        return MD5.getMessageDigest(String.valueOf(random.nextInt(10000)).getBytes());
    }

    private long genTimeStamp() {
        return System.currentTimeMillis() / 1000;
    }


    private String genOutTradNo() {
        Random random = new Random();
        return MD5.getMessageDigest(String.valueOf(random.nextInt(10000)).getBytes());
    }


    //
    private String genProductArgs(String productName, String amount) {
        if (productName == null || productName.length() <= 0 || amount == null || amount.length() <= 0) {
            Log.i(TAG, "--- genProductArgs ---" + null);
            return null;
        }
        StringBuffer xml = new StringBuffer();

        try {
            String nonceStr = genNonceStr();


            xml.append("</xml>");
            List<Pair<String, String>> packageParams = new LinkedList<Pair<String, String>>();
            packageParams.add(new Pair<>("appid", PayConstant.APPID));
            packageParams.add(new Pair<>("body", productName));
            packageParams.add(new Pair<>("mch_id", PayConstant.PARTNERID));
            packageParams.add(new Pair<>("nonce_str", nonceStr));
            packageParams.add(new Pair<>("notify_url", "http://121.40.35.3/test"));
            packageParams.add(new Pair<>("out_trade_no", genOutTradNo()));
            packageParams.add(new Pair<>("spbill_create_ip", "127.0.0.1"));
            packageParams.add(new Pair<>("total_fee", amount));
            packageParams.add(new Pair<>("trade_type", "APP"));


            String sign = genPackageSign(packageParams);
            packageParams.add(new Pair<>("sign", sign));


            String xmlstring = toXml(packageParams);

            return xmlstring;

        } catch (Exception e) {
            Log.e(TAG, "genProductArgs fail, ex = " + e.getMessage());
            return null;
        }


    }


    private String genPackageSign(List<Pair<String, String>> params) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < params.size(); i++) {
            sb.append(params.get(i).first);
            sb.append('=');
            sb.append(params.get(i).second);
            sb.append('&');
        }
        sb.append("key=");
        sb.append(PayConstant.APPSECRET);
        Log.e(TAG, "--- PayConstant.APPSECRET ---" + PayConstant.APPSECRET);

        String packageSign = MD5.getMessageDigest(sb.toString().getBytes()).toUpperCase();
        Log.e("--- genPackageSign ---", packageSign);
        return packageSign;
    }

    private void genPayReq() {
        req.appId = PayConstant.APPID;
        req.partnerId = PayConstant.PARTNERID;
        req.prepayId = resultunifiedorder.get("prepay_id");
        req.packageValue = "Sign=WXPay";
        req.nonceStr = genNonceStr();
        req.timeStamp = String.valueOf(genTimeStamp());


        List<Pair<String, String>> signParams = new LinkedList<Pair<String, String>>();
        signParams.add(new Pair<>("appid", req.appId));
        signParams.add(new Pair<>("noncestr", req.nonceStr));
        signParams.add(new Pair<>("package", req.packageValue));
        signParams.add(new Pair<>("partnerid", req.partnerId));
        signParams.add(new Pair<>("prepayid", req.prepayId));
        signParams.add(new Pair<>("timestamp", req.timeStamp));

        req.sign = genAppSign(signParams);

        sb.append("sign\n" + req.sign + "\n\n");


        Log.e("--- signParams ---", signParams.toString());

        sendPayReq();

    }

    private String genAppSign(List<Pair<String, String>> params) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < params.size(); i++) {
            sb.append(params.get(i).first);
            sb.append('=');
            sb.append(params.get(i).second);
            sb.append('&');
        }
        sb.append("key=");
        sb.append(PayConstant.APPSECRET);

        this.sb.append("sign str\n" + sb.toString() + "\n\n");
        String appSign = MD5.getMessageDigest(sb.toString().getBytes()).toUpperCase();
        Log.e("--- appSign ---", appSign);
        return appSign;
    }

    private String toXml(List<Pair<String, String>> params) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        sb.append("<xml>");
        for (int i = 0; i < params.size(); i++) {
            sb.append("<" + params.get(i).first + ">");


            sb.append(params.get(i).second);
            sb.append("</" + params.get(i).first + ">");
        }
        sb.append("</xml>");

        Log.e("orion", sb.toString());
        return new String(sb.toString().getBytes(), "ISO8859-1");
    }

    private void sendPayReq() {
        msgApi.registerApp(PayConstant.APPID);
        msgApi.sendReq(req);
    }
}
