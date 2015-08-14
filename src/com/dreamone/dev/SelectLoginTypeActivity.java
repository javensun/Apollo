package com.dreamone.dev;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.dreamone.dev.utils.Constants;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

/**
 * Created by wenjie on 15/8/13.
 */
public class SelectLoginTypeActivity extends Activity implements View.OnClickListener {
    private IWXAPI mWeixinAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_login_type);
        regToWeixin();
        initView();
    }

    private void initView() {
        findViewById(R.id.weixin_login).setOnClickListener(this);
        findViewById(R.id.weixin_session_share).setOnClickListener(this);
        findViewById(R.id.weixin_timeline_share).setOnClickListener(this);
    }

    private void loginWithWeixin() {
        toast("Login with weixin.");
    }

    private void shareToSession(boolean timeline) {
        WXTextObject textObj = new WXTextObject();
        textObj.text = "风萧萧兮易水寒";
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObj;
        msg.description = "这个是description";
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("shareToSession");
        req.message = msg;
        req.scene = timeline ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        boolean b = mWeixinAPI.sendReq(req);
        toast("share : " + b);

    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }


    private void regToWeixin() {
        mWeixinAPI = WXAPIFactory.createWXAPI(this, Constants.WeiXin.APP_ID, false);
        boolean b = mWeixinAPI.registerApp(Constants.WeiXin.APP_ID);
        toast("register : " + b);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.weixin_login:
                loginWithWeixin();
                break;
            case R.id.weixin_session_share:
                shareToSession(false);
                break;
            case R.id.weixin_timeline_share:
                shareToSession(true);
                break;
            case R.id.open_weixin:
                mWeixinAPI.openWXApp();
                break;
            default:
                break;
        }
    }

    private void toast(String s) {
        Log.i("SWJ", this.getClass().getSimpleName() + "  : " + s);
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
