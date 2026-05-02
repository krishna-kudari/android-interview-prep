package com.example.interview

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.interview.LifecycleDemoFragment.Companion.LOG_TAG
import com.google.android.material.button.MaterialButton

/**
 * Host activity for fragment transaction experiments. All logs use [LOG_TAG] (`LifecycleLab`).
 */
class LifecycleLabActivity : AppCompatActivity() {

    private fun log(msg: String) {
        Log.d(LOG_TAG, "[LifecycleLabActivity] $msg")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log("onCreate(savedInstanceState=${savedInstanceState != null})")
        setContentView(R.layout.activity_lifecycle_lab)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    LifecycleDemoFragment.newInstance("A (initial)", R.color.lifecycle_fragment_a),
                    TAG_FRAGMENT_PRIMARY
                )
                .commit()
        }

        findViewById<MaterialButton>(R.id.btn_replace_a).setOnClickListener {
            replaceNoBackStack(
                LifecycleDemoFragment.newInstance("A", R.color.lifecycle_fragment_a)
            )
        }
        findViewById<MaterialButton>(R.id.btn_replace_b).setOnClickListener {
            replaceNoBackStack(
                LifecycleDemoFragment.newInstance("B", R.color.lifecycle_fragment_b)
            )
        }
        findViewById<MaterialButton>(R.id.btn_push_a).setOnClickListener {
            replaceWithBackStack(
                LifecycleDemoFragment.newInstance("A", R.color.lifecycle_fragment_a),
                "push_a"
            )
        }
        findViewById<MaterialButton>(R.id.btn_push_b).setOnClickListener {
            replaceWithBackStack(
                LifecycleDemoFragment.newInstance("B", R.color.lifecycle_fragment_b),
                "push_b"
            )
        }
        findViewById<MaterialButton>(R.id.btn_pop).setOnClickListener {
            val fm = supportFragmentManager
            if (fm.backStackEntryCount > 0) {
                log("popBackStack() entries=${fm.backStackEntryCount}")
                fm.popBackStack()
            } else {
                log("popBackStack ignored — back stack empty")
            }
        }
        findViewById<MaterialButton>(R.id.btn_add_hide).setOnClickListener { addFragmentHideCurrent() }
        findViewById<MaterialButton>(R.id.btn_second_activity).setOnClickListener {
            startActivity(Intent(this, SecondLifecycleActivity::class.java))
        }
    }

    private fun replaceNoBackStack(fragment: LifecycleDemoFragment) {
        log("transaction: replace (no back stack)")
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, TAG_FRAGMENT_PRIMARY)
            .commit()
    }

    private fun replaceWithBackStack(fragment: LifecycleDemoFragment, name: String) {
        log("transaction: replace + addToBackStack($name)")
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, TAG_FRAGMENT_PRIMARY)
            .addToBackStack(name)
            .commit()
    }

    /**
     * Keeps the current fragment attached but hidden, adds a new fragment on top.
     * Popping the back stack removes the top fragment and calls [Fragment.onHiddenChanged] on the hidden one.
     */
    private fun addFragmentHideCurrent() {
        val fm = supportFragmentManager
        // With multiple fragments in one container, the topmost is typically last in the added list.
        val current = fm.fragments.lastOrNull()
        if (current == null) {
            log("addFragmentHideCurrent: no fragment in container")
            return
        }
        val overlay = LifecycleDemoFragment.newInstance(
            "B (on top of hidden)",
            R.color.lifecycle_fragment_b
        )
        log("transaction: hide(current) + add(overlay) + addToBackStack(overlay_hide)")
        fm.beginTransaction()
            .hide(current)
            .add(R.id.fragment_container, overlay, TAG_FRAGMENT_OVERLAY)
            .addToBackStack("overlay_hide")
            .commit()
    }

    override fun onRestart() {
        super.onRestart()
        log("onRestart (e.g. returning from another activity or after process restore)")
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

    override fun onSaveInstanceState(outState: Bundle) {
        log("onSaveInstanceState")
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        log("onDestroy")
        super.onDestroy()
    }

    companion object {
        private const val TAG_FRAGMENT_PRIMARY = "primary"
        private const val TAG_FRAGMENT_OVERLAY = "overlay"
    }
}
