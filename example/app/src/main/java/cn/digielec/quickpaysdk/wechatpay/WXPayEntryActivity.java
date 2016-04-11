package cn.digielec.quickpaysdk.wechatpay;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import cn.digielec.quickpaysdk.interfaces.onPayResultListener;
import cn.digielec.quickpaysdk.utils.QuickPayConstants;


public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

    private static final String TAG = "MicroMsg.SDKSample.WXPayEntryActivity";

    private IWXAPI api;

    private onPayResultListener mPayResultListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = WXAPIFactory.createWXAPI(this, PayConstant.APPID);

        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {
    }

    @Override
    public void onResp(BaseResp resp) {
        //  Log.d(TAG, "onPayFinish, errCode = " + resp.errCode);
        if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
            mPayResultListener = WeChatPayAPI.mPayResultListener;
            Log.i("payResultCallBack", "--- resp.errStr ---：" + resp.errStr + ";code=" + String.valueOf(resp.errCode));
            if (mPayResultListener == null) {
                Log.i("payResultCallBack", "--- payResultCallBack null ---");
                return;
            }
            switch (resp.errCode) {
                case 0:
                    mPayResultListener.onPayResultSuccess(QuickPayConstants.PAY_CHANNEL_WECHAT);
                    break;
                default:
                    mPayResultListener.onPayResultFail(QuickPayConstants.PAY_CHANNEL_WECHAT);
                    break;
            }
            finish();
        }
    }
}