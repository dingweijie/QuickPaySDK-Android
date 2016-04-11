package cn.digielec.quickpaysdk.alipay;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.alipay.sdk.app.PayTask;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import cn.digielec.quickpaysdk.interfaces.onPayResultListener;
import cn.digielec.quickpaysdk.utils.QuickPayConstants;

/**
 * Created by dw on 2016/4/7.
 */
public class AliPayAPI {

    private Activity activity;
    // 商户PID
    public static final String PARTNER = "2088511540333935";
    // 商户收款账号
    public static final String SELLER = "zfb@kapp.cn";
    // 商户私钥，pkcs8格式
    public static final String RSA_PRIVATE = "MIICeQIBADANBgkqhkiG9w0BAQEFAASCAmMwggJfAgEAAoGBAKikbfvM+Vh8UO9zjQkuX0M1p6Z92FgDIwWw+c0LVsgsTo7iuDSHlFqKK6hcmHNtWOqRzxcA+5r50FR0/lPZ3ePvkLtTICLDqCNx9fuapzVoBtOZBOMQTfBI6k5UmuLELF3uFK4MOysTLd+WxkR09GR5k+v8WQvbxLyiSt5h4bOxAgMBAAECgYEAnkUDO2fZM2QoiTGG0XEF0ovOdYBxw9fdzH+VUQwtO9vpNkws2I/NeVNnObD9GocrBaomEs7HU2DUVg8eiQ9VIAlZrB0+bzu+nGSAYIjumQ0Cu3x3tKjC1oNAMtzi7PVYdpqTRkaikxe+MqRrKExj0CSKycYURF/eGefiDKofGIECQQDeLr7sKJEsWCfD7XaFIBqgdx6V5lguboYMSA7s7YBwprerIaSy3cixZ7DSdxA7iIs2JPrjTx2b0gF6rZP15vH5AkEAwk+CDgkKJOcUHZf0AEu8MOyQR6yNS2DN1FnmYSKb2uELDIZmUPZBKpHanO1ym/LEqLh+qsg1LEz1yJM1JNk9eQJBAI3B2YKnp2hZnpV9br8lapezKMqPOvf1AIXZe9xi9C3r0QfY4VJV+vRlqgW+fZbeWaPkGEbrdYKzjMoO8XZVMVECQQCGlSsvLXOpO6PjG2wVCF/AQxlri0gR/WqSufGnNaFdaKGOe0hCq01Xfs48AvpuqScs9RxGjYTGukdCNTOJ4i2BAkEAjXbvk86cJz5XLGubfKFwD/Nv0r6bol2aSb07ti2mF3z6jwWH5bOD0uYzxwUjooPuUbfq0ensl31PPC80Nf40jA==";
    // 支付宝公钥
    public static final String RSA_PUBLIC = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCk1ecWCeTiEkNYgMlGOVqTrMNZ4/PtrTQ07uaEL/YkW30rp1pWKq5xr/zjcr00kKT7JPT+KJg2N+hUn1PPHIT5gKez6aeMnC/+jkuBg1ajqm3uOuf5sbAs0ZFt9bvjWjLzrTqbC4xRUY9VbuA+/hGDDZAlM91yVCmj9Qb+fMidBwIDAQAB";

    private static final int SDK_PAY_FLAG = 1;

