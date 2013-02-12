package com.dreamone.dev;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.platform.comapi.basestruct.GeoPoint;

public class BaiduMapActivity extends Activity {
	private static String API_KEY = "0AED3B99F7811527F5E09E1604F294806D06703F";
	BMapManager mBMapMan = null;
	MapView mMapView = null;
	MKMapViewListener mMapListener = null;
	MapController mMapController = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBMapMan = new BMapManager(getApplication());
		mBMapMan.init(API_KEY, new MyGeneralListener());
		// 注意：请在试用setContentView前初始化BMapManager对象，否则会报错
		setContentView(R.layout.baidu_map_activity);
		mMapView = (MapView) findViewById(R.id.bmapsView);
		mMapView.setBuiltInZoomControls(true);
		// 设置启用内置的缩放控件
		mMapController = mMapView.getController();
		// 得到mMapView的控制权,可以用它控制和驱动平移和缩放
		GeoPoint point = new GeoPoint((int) (39.915 * 1E6),
				(int) (116.404 * 1E6));
		// 用给定的经纬度构造一个GeoPoint，单位是微度 (度 * 1E6)
		mMapController.setCenter(point);// 设置地图中心点
		mMapController.setZoom(12);// 设置地图zoom级别
        mMapController.enableClick(true);

        mMapListener = new MKMapViewListener() {
			
			@Override
			public void onMapMoveFinish() {
				Log.d("SWJ","onMapMoveFinish: ");

			}
			
			@Override
			public void onClickMapPoi(MapPoi mapPoiInfo) {
				Log.d("SWJ","onClickMapPoi: ");
				String title = "";
				if (mapPoiInfo != null){
					title = mapPoiInfo.strText;
					int lat = mapPoiInfo.geoPt.getLatitudeE6();
					int lon = mapPoiInfo.geoPt.getLongitudeE6();
					Log.d("SWJ","onClickMapPoi: "+lat+"; "+lon);
					Toast.makeText(BaiduMapActivity.this,title + "; lat:"+lat
							+"; lon:"+lon,Toast.LENGTH_SHORT).show();
					mMapController.animateTo(mapPoiInfo.geoPt);
					Intent data = new Intent();
					data.putExtra(Constants.LATITUDE_KEY, lat);
					data.putExtra(Constants.LONGITUDE_KEY, lon);
					setResult(MainActivity.REQUEST_CODE_SELECT_LOCATION, data);
					finish();
				}
			}
		};
		mMapView.regMapViewListener(mBMapMan, mMapListener);

	}

	@Override
	protected void onDestroy() {
		mMapView.destroy();
		if (mBMapMan != null) {
			mBMapMan.destroy();
			mBMapMan = null;
		}
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		if (mBMapMan != null) {
			mBMapMan.stop();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		if (mBMapMan != null) {
			mBMapMan.start();
		}
		super.onResume();
	}
	// 常用事件监听，用来处理通常的网络错误，授权验证错误等
    private class MyGeneralListener implements MKGeneralListener {
        
        @Override
        public void onGetNetworkState(int iError) {
            if (iError == MKEvent.ERROR_NETWORK_CONNECT) {
                Toast.makeText(BaiduMapActivity.this, "您的网络出错啦！",
                    Toast.LENGTH_LONG).show();
            }
            else if (iError == MKEvent.ERROR_NETWORK_DATA) {
                Toast.makeText(BaiduMapActivity.this, "输入正确的检索条件！",
                        Toast.LENGTH_LONG).show();
            }
            // ...
        }

        @Override
        public void onGetPermissionState(int iError) {
            if (iError ==  MKEvent.ERROR_PERMISSION_DENIED) {
                //授权Key错误：
                Toast.makeText(BaiduMapActivity.this, 
                        "请在 DemoApplication.java文件输入正确的授权Key！", Toast.LENGTH_LONG).show();
            }
        }
    }
}
