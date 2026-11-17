package com.example

import com.example.data.PlayerCatalog
import com.example.engine.MatchSimulator
import com.example.model.CLUB_BUDGET_CAP_TENTHS
import com.example.model.Club
import com.example.model.Formation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MatchSimulatorTest {

    @Test
    fun `autoDraftRemaining produces valid 11 player squad within 100M budget`() {
        Formation.entries.forEach { formation ->
            val squad = PlayerCatalog.autoDraftRemaining(emptyList(), formation)
            assertEquals(11, squad.size)
            val spent = PlayerCatalog.computeBudgetSpent(squad)
            assertTrue("Spent $spent exceeded cap", spent <= CLUB_BUDGET_CAP_TENTHS)
            assertEquals(formation.gkCount, squad.count { it.position.code == "GK" })
            assertEquals(formation.defCount, squad.count { it.position.code == "DEF" })
            assertEquals(formation.midCount, squad.count { it.position.code == "MID" })
            assertEquals(formation.fwdCount, squad.count { it.position.code == "FWD" })
        }
    }

    @Test
    fun `simulateFullMatch produces decisive winner with cup tiebreaker`() {
        val homePlayers = PlayerCatalog.autoDraftRemaining(emptyList(), Formation.F433)
        val homeRatings = PlayerCatalog.computeRatings(homePlayers, Formation.F433)
        val homeClub = Club(
            id = "user_a",
            ownerId = "user_a",
            clubName = "Emerald Rovers",
            managerAlias = "Alex",
            formation = Formation.F433.code,
            playerIds = homePlayers.map { it.id },
            overallRating = homeRatings.overall,
            attackRating = homeRatings.attack,
            midfieldRating = homeRatings.midfield,
            defenseRating = homeRatings.defense,
            ready = true
        )
        val awayClub = PlayerCatalog.soloSparringClub()

        val summary = MatchSimulator.simulateFullMatch(
            homeClub = homeClub,
            awayClub = awayClub,
            isSoloScrimmage = false,
            decisiveCupTiebreaker = true,
            random = Random(42)
        )

        assertTrue(
            summary.winnerOwnerId == homeClub.ownerId || summary.winnerOwnerId == awayClub.ownerId
        )
        assertTrue(summary.events.isNotEmpty())
        assertTrue(summary.manOfTheMatch.isNotBlank())
    }
}
