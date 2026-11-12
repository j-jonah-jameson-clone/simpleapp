package com.example.simpleapp.data

import android.content.ContentValues
import android.content.Context
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase

class UserFoldersStore(context: Context) {

    private val database: SQLiteDatabase = SettingsDatabase.get(context).writableDatabase

    fun loadFolders(): List<UserFolder> {
        val contents = mutableMapOf<Long, MutableSet<Long>>()
        database.query(
            SettingsDatabase.TABLE_USER_FOLDER_IMAGES,
            arrayOf(
                SettingsDatabase.COLUMN_FOLDER_ID,
                SettingsDatabase.COLUMN_IMAGE_ID
            ),
            null, null, null, null, null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                contents.getOrPut(cursor.getLong(0)) { mutableSetOf() }
                    .add(cursor.getLong(1))
            }
        }

        val folders = mutableListOf<UserFolder>()
        database.query(
            SettingsDatabase.TABLE_USER_FOLDERS,
            arrayOf(
                SettingsDatabase.COLUMN_FOLDER_ID,
                SettingsDatabase.COLUMN_FOLDER_NAME
            ),
            null, null, null, null,
            "${SettingsDatabase.COLUMN_FOLDER_NAME} COLLATE NOCASE ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                folders += UserFolder(
                    id = id,
                    name = cursor.getString(1),
                    imageIds = contents[id].orEmpty()
                )
            }
        }
        return folders
    }

    fun createFolder(name: String): UserFolder? {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return null

        val values = ContentValues().apply {
            put(SettingsDatabase.COLUMN_FOLDER_NAME, trimmed)
        }
        val id = database.insert(SettingsDatabase.TABLE_USER_FOLDERS, null, values)
        return if (id > 0) UserFolder(id, trimmed, emptySet()) else null
    }

    fun renameFolder(folderId: Long, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return

        val values = ContentValues().apply {
            put(SettingsDatabase.COLUMN_FOLDER_NAME, trimmed)
        }
        database.update(
            SettingsDatabase.TABLE_USER_FOLDERS,
            values,
            "${SettingsDatabase.COLUMN_FOLDER_ID} = ?",
            arrayOf(folderId.toString())
        )
    }

    fun deleteFolder(folderId: Long) {
        database.delete(
            SettingsDatabase.TABLE_USER_FOLDER_IMAGES,
            "${SettingsDatabase.COLUMN_FOLDER_ID} = ?",
            arrayOf(folderId.toString())
        )
        database.delete(
            SettingsDatabase.TABLE_USER_FOLDERS,
            "${SettingsDatabase.COLUMN_FOLDER_ID} = ?",
            arrayOf(folderId.toString())
        )
    }

    fun containsImage(folderId: Long, imageId: Long): Boolean = DatabaseUtils.longForQuery(
        database,
        "SELECT COUNT(*) FROM ${SettingsDatabase.TABLE_USER_FOLDER_IMAGES} " +
                "WHERE ${SettingsDatabase.COLUMN_FOLDER_ID} = ? " +
                "AND ${SettingsDatabase.COLUMN_IMAGE_ID} = ?",
        arrayOf(folderId.toString(), imageId.toString())
    ) > 0

    fun addImage(folderId: Long, imageId: Long) {
        val values = ContentValues().apply {
            put(SettingsDatabase.COLUMN_FOLDER_ID, folderId)
            put(SettingsDatabase.COLUMN_IMAGE_ID, imageId)
        }
        database.insertWithOnConflict(
            SettingsDatabase.TABLE_USER_FOLDER_IMAGES,
            null,
            values,
            SQLiteDatabase.CONFLICT_IGNORE
        )
    }

    fun removeImage(folderId: Long, imageId: Long) {
        database.delete(
            SettingsDatabase.TABLE_USER_FOLDER_IMAGES,
            "${SettingsDatabase.COLUMN_FOLDER_ID} = ? AND ${SettingsDatabase.COLUMN_IMAGE_ID} = ?",
            arrayOf(folderId.toString(), imageId.toString())
        )
    }

    fun toggleImage(folderId: Long, imageId: Long): Boolean {
        if (containsImage(folderId, imageId)) {
            removeImage(folderId, imageId)
            return false
        }
        addImage(folderId, imageId)
        return true
    }

    fun forgetImages(removedIds: Collection<Long>) {
        if (removedIds.isEmpty()) return

        val placeholders = removedIds.joinToString(separator = ",") { "?" }
        database.delete(
            SettingsDatabase.TABLE_USER_FOLDER_IMAGES,
            "${SettingsDatabase.COLUMN_IMAGE_ID} IN ($placeholders)",
            removedIds.map(Long::toString).toTypedArray()
        )
    }
}