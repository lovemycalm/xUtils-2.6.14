/**
 * All rights Reserved, Copyright (C) HAOWU LIMITED 2011-2015
 * FileName: HomeCityFragment.java
 * @author LuoLingBin
 */

package com.haowu.website.moudle.home.city;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.haowu.website.R;
import com.haowu.website.WebsiteApplication;
import com.haowu.website.constant.HttpAddressStatic;
import com.haowu.website.moudle.MainActivity;
import com.haowu.website.moudle.base.BaseProgressFragment;
import com.haowu.website.moudle.base.BaseResponse;
import com.haowu.website.moudle.buy.newhouse.BuyFragmentNewContent;
import com.haowu.website.moudle.buy.secondhandhouse.BuyFragmentSecondContent;
import com.haowu.website.moudle.city.bean.CityVo;
import com.haowu.website.moudle.home.HomeFragment;
import com.haowu.website.moudle.home.HomeHelper;
import com.haowu.website.moudle.home.city.bean.AllCity;
import com.haowu.website.moudle.home.city.bean.HotCity;
import com.haowu.website.moudle.loginAndRegister.bean.User;
import com.haowu.website.tools.AppManager;
import com.haowu.website.tools.AppPreference;
import com.haowu.website.tools.CheckUtil;
import com.haowu.website.tools.CommonUtil;
import com.haowu.website.tools.MyLog;
import com.haowu.website.tools.ToastUser;
import com.haowu.website.tools.UserBiz;
import com.haowu.website.tools.citylist.CityBiz;
import com.haowu.website.tools.location.LocationCommonAmap.MyLocationInfo;
import com.loopj.android.http.RequestParams;

/**
 * name: HomeCityFragment <BR>
 * description: please write your description <BR>
 * create date: 2015年11月16日
 * 
 * @author: HAOWU) LuoLingBin
 */
public class HomeCityFragment extends BaseProgressFragment {

    public HomeCityActivity activity;
    private View v;
    // 定位城市
    public RelativeLayout rl_gpsCity;
    public TextView tv_city;
    public View v1;
    // 热门城市
    private ArrayList<HotCity> hotCity = new ArrayList<HotCity>();
    private HomeHotCityAdapter hotAdapter;
    private LinearLayout ll_hot_city;
    private ListView list_hot_section;
    // 全部城市
    private ArrayList<AllCity> allCity = new ArrayList<AllCity>();
    private HomeAllCityAdapter allAdapter;
    private ListView list_section;

    // private String CityName;

