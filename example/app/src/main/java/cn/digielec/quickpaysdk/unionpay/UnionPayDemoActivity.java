package cn.digielec.quickpaysdk.unionpay;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by dw on 2016/4/8.
 */
public class UnionPayDemoActivity extends Activity {
    private final String TAG = UnionPayDemoActivity.class.getSimpleName();
    private UnionPayDemoActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
    }
}
