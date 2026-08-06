package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

data class LinePath(
    val path: Path,
    val strokeWidth: Float = 6f
)

@Composable
fun SignaturePad(
    onSignatureCaptured: (Bitmap) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val paths = remember { mutableStateListOf<LinePath>() }
    var currentPath = remember { Path() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Signature manuscrite",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Dessinez ci-dessous",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("signature_canvas")
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentPath = Path().apply {
                                    moveTo(offset.x, offset.y)
                                }
                                paths.add(LinePath(currentPath))
                            },
                            onDrag = { change, _ ->
                                currentPath.lineTo(change.position.x, change.position.y)
                                // Trigger recomposition
                                paths.remove(LinePath(currentPath))
                                paths.add(LinePath(currentPath))
                            }
                        )
                    }
            ) {
                paths.forEach { linePath ->
                    drawPath(
                        path = linePath.path,
                        color = Color.Black,
                        style = Stroke(
                            width = linePath.strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            if (paths.isEmpty()) {
                Text(
                    text = "Signer avec le doigt ici...",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(
                onClick = {
                    paths.clear()
                    onClear()
                },
                modifier = Modifier.testTag("clear_signature_button")
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Effacer")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Effacer")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (paths.isNotEmpty()) {
                        val bitmap = Bitmap.createBitmap(500, 200, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        canvas.drawColor(android.graphics.Color.WHITE)
                        val paint = Paint().apply {
                            color = android.graphics.Color.BLACK
                            strokeWidth = 6f
                            style = Paint.Style.STROKE
                            strokeCap = Paint.Cap.ROUND
                            strokeJoin = Paint.Join.ROUND
                        }
                        onSignatureCaptured(bitmap)
                    }
                },
                enabled = paths.isNotEmpty(),
                modifier = Modifier.testTag("save_signature_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = "Valider")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Valider la signature")
            }
        }
    }
}
