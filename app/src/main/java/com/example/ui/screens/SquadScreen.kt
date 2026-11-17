package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.Club
import com.example.model.Formation
import com.example.model.Player
import com.example.model.Position
import com.example.model.formatBudgetTenths
import com.example.ui.components.BudgetBarCard
import com.example.ui.components.ClubCrestBadge
import com.example.ui.components.TacticalPitchView
import com.example.ui.components.parseHexColorOrFallback
import com.example.viewmodel.DraftState
import com.example.viewmodel.UiState

private val crestPalette = listOf(
    "#10B981", // Pitch Emerald
    "#F59E0B", // Trophy Gold
    "#38BDF8", // Sky Blue
    "#EF4444", // Crimson Red
    "#A855F7", // Royal Violet
    "#EC4899"  // Electric Rose
)

@Composable
fun SquadScreen(
    myClubState: UiState<Club?>,
    draftState: DraftState,
    onUpdateClubDetails: (String, String, String) -> Unit,
    onSelectFormation: (Formation) -> Unit,
    onAutoDraft: () -> Unit,
    onClearSquad: () -> Unit,
    onReleasePlayer: (String) -> Unit,
    onSaveAndPublish: () -> Unit,
    onOpenMarketForPosition: (Position) -> Unit,
    onDismissFeedback: () -> Unit
) {
    var showEditClubDialog by remember { mutableStateOf(false) }
    var inspectedPlayer by remember { mutableStateOf<Player?>(null) }

    val serverClub = (myClubState as? UiState.Success)?.data

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (myClubState is UiState.Error) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = myClubState.message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }

            item {
                AnimatedVisibility(visible = draftState.feedbackMessage != null) {
                    Surface(
                        color = if (draftState.isErrorFeedback) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = draftState.feedbackMessage.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (draftState.isErrorFeedback) MaterialTheme.colorScheme.onErrorContainer
                                else MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = onDismissFeedback,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Dismiss notice",
                                    tint = if (draftState.isErrorFeedback) MaterialTheme.colorScheme.onErrorContainer
                                    else MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Club Identity Card
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ClubCrestBadge(
                            clubName = draftState.clubName,
                            crestColorHex = draftState.crestColorHex,
                            overallRating = draftState.ratings.overall,
                            size = 54.dp
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = draftState.clubName,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (serverClub?.ready == true && !draftState.hasUnsavedChanges) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "Published online",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Mgr: ${draftState.managerAlias} • ${
                                    if (serverClub != null) "${serverClub.wins}W-${serverClub.draws}D-${serverClub.losses}L"
                                    else "New Expansion Club"
                                }",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { showEditClubDialog = true },
                            modifier = Modifier.testTag("edit_club_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit club name and crest",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Budget & Team Ratings Card
            item {
                BudgetBarCard(
                    budgetSpent = draftState.budgetSpent,
                    budgetRemaining = draftState.budgetRemaining,
                    playerCount = draftState.players.size,
                    ratings = draftState.ratings
                )
            }

            // Formation Selector
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TACTICAL FORMATION",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = draftState.formation.styleBonus,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Formation.entries.forEach { formation ->
                            val selected = draftState.formation == formation
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectFormation(formation) },
                                label = {
                                    Text(
                                        text = formation.code,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("formation_chip_${formation.code}")
                            )
                        }
                    }
                }
            }

            // Interactive 2D Pitch View
            item {
                TacticalPitchView(
                    formation = draftState.formation,
                    squad = draftState.players,
                    crestColorHex = draftState.crestColorHex,
                    onEmptySlotClick = onOpenMarketForPosition,
                    onFilledSlotClick = { player -> inspectedPlayer = player }
                )
            }

            // Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onAutoDraft,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("auto_draft_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.action_auto_draft))
                    }

                    IconButton(
                        onClick = onClearSquad,
                        enabled = draftState.playerIds.isNotEmpty(),
                        modifier = Modifier
                            .size(50.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                            .testTag("clear_squad_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteSweep,
                            contentDescription = "Clear squad",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onSaveAndPublish,
                    enabled = !draftState.isSaving,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("save_publish_button")
                ) {
                    if (draftState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Saving Club...")
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (draftState.isCompleteSquad) stringResource(R.string.action_save_publish)
                            else "Save Draft (${draftState.players.size}/11)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Starting XI Roster List
            item {
                Text(
                    text = "STARTING XI ROSTER (${draftState.players.size}/11)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (draftState.players.isEmpty()) {
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
                            Text(
                                text = "Your Pitch is Empty",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap any '+' slot on the pitch, scout stars in the Transfer Market, or press Auto-Draft XI to build a team under $100.0M.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(draftState.players, key = { it.id }) { player ->
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.width(44.dp)
                            ) {
                                Text(
                                    text = player.position.code,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = player.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${player.trait} • OVR ${player.overall} • ${formatBudgetTenths(player.priceTenths)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(
                                onClick = { onReleasePlayer(player.id) },
                                modifier = Modifier.testTag("release_squad_${player.id}")
                            ) {
                                Text("Sell", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditClubDialog) {
        EditClubDialog(
            initialClubName = draftState.clubName,
            initialManagerAlias = draftState.managerAlias,
            initialCrestHex = draftState.crestColorHex,
            onDismiss = { showEditClubDialog = false },
            onConfirm = { name, alias, hex ->
                onUpdateClubDetails(name, alias, hex)
                showEditClubDialog = false
            }
        )
    }

    inspectedPlayer?.let { player ->
        AlertDialog(
            onDismissRequest = { inspectedPlayer = null },
            title = { Text("${player.name} (${player.position.code})") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Nationality: ${player.nationality} • Jersey #${player.jerseyNumber}")
                    Text("Trait: ${player.trait}")
                    Text("Transfer Value: ${formatBudgetTenths(player.priceTenths)}")
                    Text("OVR ${player.overall} | PAC ${player.pace} | SHO ${player.shooting} | PAS ${player.passing} | DEF ${player.defending}")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onReleasePlayer(player.id)
                        inspectedPlayer = null
                    }
                ) {
                    Text("Sell Player (+${formatBudgetTenths(player.priceTenths)})", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { inspectedPlayer = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun EditClubDialog(
    initialClubName: String,
    initialManagerAlias: String,
    initialCrestHex: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var clubName by remember { mutableStateOf(initialClubName) }
    var managerAlias by remember { mutableStateOf(initialManagerAlias) }
    var selectedHex by remember { mutableStateOf(initialCrestHex) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Club Identity") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = clubName,
                    onValueChange = { clubName = it.take(32) },
                    label = { Text("Club Name (2-32 chars)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("club_name_input")
                )
                OutlinedTextField(
                    value = managerAlias,
                    onValueChange = { managerAlias = it.take(30) },
                    label = { Text("Manager Alias (2-30 chars)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manager_alias_input")
                )
                Text("Crest Color", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    crestPalette.forEach { hex ->
                        val color = parseHexColorOrFallback(hex)
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (selectedHex == hex) 3.dp else 1.dp,
                                    color = if (selectedHex == hex) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedHex = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(clubName.trim(), managerAlias.trim(), selectedHex) },
                enabled = clubName.trim().length >= 2 && managerAlias.trim().length >= 2,
                modifier = Modifier.testTag("confirm_club_edit_button")
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
