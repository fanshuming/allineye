package calvin.com.alleyes;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiBoundSearchOption;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.share.OnGetShareUrlResultListener;
import com.baidu.mapapi.search.share.ShareUrlResult;
import com.baidu.mapapi.search.share.ShareUrlSearch;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * POI检索 1.周边检索 2. 范围检索 3. 城市检索 4.详细检索
 *
 * @author shuming
 */
public class PoiSearchActivity extends Activity implements OnClickListener {

	private MapView mapView;
	private BaiduMap bdMap;
	//
	private PoiSearch poiSearch;

	private ShareUrlSearch shareUrlSearch;

	private EditText editCityEt, editSearchKeyEt;

	// 城市检索，区域检索，周边检索，下一组数据 按钮
	private Button citySearchBtn, boundSearchBtn, nearbySearchBtn, nextDataBtn;

	// 记录检索类型
	private int type;
	// 记录页标
	private int page = 1;
	private int totalPage = 0;

	private double latitude = 39.9361752;
	private double longitude = 116.400244;
	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
	private GoogleApiClient client;
	//new center
	public String myCity;
	private double  myBDLocationLong;
	private double  myBDLocationLati;
	LatLng center;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_poi_search);
		Intent intent = getIntent();
		if(intent != null){
			myCity = intent.getStringExtra("city");
			myBDLocationLong = intent.getDoubleExtra("longitude", 0);
			myBDLocationLati = intent.getDoubleExtra("latitude", 0);
		}
		center = new LatLng(myBDLocationLati, myBDLocationLong);
		init();
		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
	}

	private void init() {
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.showZoomControls(false);
		bdMap = mapView.getMap();
		MapStatus mMapStatus = new MapStatus.Builder()
				.target(center)
				.zoom(18)
				.build();
		//定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
		MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
		//改变地图状态
		bdMap.setMapStatus(mMapStatusUpdate);
		bdMap.setIndoorEnable(true);
		bdMap.setMyLocationEnabled(true);
		// 实例化PoiSearch对象
		poiSearch = PoiSearch.newInstance();
		// 设置检索监听器
		poiSearch.setOnGetPoiSearchResultListener(poiSearchListener);
		editCityEt = (EditText) findViewById(R.id.city);
		editSearchKeyEt = (EditText) findViewById(R.id.searchkey);

		citySearchBtn = (Button) findViewById(R.id.city_search_btn);
		boundSearchBtn = (Button) findViewById(R.id.bound_search_btn);
		nearbySearchBtn = (Button) findViewById(R.id.nearby_search_btn);
		nextDataBtn = (Button) findViewById(R.id.next_data_btn);
		nextDataBtn.setEnabled(false);
		citySearchBtn.setOnClickListener(this);
		boundSearchBtn.setOnClickListener(this);
		nearbySearchBtn.setOnClickListener(this);
		nextDataBtn.setOnClickListener(this);

		editSearchKeyEt.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
									  int count) {
				citySearchBtn.setEnabled(true);
				boundSearchBtn.setEnabled(true);
				nearbySearchBtn.setEnabled(true);
				nextDataBtn.setEnabled(false);
				page = 1;
				totalPage = 0;
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
										  int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		shareUrlSearch = ShareUrlSearch.newInstance();

	}

	/**
	 *
	 */
	OnGetPoiSearchResultListener poiSearchListener = new OnGetPoiSearchResultListener() {
	@Override
	public void onGetPoiResult(PoiResult poiResult) {
		if (poiResult == null
				|| poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {// 没有找到检索结果
			Toast.makeText(PoiSearchActivity.this, "未找到结果",
					Toast.LENGTH_LONG).show();
			return;
		}

		if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) {// 检索结果正常返回
			bdMap.clear();
			MyPoiOverlay poiOverlay = new MyPoiOverlay(bdMap);
			poiOverlay.setData(poiResult);// 设置POI数据
			bdMap.setOnMarkerClickListener(poiOverlay);
			poiOverlay.addToMap();// 将所有的overlay添加到地图上
			poiOverlay.zoomToSpan();
			//
			totalPage = poiResult.getTotalPageNum();// 获取总分页数
			Toast.makeText(
					PoiSearchActivity.this,
					"总共查到" + poiResult.getTotalPoiNum() + "个兴趣点, 分为"
							+ totalPage + "页", Toast.LENGTH_SHORT).show();

		}
	}

	@Override
	public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
		if (poiDetailResult.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(PoiSearchActivity.this, "抱歉，未找到结果",
					Toast.LENGTH_SHORT).show();
		} else {// 正常返回结果的时候，此处可以获得很多相关信息
			Toast.makeText(
					PoiSearchActivity.this,
					poiDetailResult.getName() + ": "
							+ poiDetailResult.getAddress(),
					Toast.LENGTH_LONG).show();
		}
	}
		@Override
		public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
			return;
		}
	};

	/**
	 * 短串检索监听器
	 */
	OnGetShareUrlResultListener shareUrlResultListener = new OnGetShareUrlResultListener() {
		@Override
		public void onGetPoiDetailShareUrlResult(ShareUrlResult shareUrlResult) {

		}

		@Override
		public void onGetLocationShareUrlResult(ShareUrlResult shareUrlResult) {

		}

		@Override
		public void onGetRouteShareUrlResult(ShareUrlResult shareUrlResult) {

		}
	};

	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
	public Action getIndexApiAction() {
		Thing object = new Thing.Builder()
				.setName("PoiSearch Page") // TODO: Define a title for the content shown.
				// TODO: Make sure this auto-generated URL is correct.
				.setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
				.build();
		return new Action.Builder(Action.TYPE_VIEW)
				.setObject(object)
				.setActionStatus(Action.STATUS_TYPE_COMPLETED)
				.build();
	}

	@Override
	public void onStart() {
		super.onStart();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client.connect();
		AppIndex.AppIndexApi.start(client, getIndexApiAction());
	}

	@Override
	public void onStop() {
		super.onStop();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		AppIndex.AppIndexApi.end(client, getIndexApiAction());
		client.disconnect();
	}

	class MyPoiOverlay extends PoiOverlay {

		public MyPoiOverlay(BaiduMap arg0) {
			super(arg0);
		}

		@Override
		public boolean onPoiClick(int arg0) {
			super.onPoiClick(arg0);
			PoiInfo poiInfo = getPoiResult().getAllPoi().get(arg0);
			poiSearch.searchPoiDetail(new PoiDetailSearchOption()
					.poiUid(poiInfo.uid));
			return true;
		}

	}

	/**
	 * 城市内搜索
	 */
	private void citySearch(int page) {
		// 设置检索参数
		PoiCitySearchOption citySearchOption = new PoiCitySearchOption();
		citySearchOption.city(editCityEt.getText().toString());// 城市
		citySearchOption.keyword(editSearchKeyEt.getText().toString());// 关键字
		citySearchOption.pageCapacity(15);// 默认每页10条
		citySearchOption.pageNum(page);// 分页编号
		// 发起检索请求
		poiSearch.searchInCity(citySearchOption);
	}

	/**
	 * 范围检索
	 */
	private void boundSearch(int page) {
		PoiBoundSearchOption boundSearchOption = new PoiBoundSearchOption();
		LatLng southwest = new LatLng(latitude - 0.01, longitude - 0.012);// 西南
		LatLng northeast = new LatLng(latitude + 0.01, longitude + 0.012);// 东北
		LatLngBounds bounds = new LatLngBounds.Builder().include(southwest)
				.include(northeast).build();// 得到一个地理范围对象
		boundSearchOption.bound(bounds);// 设置poi检索范围
		boundSearchOption.keyword(editSearchKeyEt.getText().toString());// 检索关键字
		boundSearchOption.pageNum(page);
		poiSearch.searchInBound(boundSearchOption);// 发起poi范围检索请求
	}

	/**
	 * 附近检索
	 */
	private void nearbySearch(int page) {
		PoiNearbySearchOption nearbySearchOption = new PoiNearbySearchOption();
		nearbySearchOption.location(new LatLng(latitude, longitude));
		nearbySearchOption.keyword(editSearchKeyEt.getText().toString());
		nearbySearchOption.radius(1000);// 检索半径，单位是米
		nearbySearchOption.pageNum(page);
		poiSearch.searchNearby(nearbySearchOption);// 发起附近检索请求
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.city_search_btn:
				type = 0;
				page = 1;
				citySearchBtn.setEnabled(false);
				boundSearchBtn.setEnabled(true);
				nearbySearchBtn.setEnabled(true);
				nextDataBtn.setEnabled(true);
				bdMap.clear();
				citySearch(page);
				break;
			case R.id.bound_search_btn:
				type = 1;
				page = 1;
				citySearchBtn.setEnabled(true);
				boundSearchBtn.setEnabled(false);
				nearbySearchBtn.setEnabled(true);
				nextDataBtn.setEnabled(true);
				bdMap.clear();
				boundSearch(page);
				break;
			case R.id.nearby_search_btn:
				type = 2;
				page = 1;
				citySearchBtn.setEnabled(true);
				boundSearchBtn.setEnabled(true);
				nearbySearchBtn.setEnabled(false);
				nextDataBtn.setEnabled(true);
				bdMap.clear();
				nearbySearch(page);
				break;
			case R.id.next_data_btn:
				switch (type) {
					case 0:
						if (++page <= totalPage) {
							citySearch(page);
						} else {
							Toast.makeText(PoiSearchActivity.this, "已经查到了最后一页~",
									Toast.LENGTH_SHORT).show();
						}
						break;
					case 1:
						if (++page <= totalPage) {
							boundSearch(page);
						} else {
							Toast.makeText(PoiSearchActivity.this, "已经查到了最后一页~",
									Toast.LENGTH_SHORT).show();
						}
						break;
					case 2:
						if (++page <= totalPage) {
							nearbySearch(page);
						} else {
							Toast.makeText(PoiSearchActivity.this, "已经查到了最后一页~",
									Toast.LENGTH_SHORT).show();
						}
						break;
				}
				break;
			default:
				break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		poiSearch.destroy();// 释放poi检索对象
		mapView.onDestroy();
	}

}
