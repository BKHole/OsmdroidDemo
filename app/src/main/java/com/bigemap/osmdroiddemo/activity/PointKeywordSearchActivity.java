package com.bigemap.osmdroiddemo.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.bigemap.osmdroiddemo.R;
import com.bigemap.osmdroiddemo.adapter.PointSearchAdapter;
import com.bigemap.osmdroiddemo.entity.Coordinate;
import com.bigemap.osmdroiddemo.utils.AMapUtils;
import com.bigemap.osmdroiddemo.viewholder.OnViewClickListener;

import java.util.ArrayList;
import java.util.List;

public class PointKeywordSearchActivity extends AppCompatActivity implements View.OnClickListener,
        PoiSearch.OnPoiSearchListener, Inputtips.InputtipsListener, TextWatcher {
    private static final String TAG = "PointKeywordSearchActiv";

    private AutoCompleteTextView searchText;// 输入搜索关键字
    private String keyWord = "";// 要输入的poi搜索关键字
    private ProgressDialog progDialog = null;// 搜索时进度条
    private PoiResult poiResult; // poi返回的结果
    private int currentPage = 0;// 当前页面，从0开始计数
    private PoiSearch.Query query;// Poi查询条件类
    private PoiSearch poiSearch;// POI搜索
    private List<Coordinate> coordinates;
    private PointSearchAdapter adapter;
    private RecyclerView pointRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_keyword_search);
        init();
    }

    private void init() {
        Button searButton = (Button) findViewById(R.id.searchButton);
        searButton.setOnClickListener(this);
        Button nextButton = (Button) findViewById(R.id.nextButton);
        nextButton.setOnClickListener(this);
        searchText = (AutoCompleteTextView) findViewById(R.id.keyWord);
        searchText.addTextChangedListener(this);// 添加文本输入框监听事件
        coordinates = new ArrayList<>();
        pointRecyclerView = (RecyclerView) findViewById(R.id.point_search_recycler_view);
        adapter = new PointSearchAdapter(this);
        pointRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        pointRecyclerView.setHasFixedSize(true);
        pointRecyclerView.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.VERTICAL));
    }

    /**
     * 点击搜索按钮
     */
    public void searchButton() {
        keyWord = AMapUtils.checkEditText(searchText);
        if ("".equals(keyWord)) {
            Toast.makeText(this, "请输入搜索关键字", Toast.LENGTH_SHORT).show();
        } else {
            doSearchQuery();
        }
    }

    /**
     * 点击下一页按钮
     */
    public void nextButton() {
        if (query != null && poiSearch != null && poiResult != null) {
            if (poiResult.getPageCount() - 1 > currentPage) {
                currentPage++;
                query.setPageNum(currentPage);// 设置查后一页
                poiSearch.searchPOIAsyn();
            } else {
                Toast.makeText(this, "已经没有了", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 开始进行poi搜索
     */
    protected void doSearchQuery() {
        showProgressDialog();// 显示进度框
        currentPage = 0;
        query = new PoiSearch.Query(keyWord, "", null);// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
//        query.setPageSize(10);// 设置每页最多返回多少条poiitem
        query.setPageNum(currentPage);// 设置查第一页

        poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.searchButton:
                searchButton();
                break;
            case R.id.nextButton:
                nextButton();
                break;
            default:
                break;
        }
    }

    @Override
    public void onGetInputtips(List<Tip> tipList, int rCode) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {// 正确返回
            List<String> listString = new ArrayList<String>();
            for (int i = 0; i < tipList.size(); i++) {
                listString.add(tipList.get(i).getName());
            }
            ArrayAdapter<String> aAdapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_list_item_1, listString);
            searchText.setAdapter(aAdapter);
            aAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, rCode + "", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPoiSearched(PoiResult result, int rCode) {
        dismissProgressDialog();// 隐藏对话框
        coordinates.clear();
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getQuery() != null) {// 搜索poi的结果
                if (result.getQuery().equals(query)) {// 是否是同一条
                    poiResult = result;
                    // 取得搜索到的poiItems有多少页
                    List<PoiItem> poiItems = poiResult.getPois();// 取得第一页的poiItem数据，页数从数字0开始
                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();// 当搜索不到poiItem数据时，会返回含有搜索关键字的城市信息
                    if (poiItems.size() > 0) {
                        for (PoiItem poiItem : poiItems) {
                            Coordinate coordinate = new Coordinate(poiItem.getLatLonPoint().getLongitude()
                                    , poiItem.getLatLonPoint().getLatitude(), poiItem.getTitle(), poiItem.getSnippet());
                            coordinates.add(coordinate);
                        }
                        adapter.setDataList(coordinates);
                        pointRecyclerView.setAdapter(adapter);
                        adapter.setOnViewClickListener(new OnViewClickListener() {
                            @Override
                            public void onClick(View v, Object data) {
                                Coordinate coordinate = (Coordinate) data;
                                Intent intent=new Intent(PointKeywordSearchActivity.this, MainActivity.class);
                                Bundle bundle=new Bundle();
                                bundle.putParcelable("coordinate", coordinate);
                                intent.putExtras(bundle);
                                startActivity(intent);
                                PointKeywordSearchActivity.this.finish();
                            }
                        });
                    } else if (suggestionCities.size() > 0) {
                        showSuggestCity(suggestionCities);
                    } else {
                        Toast.makeText(this, "对不起，没有搜索到相关数据！", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(this, "对不起，没有搜索到相关数据！", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "" + rCode, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * poi没有搜索到数据，返回一些推荐城市的信息
     */
    private void showSuggestCity(List<SuggestionCity> cities) {
        String information = "推荐城市\n";
        for (int i = 0; i < cities.size(); i++) {
            information += "城市名称:" + cities.get(i).getCityName() + "城市区号:"
                    + cities.get(i).getCityCode() + "城市编码:"
                    + cities.get(i).getAdCode() + "\n";
        }
        Toast.makeText(this, information, Toast.LENGTH_SHORT).show();

    }


    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String newText = s.toString().trim();
        if (!AMapUtils.IsEmptyOrNullString(newText)) {
            InputtipsQuery inputQuery = new InputtipsQuery(newText, null);
            Inputtips inputTips = new Inputtips(this, inputQuery);
            inputTips.setInputtipsListener(this);
            inputTips.requestInputtipsAsyn();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progDialog == null)
            progDialog = new ProgressDialog(this);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(false);
        progDialog.setMessage("正在搜索:\n" + keyWord);
        progDialog.show();
    }

    /**
     * 隐藏进度框
     */
    private void dismissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }

}
