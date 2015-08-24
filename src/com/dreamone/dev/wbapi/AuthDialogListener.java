package com.dreamone.dev.wbapi;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;


public class AuthDialogListener implements WeiboAuthListener {
    Context mContext;

    public AuthDialogListener(Context context) {
        mContext = context;
    }
    @Override
    public void onComplete(Bundle values) {
        String token = values.getString("access_token");
        String expires_in = values.getString("expires_in");
        Log.d("SWJ", "AuthDialogListener onComplete token:" + token + "; expire: " + expires_in+"; Keys:"+values.keySet());

        Oauth2AccessToken accessToken = new Oauth2AccessToken(token, expires_in);
        if (accessToken.isSessionValid()) {
            AccessTokenKeeper.writeAccessToken(mContext,
                    accessToken);
            Toast.makeText(mContext, "认证成功", Toast.LENGTH_SHORT)
                    .show();
        }
    }




    @Override
    public void onCancel() {
        Log.e("SWJ", "AuthDialogListener onCancel !!!");
        Toast.makeText(mContext, "Auth cancel",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onWeiboException(WeiboException e) {
        Log.e("SWJ", "AuthDialogListener onWeiboException !!!", e);
        Toast.makeText(mContext,
                "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG)
                .show();
    }

}