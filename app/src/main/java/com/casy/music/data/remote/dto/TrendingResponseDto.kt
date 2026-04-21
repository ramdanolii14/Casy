package com.casy.music.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TrendingResponseDto(
    @SerializedName("items") val items: List<TrendingItemDto> = emptyList()
)

data class TrendingItemDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("snippet") val snippet: TrendingSnippetDto = TrendingSnippetDto(),
    @SerializedName("contentDetails") val contentDetails: ContentDetailsDto? = null
)

data class TrendingSnippetDto(
    @SerializedName("title") val title: String = "",
    @SerializedName("channelTitle") val channelTitle: String = "",
    @SerializedName("thumbnails") val thumbnails: ThumbnailsDto = ThumbnailsDto()
)

data class ContentDetailsDto(
    @SerializedName("duration") val duration: String = "" // ISO 8601 e.g. "PT3M45S"
)
