package cat.xojan.random1.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Program(
        val id: String,
        val title: String?,
        val smallImageUrl: String?,
        val bigImageUrl: String?,
        val sections: List<Section>
): Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val program = other as OldProgram?

        return id == program!!.id

    }

    override fun hashCode(): Int = id.hashCode()
}