package com.wapps1.redcarga.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wapps1.redcarga.core.ui.theme.RcColor2
import com.wapps1.redcarga.core.ui.theme.RcColor5
import com.wapps1.redcarga.core.ui.theme.RcColor6

/**
 * Indicador de pasos para formularios multi-step
 * Muestra círculos numerados conectados por líneas
 */
@Composable
fun RcStepIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (step in 1..totalSteps) {
            // Círculo del paso
            StepCircle(
                stepNumber = step,
                isCompleted = step < currentStep,
                isCurrent = step == currentStep
            )

            // Línea conectora (excepto después del último paso)
            if (step < totalSteps) {
                StepConnector(
                    isCompleted = step < currentStep
                )
            }
        }
    }
}

@Composable
private fun StepCircle(
    stepNumber: Int,
    isCompleted: Boolean,
    isCurrent: Boolean
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                when {
                    isCompleted -> RcColor5
                    isCurrent -> RcColor5
                    else -> Color.White.copy(alpha = 0.9f)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stepNumber.toString(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isCurrent || isCompleted) FontWeight.Bold else FontWeight.Normal,
            fontSize = 16.sp,
            color = when {
                isCompleted || isCurrent -> Color.White
                else -> RcColor6.copy(alpha = 0.5f)
            }
        )
    }
}

@Composable
private fun StepConnector(
    isCompleted: Boolean
) {
    Box(
        modifier = Modifier
            .width(32.dp)
            .height(3.dp)
            .background(
                if (isCompleted) RcColor5 else RcColor2.copy(alpha = 0.3f)
            )
    )
}
