/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package cn.digielec.quickpaysdk.baidupay;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.baidu.android.pay.BindBack;
import com.baidu.wallet.api.BaiduWallet;
import com.baidu.wallet.core.beans.BeanConstants;
import com.baidu.wallet.core.utils.GlobalUtils;
import com.baidu.wallet.core.utils.Md5Utils;

import cn.digielec.quickpaysdk.R;

/**
 * 
 * 商服版本的demo首页
 * 
 * @author xionglei01
 * @since 2015-11-27
 */
public class WalletDemo extends Activity implements OnClickListener {

	public static final String TAG = "WalletDemo";

	private EditText mUserType;
	private EditText mToken;
	private Button mNomalizeBt;
	private Button mBindcardBt;

	private TextView mLoginStatus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_walletdemo);

		mUserType = (EditText) findViewById(R.id.userType);
		mToken = (EditText) findViewById(R.id.token);

		mNomalizeBt = (Button) findViewById(R.id.bd_normalize);
		mNomalizeBt.setOnClickListener(this);
		mNomalizeBt.setVisibility(View.VISIBLE);

		mBindcardBt = (Button) findViewById(R.id.bd_dobind);
		mBindcardBt.setOnClickListener(this);
		mBindcardBt.setVisibility(View.VISIBLE);

		mLoginStatus = (TextView) findViewById(R.id.loginStatus);
		// 加密的token
		String token = BaiduWallet.getInstance().getLoginToken();
		int type = BaiduWallet.getInstance().getLoginType();
		if (!TextUtils.isEmpty(token)) {
			mToken.setText(token);
			mUserType.setText(type + "");
		}

		setTitle(BeanConstants.SDK_VERSION);

	}

	@Override
	protected void onResume() {
		super.onResume();
		initLoginStatus();
	}

	private void initLoginStatus() {
		if (BaiduWallet.getInstance().isLogin()) {
			mToken.setText(BaiduWallet.getInstance().getLoginToken());
			mUserType.setText(BaiduWallet.getInstance().getLoginType() + "");
			mLoginStatus.setText("已登录");
		} else {
			mToken.setText("");
			mUserType.setText("");
			mLoginStatus.setText("未登录");
		}
	}

	@Override
	public void onClick(View view) {
		if (view == mNomalizeBt) {
			Intent intent = new Intent(this, NativePay.class);
			startActivity(intent);
		}

		if (view == mBindcardBt) {

        	Map<String, String> order_info = new HashMap<String, String>();
        
        	order_info.put("sp_no", "3400000001");
        	order_info.put("version", "1");
        	order_info.put("activity_no", "1001");
        	order_info.put("order_no", "" + (System.currentTimeMillis()/1000));
         	order_info.put("page_url", "http://www.baidu.com");
         	order_info.put("return_url", "http://www.baidu.com");
        	order_info.put("sign_method", "1");
        	
        	String signString = "";
        	signString += "activity_no=" + order_info.get("activity_no");
        	signString += "&order_no=" + order_info.get("order_no");
        	signString += "&page_url=" + order_info.get("page_url");
			signString += "&return_url=" + order_info.get("return_url");
        	signString += "&sign_method=" + order_info.get("sign_method");
        	signString += "&sp_no=" + order_info.get("sp_no");
        	signString += "&version=" + order_info.get("version");
        	signString += "&key=Au88LPiP5vaN5FNABBa7NC4aQV28awRK";
        	
        	
        	String signResult = Md5Utils.toMD5(signString);

        	order_info.put("sign", signResult);
        	BaiduWallet.getInstance().doBind(this, new BindBack() {
				
				@Override
				public void onBindResult(int statusCode, String params) {
					GlobalUtils.toast(WalletDemo.this, "statusCode: "+ statusCode +" params: "+params);
				}
				
				@Override
				public boolean isHideLoadingDialog() {
					// TODO Auto-generated method stub
					return false;
				}
			}, order_info);
        	
        }
	}
}