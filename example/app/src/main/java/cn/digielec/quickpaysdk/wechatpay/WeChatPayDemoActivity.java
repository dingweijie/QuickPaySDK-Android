package cn.digielec.quickpaysdk.wechatpay;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.util.Xml;
import android.widget.Toast;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;
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

import cn.digielec.quickpaysdk.retrofit.ApiService;
import cn.digielec.quickpaysdk.retrofit.RetrofitHttpUtil;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;


public class WeChatPayDemoActivity extends AppCompatActivity implements Callback<String> {
    private final String TAG = WeChatPayDemoActivity.class.getSimpleName();
    private WeChatPayDemoActivity instance;
    private final IWXAPI msgApi = WXAPIFactory.createWXAPI(this, null);
    private Map<String, String> resultunifiedorder;
    private StringBuffer sb;
    private PayReq req;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        // setContentView(R.layout.activity_main);
        req = new PayReq();
        sb = new StringBuffer();

        //生成PrepayId
//        GetPrepayIdTask getPrepayId = new GetPrepayIdTask();
//        getPrepayId.execute();
        GetPrepayID(instance, "Test", "20");
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

    private void GetPrepayID(Context context, String productName, String amount) {
        dialog = ProgressDialog.show(instance, "提示：", "正在生成预付款单ing...");
        String entity = genProductArgs(productName, amount);
        RetrofitHttpUtil retrofitHttpUtil = new RetrofitHttpUtil(context, "https://api.mch.weixin.qq.com/");
        //retrofitHttpUtil.setLogLevel(true, HttpLoggingInterceptor.Level.BODY);
        ApiService apiService = retrofitHttpUtil.getRetrofit().create(ApiService.class);
        Call<String> userInfoCall = apiService.createWeChatPayOrder(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), entity));
        userInfoCall.enqueue(this);
    }

    @Override
    public void onResponse(Response<String> response, Retrofit retrofit) {
        if (dialog != null) {
            dialog.dismiss();
        }
        if (response.isSuccess()) {
            String body = response.body();
            if (body != null && body.length() > 0) {
                Map<String, String> xml = decodeXml(body);
                if (xml.get("return_code").equals("FAIL")) {
                    new AlertDialog.Builder(this).setTitle("支付失败").setMessage(xml.get("return_msg").toString())
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialoginterface, int i) {
                                    finish();
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
        if (dialog != null) {
            dialog.dismiss();
        }
        Log.i(TAG, "onFailure " + t.getMessage());
    }

//    private class GetPrepayIdTask extends AsyncTask<Void, Void, Map<String, String>> {
//
//        private ProgressDialog dialog;
//
//
//        @Override
//        protected void onPreExecute() {
//            dialog = ProgressDialog.show(instance, "提示：", "正在生成预付款单ing...");
//        }
//
//        @Override
//        protected Map<String, String> doInBackground(Void... params) {
//
//            String url = String.format("https://api.mch.weixin.qq.com/pay/unifiedorder");
//           // String entity = genProductArgs();
//            if (entity == null)
//                return null;
//
//            Log.e("--- entity ---", entity);
//            byte[] buf = null;
//            //      byte[] buf = Util.httpPost(url, entity);
//
//            String content = new String(buf);
//            Log.e("--- content ---", content);
//            Map<String, String> xml = decodeXml(content);
//
//            return xml;
//        }
//
//        @Override
//        protected void onPostExecute(Map<String, String> result) {
//            if (dialog != null) {
//                dialog.dismiss();
//            }
//            if (result == null)
//                return;
//            sb.append("prepay_id\n" + result.get("prepay_id") + "\n\n");
//
//            resultunifiedorder = result;
//
//            genPayReq();
//
//        }
//
//        @Override
//        protected void onCancelled() {
//            super.onCancelled();
//        }
//
//
//    }


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

    private void sendPayReq() {
        msgApi.registerApp(PayConstant.APPID);
        msgApi.sendReq(req);
    }


}
