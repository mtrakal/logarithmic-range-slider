package cz.mtrakal.logarithmslider

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import cz.mtrakal.logarithmslider.databinding.ActivityMainBinding
import java.text.NumberFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    /**
     * Min amount value.
     */
    val amountMinValue: MutableLiveData<String> = MutableLiveData("0.0")

    /**
     * Max amount value.
     */
    val amountMaxValue: MutableLiveData<String> = MutableLiveData(Constants.PriceSlider.DEFAULT_AMOUNT_MAX.toString())

    /**
     * Slider steps (max value on slider).
     */
    val sliderSteps: MutableLiveData<String> = MutableLiveData(Constants.PriceSlider.SLIDER_MAX_VALUE.toString())

    /**
     * Range between [maxValue] and [minValue].
     */
    private val valuesRange: LiveData<Float> = combineWith(amountMinValue, amountMaxValue) { minValue, maxValue ->
        (maxValue.toFloatOrNull() ?: 0F) - (minValue.toFloatOrNull() ?: 0F)
    }

    private val sliderListener = object : LogarithmicRangeSlider.LogarithmicSliderListener {
        override fun onSliderMoving(rangeValues: LogarithmicRangeSlider.RangeValues) {
            update(rangeValues)
        }

        override fun onSliderStop(rangeValues: LogarithmicRangeSlider.RangeValues) {
            update(rangeValues)
        }

        private fun update(rangeValues: LogarithmicRangeSlider.RangeValues) {
            binding.apply {
                vLogBase.setText(rangeValues.logBase.toString())
                currentSliderValueMin.setText(rangeValues.min.sliderValue.toString())
                currentSliderValueMax.setText(rangeValues.max.sliderValue.toString())
                currentAmountValueMin.setText(rangeValues.min.current.toString())
                currentAmountValueMax.setText(rangeValues.max.current.toString())
            }
        }
    }

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.item = this

        initSlider()

        valuesRange.observe(this) {
            updateSlider()
        }

        sliderSteps.observe(this) {
            setSliderSteps()
            updateSlider()
        }
    }

    private fun setSliderSteps() {
        binding.vSlider.apply {
//            valueFrom = Constants.PriceSlider.SLIDER_MIN
            valueTo = sliderSteps.value?.toFloatOrNull() ?: Constants.PriceSlider.SLIDER_MAX_VALUE

            values = listOf(
                Constants.PriceSlider.DEFAULT_AMOUNT_MIN,
                sliderSteps.value?.toFloatOrNull() ?: Constants.PriceSlider.SLIDER_MAX_VALUE
            )
        }
    }

    private fun updateSlider() {
        binding.vSlider.setRangeAndStep(
            amountMinValue.value?.toFloatOrNull() ?: 0F,
            amountMaxValue.value?.toFloatOrNull() ?: 0F,
            sliderSteps.value?.toFloatOrNull() ?: Constants.PriceSlider.SLIDER_MAX_VALUE,
//            emptyList(),
            sliderListener,
        )
    }

    private fun initSlider() {
        binding.vSlider.apply {
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
}

private fun <T, K, R> combineWith(
    source1: LiveData<T>,
    source2: LiveData<K>,
    block: (T, K) -> R,
): LiveData<R> {
    val result = MediatorLiveData<R>()
    result.addSource(source1) {
        result.value = block(source1.value!!, source2.value!!)
    }
    result.addSource(source2) {
        result.value = block(source1.value!!, source2.value!!)
    }
    return result
}
