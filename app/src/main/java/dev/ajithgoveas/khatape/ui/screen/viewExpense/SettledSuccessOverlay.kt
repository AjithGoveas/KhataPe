package dev.ajithgoveas.khatape.ui.screen.viewExpense

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

@Composable
fun SettledSuccessOverlay(
    isVisible: Boolean,
    onFinished: () -> Unit
) {
    val alpha = remember { Animatable(0f) }
    val handSlide = remember { Animatable(300f) }
    var isShaking by remember { mutableStateOf(false) }

    // High density petals
    val petalCount = 120
    val petals = remember(isVisible) { List(petalCount) { PetalParticle.create() } }
    val petalAnim = remember { Animatable(0f) }

    // Coin Sparkle/Glow state
    val coinScale = remember { Animatable(0f) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            launch {
                alpha.animateTo(1f, tween(400))
                delay(3200)
                alpha.animateTo(0f, tween(800))
                onFinished()
            }

            // Hands slide with a "thud" spring
            handSlide.animateTo(-20f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))

            isShaking = true

            launch {
                coinScale.animateTo(1.2f, spring(Spring.DampingRatioHighBouncy))
                coinScale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy))
            }

            launch {
                petalAnim.animateTo(1f, tween(4000, easing = LinearEasing))
            }
        } else {
            isShaking = false
            handSlide.snapTo(300f)
            petalAnim.snapTo(0f)
            coinScale.snapTo(0f)
        }
    }

    if (isVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.99f))
                .graphicsLayer(alpha = alpha.value),
            contentAlignment = Alignment.Center
        ) {
            // Optimized Canvas: Only draws what is necessary
            Canvas(modifier = Modifier.fillMaxSize()) {
                val progress = petalAnim.value
                petals.forEach { p ->
                    p.update(progress)

                    // Only draw if on screen
                    if (p.y in -0.1f..1.1f) {
                        val rotation = p.drift * 360 * progress * p.rotationSpeed
                        val pAlpha = (1f - (progress * 1.1f)).coerceIn(0f, 1f)

                        rotate(rotation, Offset(size.width * p.x, size.height * p.y)) {
                            drawOval(
                                color = p.color,
                                topLeft = Offset(size.width * p.x, size.height * p.y),
                                size = Size(p.size, p.size * 0.6f),
                                alpha = pAlpha
                            )
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .shake(isShaking)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // LEFT HAND
                    Text(
                        text = "🫱",
                        fontSize = 110.sp,
                        modifier = Modifier
                            .zIndex(2f)
                            .graphicsLayer {
                                translationX = -handSlide.value - 15
                                rotationZ = 15f
                            }
                    )

                    // RIGHT HAND
                    Text(
                        text = "🫲",
                        fontSize = 110.sp,
                        modifier = Modifier
                            .zIndex(1f)
                            .graphicsLayer {
                                translationX = handSlide.value + 15
                                rotationZ = -15f
                            }
                    )

                    // Animated Rupee Coin
                    if (isShaking) {
                        Surface(
                            modifier = Modifier
                                .zIndex(3f)
                                .graphicsLayer {
                                    translationY = -75f
                                    scaleX = coinScale.value
                                    scaleY = coinScale.value
                                },
                            color = Color(0xFFFFD700),
                            shape = RoundedCornerShape(50),
                            shadowElevation = 8.dp
                        ) {
                            Text(
                                text = "₹",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                                color = Color(0xFF5D4037)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "Hisab Pakka!",
                    style = MaterialTheme.typography.displaySmall.copy(letterSpacing = 1.sp),
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Dosti bani rahe.",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 4.dp
                ) {
                    Text(
                        text = "🤝 NO BAAKI • NO TENSION",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

// Custom Modifier for a shake effect (as provided by you)
fun Modifier.shake(enabled: Boolean) = composed(
    factory = {
        val offsetX by animateFloatAsState(
            targetValue = if (enabled) -5f else 0f,
            animationSpec = if (enabled) repeatable(
                iterations = 6,
                animation = tween(durationMillis = 60, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ) else tween(0),
            label = "shake"
        )

        Modifier.graphicsLayer {
            translationX = if (enabled) offsetX.dp.toPx() else 0f
        }
    }
)

private data class PetalParticle(
    val xInitial: Float,
    val yInitial: Float,
    val color: Color,
    val size: Float,
    val speedX: Float,
    val speedY: Float,
    val drift: Float,
    val rotationSpeed: Float
) {
    var x = 0f
    var y = 0f

    fun update(progress: Float) {
        val wobble = sin(progress * 8f + drift) * 0.08f
        x = xInitial + (speedX * progress) + wobble
        y = yInitial + (speedY * progress) + (1.1f * progress * progress)
    }

    companion object {
        fun create() = PetalParticle(
            xInitial = (0.05f..0.95f).random(),
            yInitial = (-0.2f..0.0f).random(),
            color = listOf(
                // --- Traditional Festive Greens ---
                Color(0xFF47E850), Color(0xFF087310), Color(0xFF1B5E20),

                // --- Deep Blues & Teals ---
                Color(0xFF0974C0), Color(0xFF00ACC1), Color(0xFF006064),

                // --- Marigold & Saffron (The Classics) ---
                Color(0xFFFF9800), Color(0xFFFFC107), Color(0xFFFF5722), Color(0xFFE65100),

                // --- Gulal Pinks & Purples (Desi Vibe) ---
                Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFFF06292), Color(0xFF673AB7),

                // --- Success Gold & Royal Red ---
                Color(0xFFFFD700), Color(0xFFD32F2F), Color(0xFFC2185B)
            ).random(),
            size = (10..35).random().toFloat(),
            speedX = (-20..20).random().times(0.01f),
            speedY = (30..60).random().times(0.01f),
            drift = (0..20).random().toFloat(),
            rotationSpeed = (1..5).random().toFloat()
        )
    }
}

// Helper for random floats
private fun ClosedFloatingPointRange<Float>.random() =
    (Math.random().toFloat() * (endInclusive - start)) + start