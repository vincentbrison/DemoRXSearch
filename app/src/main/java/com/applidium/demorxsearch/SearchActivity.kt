package com.applidium.demorxsearch

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : AppCompatActivity() {

    private lateinit var viewModel: SearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setAndBindView()
    }

    private fun setAndBindView() {
        setContentView(R.layout.activity_search)
        viewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)
        viewModel.viewState().observe(this, makeViewStateListener())
        edittext_search.addTextChangedListener(makeSearchInputListener())
    }

    private fun makeViewStateListener(): Observer<ViewState> {
        return Observer { viewState ->
            when (viewState) {
                is Loading -> stateful_search.showLoading()
                is Error -> stateful_search.showError(viewState.message, { viewModel.onClickRetry() })
                is Content -> {
                    stateful_search.showContent()
                    textview_search_result.text = viewState.result
                }
            }
        }
    }

    private fun makeSearchInputListener(): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // no-op
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // no-op
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.onSearchInput(s.toString())
            }
        }
    }
}
