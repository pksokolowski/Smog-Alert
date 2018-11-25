package com.github.pksokolowski.smogalert.job

import android.app.Application
import android.content.Context
import com.github.pksokolowski.smogalert.di.PerApp
import javax.inject.Inject

@PerApp
class SettingsBackupHelper @Inject constructor(private val context: Application) {

    private val sharedPrefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    fun getSensitivity(): Int = sharedPrefs.getInt(KEY_SENSITIVITY, 0)

    fun saveSensitivity(sensitivity: Int) {
        val editor = sharedPrefs.edit()
        editor.putInt(KEY_SENSITIVITY, sensitivity)
        editor.apply()

        BootFinishedReceiver.setBootFinishedReceiverEnabled(context, sensitivity != 0)
    }

    companion object {
        const val FILE_NAME = "settings_backup_data"
        const val KEY_SENSITIVITY = "sensitivity"
    }
}