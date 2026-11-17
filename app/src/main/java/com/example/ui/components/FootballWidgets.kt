package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerCatalog
import com.example.model.CLUB_BUDGET_CAP_TENTHS
import com.example.model.Formation
import com.example.model.Player
import com.example.model.Position
import com.example.model.formatBudgetTenths
import com.example.ui.theme.CrimsonCard
import com.example.ui.theme.PitchEmerald
import com.example.ui.theme.SkyAssist
import com.example.ui.theme.TrophyGold
import com.example.ui.theme.TurfStripeDark
import com.example.ui.theme.TurfStripeLight

fun parseHexColorOrFallback(hex: String, fallback: Color = PitchEmerald): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        fallback
    }
}

@Composable
fun ClubCrestBadge(
    clubName: String,
    crestColorHex: String,
    overallRating: Long? = null,
    size: Dp = 46.dp
) {
    val crestColor = parseHexColorOrFallback(crestColorHex)
    val initials = clubName
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifBlank { "FC" }

    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        Surface(
            color = crestColor.copy(alpha = 0.22f),
            shape = CircleShape,
            border = BorderStroke(2.dp, crestColor),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = null,
                    tint = crestColor.copy(alpha = 0.28f),
                    modifier = Modifier.size(size * 0.72f)
                )
                Text(
                    text = initials,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        if (overallRating != null && overallRating > 0) {
            Surface(
                color = MaterialTheme.colorScheme.secondary,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(20.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = overallRating.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun BudgetBarCard(
    budgetSpent: Long,
    budgetRemaining: Long,
    playerCount: Int,
    ratings: PlayerCatalog.SquadRatings,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = (budgetSpent.toFloat() / CLUB_BUDGET_CAP_TENTHS.toFloat()).coerceIn(0f, 1f),
        animationSpec = spring(),
        label = "budgetProgress"
    )
    val budgetColor = when {
        budgetRemaining < 50L -> TrophyGold
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TRANSFER BUDGET",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = formatBudgetTenths(budgetRemaining),
                            style = MaterialTheme.typography.headlineMedium,
                            color = budgetColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "left of ${formatBudgetTenths(CLUB_BUDGET_CAP_TENTHS)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                }
                Surface(
                    color = if (playerCount == 11) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "$playerCount / 11 XI",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (playerCount == 11) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                color = budgetColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatChip("OVR", ratings.overall.toString(), TrophyGold, Modifier.weight(1f))
                StatChip("ATK", ratings.attack.toString(), CrimsonCard, Modifier.weight(1f))
                StatChip("MID", ratings.midfield.toString(), PitchEmerald, Modifier.weight(1f))
                StatChip("DEF", ratings.defense.toString(), SkyAssist, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (value == "0") "--" else value,
                style = MaterialTheme.typography.titleMedium,
                color = accent,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TacticalPitchView(
    formation: Formation,
    squad: List<Player>,
    crestColorHex: String,
    onEmptySlotClick: (Position) -> Unit,
    onFilledSlotClick: (Player) -> Unit,
    modifier: Modifier = Modifier
) {
    val crestColor = parseHexColorOrFallback(crestColorHex)
    val fwds = squad.filter { it.position == Position.FWD }
    val mids = squad.filter { it.position == Position.MID }
    val defs = squad.filter { it.position == Position.DEF }
    val gks = squad.filter { it.position == Position.GK }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(1.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
    ) {
        // Mowed pitch stripes & chalk lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stripeCount = 8
            val stripeHeight = size.height / stripeCount
            for (i in 0 until stripeCount) {
                drawRect(
                    color = if (i % 2 == 0) TurfStripeLight else TurfStripeDark,
                    topLeft = Offset(0f, i * stripeHeight),
                    size = Size(size.width, stripeHeight + 1f)
                )
            }

            val lineColor = Color.White.copy(alpha = 0.32f)
            val stroke = Stroke(width = 3f)
            // Outer touchlines
            drawRect(
                color = lineColor,
                topLeft = Offset(14f, 14f),
                size = Size(size.width - 28f, size.height - 28f),
                style = stroke
            )
            // Halfway line
            drawLine(
                color = lineColor,
                start = Offset(14f, size.height / 2f),
                end = Offset(size.width - 14f, size.height / 2f),
                strokeWidth = 3f
            )
            // Center circle
            drawCircle(
                color = lineColor,
                radius = size.minDimension * 0.14f,
                center = Offset(size.width / 2f, size.height / 2f),
                style = stroke
            )
            // Top penalty box
            drawRect(
                color = lineColor,
                topLeft = Offset(size.width * 0.26f, 14f),
                size = Size(size.width * 0.48f, size.height * 0.14f),
                style = stroke
            )
            // Bottom penalty box (home GK)
            drawRect(
                color = lineColor,
                topLeft = Offset(size.width * 0.26f, size.height - 14f - size.height * 0.14f),
                size = Size(size.width * 0.48f, size.height * 0.14f),
                style = stroke
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            PitchLineRow(
                position = Position.FWD,
                limit = formation.fwdCount,
                players = fwds,
                crestColor = crestColor,
                onEmptySlotClick = onEmptySlotClick,
                onFilledSlotClick = onFilledSlotClick
            )
            PitchLineRow(
                position = Position.MID,
                limit = formation.midCount,
                players = mids,
                crestColor = crestColor,
                onEmptySlotClick = onEmptySlotClick,
                onFilledSlotClick = onFilledSlotClick
            )
            PitchLineRow(
                position = Position.DEF,
                limit = formation.defCount,
                players = defs,
                crestColor = crestColor,
                onEmptySlotClick = onEmptySlotClick,
                onFilledSlotClick = onFilledSlotClick
            )
            PitchLineRow(
                position = Position.GK,
                limit = formation.gkCount,
                players = gks,
                crestColor = TrophyGold,
                onEmptySlotClick = onEmptySlotClick,
                onFilledSlotClick = onFilledSlotClick
            )
        }
    }
}

@Composable
private fun PitchLineRow(
    position: Position,
    limit: Int,
    players: List<Player>,
    crestColor: Color,
    onEmptySlotClick: (Position) -> Unit,
    onFilledSlotClick: (Player) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until limit) {
            val player = players.getOrNull(i)
            if (player != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onFilledSlotClick(player) }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(crestColor)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = player.overall.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        color = Color.Black.copy(alpha = 0.72f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = player.shortName,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onEmptySlotClick(position) }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.35f))
                            .border(1.5.dp, Color.White.copy(alpha = 0.55f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Draft ${position.label}",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = position.code,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}
