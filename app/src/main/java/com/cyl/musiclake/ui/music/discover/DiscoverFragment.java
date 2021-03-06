package com.cyl.musiclake.ui.music.discover;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.cyl.musiclake.R;
import com.cyl.musiclake.base.BaseLazyFragment;
import com.cyl.musiclake.common.Constants;
import com.cyl.musiclake.common.Extras;
import com.cyl.musiclake.common.NavigationHelper;
import com.cyl.musiclake.data.db.Playlist;
import com.cyl.musiclake.db.Artist;
import com.cyl.musiclake.ui.music.online.activity.BaiduMusicListActivity;
import com.cyl.musiclake.ui.music.online.activity.NeteasePlaylistActivity;
import com.cyl.musiclake.ui.music.online.fragment.BaiduPlaylistFragment;
import com.cyl.musiclake.ui.music.online.fragment.NeteasePlaylistFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * 功能：在线排行榜
 * 作者：yonglong on 2016/8/11 18:14
 * 邮箱：643872807@qq.com
 * 版本：2.5
 */
public class DiscoverFragment extends BaseLazyFragment<DiscoverPresenter> implements DiscoverContract.View {

    private static final String TAG = "FoundFragment";

    private TopListAdapter mBaiduAdapter;
    private TopListAdapter mNeteaseAdapter;
    private TopArtistListAdapter mArtistListAdapter;
    private BaiduRadioAdapter mRadioAdapter;
    private List<Playlist> playlist = new ArrayList<>();
    private List<Artist> artists = new ArrayList<>();
    private List<Playlist> channels = new ArrayList<>();

    @BindView(R.id.baiChartsRv)
    RecyclerView mBaiChartsRv;

    @BindView(R.id.wangChartsRv)
    RecyclerView mWangChartsRv;

    @BindView(R.id.chartsArtistRcv)
    RecyclerView mChartsArtistRcv;

    @BindView(R.id.radioRsv)
    RecyclerView mRadioRsv;


    @OnClick(R.id.seeAllArtistTv)
    void toArtistList() {
        NavigationHelper.INSTANCE.navigateFragment(getActivity(), AllListFragment.newInstance(Constants.NETEASE_ARITIST_LIST));
    }

    @OnClick(R.id.seeAllRadioTv)
    void toRadioList() {
        NavigationHelper.INSTANCE.navigateFragment(getActivity(), AllListFragment.newInstance(Constants.BAIDU_RADIO_LIST));
    }

    @OnClick(R.id.seeAllBaiTv)
    void toBaidu() {
        NavigationHelper.INSTANCE.navigateFragment(getActivity(), BaiduPlaylistFragment.newInstance());
    }

    @OnClick(R.id.seeAllWangTv)
    void toNetease() {
        NavigationHelper.INSTANCE.navigateFragment(getActivity(), NeteasePlaylistFragment.newInstance());
    }

    public static DiscoverFragment newInstance() {
        Bundle args = new Bundle();

        DiscoverFragment fragment = new DiscoverFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getLayoutId() {
        return R.layout.frag_discover;
    }

    @Override
    public void initViews() {
        //初始化列表
        mBaiChartsRv.setLayoutManager(new GridLayoutManager(getActivity(), 2, LinearLayoutManager.HORIZONTAL, false));
        //适配器
        mBaiduAdapter = new TopListAdapter(playlist);
        mBaiChartsRv.setAdapter(mBaiduAdapter);
        mBaiChartsRv.setFocusable(false);
        mBaiChartsRv.setNestedScrollingEnabled(false);
        mBaiduAdapter.bindToRecyclerView(mBaiChartsRv);

        mWangChartsRv.setLayoutManager(new GridLayoutManager(getActivity(), 2, LinearLayoutManager.HORIZONTAL, false));
        //适配器
        mNeteaseAdapter = new TopListAdapter(playlist);
        mWangChartsRv.setAdapter(mNeteaseAdapter);
        mWangChartsRv.setFocusable(false);
        mWangChartsRv.setNestedScrollingEnabled(false);
        mNeteaseAdapter.bindToRecyclerView(mWangChartsRv);

        mChartsArtistRcv.setLayoutManager(new GridLayoutManager(getActivity(), 2, LinearLayoutManager.HORIZONTAL, false));
        //适配器
        mArtistListAdapter = new TopArtistListAdapter(artists);
        mChartsArtistRcv.setAdapter(mNeteaseAdapter);
        mChartsArtistRcv.setFocusable(false);
        mChartsArtistRcv.setNestedScrollingEnabled(false);
        mArtistListAdapter.bindToRecyclerView(mChartsArtistRcv);

        //电台列表
        mRadioRsv.setLayoutManager(new GridLayoutManager(getActivity(), 2, LinearLayoutManager.HORIZONTAL, false));
        //适配器
        mRadioAdapter = new BaiduRadioAdapter(channels);
        mRadioRsv.setAdapter(mRadioAdapter);
        mRadioRsv.setFocusable(false);
        mRadioRsv.setNestedScrollingEnabled(false);
        mRadioAdapter.bindToRecyclerView(mRadioRsv);
    }

    @Override
    protected void initInjector() {
        mFragmentComponent.inject(this);
    }

    @Override
    protected void loadData() {
        mPresenter.loadBaidu();
        mPresenter.loadNetease();
        mPresenter.loadArtists();
        mPresenter.loadRaios();
    }

    @Override
    protected void listener() {
        mBaiduAdapter.setOnItemClickListener((adapter, view, position) -> {
            Playlist playlist = (Playlist) adapter.getItem(position);
            Intent intent = new Intent(getActivity(), BaiduMusicListActivity.class);
            intent.putExtra(Extras.BILLBOARD_TITLE, playlist.getName());
            intent.putExtra(Extras.BILLBOARD_DESC, playlist.getDes());
            intent.putExtra(Extras.BILLBOARD_ALBUM, playlist.getCoverUrl());
            intent.putExtra(Extras.BILLBOARD_TYPE, playlist.getPid());
            startActivity(intent);
        });

        mNeteaseAdapter.setOnItemClickListener((adapter, view, position) -> {
            Playlist playlist = (Playlist) adapter.getData().get(position);
            Intent intent = new Intent(getActivity(), NeteasePlaylistActivity.class);
            intent.putExtra("title", playlist.getName());
            intent.putExtra("id", playlist.getPid());
            startActivity(intent);
        });

        mArtistListAdapter.setOnItemClickListener((adapter, view, position) -> {
            Artist artist = (Artist) adapter.getData().get(position);
            NavigationHelper.INSTANCE.navigateToPlaylist(mFragmentComponent.getActivity(), artist);
        });

        mRadioAdapter.setOnItemClickListener((adapter, view, position) -> {
            NavigationHelper.INSTANCE.navigateToPlaylist(mFragmentComponent.getActivity(), channels.get(position), null);
        });
    }

    @Override
    public void onLazyLoad() {

    }

    @Override
    public void showEmptyView() {

    }

    @Override
    public void showBaiduCharts(List<Playlist> charts) {
        mBaiduAdapter.setNewData(charts);
    }

    @Override
    public void showNeteaseCharts(List<Playlist> charts) {
        mNeteaseAdapter.setNewData(charts);
    }

    @Override
    public void showArtistCharts(List<Artist> charts) {
        mArtistListAdapter.setNewData(charts);
    }

    @Override
    public void showRadioChannels(List<Playlist> channels) {
        this.channels = channels;
        mRadioAdapter.setNewData(channels);
    }
}