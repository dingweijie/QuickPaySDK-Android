package cn.digielec.quickpaysdk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import cn.digielec.quickpaysdk.alipay.AliPayDemoActivity;
import cn.digielec.quickpaysdk.baidupay.WalletDemo;
import cn.digielec.quickpaysdk.unionpay.UnionPayDemoActivity;
import cn.digielec.quickpaysdk.utils.QuickPayConstants;
import cn.digielec.quickpaysdk.wechatpay.WeChatPayDemoActivity;

/**
 * Created by dw on 2016/4/5.
 */
public class MainActivity extends Activity {
    private final String TAG = MainActivity.class.getSimpleName();
    private MainActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
        Button mAliPayButton = (Button) findViewById(R.id.btn_pay_ali);
        Button mWeChatPayButton = (Button) findViewById(R.id.btn_pay_wechat);
        Button mBaiDuPayButton = (Button) findViewById(R.id.btn_pay_baidu);
        Button mUnionPayButton = (Button) findViewById(R.id.btn_pay_union);

        mAliPayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Open AliPay;
                Intent intent = new Intent(instance, AliPayDemoActivity.class);
                startActivity(intent);
            }
        });

        mWeChatPayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Open WeChatPay
                Intent intent = new Intent(instance, WeChatPayDemoActivity.class);
                startActivity(intent);
            }
        });

        mBaiDuPayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Open WeChatPay
                Intent intent = new Intent(instance, WalletDemo.class);
                startActivity(intent);
            }
        });

        mUnionPayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(instance, UnionPayDemoActivity.class);
                startActivity(intent);
            }
        });
    }
}
