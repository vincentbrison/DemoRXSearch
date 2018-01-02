package com.applidium.demorxsearch

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.jakewharton.rxbinding2.widget.textChangeEvents
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : AppCompatActivity() {

    private lateinit var viewModel: SearchViewModel
    private val retryStream = PublishSubject.create<ViewEvent.Retry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setAndBindView()
    }

    private fun setAndBindView() {
        setContentView(R.layout.activity_search)
        viewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)
        viewModel.viewState().observe(this, makeViewStateListener())
    }

    override fun onResume() {
        super.onResume()
        observeViewEvents()
    }

    private fun observeViewEvents() {
        val queryStream = edittext_search
            .textChangeEvents()
            .skipInitialValue()
            .map {
                ViewEvent.StringQuery(it.text().toString())
            }
        viewModel.setUIEventStream(
            Observable.merge(retryStream, queryStream)
        )
    }

    private fun makeViewStateListener(): Observer<ViewState> {
        return Observer { viewState ->
            when (viewState) {
                is ViewState.Loading -> stateful_search.showLoading()
                is ViewState.Error -> stateful_search.showError(
                    viewState.message,
                    { retryStream.onNext(ViewEvent.Retry) }
                )
                is ViewState.Content -> {
                    stateful_search.showContent()
                    textview_search_result.text = viewState.result
                }
            }
        }
    }
}
