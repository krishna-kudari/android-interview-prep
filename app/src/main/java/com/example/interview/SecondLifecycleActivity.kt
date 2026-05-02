package com.example.interview

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.interview.LifecycleDemoFragment.Companion.LOG_TAG

/**
 * Full-screen activity on top of [LifecycleLabActivity] to observe paused/stopped host activity
 * and this activity's own lifecycle.
 */
class SecondLifecycleActivity : AppCompatActivity() {

    private fun log(msg: String) {
        Log.d(LOG_TAG, "[SecondLifecycleActivity] $msg")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log("onCreate(savedInstanceState=${savedInstanceState != null})")
        setContentView(R.layout.activity_second_lifecycle)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.second_fragment_container,
                    LifecycleDemoFragment.newInstance(
                        "Inside second activity",
                        R.color.lifecycle_fragment_second
                    ),
                    "second_inner"
                )
                .commit()
        }
    }

    override fun onStart() {
        super.onStart()
        log("onStart")
    }

    override fun onResume() {
        super.onResume()
        log("onResume")
    }

    override fun onPause() {
        log("onPause")
        super.onPause()
    }

    override fun onStop() {
        log("onStop")
        super.onStop()
    }

    override fun onDestroy() {
        log("onDestroy")
        super.onDestroy()
    }
}
