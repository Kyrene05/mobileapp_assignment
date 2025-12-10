// avatar/AvatarModels.kt
package com.example.studify.presentation.avatar

import androidx.annotation.DrawableRes
import com.example.studify.R

data class AvatarProfile(
    val baseColor: String = "grey",
    val accessories: List<String> = emptyList(),
    val owned: Set<String> = emptySet()
)

/** default 3 accesories */
data class Accessory(
    val id: String,
    val name: String,
    @DrawableRes val resId: Int
)

val ACCESSORIES_BASE = listOf(
    Accessory(id = "glasses", name = "Glasses", resId = R.drawable.acc_glasses),
    Accessory(id = "bow",     name = "Bow",     resId = R.drawable.acc_bow),
    Accessory(id = "hat",     name = "Hat",     resId = R.drawable.acc_hat)
)

/** default accesories */
val DEFAULT_OWNED = setOf("glasses","bow","hat")
