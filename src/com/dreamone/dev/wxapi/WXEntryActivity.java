package com.dreamone.dev.wxapi;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.dreamone.dev.R;
import com.dreamone.dev.utils.Constants;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.ShowMessageFromWX;
import com.tencent.mm.sdk.modelmsg.WXAppExtendObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;


public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;

    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = WXAPIFactory.createWXAPI(this, Constants.WeiXin.APP_ID, false);
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
        toast("onReq:"+ req.transaction);
        switch (req.getType()) {
            case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
                goToGetMsg();
                break;
            case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
                goToShowMsg((ShowMessageFromWX.Req) req);
                break;
            default:
                break;
        }
    }

    @Override
    public void onResp(BaseResp resp) {
        int result;
        toast("onResp:"+ resp.errStr +"; code:"+resp.errCode);
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                result = R.string.send_succeeded;
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                result = R.string.send_cancelled;
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                result = R.string.send_denied;
                break;
            default:
                result = R.string.unknow_error;
                break;
        }

        Toast.makeText(this,result,Toast.LENGTH_SHORT).show();
    }

    private void goToGetMsg() {
        toast("goToGetMsg()");
//        Intent intent = new Intent(this, GetFromWXActivity.class);
//        intent.putExtras(getIntent());
//        startActivity(intent);
//        finish();
    }

    private void goToShowMsg(ShowMessageFromWX.Req showReq) {
        toast("goToShowMsg()");
//        WXMediaMessage wxMsg = showReq.message;
//        WXAppExtendObject obj = (WXAppExtendObject) wxMsg.mediaObject;
//        StringBuffer msg = new StringBuffer();
//        msg.append("description: ");
//        msg.append(wxMsg.description);
//        msg.append("\n");
//        msg.append("extInfo: ");
//        msg.append(obj.extInfo);
//        msg.append("\n");
//        msg.append("filePath: ");
//        msg.append(obj.filePath);
//
//        Intent intent = new Intent(this, ShowFromWXActivity.class);
//        intent.putExtra(Constants.WeiXin.ShowMsgActivity.STitle, wxMsg.title);
//        intent.putExtra(Constants.WeiXin.ShowMsgActivity.SMessage, msg.toString());
//        intent.putExtra(Constants.WeiXin.ShowMsgActivity.BAThumbData, wxMsg.thumbData);
//        startActivity(intent);
        finish();
    }
    private void toast(String s) {
        Log.i("SWJ","EntryActivity : " + s);
        Toast.makeText(this,s,Toast.LENGTH_SHORT).show();
    }
}