    private onPayResultListener mPayResultListener;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    PayResult payResult = new PayResult((String) msg.obj);
                    /**
                     * 同步返回的结果必须放置到服务端进行验证（验证的规则请看https://doc.open.alipay.com/doc2/
                     * detail.htm?spm=0.0.0.0.xdvAU6&treeId=59&articleId=103665&
                     * docType=1) 建议商户依赖异步通知
                     */
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息

                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                    if (TextUtils.equals(resultStatus, "9000")) {
                        mPayResultListener.onPayResultSuccess(QuickPayConstants.PAY_CHANNEL_ALIPAY);
                        //  Toast.makeText(AliPayDemoActivity.this, "支付成功", Toast.LENGTH_SHORT).show();
                    } else {
                        // 判断resultStatus 为非"9000"则代表可能支付失败
                        // "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                        if (TextUtils.equals(resultStatus, "8000")) {
                            // Toast.makeText(AliPayDemoActivity.this, "支付结果确认中", Toast.LENGTH_SHORT).show();

                        } else {
                            // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                            //   Toast.makeText(AliPayDemoActivity.this, "支付失败", Toast.LENGTH_SHORT).show();
                            mPayResultListener.onPayResultFail(QuickPayConstants.PAY_CHANNEL_ALIPAY);
                        }
                    }
                    break;
                }
                default:
                    break;
            }
        }
    };

    public AliPayAPI(Activity activity, onPayResultListener onPayResultListener) {
        this.activity = activity;
        this.mPayResultListener = onPayResultListener;
    }


    /**
     * call alipay sdk pay.
     *
     * @param mProductName
     * @param mProductDesc
     * @param mAmount
     */
    public void doPay(String mProductName, String mProductDesc, String mAmount) {

        String orderInfo = getOrderInfo(mProductName, mProductDesc, mAmount);

        /**
         * 特别注意，这里的签名逻辑需要放在服务端，切勿将私钥泄露在代码中！
         */
        String sign = sign(orderInfo);
        try {
            /**
             * 仅需对sign 做URL编码
             */
            sign = URLEncoder.encode(sign, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        /**
         * 完整的符合支付宝参数规范的订单信息
         */
        final String payInfo = orderInfo + "&sign=\"" + sign + "\"&" + getSignType();

        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                // 构造PayTask 对象
                PayTask alipay = new PayTask(activity);
                // 调用支付接口，获取支付结果
                String result = alipay.pay(payInfo, true);

                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };

        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    /**
     * create the order info. 创建订单信息
     */
    private String getOrderInfo(String mProductName, String mProductDesc, String mAmount) {

        // 签约合作者身份ID
        String orderInfo = "partner=" + "\"" + PARTNER + "\"";

        // 签约卖家支付宝账号
        orderInfo += "&seller_id=" + "\"" + SELLER + "\"";

        // 商户网站唯一订单号
        orderInfo += "&out_trade_no=" + "\"" + getOutTradeNo() + "\"";

        // 商品名称
        orderInfo += "&subject=" + "\"" + mProductName + "\"";

        // 商品详情
        orderInfo += "&body=" + "\"" + mProductDesc + "\"";

        // 商品金额
        orderInfo += "&total_fee=" + "\"" + mAmount + "\"";

        // 服务器异步通知页面路径
        orderInfo += "&notify_url=" + "\"" + "http://notify.msp.hk/notify.htm" + "\"";

        // 服务接口名称， 固定值
        orderInfo += "&service=\"mobile.securitypay.pay\"";

        // 支付类型， 固定值
        orderInfo += "&payment_type=\"1\"";

        // 参数编码， 固定值
        orderInfo += "&_input_charset=\"utf-8\"";

        // 设置未付款交易的超时时间
        // 默认30分钟，一旦超时，该笔交易就会自动被关闭。
        // 取值范围：1m～15d。
        // m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
        // 该参数数值不接受小数点，如1.5h，可转换为90m。
        orderInfo += "&it_b_pay=\"30m\"";

        // extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
        // orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

        // 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
        orderInfo += "&return_url=\"m.alipay.com\"";

        // 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
        // orderInfo += "&paymethod=\"expressGateway\"";

        return orderInfo;
    }


    /**
     * get the out_trade_no for an order. 生成商户订单号，该值在商户端应保持唯一（可自定义格式规范）
     */
    private String getOutTradeNo() {
        SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss", Locale.getDefault());
        Date date = new Date();
        String key = format.format(date);

        Random r = new Random();
        key = key + r.nextInt();
        key = key.substring(0, 15);
        return key;
    }


    /**
     * sign the order info. 对订单信息进行签名
     *
     * @param content 待签名订单信息
     */
    private String sign(String content) {
        return SignUtils.sign(content, RSA_PRIVATE);
    }

    /**
     * get the sign type we use. 获取签名方式
     */
    private String getSignType() {
        return "sign_type=\"RSA\"";
    }
}
