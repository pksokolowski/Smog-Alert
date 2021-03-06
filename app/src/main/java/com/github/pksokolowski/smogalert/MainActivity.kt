package com.github.pksokolowski.smogalert

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.SeekBar
import com.github.pksokolowski.smogalert.db.AirQualityLog
import com.github.pksokolowski.smogalert.location.LocationAvailabilityHelper
import com.github.pksokolowski.smogalert.utils.AirQualityIndexHelper
import com.github.pksokolowski.smogalert.utils.ErrorExplanationHelper
import com.github.pksokolowski.smogalert.utils.SensitivityLevelsHelper
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var locationAvailabilityHelper: LocationAvailabilityHelper

    @Inject
    lateinit var errorExplanationHelper: ErrorExplanationHelper

    @Inject
    lateinit var airQualityIndexHelper: AirQualityIndexHelper

    private lateinit var viewModel: MainActivityViewModel

    private var isLocationAccessRequestPending = false

    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainActivityViewModel::class.java)

        viewModel.getAirQualityInfo().observe(this, Observer {
            if (it == null) return@Observer

            val textColor = airQualityIndexHelper.getColor(it)
            air_quality_textview.text = airQualityIndexHelper.getTitle(it)
            air_quality_textview.setTextColor(textColor)

            explanationTextView.text = errorExplanationHelper.explain(it)

            if (it.errorCode == AirQualityLog.ERROR_CODE_LOCATION_MISSING) {
                promptUserAboutLocationAccessIfMissing(it.id == 1L)
            }
        })

        viewModel.getDownloadStatus().observe(this, Observer {
            if (it == true) {
                progressBar.visibility = View.VISIBLE
            } else
                progressBar.visibility = View.INVISIBLE
        })

        viewModel.getSensitivity().observe(this, Observer {
            val sensitivity = it ?: 0
            setting_seek_bar.progress = sensitivity
            warningLevelTitle.text = SensitivityLevelsHelper.getTitle(sensitivity, this)
            warningLevelTitle.setTextColor(SensitivityLevelsHelper.getColor(sensitivity, this))
            warningLevelDescription.text = SensitivityLevelsHelper.explain(sensitivity, this)
        })

        setting_seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, value: Int, fromUser: Boolean) {
                if (!fromUser) return
                viewModel.setSensitivity(value)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
    }

    private fun promptUserAboutLocationAccessIfMissing(withoutDelay: Boolean) {
        if (!isLocationAccessRequestPending) {
            isLocationAccessRequestPending = true

            handler.postDelayed({
                locationAvailabilityHelper.checkAvailabilityAndPromptUserIfNeeded(this)
                isLocationAccessRequestPending = false
            }, if (withoutDelay) 0L else 5000L)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkCurrentAirQuality()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
        isLocationAccessRequestPending = false
    }
}
