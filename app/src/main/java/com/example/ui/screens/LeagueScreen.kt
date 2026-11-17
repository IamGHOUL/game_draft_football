package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.model.Club
import com.example.model.MatchRecord
import com.example.ui.components.ClubCrestBadge
import com.example.ui.theme.PitchEmerald
import com.example.ui.theme.TrophyGold
import com.example.viewmodel.UiState

@Composable
fun LeagueScreen(
    currentUserId: String,
    readyClubsState: UiState<List<Club>>,
    matchHistoryState: UiState<List<MatchRecord>>
) {
    var selectedSection by remember { mutableIntStateOf(0) } // 0 = Standings, 1 = History

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = selectedSection == 0,
                    onClick = { selectedSection = 0 },
                    leadingIcon = {
                        Icon(Icons.Filled.EmojiEvents, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    label = { Text("League Table") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("league_tab_standings")
                )
                FilterChip(
                    selected = selectedSection == 1,
                    onClick = { selectedSection = 1 },
                    leadingIcon = {
                        Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    label = { Text("Match History") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("league_tab_history")
                )
            }

            if (selectedSection == 0) {
                StandingsSection(currentUserId = currentUserId, readyClubsState = readyClubsState)
            } else {
                MatchHistorySection(currentUserId = currentUserId, matchHistoryState = matchHistoryState)
            }
        }
    }
}

@Composable
private fun StandingsSection(
    currentUserId: String,
    readyClubsState: UiState<List<Club>>
) {
    when (readyClubsState) {
        is UiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is UiState.Error -> {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
                Text(
                    text = readyClubsState.message,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        is UiState.Success -> {
            val clubs = readyClubsState.data
            if (clubs.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No clubs published yet. Build an 11-player squad and press Save & Publish XI to enter the league!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("#", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(26.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("CLUB", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("P", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("W-D-L", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(58.dp), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("GD", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(34.dp), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("PTS", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(36.dp), textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    itemsIndexed(clubs, key = { _, club -> club.ownerId }) { index, club ->
                        val isMe = club.ownerId == currentUserId
                        val rankColor = when (index) {
                            0 -> TrophyGold
                            1 -> MaterialTheme.colorScheme.tertiary
                            2 -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Surface(
                            color = if (isMe) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                            else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                width = if (isMe) 1.5.dp else 1.dp,
                                color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = rankColor.copy(alpha = 0.16f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = rankColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                ClubCrestBadge(
                                    clubName = club.clubName,
                                    crestColorHex = club.crestColorHex,
                                    size = 34.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isMe) "${club.clubName} (You)" else club.clubName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "OVR ${club.overallRating} • ${club.formation}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "${club.matchesPlayed}",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.width(28.dp),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "${club.wins}-${club.draws}-${club.losses}",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.width(58.dp),
                                    textAlign = TextAlign.Center
                                )
                                val gd = club.goalDifference
                                Text(
                                    text = if (gd > 0) "+$gd" else "$gd",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.width(34.dp),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "${club.points}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TrophyGold,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(36.dp),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchHistorySection(
    currentUserId: String,
    matchHistoryState: UiState<List<MatchRecord>>
) {
    when (matchHistoryState) {
        is UiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is UiState.Error -> {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
                Text(
                    text = matchHistoryState.message,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        is UiState.Success -> {
            val matches = matchHistoryState.data
            if (matches.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No multiplayer matches recorded yet. Head to the Arena to challenge a rival manager!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(matches, key = { it.id }) { match ->
                        val iWon = match.winnerOwnerId == currentUserId
                        val isDraw = match.winnerOwnerId == "DRAW"
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${match.homeClubName}  ${match.homeScore} - ${match.awayScore}  ${match.awayClubName}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Surface(
                                        color = when {
                                            iWon -> PitchEmerald.copy(alpha = 0.2f)
                                            isDraw -> TrophyGold.copy(alpha = 0.2f)
                                            else -> MaterialTheme.colorScheme.errorContainer
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = when {
                                                iWon -> "WIN"
                                                isDraw -> "DRAW"
                                                else -> "LOSS"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = when {
                                                iWon -> PitchEmerald
                                                isDraw -> TrophyGold
                                                else -> MaterialTheme.colorScheme.onErrorContainer
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                if (match.penaltiesUsed) {
                                    Text(
                                        text = "Penalties: ${match.homePenalties} - ${match.awayPenalties} (${match.winnerClubName})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TrophyGold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "MOTM: ${match.manOfTheMatch} • Poss ${match.homePossession}% / ${100 - match.homePossession}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (match.keyEvents.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    match.keyEvents.take(3).forEach { highlight ->
                                        Text(
                                            text = "• $highlight",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
