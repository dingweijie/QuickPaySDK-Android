package cn.digielec.quickpaysdk;

import android.content.Context;

import cn.digielec.quickpaysdk.utils.QuickPayConstants;

/**
 * Created by dw on 2016/4/7.
 */
public class QuickPaySdk {

    private Context context;

    public QuickPaySdk(Context context) {
        this.context = context;
    }

    public void doPay(int mPayChannel, String amount) {
        switch (mPayChannel) {
            case QuickPayConstants.PAY_CHANNEL_ALIPAY:
                doAliPay(amount);
                break;
            case QuickPayConstants.PAY_CHANNEL_WECHAT:
                doWeChatPay(amount);
                break;
            case QuickPayConstants.PAY_CHANNEL_BDPAY:
                doBaiDuPay(amount);
                break;
            case QuickPayConstants.PAY_CHANNEL_JDPAY:
                doJDPay(amount);
                break;
            case QuickPayConstants.PAY_CHANNEL_UNIONPAY:
                doUnionPay(amount);
                break;
        }
    }


    /**
     * call alipay sdk pay.
     */
    private synchronized void doAliPay(String amount) {

    }

    /**
     * call wechatpay sdk pay.
     */
    private synchronized void doWeChatPay(String amount) {

    }

    /**
     * call baidupay sdk pay.
     */
    private synchronized void doBaiDuPay(String amount) {

    }

    /**
     * call jdpay sdk pay.
     */
    private synchronized void doJDPay(String amount) {

    }

    /**
     * call unionpay sdk pay.
     */
    private synchronized void doUnionPay(String amount) {

    }

}
