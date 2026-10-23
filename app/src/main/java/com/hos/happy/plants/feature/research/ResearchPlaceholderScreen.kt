package com.hos.happy.plants.feature.research

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hos.happy.plants.ui.theme.Bark
import com.hos.happy.plants.ui.theme.Forest
import com.hos.happy.plants.ui.theme.Leaf
import com.hos.happy.plants.ui.theme.Mute
import com.hos.happy.plants.ui.theme.PaperDark
import com.hos.happy.plants.ui.theme.Sand
import com.hos.happy.plants.ui.theme.SurfaceWarm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResearchPlaceholderScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Research", style = MaterialTheme.typography.headlineSmall, color = Forest) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PaperDark)
            )
        },
        containerColor = PaperDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Forest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = Leaf,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Plant Research & Library",
                style = MaterialTheme.typography.headlineMedium,
                color = Forest
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "An offline botanical encyclopedia of 50+ indoor and balcony plants suited for Indian homes.",
                style = MaterialTheme.typography.bodyMedium,
                color = Mute
            )

            Spacer(modifier = Modifier.height(30.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Sand, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Upcoming Features in Research Tab",
                        style = MaterialTheme.typography.titleMedium,
                        color = Forest,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "• Search by common name (e.g. Money Plant) or botanical name", style = MaterialTheme.typography.bodyMedium, color = Bark)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "• Filter by light needs (matched directly with Light Meter)", style = MaterialTheme.typography.bodyMedium, color = Bark)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "• Verified pet safety and toxicity indicators", style = MaterialTheme.typography.bodyMedium, color = Bark)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "• Care difficulty, potting mix guides, and propagation tips", style = MaterialTheme.typography.bodyMedium, color = Bark)
                }
            }
        }
    }
}
