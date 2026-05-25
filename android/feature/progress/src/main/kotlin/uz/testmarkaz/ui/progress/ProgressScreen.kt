package uz.testmarkaz.ui.progress

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import uz.testmarkaz.data.db.entity.TopicMasteryEntity
import uz.testmarkaz.ui.theme.Error
import uz.testmarkaz.ui.theme.Secondary
import uz.testmarkaz.ui.theme.Warning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    onBack: () -> Unit,
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mening natijalarim") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Orqaga")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {

            // ── Stats overview ─────────────────────────────────────────
            item {
                state.stats?.let { stats ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Umumiy ko'rsatkichlar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                                StatItem("🏆", "${stats.totalXp}", "XP")
                                StatItem("🔥", "${stats.currentStreak}", "Kun zanjiri")
                                StatItem("📝", "${stats.totalTests}", "Testlar")
                                StatItem("✅", "${if (stats.totalTests > 0) (stats.totalCorrect * 100) / (stats.totalTests * 25) else 0}%", "O'rtacha")
                            }
                        }
                    }
                }
            }

            // ── Weak topics ────────────────────────────────────────────
            if (state.weakTopics.isNotEmpty()) {
                item {
                    Text(
                        "Zaif mavzular",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(state.weakTopics) { mastery ->
                    WeakTopicCard(mastery = mastery)
                }
            }

            // ── All mastery ────────────────────────────────────────────
            if (state.allMastery.isNotEmpty()) {
                item {
                    Text(
                        "Barcha mavzular",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(state.allMastery) { mastery ->
                    TopicMasteryCard(mastery = mastery)
                }
            } else if (!state.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📊", style = MaterialTheme.typography.headlineLarge)
                            Spacer(Modifier.height(8.dp))
                            Text("Hali ma'lumot yo'q", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Text("Test topshirgach natijalar bu yerda ko'rinadi", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, style = MaterialTheme.typography.titleLarge)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
    }
}

@Composable
private fun WeakTopicCard(mastery: TopicMasteryEntity) {
    val accuracy = if (mastery.totalAnswered > 0) (mastery.correctCount * 100) / mastery.totalAnswered else 0
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(mastery.topic, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text("${mastery.subjectCode} • ${mastery.grade}-sinf", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
            Text(
                "$accuracy%",
                style = MaterialTheme.typography.titleMedium,
                color = Error,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TopicMasteryCard(mastery: TopicMasteryEntity) {
    val accuracy = if (mastery.totalAnswered > 0) (mastery.correctCount * 100) / mastery.totalAnswered else 0
    val color = when {
        accuracy >= 80 -> Secondary
        accuracy >= 60 -> Warning
        else -> Error
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(mastery.topic, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text("${mastery.subjectCode} • ${mastery.grade}-sinf • ${mastery.totalAnswered} javob", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                LinearProgressIndicator(
                    progress = { accuracy / 100f },
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp).height(4.dp),
                    color = color
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "$accuracy%",
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
