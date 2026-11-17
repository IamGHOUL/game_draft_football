package com.example.engine

import com.example.data.PlayerCatalog
import com.example.model.Club
import com.example.model.LiveMatchEvent
import com.example.model.MatchEventType
import com.example.model.MatchSimulationSummary
import com.example.model.Player
import com.example.model.Position
import kotlin.random.Random

object MatchSimulator {

    fun simulateFullMatch(
        homeClub: Club,
        awayClub: Club,
        isSoloScrimmage: Boolean,
        decisiveCupTiebreaker: Boolean = true,
        random: Random = Random.Default
    ): MatchSimulationSummary {
        val homePlayers = PlayerCatalog.resolveSquad(homeClub.playerIds)
        val awayPlayers = PlayerCatalog.resolveSquad(awayClub.playerIds)

        val homeAtk = homeClub.attackRating.toDouble().coerceAtLeast(60.0)
        val homeMid = homeClub.midfieldRating.toDouble().coerceAtLeast(60.0)
        val homeDef = homeClub.defenseRating.toDouble().coerceAtLeast(60.0)

        val awayAtk = awayClub.attackRating.toDouble().coerceAtLeast(60.0)
        val awayMid = awayClub.midfieldRating.toDouble().coerceAtLeast(60.0)
        val awayDef = awayClub.defenseRating.toDouble().coerceAtLeast(60.0)

        val midDiff = homeMid - awayMid
        val homePossession = (50 + (midDiff * 0.85) + random.nextInt(-4, 5)).toInt().coerceIn(30, 70)

        var homeScore = 0
        var awayScore = 0
        var homeShots = 0
        var awayShots = 0
        var homeOnTarget = 0
        var awayOnTarget = 0

        val goalCountByPlayer = mutableMapOf<String, Int>()
        val events = mutableListOf<LiveMatchEvent>()

        events += LiveMatchEvent(
            minute = 1,
            isHomeTeam = null,
            type = MatchEventType.KICKOFF,
            headline = "Kick-off under the floodlights!",
            detail = "${homeClub.clubName} (${homeClub.formation}) vs ${awayClub.clubName} (${awayClub.formation})"
        )

        // Check key moments every 5 minutes across 90 minutes
        val checkMinutes = listOf(6, 13, 21, 28, 35, 42, 48, 55, 63, 71, 78, 84, 89)
        for (minute in checkMinutes) {
            val homeControlProb = (homePossession / 100.0).coerceIn(0.32, 0.68)
            val isHomeAttack = random.nextDouble() < homeControlProb

            val attackingPlayers = if (isHomeAttack) homePlayers else awayPlayers
            val defendingPlayers = if (isHomeAttack) awayPlayers else homePlayers
            val atkRating = if (isHomeAttack) homeAtk else awayAtk
            val defRating = if (isHomeAttack) awayDef else homeDef
            val attackingClubName = if (isHomeAttack) homeClub.clubName else awayClub.clubName

            val attacker = pickAttacker(attackingPlayers, random)
            val creator = pickCreator(attackingPlayers, attacker, random)
            val keeper = defendingPlayers.firstOrNull { it.position == Position.GK }
            val defender = defendingPlayers.filter { it.position == Position.DEF }.randomOrNull(random)

            // Shot happens on 85% of key highlights, card on 15%
            if (random.nextDouble() < 0.14) {
                val booked = defender ?: attackingPlayers.randomOrNull(random)
                if (booked != null) {
                    events += LiveMatchEvent(
                        minute = minute,
                        isHomeTeam = !isHomeAttack,
                        type = MatchEventType.CARD,
                        headline = "Yellow Card — ${booked.shortName}",
                        detail = "${booked.name} goes into the referee's book for a tactical foul stopping a counter."
                    )
                }
                continue
            }

            if (isHomeAttack) homeShots++ else awayShots++

            // Goal probability driven by Attack vs Defense delta + shooter finishing
            val ratingEdge = (atkRating - defRating) * 0.011
            val shooterEdge = ((attacker?.shooting ?: 75) - (keeper?.overall ?: 75)) * 0.0045
            val goalProb = (0.24 + ratingEdge + shooterEdge).coerceIn(0.10, 0.48)
            val saveProb = 0.42

            val roll = random.nextDouble()
            when {
                roll < goalProb -> {
                    if (isHomeAttack) {
                        homeScore++
                        homeOnTarget++
                    } else {
                        awayScore++
                        awayOnTarget++
                    }
                    val scorerName = attacker?.name ?: "$attackingClubName Striker"
                    goalCountByPlayer[scorerName] = (goalCountByPlayer[scorerName] ?: 0) + 1
                    val assistText = if (creator != null && creator.id != attacker?.id) {
                        " Assisted by a razor-sharp pass from ${creator.shortName}."
                    } else ""
                    events += LiveMatchEvent(
                        minute = minute,
                        isHomeTeam = isHomeAttack,
                        type = MatchEventType.GOAL,
                        headline = "GOAL! $scorerName ($attackingClubName)",
                        detail = "${attacker?.shortName ?: "Forward"} unleashes a clinical finish!$assistText"
                    )
                }
                roll < goalProb + saveProb -> {
                    if (isHomeAttack) homeOnTarget++ else awayOnTarget++
                    val keeperName = keeper?.shortName ?: "Keeper"
                    events += LiveMatchEvent(
                        minute = minute,
                        isHomeTeam = !isHomeAttack,
                        type = MatchEventType.SAVE,
                        headline = "Big Save by $keeperName!",
                        detail = "${attacker?.shortName ?: "Attacker"} strikes cleanly, but $keeperName tips it past the post."
                    )
                }
                else -> {
                    events += LiveMatchEvent(
                        minute = minute,
                        isHomeTeam = isHomeAttack,
                        type = MatchEventType.CHANCE,
                        headline = "Close Call for $attackingClubName",
                        detail = "${attacker?.shortName ?: "Forward"} rattles a half-chance just wide of the upright."
                    )
                }
            }
        }

        // Ensure extra realistic baseline shots
        homeShots = (homeShots + random.nextInt(3, 7)).coerceAtLeast(homeOnTarget)
        awayShots = (awayShots + random.nextInt(3, 7)).coerceAtLeast(awayOnTarget)

        var homePens = 0
        var awayPens = 0
        var penaltiesUsed = false

        if (homeScore == awayScore && decisiveCupTiebreaker) {
            penaltiesUsed = true
            val homeGkSkill = homePlayers.firstOrNull { it.position == Position.GK }?.overall ?: 78
            val awayGkSkill = awayPlayers.firstOrNull { it.position == Position.GK }?.overall ?: 78

            // 5-kick shootout + sudden death if needed
            for (kick in 1..5) {
                if (random.nextDouble() < (0.76 - (awayGkSkill - 80) * 0.005)) homePens++
                if (random.nextDouble() < (0.76 - (homeGkSkill - 80) * 0.005)) awayPens++
            }
            while (homePens == awayPens) {
                val hScored = random.nextDouble() < 0.75
                val aScored = random.nextDouble() < 0.75
                if (hScored) homePens++
                if (aScored) awayPens++
            }
            val penWinner = if (homePens > awayPens) homeClub.clubName else awayClub.clubName
            events += LiveMatchEvent(
                minute = 90,
                isHomeTeam = homePens > awayPens,
                type = MatchEventType.PENALTY_SHOOTOUT,
                headline = "Penalty Shootout: $homePens - $awayPens",
                detail = "Deadlocked after 90'! $penWinner holds their nerve from the spot to claim victory!"
            )
        }

        val winnerOwnerId: String
        val winnerClubName: String
        when {
            homeScore > awayScore || (penaltiesUsed && homePens > awayPens) -> {
                winnerOwnerId = homeClub.ownerId
                winnerClubName = homeClub.clubName
            }
            awayScore > homeScore || (penaltiesUsed && awayPens > homePens) -> {
                winnerOwnerId = awayClub.ownerId
                winnerClubName = awayClub.clubName
            }
            else -> {
                winnerOwnerId = "DRAW"
                winnerClubName = "Match Drawn"
            }
        }

        val motm = goalCountByPlayer.maxByOrNull { it.value }?.key
            ?: (if (winnerOwnerId == awayClub.ownerId) awayPlayers else homePlayers)
                .maxByOrNull { it.overall }?.name
            ?: "Club Captain"

        events += LiveMatchEvent(
            minute = 90,
            isHomeTeam = null,
            type = MatchEventType.FULLTIME,
            headline = if (winnerOwnerId == "DRAW") "Full Time — Draw ($homeScore-$awayScore)"
            else "Full Time — $winnerClubName Win!",
            detail = "Man of the Match: $motm"
        )

        return MatchSimulationSummary(
            homeClub = homeClub,
            awayClub = awayClub,
            isSoloScrimmage = isSoloScrimmage,
            homeScore = homeScore,
            awayScore = awayScore,
            homePenalties = homePens,
            awayPenalties = awayPens,
            penaltiesUsed = penaltiesUsed,
            winnerOwnerId = winnerOwnerId,
            winnerClubName = winnerClubName,
            manOfTheMatch = motm,
            homePossession = homePossession,
            homeShots = homeShots,
            awayShots = awayShots,
            homeShotsOnTarget = homeOnTarget,
            awayShotsOnTarget = awayOnTarget,
            events = events
        )
    }

    private fun pickAttacker(squad: List<Player>, random: Random): Player? {
        val forwards = squad.filter { it.position == Position.FWD }
        val mids = squad.filter { it.position == Position.MID }
        return when {
            forwards.isNotEmpty() && random.nextDouble() < 0.68 -> forwards.random(random)
            mids.isNotEmpty() -> mids.random(random)
            else -> squad.filter { it.position != Position.GK }.randomOrNull(random)
        }
    }

    private fun pickCreator(squad: List<Player>, scorer: Player?, random: Random): Player? {
        val candidates = squad.filter { it.id != scorer?.id && it.position != Position.GK }
        val mids = candidates.filter { it.position == Position.MID }
        return if (mids.isNotEmpty() && random.nextDouble() < 0.65) mids.random(random)
        else candidates.randomOrNull(random)
    }
}
