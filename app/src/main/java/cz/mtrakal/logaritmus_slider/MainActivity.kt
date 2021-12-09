package cz.mtrakal.logaritmus_slider

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import cz.mtrakal.logaritmus_slider.databinding.ActivityMainBinding
import java.text.NumberFormat
import java.util.*
import kotlin.math.ln
import kotlin.math.pow

class MainActivity : AppCompatActivity() {
    /**
     * Min value on slider (expected 1).
     */
    val minValue: MutableLiveData<String> = MutableLiveData<String>("100.0")

    /**
     * Max amount of most expensive car
     */
    val maxValue: MutableLiveData<String> = MutableLiveData<String>("10000.0")

    /**
     * Range between [maxValue] and [minValue].
     */
    private val valuesRange: Double
        get() = (maxValue.value?.toDoubleOrNull() ?: 0.0) - (minValue.value?.toDoubleOrNull() ?: 1.0)

    /**
     * Min value on slider (expected 1).
     */
    val currentValueMin: MutableLiveData<String> = MutableLiveData<String>("")

    /**
     * Max amount of most expensive car
     */
    val currentValueMax: MutableLiveData<String> = MutableLiveData<String>("")


    /**
     * Base of logarithm.
     */
    val logBaseLD: MutableLiveData<String> = MutableLiveData<String>("")
    var logBase: Double = 2.0

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.item = this

        setSliders()

        maxValue.observe(this) { value ->
            logBase = getLogBase(valuesRange)
            logBaseLD.value = logBase.toString()
        }
        minValue.observe(this) { value ->
            logBase = getLogBase(valuesRange)
            logBaseLD.value = logBase.toString()
        }
    }

    private fun setSliders() {
        binding.vSlider.apply {
            values = listOf(0F, 100F)
            addOnChangeListener { slider, value, fromUser ->
                // Set current slider values which we use to currency range for BE
                currentValueMin.value = getSliderCurrentValue(slider.values[0]).toString()
                currentValueMax.value = getSliderCurrentValue(slider.values[1]).toString()

            }
            setLabelFormatter { value: Float ->
                // Format slider thumb value.
                NumberFormat.getCurrencyInstance().apply {
                    maximumFractionDigits = 0
                    currency = Currency.getInstance("USD")
                }.run {
                    format(getSliderCurrentValue(value.toDouble(), logBase, minValue.value?.toDoubleOrNull() ?: 0.0))
                }
            }
        }
    }

    /**
     * Calculate amount from current slider value
     */
    private fun getSliderCurrentValue(value: Float): Double = getSliderCurrentValue(value.toDouble(), logBase, minValue.value?.toDoubleOrNull() ?: 0.0)

    /**
     * Calculate amount from current slider value
     */
    private fun getSliderCurrentValue(value: Double, logBase: Double, minValue: Double = 0.0): Double =
        value.pow(logBase) + minValue

    /**
     * Get Base of logarithm from number and steps which we set.
     * [range] is Max-Min amount from BE.
     * [sliderSteps] how many steps we have on Slider. Expect 100 paces (0-100)
     */
    private fun getLogBase(range: Double = valuesRange, sliderSteps: Double = 100.0): Double = ln(range) / ln(sliderSteps)
}