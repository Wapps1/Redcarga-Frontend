package com.wapps1.redcarga.features.auth.presentation.views
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wapps1.redcarga.R
import com.wapps1.redcarga.core.ui.theme.RcColor2
import com.wapps1.redcarga.core.ui.theme.RcColor3
import com.wapps1.redcarga.core.ui.theme.RcColor5
import com.wapps1.redcarga.core.ui.theme.RcColor6
import com.wapps1.redcarga.core.ui.theme.RedcargaTheme
import com.wapps1.redcarga.core.ui.theme.White

@Composable
private fun WelcomeBackground(
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

@Composable
fun Welcome(
    onCreateAccount: () -> Unit,
    onLogin: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        WelcomeBackground(Modifier.matchParentSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.ic_worker_celebrate),
                contentDescription = null,
                modifier = Modifier.size(260.dp)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.welcome_greeting),
                    style = MaterialTheme.typography.bodyMedium,
                    color = RcColor6,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.welcome_app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = RcColor5,
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = onCreateAccount,
                modifier = Modifier
                    .width(250.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = RcColor6
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = stringResource(R.string.welcome_btn_create_account),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            OutlinedButton(
                onClick = onLogin,
                modifier = Modifier
                    .width(250.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(2.dp, Color.White),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White.copy(alpha = 0.15f),
                    contentColor = Color.White.copy(alpha = 0.90f)
                )
            ) {
                Text(
                    text = stringResource(R.string.welcome_btn_login),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun WelcomePreview() {
    RedcargaTheme(darkTheme = false) {
        Welcome(onCreateAccount = {}, onLogin = {})
    }
}
