package com.cyl.musiclake.ui.music.search


import com.cyl.musiclake.base.BaseContract
import com.cyl.musiclake.data.db.Music
import com.cyl.musiclake.db.SearchHistoryBean

interface SearchContract {

    interface View : BaseContract.BaseView {

        fun showSearchResult(list: MutableList<Music>)

        fun showSearchSuggestion(list: MutableList<SearchHistoryBean>)
        fun showSearchHistory(list: MutableList<SearchHistoryBean>)

        fun showEmptyView()
    }

    interface Presenter : BaseContract.BasePresenter<View> {

        fun search(key: String, filter: SearchEngine.Filter, limit: Int, page: Int)

        fun getSuggestions(query: String)

        fun saveQueryInfo(query: String)
        fun getSearchHistory()

    }
}
