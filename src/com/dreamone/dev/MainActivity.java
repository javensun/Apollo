package com.dreamone.dev;

import java.io.IOException;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.keep.AccessTokenKeeper;
import com.weibo.sdk.android.net.RequestListener;
import com.weibo.sdk.android.sso.SsoHandler;
import com.weibo.sdk.android.util.Utility;
/**
 * 
 * @author liyan (liyan9@staff.sina.com.cn)
 */
public class MainActivity extends Activity {

    private Weibo mWeibo;
    private static final String CONSUMER_KEY = "966056985";//掌中新浪
    private static final String REDIRECT_URL = "http://www.sina.com";
//    private static final String CONSUMER_KEY = "783190658";//Acer平板电脑, Not Work.
//    private static final String REDIRECT_URL = "http://www.acer.com.cn";//Acer平板电脑
//    private static final String CONSUMER_KEY = "1060964734";// 替换为开发者的appkey，例如"1646212860";
    private Button authBtn, ssoBtn, cancelBtn;
    private TextView mText;
    private Button mUpdateBtn, mAddPicBtn, mSelectLocBtn, mTestEditActivityBtn;
    private EditText contentText, lonText, latText;
    private ImageView mPictureView;
	private String mPicPath;
	private String mLatitude, mLongitude;
    public static final int REQUEST_CODE_PICK_PICTURE = 0;
    public static final int REQUEST_CODE_SELECT_LOCATION = 1;
    public static final int UPDATE_SUCCESS = 0;
    
    private Handler mUIHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case UPDATE_SUCCESS: 
                    Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };
    
    private RequestListener updateListner = new RequestListener() {

		@Override
		public void onComplete(String response) {
			// TODO Auto-generated method stub
			Log.d("SWJ","updateListner onComplete repsonse:"+response);
			mUIHandler.sendEmptyMessage(UPDATE_SUCCESS);
		}

		@Override
		public void onIOException(IOException e) {
			// TODO Auto-generated method stub
			Log.e("SWJ","updateListner onIOException !!!",e);
			
		}

		@Override
		public void onError(WeiboException e) {
			// TODO Auto-generated method stub
			Log.e("SWJ","updateListner onError !!!",e);
			
		}
    	
    };
    public static Oauth2AccessToken accessToken;
    public static final String TAG = "sinasdk";
    /**
     * SsoHandler 仅当sdk支持sso时有效，
     */
    SsoHandler mSsoHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        mWeibo = Weibo.getInstance(CONSUMER_KEY, REDIRECT_URL);

        authBtn = (Button) findViewById(R.id.auth);
        authBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mWeibo.authorize(MainActivity.this, new AuthDialogListener());
            }
        });
        ssoBtn = (Button) findViewById(R.id.sso);// 触发sso的按钮
        try {
            Class sso = Class.forName("com.weibo.sdk.android.sso.SsoHandler");
            ssoBtn.setVisibility(View.VISIBLE);
        } catch (ClassNotFoundException e) {
            Log.i(TAG, "com.weibo.sdk.android.sso.SsoHandler not found");

        }
        ssoBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * 下面两个注释掉的代码，仅当sdk支持sso时有效，
                 */

                mSsoHandler = new SsoHandler(MainActivity.this, mWeibo);
                mSsoHandler.authorize(new AuthDialogListener());
            }
        });
        cancelBtn = (Button) findViewById(R.id.apiCancel);
        cancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AccessTokenKeeper.clear(MainActivity.this);
                authBtn.setVisibility(View.VISIBLE);
                ssoBtn.setVisibility(View.VISIBLE);
                cancelBtn.setVisibility(View.INVISIBLE);
                mText.setText("");
            }
        });
        contentText = (EditText) findViewById(R.id.weibo_content);
        latText = (EditText) findViewById(R.id.latitude);
        lonText = (EditText) findViewById(R.id.longitude);
        mUpdateBtn = (Button) findViewById(R.id.update);
        mUpdateBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String content = contentText.getText().toString();
