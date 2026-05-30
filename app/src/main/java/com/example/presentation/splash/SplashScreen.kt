package com.example.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DataStoreManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull

@Composable
fun SplashScreen(
    dataStoreManager: DataStoreManager,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    // Animations
    val scaleAnim = remember { Animatable(0.3f) }
    val textAlphaAnim = remember { Animatable(0f) }
    val taglineAlphaAnim = remember { Animatable(0f) }
    val taglineOffsetAnim = remember { Animatable(30f) }

    LaunchedEffect(Unit) {
        // Step 1: Scale logo with spring bounce
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        
        // Step 2: Fade in app name text
        textAlphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(600, easing = LinearOutSlowInEasing)
        )
        
        // Step 3: Slide up tagline
        taglineAlphaAnim.animateTo(1f, tween(500))
        taglineOffsetAnim.animateTo(0f, spring(Spring.DampingRatioLowBouncy))
        
        // Hold for splash time
        delay(1000)

        // Read routing preferences from DataStore session
        val onboardingCompleted = try {
            dataStoreManager.isOnboardingCompletedFlow.firstOrNull() ?: false
        } catch (e: Exception) {
            false
        }
        val session = try {
            dataStoreManager.sessionFlow.firstOrNull()
        } catch (e: Exception) {
            null
        }

        if (!onboardingCompleted) {
            onNavigateToOnboarding()
        } else if (session != null && session.accessToken.isNotEmpty()) {
            onNavigateToDashboard()
        } else {
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A), // Dark Navy
                        Color(0xFF0D9488)  // Premium Teal DERISET color
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Stylized Gradient Circle Milk Drop Logo
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(scaleAnim.value)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF14B8A6), Color(0xFF6366F1))
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Outer glow drop symbol representation
                Text(
                    text = "D",
                    color = Color.White,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App name
            Text(
                text = "DERISET",
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                modifier = Modifier.alpha(textAlphaAnim.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline sliding up
            Text(
                text = "Your Dairy Partner",
                color = Color(0xFFF1F5F9).copy(alpha = 0.8f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .offset(y = taglineOffsetAnim.value.dp)
                    .alpha(taglineAlphaAnim.value)
            )
        }
    }
}
