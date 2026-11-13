package com.example.simpleapp.data

data class UserFolder(
    val id: Long,
    val name: String,
    val imageIds: Set<Long>
) {
    val count: Int get() = imageIds.size

    fun contains(imageId: Long): Boolean = imageId in imageIds

    companion object {
        private const val ENCODED_BASE = -1000L

        fun encodeId(folderId: Long): Long = ENCODED_BASE - folderId

        fun decodeId(encodedId: Long): Long = ENCODED_BASE - encodedId

        fun isEncoded(encodedId: Long): Boolean = encodedId <= ENCODED_BASE
    }
}