package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ads.AdMobManager
import com.example.ads.findActivity
import com.example.auth.signOutFirebaseAndCredentials
import com.example.ui.components.AdMobBannerBar
import com.example.ui.components.ScoutBottomNav
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AdminLockScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.KegiatanScreen
import com.example.ui.screens.MateriScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.PusatBantuanScreen
import com.example.ui.screens.RegistrationScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TingkatanScreen
import com.example.ui.theme.PramukaGarudaTheme
import com.example.ui.theme.SneatBg
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ScoutViewModel
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    private val adMobManager = AdMobManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        adMobManager.initialize(this)
        setContent {
            PramukaGarudaTheme {
                val viewModel: ScoutViewModel = viewModel()
                PramukaGarudaApp(
                    viewModel = viewModel,
                    adMobManager = adMobManager
                )
            }
        }
    }
}

@Composable
fun PramukaGarudaApp(
    viewModel: ScoutViewModel,
    adMobManager: AdMobManager = remember { AdMobManager() }
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val rewardedAdState by adMobManager.rewardedAdState.collectAsStateWithLifecycle()

    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val canNavigateBack by viewModel.canNavigateBack.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val adminAccess by viewModel.adminAccess.collectAsStateWithLifecycle()
    val adminVerificationState by viewModel.adminVerificationState.collectAsStateWithLifecycle()
    val adminRecords by viewModel.adminRecords.collectAsStateWithLifecycle()
    val adminActionFeedback by viewModel.adminActionFeedback.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val institutionConfig by viewModel.institutionConfig.collectAsStateWithLifecycle()
    val adminConfig by viewModel.adminConfig.collectAsStateWithLifecycle()
    val isAdProUnlocked by viewModel.isAdProUnlocked.collectAsStateWithLifecycle()
    val adminLockError by viewModel.adminLockError.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val savedGmailAccounts by viewModel.savedGmailAccounts.collectAsStateWithLifecycle()
    val activities by viewModel.activities.collectAsStateWithLifecycle()
    val materials by viewModel.materials.collectAsStateWithLifecycle()
    val syllabi by viewModel.syllabi.collectAsStateWithLifecycle()
    val announcements by viewModel.announcements.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()

    val currentFirebaseUid = remember(adminVerificationState, syncState, userEmail) {
        if (runCatching { FirebaseApp.getApps(context).isNotEmpty() }.getOrDefault(false)) {
            runCatching { Firebase.auth.currentUser?.uid.orEmpty() }.getOrDefault("")
        } else {
            ""
        }
    }
    val isProUnlocked = adminConfig.isProSemesterPaid || isAdProUnlocked
    var materiInitialTab by remember { mutableIntStateOf(0) }

    BackHandler(enabled = canNavigateBack) {
        viewModel.navigateBack()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SneatBg,
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                val showBannerOnScreen = !isProUnlocked && currentScreen in setOf(
                    AppScreen.HOME,
                    AppScreen.TINGKATAN,
                    AppScreen.KEGIATAN,
                    AppScreen.MATERI,
                    AppScreen.PUSAT_BANTUAN
                )
                if (showBannerOnScreen) {
                    AdMobBannerBar(adUnitId = adMobManager.bannerAdUnitId)
                }
                ScoutBottomNav(
                    currentScreen = currentScreen,
                    onNavigate = { targetScreen ->
                        if (targetScreen == AppScreen.MATERI) {
                            materiInitialTab = 0
                        }
                        viewModel.navigateTo(targetScreen)
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxSize()
            ) {
                when (currentScreen) {
                    AppScreen.SPLASH -> {
                        SplashScreen(
                            isLoggedInSession = isLoggedIn,
                            onTimeout = { viewModel.onSplashFinished() }
                        )
                    }
                    AppScreen.AUTH -> {
                        AuthScreen(
                            savedGmailAccounts = savedGmailAccounts,
                            lastEmail = userEmail,
                            onSelectSavedGmail = { gmail ->
                                viewModel.loginWithSavedGmail(gmail)
                            },
                            onFirebaseGoogleSuccess = { firebaseUser ->
                                viewModel.loginWithFirebaseUser(firebaseUser)
                            },
                            onLoginSuccess = { email ->
                                viewModel.login(email)
                            }
                        )
                    }
                    AppScreen.REGISTRATION -> {
                        RegistrationScreen(
                            onSubmit = { name, school, gudep, rank, phone, avatarId, customPhotoPath ->
                                viewModel.submitRegistration(name, school, gudep, rank, phone, avatarId, customPhotoPath)
                            }
                        )
                    }
                    AppScreen.HOME -> {
                        HomeScreen(
                            profile = profile,
                            institutionConfig = institutionConfig,
                            materialsCount = materials.size,
                            syllabiCount = syllabi.size,
                            activities = activities,
                            announcements = announcements,
                            onNavigateToTingkatan = { viewModel.navigateTo(AppScreen.TINGKATAN) },
                            onNavigateToKegiatan = { viewModel.navigateTo(AppScreen.KEGIATAN) },
                            onNavigateToMateri = {
                                materiInitialTab = 0
                                viewModel.navigateTo(AppScreen.MATERI)
                            },
                            onNavigateToSilabus = {
                                materiInitialTab = 1
                                viewModel.navigateTo(AppScreen.MATERI)
                            },
                            onNavigateToBantuan = { viewModel.navigateTo(AppScreen.PUSAT_BANTUAN) },
                            onNavigateToProfil = { viewModel.navigateTo(AppScreen.PROFIL) }
                        )
                    }
                    AppScreen.TINGKATAN -> {
                        TingkatanScreen(
                            onBack = { viewModel.navigateBack() }
                        )
                    }
                    AppScreen.KEGIATAN -> {
                        KegiatanScreen(
                            activities = activities,
                            onToggleRegister = { actId -> viewModel.toggleRegisterActivity(actId) },
                            onBack = { viewModel.navigateBack() }
                        )
                    }
                    AppScreen.MATERI -> {
                        MateriScreen(
                            materials = materials,
                            syllabi = syllabi,
                            initialTab = materiInitialTab,
                            onBack = { viewModel.navigateBack() }
                        )
                    }
                    AppScreen.PUSAT_BANTUAN -> {
                        PusatBantuanScreen(
                            onBack = { viewModel.navigateBack() }
                        )
                    }
                    AppScreen.ADMIN_LOCK -> {
                        var adNotice by remember { mutableStateOf<String?>(null) }
                        LaunchedEffect(isProUnlocked) {
                            if (!isProUnlocked) {
                                adMobManager.loadRewardedAd(context)
                            } else {
                                adNotice = null
                            }
                        }
                        AdminLockScreen(
                            profile = profile,
                            isProUnlocked = isProUnlocked,
                            adminConfig = adminConfig,
                            lockError = adminLockError,
                            adminVerificationState = adminVerificationState,
                            rewardedAdState = rewardedAdState,
                            isUsingTestAdIds = adMobManager.isUsingTestIds,
                            adNotice = adNotice,
                            onWatchRewardedAd = {
                                adNotice = null
                                val currentActivity = activity
                                if (currentActivity != null) {
                                    adMobManager.showRewardedAd(
                                        activity = currentActivity,
                                        onRewardEarned = {
                                            adNotice = null
                                            viewModel.unlockProWithAd()
                                        },
                                        onClosedWithoutReward = {
                                            adNotice = "Iklan ditutup sebelum selesai. Tonton iklan AdMob hingga selesai untuk membuka Dashboard Pro."
                                        }
                                    )
                                } else {
                                    adMobManager.loadRewardedAd(context)
                                }
                            },
                            onRetryLoadAd = {
                                adNotice = null
                                adMobManager.loadRewardedAd(context)
                            },
                            onRetryFirebaseVerification = {
                                viewModel.verifyFirebaseAdminAccess(navigateOnSuccess = true)
                            },
                            onUnlockWithPayment = { viewModel.unlockProWithPayment() },
                            onSubmitFirstSetup = { fullName, rank, gudep, institutionName, photoPath, pin ->
                                viewModel.submitFirstTimeAdminSetup(fullName, rank, gudep, institutionName, photoPath, pin)
                            },
                            onVerifyKeywordAccess = { nameInput, rankInput, pinInput ->
                                viewModel.verifyAdminKeywordAccess(nameInput, rankInput, pinInput)
                            },
                            onClearError = { viewModel.clearAdminLockError() },
                            onBack = { viewModel.navigateBack() }
                        )
                    }
                    AppScreen.ADMIN_DASHBOARD -> {
                        AdminDashboardScreen(
                            institutionConfig = institutionConfig,
                            adminConfig = adminConfig,
                            members = members,
                            materials = materials,
                            syllabi = syllabi,
                            activities = activities,
                            announcements = announcements,
                            adminActionFeedback = adminActionFeedback,
                            onSyncToFirebase = { viewModel.syncNowToFirebase() },
                            onUpdateInstitution = { name, gudep, logoPath ->
                                viewModel.updateInstitutionConfig(name, gudep, logoPath)
                            },
                            onAddMaterial = { title, category, summary, content, details ->
                                viewModel.addMaterial(title, category, summary, content, details)
                            },
                            onUpdateMaterial = { id, title, category, summary, content, details ->
                                viewModel.updateMaterial(id, title, category, summary, content, details)
                            },
                            onAddSyllabus = { semester, rank, programTitle, weekRange, competencyTarget, method, status ->
                                viewModel.addSyllabus(semester, rank, programTitle, weekRange, competencyTarget, method, status)
                            },
                            onUpdateSyllabus = { id, semester, rank, programTitle, weekRange, competencyTarget, method, status ->
                                viewModel.updateSyllabus(id, semester, rank, programTitle, weekRange, competencyTarget, method, status)
                            },
                            onAddActivity = { title, category, date, time, location, leader, quota, desc ->
                                viewModel.addActivity(title, category, date, time, location, leader, quota, desc)
                            },
                            onAddAnnouncement = { title, content, priority ->
                                viewModel.addAnnouncement(title, content, priority)
                            },
                            onAddMember = { name, school, gudep, rank, skuLevel, phone ->
                                viewModel.addMember(name, school, gudep, rank, skuLevel, phone)
                            },
                            onToggleMemberVerified = { memberId ->
                                viewModel.toggleMemberVerified(memberId)
                            },
                            onUpdateAdminPin = { newPin ->
                                viewModel.updateAdminPin(newPin)
                            },
                            onClearAdminFeedback = { viewModel.clearAdminFeedback() },
                            onLockAdminAccess = { viewModel.lockAdminAccess() },
                            onBack = { viewModel.navigateBack() }
                        )
                    }
                    AppScreen.PROFIL -> {
                        ProfileScreen(
                            profile = profile,
                            userEmail = userEmail,
                            institutionConfig = institutionConfig,
                            adminConfig = adminConfig,
                            isProUnlocked = isProUnlocked,
                            adminAccess = adminAccess,
                            syncState = syncState,
                            adminVerificationState = adminVerificationState,
                            adminRecords = adminRecords,
                            currentFirebaseUid = currentFirebaseUid,
                            adminActionFeedback = adminActionFeedback,
                            adminLockError = adminLockError,
                            onNavigateToDashboard = { viewModel.navigateTo(AppScreen.HOME) },
                            onCreateNewAccount = { name, email, school, gudep, rank, phone ->
                                viewModel.createNewAccountByPembina(name, email, school, gudep, rank, phone)
                            },
                            onSyncToFirebase = { viewModel.syncNowToFirebase() },
                            onFirebaseGoogleLinked = { user ->
                                viewModel.loginWithFirebaseUser(user, stayOnCurrentScreen = true)
                                viewModel.syncNowToFirebase()
                            },
                            onVerifyFirebaseAdmin = {
                                viewModel.verifyFirebaseAdminAccess(navigateOnSuccess = false)
                            },
                            onFirebaseAdminGoogleSignIn = { user ->
                                viewModel.loginWithFirebaseUser(
                                    user = user,
                                    stayOnCurrentScreen = true,
                                    openAdminOnVerified = true
                                )
                            },
                            onGrantAdminRole = { targetUid, targetEmail ->
                                viewModel.grantAdminRole(targetUid, targetEmail)
                            },
                            onClearAdminFeedback = { viewModel.clearAdminFeedback() },
                            onOpenAdminSection = { viewModel.openAdminSection() },
                            onUpdateProfile = { updated -> viewModel.updateProfile(updated) },
                            onLogout = {
                                viewModel.prepareForSignOut()
                                signOutFirebaseAndCredentials(
                                    credentialManager = credentialManager,
                                    scope = scope,
                                    onSignOutComplete = { viewModel.logout() }
                                )
                            },
                            onBack = { viewModel.navigateBackFromProfile() }
                        )
                    }
                }
            }
        }
    }
}
