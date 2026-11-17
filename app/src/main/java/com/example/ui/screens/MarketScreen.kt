package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.PlayerCatalog
import com.example.model.Player
import com.example.model.Position
import com.example.model.formatBudgetTenths
import com.example.ui.theme.TrophyGold
import com.example.viewmodel.DraftState

@Composable
fun MarketScreen(
    draftState: DraftState,
    initialPositionFilter: Position?,
    onClearInitialFilter: () -> Unit,
    onSignPlayer: (Player) -> Unit,
    onReleasePlayer: (String) -> Unit
) {
    var selectedPosition by remember(initialPositionFilter) { mutableStateOf(initialPositionFilter) }
    var searchQuery by remember { mutableStateOf("") }
    var affordableOnly by remember { mutableStateOf(false) }

    val filteredPlayers = remember(selectedPosition, searchQuery, affordableOnly, draftState.budgetRemaining, draftState.playerIds) {
        PlayerCatalog.allPlayers
            .filter { player ->
                (selectedPosition == null || player.position == selectedPosition) &&
                    (searchQuery.isBlank() ||
                        player.name.contains(searchQuery, ignoreCase = true) ||
                        player.trait.contains(searchQuery, ignoreCase = true) ||
                        player.nationality.contains(searchQuery, ignoreCase = true)) &&
                    (!affordableOnly || player.priceTenths <= draftState.budgetRemaining || draftState.playerIds.contains(player.id))
            }
            .sortedByDescending { it.overall }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize()
        ) {
            // Transfer Market Header
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "REMAINING BUDGET",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatBudgetTenths(draftState.budgetRemaining),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Squad ${draftState.players.size}/11 • ${draftState.formation.code}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search name, trait, or nation...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("market_search_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedPosition == null,
                            onClick = {
                                selectedPosition = null
                                onClearInitialFilter()
                            },
                            label = { Text("ALL") },
                            modifier = Modifier.testTag("filter_pos_all")
                        )
                        Position.entries.forEach { pos ->
                            val countInPos = draftState.players.count { it.position == pos }
                            val limit = draftState.formation.slotLimitFor(pos)
                            FilterChip(
                                selected = selectedPosition == pos,
                                onClick = {
                                    selectedPosition = if (selectedPosition == pos) null else pos
                                    onClearInitialFilter()
                                },
                                label = { Text("${pos.code} $countInPos/$limit") },
                                modifier = Modifier.testTag("filter_pos_${pos.code}")
                            )
                        }
                        FilterChip(
                            selected = affordableOnly,
                            onClick = { affordableOnly = !affordableOnly },
                            label = { Text("Affordable") },
                            modifier = Modifier.testTag("filter_affordable")
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredPlayers, key = { it.id }) { player ->
                    val isSigned = draftState.playerIds.contains(player.id)
                    val blockReason = if (isSigned) null
                    else PlayerCatalog.canSignReason(player, draftState.players, draftState.formation)

                    MarketPlayerCard(
                        player = player,
                        isSigned = isSigned,
                        blockReason = blockReason,
                        onSign = { onSignPlayer(player) },
                        onRelease = { onReleasePlayer(player.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MarketPlayerCard(
    player: Player,
    isSigned: Boolean,
    blockReason: String?,
    onSign: () -> Unit,
    onRelease: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (isSigned) 1.5.dp else 1.dp,
            color = if (isSigned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = TrophyGold.copy(alpha = 0.18f),
                shape = CircleShape,
                border = BorderStroke(1.5.dp, TrophyGold),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = player.overall.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = TrophyGold,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = player.position.code,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = player.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${player.nationality} • ${player.trait}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "PAC ${player.pace}  SHO ${player.shooting}  PAS ${player.passing}  DEF ${player.defending}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatBudgetTenths(player.priceTenths),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (isSigned) {
                    OutlinedButton(
                        onClick = onRelease,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("sign_player_${player.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Signed")
                    }
                } else {
                    Button(
                        onClick = onSign,
                        enabled = blockReason == null,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("sign_player_${player.id}")
                    ) {
                        if (blockReason == null) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sign")
                        } else {
                            Text(
                                text = if (blockReason == "Over budget") "Too $$"
                                else if (blockReason.contains("slots full")) "Full"
                                else "Max 11",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}
