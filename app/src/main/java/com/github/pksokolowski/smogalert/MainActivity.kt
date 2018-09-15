package com.github.pksokolowski.smogalert

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.SeekBar
import com.github.pksokolowski.smogalert.location.LocationAvailabilityHelper
import com.github.pksokolowski.smogalert.utils.AirQualityIndexHelper
import com.github.pksokolowski.smogalert.utils.ErrorExplanationHelper
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
            if (it == null) return@Observer
            val index = it.airQualityIndex
            val textColor = AirQualityIndexHelper.getColor(index, this)

            air_quality_textview.text = AirQualityIndexHelper.getTitle(index, this)
            air_quality_textview.setTextColor(textColor)

            explanationTextView.text = ErrorExplanationHelper.explain(it, this)
        })

        viewModel.getDownloadStatus().observe(this, Observer {
            if (it == true) {
                progressBar.visibility = View.VISIBLE
            } else
                progressBar.visibility = View.INVISIBLE
        })

        viewModel.getSensitivity().observe(this, Observer {
            setting_seek_bar.progress = it ?: 0
        })

        setting_seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, value: Int, fromUser: Boolean) {
                if (!fromUser) return
                viewModel.setMinimumWarningIndexLevel(value)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkCurrentAirQuality()
    }
}
