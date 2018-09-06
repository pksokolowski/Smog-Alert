package com.github.pksokolowski.smogalert.repository

import com.github.pksokolowski.smogalert.database.StationsDao
import com.github.pksokolowski.smogalert.di.PerApp
import javax.inject.Inject

@PerApp
class StationsRepository @Inject constructor(stationsDao: StationsDao){
}