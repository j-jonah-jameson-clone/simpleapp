package com.example.simpleapp.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

internal class SettingsDatabase private constructor(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_SETTINGS)
        db.execSQL(SQL_CREATE_FAVORITES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SETTINGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FAVORITES")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_NAME = "simpleapp.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_SETTINGS = "settings"
        const val COLUMN_SETTING_KEY = "setting_key"
        const val COLUMN_SETTING_VALUE = "setting_value"
        const val TABLE_FAVORITES = "favorites"
        const val COLUMN_IMAGE_ID = "image_id"

        private const val SQL_CREATE_SETTINGS =
            "CREATE TABLE IF NOT EXISTS $TABLE_SETTINGS (" +
                    "$COLUMN_SETTING_KEY TEXT PRIMARY KEY NOT NULL, " +
                    "$COLUMN_SETTING_VALUE TEXT NOT NULL)"

        private const val SQL_CREATE_FAVORITES =
            "CREATE TABLE IF NOT EXISTS $TABLE_FAVORITES (" +
                    "$COLUMN_IMAGE_ID INTEGER PRIMARY KEY NOT NULL)"

        @Volatile
        private var instance: SettingsDatabase? = null

        fun get(context: Context): SettingsDatabase =
            instance ?: synchronized(this) {
                instance ?: SettingsDatabase(context.applicationContext).also { instance = it }
            }
    }
}