package cn.digielec.quickpaysdk.interfaces;

/**
 * Created by dw on 2016/4/7.
 */
public interface onPayResultListener {

    void onPayResultSuccess(int mPayChanner);

    void onPayResultFail(int mPayChanner);
}
