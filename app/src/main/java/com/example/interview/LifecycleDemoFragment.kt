package com.example.interview

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

/**
 * Fragment that logs the full lifecycle plus [onHiddenChanged] (hide/show transactions).
 * Filter Logcat: tag `LifecycleLab`.
 */
class LifecycleDemoFragment : Fragment() {

    private val displayName: String
        get() = requireArguments().getString(ARG_NAME) ?: "?"

    private val backgroundColorRes: Int
        get() = requireArguments().getInt(ARG_COLOR)

    private fun log(msg: String) {
        Log.d(LOG_TAG, "${prefix()} $msg")
    }

    private fun prefix(): String = "[Fragment $displayName id=${System.identityHashCode(this)}]"

    override fun onAttach(context: Context) {
        super.onAttach(context)
        log("onAttach")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log("onCreate(savedInstanceState=${savedInstanceState != null})")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        log("onCreateView(savedInstanceState=${savedInstanceState != null})")
        return inflater.inflate(R.layout.fragment_lifecycle_demo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        log("onViewCreated(savedInstanceState=${savedInstanceState != null})")
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), backgroundColorRes))
        view.findViewById<TextView>(R.id.fragment_label).text = getString(
            R.string.lifecycle_fragment_label,
            displayName
        )
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        log("onViewStateRestored(savedInstanceState=${savedInstanceState != null})")
    }

    override fun onStart() {
        super.onStart()
        log("onStart — isHidden=$isHidden isAdded=$isAdded isVisible=$isVisible")
    }

    override fun onResume() {
        super.onResume()
        log("onResume")
    }

    override fun onPause() {
        super.onPause()
        log("onPause")
    }

    override fun onStop() {
        super.onStop()
        log("onStop")
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        log("onHiddenChanged(hidden=$hidden) — still attached, view may exist")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        log("onSaveInstanceState")
    }

    override fun onDestroyView() {
        log("onDestroyView")
        super.onDestroyView()
    }

    override fun onDestroy() {
        log("onDestroy")
        super.onDestroy()
    }

    override fun onDetach() {
        log("onDetach")
        super.onDetach()
    }

    companion object {
        const val LOG_TAG = "LifecycleLab"

        private const val ARG_NAME = "name"
        private const val ARG_COLOR = "color_res"

        fun newInstance(name: String, colorRes: Int): LifecycleDemoFragment {
            return LifecycleDemoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_NAME, name)
                    putInt(ARG_COLOR, colorRes)
                }
            }
        }
    }
}
