package calvin.com.alleyes;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.BDNotifyListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

/**
 * 程序启动引导页，选择不同的功能进入不同的界面
 *
 * @author shuming
 *
 */
public class LaunchActivity extends Activity {

	private ListView listview;
	private MapView mapview;

	private LocationClient locationClient;
	private BDLocationListener locationListener;

	private double longitude;// 精度
	private double latitude;// 维度
	private float radius;// 定位精度半径，单位是米
	private String addrStr;// 反地理编码
	private String province;// 省份信息
	private String city;// 城市信息
	private String district;// 区县信息
	private float direction;// 手机方向信息
	private int locType;
	private String Phonenumber;
	// 定位模式 （普通-跟随-罗盘）
	private MyLocationConfiguration.LocationMode currentMode;
	// 定位图标描述
	private static final String TAG = LaunchActivity.class.getSimpleName();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launch);

		Intent intent = getIntent();
		if(intent != null){
            Phonenumber = intent.getStringExtra("Phone");
		}

		if ( Build.VERSION.SDK_INT >= 23) {
			if (!(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
				if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
					Toast.makeText(this, "grant the permission", Toast.LENGTH_LONG).show();
				}
				requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
			}
		}
		listview = (ListView) findViewById(R.id.activity_listview);
		currentMode = MyLocationConfiguration.LocationMode.NORMAL;

		initLocation();
		init();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if(requestCode == 1){
			int grantResult = grantResults[0];
			boolean granted = (grantResult == PackageManager.PERMISSION_GRANTED);
			Log.d(TAG,"granted"+granted);

		}
	}

	/**
	 * 初始化listview列表
	 */
	private void init() {
		final Class[] clazz = { POI_Suggestion.class,BasisMapActivity.class,
				AddOverlayActivity.class, MapControllActivity.class,
				BusLineSearchActivity.class, IndoorLocationActivity.class,
				ShareActivity.class,RadarDemo.class,MainActivity.class,SearchSameApp.class};
		String arr[] = { "千里眼","地图图层展示", "添加覆盖物", "地图控制 ","公交线路查询",
				"定位","分享位置信息","雷达","退出","找同城"};
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, arr);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				Intent intent = new Intent(LaunchActivity.this,  clazz[position]);
				Bundle bundle = new Bundle();

				bundle.putDouble("longitude",longitude);
				bundle.putDouble("latitude",latitude);
				bundle.putString("city", city);
				bundle.putString("inputEmail",Phonenumber);

				intent.putExtras(bundle);
				startActivity(intent);
				//startActivity(new Intent(LaunchActivity.this, clazz[position]));
			}
		});
	}
	private void initLocation() {
		// 1. 初始化LocationClient类
		locationClient = new LocationClient(getApplicationContext());
		// 2. 声明LocationListener类
		locationListener = new LaunchActivity.MyLocationListener();
		// 3. 注册监听函数
		locationClient.registerLocationListener(locationListener);
		// 4. 设置参数
		LocationClientOption locOption = new LocationClientOption();
		locOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
		locOption.setCoorType("bd09ll");// 设置定位结果类型
		locOption.setScanSpan(5000);// 设置发起定位请求的间隔时间,ms
		locOption.setIsNeedAddress(true);// 返回的定位结果包含地址信息
		locOption.setNeedDeviceDirect(true);// 设置返回结果包含手机的方向

		locationClient.setLocOption(locOption);
		// 6. 开启/关闭 定位SDK
		locationClient.start();
		// locationClient.stop();
		// 发起定位，异步获取当前位置，因为是异步的，所以立即返回，不会引起阻塞
		// 定位的结果在ReceiveListener的方法onReceive方法的参数中返回。
		// 当定位SDK从定位依据判定，位置和上一次没发生变化，而且上一次定位结果可用时，则不会发生网络请求，而是返回上一次的定位结果。
		// 返回值，0：正常发起了定位 1：service没有启动 2：没有监听函数
		// 6：两次请求时间太短（前后两次请求定位时间间隔不能小于1000ms）
		/*
		 * if (locationClient != null && locationClient.isStarted()) {
		 * requestResult = locationClient.requestLocation(); } else {
		 * Log.d("LocSDK5", "locClient is null or not started"); }
		 */

	}

	/**
	 *
	 * @author ys
	 *
	 */
	class MyLocationListener implements BDLocationListener {
		// 异步返回的定位结果
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null) {
				return;
			}
			locType = location.getLocType();
			//Toast.makeText(LaunchActivity.this, "当前定位的返回值是："+locType, Toast.LENGTH_SHORT).show();
			longitude = location.getLongitude();
			latitude = location.getLatitude();
			if (location.hasRadius()) {// 判断是否有定位精度半径
				radius = location.getRadius();
			}
			if (locType == BDLocation.TypeGpsLocation) {//
				/*Toast.makeText(
						LaunchActivity.this,
						"当前速度是：" + location.getSpeed() + "~~定位使用卫星数量："
								+ location.getSatelliteNumber(),
						Toast.LENGTH_SHORT).show();*/
			} else if (locType == BDLocation.TypeNetWorkLocation) {
				addrStr = location.getAddrStr();// 获取反地理编码(文字描述的地址)
				/*Toast.makeText(LaunchActivity.this, addrStr,
						Toast.LENGTH_SHORT).show();*/
			}
			direction = location.getDirection();// 获取手机方向，【0~360°】,手机上面正面朝北为0°
			province = location.getProvince();// 省份
			city = location.getCity();// 城市
			district = location.getDistrict();// 区县
			latitude = location.getLatitude();
			longitude = location.getLongitude();
			/*Toast.makeText(LaunchActivity.this,
					city + "~" + latitude + "~" + longitude, Toast.LENGTH_LONG)
					.show();*/
		}
		@Override
		public void onConnectHotSpotMessage(String s, int i) {
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		locationClient.unRegisterLocationListener(locationListener);
		locationClient.stop();
	}
}

