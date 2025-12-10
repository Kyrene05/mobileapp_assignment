package com.example.studify.presentation.avatar

import androidx.annotation.DrawableRes
import com.example.studify.R

data class AccessoryItem(
    val id: String,
    val name: String,
    @DrawableRes val resId: Int,
    val price: Int
)

val ACCESSORIES = listOf(
    AccessoryItem(
        id = "cap",
        name = "Cap",
        resId = R.drawable.acc_cap,
        price = 350
    ),
    AccessoryItem(
        id = "crown",
        name = "Crown",
        resId = R.drawable.acc_crown,
        price = 600
    ),
    AccessoryItem(
        id = "shades",
        name = "Shades",
        resId = R.drawable.acc_shades,
        price = 500
    ),
    AccessoryItem(
        id = "love",
        name = "Love you",
        resId = R.drawable.acc_love,
        price = 280
    ),
    AccessoryItem(
        id = "magichat",
        name = "Magic hat",
        resId = R.drawable.acc_magichat,
        price = 450
    ),
    AccessoryItem(
        id = "gradcap",
        name = "Graduate Cap",
        resId = R.drawable.acc_gradcap,
        price = 380
    ),
    AccessoryItem(
        id = "gojo",
        name = "Gojo Satoru",
        resId = R.drawable.acc_gojo,
        price = 650
    ),
    AccessoryItem(
        id = "beanie",
        name = "Beanie",
        resId = R.drawable.acc_beanie,
        price = 320
    ),
    AccessoryItem(
        id = "bowtie",
        name = "Bow tie",
        resId = R.drawable.acc_bowtie,
        price = 550
    )
)
