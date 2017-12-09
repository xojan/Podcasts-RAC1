package cat.xojan.random1.domain.model

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
class Images(@Json(name = "person-small") val imageUrl: String) : Parcelable