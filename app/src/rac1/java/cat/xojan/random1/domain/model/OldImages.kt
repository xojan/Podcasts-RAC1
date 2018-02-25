package cat.xojan.random1.domain.model

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
class OldImages(@Json(name = "person-small") val imageUrl: String,
                @Json(name = "app") val bigImageUrl: String) : Parcelable