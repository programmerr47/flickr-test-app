package com.github.programmerr47.flickrawesomeclient

import com.google.gson.annotations.SerializedName

/**
 * We need to add this silly class because of response for search list is not
 * {"page": 1, ..., "photo":[...]}, but
 * {"photos": {"page": 1, ..., "photo":[...]}}
 * <br><br>
 * Unfortunately there is no conversion strategies for responses built in retrofit
 * to attach each strategy for each concrete request.
 * <br><br>
 * So as far as I know, you need to play with Interceptors, or with Converter Factories,
 * or with Type Adapters or with Deserializers. All those paths are long roads for just one
 * query in the sample task. But lack of this feature is undoubtedly is a huge flaw of Retrofit
 * <br><br>
 * That's why we have this useless wrapper :(
 */
data class PhotoListResponse(
        @SerializedName("photos") val body: PhotoList
)

data class PhotoList(
        val page: Int,
        val pages: Int,
        @SerializedName("perpage") val perPage: Int,
        val total: Int,
        @SerializedName("photo") val list: List<Photo>
) {
    fun canTakeMore() = page < pages

    operator fun plus(other: PhotoList) = PhotoList(
            page + 1,
            pages,
            perPage,
            total,
            list + other.list
    )
}

data class Photo(
        val id: String,
        val ownerId: String,
        val secret: String,
        val serverId: String,
        val farmId: String,
        val title: String,
        val isPublic: Boolean,
        val isFriend: Boolean,
        val isFamily: Boolean
) {
    fun generateUrl() = "https://farm$farmId.staticflickr.com/$serverId/${id}_$secret.jpg"
}