package com.wapps1.redcarga.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wapps1.redcarga.core.ui.theme.RcColor2
import com.wapps1.redcarga.core.ui.theme.RcColor3
import com.wapps1.redcarga.core.ui.theme.RcColor5
import com.wapps1.redcarga.core.ui.theme.White


@Composable
fun RcBackground(
    modifier: Modifier = Modifier,
    blurDp: Dp = 250.dp
) {
    Box(modifier = modifier.background(White)) {
        val density = LocalDensity.current
        val blurPx = with(density) { blurDp.toPx() }

        Canvas(
            Modifier
                .fillMaxSize()
                .drawWithCache {
                    val fwPaint = Paint().asFrameworkPaint().apply {
                        isAntiAlias = true
                        maskFilter = android.graphics.BlurMaskFilter(
                            blurPx,
                            android.graphics.BlurMaskFilter.Blur.NORMAL
                        )
                    }

                    val w = size.width
                    val h = size.height

                    val redRect = android.graphics.RectF(
                        -1.00f * w,
                        0.17f * h,
                        -1.00f * w + 1.54f * w,
                        0.17f * h + 1.05f * h
                    )

                    val orangeRect = android.graphics.RectF(
                        0.40f * w,
                        -0.33f * h,
                        0.40f * w + 1.39f * w,
                        -0.33f * h + 0.89f * h
                    )

                    val pinkRect = android.graphics.RectF(
                        0.54f * w,
                        0.40f * h,
                        0.54f * w + 1.47f * w,
                        0.40f * h + 0.71f * h
                    )

                    onDrawBehind {
                        fwPaint.color = RcColor5.copy(alpha = 0.65f).toArgb()
                        drawIntoCanvas { it.nativeCanvas.drawOval(redRect, fwPaint) }
                        fwPaint.color = RcColor2.copy(alpha = 0.50f).toArgb()
                        drawIntoCanvas { it.nativeCanvas.drawOval(orangeRect, fwPaint) }
                        fwPaint.color = RcColor3.copy(alpha = 0.90f).toArgb()
                        drawIntoCanvas { it.nativeCanvas.drawOval(pinkRect, fwPaint) }
                    }
                }
        ) {}
    }
}
