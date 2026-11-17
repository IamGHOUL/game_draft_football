package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PitchDraftRepository
import com.example.data.PlayerCatalog
import com.example.engine.MatchSimulator
import com.example.model.CLUB_BUDGET_CAP_TENTHS
import com.example.model.Club
import com.example.model.Formation
import com.example.model.LiveMatchEvent
import com.example.model.MatchRecord
import com.example.model.MatchSimulationSummary
import com.example.model.Player
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class DraftState(
    val initializedFromServer: Boolean = false,
    val clubName: String = "Emerald Rovers",
    val managerAlias: String = "Manager",
    val formation: Formation = Formation.F433,
    val crestColorHex: String = "#10B981",
    val playerIds: List<String> = emptyList(),
    val isSaving: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val feedbackMessage: String? = null,
    val isErrorFeedback: Boolean = false
) {
    val players: List<Player> get() = PlayerCatalog.resolveSquad(playerIds)
    val budgetSpent: Long get() = PlayerCatalog.computeBudgetSpent(players)
    val budgetRemaining: Long get() = (CLUB_BUDGET_CAP_TENTHS - budgetSpent).coerceAtLeast(0L)
    val ratings: PlayerCatalog.SquadRatings get() = PlayerCatalog.computeRatings(players, formation)
    val isCompleteSquad: Boolean get() = players.size == 11
}

data class SimulationUiState(
    val active: Boolean = false,
    val isRunning: Boolean = false,
    val isFinished: Boolean = false,
    val currentMinute: Int = 0,
    val currentHomeScore: Int = 0,
    val currentAwayScore: Int = 0,
    val visibleEvents: List<LiveMatchEvent> = emptyList(),
    val summary: MatchSimulationSummary? = null,
    val speedMultiplier: Int = 1,
    val isPersistingResult: Boolean = false,
    val persistenceError: String? = null
)

