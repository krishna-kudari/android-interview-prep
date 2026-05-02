package com.example.interview.recyclerlab

import android.os.Bundle
import android.view.Choreographer
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.interview.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.button.MaterialButton
import kotlin.collections.ArrayDeque
import kotlin.math.roundToInt

/**
 * Hands-on lab: ViewHolder reuse, [androidx.recyclerview.widget.DiffUtil], shared
 * [RecyclerView.RecycledViewPool], layout/prefetch tuning, and frame-time sampling vs 16ms/32ms budgets.
 */
class RecyclerViewLabActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var textFrameMetrics: TextView
    private lateinit var textPoolStats: TextView
    private lateinit var textScrollState: TextView
    private lateinit var stressSwitch: MaterialSwitch

    private var nextRowId = 100L
    private var currentItems: List<LabListItem> = emptyList()

    private val frameDeltasMs = ArrayDeque<Double>()
    private var lastFrameTimeNs: Long = 0L
    private var scrollStateLabel: String = "IDLE"

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNs: Long) {
            if (lastFrameTimeNs != 0L) {
                val deltaMs = (frameTimeNs - lastFrameTimeNs) / 1_000_000.0
                if (deltaMs > 0) {
                    if (frameDeltasMs.size >= 120) frameDeltasMs.removeFirst()
                    frameDeltasMs.addLast(deltaMs)
                }
            }
            lastFrameTimeNs = frameTimeNs
            updateInstrumentationUi()
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            scrollStateLabel = when (newState) {
                RecyclerView.SCROLL_STATE_IDLE -> "IDLE"
                RecyclerView.SCROLL_STATE_DRAGGING -> "DRAGGING"
                RecyclerView.SCROLL_STATE_SETTLING -> "SETTLING"
                else -> newState.toString()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recycler_lab)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.recycler_lab_root)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar_recycler_lab)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        textFrameMetrics = findViewById(R.id.text_frame_metrics)
        textPoolStats = findViewById(R.id.text_pool_stats)
        textScrollState = findViewById(R.id.text_scroll_state)
        stressSwitch = findViewById(R.id.switch_stress_bind)

        recyclerView = findViewById(R.id.recycler_lab_main)
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            isItemPrefetchEnabled = true
            initialPrefetchItemCount = 4
        }
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(4)
        recyclerView.addOnScrollListener(scrollListener)

        tuneRecycledViewPool(recyclerView.recycledViewPool)

        val adapter = RecyclerLabAdapter(recyclerView) { stressSwitch.isChecked }
        recyclerView.adapter = adapter

        currentItems = buildInitialList()
        adapter.submitList(currentItems.toList())

        findViewById<MaterialButton>(R.id.btn_add_row).setOnClickListener { addSimpleRow() }
        findViewById<MaterialButton>(R.id.btn_remove_simple).setOnClickListener { removeLastSimpleRow() }
        findViewById<MaterialButton>(R.id.btn_shuffle).setOnClickListener { shuffleSimpleRows() }
        findViewById<MaterialButton>(R.id.btn_bump_values).setOnClickListener { bumpSimpleValues() }
        findViewById<MaterialButton>(R.id.btn_reset_list).setOnClickListener {
            nextRowId = 100L
            currentItems = buildInitialList()
            (recyclerView.adapter as RecyclerLabAdapter).submitList(currentItems.toList())
        }
    }

    override fun onStart() {
        super.onStart()
        lastFrameTimeNs = 0L
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    override fun onStop() {
        Choreographer.getInstance().removeFrameCallback(frameCallback)
        super.onStop()
    }

    private fun tuneRecycledViewPool(pool: RecyclerView.RecycledViewPool) {
        pool.setMaxRecycledViews(RecyclerLabViewTypes.EDUCATION, 2)
        pool.setMaxRecycledViews(RecyclerLabViewTypes.SECTION, 4)
        pool.setMaxRecycledViews(RecyclerLabViewTypes.SIMPLE, 12)
        pool.setMaxRecycledViews(RecyclerLabViewTypes.NESTED, 4)
        // Horizontal chip rows use viewType 0 on the inner adapter.
        pool.setMaxRecycledViews(0, 20)
    }

    private fun buildInitialList(): List<LabListItem> {
        return listOf(
            LabListItem.EducationCard(
                1L,
                getString(R.string.recycler_lab_education_title),
                getString(R.string.recycler_lab_education_body),
            ),
            LabListItem.SectionHeader(2L, getString(R.string.recycler_lab_section_diffutil)),
            LabListItem.SimpleRow(
                10L,
                getString(R.string.recycler_lab_row_bind_title),
                12,
                ContextCompat.getColor(this, R.color.recycler_lab_accent_1),
            ),
            LabListItem.SimpleRow(
                11L,
                getString(R.string.recycler_lab_row_payload_title),
                48,
                ContextCompat.getColor(this, R.color.recycler_lab_accent_2),
            ),
            LabListItem.SimpleRow(
                12L,
                getString(R.string.recycler_lab_row_stable_title),
                76,
                ContextCompat.getColor(this, R.color.recycler_lab_accent_3),
            ),
            LabListItem.SectionHeader(3L, getString(R.string.recycler_lab_section_nested)),
            LabListItem.NestedCarouselRow(
                20L,
                (1..24).map { idx -> getString(R.string.recycler_lab_chip_label, idx) },
            ),
        )
    }

    private fun addSimpleRow() {
        val id = nextRowId++
        val colorRes = when (id % 4) {
            0L -> R.color.recycler_lab_accent_1
            1L -> R.color.recycler_lab_accent_2
            2L -> R.color.recycler_lab_accent_3
            else -> R.color.recycler_lab_accent_4
        }
        val newRow = LabListItem.SimpleRow(
            id,
            getString(R.string.recycler_lab_dynamic_row_title, id),
            5,
            ContextCompat.getColor(this, colorRes),
        )
        currentItems = currentItems + newRow
        (recyclerView.adapter as RecyclerLabAdapter).submitList(currentItems.toList())
    }

    private fun removeLastSimpleRow() {
        val idx = currentItems.indexOfLast { it is LabListItem.SimpleRow }
        if (idx < 0) return
        currentItems = currentItems.filterIndexed { i, _ -> i != idx }
        (recyclerView.adapter as RecyclerLabAdapter).submitList(currentItems.toList())
    }

    private fun shuffleSimpleRows() {
        val simple = currentItems.filterIsInstance<LabListItem.SimpleRow>()
        if (simple.size <= 1) return
        val shuffled = simple.shuffled().iterator()
        currentItems = currentItems.map { item ->
            if (item is LabListItem.SimpleRow) shuffled.next() else item
        }
        (recyclerView.adapter as RecyclerLabAdapter).submitList(currentItems.toList())
    }

    private fun bumpSimpleValues() {
        currentItems = currentItems.map { item ->
            if (item is LabListItem.SimpleRow) {
                item.copy(value = (item.value + 7) % 101)
            } else {
                item
            }
        }
        (recyclerView.adapter as RecyclerLabAdapter).submitList(currentItems.toList())
    }

    private fun updateInstrumentationUi() {
        val deltas = frameDeltasMs
        if (deltas.isEmpty()) {
            textFrameMetrics.text = getString(R.string.recycler_lab_metrics_warmup)
        } else {
            val last = deltas.last()
            val avg = deltas.average()
            val over16 = deltas.count { it > 16.67 }
            val over32 = deltas.count { it > 33.33 }
            val pct16 = (over16 * 100.0 / deltas.size).roundToInt().coerceIn(0, 100)
            val pct32 = (over32 * 100.0 / deltas.size).roundToInt().coerceIn(0, 100)
            textFrameMetrics.text = getString(
                R.string.recycler_lab_metrics_template,
                last,
                avg,
                pct16,
                pct32,
            )
        }

        val pool = recyclerView.recycledViewPool
        textPoolStats.text = getString(
            R.string.recycler_lab_pool_template,
            pool.getRecycledViewCount(RecyclerLabViewTypes.EDUCATION),
            pool.getRecycledViewCount(RecyclerLabViewTypes.SECTION),
            pool.getRecycledViewCount(RecyclerLabViewTypes.SIMPLE),
            pool.getRecycledViewCount(RecyclerLabViewTypes.NESTED),
            pool.getRecycledViewCount(0),
        )
        textScrollState.text = getString(R.string.recycler_lab_scroll_template, scrollStateLabel)
    }
}
