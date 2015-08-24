package com.dreamone.dev;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.dreamone.dev.utils.Constants;
import com.dreamone.dev.utils.Util;
import com.dreamone.dev.wbapi.AccessTokenKeeper;
import com.dreamone.dev.wbapi.AuthDialogListener;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.*;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.exception.WeiboException;
import com.tencent.mm.sdk.modelmsg.*;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

/**
 * Created by wenjie on 15/8/13.
 */
public class SelectLoginTypeActivity extends Activity implements View.OnClickListener,IWeiboHandler.Response {
    private static final String TAG = "SelectLoginTypeActivity";
    private IWXAPI mWeixinAPI;
    private IWeiboShareAPI mWeiboShareAPI;
    private AuthInfo mWeiboAuthInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_login_type);
        regToWXWB();
        initView();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // 从当前应用唤起微博并进行分享后，返回到当前应用时，需要在此处调用该函数
        // 来接收微博客户端返回的数据；执行成功，返回 true，并调用
        // {@link IWeiboHandler.Response#onResponse}；失败返回 false，不调用上述回调
        mWeiboShareAPI.handleWeiboResponse(intent, this);
    }

    private void initView() {
        findViewById(R.id.weixin_login).setOnClickListener(this);
        findViewById(R.id.weixin_session_share).setOnClickListener(this);
        findViewById(R.id.weixin_timeline_share).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.weixin_login:
                loginWithWeixin();
                break;
            case R.id.weixin_session_share:
                shareToWeixin(false);
                break;
            case R.id.weixin_timeline_share:
                shareToWeixin(true);
                break;
            case R.id.open_weixin:
                mWeixinAPI.openWXApp();
                break;
            case R.id.weibo_auth:
                loginWithWeiBo();
                break;
            case R.id.weibo_share:
                share2Weibo(true);
                break;
            default:
                break;
        }
    }


    private void regToWXWB() {
        mWeixinAPI = WXAPIFactory.createWXAPI(this, Constants.WeiXin.APP_ID, false);
        boolean b = mWeixinAPI.registerApp(Constants.WeiXin.APP_ID);

        mWeiboAuthInfo = new AuthInfo(this, Constants.WB.APP_KEY, Constants.WB.REDIRECT_URL, Constants.WB.SCOPE);
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this, Constants.WB.APP_KEY);
        mWeiboShareAPI.registerApp();
    }

    private void loginWithWeixin() {
        toast("微信登录需要待审核通过");
        SendAuth.Req req = new SendAuth.Req();
        req.scope = Constants.WeiXin.SCOPE;
        req.state = Constants.WeiXin.STATE;
        mWeixinAPI.sendReq(req);
    }
    
    private void shareToWeixin(boolean timeline) {
        //链接消息
        WXWebpageObject webpageObject = new WXWebpageObject();
        webpageObject.webpageUrl = "http://weibo.com/aniubility";
        //文本消息
        WXTextObject textObj = new WXTextObject();
        textObj.text = "风萧萧兮易水寒";
        //图片消息
        WXImageObject imageObject = new WXImageObject();
        imageObject.imageData = Util.bitmapToBytes(BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher));


        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = webpageObject;
        msg.setThumbImage(BitmapFactory.decodeResource(getResources(),R.drawable.icon));
        msg.mediaTagName = "这是mediaTagName";
        msg.messageAction = "This is messageAction";
        msg.title = "这个是WXMediaMessage的Title";
        msg.description = "这个是WXMediaMessage的description";//Ignored when shared to timeline.
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("shareToSession");
        req.message = msg;
        req.scene = timeline ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        mWeixinAPI.sendReq(req);
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }


    private void loginWithWeiBo() {
        //网页登录
//        mWeibo.authorize(this, new AuthDialogListener(this));
        //AllinOne 登录
        SsoHandler mSsoHandler = new SsoHandler(this, mWeiboAuthInfo);
        mSsoHandler.authorize(new AuthDialogListener(this));
    }

    private void share2Weibo (boolean client) {
        // 1. 初始化微博的分享消息
        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();

        weiboMessage.textObject = getTextObj();
        weiboMessage.imageObject = getImageObj();
        weiboMessage.mediaObject = null;//可以添加网页，视频，音频等。
        // 2. 初始化从第三方到微博的消息请求
        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        // 用transaction唯一标识一个请求
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = weiboMessage;

        // 3. 发送请求消息到微博，唤起微博分享界面
        AuthInfo authInfo = new AuthInfo(this, Constants.WB.APP_KEY, Constants.WB.REDIRECT_URL, Constants.WB.SCOPE);
        Oauth2AccessToken accessToken = AccessTokenKeeper.readAccessToken(getApplicationContext());
        String token = "";
        if (accessToken != null) {
            token = accessToken.getToken();
        }
        mWeiboShareAPI.sendRequest(this, request, authInfo, token, new WeiboAuthListener() {
            @Override
            public void onWeiboException(WeiboException arg0) {
            }

            @Override
            public void onComplete(Bundle bundle) {
                Oauth2AccessToken newToken = Oauth2AccessToken.parseAccessToken(bundle);
                AccessTokenKeeper.writeAccessToken(getApplicationContext(), newToken);
                Toast.makeText(getApplicationContext(), "onAuthorizeComplete token = " + newToken.getToken(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
            }
        });
    }

    private void toast(String s) {
        Log.i(TAG, this.getClass().getSimpleName() + "  : " + s);
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResponse(BaseResponse baseResp) {
        //IWeiboHandler.Response
        Log.d(TAG, "IWeiboHandler.Response onResponse code : " + baseResp.errCode + "; msg : " + baseResp.errMsg);
        switch (baseResp.errCode) {
            case WBConstants.ErrorCode.ERR_OK:
                Toast.makeText(this, R.string.weibosdk_demo_toast_share_success, Toast.LENGTH_LONG).show();
                break;
            case WBConstants.ErrorCode.ERR_CANCEL:
                Toast.makeText(this, R.string.weibosdk_demo_toast_share_canceled, Toast.LENGTH_LONG).show();
                break;
            case WBConstants.ErrorCode.ERR_FAIL:
                Toast.makeText(this,
                        getString(R.string.weibosdk_demo_toast_share_failed) + "Error Message: " + baseResp.errMsg,
                        Toast.LENGTH_LONG).show();
                break;
        }
    }

    private String getPath() {
        Resources r = getResources();
        int drawable = R.drawable.ic_launcher;
        Uri uri =  Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + r.getResourcePackageName(drawable) + "/"
                + r.getResourceTypeName(drawable) + "/"
                + r.getResourceEntryName(drawable));
        String [] proj={MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query( uri,
                proj, // Which columns to return
                null, // WHERE clause; which rows to return (all rows)
                null, // WHERE clause selection arguments (none)
                null); // Order-by clause (ascending by name)


        String path = null;
        if(cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();

            path = cursor.getString(column_index);
            cursor.close();
        }
        Log.i(TAG, "path = " + path + "; uri = " + uri + "; cursor = " + cursor);
        return path;
    }
    /**
     * 创建图片消息对象。
     *
     * @return 图片消息对象。
     */
    private ImageObject getImageObj() {
        ImageObject imageObject = new ImageObject();
        //        设置缩略图。 注意：最终压缩过的缩略图大小不得超过 32kb。
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        imageObject.setImageObject(bitmap);
        return imageObject;
    }
    /**
     * 创建文本消息对象。
     *
     * @return 文本消息对象。
     */
    private TextObject getTextObj() {
        TextObject textObject = new TextObject();
        textObject.text = "两个黄鹂鸣翠柳,http://www.baidu.com";
        return textObject;
    }
}
