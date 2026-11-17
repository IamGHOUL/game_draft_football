package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.PlayerCatalog
import com.example.model.Club
import com.example.model.LiveMatchEvent
import com.example.model.MatchEventType
import com.example.ui.components.ClubCrestBadge
import com.example.ui.theme.CrimsonCard
import com.example.ui.theme.PitchEmerald
import com.example.ui.theme.TrophyGold
import com.example.viewmodel.DraftState
import com.example.viewmodel.SimulationUiState
import com.example.viewmodel.UiState

@Composable
fun ArenaScreen(
    currentUserId: String,
    draftState: DraftState,
    readyClubsState: UiState<List<Club>>,
    simState: SimulationUiState,
    onChallengeClub: (Club, Boolean) -> Unit,
    onSoloScrimmage: (Boolean) -> Unit,
    onSetSimSpeed: (Int) -> Unit,
    onSkipToFullTime: () -> Unit,
    onCloseSimulation: () -> Unit,
    onGoToSquad: () -> Unit
) {
    var cupTiebreaker by remember { mutableStateOf(true) }

    if (simState.active) {
        LiveMatchSimulatorView(
            simState = simState,
            onSetSpeed = onSetSimSpeed,
            onSkipToFullTime = onSkipToFullTime,
            onClose = onCloseSimulation
        )
        return
    }

    val squadReady = draftState.isCompleteSquad

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Floodlit Matchday Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(155.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_stadium_hero_1794908517805),
                        contentDescription = stringResource(R.string.hero_stadium_desc),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.82f),
                                        Color.Black.copy(alpha = 0.35f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "MATCHDAY ARENA",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White
                            )
                            Text(
                                text = "Challenge rival managers & simulate 90' matches.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Cup Tiebreaker Penalties",
                                style = MaterialTheme.typography.labelLarge,
                                color = TrophyGold
                            )
                            Switch(
                                checked = cupTiebreaker,
                                onCheckedChange = { cupTiebreaker = it },
                                modifier = Modifier.testTag("cup_tiebreaker_switch")
                            )
                        }
                    }
                }
            }

            if (!squadReady) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Complete Your Starting XI (${draftState.players.size}/11)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "Draft 11 players in Squad / Market before kicking off a match.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Button(onClick = onGoToSquad) {
                                Text("Build XI")
                            }
                        }
                    }
                }
            }

            // Solo Practice Scrimmage Card (explicitly labeled Practice Mode)
            item {
                val sparring = remember { PlayerCatalog.soloSparringClub() }
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ClubCrestBadge(
                            clubName = sparring.clubName,
                            crestColorHex = sparring.crestColorHex,
                            overallRating = sparring.overallRating,
                            size = 46.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = sparring.clubName,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Offline Practice • ${sparring.formation} • Does not affect League",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = { onSoloScrimmage(cupTiebreaker) },
                            enabled = squadReady,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("solo_scrimmage_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Practice")
                        }
                    }
                }
            }

            item {
                Text(
                    text = "ONLINE RIVAL MANAGERS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            when (readyClubsState) {
                is UiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                is UiState.Error -> {
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = readyClubsState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }

                is UiState.Success -> {
                    val rivals = readyClubsState.data.filter { it.ownerId != currentUserId }
                    if (rivals.isEmpty()) {
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.People,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "No Rival Managers Published Yet",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "When other players sign in with Google and publish an 11-player club, they appear here for head-to-head challenges. Try the Practice Match above anytime!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(rivals, key = { it.ownerId }) { rival ->
                            RivalClubCard(
                                rival = rival,
                                canChallenge = squadReady,
                                onChallenge = { onChallengeClub(rival, cupTiebreaker) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RivalClubCard(
    rival: Club,
    canChallenge: Boolean,
    onChallenge: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ClubCrestBadge(
                clubName = rival.clubName,
                crestColorHex = rival.crestColorHex,
                overallRating = rival.overallRating,
                size = 48.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rival.clubName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Mgr: ${rival.managerAlias} • ${rival.formation} • ${rival.wins}W-${rival.draws}D-${rival.losses}L",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "ATK ${rival.attackRating}  MID ${rival.midfieldRating}  DEF ${rival.defenseRating}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onChallenge,
                enabled = canChallenge,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("challenge_club_${rival.ownerId}")
            ) {
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.action_challenge))
            }
        }
    }
}

@Composable
private fun LiveMatchSimulatorView(
    simState: SimulationUiState,
    onSetSpeed: (Int) -> Unit,
    onSkipToFullTime: () -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }
    val summary = simState.summary ?: return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Controls
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (simState.isFinished) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (simState.isFinished) "FULL TIME" else "LIVE MATCHDAY • ${simState.currentMinute}'",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (simState.isFinished) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("close_simulation_button")
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Exit match view")
                    }
                }
            }

            // Broadcast Scoreboard
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                ClubCrestBadge(
                                    clubName = summary.homeClub.clubName,
                                    crestColorHex = summary.homeClub.crestColorHex,
                                    overallRating = summary.homeClub.overallRating,
                                    size = 52.dp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = summary.homeClub.clubName,
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = summary.homeClub.formation,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    text = "${simState.currentHomeScore}  :  ${simState.currentAwayScore}",
                                    style = MaterialTheme.typography.displayLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (simState.isFinished && summary.penaltiesUsed) {
                                    Text(
                                        text = "PENS ${summary.homePenalties} - ${summary.awayPenalties}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TrophyGold
                                    )
                                }
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                ClubCrestBadge(
                                    clubName = summary.awayClub.clubName,
                                    crestColorHex = summary.awayClub.crestColorHex,
                                    overallRating = summary.awayClub.overallRating,
                                    size = 52.dp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = summary.awayClub.clubName,
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = summary.awayClub.formation,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        LinearProgressIndicator(
                            progress = { (simState.currentMinute / 90f).coerceIn(0f, 1f) },
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )

                        if (simState.isRunning) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { onSetSpeed(1) },
                                    shape = RoundedCornerShape(10.dp)
                                ) { Text("1x") }
                                OutlinedButton(
                                    onClick = { onSetSpeed(4) },
                                    shape = RoundedCornerShape(10.dp)
                                ) { Text("4x") }
                                Button(
                                    onClick = onSkipToFullTime,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("skip_simulation_button")
                                ) {
                                    Icon(Icons.Filled.FastForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Instant Result")
                                }
                            }
                        }
                    }
                }
            }

            // Winner Banner when Full Time
            item {
                AnimatedVisibility(visible = simState.isFinished) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.5.dp, TrophyGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_trophy_crest_1794908515999),
                                contentDescription = stringResource(R.string.trophy_crest_desc),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(58.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, TrophyGold, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (summary.winnerOwnerId == "DRAW") "MATCH DRAWN"
                                    else "WINNER: ${summary.winnerClubName.uppercase()}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = TrophyGold
                                )
                                Text(
                                    text = "Man of the Match: ${summary.manOfTheMatch}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = when {
                                        summary.isSoloScrimmage -> "Practice Scrimmage complete"
                                        simState.isPersistingResult -> "Recording official result to League..."
                                        simState.persistenceError != null -> simState.persistenceError
                                        else -> "Official multiplayer result saved to League!"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            }

            // Match Stats Card
            if (simState.isFinished) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "MATCH STATISTICS",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            MatchStatBar("Possession", "${summary.homePossession}%", "${100 - summary.homePossession}%", summary.homePossession / 100f)
                            val totalShots = (summary.homeShots + summary.awayShots).coerceAtLeast(1)
                            MatchStatBar("Total Shots", "${summary.homeShots}", "${summary.awayShots}", summary.homeShots.toFloat() / totalShots)
                            val totalSot = (summary.homeShotsOnTarget + summary.awayShotsOnTarget).coerceAtLeast(1)
                            MatchStatBar("On Target", "${summary.homeShotsOnTarget}", "${summary.awayShotsOnTarget}", summary.homeShotsOnTarget.toFloat() / totalSot)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "LIVE COMMENTARY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(simState.visibleEvents) { event ->
                EventRow(event)
            }

            if (simState.isFinished) {
                item {
                    Button(
                        onClick = onClose,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("finish_simulation_button")
                    ) {
                        Text("Back to Arena", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchStatBar(label: String, homeVal: String, awayVal: String, homeRatio: Float) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(homeVal, style = MaterialTheme.typography.labelMedium, color = PitchEmerald)
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(awayVal, style = MaterialTheme.typography.labelMedium, color = TrophyGold)
        }
        LinearProgressIndicator(
            progress = { homeRatio.coerceIn(0.05f, 0.95f) },
            color = PitchEmerald,
            trackColor = TrophyGold,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )
    }
}

@Composable
private fun EventRow(event: LiveMatchEvent) {
    val accent = when (event.type) {
        MatchEventType.GOAL -> PitchEmerald
        MatchEventType.PENALTY_SHOOTOUT, MatchEventType.FULLTIME -> TrophyGold
        MatchEventType.CARD -> CrimsonCard
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = if (event.type == MatchEventType.GOAL) 1.5.dp else 1.dp,
            color = if (event.type == MatchEventType.GOAL) PitchEmerald else MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                color = accent.copy(alpha = 0.16f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.width(44.dp)
            ) {
                Text(
                    text = "${event.minute}'",
                    style = MaterialTheme.typography.labelMedium,
                    color = accent,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (event.type == MatchEventType.GOAL) {
                        Icon(
                            imageVector = Icons.Filled.SportsSoccer,
                            contentDescription = "Goal",
                            tint = PitchEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                    } else if (event.type == MatchEventType.FULLTIME) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = "Full time",
                            tint = TrophyGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                    } else if (event.type == MatchEventType.SAVE) {
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = "Save",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                    }
                    Text(
                        text = event.headline,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = event.detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
