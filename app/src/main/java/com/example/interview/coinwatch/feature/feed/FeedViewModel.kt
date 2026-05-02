package com.example.interview.coinwatch.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.interview.coinwatch.domain.model.Coin
import com.example.interview.coinwatch.domain.usecase.GetCoinsFeedUseCase
import com.example.interview.coinwatch.domain.usecase.SearchCoinsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getCoinsFeedUseCase: GetCoinsFeedUseCase,
    private val searchCoinsUseCase: SearchCoinsUseCase
) : ViewModel() {
    private val _searchQuery = MutableStateFlow<String>("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {

    }

    val isSearchActive: StateFlow<Boolean> = _searchQuery.map { it.isNotBlank() }.stateIn(
        scope = viewModelScope,
        initialValue = false,
        started = SharingStarted.WhileSubscribed(5000)
    )

    val feedPagingData: Flow<PagingData<Coin>> = getCoinsFeedUseCase().cachedIn(viewModelScope)

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchPagingData: Flow<PagingData<Coin>> =
        _searchQuery.debounce(300).filter { it.isNotBlank() }.flatMapLatest {
            searchCoinsUseCase(it)
        }.cachedIn(viewModelScope)


    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onSearchCleared() {
        _searchQuery.value = ""
    }
}