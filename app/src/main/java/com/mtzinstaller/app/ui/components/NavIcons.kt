package com.mtzinstaller.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** Ikon garis membulat khusus untuk navbar (lebih halus dari ikon bawaan). */
object NavIcons {
    private fun line(name: String, vararg paths: String): ImageVector =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
            paths.forEach {
                addPath(
                    pathData = addPathNodes(it),
                    fill = null,
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 1.9f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                )
            }
        }.build()

    val Home = line(
        "nav_home",
        "M3.5 10.2 11.3 3.6a1.1 1.1 0 0 1 1.4 0l7.8 6.6V19a2 2 0 0 1-2 2h-3.3v-5.2a1 1 0 0 0-1-1h-4.4a1 1 0 0 0-1 1V21H5.5a2 2 0 0 1-2-2z",
    )
    val Explore = line(
        "nav_explore",
        "M12 3a9 9 0 1 0 0 18a9 9 0 1 0 0-18z",
        "M15.6 8.4l-1.9 5.3-5.3 1.9 1.9-5.3z",
    )
    val Collection = line(
        "nav_collection",
        "M8 3.5h8.5a2 2 0 0 1 2 2V16",
        "M5.5 6.5h9a2 2 0 0 1 2 2V19a2 2 0 0 1-2 2h-9a2 2 0 0 1-2-2V8.5a2 2 0 0 1 2-2z",
        "M7 15.5l2.4-2.6 2.1 2 1.6-1.4 1.4 2",
    )
    val Profile = line(
        "nav_help",
        "M12 3a9 9 0 1 0 0 18a9 9 0 1 0 0-18z",
        "M9.6 9.3a2.5 2.5 0 1 1 3.3 2.3c-.6.3-.9.8-.9 1.4v.5",
        "M12 16.6v.01",
    )
    val Plus = line("nav_plus", "M12 5.5v13", "M5.5 12h13")
}
