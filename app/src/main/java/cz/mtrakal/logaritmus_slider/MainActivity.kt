package cz.mtrakal.logaritmus_slider

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import cz.mtrakal.logaritmus_slider.databinding.ActivityMainBinding
import java.text.NumberFormat
import java.util.*
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log
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
        get() = (maxValue.value?.toDoubleOrNull() ?: 0.0) - (minValue.value?.toDoubleOrNull() ?: 1.0) + 1 // + 1 because log fun have start at 1.

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
    val logBase: MutableLiveData<String> = MutableLiveData<String>("2.0")


    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityMainBinding?>(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.item = this

        setSliders()

        maxValue.observe(this) { value ->
            logBase.value = getLogBase(valuesRange).toString()
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
                    format(getSliderCurrentValue(value))
                }
            }
        }
    }

    /**
     * Calculate amount from current slider value
     */
    private fun getSliderCurrentValue(value: Float): Double =
        (logBase.value?.toDoubleOrNull() ?: 2.0).pow(value.toDouble()) + (minValue.value?.toDoubleOrNull() ?: 0.0) - 1.0 // -1.0 because log starts at 1.

    /**
     * Get Base of logarithm from number and steps which we set.
     * [maxValue] is Max amount from BE.
     * [sliderSteps] how many steps we have on Slider. Expect 100 paces (0-100)
     */
    private fun getLogBase(maxValue: Double = valuesRange, sliderSteps: Double = 100.0): Double = exp(ln(maxValue) / sliderSteps)
}