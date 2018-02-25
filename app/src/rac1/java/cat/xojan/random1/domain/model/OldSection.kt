package cat.xojan.random1.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OldSection(val id: String,
                      val title: String,
                      var imageUrl: String?,
                      val active: Boolean,
                      var type: OldSectionType,
                      var programId: String?
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val section = other as OldSection?

        return id == section!!.id

    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}