//				String lat = latText.getText().toString();
//				String lon = lonText.getText().toString();
				String lat = String.valueOf(mLatitude);
				String lon = String.valueOf(mLongitude);
				Log.d("SWJ","weibo content = "+content+"; lat="+lat+"; lon="+lon);
				if(TextUtils.isEmpty(mPicPath)) {
					new StatusesAPI(accessToken).update(content, lat, lon, updateListner);
				} else {
					new StatusesAPI(accessToken).upload(content, mPicPath, lat, lon, updateListner);
				}
			}
        	
        });
        mAddPicBtn = (Button) findViewById(R.id.add_pic);
        mAddPicBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent (Intent.ACTION_PICK);
				intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
				startActivityForResult(intent, REQUEST_CODE_PICK_PICTURE);
			}
        	
        });

        mSelectLocBtn = (Button) findViewById(R.id.btn_select_location);
        mSelectLocBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent ();
				intent.setClass(getApplicationContext(), BaiduMapActivity.class);
				startActivityForResult(intent, REQUEST_CODE_SELECT_LOCATION);
			}
		});
        mText = (TextView) findViewById(R.id.show);
        mPictureView = (ImageView) findViewById(R.id.picture_view);

        MainActivity.accessToken = AccessTokenKeeper.readAccessToken(this);
        if (MainActivity.accessToken.isSessionValid()) {
            Weibo.isWifi = Utility.isWifi(this);
            try {
                Class sso = Class.forName("com.weibo.sdk.android.api.WeiboAPI");// 如果支持weiboapi的话，显示api功能演示入口按钮
//                apiBtn.setVisibility(View.VISIBLE);
            } catch (ClassNotFoundException e) {
                // e.printStackTrace();
                Log.i(TAG, "com.weibo.sdk.android.api.WeiboAPI not found");

            }
            authBtn.setVisibility(View.INVISIBLE);
            ssoBtn.setVisibility(View.INVISIBLE);
            cancelBtn.setVisibility(View.VISIBLE);
            String date = new java.text.SimpleDateFormat("yyyy/MM/dd hh:mm:ss")
                    .format(new java.util.Date(MainActivity.accessToken
                            .getExpiresTime()));
            mText.setText("access_token 仍在有效期内,无需再次登录: \naccess_token:"
                    + MainActivity.accessToken.getToken() + "\n有效期：" + date);
        } else {
            mText.setText("使用SSO登录前，请检查手机上是否已经安装新浪微博客户端，目前仅3.0.0及以上微博客户端版本支持SSO；如果未安装，将自动转为Oauth2.0进行认证");
        }

    }

    private void initViews() {
    	mTestEditActivityBtn = (Button) findViewById(R.id.TestEditActivity);
    	mTestEditActivityBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent("com.dreamone.action.test_edit_activity");
				startActivity(intent);
			}
    	});
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    class AuthDialogListener implements WeiboAuthListener {

        @Override
        public void onComplete(Bundle values) {
			Log.d("SWJ","AuthDialogListener onComplete values:" +values.toString());
            String token = values.getString("access_token");
            String expires_in = values.getString("expires_in");
            MainActivity.accessToken = new Oauth2AccessToken(token, expires_in);
            if (MainActivity.accessToken.isSessionValid()) {
                String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                        .format(new java.util.Date(MainActivity.accessToken
                                .getExpiresTime()));
                mText.setText("认证成功: \r\n access_token: " + token + "\r\n"
                        + "expires_in: " + expires_in + "\r\n有效期：" + date);
                try {
                    Class sso = Class
                            .forName("com.weibo.sdk.android.api.WeiboAPI");// 如果支持weiboapi的话，显示api功能演示入口按钮
//                    apiBtn.setVisibility(View.VISIBLE);
                } catch (ClassNotFoundException e) {
                    // e.printStackTrace();
                    Log.i(TAG, "com.weibo.sdk.android.api.WeiboAPI not found");

                }
                cancelBtn.setVisibility(View.VISIBLE);
                AccessTokenKeeper.keepAccessToken(MainActivity.this,
                        accessToken);
                Toast.makeText(MainActivity.this, "认证成功", Toast.LENGTH_SHORT)
                        .show();
            }
        }

        @Override
        public void onError(WeiboDialogError e) {
			Log.e("SWJ","AuthDialogListener onError !!!",e);
            Toast.makeText(getApplicationContext(),
                    "Auth error : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCancel() {
			Log.e("SWJ","AuthDialogListener onCancel !!!");
            Toast.makeText(getApplicationContext(), "Auth cancel",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onWeiboException(WeiboException e) {
        	Log.e("SWJ","AuthDialogListener onWeiboException !!!",e);
            Toast.makeText(getApplicationContext(),
                    "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG)
                    .show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null) {
        	Log.d("SWJ","onActivityResult: requestCode="+requestCode+"; resultCode="+resultCode
        			+"data.getData()="+data.getData());
        	if(REQUEST_CODE_PICK_PICTURE == requestCode) {
        		Uri picUri = data.getData();
        		mPictureView.setImageURI(picUri);
        		String[] proj = {MediaStore.Images.ImageColumns.DATA};
        		Cursor c = this.getContentResolver().query(picUri, proj, null, null, null);
        		if(c!=null) {
        			c.moveToFirst();
        			int column_index = c.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA);
        			mPicPath=c.getString(column_index);
        			c.close();
        		}
        	} else if ( REQUEST_CODE_SELECT_LOCATION == requestCode){
        		double lat = data.getIntExtra(Constants.LATITUDE_KEY, 0);
        		double lon =data.getIntExtra(Constants.LONGITUDE_KEY, 0);
            	Log.e(TAG,"onActivityResult got lat:"+lat/1000000+"; lon: "+lon/1000000);
        		mLatitude = String.valueOf(lat/1000000);
        		mLongitude = String.valueOf(lon/1000000);
        		latText.setText(String.valueOf(mLatitude));
        		lonText.setText(String.valueOf(mLongitude));
        	}
        } else {
        	Log.e(TAG,"onActivityResult got null!!!");
        }
        /**
         * 下面两个注释掉的代码，仅当sdk支持sso时有效，
         */
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }

}
