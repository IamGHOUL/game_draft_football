package com.example.data

import android.content.Context
import com.example.R
import com.example.model.CLUB_BUDGET_CAP_TENTHS
import com.example.model.Club
import com.example.model.Formation
import com.example.model.MatchRecord
import com.example.model.MatchSimulationSummary
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class PitchDraftRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeMyClub(userId: String): Flow<Club?> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user changed." }
        val docPath = "clubs/$userId"
        emitAll(
            db.collection("clubs").document(userId)
                .snapshots()
                .map { snapshot ->
                    if (!snapshot.exists()) null
                    else snapshot.toObject(Club::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(toLog, OperationType.GET, docPath)
                    }
                    throw error
                }
        )
    }

    fun observeReadyClubs(): Flow<List<Club>> = flow {
        val uid = requireUserId()
        val path = "clubs"
        emitAll(
            db.collection(path)
                .whereEqualTo("ready", true)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Club::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(toLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeMyMatches(userId: String): Flow<List<MatchRecord>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user changed." }
        val path = "matches"
        emitAll(
            db.collection(path)
                .whereArrayContains("participantIds", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(MatchRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(toLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveClub(
        existingClub: Club?,
        clubName: String,
        managerAlias: String,
        formation: Formation,
        crestColorHex: String,
        playerIds: List<String>
    ): Result<Unit> {
        val cleanName = clubName.trim().take(32)
        val cleanAlias = managerAlias.trim().take(30)
        var docPath = "clubs/${auth.currentUser?.uid ?: "unauthenticated"}"
        return try {
            val uid = requireUserId()
            require(cleanName.length in 2..32) { "Club name must be 2 to 32 characters." }
            require(cleanAlias.length in 2..30) { "Manager name must be 2 to 30 characters." }
            require(playerIds.size <= 11) { "Squad cannot exceed 11 starters." }

            val players = PlayerCatalog.resolveSquad(playerIds)
            val spent = PlayerCatalog.computeBudgetSpent(players)
            require(spent <= CLUB_BUDGET_CAP_TENTHS) { "Squad exceeds $100.0M transfer budget." }
            val remaining = CLUB_BUDGET_CAP_TENTHS - spent
            val ratings = PlayerCatalog.computeRatings(players, formation)
            val ready = players.size == 11

            val docRef = db.collection("clubs").document(uid)
            docPath = docRef.path

            if (existingClub == null) {
                val createPayload = mapOf(
                    "ownerId" to uid,
                    "clubName" to cleanName,
                    "managerAlias" to cleanAlias,
                    "formation" to formation.code,
                    "crestColorHex" to crestColorHex,
                    "playerIds" to players.map { it.id },
                    "budgetCap" to CLUB_BUDGET_CAP_TENTHS,
                    "budgetSpent" to spent,
                    "budgetRemaining" to remaining,
                    "overallRating" to ratings.overall,
                    "attackRating" to ratings.attack,
                    "midfieldRating" to ratings.midfield,
                    "defenseRating" to ratings.defense,
                    "ready" to ready,
                    "wins" to 0L,
                    "draws" to 0L,
                    "losses" to 0L,
                    "points" to 0L,
                    "goalsFor" to 0L,
                    "goalsAgainst" to 0L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(createPayload).await()
            } else {
                val updatePayload = mapOf(
                    "clubName" to cleanName,
                    "managerAlias" to cleanAlias,
                    "formation" to formation.code,
                    "crestColorHex" to crestColorHex,
                    "playerIds" to players.map { it.id },
                    "budgetSpent" to spent,
                    "budgetRemaining" to remaining,
                    "overallRating" to ratings.overall,
                    "attackRating" to ratings.attack,
                    "midfieldRating" to ratings.midfield,
                    "defenseRating" to ratings.defense,
                    "ready" to ready,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updatePayload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val op = if (existingClub == null) OperationType.CREATE else OperationType.UPDATE
            handleFirestoreError(e, op, docPath)
            Result.failure(e)
        }
    }

    suspend fun recordMultiplayerMatch(summary: MatchSimulationSummary): Result<Unit> {
        var docPath = "matches"
        return try {
            val uid = requireUserId()
            check(!summary.isSoloScrimmage) { "Solo scrimmages are not recorded to multiplayer standings." }
            check(summary.homeClub.ownerId == uid) { "Only the challenging manager can record this match." }
            require(summary.homeClub.ownerId != summary.awayClub.ownerId) { "Cannot record a self-match." }

            val matchRef = db.collection("matches").document()
            docPath = matchRef.path

            val highlights = summary.events
                .filter { it.type != com.example.model.MatchEventType.KICKOFF }
                .take(12)
                .map { "${it.minute}' ${it.headline}".take(120) }

            val matchPayload = mapOf(
                "homeOwnerId" to summary.homeClub.ownerId,
                "awayOwnerId" to summary.awayClub.ownerId,
                "participantIds" to listOf(summary.homeClub.ownerId, summary.awayClub.ownerId),
                "homeClubName" to summary.homeClub.clubName.take(32),
                "awayClubName" to summary.awayClub.clubName.take(32),
                "homeScore" to summary.homeScore.toLong(),
                "awayScore" to summary.awayScore.toLong(),
                "homePenalties" to summary.homePenalties.toLong(),
                "awayPenalties" to summary.awayPenalties.toLong(),
                "penaltiesUsed" to summary.penaltiesUsed,
                "winnerOwnerId" to summary.winnerOwnerId,
                "winnerClubName" to summary.winnerClubName.take(32),
                "manOfTheMatch" to summary.manOfTheMatch.take(40),
                "homePossession" to summary.homePossession.toLong(),
                "homeShots" to summary.homeShots.toLong(),
                "awayShots" to summary.awayShots.toLong(),
                "keyEvents" to highlights,
                "status" to "completed",
                "createdAt" to FieldValue.serverTimestamp()
            )
            matchRef.set(matchPayload).await()

            // Update ONLY the current user's own club standings per zero-trust rules
            val homeWon = summary.winnerOwnerId == uid
            val isDraw = summary.winnerOwnerId == "DRAW"
            val newWins = summary.homeClub.wins + if (homeWon) 1L else 0L
            val newDraws = summary.homeClub.draws + if (isDraw) 1L else 0L
            val newLosses = summary.homeClub.losses + if (!homeWon && !isDraw) 1L else 0L
            val newPoints = summary.homeClub.points + when {
                homeWon -> 3L
                isDraw -> 1L
                else -> 0L
            }

            val clubRef = db.collection("clubs").document(uid)
            docPath = clubRef.path
            clubRef.update(
                mapOf(
                    "wins" to newWins,
                    "draws" to newDraws,
                    "losses" to newLosses,
                    "points" to newPoints,
                    "goalsFor" to (summary.homeClub.goalsFor + summary.homeScore.toLong()),
                    "goalsAgainst" to (summary.homeClub.goalsAgainst + summary.awayScore.toLong()),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }
}
