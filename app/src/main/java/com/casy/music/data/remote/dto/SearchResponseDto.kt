package com.casy.music.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SearchResponseDto(
    @SerializedName("items") val items: List<SearchItemDto> = emptyList()
)

data class SearchItemDto(
    @SerializedName("id") val id: SearchIdDto,
    @SerializedName("snippet") val snippet: SnippetDto
)

data class SearchIdDto(
    @SerializedName("videoId") val videoId: String = ""
)

data class SnippetDto(
    @SerializedName("title") val title: String = "",
    @SerializedName("channelTitle") val channelTitle: String = "",
    @SerializedName("thumbnails") val thumbnails: ThumbnailsDto = ThumbnailsDto()
)

data class ThumbnailsDto(
    @SerializedName("medium") val medium: ThumbnailDto? = null,
    @SerializedName("high") val high: ThumbnailDto? = null,
    @SerializedName("default") val default: ThumbnailDto? = null
) {
    fun getBest(): String =
        high?.url ?: medium?.url ?: default?.url ?: ""
}

data class ThumbnailDto(
    @SerializedName("url") val url: String = ""
)
