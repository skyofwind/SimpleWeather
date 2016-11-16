package com.example.dzj.theweather;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.Toast;

import com.baidu.apistore.sdk.ApiCallBack;
import com.baidu.apistore.sdk.ApiStoreSDK;
import com.baidu.apistore.sdk.network.Parameters;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.google.gson.Gson;
import com.json.analysis.Basic;
import com.json.analysis.Cond;
import com.json.analysis.DailyForecast;
import com.json.analysis.Now;
import com.json.analysis.PinyinTool;
import com.json.analysis.Tmp;
import com.json.analysis.WeatherResult;
import com.json.analysis.Wind;

import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by dzj on 2016/11/1.
 */

public class LocationWeather extends AppCompatActivity {

    //android6.0需要使用的权限声明
    private final int SDK_PERMISSION_REQUEST = 127;
    private String permissionInfo;
    //百度地图调用
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    //布局组件
    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mAdapter;
    private List<String> mtime,mtmp,mwind,mtxt,ncity,ntmp;
    private List<Integer> micon;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        locationstar();
        //调用RecyclerView初始化
        mRecyclerView=(RecyclerView)findViewById(R.id.id_recyclerview);
        mAdapter=new RecyclerViewAdapter(this,mtime,mtmp,micon,mwind,mtxt,ntmp,ncity);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL_LIST));

    }
    //初始化RecyclerView中传递的数组初始化
    public  void initData(){
        mtime=new ArrayList<String>();//日期数组
        mtmp=new ArrayList<String>();//日温数组
        micon=new ArrayList();//图片id数组
        mwind=new ArrayList<String>();//风力数组
        mtxt=new ArrayList<String>();//天气信息
        ncity=new ArrayList<String>();
        ntmp=new ArrayList<String>();
    }
    //清空RecyclerView以及清空数组数据
    private void dataDelete(){
        try{
            mRecyclerView.removeAllViews();
            mtime.clear();
            mtmp.clear();
            micon.clear();
            mwind.clear();
            mtxt.clear();
            ncity.clear();
            ntmp.clear();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    //更新RecyclerView数据
    private void setAdapter(){
        mAdapter.notifyDataSetChanged();
    }
    //调用百度定位组件
    public void locationstar(){
        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener( myListener );    //注册监听函数
        initLocation();
        mLocationClient.start();
    }
    //百度定位相关
    private void initLocation(){
        LocationClientOption mOption = new LocationClientOption();
        mOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        mOption.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
        mOption.setScanSpan(3000);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        mOption.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        mOption.setIsNeedLocationDescribe(true);//可选，设置是否需要地址描述
        mOption.setNeedDeviceDirect(false);//可选，设置是否需要设备方向结果
        mOption.setLocationNotify(false);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        mOption.setIgnoreKillProcess(true);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        mOption.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        mOption.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        mOption.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        mLocationClient.setLocOption(mOption);
    }
    //百度定位监听器
    public class MyLocationListener implements BDLocationListener {
        String excity="",city="",citypy="";
        PinyinTool tool=new PinyinTool();
        @Override
        public void onReceiveLocation(BDLocation location) {
            //Receive Location
            StringBuffer sb = new StringBuffer(256);
            try{
                //得到当前城市名并转为拼音传入天气接口函数
                excity=location.getCity();
                int num=excity.length();
                if(excity.substring(num-1,num).equals("市")){
                    city=excity.substring(0,num-1);
                    citypy=tool.toPinYin(city,"",PinyinTool.Type.LOWERCASE);
                }else if(excity.substring(num-2,num).equals("地区")){
                    city=excity.substring(0,num-2);
                    citypy=tool.toPinYin(city,"",PinyinTool.Type.LOWERCASE);
                }else{
                    sb.append("\n城市 : ");// 城市
                    sb.append(location.getCity());
                    sb.append("\n区 : ");// 区
                    sb.append(location.getDistrict());
                    sb.append("\n街道 : ");// 街道
                    sb.append(location.getStreet());
                    sb.append("\n地址信息 : ");// 地址信息
                    sb.append(location.getAddrStr());
                    Toast.makeText(getApplicationContext(), "当前只支持查询市或地区的天气，您所在地区暂不支持。您当前所在位置是："+sb, Toast.LENGTH_LONG).show();
                }
                if(citypy!=null){
                    getWeather(citypy);
                    mLocationClient.stop();
                }else{
                    Toast.makeText(getApplicationContext(), "定位不成功，无法获取数据！", Toast.LENGTH_LONG).show();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
   }
    //天气接口函数
    private void getWeather(String city){
        try{
            Parameters para = new Parameters();
            para.put("city", city);
            ApiStoreSDK.execute("http://apis.baidu.com/heweather/weather/free",
                    ApiStoreSDK.GET,
                    para,
                    new ApiCallBack() {
                        @Override
                        public void onSuccess(int status, String responseString) {
                            Log.i("sdkdemo", "onSuccess");
                            JSONObject jsonObject = JSONObject.fromObject(responseString);
                            String str=jsonObject.getString("HeWeather data service 3.0");
                            String thestr=str.substring(1,str.length()-1);
                            //解析json数据获取城市名及部分天气信息
                            Gson mg=new Gson();
                            WeatherResult wr=mg.fromJson(thestr,WeatherResult.class);
                            Basic bs=wr.getBasic();
                            Now now=wr.getNow();
                            ncity.add(bs.getCity());
                            ntmp.add(now.getTmp()+"°");
                            List<DailyForecast> list=wr.getForecast();

                            if(list!=null){
                                int size= list.size();
                                if(size>0){
                                    for(int i=0;i<size;i++){
                                        mtime.add(list.get(i).getDate());
                                        Cond cn=list.get(i).getCond();
                                        getImgByWeather(cn.getTxt_d());
                                        if(cn.getTxt_d().equals(cn.getTxt_n())){
                                            mtxt.add(cn.getTxt_d());
                                        }else{
                                            mtxt.add(cn.getTxt_d()+"转"+cn.getTxt_n());
                                        }
                                        Tmp tmp=list.get(i).getTmp();
                                        mtmp.add(tmp.getMin()+"~"+tmp.getMax()+"°C");
                                        Wind wind=list.get(i).getWind();
                                        if(wind.getDir().equals("无持续风向")){
                                            mwind.add(wind.getSc());
                                        }else{
                                            if(wind.getSc().equals("微风")){
                                                mwind.add(wind.getDir()+wind.getSc());
                                            }else{
                                                mwind.add(wind.getDir()+wind.getSc()+"级");
                                            }
                                        }
                                    }
                                }
                            }
                            setAdapter();
                        }
                        @Override
                        public void onComplete() {
                            Log.i("sdkdemo", "onComplete");
                        }
                        @Override
                        public void onError(int status, String responseString, Exception e) {
                            Log.i("sdkdemo", "onError, status: " + status);
                            Log.i("sdkdemo", "errMsg: " + (e == null ? "" : e.getMessage()));
                            Toast.makeText(getApplicationContext(), "当前网络不稳定，请稍后重试", Toast.LENGTH_LONG).show();
                            //Toast.makeText(getApplicationContext(), getStackTrace(e), Toast.LENGTH_LONG).show();
                        }
                    });
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    /*
    String getStackTrace(Throwable e) {
        if (e == null) {
            return "";
        }
        StringBuilder str = new StringBuilder();
        str.append(e.getMessage()).append("\n");
        for (int i = 0; i < e.getStackTrace().length; i++) {
            str.append(e.getStackTrace()[i]).append("\n");
        }
        return str.toString();
    }
    */

    //判断天气对应图标
    public void getImgByWeather(String weather){

        if("晴".equals(weather)){
            micon.add(R.drawable.a00);
        }else if("多云".equals(weather)||"晴间多云".equals(weather)){
            micon.add(R.drawable.a01);
        }
        else if("阴".equals(weather)){
            micon.add(R.drawable.a02);
        }
        else if("阵雨".equals(weather)){
            micon.add(R.drawable.a03);
        }
        else if("雷阵雨".equals(weather)){
            micon.add(R.drawable.a04);
        }
        else if("雷阵雨伴有冰雹".equals(weather)){
            micon.add(R.drawable.a05);
        }
        else if("雨夹雪".equals(weather)){
            micon.add(R.drawable.a06);
        }
        else if("小雨".equals(weather)){
            micon.add(R.drawable.a07);
        }
        else if("中雨".equals(weather)){
            micon.add(R.drawable.a08);
        }
        else if("大雨".equals(weather)){
            micon.add(R.drawable.a09);
        }
        else if("暴雨".equals(weather)){
            micon.add(R.drawable.a10);
        }
        else if("大暴雨".equals(weather)){
            micon.add(R.drawable.a11);
        }
        else if("特大暴雨".equals(weather)){
            micon.add(R.drawable.a12);
        }
        else if("阵雪".equals(weather)){
            micon.add(R.drawable.a13);
        }
        else if("小雪".equals(weather)){
            micon.add(R.drawable.a14);
        }
        else if("中雪".equals(weather)){
            micon.add(R.drawable.a15);
        }
        else if("大雪".equals(weather)){
            micon.add(R.drawable.a16);
        }
        else if("暴雪".equals(weather)){
            micon.add(R.drawable.a17);
        }
        else if("雾".equals(weather)){
            micon.add(R.drawable.a18);
        }
        else if("冻雨".equals(weather)){
            micon.add(R.drawable.a19);
        }
        else if("沙尘暴".equals(weather)){
            micon.add(R.drawable.a20);
        }
        else if("小到中雨".equals(weather)){
            micon.add(R.drawable.a21);
        }
        else if("中到大雨".equals(weather)){
            micon.add(R.drawable.a22);
        }
        else if("大到暴雨".equals(weather)){
            micon.add(R.drawable.a23);
        }
        else if("暴雨到大暴雨".equals(weather)){
            micon.add(R.drawable.a24);
        }else if("大暴雨到特大暴雨".equals(weather)){
            micon.add(R.drawable.a25);
        }
        else if("小到中雪".equals(weather)){
            micon.add(R.drawable.a26);
        }
        else if("中到大雪".equals(weather)){
            micon.add(R.drawable.a27);
        }
        else if("大到暴雪".equals(weather)){
            micon.add(R.drawable.a28);
        }
        else if("浮尘".equals(weather)){
            micon.add(R.drawable.a29);
        }
        else if("扬沙".equals(weather)){
            micon.add(R.drawable.a30);
        }
        else if("强沙尘暴".equals(weather)){
            micon.add(R.drawable.a31);
        }
        else if("霾".equals(weather)){
            micon.add(R.drawable.a53);
        }else{
            micon.add(R.drawable.undefined);
        }

    }
    //权限相关
    @TargetApi(23)
    private void getPersimmions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<String>();
            /***
             * 定位权限为必须权限，用户如果禁止，则每次进入都会申请
             */
            // 定位精确位置
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
			/*
			 * 读写权限和电话状态权限非必要权限(建议授予)只会申请一次，用户同意或者禁止，只会弹一次
			 */
            // 读写权限
            if (addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissionInfo += "Manifest.permission.WRITE_EXTERNAL_STORAGE Deny \n";
            }
            // 读取电话状态权限
            if (addPermission(permissions, Manifest.permission.READ_PHONE_STATE)) {
                permissionInfo += "Manifest.permission.READ_PHONE_STATE Deny \n";
            }

            if (permissions.size() > 0) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), SDK_PERMISSION_REQUEST);
            }
        }
    }

    @TargetApi(23)
    private boolean addPermission(ArrayList<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) { // 如果应用没有获得对应权限,则添加到列表中,准备批量申请
            if (shouldShowRequestPermissionRationale(permission)){
                return true;
            }else{
                permissionsList.add(permission);
                return false;
            }

        }else{
            return true;
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }
    //右上角菜单查询中国省市天气
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        SubMenu zhixiashi=menu.addSubMenu(1,0,1,"直辖市");
        zhixiashi.setHeaderTitle("直辖市");
        zhixiashi.add(1,1,1,"北京市");
        zhixiashi.add(1,2,1,"上海市");
        zhixiashi.add(1,3,1,"天津市");
        zhixiashi.add(1,4,1,"重庆市");

        SubMenu heilongjiang=menu.addSubMenu(2,0,1,"黑龙江省");
        heilongjiang.setHeaderTitle("黑龙江省");
        heilongjiang.add(2,1,1,"哈尔滨市");
        heilongjiang.add(2,2,1,"齐齐哈尔市");
        heilongjiang.add(2,3,1,"鸡西市");
        heilongjiang.add(2,4,1,"鹤岗市");
        heilongjiang.add(2,5,1,"双鸭山市");
        heilongjiang.add(2,6,1,"大庆市");
        heilongjiang.add(2,7,1,"伊春市");
        heilongjiang.add(2,8,1,"七台河市");
        heilongjiang.add(2,9,1,"牡丹江市");
        heilongjiang.add(2,10,1,"黑河市");
        heilongjiang.add(2,11,1,"绥化市");
        heilongjiang.add(2,12,1,"大兴安岭地区");

        SubMenu jilin=menu.addSubMenu(3,0,1,"吉林省");
        jilin.setHeaderTitle("吉林省");
        jilin.add(3,1,1,"长春市");
        jilin.add(3,2,1,"吉林市");
        jilin.add(3,3,1,"四平市");
        jilin.add(3,4,1,"辽源市");
        jilin.add(3,5,1,"通化市");
        jilin.add(3,6,1,"白山市");
        jilin.add(3,7,1,"松原市");
        jilin.add(3,8,1,"白城市");
        jilin.add(3,9,1,"延边朝鲜族自治州");


        SubMenu liaoning=menu.addSubMenu(4,0,1,"辽宁省");
        liaoning.setHeaderTitle("辽宁省");
        jilin.add(4,1,1,"沈阳市");
        jilin.add(4,2,1,"大连市");
        jilin.add(4,3,1,"鞍山市");
        jilin.add(4,4,1,"抚顺市");
        jilin.add(4,5,1,"本溪市");
        jilin.add(4,6,1,"丹东市");
        jilin.add(4,7,1,"锦州市");
        jilin.add(4,8,1,"营口市");
        jilin.add(4,9,1,"阜新市");
        jilin.add(4,10,1,"辽阳市");
        jilin.add(4,11,1,"盘锦市");
        jilin.add(4,12,1,"铁岭市");
        jilin.add(4,13,1,"朝阳市");
        jilin.add(4,14,1,"葫芦岛市");

        SubMenu jiangsu=menu.addSubMenu(5,0,1,"江苏省");
        jiangsu.setHeaderTitle("江苏省");
        jiangsu.add(5,1,1,"南京市");
        jiangsu.add(5,2,1,"无锡市");
        jiangsu.add(5,3,1,"徐州市");
        jiangsu.add(5,4,1,"常州市");
        jiangsu.add(5,5,1,"苏州市");
        jiangsu.add(5,6,1,"南通市");
        jiangsu.add(5,7,1,"连云港市");
        jiangsu.add(5,8,1,"淮安市");
        jiangsu.add(5,9,1,"盐城市");
        jiangsu.add(5,10,1,"扬州市");
        jiangsu.add(5,11,1,"镇江市");
        jiangsu.add(5,12,1,"泰州市");
        jiangsu.add(5,13,1,"宿迁市");

        SubMenu shandong=menu.addSubMenu(6,0,1,"山东省");
        shandong.setHeaderTitle("山东省");
        shandong.add(6,1,1,"济南市");
        shandong.add(6,2,1,"青岛市");
        shandong.add(6,3,1,"淄博市");
        shandong.add(6,4,1,"枣庄市");
        shandong.add(6,5,1,"东营市");
        shandong.add(6,6,1,"烟台市");
        shandong.add(6,7,1,"潍坊市");
        shandong.add(6,8,1,"济宁市");
        shandong.add(6,9,1,"泰安市");
        shandong.add(6,10,1,"威海市");
        shandong.add(6,11,1,"日照市");
        shandong.add(6,12,1,"莱芜市");
        shandong.add(6,13,1,"临沂市");
        shandong.add(6,14,1,"德州市");
        shandong.add(6,15,1,"聊城市");
        shandong.add(6,16,1,"滨州地区");
        shandong.add(6,17,1,"菏泽地区");

        SubMenu anhui=menu.addSubMenu(7,0,1,"安徽省");
        anhui.setHeaderTitle("安徽省");
        anhui.add(7,1,1,"合肥市");
        anhui.add(7,2,1,"芜湖市");
        anhui.add(7,3,1,"蚌埠市");
        anhui.add(7,4,1,"淮南市");
        anhui.add(7,5,1,"马鞍山市");
        anhui.add(7,6,1,"淮北市");
        anhui.add(7,7,1,"铜陵市");
        anhui.add(7,8,1,"安庆市");
        anhui.add(7,9,1,"黄山市");
        anhui.add(7,10,1,"滁州市");
        anhui.add(7,11,1,"阜阳市");
        anhui.add(7,12,1,"宿州市");
        anhui.add(7,13,1,"六安市");
        anhui.add(7,14,1,"宣城市");
        anhui.add(7,15,1,"巢湖市");
        anhui.add(7,16,1,"池州市");

        SubMenu hebie=menu.addSubMenu(8,0,1,"河北省");
        hebie.setHeaderTitle("河北省");
        hebie.add(8,1,1,"石家庄市");
        hebie.add(8,2,1,"唐山市");
        hebie.add(8,3,1,"秦皇岛市");
        hebie.add(8,4,1,"邯郸市");
        hebie.add(8,5,1,"邢台市");
        hebie.add(8,6,1,"保定市");
        hebie.add(8,7,1,"张家口市");
        hebie.add(8,8,1,"承德市");
        hebie.add(8,9,1,"沧州市");
        hebie.add(8,10,1,"廊坊市");
        hebie.add(8,11,1,"衡水市");

        SubMenu henan=menu.addSubMenu(9,0,1,"河南省");
        henan.setHeaderTitle("河南省");
        henan.add(9,1,1,"郑州市");
        henan.add(9,2,1,"开封市");
        henan.add(9,3,1,"洛阳市");
        henan.add(9,4,1,"平顶山市");
        henan.add(9,5,1,"安阳市");
        henan.add(9,6,1,"鹤壁市");
        henan.add(9,7,1,"新乡市");
        henan.add(9,8,1,"焦作市");
        henan.add(9,9,1,"濮阳市");
        henan.add(9,10,1,"许昌市");
        henan.add(9,11,1,"漯河市");
        henan.add(9,12,1,"三门峡市");
        henan.add(9,13,1,"南阳市");
        henan.add(9,14,1,"商丘市");
        henan.add(9,15,1,"信阳市");
        henan.add(9,16,1,"周口市");
        henan.add(9,17,1,"驻马店地区");
        henan.add(9,18,1,"济源市");

        SubMenu hubei=menu.addSubMenu(10,0,1,"湖北省");
        hubei.setHeaderTitle("湖北省");
        hubei.add(10,1,1,"武汉市");
        hubei.add(10,2,1,"黄石市");
        hubei.add(10,3,1,"十堰市");
        hubei.add(10,4,1,"宜昌市");
        hubei.add(10,5,1,"襄樊市");
        hubei.add(10,6,1,"鄂州市");
        hubei.add(10,7,1,"荆门市");
        hubei.add(10,8,1,"孝感市");
        hubei.add(10,9,1,"荆州市");
        hubei.add(10,10,1,"黄冈市");
        hubei.add(10,11,1,"咸宁市");
        hubei.add(10,12,1,"随州市");
        hubei.add(10,13,1,"恩施土家族苗族自治州");
        hubei.add(10,14,1,"仙桃市");
        hubei.add(10,15,1,"潜江市");
        hubei.add(10,16,1,"天门市");
        hubei.add(10,17,1,"神农架林区");

        SubMenu hunan=menu.addSubMenu(11,0,1,"湖南省");
        hunan.setHeaderTitle("湖南省");
        hunan.add(11,1,1,"长沙市");
        hunan.add(11,2,1,"株洲市");
        hunan.add(11,3,1,"湘潭市");
        hunan.add(11,4,1,"衡阳市");
        hunan.add(11,5,1,"邵阳市");
        hunan.add(11,6,1,"岳阳市");
        hunan.add(11,7,1,"常德市");
        hunan.add(11,8,1,"张家界市");
        hunan.add(11,9,1,"益阳市");
        hunan.add(11,10,1,"郴州市");
        hunan.add(11,11,1,"永州市");
        hunan.add(11,12,1,"怀化市");
        hunan.add(11,13,1,"娄底地区");
        hunan.add(11,14,1,"湘西土家族苗族自治州");

        SubMenu jiangxi=menu.addSubMenu(12,0,1,"江西省");
        jiangxi.setHeaderTitle("江西省");
        jiangxi.add(12,1,1,"南昌市");
        jiangxi.add(12,2,1,"景德镇市");
        jiangxi.add(12,3,1,"萍乡市");
        jiangxi.add(12,4,1,"九江市");
        jiangxi.add(12,5,1,"新余市");
        jiangxi.add(12,6,1,"鹰潭市");
        jiangxi.add(12,7,1,"赣州市");
        jiangxi.add(12,8,1,"宜春市");
        jiangxi.add(12,9,1,"上饶市");
        jiangxi.add(12,10,1,"吉安市");
        jiangxi.add(12,11,1,"抚州市");

        SubMenu shanxi0=menu.addSubMenu(13,0,1,"陕西省");
        shanxi0.setHeaderTitle("陕西省");
        shanxi0.add(13,1,1,"西安市");
        shanxi0.add(13,2,1,"铜川市");
        shanxi0.add(13,3,1,"宝鸡市");
        shanxi0.add(13,4,1,"咸阳市");
        shanxi0.add(13,5,1,"渭南市");
        shanxi0.add(13,6,1,"延安市");
        shanxi0.add(13,7,1,"汉中市");
        shanxi0.add(13,8,1,"安康地区");
        shanxi0.add(13,9,1,"商洛地区");
        shanxi0.add(13,10,1,"榆林地区");

        SubMenu shanxi=menu.addSubMenu(14,0,1,"山西省");
        shanxi.setHeaderTitle("山西省");
        shanxi.add(14,1,1,"太原市");
        shanxi.add(14,2,1,"大同市");
        shanxi.add(14,3,1,"阳泉市");
        shanxi.add(14,4,1,"长治市");
        shanxi.add(14,5,1,"晋城市");
        shanxi.add(14,6,1,"朔州市");
        shanxi.add(14,7,1,"忻州市");
        shanxi.add(14,8,1,"吕梁市");
        shanxi.add(14,9,1,"晋中市");
        shanxi.add(14,10,1,"临汾市");
        shanxi.add(14,11,1,"运城市");

        SubMenu qinghai=menu.addSubMenu(15,0,1,"青海省");
        qinghai.setHeaderTitle("青海省");
        qinghai.add(15,1,1,"西宁市");
        qinghai.add(15,2,1,"海东地区");
        qinghai.add(15,3,1,"海北藏族自治州");
        qinghai.add(15,4,1,"黄南藏族自治州");
        qinghai.add(15,5,1,"海南藏族自治州");
        qinghai.add(15,6,1,"果洛藏族自治州");
        qinghai.add(15,7,1,"玉树藏族自治州");
        qinghai.add(15,8,1,"海西蒙古族藏族自治州");

        SubMenu hainan=menu.addSubMenu(16,0,1,"海南省");
        hainan.setHeaderTitle("海南省");
        hainan.add(16,1,1,"琼海市");
        hainan.add(16,2,1,"儋州市");
        hainan.add(16,3,1,"五指山市");
        hainan.add(16,4,1,"文昌市");
        hainan.add(16,5,1,"万宁市");
        hainan.add(16,6,1,"东方市");
        hainan.add(16,7,1,"海口市");
        hainan.add(16,8,1,"三亚市");

        SubMenu guangdong=menu.addSubMenu(17,0,1,"广东省");
        guangdong.setHeaderTitle("广东省");
        guangdong.add(17,1,1,"广州市");
        guangdong.add(17,2,1,"韶关市");
        guangdong.add(17,3,1,"深圳市");
        guangdong.add(17,4,1,"珠海市");
        guangdong.add(17,5,1,"汕头市");
        guangdong.add(17,6,1,"佛山市");
        guangdong.add(17,7,1,"江门市");
        guangdong.add(17,8,1,"湛江市");
        guangdong.add(17,9,1,"茂名市");
        guangdong.add(17,10,1,"肇庆市");
        guangdong.add(17,11,1,"惠州市");
        guangdong.add(17,12,1,"梅州市");
        guangdong.add(17,13,1,"汕尾市");
        guangdong.add(17,14,1,"河源市");
        guangdong.add(17,15,1,"阳江市");
        guangdong.add(17,16,1,"清远市");
        guangdong.add(17,17,1,"东莞市");
        guangdong.add(17,18,1,"中山市");
        guangdong.add(17,19,1,"潮州市");
        guangdong.add(17,20,1,"揭阳市");
        guangdong.add(17,21,1,"云浮市");

        SubMenu guizhou=menu.addSubMenu(18,0,1,"贵州省");
        guizhou.setHeaderTitle("贵州省");
        guizhou.add(18,1,1,"贵阳市");
        guizhou.add(18,2,1,"六盘水市");
        guizhou.add(18,3,1,"遵义市");
        guizhou.add(18,4,1,"铜仁地区");
        guizhou.add(18,5,1,"黔西南布依族苗族自治州");
        guizhou.add(18,6,1,"毕节地区");
        guizhou.add(18,7,1,"安顺地区");
        guizhou.add(18,8,1,"黔东南苗族侗族自治州");
        guizhou.add(18,9,1,"黔南布依族苗族自治州");

        SubMenu zhejiang=menu.addSubMenu(19,0,1,"浙江省");
        zhejiang.setHeaderTitle("浙江省");
        zhejiang.add(19,1,1,"杭州市");
        zhejiang.add(19,2,1,"宁波市");
        zhejiang.add(19,3,1,"温州市");
        zhejiang.add(19,4,1,"嘉兴市");
        zhejiang.add(19,5,1,"湖州市");
        zhejiang.add(19,6,1,"绍兴市");
        zhejiang.add(19,7,1,"金华市");
        zhejiang.add(19,8,1,"衢州市");
        zhejiang.add(19,9,1,"舟山市");
        zhejiang.add(19,10,1,"台州市");
        zhejiang.add(19,11,1,"丽水市");

        SubMenu fujian=menu.addSubMenu(20,0,1,"福建省");
        fujian.setHeaderTitle("福建省");
        fujian.add(20,1,1,"福州市");
        fujian.add(20,2,1,"厦门市");
        fujian.add(20,3,1,"宁德市");
        fujian.add(20,4,1,"莆田市");
        fujian.add(20,5,1,"泉州市");
        fujian.add(20,6,1,"漳州市");
        fujian.add(20,7,1,"龙岩市");
        fujian.add(20,8,1,"三明市");
        fujian.add(20,9,1,"南平市");

        SubMenu taiwan=menu.addSubMenu(21,0,1,"台湾省");
        taiwan.setHeaderTitle("台湾省");
        taiwan.add(21,1,1,"桃园市");
        taiwan.add(21,2,1,"台北市");
        taiwan.add(21,3,1,"新竹市");
        taiwan.add(21,4,1,"高雄市");
        taiwan.add(21,5,1,"台中市");
        taiwan.add(21,6,1,"基隆市");
        taiwan.add(21,7,1,"台南市");
        taiwan.add(21,8,1,"嘉义市");
        taiwan.add(21,9,1,"新北市");

        SubMenu gansu=menu.addSubMenu(22,0,1,"甘肃省");
        gansu.setHeaderTitle("甘肃省");
        gansu.add(22,1,1,"兰州市");
        gansu.add(22,2,1,"嘉峪关市");
        gansu.add(22,3,1,"金昌市");
        gansu.add(22,4,1,"白银市");
        gansu.add(22,5,1,"天水市");
        gansu.add(22,6,1,"酒泉地区");
        gansu.add(22,7,1,"张掖地区");
        gansu.add(22,8,1,"武威地区");
        gansu.add(22,9,1,"定西地区");
        gansu.add(22,10,1,"陇南地区");
        gansu.add(22,11,1,"平凉地区");
        gansu.add(22,12,1,"庆阳地区");
        gansu.add(22,13,1,"临夏回族自治州");
        gansu.add(22,14,1,"甘南藏族自治州");


        SubMenu yunnan=menu.addSubMenu(23,0,1,"云南省");
        yunnan.setHeaderTitle("云南省");
        yunnan.add(23,1,1,"昆明市");
        yunnan.add(23,2,1,"曲靖市");
        yunnan.add(23,3,1,"玉溪市");
        yunnan.add(23,4,1,"昭通地区");
        yunnan.add(23,5,1,"楚雄彝族自治州");
        yunnan.add(23,6,1,"红河哈尼族彝族自治州");
        yunnan.add(23,7,1,"文山壮族苗族自治州");
        yunnan.add(23,8,1,"思茅市");
        yunnan.add(23,9,1,"西双版纳傣族自治州");
        yunnan.add(23,10,1,"大理白族自治州");
        yunnan.add(23,11,1,"保山地区");
        yunnan.add(23,12,1,"德宏傣族景颇族自治州");
        yunnan.add(23,13,1,"丽江地区");
        yunnan.add(23,14,1,"怒江傈僳族自治州");
        yunnan.add(23,15,1,"迪庆藏族自治州");
        yunnan.add(23,16,1,"临沧地区");

        SubMenu neimenggu=menu.addSubMenu(24,0,1,"内蒙古自治区");
        neimenggu.setHeaderTitle("内蒙古自治区");
        neimenggu.add(24,1,1,"呼和浩特市");
        neimenggu.add(24,2,1,"包头市");
        neimenggu.add(24,3,1,"乌海市");
        neimenggu.add(24,4,1,"赤峰市");
        neimenggu.add(24,5,1,"呼伦贝尔市");
        neimenggu.add(24,6,1,"兴安盟");
        neimenggu.add(24,7,1,"通辽市");
        neimenggu.add(24,8,1,"锡林郭勒盟");
        neimenggu.add(24,9,1,"乌兰察布盟");
        neimenggu.add(24,10,1,"伊克昭盟");
        neimenggu.add(24,11,1,"巴彦淖尔盟");
        neimenggu.add(24,12,1,"阿拉善盟");


        SubMenu ningxia=menu.addSubMenu(25,0,1,"宁夏回族自治区");
        ningxia.setHeaderTitle("宁夏回族自治区");
        ningxia.add(25,1,1,"银川市");
        ningxia.add(25,2,1,"石嘴山市");
        ningxia.add(25,3,1,"吴忠市");
        ningxia.add(25,4,1,"固原地区");
        ningxia.add(25,5,1,"中卫市");

        SubMenu xinjiang=menu.addSubMenu(26,0,1,"新疆维吾尔自治区");
        xinjiang.setHeaderTitle("新疆维吾尔自治区");
        xinjiang.add(26,1,1,"乌鲁木齐市");
        xinjiang.add(26,2,1,"克拉玛依市");
        xinjiang.add(26,3,1,"哈密地区");
        xinjiang.add(26,4,1,"昌吉回族自治州");
        xinjiang.add(26,5,1,"博尔塔拉蒙古自治州");
        xinjiang.add(26,6,1,"巴音郭楞蒙古自治州");
        xinjiang.add(26,7,1,"阿克苏地区");
        xinjiang.add(26,8,1,"克孜勒苏柯尔克孜自治州");
        xinjiang.add(26,9,1,"喀什地区");
        xinjiang.add(26,10,1,"和田地区");
        xinjiang.add(26,11,1,"伊犁哈萨克自治州");
        xinjiang.add(26,12,1,"塔城地区");
        xinjiang.add(26,13,1,"阿勒泰地区");
        xinjiang.add(26,14,1,"石河子市");
        xinjiang.add(26,15,1,"阿拉尔市");
        xinjiang.add(26,16,1,"图木舒克市");
        xinjiang.add(26,17,1,"五家渠市");

        SubMenu xizang=menu.addSubMenu(27,0,1,"西藏自治区");
        xizang.setHeaderTitle("西藏自治区");
        xizang.add(27,1,1,"拉萨市");
        xizang.add(27,2,1,"昌都地区");
        xizang.add(27,3,1,"山南地区");
        xizang.add(27,4,1,"日喀则地区");
        xizang.add(27,5,1,"那曲地区");
        xizang.add(27,6,1,"阿里地区");
        xizang.add(27,7,1,"林芝地区");

        SubMenu guangxi=menu.addSubMenu(28,0,1,"广西壮族自治区");
        guangxi.setHeaderTitle("广西壮族自治区");
        guangxi.add(28,1,1,"南宁市");
        guangxi.add(28,2,1,"柳州市");
        guangxi.add(28,3,1,"桂林市");
        guangxi.add(28,4,1,"梧州市");
        guangxi.add(28,5,1,"北海市");
        guangxi.add(28,6,1,"防城港市");
        guangxi.add(28,7,1,"钦州市");
        guangxi.add(28,8,1,"贵港市");
        guangxi.add(28,9,1,"玉林市");
        guangxi.add(28,10,1,"崇左市");
        guangxi.add(28,11,1,"来宾市");
        guangxi.add(28,12,1,"贺州市");
        guangxi.add(28,13,1,"百色市");
        guangxi.add(28,14,1,"河池市");

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getGroupId()==1){
            switch(item.getItemId()){
                case 1:
                    dataDelete();
                    getWeather("北京");
                    break;
                case 2:
                    dataDelete();
                    getWeather("上海");
                    break;
                case 3:
                    dataDelete();
                    getWeather("天津");
                    break;
                case 4:
                    dataDelete();
                    getWeather("重庆");
                    break;
            }
        }else if(item.getGroupId()==2){
            switch(item.getItemId()){
                case 1:
                    dataDelete();
                    getWeather("哈尔滨");
                    break;
                case 2:
                    dataDelete();
                    getWeather("齐齐哈尔");
                    break;
                case 3:
                    dataDelete();
                    getWeather("鸡西");

                    break;
                case 4:
                    dataDelete();
                    getWeather("鹤岗");

                    break;
                case 5:
                    dataDelete();
                    getWeather("双鸭山");

                    break;
                case 6:
                    dataDelete();
                    getWeather("大庆");

                    break;
                case 7:
                    dataDelete();
                    getWeather("伊春");

                    break;
                case 8:
                    dataDelete();
                    getWeather("七台河");

                    break;
                case 9:
                    dataDelete();
                    getWeather("牡丹江");

                    break;
                case 10:
                    dataDelete();
                    getWeather("黑河");

                    break;
                case 11:
                    dataDelete();
                    getWeather("绥化");

                    break;
                case 12:
                    dataDelete();
                    getWeather("大兴安岭");

                    break;

            }

        }else if(item.getGroupId()==3){
            switch(item.getItemId()){
                case 1:
                    dataDelete();
                    getWeather("长春");

                    break;
                case 2:
                    dataDelete();
                    getWeather("吉林");

                    break;
                case 3:
                    dataDelete();
                    getWeather("四平");

                    break;
                case 4:
                    dataDelete();
                    getWeather("辽源");

                    break;
                case 5:
                    dataDelete();
                    getWeather("通化");

                    break;
                case 6:
                    dataDelete();
                    getWeather("白山");

                    break;
                case 7:
                    dataDelete();
                    getWeather("松原");

                    break;
                case 8:
                    dataDelete();
                    getWeather("白城");

                    break;
                case 9:
                    tip();
                    break;
            }

        }else if(item.getGroupId()==4){
            switch(item.getItemId()){
                case 1:
                    dataDelete();
                    getWeather("沈阳");
                    break;
                case 2:
                    dataDelete();
                    getWeather("大连");
                    break;
                case 3:
                    dataDelete();
                    getWeather("鞍山");
                    break;
                case 4:
                    dataDelete();
                    getWeather("抚顺");
                    break;
                case 5:
                    dataDelete();
                    getWeather("本溪");
                    break;
                case 6:
                    dataDelete();
                    getWeather("丹东");
                    break;
                case 7:
                    dataDelete();
                    getWeather("锦州");
                    break;
                case 8:
                    dataDelete();
                    getWeather("营口");
                    break;
                case 9:
                    dataDelete();
                    getWeather("阜新");
                    break;
                case 10:
                    dataDelete();
                    getWeather("辽阳");
                    break;
                case 11:
                    dataDelete();
                    getWeather("盘锦");
                    break;
                case 12:
                    dataDelete();
                    getWeather("铁岭");
                    break;
                case 13:
                    dataDelete();
                    getWeather("朝阳");
                    break;
                case 14:
                    dataDelete();
                    getWeather("葫芦岛");
                    break;
            }

        }else if(item.getGroupId()==5){
            switch(item.getItemId()){
                case 1:
                    dataDelete();
                    getWeather("南京");
                    break;
                case 2:
                    dataDelete();
                    getWeather("无锡");
                    break;
                case 3:
                    dataDelete();
                    getWeather("徐州");
                    break;
                case 4:
                    dataDelete();
                    getWeather("常州");
                    break;
                case 5:
                    dataDelete();
                    getWeather("苏州");
                    break;
                case 6:
                    dataDelete();
                    getWeather("南通");
                    break;
                case 7:
                    dataDelete();
                    getWeather("连云港");
                    break;
                case 8:
                    dataDelete();
                    getWeather("淮安");
                    break;
                case 9:
                    dataDelete();
                    getWeather("盐城");
                    break;
                case 10:
                    dataDelete();
                    getWeather("扬州");
                    break;
                case 11:
                    dataDelete();
                    getWeather("镇江");
                    break;
                case 12:
                    dataDelete();
                    getWeather("泰州");
                    break;
                case 13:
                    dataDelete();
                    getWeather("宿迁");
                    break;
            }

        }else if(item.getGroupId()==6){
            switch(item.getItemId()){
                case 1:
                    dataDelete();
                    getWeather("济南");
                    break;
                case 2:
                    dataDelete();
                    getWeather("青岛");
                    break;
                case 3:
                    dataDelete();
                    getWeather("淄博");
                    break;
                case 4:
                    dataDelete();
                    getWeather("枣庄");
                    break;
                case 5:
                    dataDelete();
                    getWeather("东营");
                    break;
                case 6:
                    dataDelete();
                    getWeather("烟台");
                    break;
                case 7:
                    dataDelete();
                    getWeather("潍坊");
                    break;
                case 8:
                    dataDelete();
                    getWeather("济宁");
                    break;
                case 9:
                    dataDelete();
                    getWeather("泰安");
                    break;
                case 10:
                    dataDelete();
                    getWeather("威海");
                    break;
                case 11:
                    dataDelete();
                    getWeather("日照");
                    break;
                case 12:
                    dataDelete();
                    getWeather("莱芜");
                    break;
                case 13:
                    dataDelete();
                    getWeather("临沂");
                    break;
                case 14:
                    dataDelete();
                    getWeather("德州");
                    break;
                case 15:
                    dataDelete();
                    getWeather("聊城");
                    break;
                case 16:
                    dataDelete();
                    getWeather("滨州");
                    break;
                case 17:
                    dataDelete();
                    getWeather("菏泽");
                    break;
            }

        }else if(item.getGroupId()==7){
            switch(item.getItemId()){
                case 1:
                    dataDelete();
                    getWeather("合肥");
                    break;
                case 2:
                    dataDelete();
                    getWeather("芜湖");
                    break;
                case 3:
                    dataDelete();
                    getWeather("蚌埠");
                    break;
                case 4:
                    dataDelete();
                    getWeather("淮南");
                    break;
                case 5:
                    dataDelete();
                    getWeather("马鞍山");
                    break;
                case 6:
                    dataDelete();
                    getWeather("淮北");
                    break;
                case 7:
                    dataDelete();
                    getWeather("铜陵");
                    break;
                case 8:
                    dataDelete();
                    getWeather("安庆");
                    break;
                case 9:
                    dataDelete();
                    getWeather("黄山");
                    break;
                case 10:
                    dataDelete();
                    getWeather("滁州");
                    break;
                case 11:
                    dataDelete();
                    getWeather("阜阳");
                    break;
                case 12:
                    dataDelete();
                    getWeather("宿州");
                    break;
                case 13:
                    dataDelete();
                    getWeather("六安");
                    break;
                case 14:
                    dataDelete();
                    getWeather("宣城");
                    break;
                case 15:
                    dataDelete();
                    getWeather("巢湖");
                    break;
                case 16:
                    dataDelete();
                    getWeather("池州");
                    break;
            }

        }else if(item.getGroupId()==8){
            switch(item.getItemId()){
                case 1:
                    dataDelete();
                    getWeather("石家庄");
                    break;
                case 2:
                    dataDelete();
                    getWeather("唐山");
                    break;
                case 3:
                    dataDelete();
                    getWeather("秦皇岛");
                    break;
                case 4:
                    dataDelete();
                    getWeather("邯郸");
                    break;
                case 5:
                    dataDelete();
                    getWeather("邢台");
                    break;
                case 6:
                    dataDelete();
                    getWeather("保定");
                    break;
                case 7:
                    dataDelete();
                    getWeather("张家口");
                    break;
                case 8:
                    dataDelete();
                    getWeather("承德");
                    break;
                case 9:
                    dataDelete();
                    getWeather("沧州");
                    break;
                case 10:
                    dataDelete();
                    getWeather("廊坊");
                    break;
                case 11:
                    dataDelete();
                    getWeather("衡水");
                    break;
            }

        }else if(item.getGroupId()==9){
            switch(item.getItemId()){
                case 1:
                    dataDelete();
                    getWeather("郑州");
                    break;
                case 2:
                    dataDelete();
                    getWeather("开封");
                    break;
                case 3:
                    dataDelete();
                    getWeather("洛阳");
                    break;
                case 4:
                    dataDelete();
                    getWeather("平顶山");
                    break;
                case 5:
                    dataDelete();
                    getWeather("安阳");
                    break;
                case 6:
                    dataDelete();
                    getWeather("鹤壁");
                    break;
                case 7:
                    dataDelete();
                    getWeather("新乡");
                    break;
                case 8:
                    dataDelete();
                    getWeather("焦作");
                    break;
                case 9:
                    dataDelete();
                    getWeather("濮阳");
                    break;
                case 10:
                    dataDelete();
                    getWeather("许昌");
                    break;
                case 11:
                    dataDelete();
                    getWeather("漯河");
                    break;
                case 12:
                    dataDelete();
                    getWeather("三门峡");
                    break;
                case 13:
                    dataDelete();
                    getWeather("南阳");
                    break;
                case 14:
                    dataDelete();
                    getWeather("商丘");
                    break;
                case 15:
                    dataDelete();
                    getWeather("信阳");
                    break;
                case 16:
                    dataDelete();
                    getWeather("周口");
                    break;
                case 17:
                    dataDelete();
                    getWeather("驻马店");
                    break;
                case 18:
                    dataDelete();
                    getWeather("济源");
                    break;
            }

        }else if(item.getGroupId()==10){
            switch(item.getItemId()){
                case 1:
                    dataDelete();
                    getWeather("武汉");
                    break;
                case 2:
                    dataDelete();
                    getWeather("黄石");
                    break;
                case 3:
                    dataDelete();
                    getWeather("十堰");
                    break;
                case 4:
                    dataDelete();
                    getWeather("宜昌");
                    break;
                case 5:
                    dataDelete();
                    getWeather("襄樊");
                    break;
                case 6:
                    dataDelete();
                    getWeather("鄂州");
                    break;
                case 7:
                    dataDelete();
                    getWeather("荆门");
                    break;
                case 8:
                    dataDelete();
                    getWeather("孝感");
                    break;
                case 9:
                    dataDelete();
                    getWeather("荆州");
                    break;
                case 10:
                    dataDelete();
                    getWeather("黄冈");
                    break;
                case 11:
                    dataDelete();
                    getWeather("咸宁");
                    break;
                case 12:
                    dataDelete();
                    getWeather("随州");
                    break;
                case 13:

                    break;
                case 14:
                    dataDelete();
                    getWeather("仙桃");
                    break;
                case 15:
                    dataDelete();
                    getWeather("潜江");
                    break;
                case 16:
                    dataDelete();
                    getWeather("天门");
                    break;
                case 17:
                    dataDelete();
                    getWeather("神农架");
                    break;
            }

        }else if(item.getGroupId()==11){
            switch(item.getItemId()){
                case 1:
                    dataDelete();
                    getWeather("长沙");
                    break;
                case 2:
                    dataDelete();
                    getWeather("株洲");
                    break;
                case 3:
                    dataDelete();
                    getWeather("湘潭");
                    break;
                case 4:
                    dataDelete();
                    getWeather("衡阳");
                    break;
                case 5:
                    dataDelete();
                    getWeather("邵阳");
                    break;
                case 6:
                    dataDelete();
                    getWeather("岳阳");
                    break;
                case 7:
                    dataDelete();
                    getWeather("常德");
                    break;
                case 8:
                    dataDelete();
                    getWeather("张家界");
                    break;
                case 9:
                    dataDelete();
                    getWeather("益阳");
                    break;
                case 10:
                    dataDelete();
                    getWeather("郴州");
                    break;
                case 11:
                    dataDelete();
                    getWeather("永州");
                    break;
                case 12:
                    dataDelete();
                    getWeather("怀化");
                    break;
                case 13:
                    dataDelete();
                    getWeather("娄底");
                    break;
                case 14:

                    break;
            }

        }else if(item.getGroupId()==12){
            switch(item.getItemId()){
                case 1:
                    dataDelete();
                    getWeather("南昌");
                    break;
                case 2:
                    dataDelete();
                    getWeather("景德镇");
                    break;
                case 3:
                    dataDelete();
                    getWeather("萍乡");
                    break;
                case 4:
                    dataDelete();
                    getWeather("九江");
                    break;
                case 5:
                    dataDelete();
                    getWeather("新余");
                    break;
                case 6:
                    dataDelete();
                    getWeather("鹰潭");
                    break;
                case 7:
                    dataDelete();
                    getWeather("赣州");
                    break;
                case 8:
                    dataDelete();
                    getWeather("宜春");
                    break;
                case 9:
                    dataDelete();
                    getWeather("上饶");
                    break;
                case 10:
                    dataDelete();
                    getWeather("吉安");
                    break;
                case 11:
                    dataDelete();
                    getWeather("抚州");
                    break;
            }

        }else if(item.getGroupId()==13){
            switch(item.getItemId()){
                case 1:
                    dataDelete();
                    getWeather("西安");
                    break;
                case 2:
                    dataDelete();
                    getWeather("铜川");
                    break;
                case 3:
                    dataDelete();
                    getWeather("宝鸡");
                    break;
                case 4:
                    dataDelete();
                    getWeather("咸阳");
                    break;
                case 5:
                    dataDelete();
                    getWeather("渭南");
                    break;
                case 6:
                    dataDelete();
                    getWeather("延安");
                    break;
                case 7:
                    dataDelete();
                    getWeather("汉中");
                    break;
                case 8:
                    dataDelete();
                    getWeather("安康");
                    break;
                case 9:
                    dataDelete();
                    getWeather("商洛");
                    break;
                case 10:
                    dataDelete();
                    getWeather("榆林");
                    break;
            }

        }else if(item.getGroupId()==14){
            switch(item.getItemId()){
                case 1:
                    dataDelete();
                    getWeather("太原");
                    break;
                case 2:
                    dataDelete();
                    getWeather("大同");
                    break;
                case 3:
                    dataDelete();
                    getWeather("阳泉");
                    break;
                case 4:
                    dataDelete();
                    getWeather("长治");
                    break;
                case 5:
                    dataDelete();
                    getWeather("晋城");
                    break;
                case 6:
                    dataDelete();
                    getWeather("朔州");
                    break;
                case 7:
                    dataDelete();
                    getWeather("忻州");
                    break;
                case 8:
                    dataDelete();
                    getWeather("吕梁");
                    break;
                case 9:
                    dataDelete();
                    getWeather("晋中");
                    break;
                case 10:
                    dataDelete();
                    getWeather("临汾");
                    break;
                case 11:
                    dataDelete();
                    getWeather("运城");
                    break;
            }

        }else if(item.getGroupId()==15){
            switch(item.getItemId()){
                case 1:
                    dataDelete();
                    getWeather("西宁");
                    break;
                case 2:
                    dataDelete();
                    getWeather("海东");
                    break;
                case 3:
                    tip();
                    break;
                case 4:
                    tip();
                    break;
                case 5:
                    tip();
                    break;
                case 6:
                    tip();
                    break;
                case 7:
                    tip();
                    break;
                case 8:
                    tip();
                    break;
            }
        }else if(item.getGroupId()==16){
            switch(item.getItemId()){
                case 1:
                    dataDelete();
                    getWeather("琼海");
                    break;
                case 2:
                    dataDelete();
                    getWeather("儋州");
                    break;
                case 3:
                    dataDelete();
                    getWeather("五指山");
                    break;
                case 4:
                    dataDelete();
                    getWeather("文昌");
                    break;
                case 5:
                    dataDelete();
                    getWeather("万宁");
                    break;
                case 6:
                    dataDelete();
                    getWeather("东方");
                    break;
                case 7:
                    dataDelete();
                    getWeather("海口");
                    break;
                case 8:
                    dataDelete();
                    getWeather("三亚");
                    break;
            }
        }else if(item.getGroupId()==17){
            switch(item.getItemId()){
                case 1:
                    dataDelete();
                    getWeather("广州");
                    break;
                case 2:
                    dataDelete();
                    getWeather("韶关");
                    break;
                case 3:
                    dataDelete();
                    getWeather("深圳");
                    break;
                case 4:
                    dataDelete();
                    getWeather("珠海");
                    break;
                case 5:
                    dataDelete();
                    getWeather("汕头");
                    break;
                case 6:
                    dataDelete();
                    getWeather("佛山");
                    break;
                case 7:
                    dataDelete();
                    getWeather("江门");
                    break;
                case 8:
                    dataDelete();
                    getWeather("湛江");
                    break;
                case 9:
                    dataDelete();
                    getWeather("茂名");
                    break;
                case 10:
                    dataDelete();
                    getWeather("肇庆");
                    break;
                case 11:
                    dataDelete();
                    getWeather("惠州");
                    break;
                case 12:
                    dataDelete();
                    getWeather("梅州");
                    break;
                case 13:
                    dataDelete();
                    getWeather("汕尾");
                    break;
                case 14:
                    dataDelete();
                    getWeather("河源");
                    break;
                case 15:
                    dataDelete();
                    getWeather("阳江");
                    break;
                case 16:
                    dataDelete();
                    getWeather("清远");
                    break;
                case 17:
                    dataDelete();
                    getWeather("东莞");
                    break;
                case 18:
                    dataDelete();
                    getWeather("中山");
                    break;
                case 19:
                    dataDelete();
                    getWeather("潮州");
                    break;
                case 20:
                    dataDelete();
                    getWeather("揭阳");
                    break;
                case 21:
                    dataDelete();
                    getWeather("云浮");
                    break;
            }

        }else if(item.getGroupId()==18){
            switch(item.getItemId()){
                case 1:
                    dataDelete();
                    getWeather("贵阳");
                    break;
                case 2:
                    dataDelete();
                    getWeather("六盘水");
                    break;
                case 3:
                    dataDelete();
                    getWeather("遵义");
                    break;
                case 4:
                    dataDelete();
                    getWeather("铜仁");
                    break;
                case 5:
                    tip();
                    break;
                case 6:
                    dataDelete();
                    getWeather("毕节");
                    break;
                case 7:
                    dataDelete();
                    getWeather("安顺");
                    break;
                case 8:
                    tip();
                    break;
                case 9:
                    tip();
                    break;
            }

        }else if(item.getGroupId()==19){
            switch(item.getItemId()){
                case 1:
                    dataDelete();
                    getWeather("杭州");
                    break;
                case 2:
                    dataDelete();
                    getWeather("宁波");
                    break;
                case 3:
                    dataDelete();
                    getWeather("温州");
                    break;
                case 4:
                    dataDelete();
                    getWeather("嘉兴");
                    break;
                case 5:
                    dataDelete();
                    getWeather("湖州");
                    break;
                case 6:
                    dataDelete();
                    getWeather("绍兴");
                    break;
                case 7:
                    dataDelete();
                    getWeather("金华");
                    break;
                case 8:
                    dataDelete();
                    getWeather("衢州");
                    break;
                case 9:
                    dataDelete();
                    getWeather("舟山");
                    break;
                case 10:
                    dataDelete();
                    getWeather("台州");
                    break;
                case 11:
                    dataDelete();
                    getWeather("丽水");
                    break;
            }

        }else if(item.getGroupId()==20){
            switch(item.getItemId()){
                case 1:
                    dataDelete();
                    getWeather("福州");
                    break;
                case 2:
                    dataDelete();
                    getWeather("厦门");
                    break;
                case 3:
                    dataDelete();
                    getWeather("宁德");
                    break;
                case 4:
                    dataDelete();
                    getWeather("莆田");
                    break;
                case 5:
                    dataDelete();
                    getWeather("泉州");
                    break;
                case 6:
                    dataDelete();
                    getWeather("漳州");
                    break;
                case 7:
                    dataDelete();
                    getWeather("龙岩");
                    break;
                case 8:
                    dataDelete();
                    getWeather("三明");
                    break;
                case 9:
                    dataDelete();
                    getWeather("南平");
                    break;
            }

        }else if(item.getGroupId()==21){
            switch(item.getItemId()){
                case 1:
                    tip();
                    break;
                case 2:
                    tip();
                    break;
                case 3:
                    tip();
                    break;
                case 4:
                    tip();
                    break;
                case 5:
                    tip();
                    break;
                case 6:
                    tip();
                    break;
                case 7:
                    tip();
                    break;
                case 8:
                    tip();
                    break;
                case 9:
                    tip();
                    break;
            }

        }else if(item.getGroupId()==22){
            switch(item.getItemId()){
                case 1:
                    dataDelete();
                    getWeather("兰州");
                    break;
                case 2:
                    dataDelete();
                    getWeather("嘉峪关");
                    break;
                case 3:
                    dataDelete();
                    getWeather("金昌");
                    break;
                case 4:
                    dataDelete();
                    getWeather("白银");
                    break;
                case 5:
                    dataDelete();
                    getWeather("天水");
                    break;
                case 6:
                    dataDelete();
                    getWeather("酒泉地");
                    break;
                case 7:
                    dataDelete();
                    getWeather("张掖");
                    break;
                case 8:
                    dataDelete();
                    getWeather("武威");
                    break;
                case 9:
                    dataDelete();
                    getWeather("定西");
                    break;
                case 10:
                    dataDelete();
                    getWeather("陇南");
                    break;
                case 11:
                    dataDelete();
                    getWeather("平凉");
                    break;
                case 12:
                    dataDelete();
                    getWeather("庆阳");
                    break;
                case 13:
                    tip();
                    break;
                case 14:
                    tip();
                    break;
            }

        }else if(item.getGroupId()==23){
            switch(item.getItemId()){
                case 1:
                    dataDelete();
                    getWeather("昆明");
                    break;
                case 2:
                    dataDelete();
                    getWeather("曲靖");
                    break;
                case 3:
                    dataDelete();
                    getWeather("玉溪");
                    break;
                case 4:
                    dataDelete();
                    getWeather("昭通");
                    break;
                case 5:
                    tip();
                    break;
                case 6:
                    tip();
                    break;
                case 7:
                    tip();
                    break;
                case 8:
                    dataDelete();
                    getWeather("思茅");
                    break;
                case 9:
                    tip();
                    break;
                case 10:
                    tip();
                    break;
                case 11:
                    dataDelete();
                    getWeather("保山");
                    break;
                case 12:
                    tip();
                    break;
                case 13:
                    dataDelete();
                    getWeather("丽江");
                    break;
                case 14:
                    tip();
                    break;
                case 15:
                    tip();
                    break;
                case 16:
                    dataDelete();
                    getWeather("临沧");
                    break;
            }

        }else if(item.getGroupId()==24){
            switch(item.getItemId()){
                case 1:
                    dataDelete();
                    getWeather("呼和浩特");
                    break;
                case 2:
                    dataDelete();
                    getWeather("包头");
                    break;
                case 3:
                    dataDelete();
                    getWeather("乌海");
                    break;
                case 4:
                    dataDelete();
                    getWeather("赤峰");
                    break;
                case 5:
                    dataDelete();
                    getWeather("呼伦贝尔");
                    break;
                case 6:
                    dataDelete();
                    getWeather("兴安");
                    break;
                case 7:
                    dataDelete();
                    getWeather("通辽");
                    break;
                case 8:
                    tip();
                    break;
                case 9:
                    tip();
                    break;
                case 10:
                    tip();
                    break;
                case 11:
                    tip();
                    break;
                case 12:
                    tip();
                    break;
            }

        }else if(item.getGroupId()==25){
            switch(item.getItemId()){
                case 1:
                    dataDelete();
                    getWeather("银川");
                    break;
                case 2:
                    dataDelete();
                    getWeather("石嘴山");
                    break;
                case 3:
                    dataDelete();
                    getWeather("吴忠");
                    break;
                case 4:
                    dataDelete();
                    getWeather("固原");
                    break;
                case 5:
                    dataDelete();
                    getWeather("中卫");
                    break;
            }

        }else if(item.getGroupId()==26){
            switch(item.getItemId()){
                case 1:
                    dataDelete();
                    getWeather("乌鲁木齐");
                    break;
                case 2:
                    dataDelete();
                    getWeather("克拉玛依");
                    break;
                case 3:
                    dataDelete();
                    getWeather("哈密");
                    break;
                case 4:
                    tip();
                    break;
                case 5:
                    tip();
                    break;
                case 6:
                    tip();
                    break;
                case 7:
                    dataDelete();
                    getWeather("阿克苏");
                    break;
                case 8:
                    tip();
                    break;
                case 9:
                    dataDelete();
                    getWeather("喀什");
                    break;
                case 10:
                    dataDelete();
                    getWeather("和田");
                    break;
                case 11:
                    tip();
                    break;
                case 12:
                    dataDelete();
                    getWeather("塔城");
                    break;
                case 13:
                    dataDelete();
                    getWeather("阿勒泰");
                    break;
                case 14:
                    dataDelete();
                    getWeather("石河子");
                    break;
                case 15:
                    dataDelete();
                    getWeather("阿拉尔");
                    break;
                case 16:
                    dataDelete();
                    getWeather("图木舒克");
                    break;
                case 17:
                    dataDelete();
                    getWeather("五家渠");
                    break;
            }

        }else if(item.getGroupId()==27){
            switch(item.getItemId()){
                case 1:
                    dataDelete();
                    getWeather("拉萨");
                    break;
                case 2:
                    dataDelete();
                    getWeather("昌都");
                    break;
                case 3:
                    dataDelete();
                    getWeather("山南");
                    break;
                case 4:
                    dataDelete();
                    getWeather("日喀则");
                    break;
                case 5:
                    dataDelete();
                    getWeather("那曲");
                    break;
                case 6:
                    dataDelete();
                    getWeather("阿里");
                    break;
                case 7:
                    dataDelete();
                    getWeather("林芝");
                    break;
            }

        }else if(item.getGroupId()==28){
            switch(item.getItemId()){
                case 1:
                    dataDelete();
                    getWeather("南宁");
                    break;
                case 2:
                    dataDelete();
                    getWeather("柳州");
                    break;
                case 3:
                    dataDelete();
                    getWeather("桂林");
                    break;
                case 4:
                    dataDelete();
                    getWeather("梧州");
                    break;
                case 5:
                    dataDelete();
                    getWeather("北海");
                    break;
                case 6:
                    dataDelete();
                    getWeather("防城港");
                    break;
                case 7:
                    dataDelete();
                    getWeather("钦州");
                    break;
                case 8:
                    dataDelete();
                    getWeather("贵港");
                    break;
                case 9:
                    dataDelete();
                    getWeather("玉林");
                    break;
                case 10:
                    dataDelete();
                    getWeather("崇左");
                    break;
                case 11:
                    dataDelete();
                    getWeather("来宾");
                    break;
                case 12:
                    dataDelete();
                    getWeather("贺州");
                    break;
                case 13:
                    dataDelete();
                    getWeather("百色");
                    break;
                case 14:
                    dataDelete();
                    getWeather("河池");
                    break;
            }
        }
        return super.onOptionsItemSelected(item);
    }
    public void tip(){
        Toast.makeText(getApplicationContext(), "暂不支持此地区", Toast.LENGTH_LONG).show();
    }
}
