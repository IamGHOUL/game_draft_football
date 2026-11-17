package com.example.data

import com.example.model.CLUB_BUDGET_CAP_TENTHS
import com.example.model.Club
import com.example.model.Formation
import com.example.model.Player
import com.example.model.Position

object PlayerCatalog {

    val allPlayers: List<Player> = listOf(
        // GOALKEEPERS (6)
        Player("gk_1", "Thibaut Laurent", "Laurent", 1, "BEL", Position.GK, 90, 52, 40, 78, 89, 115L, "Shot Stopper"),
        Player("gk_2", "Alisson Costa", "Costa", 1, "BRA", Position.GK, 88, 55, 42, 85, 86, 100L, "Sweeper Keeper"),
        Player("gk_3", "Diogo Marques", "Marques", 99, "POR", Position.GK, 85, 54, 38, 82, 83, 80L, "Penalty Specialist"),
        Player("gk_4", "Lucas Chevalier", "Chevalier", 30, "FRA", Position.GK, 82, 53, 35, 76, 80, 65L, "Reflex Save"),
        Player("gk_5", "Kenji Takahashi", "Takahashi", 12, "JPN", Position.GK, 78, 56, 36, 75, 76, 45L, "Quick Distribution"),
        Player("gk_6", "Tomas Novak", "Novak", 13, "CZE", Position.GK, 75, 49, 32, 71, 74, 35L, "Commanding Box"),

        // DEFENDERS (14)
        Player("def_1", "Virgil van Dijk", "Van Dijk", 4, "NED", Position.DEF, 91, 80, 62, 78, 92, 130L, "Aerial Fortress"),
        Player("def_2", "William Saliba", "Saliba", 2, "FRA", Position.DEF, 89, 83, 55, 77, 90, 115L, "Clean Tackler"),
        Player("def_3", "Achraf Hakimi", "Hakimi", 2, "MAR", Position.DEF, 88, 93, 75, 82, 81, 110L, "Overlapping Engine"),
        Player("def_4", "Theo Hernandez", "Hernandez", 19, "FRA", Position.DEF, 87, 92, 74, 80, 82, 105L, "Turbo Wingback"),
        Player("def_5", "Ruben Dias", "Dias", 3, "POR", Position.DEF, 88, 72, 52, 76, 90, 105L, "Defensive Leader"),
        Player("def_6", "Alessandro Bastoni", "Bastoni", 95, "ITA", Position.DEF, 86, 77, 56, 83, 87, 95L, "Ball-Playing CB"),
        Player("def_7", "Josko Gvardiol", "Gvardiol", 24, "CRO", Position.DEF, 85, 81, 66, 79, 86, 90L, "Versatile Wall"),
        Player("def_8", "Trent Alexander", "Alexander", 66, "ENG", Position.DEF, 86, 79, 72, 92, 78, 95L, "Pinpoint Crosser"),
        Player("def_9", "Ronald Araujo", "Araujo", 4, "URU", Position.DEF, 84, 85, 58, 70, 86, 85L, "Recovery Sprint"),
        Player("def_10", "Pedro Porro", "Porro", 23, "ESP", Position.DEF, 82, 83, 73, 81, 78, 70L, "Set-Piece Whip"),
        Player("def_11", "Min-jae Kim", "Kim", 3, "KOR", Position.DEF, 83, 80, 50, 72, 85, 75L, "Iron Duelist"),
        Player("def_12", "Antonee Robinson", "Robinson", 33, "USA", Position.DEF, 80, 88, 60, 75, 78, 55L, "Relentless Runner"),
        Player("def_13", "Malo Gusto", "Gusto", 27, "FRA", Position.DEF, 79, 86, 61, 76, 76, 50L, "Quick Interceptor"),
        Player("def_14", "Kofi Mensah", "Mensah", 5, "GHA", Position.DEF, 75, 77, 48, 68, 77, 35L, "Low-Block Anchor"),

        // MIDFIELDERS (14)
        Player("mid_1", "Rodri Hernandez", "Rodri", 16, "ESP", Position.MID, 92, 73, 81, 91, 88, 150L, "Metronome"),
        Player("mid_2", "Jude Bellingham", "Bellingham", 5, "ENG", Position.MID, 91, 83, 87, 86, 79, 145L, "Box Crasher"),
        Player("mid_3", "Kevin De Bruyne", "De Bruyne", 17, "BEL", Position.MID, 90, 74, 86, 94, 65, 135L, "Visionary Architect"),
        Player("mid_4", "Federico Valverde", "Valverde", 8, "URU", Position.MID, 89, 89, 83, 86, 82, 125L, "Box-to-Box Engine"),
        Player("mid_5", "Martin Odegaard", "Odegaard", 8, "NOR", Position.MID, 88, 78, 81, 90, 68, 115L, "Through-Ball Maestro"),
        Player("mid_6", "Florian Wirtz", "Wirtz", 10, "GER", Position.MID, 88, 84, 82, 88, 60, 115L, "Pocket Playmaker"),
        Player("mid_7", "Declan Rice", "Rice", 41, "ENG", Position.MID, 87, 79, 74, 84, 86, 105L, "Ball Winner"),
        Player("mid_8", "Pedri Gonzalez", "Pedri", 8, "ESP", Position.MID, 86, 80, 72, 88, 72, 95L, "Press Resistant"),
        Player("mid_9", "Alexis Mac Allister", "Mac Allister", 10, "ARG", Position.MID, 85, 75, 80, 86, 76, 85L, "Tempo Controller"),
        Player("mid_10", "Tijjani Reijnders", "Reijnders", 14, "NED", Position.MID, 83, 81, 77, 83, 74, 70L, "Ball Carrier"),
        Player("mid_11", "Eduardo Camavinga", "Camavinga", 6, "FRA", Position.MID, 83, 82, 70, 82, 81, 75L, "Press Breaker"),
        Player("mid_12", "Joao Neves", "Neves", 87, "POR", Position.MID, 82, 79, 71, 83, 80, 65L, "Tenacious Terrier"),
        Player("mid_13", "Weston McKennie", "McKennie", 16, "USA", Position.MID, 79, 80, 74, 77, 76, 50L, "Aerial Midfielder"),
        Player("mid_14", "Lucas Bergvall", "Bergvall", 15, "SWE", Position.MID, 76, 78, 71, 78, 68, 35L, "Youth Spark"),

        // FORWARDS (10)
        Player("fwd_1", "Kylian Mbappe", "Mbappe", 9, "FRA", Position.FWD, 93, 97, 91, 82, 38, 175L, "Supersonic Finisher"),
        Player("fwd_2", "Erling Haaland", "Haaland", 9, "NOR", Position.FWD, 92, 89, 94, 68, 45, 165L, "Clinical Cyborg"),
        Player("fwd_3", "Vinicius Junior", "Vini Jr", 7, "BRA", Position.FWD, 91, 95, 85, 83, 32, 155L, "Electric Dribbler"),
        Player("fwd_4", "Mohamed Salah", "Salah", 11, "EGY", Position.FWD, 90, 89, 89, 84, 45, 140L, "Inside Curler"),
        Player("fwd_5", "Bukayo Saka", "Saka", 7, "ENG", Position.FWD, 88, 86, 84, 85, 60, 120L, "Clutch Winger"),
        Player("fwd_6", "Lautaro Martinez", "Martinez", 10, "ARG", Position.FWD, 88, 83, 89, 76, 52, 115L, "Poacher Instinct"),
        Player("fwd_7", "Rafael Leao", "Leao", 10, "POR", Position.FWD, 86, 93, 82, 79, 34, 95L, "Counter Threat"),
        Player("fwd_8", "Julian Alvarez", "Alvarez", 19, "ARG", Position.FWD, 85, 85, 85, 81, 56, 85L, "Pressing Forward"),
        Player("fwd_9", "Omar Marmoush", "Marmoush", 7, "EGY", Position.FWD, 83, 90, 82, 76, 42, 65L, "Direct Striker"),
        Player("fwd_10", "Mateo Retegui", "Retegui", 32, "ITA", Position.FWD, 80, 78, 83, 68, 40, 45L, "Target Striker")
    )

