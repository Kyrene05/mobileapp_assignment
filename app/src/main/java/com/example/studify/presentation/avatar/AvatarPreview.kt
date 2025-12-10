// avatar/AvatarPreview.kt
package com.example.studify.presentation.avatar

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.studify.R


// ---------- Accessory layout spec (scale & position) ----------
data class AccessorySpec(
    @DrawableRes val resId: Int,
    val scale: Float = 1f,
    val anchorX: Float = 0.5f,
    val anchorY: Float = 0.5f,
    val offsetXDp: Float = 0f,
    val offsetYDp: Float = 0f
)


private val ACCESSORY_SPECS: Map<String, AccessorySpec> = mapOf(

    "glasses" to AccessorySpec(
        resId = R.drawable.acc_glasses,
        scale = 0.62f, anchorX = 0.48f, anchorY = 0.38f
    ),
    "bow" to AccessorySpec(
        resId = R.drawable.acc_bow,
        scale = 0.80f, anchorX = 0.49f, anchorY = 0.61f
    ),
    "hat" to AccessorySpec(
        resId = R.drawable.acc_hat,
        scale = 0.50f, anchorX = 0.48f, anchorY = 0.11f
    ),

    "cap" to AccessorySpec(
        resId = R.drawable.acc_cap,
        scale = 0.62f, anchorX = 0.45f, anchorY = 0.12f, offsetXDp = 0f, offsetYDp = -2f
    ),
    "crown" to AccessorySpec(
        resId = R.drawable.acc_crown,
        scale = 0.45f, anchorX = 0.48f, anchorY = 0.12f
    ),
    "shades" to AccessorySpec(
        resId = R.drawable.acc_shades,
        scale = 0.60f, anchorX = 0.48f, anchorY = 0.38f
    ),
    "love" to AccessorySpec(
        resId = R.drawable.acc_love,
        scale = 0.50f, anchorX = 0.49f, anchorY = 0.38f
    ),
    "magichat" to AccessorySpec(
        resId = R.drawable.acc_magichat,
        scale = 0.50f, anchorX = 0.48f, anchorY = 0.13f
    ),
    "gradcap" to AccessorySpec(
        resId = R.drawable.acc_gradcap,
        scale = 0.54f, anchorX = 0.48f, anchorY = 0.13f
    ),
    "gojo" to AccessorySpec(
        resId = R.drawable.acc_gojo,
        scale = 0.65f, anchorX = 0.49f, anchorY = 0.35f
    ),
    "beanie" to AccessorySpec(
        resId = R.drawable.acc_beanie,
        scale = 0.57f, anchorX = 0.49f, anchorY = 0.13f
    ),
    "bowtie" to AccessorySpec(
        resId = R.drawable.acc_bowtie,
        scale = 0.40f, anchorX = 0.50f, anchorY = 0.62f
    )
)

/* ---------- Color layer by mask + tint ---------- */


private val CatColorMap = mapOf(
    "grey" to Color(0xFF9B9B9B),
    "pink" to Color(0xFFFFB3C7),
    "blue" to Color(0xFF77B6FF),
    "red"  to Color(0xFFE74C3C)
)


@Composable
private fun CatColorLayer(
    colorKey: String,
    sizeDp: Dp
) {
    val color = CatColorMap[colorKey] ?: CatColorMap.getValue("grey")
    Image(
        painter = painterResource(R.drawable.cat_fill_mask),
        contentDescription = null,
        colorFilter = ColorFilter.tint(color, BlendMode.SrcIn),
        modifier = Modifier.size(sizeDp)
    )
}

/* ---------- Single accessory renderer ---------- */

@Composable
private fun AccessoryLayer(
    id: String,
    baseSize: Dp,
) {
    val spec = ACCESSORY_SPECS[id] ?: return
    val density = LocalDensity.current

    val basePx = with(density) { baseSize.toPx() }
    val anchorXpx = spec.anchorX * basePx
    val anchorYpx = spec.anchorY * basePx
    val dx = with(density) { spec.offsetXDp.dp.toPx() }
    val dy = with(density) { spec.offsetYDp.dp.toPx() }

    Image(
        painter = painterResource(spec.resId),
        contentDescription = null,
        modifier = Modifier.graphicsLayer {
            translationX = anchorXpx - basePx / 2f + dx
            translationY = anchorYpx - basePx / 2f + dy
            scaleX = spec.scale
            scaleY = spec.scale
        }
    )
}

/* ---------- Avatar preview composable ---------- */

@Composable
fun AvatarPreview(
    profile: AvatarProfile,
    modifier: Modifier = Modifier
) {
    val baseSize = 220.dp

    Box(modifier, contentAlignment = Alignment.Center) {

        CatColorLayer(colorKey = profile.baseColor, sizeDp = baseSize)

        Image(
            painter = painterResource(R.drawable.cat_base),
            contentDescription = null,
            modifier = Modifier.size(baseSize)
        )

        profile.accessories.forEach { id ->
            AccessoryLayer(id = id, baseSize = baseSize)
        }
    }
}

/* ---------- Previews ---------- */

@Preview(
    name = "Avatar – Base only",
    showBackground = true,
    backgroundColor = 0xFFF8E9D2
)
@Composable
private fun PreviewAvatarBaseOnly() {
    MaterialTheme {
        AvatarPreview(
            profile = AvatarProfile(baseColor = "pink"),
            modifier = Modifier.size(260.dp)
        )
    }
}

@Preview(
    name = "Avatar – Pink + Hat",
    showBackground = true,
    backgroundColor = 0xFFF8E9D2
)
@Composable
private fun PreviewAvatarPinkHat() {
    MaterialTheme {
        AvatarPreview(
            profile = AvatarProfile(
                baseColor = "pink",
                accessories = listOf("hat")
            ),
            modifier = Modifier.size(260.dp)
        )
    }
}
