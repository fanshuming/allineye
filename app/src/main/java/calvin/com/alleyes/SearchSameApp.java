package calvin.com.alleyes;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.GroundOverlayOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.radar.RadarNearbyResult;
import com.baidu.mapapi.radar.RadarNearbySearchOption;
import com.baidu.mapapi.radar.RadarSearchError;
import com.baidu.mapapi.radar.RadarSearchListener;
import com.baidu.mapapi.radar.RadarSearchManager;
import com.baidu.mapapi.radar.RadarUploadInfo;
import com.baidu.mapapi.radar.RadarUploadInfoCallback;
import com.baidu.mapapi.search.core.CityInfo;
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
import com.baidu.mapapi.search.poi.PoiSortType;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2017/7/24.
 */

public class SearchSameApp extends FragmentActivity implements
        OnGetPoiSearchResultListener, OnGetSuggestionResultListener,BDLocationListener,  RadarUploadInfoCallback,
        RadarSearchListener, BaiduMap.OnMarkerClickListener, BaiduMap.OnMapClickListener {

        private PoiSearch mPoiSearch = null;
        private SuggestionSearch mSuggestionSearch = null;
        private BaiduMap mBaiduMap = null;
        private List<String> suggest;
        /**
         * 搜索关键字输入窗口
         */
        private LocationClient mLocationClient = null;
        public String myCity;
        private EditText editCity = null;
        private AutoCompleteTextView keyWorldsView = null;
        private ArrayAdapter<String> sugAdapter = null;
        private int loadIndex = 0;
        private double  myBDLocationLong;
        private double  myBDLocationLati;

        int radius = 100;
        LatLng center;
        LatLng southwest;
        LatLng northeast;
        LatLngBounds searchbound;
    // 定位相关
    LocationClient mLocClient;
    private int pageIndex = 0;
    private int curPage = 0;
    private int totalPage = 0;
    private LatLng pt = null;
    // 周边雷达相关
    RadarNearbyResult listResult = null;
    ListView mResultListView = null;
    private String userID = "101";
    private String userComment = "what is your name";
    private String inputEmail;
    private boolean uploadAuto = false;
    PoiResult presult = null;
    private TextView popupText = null; // 泡泡view
    BitmapDescriptor ff8 = BitmapDescriptorFactory.fromResource(R.drawable.eyeman);
        /*
        LatLng center = new LatLng(39.92235, 116.380338);
        LatLng southwest = new LatLng(39.92235, 116.380338);
        LatLng northeast = new LatLng(39.947246, 116.414977);
        LatLngBounds searchbound = new LatLngBounds.Builder().include(southwest).include(northeast).build();
    */
        int searchType = 0;  // 搜索的类型，在显示时区分
        /**
         * ATTENTION: This was auto-generated to implement the App Indexing API.
         * See https://g.co/AppIndexing/AndroidStudio for more information.
         */
        private GoogleApiClient client;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_poisearch);
            Intent intent = getIntent();
            if(intent != null){
                myCity = intent.getStringExtra("city");
                myBDLocationLong = intent.getDoubleExtra("longitude", 0);
                myBDLocationLati = intent.getDoubleExtra("latitude", 0);
                inputEmail = intent.getStringExtra("inputEmail");
            }
            center = new LatLng(myBDLocationLati, myBDLocationLong);
            southwest = new LatLng(myBDLocationLati, myBDLocationLong);
            northeast = new LatLng((myBDLocationLati+0.02), (myBDLocationLong + 0.03));
            searchbound = new LatLngBounds.Builder().include(southwest).include(northeast).build();
        /*Toast.makeText(POI_Suggestion.this,
                myCity + "~" + myBDLocationLati + "~" + myBDLocationLong, Toast.LENGTH_LONG)
                .show();*/
            //initLocation();
            // 初始化搜索模块，注册搜索事件监听
            mPoiSearch = PoiSearch.newInstance();
            mPoiSearch.setOnGetPoiSearchResultListener(this);

            // 初始化建议搜索模块，注册建议搜索事件监听
            mSuggestionSearch = SuggestionSearch.newInstance();
            mSuggestionSearch.setOnGetSuggestionResultListener(this);


            // 周边雷达设置监听
            RadarSearchManager.getInstance().addNearbyInfoListener(this);
            // 周边雷达设置用户，id为空默认是设备标识
            RadarSearchManager.getInstance().setUserID(userID);


            // first upload myself position

            RadarUploadInfo info = new RadarUploadInfo();
            info.comments = userComment;
            info.pt = center;

            RadarSearchManager.getInstance().uploadInfoRequest(info);

            editCity = (EditText) findViewById(R.id.city);
            editCity.setText(myCity);
            keyWorldsView = (AutoCompleteTextView) findViewById(R.id.searchkey);
            sugAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_dropdown_item_1line);
            keyWorldsView.setAdapter(sugAdapter);
            keyWorldsView.setThreshold(1);
            mBaiduMap = ((SupportMapFragment) (getSupportFragmentManager()
                    .findFragmentById(R.id.map))).getBaiduMap();

            MapStatus mMapStatus = new MapStatus.Builder().target(center).zoom(18).build();
            //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
            MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
            //改变地图状态

            mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {

                @Override
                public boolean onMarkerClick(Marker arg0) {
                    // TODO Auto-generated method stub
                    //Toast.makeText(getApplicationContext(), "Marker被点击了！", Toast.LENGTH_SHORT).show();
                    mBaiduMap.hideInfoWindow();
                    if (arg0 != null) {
                        popupText = new TextView(SearchSameApp.this);
                        popupText.setBackgroundResource(R.drawable.popup);
                        popupText.setTextColor(0xFF000000);

                        popupText.setText(arg0.getExtraInfo().getString("des") + arg0.getTitle() + inputEmail);
                        mBaiduMap.showInfoWindow(new InfoWindow(popupText, arg0.getPosition(), -47));
                        MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(arg0.getPosition());
                        mBaiduMap.setMapStatus(update);
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            mBaiduMap.setMapStatus(mMapStatusUpdate);
            mBaiduMap.setIndoorEnable(true);
            mBaiduMap.setMyLocationEnabled(true);
            // searchButtonProcess(null);

            /**
             * 当输入关键字变化时，动态更新建议列表
             */
            keyWorldsView.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable arg0) {

                }

                @Override
                public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

                }

                @Override
                public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                    if (cs.length() <= 0) {
                        return;
                    }

                    /**
                     * 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
                     */
                    mSuggestionSearch.requestSuggestion((new SuggestionSearchOption()).keyword(cs.toString()).city(editCity.getText().toString()));
                }
            });

            // ATTENTION: This was auto-generated to implement the App Indexing API.
            // See https://g.co/AppIndexing/AndroidStudio for more information.
            client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        }

        @Override
        protected void onPause() {
            super.onPause();
        }

        @Override
        protected void onResume() {
            super.onResume();
        }

        @Override
        protected void onDestroy() {
            mPoiSearch.destroy();
            mSuggestionSearch.destroy();
            RadarSearchManager.getInstance().removeNearbyInfoListener(this);
            RadarSearchManager.getInstance().clearUserInfo();
            RadarSearchManager.getInstance().destroy();
            // 释放地图
            ff8.recycle();
            //mMapView.onDestroy();
            mBaiduMap = null;
            super.onDestroy();
        }

        @Override
        protected void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
        }

        @Override
        protected void onRestoreInstanceState(Bundle savedInstanceState) {
            super.onRestoreInstanceState(savedInstanceState);
        }

        /**
         * 响应城市内搜索按钮点击事件
         *
         * @param v
         */
        public void searchButtonProcess(View v) {
            searchType = 1;
            String citystr = editCity.getText().toString();
            String keystr = keyWorldsView.getText().toString();
            mPoiSearch.searchInCity((new PoiCitySearchOption()).city(citystr).keyword(keystr).pageNum(loadIndex));
        }

        /**
         * 响应周边搜索按钮点击事件
         *
         * @param v
         */
        public void searchNearbyProcess(View v) {
            searchType = 2;
            PoiNearbySearchOption nearbySearchOption = new PoiNearbySearchOption().keyword(keyWorldsView.getText()
                    .toString()).sortType(PoiSortType.distance_from_near_to_far).location(center)
                    .radius(radius).pageNum(loadIndex);
            mPoiSearch.searchNearby(nearbySearchOption);
        }

        public void goToNextPage(View v) {
            loadIndex++;
            searchButtonProcess(null);
        }

        /**
         * 响应区域搜索按钮点击事件
         *
         * @param v
         */
        public void searchBoundProcess(View v) {
            searchType = 3;
            mPoiSearch.searchInBound(new PoiBoundSearchOption().bound(searchbound).keyword(keyWorldsView.getText().toString()));
        }


        /**
         * 获取POI搜索结果，包括searchInCity，searchNearby，searchInBound返回的搜索结果
         *
         * @param result
         */
        public void onGetPoiResult(PoiResult result) {
            if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
                Toast.makeText(calvin.com.alleyes.SearchSameApp.this, "未找到结果", Toast.LENGTH_LONG).show();
                return;
            }
            if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                mBaiduMap.clear();
                PoiOverlay overlay = new calvin.com.alleyes.SearchSameApp.MyPoiOverlay(mBaiduMap);
                mBaiduMap.setOnMarkerClickListener(overlay);
                presult = result;
                overlay.setData(result);
                overlay.addToMap();
                overlay.zoomToSpan();

                switch (searchType) {
                    case 2:
                        showNearbyArea(center, radius);
                        break;
                    case 3:
                        showBound(searchbound);
                        break;
                    default:
                        break;
                }

                return;
            }
            if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {

                // 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
                String strInfo = "在";
                for (CityInfo cityInfo : result.getSuggestCityList()) {
                    strInfo += cityInfo.city;
                    strInfo += ",";
                }
                strInfo += "找到结果";
                Toast.makeText(calvin.com.alleyes.SearchSameApp.this, strInfo, Toast.LENGTH_LONG).show();
            }
        }

        /**
         * 获取POI详情搜索结果，得到searchPoiDetail返回的搜索结果
         *
         * @param result
         */
        public void onGetPoiDetailResult(PoiDetailResult result) {
            if (result.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(calvin.com.alleyes.SearchSameApp.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(calvin.com.alleyes.SearchSameApp.this, result.getName() + ": " + result.getAddress(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

        }

        /**
         * 获取在线建议搜索结果，得到requestSuggestion返回的搜索结果
         *
         * @param res
         */
        @Override
        public void onGetSuggestionResult(SuggestionResult res) {
            if (res == null || res.getAllSuggestions() == null) {
                return;
            }
            suggest = new ArrayList<String>();
            for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
                if (info.key != null) {
                    suggest.add(info.key);
                }
            }
            sugAdapter = new ArrayAdapter<String>(calvin.com.alleyes.SearchSameApp.this, android.R.layout.simple_dropdown_item_1line, suggest);
            keyWorldsView.setAdapter(sugAdapter);
            sugAdapter.notifyDataSetChanged();
        }

        /**
         * ATTENTION: This was auto-generated to implement the App Indexing API.
         * See https://g.co/AppIndexing/AndroidStudio for more information.
         */
        public Action getIndexApiAction() {
            Thing object = new Thing.Builder()
                    .setName("POI_Suggestion Page") // TODO: Define a title for the content shown.
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

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            Toast.makeText(SearchSameApp.this, "onReceiveLocation", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }

    @Override
    public void onMapClick(LatLng latLng) {
        pt = latLng;
        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(100)
                // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(100).latitude(pt.latitude)
                .longitude(pt.latitude).build();
        if (mBaiduMap != null) {
            mBaiduMap.setMyLocationData(locData);
        }
        RadarNearbySearchOption option = new RadarNearbySearchOption()
                .centerPt(pt).pageNum(pageIndex).radius(2000).pageCapacity(11);
        RadarSearchManager.getInstance().nearbyInfoRequest(option);
        Toast.makeText(SearchSameApp.this, "onMapClick"+latLng.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMapPoiClick(MapPoi mapPoi) {
        Toast.makeText(SearchSameApp.this, "onMapPoiClick"+mapPoi.toString(), Toast.LENGTH_LONG).show();
        return false;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(SearchSameApp.this, "onMarkerClick"+marker.toString(), Toast.LENGTH_LONG).show();
        return false;
    }
    /**
     * 更新结果地图
     *
     * @param res
     */
    public void parseResultToMap(RadarNearbyResult res) {
        mBaiduMap.clear();
        if (res != null && res.infoList != null && res.infoList.size() > 0) {
            for (int i = 0; i < res.infoList.size(); i++) {
                MarkerOptions option = new MarkerOptions().icon(ff8).position(res.infoList.get(i).pt);
                option.title("what can I do for you? connect me,please.").position(res.infoList.get(i).pt);

                Bundle des = new Bundle();
                if (res.infoList.get(i).comments == null || res.infoList.get(i).comments.equals("")) {
                    des.putString("des", "没有备注");
                } else {
                    des.putString("des", res.infoList.get(i).comments);
                }
                option.extraInfo(des);

                mBaiduMap.addOverlay(option);
            }
        }

    }
    @Override
    public void onGetNearbyInfoList(RadarNearbyResult radarNearbyResult, RadarSearchError radarSearchError) {
        // TODO Auto-generated method stub
        if (radarSearchError == RadarSearchError.RADAR_NO_ERROR) {
            Toast.makeText(SearchSameApp.this, "查询周边成功"+radarNearbyResult.toString(), Toast.LENGTH_LONG)
                    .show();
            // 获取成功
            listResult = radarNearbyResult;
            curPage = radarNearbyResult.pageIndex;
            totalPage = radarNearbyResult.pageNum;
            // 处理数据
           // parseResultToList(listResult);
            parseResultToMap(listResult);
        }else if(radarSearchError == RadarSearchError.RADAR_NO_RESULT){
            Toast.makeText(SearchSameApp.this, "周围没有使用此APP的人！"+radarNearbyResult.toString(), Toast.LENGTH_LONG).show();
        }else {
            // 获取失败
            curPage = 0;
            totalPage = 0;
            Toast.makeText(SearchSameApp.this, "查询失败" + radarSearchError.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onGetUploadState(RadarSearchError radarSearchError) {

        // TODO Auto-generated method stub
        if (radarSearchError == RadarSearchError.RADAR_NO_ERROR) {
            // 上传成功
            if (!uploadAuto) {
                Toast.makeText(SearchSameApp.this, "单次上传位置成功", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(SearchSameApp.this, "连续上传位置成功", Toast.LENGTH_LONG).show();
            }
            RadarNearbySearchOption option = new RadarNearbySearchOption().centerPt(pt).pageNum(pageIndex).radius(2000).pageCapacity(11);
            RadarSearchManager.getInstance().nearbyInfoRequest(option);
        } else {
            // 上传失败
            if (uploadAuto) {
                Toast.makeText(SearchSameApp.this, "单次上传位置失败", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(SearchSameApp.this, "连续上传位置失败", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onGetClearInfoState(RadarSearchError radarSearchError) {

    }

    @Override
    public RadarUploadInfo onUploadInfoCallback() {
        // TODO Auto-generated method stub
        RadarUploadInfo info = new RadarUploadInfo();
        info.comments = userComment;
        info.pt = pt;
        Log.e("hjtest", "OnUploadInfoCallback");
        return info;
    }

    private class MyPoiOverlay extends PoiOverlay {
            public MyPoiOverlay(BaiduMap baiduMap) {
                super(baiduMap);
            }

            @Override
            public boolean onPoiClick(int index) {
                super.onPoiClick(index);

                PoiInfo poi = getPoiResult().getAllPoi().get(index);
                mPoiSearch.searchPoiDetail((new PoiDetailSearchOption()).poiUid(poi.uid));

                pt = new LatLng( poi.location.latitude,  poi.location.longitude);
                //Toast.makeText(SearchSameApp.this, "weizhi", Toast.LENGTH_SHORT).show();
                RadarUploadInfo info = new RadarUploadInfo();
                info.comments = userComment;
                info.pt = pt;

                RadarSearchManager.getInstance().uploadInfoRequest(info);
                if (pt == null) {
                    Toast.makeText(SearchSameApp.this, "未获取到位置", Toast.LENGTH_LONG).show();
                    return false;
                }
               //uploadAuto = true;
                //RadarSearchManager.getInstance().startUploadAuto(SearchSameApp.this, 5000);
                return true;
            }
        public boolean onMarkerClick(Marker marker) {
            super.onMarkerClick(marker);
            Toast.makeText(SearchSameApp.this, "你想看哪里？", Toast.LENGTH_SHORT)
                    .show();
            return false;
        }
        }

        /**
         * 对周边检索的范围进行绘制
         *
         * @param center
         * @param radius
         */
        public void showNearbyArea(LatLng center, int radius) {
            BitmapDescriptor centerBitmap = BitmapDescriptorFactory
                    .fromResource(R.drawable.icon_geo);
            MarkerOptions ooMarker = new MarkerOptions().position(center).icon(centerBitmap);
            mBaiduMap.addOverlay(ooMarker);

            OverlayOptions ooCircle = new CircleOptions().fillColor(0xCCCCCC00)
                    .center(center).stroke(new Stroke(5, 0xFFFF00FF))
                    .radius(radius);
            mBaiduMap.addOverlay(ooCircle);
        }

        /**
         * 对区域检索的范围进行绘制
         *
         * @param bounds
         */
        public void showBound(LatLngBounds bounds) {
            BitmapDescriptor bdGround = BitmapDescriptorFactory
                    .fromResource(R.drawable.ground_overlay);

            OverlayOptions ooGround = new GroundOverlayOptions()
                    .positionFromBounds(bounds).image(bdGround).transparency(0.8f);
            mBaiduMap.addOverlay(ooGround);

            MapStatusUpdate u = MapStatusUpdateFactory
                    .newLatLng(bounds.getCenter());
            mBaiduMap.setMapStatus(u);

            bdGround.recycle();
        }
    }

