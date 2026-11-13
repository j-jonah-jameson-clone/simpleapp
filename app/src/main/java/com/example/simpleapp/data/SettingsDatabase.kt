package com.example.simpleapp.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

internal class SettingsDatabase private constructor(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_SETTINGS)
        db.execSQL(SQL_CREATE_FAVORITES)
        db.execSQL(SQL_CREATE_USER_FOLDERS)
        db.execSQL(SQL_CREATE_USER_FOLDER_IMAGES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL(SQL_CREATE_USER_FOLDERS)
            db.execSQL(SQL_CREATE_USER_FOLDER_IMAGES)
        }
    }

    companion object {
        private const val DATABASE_NAME = "simpleapp.db"
        private const val DATABASE_VERSION = 2

        const val TABLE_SETTINGS = "settings"
        const val COLUMN_SETTING_KEY = "setting_key"
        const val COLUMN_SETTING_VALUE = "setting_value"

        const val TABLE_FAVORITES = "favorites"
        const val COLUMN_IMAGE_ID = "image_id"

        const val TABLE_USER_FOLDERS = "user_folders"
        const val COLUMN_FOLDER_ID = "folder_id"
        const val COLUMN_FOLDER_NAME = "folder_name"

        const val TABLE_USER_FOLDER_IMAGES = "user_folder_images"

        private const val SQL_CREATE_SETTINGS =
            "CREATE TABLE IF NOT EXISTS $TABLE_SETTINGS (" +
                    "$COLUMN_SETTING_KEY TEXT PRIMARY KEY NOT NULL, " +
                    "$COLUMN_SETTING_VALUE TEXT NOT NULL)"

        private const val SQL_CREATE_FAVORITES =
            "CREATE TABLE IF NOT EXISTS $TABLE_FAVORITES (" +
                    "$COLUMN_IMAGE_ID INTEGER PRIMARY KEY NOT NULL)"

        private const val SQL_CREATE_USER_FOLDERS =
            "CREATE TABLE IF NOT EXISTS $TABLE_USER_FOLDERS (" +
                    "$COLUMN_FOLDER_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COLUMN_FOLDER_NAME TEXT NOT NULL)"

        private const val SQL_CREATE_USER_FOLDER_IMAGES =
            "CREATE TABLE IF NOT EXISTS $TABLE_USER_FOLDER_IMAGES (" +
                    "$COLUMN_FOLDER_ID INTEGER NOT NULL, " +
                    "$COLUMN_IMAGE_ID INTEGER NOT NULL, " +
                    "PRIMARY KEY ($COLUMN_FOLDER_ID, $COLUMN_IMAGE_ID))"

        @Volatile
        private var instance: SettingsDatabase? = null

        fun get(context: Context): SettingsDatabase =
            instance ?: synchronized(this) {
                instance ?: SettingsDatabase(context.applicationContext).also { instance = it }
            }
    }
}