    private val byId = allPlayers.associateBy { it.id }

    fun getPlayer(id: String): Player? = byId[id]

    fun resolveSquad(playerIds: List<String>): List<Player> =
        playerIds.mapNotNull { byId[it] }

    fun computeBudgetSpent(players: List<Player>): Long =
        players.sumOf { it.priceTenths }

    data class SquadRatings(
        val overall: Long,
        val attack: Long,
        val midfield: Long,
        val defense: Long
    )

    fun computeRatings(players: List<Player>, formation: Formation): SquadRatings {
        if (players.isEmpty()) return SquadRatings(0L, 0L, 0L, 0L)
        val fwds = players.filter { it.position == Position.FWD }
        val mids = players.filter { it.position == Position.MID }
        val defs = players.filter { it.position == Position.DEF || it.position == Position.GK }

        val atkBase = if (fwds.isNotEmpty()) fwds.map { it.overall }.average() else players.map { it.shooting }.average()
        val midBase = if (mids.isNotEmpty()) mids.map { it.overall }.average() else players.map { it.passing }.average()
        val defBase = if (defs.isNotEmpty()) defs.map { it.overall }.average() else players.map { it.defending }.average()

        val atkBonus = if (formation == Formation.F433 && players.size == 11) 2 else 0
        val midBonus = when {
            formation == Formation.F442 && players.size == 11 -> 2
            formation == Formation.F352 && players.size == 11 -> 1
            else -> 0
        }
        val defBonus = if (formation == Formation.F352 && players.size == 11) 1 else 0

        val atk = (atkBase + atkBonus).toInt().coerceIn(0, 99).toLong()
        val mid = (midBase + midBonus).toInt().coerceIn(0, 99).toLong()
        val def = (defBase + defBonus).toInt().coerceIn(0, 99).toLong()
        val ovr = ((atk + mid + def) / 3.0).toInt().coerceIn(0, 99).toLong()
        return SquadRatings(ovr, atk, mid, def)
    }

