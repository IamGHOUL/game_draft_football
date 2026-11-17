package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.auth.AuthScreen
import com.example.auth.authStateFlow
import com.example.auth.signOut
import com.example.data.PitchDraftRepository
import com.example.model.Position
import com.example.ui.screens.ArenaScreen
import com.example.ui.screens.LeagueScreen
import com.example.ui.screens.MarketScreen
import com.example.ui.screens.SquadScreen
import com.example.viewmodel.PitchDraftViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

private enum class PitchTab(
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    SQUAD(R.string.tab_squad, Icons.Filled.Groups, Icons.Outlined.Groups, "nav_tab_squad"),
    MARKET(R.string.tab_market, Icons.Filled.Storefront, Icons.Outlined.Storefront, "nav_tab_market"),
    ARENA(R.string.tab_arena, Icons.Filled.SportsSoccer, Icons.Outlined.SportsSoccer, "nav_tab_arena"),
    LEAGUE(R.string.tab_league, Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents, "nav_tab_league")
}

@Composable
fun PitchDraftApp(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        key(user.uid) {
            AuthenticatedPitchDraftContent(
                currentUserId = user.uid,
                defaultManagerName = user.displayName ?: "Manager"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthenticatedPitchDraftContent(
    currentUserId: String,
    defaultManagerName: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: PitchDraftViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                PitchDraftViewModel(
                    repository = PitchDraftRepository(db),
                    currentUserId = currentUserId,
                    defaultManagerName = defaultManagerName
                )
            }
        }
    )

    val myClubState by viewModel.myClubState.collectAsStateWithLifecycle()
    val readyClubsState by viewModel.readyClubsState.collectAsStateWithLifecycle()
    val matchHistoryState by viewModel.matchHistoryState.collectAsStateWithLifecycle()
    val draftState by viewModel.draftState.collectAsStateWithLifecycle()
    val simState by viewModel.simState.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(PitchTab.SQUAD) }
    var marketPositionFilter by remember { mutableStateOf<Position?>(null) }

    if (currentTab != PitchTab.SQUAD && !simState.active) {
        BackHandler { currentTab = PitchTab.SQUAD }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 720.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                if (!simState.active) {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.app_name).uppercase(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(currentTab.labelRes),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { signOut(credentialManager, scope) },
                                modifier = Modifier.testTag("sign_out_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = stringResource(R.string.sign_out),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                }
            },
            bottomBar = {
                if (!useRail && !simState.active) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        PitchTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = stringResource(tab.labelRes)
                                    )
                                },
                                label = { Text(stringResource(tab.labelRes)) },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail && !simState.active) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        PitchTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = stringResource(tab.labelRes)
                                    )
                                },
                                label = { Text(stringResource(tab.labelRes)) },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    when (currentTab) {
                        PitchTab.SQUAD -> SquadScreen(
                            myClubState = myClubState,
                            draftState = draftState,
                            onUpdateClubDetails = viewModel::updateClubDetails,
                            onSelectFormation = viewModel::selectFormation,
                            onAutoDraft = viewModel::autoDraftSquad,
                            onClearSquad = viewModel::clearSquad,
                            onReleasePlayer = viewModel::releasePlayer,
                            onSaveAndPublish = viewModel::saveAndPublishClub,
                            onOpenMarketForPosition = { pos ->
                                marketPositionFilter = pos
                                currentTab = PitchTab.MARKET
                            },
                            onDismissFeedback = viewModel::dismissFeedback
                        )

                        PitchTab.MARKET -> MarketScreen(
                            draftState = draftState,
                            initialPositionFilter = marketPositionFilter,
                            onClearInitialFilter = { marketPositionFilter = null },
                            onSignPlayer = viewModel::signPlayer,
                            onReleasePlayer = viewModel::releasePlayer
                        )

                        PitchTab.ARENA -> ArenaScreen(
                            currentUserId = currentUserId,
                            draftState = draftState,
                            readyClubsState = readyClubsState,
                            simState = simState,
                            onChallengeClub = { rival, penalties ->
                                viewModel.startMatchSimulation(
                                    awayClub = rival,
                                    isSoloScrimmage = false,
                                    decisivePenalties = penalties
                                )
                            },
                            onSoloScrimmage = { penalties ->
                                viewModel.startMatchSimulation(
                                    awayClub = com.example.data.PlayerCatalog.soloSparringClub(),
                                    isSoloScrimmage = true,
                                    decisivePenalties = penalties
                                )
                            },
                            onSetSimSpeed = viewModel::setSimSpeed,
                            onSkipToFullTime = viewModel::skipToFullTime,
                            onCloseSimulation = viewModel::closeSimulation,
                            onGoToSquad = { currentTab = PitchTab.SQUAD }
                        )

                        PitchTab.LEAGUE -> LeagueScreen(
                            currentUserId = currentUserId,
                            readyClubsState = readyClubsState,
                            matchHistoryState = matchHistoryState
                        )
                    }
                }
            }
        }
    }
}
