package com.github.pksokolowski.smogalert.db

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.RoomDatabase

object CreateTriggersCallback: RoomDatabase.Callback(){
    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        db.execSQL("CREATE TRIGGER IF NOT EXISTS delete_old_aq_logs AFTER INSERT ON air_quality_logs BEGIN DELETE FROM air_quality_logs WHERE id NOT IN (SELECT id FROM air_quality_logs ORDER BY id DESC LIMIT 3); END")
        db.execSQL("CREATE TRIGGER IF NOT EXISTS delete_old_update_logs AFTER INSERT ON stations_update_logs BEGIN DELETE FROM stations_update_logs WHERE id NOT IN (SELECT id FROM stations_update_logs ORDER BY id DESC LIMIT 1); END")
    }
}