    /**
     * Checks whether [candidate] can be signed given current drafted squad, formation slots, and budget.
     */
    fun canSignReason(
        candidate: Player,
        currentSquad: List<Player>,
        formation: Formation,
        budgetCap: Long = CLUB_BUDGET_CAP_TENTHS
    ): String? {
        if (currentSquad.any { it.id == candidate.id }) return "Already in squad"
        if (currentSquad.size >= 11) return "Squad full (11/11)"
        val posCount = currentSquad.count { it.position == candidate.position }
        val limit = formation.slotLimitFor(candidate.position)
        if (posCount >= limit) return "${candidate.position.code} slots full ($posCount/$limit)"
        val remaining = budgetCap - computeBudgetSpent(currentSquad)
        if (candidate.priceTenths > remaining) return "Over budget"
        return null
    }

    /**
     * Trims players if switching to a formation with fewer slots in a position.
     */
    fun fitSquadToFormation(currentSquad: List<Player>, targetFormation: Formation): List<Player> {
        val result = mutableListOf<Player>()
        for (pos in Position.entries) {
            val limit = targetFormation.slotLimitFor(pos)
            result += currentSquad.filter { it.position == pos }.take(limit)
        }
        return result
    }

    /**
     * Smartly fills any empty formation slots within the $100.0M budget while reserving enough funds
     * so every remaining slot can afford at least the cheapest player in that position.
     */
    fun autoDraftRemaining(
        currentSquad: List<Player>,
        formation: Formation,
        budgetCap: Long = CLUB_BUDGET_CAP_TENTHS
    ): List<Player> {
        val squad = fitSquadToFormation(currentSquad, formation).toMutableList()
        var spent = computeBudgetSpent(squad)

        val neededSlots = mutableListOf<Position>()
        for (pos in Position.entries) {
            val have = squad.count { it.position == pos }
            val limit = formation.slotLimitFor(pos)
            repeat((limit - have).coerceAtLeast(0)) {
                neededSlots.add(pos)
            }
        }

        for ((index, pos) in neededSlots.withIndex()) {
            val remainingByPos = neededSlots.drop(index + 1).groupingBy { it }.eachCount()
            val futureReserve = remainingByPos.entries.sumOf { (remPos, count) ->
                allPlayers
                    .filter { p -> p.position == remPos && squad.none { it.id == p.id } }
                    .sortedBy { it.priceTenths }
                    .take(count)
                    .sumOf { it.priceTenths }
            }
            val maxAffordable = (budgetCap - spent - futureReserve).coerceAtLeast(0L)
            val candidates = allPlayers
                .filter { p -> p.position == pos && squad.none { it.id == p.id } && p.priceTenths <= maxAffordable }
                .sortedByDescending { it.overall }

            val pick = candidates.take(3).randomOrNull()
                ?: allPlayers.filter { p -> p.position == pos && squad.none { it.id == p.id } }
                    .minByOrNull { it.priceTenths }
            if (pick != null && spent + pick.priceTenths <= budgetCap) {
                squad.add(pick)
                spent += pick.priceTenths
            }
        }
        return squad
    }

    /**
     * Local practice sparring XI (used only for explicit Offline Solo Practice mode when no rival is online).
     */
    fun soloSparringClub(): Club {
        val ids = listOf(
            "gk_5",
            "def_10", "def_12", "def_13", "def_14",
            "mid_10", "mid_12", "mid_13", "mid_14",
            "fwd_9", "fwd_10"
        )
        val players = resolveSquad(ids)
        val ratings = computeRatings(players, Formation.F442)
        val spent = computeBudgetSpent(players)
        return Club(
            id = "local_sparring_xi",
            ownerId = "local_sparring_xi",
            clubName = "Academy Sparring XI",
            managerAlias = "Practice Bot",
            formation = Formation.F442.code,
            crestColorHex = "#64748B",
            playerIds = ids,
            budgetCap = CLUB_BUDGET_CAP_TENTHS,
            budgetSpent = spent,
            budgetRemaining = CLUB_BUDGET_CAP_TENTHS - spent,
            overallRating = ratings.overall,
            attackRating = ratings.attack,
            midfieldRating = ratings.midfield,
            defenseRating = ratings.defense,
            ready = true
        )
    }
}