    public static HomeCityFragment newInstance(HomeCityActivity activity) {
        HomeCityFragment fragment = new HomeCityFragment();
        fragment.activity = activity;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.activity_housecity, null);
        initView(v);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setContentView(v);
        setEmptyText("网络异常");
        httpForCityList(true);
    }

    private void initView(View v) {
        // CityName = activity.getIntent().getStringExtra("cityName");
        rl_gpsCity = (RelativeLayout) v.findViewById(R.id.rl_gpsCity);
        rl_gpsCity.setVisibility(View.GONE);
        tv_city = (TextView) v.findViewById(R.id.tv_city);
        v1 = v.findViewById(R.id.v1);
        v1.setVisibility(View.GONE);

        ll_hot_city = (LinearLayout) v.findViewById(R.id.ll_hot_city);
        list_hot_section = (ListView) v.findViewById(R.id.list_hot_section);
        list_section = (ListView) v.findViewById(R.id.list_section);

        cityLocation();
        setFailViewOnClick(new failViewOnClick() {

            @Override
            public void onClick() {
                httpForCityList(true);
            }
        });
    }

    /** 获取定位城市数据 */
    public void cityLocation() {
        final MyLocationInfo locationInfo = WebsiteApplication.getInstance().getLocationInfo();
        // 获取缓存城市
        List<CityVo> citys = CommonUtil.strToList(AppPreference.getCityList(getActivity()), CityVo.class);
        if (locationInfo != null) {
            final String[] cityName = locationInfo.getCityName().split("市");
            final String cityId = CityBiz.getCooperateCityIdByName(activity, cityName[0]);
            for (CityVo cityVo : citys) {
                if (cityName[0].equals(cityVo.getCity_name())) {
                    rl_gpsCity.setVisibility(View.VISIBLE);
                    v1.setVisibility(View.VISIBLE);
                    MyLog.d("MyLog", "匹配成功：" + cityName[0] + "-----------" + cityVo.getCity_name());
                }
            }
            tv_city.setText(cityName[0]);
            MyLog.d("MyLog", "定位到的：" + cityName[0]);
            rl_gpsCity.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    User user = UserBiz.getUser(activity);
                    user.setCityId(cityId + "");
                    user.setLatitude("");
                    user.setLongitude("");
                    UserBiz.saveUser(activity, user);
                    BuyFragmentNewContent.NewHouseRefresh = true;
                    BuyFragmentSecondContent.SecondHouseRefresh = true;
                    WebsiteApplication.RefreshsignSec = true;
                    WebsiteApplication.RefreshsignNew = true;
                    WebsiteApplication.RefreshLable = true;
                    HomeFragment.ragmentRefresh = true;
                    if ("one".equals(activity.type)) {
                        Intent intent = new Intent(activity, MainActivity.class);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent();
                        intent.putExtra("cityId", cityId + "");
                        intent.putExtra("cityName", cityName[0]);
                        activity.setResult(Activity.RESULT_OK, intent);
                    }
                    AppManager.getInstance().finishActivity(activity);
                }
            });
        }
    }

    /**
     * Method name: httpForCityList <BR>
     * Description: 请求城市列表 <BR>
     * Remark: <BR>
     * 
     * @param url
     * @param params
     * @param action void<BR>
     */
    public void httpForCityList(boolean isre) {
        if (isre) {
            setContentShown(false);
        }
        Handler handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                setContentShown(true);
                int what = msg.what;
                BaseResponse baseObj = (BaseResponse) msg.obj;
                if (baseObj == null) {
                    baseObj = new BaseResponse();
                }
                switch (what) {
                    case 1:
                        String dataStr = baseObj.data;
                        JSONObject dataObject = JSON.parseObject(dataStr);
                        if (CheckUtil.isEmpty(dataStr))
                            return;
                        // 保存版本号
                        String dateVersions = dataObject.getString("version");
                        AppPreference.saveCityListDateVersion(activity, dateVersions);
                        // 热门城市
                        String hotCity = dataObject.getString("hotCity");
                        AppPreference.saveHotCityList(activity, hotCity);
                        // 全部城市
                        String allCity = dataObject.getString("allCity");
                        AppPreference.saveTotalCityList(activity, allCity);

                        setData();
                        break;
                    case 0:
                        setData();
                        break;
                    case 2:
                        setContentShown(true);
                        setContentEmpty(false);
                        // 请求成功，数据无更新
                        setData();
                        break;
                    case -1:
                        setEmptyText("网络异常");
                        setContentEmpty(true);
                        ToastUser.showToastNetError(activity);
                        break;
                    default:
                        break;
                }
            }
        };
        final RequestParams params = new RequestParams();
        final String dateVersion = AppPreference.getCityListDateVersion(activity);
        params.put("dataModifyVersion", dateVersion);
        HomeHelper.request(getActivity(), HttpAddressStatic.GET_COOPERATE_CITY, params, handler);
    }

    private void setData() {
        // 热门城市
        hotCity = new ArrayList<HotCity>(CityBiz.getHotCooperateProvinceList(activity));
        hotAdapter = new HomeHotCityAdapter(hotCity);
        list_hot_section.setAdapter(hotAdapter);
        if (hotCity != null && hotCity.size() > 0) {
            ll_hot_city.setVisibility(View.VISIBLE);
        } else {
            ll_hot_city.setVisibility(View.GONE);
        }
        // 合作城市
        allCity = new ArrayList<AllCity>(CityBiz.getTotalCooperateProvinceList(activity));
        allAdapter = new HomeAllCityAdapter(allCity);
        list_section.setAdapter(allAdapter);
    }

    /**
     * 
     * name: HouseCityAdapter <BR>
     * description: 热门城市 <BR>
     * create date: 2015年11月2日
     * 
     * @author: HAOWU) LuoLingBin
     */
    public class HomeHotCityAdapter extends BaseAdapter {

        private ArrayList<HotCity> hotCity;

        public HomeHotCityAdapter(ArrayList<HotCity> hotCity) {
            this.hotCity = hotCity;
        }

        @Override
        public int getCount() {
            if (hotCity == null) {
                return 0;
            }
            return hotCity.size();
        }

        @Override
        public Object getItem(int position) {
            return hotCity.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                final LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = vi.inflate(R.layout.item_house_city, null);
            }
            final TextView textView1 = (TextView) view.findViewById(R.id.tv_house_city);
            if (textView1 != null) {
                textView1.setText(hotCity.get(position).getCity_name());
            }
            final String hotcityId = hotCity.get(position).getId() + "";
            final String hotcityName = hotCity.get(position).getCity_name();
            final String longitude = hotCity.get(position).getLongitude();
            final String latitude = hotCity.get(position).getLatitude();
            view.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    User user = UserBiz.getUser(activity);
                    user.setCityId(hotcityId + "");
                    user.setLatitude(latitude);
                    user.setLongitude(longitude);
                    UserBiz.saveUser(activity, user);
                    BuyFragmentNewContent.NewHouseRefresh = true;
                    BuyFragmentSecondContent.SecondHouseRefresh = true;
                    WebsiteApplication.RefreshsignSec = true;
                    WebsiteApplication.RefreshsignNew = true;
                    WebsiteApplication.RefreshLable = true;
                    HomeFragment.ragmentRefresh = true;
                    MyLog.d("MyLog", hotcityId + "");
                    if ("one".equals(activity.type)) {
                        Intent intent = new Intent(activity, MainActivity.class);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent();
                        intent.putExtra("cityId", hotcityId + "");
                        intent.putExtra("cityName", hotcityName);
                        MyLog.d("MyLog", "选择的城市：" + hotcityName);
                        activity.setResult(Activity.RESULT_OK, intent);
                    }
                    AppManager.getInstance().finishActivity(activity);
                }
            });
            return view;
        }

    }

    /**
     * 
     * name: HouseCityAdapter <BR>
     * description: 全部城市 <BR>
     * create date: 2015年11月2日
     * 
     * @author: HAOWU) LuoLingBin
     */
    public class HomeAllCityAdapter extends BaseAdapter {

        private ArrayList<AllCity> allCity;

        // private String cityId;
        // private String cityName;

        public HomeAllCityAdapter(ArrayList<AllCity> allCity) {
            this.allCity = allCity;
        }

        @Override
        public int getCount() {
            if (allCity == null) {
                return 0;
            }
            return allCity.size();
        }

        @Override
        public Object getItem(int position) {
            return allCity.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                final LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = vi.inflate(R.layout.item_house_city, null);
            }
            final TextView textView1 = (TextView) view.findViewById(R.id.tv_house_city);
            if (textView1 != null) {
                textView1.setText(allCity.get(position).getCity_name());
            }
            final String cityId = allCity.get(position).getId() + "";
            final String cityName = allCity.get(position).getCity_name();
            final String longitude = allCity.get(position).getLongitude();
            final String latitude = allCity.get(position).getLatitude();
            view.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    User user = UserBiz.getUser(activity);
                    user.setCityId(cityId + "");
                    user.setLatitude(latitude);
                    user.setLongitude(longitude);
                    UserBiz.saveUser(activity, user);
                    BuyFragmentNewContent.NewHouseRefresh = true;
                    BuyFragmentSecondContent.SecondHouseRefresh = true;
                    WebsiteApplication.RefreshsignSec = true;
                    WebsiteApplication.RefreshsignNew = true;
                    WebsiteApplication.RefreshLable = true;
                    HomeFragment.ragmentRefresh = true;
                    MyLog.d("MyLog", cityId + "");
                    if ("one".equals(activity.type)) {
                        Intent intent = new Intent(activity, MainActivity.class);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent();
                        intent.putExtra("cityId", cityId + "");
                        intent.putExtra("cityName", cityName);
                        MyLog.d("MyLog", "选择的城市：" + cityName);
                        activity.setResult(Activity.RESULT_OK, intent);
                    }
                    AppManager.getInstance().finishActivity(activity);
                }
            });
            return view;
        }

    }

}
