package cz.mtrakal.logarithmslider

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.R
import com.google.android.material.slider.RangeSlider
import cz.mtrakal.logarithmslider.LogarithmicRangeSlider.LogarithmicSliderListener
import kotlin.math.ln
import kotlin.math.pow

/**
 * Slider uses range 0-100 in all cases now.
 * It return logarithmic values for input range in [LogarithmicSliderListener]
 *
 */
class LogarithmicRangeSlider : RangeSlider {
    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, R.attr.sliderStyle)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        registerListeners()

        valueTo = sliderSteps
    }

    private var listener: LogarithmicSliderListener? = null

    /**
     * Min value (expected 1).
     * Amount, not slider range.
     */
    private var minValue: Float = Constants.PriceSlider.DEFAULT_AMOUNT_MIN

    /**
     * Max value
     * Amount, not slider range.
     */
    private var maxValue: Float = Constants.PriceSlider.DEFAULT_AMOUNT_MAX

    /**
     * Range between [maxValue] and [minValue].
     * Amount range, not slider range.
     */
    private val valuesRange: Float
        get() = maxValue - minValue

    /**
     * Base of logarithm.
     */
    private var logBase: Float = Numbers.I1.toFloat()

    /**
     * Steps on slider (affect results for small numbers).
     */
    private var sliderSteps: Float = Constants.PriceSlider.SLIDER_MAX_VALUE
        set(value) {
            field = value.also {
                valueTo = it
            }
        }

    /**
     * Result data for [LogarithmicSliderListener] with both (slider range and amount range) values.
     */
    private val rangeValues: RangeValues
        get() = RangeValues(
            RangeValues.Value(
                minValue.orZero(),
                getSliderCurrentValue(values.minOf { it }),
                values.minOf { it },

                ),
            RangeValues.Value(
                maxValue.orZero(),
                getSliderCurrentValue(values.maxOf { it }),
                values.maxOf { it },
            ),
            valuesRange,
            logBase
        )

    private fun callOnDataChanged(isMoving: Boolean = true) {
        when (isMoving) {
            true -> listener?.onSliderMoving(rangeValues)
            false -> listener?.onSliderStop(rangeValues)
        }
    }

    /**
     * Calculate amount from current slider value
     */
    fun getSliderCurrentValue(
        value: Float,
    ): Float = value.pow(logBase) + minValue.orZero()

    /**
     * Get Base of logarithm from number and steps which we set.
     * [amountRange] is Max amount from BE.
     * [sliderSteps] how many steps we have on Slider. Expect 100 paces (0-100)
     */
    private fun getLogBase(): Float = (ln(valuesRange.toDouble()) / ln(sliderSteps.toDouble())).toFloat()

    /**
     * Calculate range for slider from amount.
     */
    private fun getSliderValueFromAmount(amountValue: Float) = amountValue.pow(1 / logBase)

    /**
     * Set data to slider.
     */
    fun setRangeAndStep(
        /**
         * Min value (amount), not slider value.
         */
        min: Float = Constants.PriceSlider.DEFAULT_AMOUNT_MIN,
        /**
         * Max value (amount), not slider value.
         */
        max: Float = Constants.PriceSlider.DEFAULT_AMOUNT_MAX,
        sliderSteps: Float = Constants.PriceSlider.SLIDER_MAX_VALUE,
//        currentValues: List<CurrentValue> = listOf(
//            Constants.PriceSlider.SLIDER_MIN,
//            Constants.PriceSlider.SLIDER_MAX,
//        ).map { CurrentValue(defaultSliderValue = it) },
        logarithmicSliderListener: LogarithmicSliderListener? = null,
    ) {
        // clear()
//        valueFrom = Constants.PriceSlider.SLIDER_MIN
//        valueTo = Constants.PriceSlider.SLIDER_MAX

        this.sliderSteps = sliderSteps
        minValue = min
        maxValue = max
        logBase = getLogBase()
        // setCustomValues(currentValues)
        listener = logarithmicSliderListener
    }

    fun setCustomValues(
        currentValues: List<CurrentValue> = listOf(
            Constants.PriceSlider.DEFAULT_AMOUNT_MIN,
            Constants.PriceSlider.SLIDER_MAX_VALUE,
        ).map { CurrentValue(defaultSliderValue = it) },
    ) {
        values = currentValues.map { it.current?.let { getSliderValueFromAmount(it) }.ifNullThen { it.defaultSliderValue } }
        callOnDataChanged()
    }

    private fun clear() {
        listener = null
        values = listOf(0F) // Clean values before set min/max to avoid OutOfBounds exception.
    }

    private fun registerListeners() {
        addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: RangeSlider) {
                /* no-op */
            }

            override fun onStopTrackingTouch(slider: RangeSlider) {
                callOnDataChanged(false)
            }
        })

        addOnChangeListener { slider, value, fromUser ->
            if (fromUser) {
                callOnDataChanged()
            }
        }
    }

    data class CurrentValue(val current: Float? = null, val defaultSliderValue: Float)

    data class RangeValues(
        val min: Value,
        val max: Value,
        val valuesRange: Float,
        val logBase: Float
    ) {
        data class Value(
            val extreme: Float,
            val current: Float,
            val sliderValue: Float,
        ) {
            val isExtreme: Boolean
                get() = current == extreme
        }
    }

    interface LogarithmicSliderListener {
        fun onSliderMoving(rangeValues: RangeValues)
        fun onSliderStop(rangeValues: RangeValues)
    }
}

object Constants {
    object PriceSlider {
        const val DEFAULT_AMOUNT_MIN = 0F
        const val DEFAULT_AMOUNT_MAX = 100000F

        const val SLIDER_MAX_VALUE = 100F
    }
}

private object Numbers {
    const val I1 = 1
}

private fun Float?.orZero(): Float = this ?: 0F

/**
 * Return [T] or [block] when [T] is `null`.
 */
private inline fun <T> T?.ifNullThen(block: () -> T): T = this ?: block()
