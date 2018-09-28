package com.github.pksokolowski.smogalert.database

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.RoomDatabase

object CreateTriggersCallback: RoomDatabase.Callback(){
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        db.execSQL("CREATE TRIGGER delete_old_aq_logs AFTER INSERT ON air_quality_logs BEGIN DELETE FROM air_quality_logs WHERE id NOT IN (SELECT id FROM air_quality_logs ORDER BY id DESC LIMIT 2); END")
        db.execSQL("CREATE TRIGGER delete_old_update_logs AFTER INSERT ON stations_update_logs BEGIN DELETE FROM stations_update_logs WHERE id NOT IN (SELECT id FROM stations_update_logs ORDER BY id DESC LIMIT 1); END")
    }
}