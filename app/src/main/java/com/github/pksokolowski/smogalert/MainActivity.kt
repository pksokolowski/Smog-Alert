package com.github.pksokolowski.smogalert

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.SeekBar
import com.github.pksokolowski.smogalert.job.AirCheckParams.Companion.INDEX_LEVEL_UNREACHABLE
import com.github.pksokolowski.smogalert.location.LocationAvailabilityHelper
import com.github.pksokolowski.smogalert.utils.TimeHelper
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var locationAvailabilityHelper: LocationAvailabilityHelper

    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainActivityViewModel::class.java)

        locationAvailabilityHelper.checkAvailabilityAndPromptUserIfNeeded(this)


        viewModel.getAirQualityInfo().observe(this, Observer {
            if (it == null) {
                textView.text = "log is null"
                return@Observer
            }
            val timeStamp = TimeHelper.getTimeStampString(it.timeStamp)
            textView.text = "${it.toString()}\n\n$timeStamp"
        })

        a_button.setOnClickListener {
            viewModel.checkCurrentAirQuality()
        }

        viewModel.getWarningIndexLevel().observe(this, Observer {
            val minimumWarningIndexLevel = it ?: INDEX_LEVEL_UNREACHABLE
            val translatedValue = if (minimumWarningIndexLevel == INDEX_LEVEL_UNREACHABLE) 0
            else 5 - minimumWarningIndexLevel

            setting_seek_bar.progress = translatedValue
        })

        setting_seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, value: Int, fromUser: Boolean) {
                if (!fromUser) return
                // translate UI chosen value to an airQualityIndex warning threshold
                val warningLevel = if (value == 0) INDEX_LEVEL_UNREACHABLE else 5 - value

                viewModel.setMinimumWarningIndexLevel(warningLevel)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
    }
}
