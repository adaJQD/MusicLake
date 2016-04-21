package com.cyl.music_hnust.fragment;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.cyl.music_hnust.CommentActivity;
import com.cyl.music_hnust.Json.JsonParsing;
import com.cyl.music_hnust.LoginActivity;
import com.cyl.music_hnust.R;
import com.cyl.music_hnust.adapter.MyRecyclerViewAdapter;
import com.cyl.music_hnust.application.MyApplication;
import com.cyl.music_hnust.bean.Dynamic;
import com.cyl.music_hnust.bean.User;
import com.cyl.music_hnust.bean.UserStatus;
import com.cyl.music_hnust.http.HttpUtil;
import com.cyl.music_hnust.utils.FormatUtil;
import com.cyl.music_hnust.utils.SnackbarUtil;
import com.cyl.music_hnust.utils.ToastUtil;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

import static android.net.Uri.encode;


/**
 * Created by Monkey on 2015/6/29.
 */
public class MyFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, MyRecyclerViewAdapter.OnItemClickListener {

    private View mView;
    private static SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    //    private LinearLayoutManager mLayoutManager;
    public static MyRecyclerViewAdapter mRecyclerViewAdapter;
    private RequestQueue mRequestQueue;
    private MyApplication myApplication;
    private ImageLoader imageLoader;
    public static List<Dynamic> mdatas;
    public static int position;
    public static boolean loadmoring = true;
    MyHandler handler;

    private static class MyHandler extends Handler {
        private final WeakReference<MyFragment> myMusicfragment;

        private MyHandler(MyFragment myfragment) {
            myMusicfragment = new WeakReference<MyFragment>(myfragment);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (loadmoring) {


                        Bundle bundle = new Bundle();
                        bundle = msg.getData();
                        String response = (String) bundle.get("response");

                        JSONObject dataJson = null;
                        try {
                            dataJson = new JSONObject(response);

                            mdatas = JsonParsing.getDynamic(dataJson);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Log.e("size", mdatas.size() + "");
                        mRecyclerViewAdapter.myDatas = mdatas;
                        mRecyclerViewAdapter.notifyDataSetChanged();

                        mRecyclerViewAdapter.loadmore = "点击加载更多...";
                    } else {
                        mRecyclerViewAdapter.loadmore = "暂无更多";
                    }
                    break;
                case 1:
                    if (loadmoring) {
                       // mdatas.clear();
                        List<Dynamic> newdatas = new ArrayList<>();
                        Bundle bundle = new Bundle();
                        bundle = msg.getData();
                        String response = (String) bundle.get("response");

                        JSONObject dataJson = null;
                        try {
                            dataJson = new JSONObject(response);

                            newdatas = JsonParsing.getDynamic(dataJson);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Log.e("size", mdatas.size() + "");
                        mdatas.addAll(newdatas);
                        mRecyclerViewAdapter.myDatas.addAll(newdatas);
                        mRecyclerViewAdapter.notifyDataSetChanged();

                        mRecyclerViewAdapter.loadmore = "点击加载更多...";
                    } else {
                        mRecyclerViewAdapter.loadmore = "暂无更多";
                    }
                    mSwipeRefreshLayout.setRefreshing(false);
                    break;

                case 3:
                    mSwipeRefreshLayout.setRefreshing(false);
                    break;
            }
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.frag_main, container, false);
        myApplication = new MyApplication();
        mdatas = new ArrayList<>();
        mRequestQueue = myApplication.getHttpQueues();

        imageLoader = new ImageLoader(mRequestQueue, new ImageLoader.ImageCache() {
            @Override
            public void putBitmap(String url, Bitmap bitmap) {
            }

            @Override
            public Bitmap getBitmap(String url) {
                return null;
            }
        });
        return mView;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        handler = new MyHandler(MyFragment.this);


        mSwipeRefreshLayout = (SwipeRefreshLayout) mView.findViewById(R.id.id_swiperefreshlayout);
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.id_recyclerview);

        configRecyclerView();
//        mLayoutManager = new LinearLayoutManager(getActivity());
        // 刷新时，指示器旋转后变化的颜色
        mSwipeRefreshLayout.setColorSchemeResources(R.color.main_blue_light, R.color.main_blue_dark,
                R.color.setting_blue, R.color.setting_blue);
        mSwipeRefreshLayout.setOnRefreshListener(this);
    }