class PitchDraftViewModel(
    private val repository: PitchDraftRepository,
    val currentUserId: String,
    defaultManagerName: String
) : ViewModel() {

    val myClubState: StateFlow<UiState<Club?>> = repository.observeMyClub(currentUserId)
        .map<Club?, UiState<Club?>> { club ->
            syncDraftIfNeeded(club)
            UiState.Success(club)
        }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing club", rootError)
            emit(UiState.Error(rootError.localizedMessage ?: "Failed to load your club"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val readyClubsState: StateFlow<UiState<List<Club>>> = repository.observeReadyClubs()
        .map<List<Club>, UiState<List<Club>>> { clubs ->
            UiState.Success(
                clubs.sortedWith(
                    compareByDescending<Club> { it.points }
                        .thenByDescending { it.goalDifference }
                        .thenByDescending { it.overallRating }
                )
            )
        }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing ready clubs", rootError)
            emit(UiState.Error(rootError.localizedMessage ?: "Failed to load league clubs"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val matchHistoryState: StateFlow<UiState<List<MatchRecord>>> = repository.observeMyMatches(currentUserId)
        .map<List<MatchRecord>, UiState<List<MatchRecord>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing match history", rootError)
            emit(UiState.Error(rootError.localizedMessage ?: "Failed to load match history"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    private val cleanDefaultAlias = defaultManagerName.trim()
        .ifBlank { "Coach" }
        .take(30)
        .let { if (it.length < 2) "Coach" else it }

    private val _draftState = MutableStateFlow(
        DraftState(
            clubName = "${cleanDefaultAlias.split(" ").firstOrNull()?.take(16) ?: "Apex"} FC".let {
                if (it.length < 2) "Apex FC" else it
            },
            managerAlias = cleanDefaultAlias
        )
    )
    val draftState: StateFlow<DraftState> = _draftState.asStateFlow()

    private val _simState = MutableStateFlow(SimulationUiState())
    val simState: StateFlow<SimulationUiState> = _simState.asStateFlow()

    private var simJob: Job? = null

    private fun syncDraftIfNeeded(serverClub: Club?) {
        _draftState.update { current ->
            if (current.initializedFromServer || current.hasUnsavedChanges) {
                current
            } else if (serverClub != null) {
                current.copy(
                    initializedFromServer = true,
                    clubName = serverClub.clubName,
                    managerAlias = serverClub.managerAlias,
                    formation = Formation.fromCode(serverClub.formation),
                    crestColorHex = serverClub.crestColorHex,
                    playerIds = serverClub.playerIds
                )
            } else {
                current.copy(initializedFromServer = true)
            }
        }
    }

    fun updateClubDetails(clubName: String, managerAlias: String, crestColorHex: String) {
        _draftState.update {
            it.copy(
                clubName = clubName.take(32),
                managerAlias = managerAlias.take(30),
                crestColorHex = crestColorHex,
                hasUnsavedChanges = true,
                feedbackMessage = null
            )
        }
    }

    fun selectFormation(formation: Formation) {
        _draftState.update { current ->
            val fitted = PlayerCatalog.fitSquadToFormation(current.players, formation)
            current.copy(
                formation = formation,
                playerIds = fitted.map { it.id },
                hasUnsavedChanges = true,
                feedbackMessage = "Formation set to ${formation.code}" +
                    if (fitted.size < current.playerIds.size) " (adjusted overflow slots)" else "",
                isErrorFeedback = false
            )
        }
    }

    fun signPlayer(player: Player) {
        _draftState.update { current ->
            val reason = PlayerCatalog.canSignReason(player, current.players, current.formation)
            if (reason != null) {
                current.copy(feedbackMessage = reason, isErrorFeedback = true)
            } else {
                current.copy(
                    playerIds = current.playerIds + player.id,
                    hasUnsavedChanges = true,
                    feedbackMessage = "Signed ${player.shortName}!",
                    isErrorFeedback = false
                )
            }
        }
    }

    fun releasePlayer(playerId: String) {
        _draftState.update { current ->
            val updated = current.playerIds.filterNot { it == playerId }
            current.copy(
                playerIds = updated,
                hasUnsavedChanges = true,
                feedbackMessage = "Player released — budget refunded.",
                isErrorFeedback = false
            )
        }
    }

    fun autoDraftSquad() {
        _draftState.update { current ->
            val drafted = PlayerCatalog.autoDraftRemaining(current.players, current.formation)
            current.copy(
                playerIds = drafted.map { it.id },
                hasUnsavedChanges = true,
                feedbackMessage = "Auto-drafted ${drafted.size}/11 squad within budget!",
                isErrorFeedback = false
            )
        }
    }

    fun clearSquad() {
        _draftState.update { current ->
            current.copy(
                playerIds = emptyList(),
                hasUnsavedChanges = true,
                feedbackMessage = "Squad cleared — $100.0M budget ready.",
                isErrorFeedback = false
            )
        }
    }

    fun dismissFeedback() {
        _draftState.update { it.copy(feedbackMessage = null) }
    }

    fun saveAndPublishClub() {
        val currentDraft = _draftState.value
        val existingServerClub = (myClubState.value as? UiState.Success)?.data
        if (currentDraft.clubName.trim().length < 2) {
            _draftState.update {
                it.copy(feedbackMessage = "Club name must be at least 2 characters.", isErrorFeedback = true)
            }
            return
        }
        if (currentDraft.managerAlias.trim().length < 2) {
            _draftState.update {
                it.copy(feedbackMessage = "Manager name must be at least 2 characters.", isErrorFeedback = true)
            }
            return
        }
        viewModelScope.launch {
            _draftState.update { it.copy(isSaving = true, feedbackMessage = null) }
            val result = repository.saveClub(
                existingClub = existingServerClub,
                clubName = currentDraft.clubName,
                managerAlias = currentDraft.managerAlias,
                formation = currentDraft.formation,
                crestColorHex = currentDraft.crestColorHex,
                playerIds = currentDraft.playerIds
            )
            _draftState.update { state ->
                result.fold(
                    onSuccess = {
                        state.copy(
                            isSaving = false,
                            hasUnsavedChanges = false,
                            feedbackMessage = if (state.isCompleteSquad) {
                                "Club saved & published for multiplayer challenges!"
                            } else {
                                "Draft saved (${state.playerIds.size}/11). Add 11 players to compete online."
                            },
                            isErrorFeedback = false
                        )
                    },
                    onFailure = { err ->
                        state.copy(
                            isSaving = false,
                            feedbackMessage = err.localizedMessage ?: "Failed to save club.",
                            isErrorFeedback = true
                        )
                    }
                )
            }
        }
    }

    fun startMatchSimulation(
        awayClub: Club,
        isSoloScrimmage: Boolean,
        decisivePenalties: Boolean = true
    ) {
        val serverClub = (myClubState.value as? UiState.Success)?.data
        val draft = _draftState.value
        val homeClub = serverClub?.takeIf { !draft.hasUnsavedChanges && it.ready } ?: Club(
            id = currentUserId,
            ownerId = currentUserId,
            clubName = draft.clubName.trim().ifBlank { "My Club" },
            managerAlias = draft.managerAlias.trim().ifBlank { "Manager" },
            formation = draft.formation.code,
            crestColorHex = draft.crestColorHex,
            playerIds = draft.playerIds,
            budgetCap = CLUB_BUDGET_CAP_TENTHS,
            budgetSpent = draft.budgetSpent,
            budgetRemaining = draft.budgetRemaining,
            overallRating = draft.ratings.overall,
            attackRating = draft.ratings.attack,
            midfieldRating = draft.ratings.midfield,
            defenseRating = draft.ratings.defense,
            ready = draft.isCompleteSquad,
            wins = serverClub?.wins ?: 0L,
            draws = serverClub?.draws ?: 0L,
            losses = serverClub?.losses ?: 0L,
            points = serverClub?.points ?: 0L,
            goalsFor = serverClub?.goalsFor ?: 0L,
            goalsAgainst = serverClub?.goalsAgainst ?: 0L
        )

        if (homeClub.playerIds.size < 11) {
            _draftState.update {
                it.copy(
                    feedbackMessage = "Draft 11 players before kicking off a match!",
                    isErrorFeedback = true
                )
            }
            return
        }

        val summary = MatchSimulator.simulateFullMatch(
            homeClub = homeClub,
            awayClub = awayClub,
            isSoloScrimmage = isSoloScrimmage,
            decisiveCupTiebreaker = decisivePenalties
        )

        simJob?.cancel()
        _simState.value = SimulationUiState(
            active = true,
            isRunning = true,
            isFinished = false,
            currentMinute = 0,
            currentHomeScore = 0,
            currentAwayScore = 0,
            visibleEvents = emptyList(),
            summary = summary,
            speedMultiplier = 1
        )

        simJob = viewModelScope.launch {
            var runningHome = 0
            var runningAway = 0
            val shown = mutableListOf<LiveMatchEvent>()

            for (event in summary.events) {
                val speed = _simState.value.speedMultiplier
                if (speed < 100) {
                    val stepDelay = if (speed >= 4) 160L else 550L
                    delay(stepDelay)
                }
                if (event.type == com.example.model.MatchEventType.GOAL) {
                    if (event.isHomeTeam == true) runningHome++ else if (event.isHomeTeam == false) runningAway++
                }
                shown.add(0, event)
                _simState.update {
                    it.copy(
                        currentMinute = event.minute,
                        currentHomeScore = runningHome,
                        currentAwayScore = runningAway,
                        visibleEvents = shown.toList()
                    )
                }
            }

            finishAndPersistSimulation(summary)
        }
    }

    fun setSimSpeed(multiplier: Int) {
        _simState.update { it.copy(speedMultiplier = multiplier) }
    }

    fun skipToFullTime() {
        val summary = _simState.value.summary ?: return
        simJob?.cancel()
        _simState.update {
            it.copy(
                currentMinute = 90,
                currentHomeScore = summary.homeScore,
                currentAwayScore = summary.awayScore,
                visibleEvents = summary.events.reversed(),
                speedMultiplier = 100
            )
        }
        viewModelScope.launch {
            finishAndPersistSimulation(summary)
        }
    }

    private suspend fun finishAndPersistSimulation(summary: MatchSimulationSummary) {
        _simState.update {
            it.copy(
                isRunning = false,
                isFinished = true,
                currentMinute = 90,
                currentHomeScore = summary.homeScore,
                currentAwayScore = summary.awayScore,
                isPersistingResult = !summary.isSoloScrimmage
            )
        }

        if (!summary.isSoloScrimmage) {
            val res = repository.recordMultiplayerMatch(summary)
            _simState.update { state ->
                res.fold(
                    onSuccess = { state.copy(isPersistingResult = false, persistenceError = null) },
                    onFailure = { err ->
                        state.copy(
                            isPersistingResult = false,
                            persistenceError = err.localizedMessage ?: "Could not sync match result."
                        )
                    }
                )
            }
        }
    }

    fun closeSimulation() {
        simJob?.cancel()
        _simState.value = SimulationUiState()
    }

    private companion object {
        const val TAG = "PitchDraftVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
