package com.github.pksokolowski.smogalert

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.github.pksokolowski.smogalert.location.LocationAvailabilityHelper
import com.github.pksokolowski.smogalert.notifications.NotificationHelper
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

    @Inject
    lateinit var notificationHelper: NotificationHelper

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

            notificationHelper.showAlert(it)
        })

        a_button.setOnClickListener {
            viewModel.checkCurrentAirQuality()
        }

        enable_alarms_button.setOnClickListener{
            viewModel.setAlarmsEnabled(true)
        }

        disable_alarms_button.setOnClickListener {
            viewModel.setAlarmsEnabled(false)
        }
    }
}
