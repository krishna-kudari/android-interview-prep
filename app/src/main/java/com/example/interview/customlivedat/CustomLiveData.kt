package com.example.interview.customlivedat

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner


fun interface Observer<T> {
    fun onChanged(value: T?)
}

open class CustomLiveData<T> {

    private val mainLooper = Looper.getMainLooper()
    private val handler = Handler(mainLooper)
    private var _value: T? = null
    val value: T? get() = _value

    private val observers = LinkedHashMap<Observer<T>, ObserverWrapper<T>>()

    fun observe(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {

        assetMainThread("observe")

        if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) return

        val observerWrapper = LifecycleBoundObserver(lifecycleOwner, observer)
        val existing = observers.putIfAbsent(observer, observerWrapper)
        if (existing != null) return

        lifecycleOwner.lifecycle.addObserver(observerWrapper)
        dispatchIfActive(observerWrapper)
    }

    protected open fun setValue(v: T?) {
        assetMainThread("setValue")
        _value = v
        dispatchValue(value)
    }

    protected open fun postValue(v: T?) {
        handler.post {
            setValue(v)
        }
    }

    private fun dispatchValue(v: T?) {
        observers.values.forEach { wrapper ->
            dispatchIfActive(observerWrapper = wrapper)
        }
    }

    private fun dispatchIfActive(observerWrapper: ObserverWrapper<T>) {
        if (observerWrapper.isActive().not()) return
        observerWrapper.observer.onChanged(value)
    }

    private fun assetMainThread(methodName: String) {
        check(!mainLooper.isCurrentThread) {
            "Cannot call $methodName from background thread"
        }
    }
}


class MutableCustomLiveData<T> : CustomLiveData<T>() {
    public override fun setValue(v: T?) {
        super.setValue(v)
    }

    override fun postValue(v: T?) {
        super.postValue(v)
    }
}

abstract class ObserverWrapper<T>(
    val observer: Observer<T>,
) {
    abstract fun isActive(): Boolean
    open fun detach() {}
}

class LifecycleBoundObserver<T>(
    private val owner: LifecycleOwner,
    observer: Observer<T>
) : ObserverWrapper<T>(observer), DefaultLifecycleObserver {

    override fun isActive() = owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)

    override fun detach() {
        owner.lifecycle.removeObserver(this)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        owner.lifecycle.removeObserver(this)
    }
}