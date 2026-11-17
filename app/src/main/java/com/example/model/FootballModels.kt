package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

enum class Position(val code: String, val label: String) {
    GK("GK", "Goalkeeper"),
    DEF("DEF", "Defender"),
    MID("MID", "Midfielder"),
    FWD("FWD", "Forward")
}

enum class Formation(
    val code: String,
    val gkCount: Int,
    val defCount: Int,
    val midCount: Int,
    val fwdCount: Int,
    val styleBonus: String
) {
    F433("4-3-3", 1, 4, 3, 3, "High-Press Wing Attack (+2 ATK)"),
    F442("4-4-2", 1, 4, 4, 2, "Balanced Twin Striker (+2 MID)"),
    F352("3-5-2", 1, 3, 5, 2, "Midfield Overload (+1 MID, +1 DEF)");

    fun slotLimitFor(position: Position): Int = when (position) {
        Position.GK -> gkCount
        Position.DEF -> defCount
        Position.MID -> midCount
        Position.FWD -> fwdCount
    }

    companion object {
        fun fromCode(code: String): Formation =
            entries.firstOrNull { it.code == code } ?: F433
    }
}

data class Player(
    val id: String,
    val name: String,
    val shortName: String,
    val jerseyNumber: Int,
    val nationality: String,
    val position: Position,
    val overall: Int,
    val pace: Int,
    val shooting: Int,
    val passing: Int,
    val defending: Int,
    val priceTenths: Long, // 100 = $10.0M
    val trait: String
)

fun formatBudgetTenths(tenths: Long): String {
    val whole = tenths / 10
    val frac = tenths % 10
    return "$${whole}.${frac}M"
}

const val CLUB_BUDGET_CAP_TENTHS: Long = 1000L // $100.0M

@Keep
@IgnoreExtraProperties
data class Club(
    @DocumentId val id: String = "",
    val ownerId: String = "",
    val clubName: String = "",
    val managerAlias: String = "",
    val formation: String = "4-3-3",
    val crestColorHex: String = "#10B981",
    val playerIds: List<String> = emptyList(),
    val budgetCap: Long = CLUB_BUDGET_CAP_TENTHS,
    val budgetSpent: Long = 0L,
    val budgetRemaining: Long = CLUB_BUDGET_CAP_TENTHS,
    val overallRating: Long = 0L,
    val attackRating: Long = 0L,
    val midfieldRating: Long = 0L,
    val defenseRating: Long = 0L,
    val ready: Boolean = false,
    val wins: Long = 0L,
    val draws: Long = 0L,
    val losses: Long = 0L,
    val points: Long = 0L,
    val goalsFor: Long = 0L,
    val goalsAgainst: Long = 0L,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    val goalDifference: Long get() = goalsFor - goalsAgainst
    val matchesPlayed: Long get() = wins + draws + losses
}

@Keep
@IgnoreExtraProperties
data class MatchRecord(
    @DocumentId val id: String = "",
    val homeOwnerId: String = "",
    val awayOwnerId: String = "",
    val participantIds: List<String> = emptyList(),
    val homeClubName: String = "",
    val awayClubName: String = "",
    val homeScore: Long = 0L,
    val awayScore: Long = 0L,
    val homePenalties: Long = 0L,
    val awayPenalties: Long = 0L,
    val penaltiesUsed: Boolean = false,
    val winnerOwnerId: String = "",
    val winnerClubName: String = "",
    val manOfTheMatch: String = "",
    val homePossession: Long = 50L,
    val homeShots: Long = 0L,
    val awayShots: Long = 0L,
    val keyEvents: List<String> = emptyList(),
    val status: String = "completed",
    val createdAt: Timestamp? = null
)

enum class MatchEventType {
    KICKOFF,
    GOAL,
    SAVE,
    CARD,
    CHANCE,
    FULLTIME,
    PENALTY_SHOOTOUT
}

data class LiveMatchEvent(
    val minute: Int,
    val isHomeTeam: Boolean?,
    val type: MatchEventType,
    val headline: String,
    val detail: String
)

data class MatchSimulationSummary(
    val homeClub: Club,
    val awayClub: Club,
    val isSoloScrimmage: Boolean,
    val homeScore: Int,
    val awayScore: Int,
    val homePenalties: Int,
    val awayPenalties: Int,
    val penaltiesUsed: Boolean,
    val winnerOwnerId: String,
    val winnerClubName: String,
    val manOfTheMatch: String,
    val homePossession: Int,
    val homeShots: Int,
    val awayShots: Int,
    val homeShotsOnTarget: Int,
    val awayShotsOnTarget: Int,
    val events: List<LiveMatchEvent>
)
