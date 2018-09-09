package com.github.programmerr47.flickrawesomeclient.net

import com.github.programmerr47.flickrawesomeclient.models.Photo
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * We need that deserializer, because we want to build consistent model, that
 * follow naming rules and uses correct types for it's field.
 *
 * Unfortunately Flickr API provides 0/1 for booleans and also using not consistent names.
 * For example *serverId* is just *server* for that model, but on other doc pages
 * it is still *serverId* (or *ispublic* field instead of *isPublic*). And if for last
 * we can use {@link @SerializedName}, then for first one the best decisions
 * (if we want to have consistent models) is to use JsonDeserializer
 *
 * Also we dont need for explicit constants for json fields, because this logic is internal and fully encapsulated.
 */
class PhotoDeserializer : JsonDeserializer<Photo> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Photo {
        val jsonObj = json.asJsonObject
        return Photo(
                id = jsonObj["id"].asString,
                ownerId = jsonObj["owner"].asString,
                secret = jsonObj["secret"].asString,
                serverId = jsonObj["server"].asString,
                farmId = jsonObj["farm"].asString,
                title = jsonObj["title"].asString,
                isPublic = intToBoolean(jsonObj["ispublic"]),
                isFriend = intToBoolean(jsonObj["isfriend"]),
                isFamily = intToBoolean(jsonObj["isfamily"])
        )
    }

    private fun intToBoolean(element: JsonElement) = element.asInt > 0
}