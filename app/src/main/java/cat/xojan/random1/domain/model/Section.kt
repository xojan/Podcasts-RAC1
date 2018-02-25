package cat.xojan.random1.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Section(
        val id: String,
        val title: String,
        val imageUrl: String?,
        val programId: String?
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