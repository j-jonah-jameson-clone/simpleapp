package com.example.simpleapp.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase

class SettingsStore(context: Context) {

    private val database: SQLiteDatabase = SettingsDatabase.get(context).writableDatabase

    var gridColumns: Int
        get() = readInt(KEY_GRID_COLUMNS, DEFAULT_GRID_COLUMNS)
            .coerceIn(MIN_GRID_COLUMNS, MAX_GRID_COLUMNS)
        set(value) = writeValue(
            KEY_GRID_COLUMNS,
            value.coerceIn(MIN_GRID_COLUMNS, MAX_GRID_COLUMNS).toString()
        )

    private fun readInt(key: String, defaultValue: Int): Int =
        readString(key)?.toIntOrNull() ?: defaultValue

    private fun readString(key: String): String? =
        database.query(
            SettingsDatabase.TABLE_SETTINGS,
            arrayOf(SettingsDatabase.COLUMN_SETTING_VALUE),
            "${SettingsDatabase.COLUMN_SETTING_KEY} = ?",
            arrayOf(key),
            null, null, null
        ).use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }

    private fun writeValue(key: String, value: String) {
        val values = ContentValues().apply {
            put(SettingsDatabase.COLUMN_SETTING_KEY, key)
            put(SettingsDatabase.COLUMN_SETTING_VALUE, value)
        }
        database.insertWithOnConflict(
            SettingsDatabase.TABLE_SETTINGS,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    companion object {
        private const val KEY_GRID_COLUMNS = "grid_columns"
        const val MIN_GRID_COLUMNS = 2
        const val MAX_GRID_COLUMNS = 5
        const val DEFAULT_GRID_COLUMNS = 3
    }
}