    private void configRecyclerView() {


        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerViewAdapter = new MyRecyclerViewAdapter(getActivity(), mdatas, imageLoader);
        mRecyclerViewAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);

        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    @Override
    public void onRefresh() {

        // 刷新时模拟数据的变化
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
                User userinfo = UserStatus.getUserInfo(getContext());
                volley_StringRequest_GET(userinfo.getUser_id(), 0, null, null);

            }
        }, 1000);
    }

    @Override
    public void onItemClick(View view, int position) {
        switch (view.getId()) {
//            case R.id.item_action_love_agree:
//                User userinfo1 = UserStatus.getUserInfo(getContext());
//                if (userinfo1.getUser_name() != null) {
//                    this.position = position;
//                    volley_StringRequest_GET(userinfo1.getUser_id(), 2, null, mRecyclerViewAdapter.myDatas.get(position).getDynamic_id());
//
//                } else {
//                    Intent it = new Intent(getContext(), LoginActivity.class);
//                    startActivity(it);
//                }
//
//                break;
            case R.id.container:
            case R.id.content_text:
                Intent it = new Intent(getContext(), CommentActivity.class);
                it.putExtra("position", position);
                it.putExtra("dynamic_id", mdatas.get(position).getDynamic_id());
                startActivity(it);
                SnackbarUtil.show(mRecyclerView, "comment+1", 0);
                break;
            case R.id.foot_text:
                int poi = 0;
                if (loadmoring) {
                    if (position - 1 >= 0) {
                        poi = position - 1;
                        User userinfo = UserStatus.getUserInfo(getContext());
                        Log.e("dd", userinfo.getUser_id());
                        String time = mdatas.get(poi).getTime();
                        moreSecret(userinfo.getUser_id(), 1, time);
                    } else {
                        User userinfo = UserStatus.getUserInfo(getContext());
                        volley_StringRequest_GET(userinfo.getUser_id(), 0, null, null);
                    }
                    mRecyclerViewAdapter.loadmore = "点击加载更多...";
                } else {
                    mRecyclerViewAdapter.loadmore = "暂无更多";
                }
                mRecyclerViewAdapter.notifyDataSetChanged();
                break;

        }
    }

    /**
     * 利用StringRequest实现Get请求
     */
    private void volley_StringRequest_GET(String user_id, final int requestcode, String starttime, String serect_id) {
        String url = "";
        if (requestcode == 0) {
            url = "http://119.29.27.116/hcyl/music_BBS/operate.php?updateDetail&&user_id=" + user_id;
        } else if (requestcode == 1) {
            url = "http://119.29.27.116/hcyl/music_BBS/operate.php?user_id=" + user_id + "&&moreSecret&&start=" + starttime;
        }
        // 2 创建StringRequest对象
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // VolleyLog.v("Response:%n %s", response.toString());
                        Log.i("log", response.toString());
                        try {


                            Message message = new Message();
                            message.what = requestcode;
                            if (requestcode != 2) {
                                JSONArray secretDetail = response.getJSONArray("secretDetail");
                                if (secretDetail.length() == 0) {
                                    loadmoring = false;
                                } else {
                                    loadmoring = true;


                                    Bundle bundle = new Bundle();
                                    bundle.putString("response", response.toString());
                                    message.setData(bundle);
                                }
                            }
                            handler.sendMessage(message);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                // VolleyLog.e("Error: ", error.getMessage());
            }
        });
        jsonObjectRequest.setTag("info");
        // 3 将StringRequest添加到RequestQueue
        mRequestQueue.add(jsonObjectRequest);
    }
    private void moreSecret(String user_id, final int requestcode, String starttime) {
        String url = "http://119.29.27.116/hcyl/music_BBS/operate.php?user_id="
                + user_id + "&moreSecret&start=" + starttime;
        HttpUtil.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                // VolleyLog.v("Response:%n %s", response.toString());
                try {
                    String responses = new String(responseBody, "utf-8");

                    Log.i("log", responses);
                    Message message = new Message();
                    message.what = 1;
                    JSONObject response = new JSONObject(responses);
                    JSONArray secretDetail = response.getJSONArray("secretDetail");

                    if (secretDetail.length() == 0) {
                        loadmoring = false;
                    } else {
                        loadmoring = true;
                        Bundle bundle = new Bundle();
                        bundle.putString("response", response.toString());
                        Log.e("response", response.toString());
                        message.setData(bundle);
                    }
                    handler.sendMessage(message);

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                ToastUtil.show(getActivity(), "网络连接异常，请检查网络！");
            }
        });


    }


}
