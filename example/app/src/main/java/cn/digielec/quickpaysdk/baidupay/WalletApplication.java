package cn.digielec.quickpaysdk.baidupay;

import android.app.Application;

import com.baidu.wallet.api.BaiduWallet;


public class WalletApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        
        /**
         * 商户接入时需要在自己工程的application中加入以下钱包初始化的代码
         */
        BaiduWallet.getInstance().initWallet(this);
    }

}
