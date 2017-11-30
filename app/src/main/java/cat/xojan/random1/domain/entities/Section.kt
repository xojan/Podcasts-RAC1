package cat.xojan.random1.domain.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Section(val id: String,
              val title: String,
              var imageUrl: String?,
              val active: Boolean,
              var type: SectionType,
              var programId: String?
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val section = other as Section?

        return id == section!!.id

    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}