package calvin.com.alleyes;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

/**
 * Created by admin on 2017/4/30.
 */

public class getCity extends Activity {

    private TextView mTv = null;
    public LocationClient mLocationClient = null;
    public MyLocationListenner myListener = new MyLocationListenner();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_getpos);

        mLocationClient = new LocationClient(this);
        mLocationClient.registerLocationListener(myListener);
        mTv = (TextView) findViewById(R.id.gettextview);
        setLocationOption();
        mLocationClient.start();
    }

    public class MyLocationListenner implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            mTv.setText(location.getCity());
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }

    }

    @Override
    public void onDestroy() {
        mLocationClient.stop();
        super.onDestroy();
    }

    //设置相关参数
    private void setLocationOption() {
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); //打开gps
        option.setServiceName("com.baidu.location.service_v2.2");

        option.disableCache(true);
        mLocationClient.setLocOption(option);
    }

}
