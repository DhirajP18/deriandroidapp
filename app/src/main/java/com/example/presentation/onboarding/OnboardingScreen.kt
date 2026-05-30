package com.example.presentation.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DataStoreManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    dataStoreManager: DataStoreManager,
    onNavigateToLogin: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    val pages = listOf(
        OnboardingPageData(
            titleEn = "Smart Milk Collection",
            titleMr = "हुशार दूध संकलन",
            subtitleEn = "Record morning and evening milk entries for every farmer effortlessly and instantly.",
            subtitleMr = "प्रत्येक शेतकऱ्यासाठी सकाळी आणि संध्याकाळी दूध नोंदी सहज आणि झटपट नोंदवा.",
            colorSpec = listOf(Color(0xFF0F172A), Color(0xFF0D9488))
        ),
        OnboardingPageData(
            titleEn = "Instant Weekly Bills",
            titleMr = "साप्ताहिक पेमेंट बिले तयार करा",
            subtitleEn = "Generate, preview, and send printed bill invoices to farmers in a single click.",
            subtitleMr = "एका क्लिकवर शेतकऱ्यांना बिले तयार करा, तपासा आणि थेट पाठवा.",
            colorSpec = listOf(Color(0xFF0F172A), Color(0xFF4F46E5))
        ),
        OnboardingPageData(
            titleEn = "Manage Your Dairy",
            titleMr = "डॅशबोर्ड आणि नियंत्रण",
            subtitleEn = "Track farmers, payments, milk statistics, shift records, and full dues ledger in one place.",
            subtitleMr = "शेतकरी माहिती, पशुखाद्य हिशोब, थकीत येणे आणि एकत्रित अहवाल सर्व काही एकाच डॅशबोर्डवरून पहा.",
            colorSpec = listOf(Color(0xFF0F172A), Color(0xFF1E1B4B))
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val page = pages[pageIndex]
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(page.colorSpec))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Large styled shape simulating an illustration
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.White.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFF2DD4BF), Color.Transparent)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (pageIndex) {
                                0 -> "🥛"
                                1 -> "💸"
                                else -> "📋"
                            },
                            fontSize = 62.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Bilingual Text Headings
                Text(
                    text = page.titleEn,
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = page.titleMr,
                    color = Color(0xFF2DD4BF),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = page.subtitleEn,
                    color = Color(0xFFCBD5E1),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = page.subtitleMr,
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Dot Indicators
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
                val active = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(width = if (active) 18.dp else 8.dp, height = 8.dp)
                        .clip(CircleShape)
                        .background(if (active) Color(0xFF2DD4BF) else Color.White.copy(alpha = 0.3f))
                )
            }
        }

        // Skip Button (pages 1-2)
        if (pagerState.currentPage < 2) {
            TextButton(
                onClick = {
                    coroutineScope.launch {
                        dataStoreManager.saveOnboardingCompleted(true)
                        onNavigateToLogin()
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(horizontal = 16.dp, vertical = 40.dp)
            ) {
                Text("Skip / वगळा", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
            }
        }

        // Next / Get Started button at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 40.dp)
        ) {
            if (pagerState.currentPage == 2) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            dataStoreManager.saveOnboardingCompleted(true)
                            onNavigateToLogin()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Get Started / सुरुवात करा", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            } else {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

data class OnboardingPageData(
    val titleEn: String,
    val titleMr: String,
    val subtitleEn: String,
    val subtitleMr: String,
    val colorSpec: List<Color>
)
