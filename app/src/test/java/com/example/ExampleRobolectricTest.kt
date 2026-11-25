package com.example

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.test.core.app.ApplicationProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import com.example.ads.AdMobManager
import com.example.ads.RewardedAdState
import com.example.data.ScoutPreferences
import com.example.model.AdminConfigProfile
import com.example.model.AdminRole
import com.example.model.AdminVerificationState
import com.example.model.ScoutMaterial
import com.example.model.ScoutProfile
import com.example.model.ScoutRank
import com.example.ui.screens.AdminLockScreen
import com.example.ui.screens.MateriScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.PramukaGarudaTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ScoutViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Pramuka Garuda", appName)
  }

  @Test
  fun `registration and logout persistence rule`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val prefs = ScoutPreferences(app)

    prefs.isRegistered = false
    prefs.isLoggedIn = false
    prefs.adminAccess = false

    val viewModel = ScoutViewModel(app)
    assertFalse(viewModel.isRegistered.value)

    viewModel.submitRegistration(
      name = "Arif Wirawan",
      school = "SMPN 1 Garuda",
      gudep = "01.001 - 01.002",
      rank = ScoutRank.PENGGALANG,
      phone = "08123456789",
      avatarId = 1
    )

    assertTrue(viewModel.isRegistered.value)
    assertTrue(prefs.isRegistered)

    viewModel.logout()
    assertFalse(viewModel.isLoggedIn.value)
    assertTrue(viewModel.isRegistered.value)
    assertTrue(prefs.isRegistered)
  }

  @Test
  fun `admin dashboard restricted to Firebase Auth authorized personnel and gated by Pro options plus first setup and keywords`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val prefs = ScoutPreferences(app)
    prefs.adminAccess = true

    val viewModel = ScoutViewModel(app)
    assertFalse(viewModel.adminAccess.value)
    assertFalse(viewModel.isFirebaseAdminAuthorized())

    // 1. Unauthenticated / non-authorized user CANNOT open Admin Lock or Dashboard even if local rank is Pembina/Bina Damping
    viewModel.updateProfile(viewModel.profile.value.copy(rank = ScoutRank.BINA_DAMPING))
    viewModel.openAdminSection()
    assertEquals(AppScreen.PROFIL, viewModel.currentScreen.value)
    assertFalse(viewModel.adminAccess.value)
    assertNotNull(viewModel.adminLockError.value)

    viewModel.navigateTo(AppScreen.ADMIN_DASHBOARD)
    assertEquals(AppScreen.PROFIL, viewModel.currentScreen.value)
    viewModel.navigateTo(AppScreen.ADMIN_LOCK)
    assertEquals(AppScreen.PROFIL, viewModel.currentScreen.value)

    // Denied Firebase user is also strictly blocked
    viewModel.authPresenceOverrideForTest = { true }
    viewModel.setAdminVerificationStateForTest(
      AdminVerificationState.Denied(
        uid = "uid_anggota_1",
        email = "anggota@gmail.com",
        assignedRole = "anggota",
        reason = "Akun anggota tidak memiliki izin admin."
      )
    )
    viewModel.navigateTo(AppScreen.ADMIN_DASHBOARD)
    assertEquals(AppScreen.PROFIL, viewModel.currentScreen.value)
    assertFalse(viewModel.adminAccess.value)

    // 2. Authorized Firebase Auth admin -> allowed to reach ADMIN_LOCK (2-option Pro gate)
    viewModel.setAdminVerificationStateForTest(
      AdminVerificationState.Authorized(
        uid = "uid_admin_arif",
        email = "arifwir6@gmail.com",
        displayName = "Arif Wirawan"
      )
    )
    assertTrue(viewModel.isFirebaseAdminAuthorized())
    viewModel.openAdminSection()
    assertEquals(AppScreen.ADMIN_LOCK, viewModel.currentScreen.value)
    assertFalse(viewModel.isProAccessActive())

    // Option 1: Unlock with ad -> Pro becomes active for session
    viewModel.unlockProWithAd()
    assertTrue(viewModel.isProAccessActive())
    assertFalse(viewModel.adminConfig.value.isInitialized)

    // First-time Admin onboarding setup
    viewModel.submitFirstTimeAdminSetup(
      fullName = "Kak Budi Santoso",
      rank = ScoutRank.BINA_DAMPING,
      gudep = "07.111 - 07.112",
      institutionName = "SMPN 7 Garuda",
      photoPath = "/data/user/0/com.example/files/admin_photo_1.png",
      pin = "2468"
    )
    assertTrue(viewModel.adminConfig.value.isInitialized)
    assertTrue(viewModel.adminAccess.value)
    assertEquals(AppScreen.ADMIN_DASHBOARD, viewModel.currentScreen.value)
    assertEquals("SMPN 7 Garuda", viewModel.institutionConfig.value.name)
    assertEquals("07.111 - 07.112", viewModel.institutionConfig.value.gudep)

    // Locking Admin returns to PROFIL (Fitur Akun) and resets Ad-based Pro unlock
    viewModel.lockAdminAccess()
    assertFalse(viewModel.adminAccess.value)
    assertFalse(viewModel.isProAccessActive())
    assertEquals(AppScreen.PROFIL, viewModel.currentScreen.value)

    // Option 2: Subscribe Pro Rp 20.000 / 1 Semester -> persists even after lock
    viewModel.unlockProWithPayment()
    assertTrue(viewModel.isProAccessActive())
    assertTrue(prefs.getAdminConfig().isProSemesterPaid)

    // Wrong keyword or PIN must be rejected
    assertFalse(viewModel.verifyAdminKeywordAccess("Salah Nama", ScoutRank.BINA_DAMPING, "2468"))
    assertNotNull(viewModel.adminLockError.value)
    assertFalse(viewModel.verifyAdminKeywordAccess("Kak Budi Santoso", ScoutRank.PEMBINA, "2468"))
    assertFalse(viewModel.verifyAdminKeywordAccess("Kak Budi Santoso", ScoutRank.BINA_DAMPING, "0000"))

    // Exact match (case-insensitive name + rank + PIN) unlocks ADMIN_DASHBOARD
    assertTrue(viewModel.verifyAdminKeywordAccess("kak budi santoso", ScoutRank.BINA_DAMPING, "2468"))
    assertTrue(viewModel.adminAccess.value)
    assertEquals(AppScreen.ADMIN_DASHBOARD, viewModel.currentScreen.value)
  }

  @Test
  fun `admin can update institution logo gudep edit material and manage semester syllabus`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = ScoutViewModel(app)

    // Update Institution name, Gudep & Logo
    val logoPath = "/data/user/0/com.example/files/institution_logo_99.png"
    viewModel.updateInstitutionConfig("MA Negeri 2 Nusantara", "09.201 - 09.202", logoPath)
    assertEquals("MA Negeri 2 Nusantara", viewModel.institutionConfig.value.name)
    assertEquals("09.201 - 09.202", viewModel.institutionConfig.value.gudep)
    assertEquals(logoPath, viewModel.institutionConfig.value.logoPath)

    // Edit existing material
    val targetMat = viewModel.materials.value.first()
    viewModel.updateMaterial(
      id = targetMat.id,
      title = "Materi Sandi & Morse Terbaru",
      category = targetMat.category,
      summary = "Ringkasan diperbarui Pembina",
      content = "Isi materi diperbarui untuk latihan semester ini.",
      details = listOf("Butir 1 Baru", "Butir 2 Baru")
    )
    val updatedMat = viewModel.materials.value.first { it.id == targetMat.id }
    assertEquals("Materi Sandi & Morse Terbaru", updatedMat.title)
    assertEquals(2, updatedMat.details.size)

    // Add & Edit Semester Syllabus
    val initialSyllabusCount = viewModel.syllabi.value.size
    viewModel.addSyllabus(
      semester = "Semester Ganjil 2026/2027",
      rank = ScoutRank.PENGGALANG,
      programTitle = "Latihan Kompas & Peta Pita",
      weekRange = "Minggu 13 - 14",
      competencyTarget = "Peserta dapat membuat peta pita perjalanan 3 km",
      method = "Penjelajahan Regu",
      status = "Terjadwal"
    )
    assertEquals(initialSyllabusCount + 1, viewModel.syllabi.value.size)
    val addedSyl = viewModel.syllabi.value.first()
    assertEquals("Latihan Kompas & Peta Pita", addedSyl.programTitle)

    viewModel.updateSyllabus(
      id = addedSyl.id,
      semester = addedSyl.semester,
      rank = addedSyl.rank,
      programTitle = "Latihan Kompas & Peta Pita Lanjutan",
      weekRange = "Minggu 14 - 15",
      competencyTarget = addedSyl.competencyTarget,
      method = addedSyl.method,
      status = "Berjalan"
    )
    val editedSyl = viewModel.syllabi.value.first { it.id == addedSyl.id }
    assertEquals("Latihan Kompas & Peta Pita Lanjutan", editedSyl.programTitle)
    assertEquals("Berjalan", editedSyl.status)
  }

  @Test
  fun `saved gmail account skips registration form`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val prefs = ScoutPreferences(app)

    prefs.isRegistered = false
    prefs.isLoggedIn = false
    prefs.addSavedGmailAccount("arifwir6@gmail.com")

    val viewModel = ScoutViewModel(app)
    assertTrue(viewModel.isRegistered.value)
    assertTrue(viewModel.savedGmailAccounts.value.contains("arifwir6@gmail.com"))

    viewModel.loginWithSavedGmail("arifwir6@gmail.com")
    assertEquals(AppScreen.HOME, viewModel.currentScreen.value)

    viewModel.logout()
    assertEquals(AppScreen.AUTH, viewModel.currentScreen.value)
    viewModel.login("arifwir6@gmail.com")
    assertEquals(AppScreen.HOME, viewModel.currentScreen.value)
  }

  @Test
  fun `custom profile photo path persists across update and logout`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val prefs = ScoutPreferences(app)
    prefs.isRegistered = false
    prefs.isLoggedIn = false

    val viewModel = ScoutViewModel(app)
    val initialPhotoPath = "/data/user/0/com.example/files/scout_avatar_123.png"
    viewModel.submitRegistration(
      name = "Pandu Garuda",
      school = "SMAN 1 Garuda",
      gudep = "02.011 - 02.012",
      rank = ScoutRank.PENEGAK,
      phone = "0812999888",
      avatarId = 2,
      customPhotoPath = initialPhotoPath
    )

    assertEquals(initialPhotoPath, viewModel.profile.value.customPhotoPath)
    assertEquals(initialPhotoPath, prefs.getProfile().customPhotoPath)

    val updatedPhotoPath = "/data/user/0/com.example/files/scout_avatar_456.png"
    viewModel.updateProfile(viewModel.profile.value.copy(customPhotoPath = updatedPhotoPath))
    assertEquals(updatedPhotoPath, viewModel.profile.value.customPhotoPath)
    assertEquals(updatedPhotoPath, prefs.getProfile().customPhotoPath)

    viewModel.logout()
    assertEquals(updatedPhotoPath, prefs.getProfile().customPhotoPath)
  }

  @Test
  fun `activity registration respects quota and finished status`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = ScoutViewModel(app)

    val initialOpen = viewModel.activities.value.first { it.id == "act_1" }
    assertFalse(initialOpen.isUserRegistered)
    viewModel.toggleRegisterActivity("act_1")
    val afterReg = viewModel.activities.value.first { it.id == "act_1" }
    assertTrue(afterReg.isUserRegistered)
    assertEquals(initialOpen.registeredCount + 1, afterReg.registeredCount)

    viewModel.toggleRegisterActivity("act_3")
    val fullAct = viewModel.activities.value.first { it.id == "act_3" }
    assertFalse(fullAct.isUserRegistered)
    assertEquals(fullAct.quota, fullAct.registeredCount)

    viewModel.toggleRegisterActivity("act_4")
    val finishedAct = viewModel.activities.value.first { it.id == "act_4" }
    assertFalse(finishedAct.isUserRegistered)
  }

  @Test
  fun `startup flow routes first launch to Auth-Registration-Home and active session to Auto-Login Profile`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val prefs = ScoutPreferences(app)
    prefs.isRegistered = false
    prefs.isLoggedIn = false
    prefs.adminAccess = false

    // 1. Pembukaan Pertama: Splash -> Auth -> Registration -> Beranda (HOME)
    val firstVm = ScoutViewModel(app)
    firstVm.onSplashFinished()
    assertEquals(AppScreen.AUTH, firstVm.currentScreen.value)

    firstVm.login("pandu.baru@gmail.com")
    assertEquals(AppScreen.REGISTRATION, firstVm.currentScreen.value)

    firstVm.submitRegistration(
      name = "Kak Pembina Agung",
      school = "SMPN 1 Garuda",
      gudep = "01.001 - 01.002",
      rank = ScoutRank.PEMBINA,
      phone = "08123456789",
      avatarId = 1
    )
    assertEquals(AppScreen.HOME, firstVm.currentScreen.value)
    assertTrue(firstVm.isLoggedIn.value)
    assertTrue(firstVm.isRegistered.value)

    // 2. Pembukaan Ke-2 / Setelahnya (sesi login masih aktif): Splash -> Auto-Login -> Halaman Profil
    val secondVm = ScoutViewModel(app)
    secondVm.onSplashFinished()
    assertEquals(AppScreen.PROFIL, secondVm.currentScreen.value)

    // Pembina can create new member account
    val beforeCount = secondVm.members.value.size
    secondVm.createNewAccountByPembina(
      name = "Rizky Pratama",
      email = "rizky@gmail.com",
      school = "SMPN 1 Garuda",
      gudep = "01.001 - 01.002",
      rank = ScoutRank.PENGGALANG,
      phone = "081355556666"
    )
    assertEquals(beforeCount + 1, secondVm.members.value.size)
    assertEquals("Rizky Pratama", secondVm.members.value.first().name)
  }

  @Test
  fun `profile screen shows Buat Akun Baru only for Pembina while Dashboard and Logout always show`() {
    // Case 1: Pembina sees Avatar, Greeting + Name + Tingkatan, Masuk ke Dashboard, Buat Akun Baru, and Logout
    composeTestRule.setContent {
      PramukaGarudaTheme {
        ProfileScreen(
          profile = ScoutProfile(
            name = "Kakak Pembina Utama",
            rank = ScoutRank.PEMBINA
          ),
          onUpdateProfile = {},
          onLogout = {},
          onBack = {}
        )
      }
    }
    composeTestRule.onNodeWithTag("profile_landing_avatar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_landing_greeting").assertIsDisplayed()
    composeTestRule.onNodeWithText("Masuk ke Dashboard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_create_new_account").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_logout").assertIsDisplayed()
  }

  @Test
  fun `profile screen hides Buat Akun Baru for non-Pembina while Logout stays visible`() {
    composeTestRule.setContent {
      PramukaGarudaTheme {
        ProfileScreen(
          profile = ScoutProfile(
            name = "Adik Penggalang",
            rank = ScoutRank.PENGGALANG
          ),
          onUpdateProfile = {},
          onLogout = {},
          onBack = {}
        )
      }
    }
    composeTestRule.onNodeWithTag("profile_landing_avatar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_landing_greeting").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_enter_dashboard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_create_new_account").assertDoesNotExist()
    composeTestRule.onNodeWithTag("btn_logout").assertIsDisplayed()
  }

  @Test
  fun `admob manager resolves configured ids and admin lock screen triggers rewarded ad playback when ready`() {
    val manager = AdMobManager()
    assertTrue(manager.appId.startsWith("ca-app-pub-"))
    assertTrue(manager.rewardedAdUnitId.startsWith("ca-app-pub-"))
    assertTrue(manager.bannerAdUnitId.startsWith("ca-app-pub-"))

    var watchTriggered = false
    composeTestRule.setContent {
      PramukaGarudaTheme {
        AdminLockScreen(
          profile = ScoutProfile(name = "Kak Pembina", rank = ScoutRank.PEMBINA),
          isProUnlocked = false,
          adminConfig = AdminConfigProfile(),
          lockError = null,
          rewardedAdState = RewardedAdState.Ready,
          isUsingTestAdIds = manager.isUsingTestIds,
          onWatchRewardedAd = { watchTriggered = true },
          onRetryLoadAd = {},
          onUnlockWithPayment = {},
          onSubmitFirstSetup = { _, _, _, _, _, _ -> },
          onVerifyKeywordAccess = { _, _, _ -> },
          onClearError = {},
          onBack = {}
        )
      }
    }

    composeTestRule.onNodeWithTag("pro_option_ad_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_unlock_ad_pro").performClick()
    assertTrue(watchTriggered)
  }

  @Test
  fun `admob manifest metadata is resolved and loading failed early-close states work smoothly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appInfo = context.packageManager.getApplicationInfo(
      context.packageName,
      PackageManager.GET_META_DATA
    )
    val manifestAppId = appInfo.metaData?.getString("com.google.android.gms.ads.APPLICATION_ID")
    assertNotNull("AdMob APPLICATION_ID meta-data must exist in merged manifest", manifestAppId)
    assertTrue(
      "AdMob APPLICATION_ID must be resolved (got $manifestAppId)",
      manifestAppId!!.startsWith("ca-app-pub-") && !manifestAppId.contains("$")
    )

    val pkgInfo = context.packageManager.getPackageInfo(
      context.packageName,
      PackageManager.GET_PERMISSIONS
    )
    val requestedPermissions = pkgInfo.requestedPermissions?.toSet() ?: emptySet()
    assertTrue(requestedPermissions.contains("android.permission.INTERNET"))
    assertFalse(requestedPermissions.contains("android.permission.ACCESS_ADSERVICES_ATTRIBUTION"))
    assertFalse(requestedPermissions.contains("android.permission.ACCESS_ADSERVICES_TOPICS"))
    assertFalse(requestedPermissions.contains("android.permission.ACCESS_ADSERVICES_AD_ID"))

    val manager = AdMobManager()
    manager.initialize(context)

    var adState by mutableStateOf<RewardedAdState>(RewardedAdState.Loading)
    var notice by mutableStateOf<String?>(null)
    var retryCount = 0

    composeTestRule.setContent {
      PramukaGarudaTheme {
        AdminLockScreen(
          profile = ScoutProfile(name = "Kak Pembina", rank = ScoutRank.PEMBINA),
          isProUnlocked = false,
          adminConfig = AdminConfigProfile(),
          lockError = null,
          rewardedAdState = adState,
          isUsingTestAdIds = true,
          adNotice = notice,
          onWatchRewardedAd = {},
          onRetryLoadAd = { retryCount++ },
          onUnlockWithPayment = {},
          onSubmitFirstSetup = { _, _, _, _, _, _ -> },
          onVerifyKeywordAccess = { _, _, _ -> },
          onClearError = {},
          onBack = {}
        )
      }
    }

    // 1. Loading state disables CTA button cleanly
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("btn_unlock_ad_pro").assertIsNotEnabled()
    composeTestRule.onNodeWithText("Memuat Iklan AdMob...").assertIsDisplayed()

    // 2. Failed state shows error notice & allows retry
    adState = RewardedAdState.Failed("Koneksi terputus")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("admob_status_notice").assertIsDisplayed()
    composeTestRule.onNodeWithText("Koneksi terputus").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_unlock_ad_pro").assertIsEnabled().performClick()
    assertEquals(1, retryCount)

    // 3. Early close notice renders when user dismisses ad before reward
    adState = RewardedAdState.Ready
    notice = "Iklan ditutup sebelum selesai."
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("admob_status_notice").assertIsDisplayed()
    composeTestRule.onNodeWithText("Tonton Iklan AdMob & Buka Pro").assertIsDisplayed()
  }

  @Test
  fun `admob banner syncs with pro status on home screen`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val prefs = ScoutPreferences(app)
    prefs.isRegistered = false
    prefs.isLoggedIn = false
    val viewModel = ScoutViewModel(app)
    viewModel.login("anggota@gmail.com")
    viewModel.submitRegistration(
      name = "Kakak Pandu",
      school = "SMPN 1 Garuda",
      gudep = "01.001 - 01.002",
      rank = ScoutRank.PEMBINA,
      phone = "08123456789",
      avatarId = 1
    )
    assertEquals(AppScreen.HOME, viewModel.currentScreen.value)

    val adMobManager = AdMobManager()
    composeTestRule.setContent {
      PramukaGarudaTheme {
        PramukaGarudaApp(viewModel = viewModel, adMobManager = adMobManager)
      }
    }

    // Non-Pro user on HOME has AdMob banner mounted in bottomBar
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("admob_banner_bar").assertExists()

    // Unlock Pro via AdMob reward -> Banner automatically hides
    viewModel.unlockProWithAd()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("admob_banner_bar").assertDoesNotExist()

    // Lock Ad Pro back -> Banner returns on HOME
    viewModel.lockAdminAccess()
    viewModel.navigateTo(AppScreen.HOME)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("admob_banner_bar").assertExists()
  }

  @Test
  fun `profile stats materials syllabi and activity registration persist across viewmodel restart`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val firstVm = ScoutViewModel(app)

    // Update profile stats (skuProgress, tkkCount, isGaruda)
    firstVm.updateProfile(
      firstVm.profile.value.copy(
        skuProgress = 92,
        tkkCount = 14,
        isGaruda = false
      )
    )

    // Edit first material
    val targetMaterial = firstVm.materials.value.first()
    firstVm.updateMaterial(
      id = targetMaterial.id,
      title = "Materi Persisten Teruji",
      category = "Navigasi Darat",
      summary = "Ringkasan persisten",
      content = "Konten lengkap persisten",
      details = listOf("Poin A", "Poin B")
    )

    // Add syllabus item
    firstVm.addSyllabus(
      semester = "Semester Genap 2026/2027",
      rank = ScoutRank.PENGGALANG,
      programTitle = "Bivak & Survival Malam",
      weekRange = "Minggu 5 - 6",
      competencyTarget = "Mampu mendirikan bivak darurat",
      method = "Praktik Lapangan",
      status = "Terjadwal"
    )

    // Register for act_1
    firstVm.toggleRegisterActivity("act_1")

    // Re-create ViewModel simulating fresh process restart from SharedPreferences
    val restartedVm = ScoutViewModel(app)
    assertEquals(92, restartedVm.profile.value.skuProgress)
    assertEquals(14, restartedVm.profile.value.tkkCount)
    assertFalse(restartedVm.profile.value.isGaruda)

    val restoredMaterial = restartedVm.materials.value.first { it.id == targetMaterial.id }
    assertEquals("Materi Persisten Teruji", restoredMaterial.title)
    assertEquals("Navigasi Darat", restoredMaterial.category)

    assertTrue(restartedVm.syllabi.value.any { it.programTitle == "Bivak & Survival Malam" })
    assertTrue(restartedVm.activities.value.first { it.id == "act_1" }.isUserRegistered)
  }

  @Test
  fun `canNavigateBack and profile back navigation behave properly across root and secondary screens`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val prefs = ScoutPreferences(app)
    prefs.isRegistered = false
    prefs.isLoggedIn = false
    prefs.adminAccess = false

    val viewModel = ScoutViewModel(app)
    viewModel.onSplashFinished()
    assertEquals(AppScreen.AUTH, viewModel.currentScreen.value)
    assertFalse(viewModel.canNavigateBack.value)

    // AUTH -> REGISTRATION allows back to AUTH
    viewModel.login("calon.anggota@gmail.com")
    assertEquals(AppScreen.REGISTRATION, viewModel.currentScreen.value)
    assertTrue(viewModel.canNavigateBack.value)
    viewModel.navigateBack()
    assertEquals(AppScreen.AUTH, viewModel.currentScreen.value)

    // Complete registration -> HOME has empty backStack so canNavigateBack is false
    viewModel.login("calon.anggota@gmail.com")
    viewModel.submitRegistration(
      name = "Bima Pratama",
      school = "SMPN 2 Garuda",
      gudep = "03.001 - 03.002",
      rank = ScoutRank.PEMBINA,
      phone = "0812333444",
      avatarId = 1
    )
    assertEquals(AppScreen.HOME, viewModel.currentScreen.value)
    assertFalse(viewModel.canNavigateBack.value)

    // Sub-screen enables canNavigateBack
    viewModel.navigateTo(AppScreen.TINGKATAN)
    assertTrue(viewModel.canNavigateBack.value)
    viewModel.navigateBack()
    assertEquals(AppScreen.HOME, viewModel.currentScreen.value)
    assertFalse(viewModel.canNavigateBack.value)

    // Auto-login landing on PROFIL: hardware back is disabled (canNavigateBack == false),
    // while TopBar back arrow (navigateBackFromProfile) navigates cleanly to HOME.
    val returningVm = ScoutViewModel(app)
    returningVm.onSplashFinished()
    assertEquals(AppScreen.PROFIL, returningVm.currentScreen.value)
    assertFalse(returningVm.canNavigateBack.value)

    returningVm.navigateBackFromProfile()
    assertEquals(AppScreen.HOME, returningVm.currentScreen.value)
  }

  @Test
  fun `materi screen dynamically renders custom category filter chip added by admin`() {
    val customCategory = "Navigasi Hutan"
    val customMaterial = ScoutMaterial(
      id = "mat_custom_nav",
      title = "Membaca Peta Topografi",
      category = customCategory,
      summary = "Pengenalan kontur peta",
      content = "Panduan membaca garis kontur dan azimuth kompas.",
      details = listOf("Menentukan koordinat 6 angka")
    )

    composeTestRule.setContent {
      PramukaGarudaTheme {
        MateriScreen(
          materials = listOf(customMaterial),
          onBack = {}
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("materi_category_row")
      .performScrollToNode(hasTestTag("filter_materi_$customCategory"))
    composeTestRule.onNodeWithTag("filter_materi_$customCategory")
      .assertIsDisplayed()
      .performClick()
    composeTestRule.onNodeWithTag("materi_catalog_list")
      .performScrollToNode(hasText("Membaca Peta Topografi"))
    composeTestRule.onNodeWithText("Membaca Peta Topografi").assertIsDisplayed()
  }

  @Test
  fun `profile screen renders Firebase Auth admin states from unauthenticated to denied to authorized`() {
    var verificationState by mutableStateOf<AdminVerificationState>(AdminVerificationState.Unauthenticated)
    var openAdminClicks = 0
    var recheckClicks = 0
    var grantedPair: Pair<String, String>? = null

    composeTestRule.setContent {
      PramukaGarudaTheme {
        ProfileScreen(
          profile = ScoutProfile(name = "Arif Wirawan", rank = ScoutRank.PEMBINA),
          userEmail = "arifwir6@gmail.com",
          adminVerificationState = verificationState,
          adminRecords = listOf(
            AdminRole(
              id = "uid_admin_arif",
              adminUid = "uid_admin_arif",
              email = "arifwir6@gmail.com",
              role = "admin",
              active = true
            )
          ),
          currentFirebaseUid = "uid_admin_arif",
          onVerifyFirebaseAdmin = { recheckClicks++ },
          onGrantAdminRole = { uid, email -> grantedPair = uid to email },
          onOpenAdminSection = { openAdminClicks++ },
          onUpdateProfile = {},
          onLogout = {},
          onBack = {}
        )
      }
    }

    // 1. Unauthenticated state shows Firebase Auth requirement and Google sign-in button
    composeTestRule.onNodeWithTag("profile_scroll_list")
      .performScrollToNode(hasTestTag("account_admin_menu_card"))
    composeTestRule.onNodeWithText("BUTUH FIREBASE AUTH").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_admin_google_auth").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_open_admin_dashboard").assertDoesNotExist()

    // 2. Denied state shows denial banner, recheck & switch buttons, and hides Dashboard launch button
    verificationState = AdminVerificationState.Denied(
      uid = "uid_anggota_1",
      email = "anggota@gmail.com",
      assignedRole = "anggota",
      reason = "Akun anggota@gmail.com tidak terdaftar sebagai admin."
    )
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("profile_scroll_list")
      .performScrollToNode(hasTestTag("account_admin_menu_card"))
    composeTestRule.onNodeWithText("AKSES DITOLAK").assertIsDisplayed()
    composeTestRule.onNodeWithTag("admin_auth_error_banner", useUnmergedTree = true)
      .performScrollTo()
      .assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_recheck_admin_auth").performScrollTo().assertIsDisplayed().performClick()
    assertEquals(1, recheckClicks)
    composeTestRule.onNodeWithTag("btn_switch_admin_google").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_open_admin_dashboard").assertDoesNotExist()

    // 3. Authorized state unlocks Dashboard Admin & Manage Admin Personnel modal
    verificationState = AdminVerificationState.Authorized(
      uid = "uid_admin_arif",
      email = "arifwir6@gmail.com",
      displayName = "Arif Wirawan"
    )
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("profile_scroll_list")
      .performScrollToNode(hasTestTag("account_admin_menu_card"))
    composeTestRule.onNodeWithText("ADMIN TERVERIFIKASI").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_open_admin_dashboard").performScrollTo().assertIsDisplayed().performClick()
    assertEquals(1, openAdminClicks)

    composeTestRule.onNodeWithTag("btn_manage_admin_personnel").performScrollTo().assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("dialog_manage_admins").assertIsDisplayed()
    composeTestRule.onNodeWithTag("input_grant_admin_uid").performTextInput("uid_pembina_2")
    composeTestRule.onNodeWithTag("input_grant_admin_email").performTextInput("bina.damping@gmail.com")
    composeTestRule.onNodeWithTag("btn_submit_grant_admin").performClick()
    assertEquals("uid_pembina_2" to "bina.damping@gmail.com", grantedPair)
  }

  @Test
  fun `admin lock screen blocks Pro and keyword gates when Firebase Auth state is not Authorized`() {
    composeTestRule.setContent {
      PramukaGarudaTheme {
        AdminLockScreen(
          profile = ScoutProfile(name = "Anggota", rank = ScoutRank.PEMBINA),
          isProUnlocked = false,
          adminConfig = AdminConfigProfile(),
          lockError = null,
          adminVerificationState = AdminVerificationState.Denied(
            uid = "uid_anggota_1",
            email = "anggota@gmail.com",
            assignedRole = "anggota",
            reason = "Hanya personel admin Firebase Auth yang diizinkan."
          ),
          onUnlockWithPayment = {},
          onSubmitFirstSetup = { _, _, _, _, _, _ -> },
          onVerifyKeywordAccess = { _, _, _ -> },
          onClearError = {},
          onBack = {}
        )
      }
    }

    composeTestRule.onNodeWithTag("admin_lock_firebase_required_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("pro_option_ad_card").assertDoesNotExist()
    composeTestRule.onNodeWithTag("admin_keyword_gate_card").assertDoesNotExist()
  }
}
