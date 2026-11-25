package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AdminAuthRepository
import com.example.data.ScoutFirestoreRepository
import com.example.data.ScoutPreferences
import com.example.data.ScoutSampleData
import com.example.model.AdminConfigProfile
import com.example.model.AdminRole
import com.example.model.AdminVerificationState
import com.example.model.InstitutionConfig
import com.example.model.ScoutActivity
import com.example.model.ScoutAnnouncement
import com.example.model.ScoutMaterial
import com.example.model.ScoutMember
import com.example.model.ScoutProfile
import com.example.model.ScoutRank
import com.example.model.SemesterSyllabus
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

enum class AppScreen {
    SPLASH,
    AUTH,
    REGISTRATION, // Only shown once if isRegistered == false
    HOME,
    TINGKATAN,
    KEGIATAN,
    MATERI,
    PUSAT_BANTUAN,
    ADMIN_LOCK,
    ADMIN_DASHBOARD,
    PROFIL
}

sealed interface FirebaseSyncState {
    data object LocalOnly : FirebaseSyncState
    data object Syncing : FirebaseSyncState
    data class Synced(val message: String) : FirebaseSyncState
    data class Error(val message: String) : FirebaseSyncState
}

class ScoutViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = ScoutPreferences(application)
    private var cachedAdminRepo: AdminAuthRepository? = null
    private var cachedFirestoreRepo: ScoutFirestoreRepository? = null
    private val syncJobs = mutableListOf<Job>()
    private var membersSyncJob: Job? = null
    private var activeSyncUid: String? = null
    private var latestRemoteRegistrations: Map<String, Boolean> = emptyMap()

    private val _currentScreen = MutableStateFlow(AppScreen.SPLASH)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _canNavigateBack = MutableStateFlow(false)
    val canNavigateBack: StateFlow<Boolean> = _canNavigateBack.asStateFlow()

    private val _isRegistered = MutableStateFlow(prefs.isRegistered || prefs.hasSavedGmailAccount())
    val isRegistered: StateFlow<Boolean> = _isRegistered.asStateFlow()

    private val _adminAccess = MutableStateFlow(false)
    val adminAccess: StateFlow<Boolean> = _adminAccess.asStateFlow()

    private val _adminVerificationState = MutableStateFlow<AdminVerificationState>(
        AdminVerificationState.Unauthenticated
    )
    val adminVerificationState: StateFlow<AdminVerificationState> = _adminVerificationState.asStateFlow()

    private val _syncState = MutableStateFlow<FirebaseSyncState>(FirebaseSyncState.LocalOnly)
    val syncState: StateFlow<FirebaseSyncState> = _syncState.asStateFlow()

    private val _adminRecords = MutableStateFlow<List<AdminRole>>(emptyList())
    val adminRecords: StateFlow<List<AdminRole>> = _adminRecords.asStateFlow()

    private val _adminActionFeedback = MutableStateFlow<String?>(null)
    val adminActionFeedback: StateFlow<String?> = _adminActionFeedback.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(prefs.isLoggedIn)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userEmail = MutableStateFlow(prefs.userEmail)
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _savedGmailAccounts = MutableStateFlow(prefs.getSavedGmailAccounts())
    val savedGmailAccounts: StateFlow<List<String>> = _savedGmailAccounts.asStateFlow()

    private val _profile = MutableStateFlow(prefs.getProfile())
    val profile: StateFlow<ScoutProfile> = _profile.asStateFlow()

    private val _institutionConfig = MutableStateFlow(prefs.getInstitutionConfig())
    val institutionConfig: StateFlow<InstitutionConfig> = _institutionConfig.asStateFlow()

    private val _adminConfig = MutableStateFlow(prefs.getAdminConfig())
    val adminConfig: StateFlow<AdminConfigProfile> = _adminConfig.asStateFlow()

    private val _isAdProUnlocked = MutableStateFlow(false)
    val isAdProUnlocked: StateFlow<Boolean> = _isAdProUnlocked.asStateFlow()

    private val _adminLockError = MutableStateFlow<String?>(null)
    val adminLockError: StateFlow<String?> = _adminLockError.asStateFlow()

    private val _materials = MutableStateFlow(prefs.getMaterials(ScoutSampleData.materials))
    val materials: StateFlow<List<ScoutMaterial>> = _materials.asStateFlow()

    private val _syllabi = MutableStateFlow(prefs.getSyllabi(ScoutSampleData.syllabi))
    val syllabi: StateFlow<List<SemesterSyllabus>> = _syllabi.asStateFlow()

    private val _activities = MutableStateFlow(prefs.getActivities(ScoutSampleData.activities))
    val activities: StateFlow<List<ScoutActivity>> = _activities.asStateFlow()

    private val _announcements = MutableStateFlow(prefs.getAnnouncements(ScoutSampleData.announcements))
    val announcements: StateFlow<List<ScoutAnnouncement>> = _announcements.asStateFlow()

    private val _members = MutableStateFlow(prefs.getMembers(ScoutSampleData.members))
    val members: StateFlow<List<ScoutMember>> = _members.asStateFlow()

    private val backStack = mutableListOf<AppScreen>()

    init {
        if (prefs.hasSavedGmailAccount() && !prefs.isRegistered) {
            prefs.isRegistered = true
            _isRegistered.value = true
        }
        prefs.adminAccess = false
        refreshCanNavigateBack()
    }

    private fun refreshCanNavigateBack() {
        val screen = _currentScreen.value
        _canNavigateBack.value = when {
            screen == AppScreen.SPLASH || screen == AppScreen.AUTH -> false
            backStack.isNotEmpty() -> true
            screen == AppScreen.HOME || screen == AppScreen.PROFIL -> false
            else -> true
        }
    }

    private fun setCurrentScreen(screen: AppScreen) {
        _currentScreen.value = screen
        refreshCanNavigateBack()
    }

    @androidx.annotation.VisibleForTesting
    internal var authPresenceOverrideForTest: (() -> Boolean)? = null

    @androidx.annotation.VisibleForTesting
    internal fun setAdminVerificationStateForTest(state: AdminVerificationState) {
        _adminVerificationState.value = state
    }

    private fun isFirebaseInitialized(): Boolean =
        runCatching { FirebaseApp.getApps(getApplication()).isNotEmpty() }.getOrDefault(false)

    private fun resolveAdminAuthRepository(): AdminAuthRepository? {
        if (!isFirebaseInitialized()) return null
        return cachedAdminRepo ?: runCatching { AdminAuthRepository(getApplication()) }
            .getOrNull()
            ?.also { cachedAdminRepo = it }
    }

    private fun resolveFirestoreRepository(): ScoutFirestoreRepository? {
        if (!isFirebaseInitialized()) return null
        return cachedFirestoreRepo ?: runCatching { ScoutFirestoreRepository(getApplication()) }
            .getOrNull()
            ?.also { cachedFirestoreRepo = it }
    }

    private fun getCurrentFirebaseUserSafely(): FirebaseUser? {
        if (!isFirebaseInitialized()) return null
        return runCatching { Firebase.auth.currentUser }.getOrNull()
    }

    private fun isFirebaseUserSignedIn(): Boolean =
        authPresenceOverrideForTest?.invoke() ?: (getCurrentFirebaseUserSafely() != null)

    fun isFirebaseAdminAuthorized(): Boolean =
        isFirebaseUserSignedIn() && _adminVerificationState.value is AdminVerificationState.Authorized

    private fun buildAdminRestrictionMessage(): String {
        return when (val state = _adminVerificationState.value) {
            is AdminVerificationState.Authorized -> ""
            is AdminVerificationState.Checking ->
                "Sedang memverifikasi otoritas admin di Firebase Auth & Firestore..."
            is AdminVerificationState.Denied -> state.reason
            is AdminVerificationState.Error -> state.message
            AdminVerificationState.Unauthenticated ->
                "Akses Dashboard Admin dibatasi khusus personel berwenang. Masuk dengan Akun Google (Firebase Auth) di Fitur Akun."
        }
    }

    fun navigateTo(screen: AppScreen) {
        val authorizedFirebaseAdmin = isFirebaseAdminAuthorized()
        val target = when {
            (screen == AppScreen.ADMIN_DASHBOARD || screen == AppScreen.ADMIN_LOCK) && !authorizedFirebaseAdmin -> {
                _adminAccess.value = false
                prefs.adminAccess = false
                _adminLockError.value = buildAdminRestrictionMessage()
                AppScreen.PROFIL
            }
            screen == AppScreen.ADMIN_DASHBOARD && !_adminAccess.value -> AppScreen.ADMIN_LOCK
            else -> screen
        }
        if (_currentScreen.value != target) {
            if (_currentScreen.value != AppScreen.SPLASH && _currentScreen.value != AppScreen.AUTH && _currentScreen.value != AppScreen.REGISTRATION) {
                backStack.add(_currentScreen.value)
            }
            setCurrentScreen(target)
        }
    }

    fun navigateBack(): Boolean {
        if (_currentScreen.value == AppScreen.REGISTRATION) {
            backStack.clear()
            prefs.isLoggedIn = false
            _isLoggedIn.value = false
            setCurrentScreen(AppScreen.AUTH)
            return true
        }
        if (backStack.isNotEmpty()) {
            val previous = backStack.removeAt(backStack.size - 1)
            val resolved = if (previous == AppScreen.ADMIN_DASHBOARD && !_adminAccess.value) {
                AppScreen.HOME
            } else {
                previous
            }
            setCurrentScreen(resolved)
            return true
        } else if (_currentScreen.value != AppScreen.HOME && _currentScreen.value != AppScreen.PROFIL && _isLoggedIn.value && _isRegistered.value) {
            setCurrentScreen(AppScreen.HOME)
            return true
        }
        refreshCanNavigateBack()
        return false
    }

    fun navigateBackFromProfile() {
        if (!navigateBack()) {
            backStack.clear()
            setCurrentScreen(AppScreen.HOME)
        }
    }

    fun onSplashFinished() {
        val hasSavedGmail = prefs.hasSavedGmailAccount()
        val alreadyRegistered = _isRegistered.value || hasSavedGmail
        if (alreadyRegistered && !_isRegistered.value) {
            prefs.isRegistered = true
            _isRegistered.value = true
        }

        val firebaseUser = getCurrentFirebaseUserSafely()
        if (firebaseUser != null) {
            val email = (firebaseUser.email ?: "").trim()
            if (email.isNotBlank()) {
                prefs.userEmail = email
                _userEmail.value = email
                if (ScoutPreferences.isGmailAddress(email)) {
                    prefs.addSavedGmailAccount(email)
                    _savedGmailAccounts.value = prefs.getSavedGmailAccounts()
                }
            }
            if (!_isLoggedIn.value) {
                prefs.isLoggedIn = true
                _isLoggedIn.value = true
            }
        }

        if (!_isLoggedIn.value) {
            backStack.clear()
            setCurrentScreen(AppScreen.AUTH)
        } else if (!alreadyRegistered) {
            backStack.clear()
            setCurrentScreen(AppScreen.REGISTRATION)
        } else {
            // Pembukaan Ke-2 / Ke-3 / Setiap Kali Setelahnya (sesi login masih aktif):
            // Splash -> Auto-Login -> Halaman Profil
            _profile.value = prefs.getProfile()
            backStack.clear()
            setCurrentScreen(AppScreen.PROFIL)
            if (firebaseUser != null) {
                verifyFirebaseAdminAccess(navigateOnSuccess = false)
                attachFirestoreSyncIfSignedIn()
            }
        }
    }

    fun login(email: String) {
        val normalizedEmail = email.trim()
        prefs.isLoggedIn = true
        prefs.userEmail = normalizedEmail
        _isLoggedIn.value = true
        _userEmail.value = normalizedEmail

        val hasSavedGmail = prefs.hasSavedGmailAccount() || prefs.isGmailAccountSaved(normalizedEmail)
        val alreadyRegistered = _isRegistered.value || hasSavedGmail

        if (alreadyRegistered) {
            if (!_isRegistered.value) {
                prefs.isRegistered = true
                _isRegistered.value = true
            }
            if (ScoutPreferences.isGmailAddress(normalizedEmail)) {
                prefs.addSavedGmailAccount(normalizedEmail)
                _savedGmailAccounts.value = prefs.getSavedGmailAccounts()
            }
            _profile.value = prefs.getProfile()
            backStack.clear()
            setCurrentScreen(AppScreen.HOME)
        } else {
            backStack.clear()
            setCurrentScreen(AppScreen.REGISTRATION)
        }
    }

    fun loginWithFirebaseUser(
        user: FirebaseUser,
        stayOnCurrentScreen: Boolean = false,
        openAdminOnVerified: Boolean = false
    ) {
        val email = (user.email ?: "").trim().ifBlank { "${user.uid}@firebase.user" }
        if (!stayOnCurrentScreen) {
            login(email)
        } else {
            prefs.isLoggedIn = true
            prefs.userEmail = email
            _isLoggedIn.value = true
            _userEmail.value = email
            if (ScoutPreferences.isGmailAddress(email)) {
                prefs.addSavedGmailAccount(email)
                _savedGmailAccounts.value = prefs.getSavedGmailAccounts()
            }
        }
        verifyFirebaseAdminAccess(navigateOnSuccess = openAdminOnVerified)
        attachFirestoreSyncIfSignedIn()
    }

    fun loginWithSavedGmail(gmail: String) {
        val normalized = gmail.trim().lowercase()
        prefs.addSavedGmailAccount(normalized)
        prefs.isRegistered = true
        _isRegistered.value = true
        _savedGmailAccounts.value = prefs.getSavedGmailAccounts()
        login(normalized)
    }

    fun submitRegistration(
        name: String,
        school: String,
        gudep: String,
        rank: ScoutRank,
        phone: String,
        avatarId: Int,
        customPhotoPath: String = ""
    ) {
        val newProfile = ScoutProfile(
            name = name.ifBlank { "Anggota Pramuka" },
            school = school.ifBlank { "Gugus Depan Pangkalan Garuda" },
            gudep = gudep.ifBlank { "01.001 - 01.002" },
            rank = rank,
            phone = phone.ifBlank { "-" },
            avatarId = avatarId,
            customPhotoPath = customPhotoPath,
            npa = "PG-${(1000..9999).random()}",
            skuProgress = 60,
            tkkCount = 5,
            isGaruda = true
        )
        prefs.saveProfile(newProfile)
        prefs.isLoggedIn = true
        prefs.isRegistered = true
        _isLoggedIn.value = true
        _profile.value = newProfile
        _institutionConfig.value = prefs.getInstitutionConfig()
        _isRegistered.value = true

        if (ScoutPreferences.isGmailAddress(prefs.userEmail)) {
            prefs.addSavedGmailAccount(prefs.userEmail)
            _savedGmailAccounts.value = prefs.getSavedGmailAccounts()
        }

        val newMember = ScoutMember(
            id = "mem_${System.currentTimeMillis()}",
            name = newProfile.name,
            school = newProfile.school,
            gudep = newProfile.gudep,
            rank = newProfile.rank,
            skuLevel = "${newProfile.rank.title} Baru",
            joinDate = "Hari Ini",
            phone = newProfile.phone,
            verified = true
        )
        _members.update { list ->
            (listOf(newMember) + list).also { prefs.saveMembers(it) }
        }

        val firebaseUser = getCurrentFirebaseUserSafely()
        val repo = resolveFirestoreRepository()
        if (firebaseUser != null && repo != null) {
            viewModelScope.launch {
                repo.saveUserProfile(firebaseUser.uid, newProfile)
                if (_adminVerificationState.value is AdminVerificationState.Authorized) {
                    repo.upsertMember(newMember)
                }
            }
            attachFirestoreSyncIfSignedIn()
        }

        backStack.clear()
        setCurrentScreen(AppScreen.HOME)
    }

    fun createNewAccountByPembina(
        name: String,
        email: String,
        school: String,
        gudep: String,
        rank: ScoutRank,
        phone: String
    ) {
        val cleanName = name.trim()
        if (cleanName.isBlank()) return

        val cleanEmail = email.trim().lowercase()
        if (ScoutPreferences.isGmailAddress(cleanEmail)) {
            prefs.addSavedGmailAccount(cleanEmail)
            _savedGmailAccounts.value = prefs.getSavedGmailAccounts()
        }

        val member = ScoutMember(
            id = "mem_${System.currentTimeMillis()}",
            name = cleanName,
            school = school.trim().ifBlank { _institutionConfig.value.name },
            gudep = gudep.trim().ifBlank { _institutionConfig.value.gudep },
            rank = rank,
            skuLevel = "${rank.title} Terdaftar",
            joinDate = "Hari Ini",
            phone = phone.trim().ifBlank { "-" },
            verified = true
        )
        _members.update { list ->
            (listOf(member) + list).also { prefs.saveMembers(it) }
        }
        _adminActionFeedback.value = "Akun anggota baru \"$cleanName\" (${rank.title}) berhasil dibuat."

        val repo = resolveFirestoreRepository()
        if (getCurrentFirebaseUserSafely() != null && repo != null &&
            (_adminAccess.value || _adminVerificationState.value is AdminVerificationState.Authorized)
        ) {
            viewModelScope.launch {
                repo.upsertMember(member)
            }
        }
    }

    fun updateProfile(newProfile: ScoutProfile) {
        prefs.saveProfile(newProfile)
        _profile.value = newProfile

        val firebaseUser = getCurrentFirebaseUserSafely()
        val repo = resolveFirestoreRepository()
        if (firebaseUser != null && repo != null) {
            viewModelScope.launch {
                _syncState.value = FirebaseSyncState.Syncing
                repo.saveUserProfile(firebaseUser.uid, newProfile)
                    .onSuccess {
                        _syncState.value = FirebaseSyncState.Synced("Profil tersinkron ke Firebase")
                    }
                    .onFailure { err ->
                        _syncState.value = FirebaseSyncState.Error(err.localizedMessage ?: "Gagal sinkron profil")
                    }
            }
        }
    }

    fun prepareForSignOut() {
        stopFirestoreSync()
    }

    fun logout() {
        stopFirestoreSync()
        latestRemoteRegistrations = emptyMap()
        prefs.logout()
        _isLoggedIn.value = false
        _adminAccess.value = false
        _isAdProUnlocked.value = false
        _adminLockError.value = null
        prefs.adminAccess = false
        _adminVerificationState.value = AdminVerificationState.Unauthenticated
        _syncState.value = FirebaseSyncState.LocalOnly
        _adminRecords.value = emptyList()
        if (isFirebaseInitialized()) {
            runCatching { Firebase.auth.signOut() }
        }
        backStack.clear()
        setCurrentScreen(AppScreen.AUTH)
    }

    // --- FIREBASE CLOUD SYNC ---
    fun attachFirestoreSyncIfSignedIn() {
        val user = getCurrentFirebaseUserSafely() ?: run {
            _syncState.value = FirebaseSyncState.LocalOnly
            return
        }
        val repo = resolveFirestoreRepository() ?: return
        if (activeSyncUid == user.uid && syncJobs.isNotEmpty()) return

        stopFirestoreSync()
        activeSyncUid = user.uid
        _syncState.value = FirebaseSyncState.Syncing

        syncJobs += viewModelScope.launch {
            repo.observeUserProfile(user.uid)
                .catch { error ->
                    val root = unwrapFirestoreError(error)
                    if (root == null && error is CancellationException) throw error
                    Log.w(TAG, "Profile sync error", root ?: error)
                    _syncState.value = FirebaseSyncState.Error((root ?: error).localizedMessage ?: "Gagal sinkron profil")
                }
                .collect { remoteProfile ->
                    if (remoteProfile != null) {
                        val mergedProfile = remoteProfile.copy(
                            customPhotoPath = resolveUsableImagePath(
                                remotePath = remoteProfile.customPhotoPath,
                                localFallbackPath = _profile.value.customPhotoPath
                            )
                        )
                        prefs.saveProfile(mergedProfile)
                        _profile.value = mergedProfile
                        _isRegistered.value = true
                        _syncState.value = FirebaseSyncState.Synced("Terhubung & tersinkron ke Firebase")
                        if (_currentScreen.value == AppScreen.REGISTRATION) {
                            backStack.clear()
                            setCurrentScreen(AppScreen.HOME)
                        }
                    } else if (_isRegistered.value) {
                        repo.saveUserProfile(user.uid, _profile.value)
                            .onSuccess {
                                _syncState.value = FirebaseSyncState.Synced("Profil awal diunggah ke Firebase")
                            }
                    } else {
                        _syncState.value = FirebaseSyncState.Synced("Akun Firebase aktif")
                    }
                }
        }

        syncJobs += viewModelScope.launch {
            repo.observeActivityRegistrations(user.uid)
                .catch { error ->
                    if (unwrapFirestoreError(error) == null && error is CancellationException) throw error
                    Log.w(TAG, "Activity registrations sync error", error)
                }
                .collect { regMap ->
                    latestRemoteRegistrations = regMap
                    _activities.update { list ->
                        list.map { act ->
                            val registered = regMap[act.id]
                            if (registered != null) act.copy(isUserRegistered = registered) else act
                        }.also { prefs.saveActivities(it) }
                    }
                }
        }

        syncJobs += viewModelScope.launch {
            repo.observeInstitutionConfig()
                .catch { error ->
                    if (unwrapFirestoreError(error) == null && error is CancellationException) throw error
                    Log.w(TAG, "Institution sync error", error)
                }
                .collect { remoteInst ->
                    if (remoteInst != null) {
                        val merged = remoteInst.copy(
                            logoPath = resolveUsableImagePath(
                                remotePath = remoteInst.logoPath,
                                localFallbackPath = _institutionConfig.value.logoPath
                            )
                        )
                        prefs.saveInstitutionConfig(merged)
                        _institutionConfig.value = merged
                    }
                }
        }

        syncJobs += viewModelScope.launch {
            repo.observeMaterials()
                .catch { error ->
                    if (unwrapFirestoreError(error) == null && error is CancellationException) throw error
                    Log.w(TAG, "Materials sync error", error)
                }
                .collect { remoteMaterials ->
                    if (remoteMaterials.isNotEmpty()) {
                        val remoteIds = remoteMaterials.map { it.id }.toSet()
                        val remainingDefaults = _materials.value.filter { it.id !in remoteIds }
                        val merged = remoteMaterials + remainingDefaults
                        _materials.value = merged
                        prefs.saveMaterials(merged)
                    }
                }
        }

        syncJobs += viewModelScope.launch {
            repo.observeSyllabi()
                .catch { error ->
                    if (unwrapFirestoreError(error) == null && error is CancellationException) throw error
                    Log.w(TAG, "Syllabi sync error", error)
                }
                .collect { remoteSyllabi ->
                    if (remoteSyllabi.isNotEmpty()) {
                        val remoteIds = remoteSyllabi.map { it.id }.toSet()
                        val remainingDefaults = _syllabi.value.filter { it.id !in remoteIds }
                        val merged = remoteSyllabi + remainingDefaults
                        _syllabi.value = merged
                        prefs.saveSyllabi(merged)
                    }
                }
        }

        syncJobs += viewModelScope.launch {
            repo.observeActivities()
                .catch { error ->
                    if (unwrapFirestoreError(error) == null && error is CancellationException) throw error
                    Log.w(TAG, "Activities sync error", error)
                }
                .collect { remoteActivities ->
                    if (remoteActivities.isNotEmpty()) {
                        val remoteIds = remoteActivities.map { it.id }.toSet()
                        val mergedRemote = remoteActivities.map { act ->
                            act.copy(isUserRegistered = latestRemoteRegistrations[act.id] ?: act.isUserRegistered)
                        }
                        val remainingDefaults = _activities.value.filter { it.id !in remoteIds }
                        val merged = mergedRemote + remainingDefaults
                        _activities.value = merged
                        prefs.saveActivities(merged)
                    }
                }
        }

        syncJobs += viewModelScope.launch {
            repo.observeAnnouncements()
                .catch { error ->
                    if (unwrapFirestoreError(error) == null && error is CancellationException) throw error
                    Log.w(TAG, "Announcements sync error", error)
                }
                .collect { remoteAnnouncements ->
                    if (remoteAnnouncements.isNotEmpty()) {
                        val remoteIds = remoteAnnouncements.map { it.id }.toSet()
                        val remainingDefaults = _announcements.value.filter { it.id !in remoteIds }
                        val merged = remoteAnnouncements + remainingDefaults
                        _announcements.value = merged
                        prefs.saveAnnouncements(merged)
                    }
                }
        }
    }

    private fun startAdminMembersListenerIfAuthorized() {
        val repo = resolveFirestoreRepository() ?: return
        if (getCurrentFirebaseUserSafely() == null) return
        if (_adminVerificationState.value !is AdminVerificationState.Authorized) return
        if (membersSyncJob?.isActive == true) return

        membersSyncJob = viewModelScope.launch {
            repo.observeMembers()
                .catch { error ->
                    if (unwrapFirestoreError(error) == null && error is CancellationException) throw error
                    Log.w(TAG, "Admin members sync error", error)
                }
                .collect { remoteMembers ->
                    if (remoteMembers.isNotEmpty()) {
                        val remoteIds = remoteMembers.map { it.id }.toSet()
                        val remainingDefaults = _members.value.filter { it.id !in remoteIds }
                        val merged = remoteMembers + remainingDefaults
                        _members.value = merged
                        prefs.saveMembers(merged)
                    }
                }
        }
    }

    private fun stopFirestoreSync() {
        syncJobs.forEach { it.cancel() }
        syncJobs.clear()
        membersSyncJob?.cancel()
        membersSyncJob = null
        activeSyncUid = null
    }

    fun syncNowToFirebase() {
        val user = getCurrentFirebaseUserSafely()
        val repo = resolveFirestoreRepository()
        if (user == null || repo == null) {
            _syncState.value = FirebaseSyncState.Error(
                "Masuk dengan Akun Google (Firebase) untuk menyinkronkan data ke cloud."
            )
            return
        }

        viewModelScope.launch {
            _syncState.value = FirebaseSyncState.Syncing
            val profileResult = repo.saveUserProfile(user.uid, _profile.value)
            if (profileResult.isFailure) {
                val msg = profileResult.exceptionOrNull()?.localizedMessage ?: "Gagal sinkron profil"
                _syncState.value = FirebaseSyncState.Error(msg)
                return@launch
            }

            _activities.value.forEach { act ->
                if (act.isUserRegistered || latestRemoteRegistrations.containsKey(act.id)) {
                    repo.setActivityRegistration(user.uid, act.id, act.isUserRegistered)
                }
            }

            if (_adminVerificationState.value is AdminVerificationState.Unauthenticated ||
                _adminVerificationState.value is AdminVerificationState.Checking
            ) {
                resolveAdminAuthRepository()?.let { adminRepo ->
                    val verifiedState = adminRepo.verifyCurrentUserAdminRole()
                    _adminVerificationState.value = verifiedState
                    if (verifiedState is AdminVerificationState.Authorized) {
                        loadAdminRecordsInternal(adminRepo)
                        startAdminMembersListenerIfAuthorized()
                    }
                }
            }

            if (_adminVerificationState.value is AdminVerificationState.Authorized) {
                repo.syncFullAdminCatalog(
                    institutionConfig = _institutionConfig.value,
                    materials = _materials.value,
                    syllabi = _syllabi.value,
                    activities = _activities.value,
                    announcements = _announcements.value,
                    members = _members.value
                ).onSuccess { count ->
                    _syncState.value = FirebaseSyncState.Synced("Tersinkron penuh ($count dokumen cloud)")
                    _adminActionFeedback.value = "Seluruh data ($count item) berhasil disinkronkan ke Firebase."
                }.onFailure { err ->
                    val msg = err.localizedMessage ?: "Sebagian data admin gagal disinkronkan."
                    _syncState.value = FirebaseSyncState.Error(msg)
                }
            } else {
                _syncState.value = FirebaseSyncState.Synced("Profil & kegiatan anggota tersinkron ke Firebase")
            }
            attachFirestoreSyncIfSignedIn()
        }
    }

    private fun resolveUsableImagePath(remotePath: String, localFallbackPath: String): String {
        val cleanRemote = remotePath.trim()
        val remoteUsable = cleanRemote.isNotBlank() && (
            cleanRemote.startsWith("http://", ignoreCase = true) ||
                cleanRemote.startsWith("https://", ignoreCase = true) ||
                File(cleanRemote).exists()
            )
        if (remoteUsable) return cleanRemote

        val cleanLocal = localFallbackPath.trim()
        val localUsable = cleanLocal.isNotBlank() && (
            cleanLocal.startsWith("http://", ignoreCase = true) ||
                cleanLocal.startsWith("https://", ignoreCase = true) ||
                File(cleanLocal).exists()
            )
        return if (localUsable) cleanLocal else ""
    }

    private fun unwrapFirestoreError(error: Throwable): FirebaseFirestoreException? {
        return generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
    }

    // Admin Access Flow (Accessed via Account / Profile and strictly gated by Firebase Auth)
    fun openAdminSection() {
        _adminLockError.value = null
        if (!isFirebaseAdminAuthorized()) {
            _adminAccess.value = false
            prefs.adminAccess = false
            _adminLockError.value = buildAdminRestrictionMessage()
            if (isFirebaseUserSignedIn()) {
                verifyFirebaseAdminAccess(navigateOnSuccess = true)
            } else {
                _adminVerificationState.value = AdminVerificationState.Unauthenticated
                navigateTo(AppScreen.PROFIL)
            }
            return
        }
        if (_adminAccess.value) {
            navigateTo(AppScreen.ADMIN_DASHBOARD)
        } else {
            _adminAccess.value = false
            prefs.adminAccess = false
            navigateTo(AppScreen.ADMIN_LOCK)
        }
    }

    fun isProAccessActive(): Boolean {
        return _adminConfig.value.isProSemesterPaid || _isAdProUnlocked.value
    }

    fun unlockProWithAd() {
        _isAdProUnlocked.value = true
        _adminLockError.value = null
        _adminActionFeedback.value = "Akses User Dashboard Pro aktif melalui tayangan iklan."
    }

    fun unlockProWithPayment(semesterLabel: String = "Semester Aktif 2026/2027 (Rp 20.000 / 1 Semester)") {
        val updated = _adminConfig.value.copy(
            isProSemesterPaid = true,
            proSemesterLabel = semesterLabel
        )
        prefs.saveAdminConfig(updated)
        _adminConfig.value = updated
        _adminLockError.value = null
        _adminActionFeedback.value = "Langganan Pro 1 Semester (Rp 20.000) berhasil diaktifkan."
    }

    fun submitFirstTimeAdminSetup(
        fullName: String,
        rank: ScoutRank,
        gudep: String,
        institutionName: String,
        photoPath: String,
        pin: String
    ) {
        if (!isFirebaseAdminAuthorized()) {
            _adminAccess.value = false
            prefs.adminAccess = false
            _adminLockError.value = buildAdminRestrictionMessage()
            backStack.removeAll { it == AppScreen.ADMIN_LOCK || it == AppScreen.ADMIN_DASHBOARD }
            setCurrentScreen(AppScreen.PROFIL)
            return
        }

        val cleanName = fullName.trim()
        val cleanGudep = gudep.trim()
        val cleanInstitution = institutionName.trim()
        val cleanPin = pin.trim()

        val updatedAdmin = _adminConfig.value.copy(
            fullName = cleanName,
            rank = rank,
            gudep = cleanGudep,
            institutionName = cleanInstitution,
            photoPath = photoPath,
            pin = cleanPin,
            isInitialized = true
        )
        prefs.saveAdminConfig(updatedAdmin)
        _adminConfig.value = updatedAdmin

        val updatedInst = _institutionConfig.value.copy(
            name = cleanInstitution,
            gudep = cleanGudep
        )
        prefs.saveInstitutionConfig(updatedInst)
        _institutionConfig.value = updatedInst

        _adminAccess.value = true
        prefs.adminAccess = true
        _adminLockError.value = null
        _adminActionFeedback.value = "Profil Admin & PIN Akses berhasil dibuat."
        backStack.removeAll { it == AppScreen.ADMIN_LOCK || it == AppScreen.ADMIN_DASHBOARD }
        setCurrentScreen(AppScreen.ADMIN_DASHBOARD)

        if (getCurrentFirebaseUserSafely() != null &&
            _adminVerificationState.value is AdminVerificationState.Authorized
        ) {
            viewModelScope.launch {
                resolveFirestoreRepository()?.saveInstitutionConfig(updatedInst)
            }
        }
    }

    fun verifyAdminKeywordAccess(
        nameInput: String,
        rankInput: ScoutRank,
        pinInput: String
    ): Boolean {
        if (!isFirebaseAdminAuthorized()) {
            _adminAccess.value = false
            prefs.adminAccess = false
            _adminLockError.value = buildAdminRestrictionMessage()
            return false
        }
        if (!isProAccessActive()) {
            _adminLockError.value = "Pilih opsi buka akses Pro terlebih dahulu (Tonton Iklan atau Bayar Rp 20.000 / 1 Semester)."
            return false
        }
        val saved = _adminConfig.value
        if (!saved.isInitialized) {
            _adminLockError.value = "Silakan lengkapi data diri admin pertama kali terlebih dahulu."
            return false
        }
        val nameMatches = nameInput.trim().equals(saved.fullName.trim(), ignoreCase = true)
        val rankMatches = rankInput == saved.rank
        val pinMatches = pinInput.trim() == saved.pin.trim()

        if (!nameMatches || !rankMatches || !pinMatches) {
            _adminLockError.value = "Kata kunci tidak cocok! Pastikan Nama, Tingkatan Pramuka, dan PIN Akses Dashboard sesuai."
            return false
        }

        _adminAccess.value = true
        prefs.adminAccess = true
        _adminLockError.value = null
        backStack.removeAll { it == AppScreen.ADMIN_LOCK || it == AppScreen.ADMIN_DASHBOARD }
        setCurrentScreen(AppScreen.ADMIN_DASHBOARD)
        return true
    }

    fun clearAdminLockError() {
        _adminLockError.value = null
    }

    fun updateAdminPin(newPin: String) {
        val cleanPin = newPin.trim()
        if (cleanPin.length < 4) {
            _adminActionFeedback.value = "PIN Akses minimal 4 karakter/angka."
            return
        }
        val updated = _adminConfig.value.copy(pin = cleanPin)
        prefs.saveAdminConfig(updated)
        _adminConfig.value = updated
        _adminActionFeedback.value = "PIN Akses Dashboard Admin berhasil diperbarui."
    }

    fun verifyFirebaseAdminAccess(navigateOnSuccess: Boolean = false) {
        val repo = resolveAdminAuthRepository()
        if (repo == null || getCurrentFirebaseUserSafely() == null) {
            _adminVerificationState.value = AdminVerificationState.Unauthenticated
            _adminRecords.value = emptyList()
            _adminAccess.value = false
            prefs.adminAccess = false
            return
        }

        viewModelScope.launch {
            _adminVerificationState.value = AdminVerificationState.Checking
            val result = repo.verifyCurrentUserAdminRole()
            _adminVerificationState.value = result
            when (result) {
                is AdminVerificationState.Authorized -> {
                    _adminLockError.value = null
                    loadAdminRecordsInternal(repo)
                    startAdminMembersListenerIfAuthorized()
                    if (navigateOnSuccess) {
                        val target = if (_adminAccess.value) AppScreen.ADMIN_DASHBOARD else AppScreen.ADMIN_LOCK
                        if (_currentScreen.value != target) {
                            navigateTo(target)
                        }
                    }
                }
                else -> {
                    _adminRecords.value = emptyList()
                    _adminAccess.value = false
                    prefs.adminAccess = false
                    _adminLockError.value = buildAdminRestrictionMessage()
                    if (_currentScreen.value == AppScreen.ADMIN_LOCK || _currentScreen.value == AppScreen.ADMIN_DASHBOARD) {
                        backStack.removeAll { it == AppScreen.ADMIN_LOCK || it == AppScreen.ADMIN_DASHBOARD }
                        setCurrentScreen(AppScreen.PROFIL)
                    }
                }
            }
        }
    }

    fun grantAdminRole(targetUid: String, targetEmail: String) {
        val repo = resolveAdminAuthRepository()
        if (repo == null || !isFirebaseAdminAuthorized()) {
            _adminActionFeedback.value = "Hanya personel admin terverifikasi Firebase Auth yang dapat memberi otoritas admin."
            return
        }

        viewModelScope.launch {
            val result = try {
                repo.grantAdminRole(targetUid, targetEmail)
            } catch (e: IllegalArgumentException) {
                Result.failure(e)
            }
            result.onSuccess {
                loadAdminRecordsInternal(repo)
                _adminActionFeedback.value = "Otoritas admin berhasil diberikan kepada ${targetEmail.trim()}."
            }.onFailure { err ->
                _adminActionFeedback.value = err.localizedMessage ?: "Gagal memberikan otoritas admin."
            }
        }
    }

    fun lockAdminAccess() {
        prefs.adminAccess = false
        _adminAccess.value = false
        _isAdProUnlocked.value = false
        _adminLockError.value = null
        backStack.removeAll { it == AppScreen.ADMIN_LOCK || it == AppScreen.ADMIN_DASHBOARD }
        setCurrentScreen(AppScreen.PROFIL)
    }

    private suspend fun loadAdminRecordsInternal(repo: AdminAuthRepository) {
        repo.getAdminRecords().onSuccess { records ->
            _adminRecords.value = records
        }
    }

    fun clearAdminFeedback() {
        _adminActionFeedback.value = null
    }

    // Admin CRUD Operations (with Firestore Cloud Sync when authorized)
    fun updateInstitutionConfig(name: String, gudep: String, logoPath: String) {
        val updated = InstitutionConfig(
            name = name.trim().ifBlank { "Pangkalan SMP Negeri 1 Garuda" },
            gudep = gudep.trim().ifBlank { "01.001 - 01.002" },
            logoPath = logoPath
        )
        prefs.saveInstitutionConfig(updated)
        _institutionConfig.value = updated

        val syncedAdmin = _adminConfig.value.copy(
            institutionName = updated.name,
            gudep = updated.gudep
        )
        prefs.saveAdminConfig(syncedAdmin)
        _adminConfig.value = syncedAdmin

        val syncedProfile = _profile.value.copy(
            school = updated.name,
            gudep = updated.gudep
        )
        prefs.saveProfile(syncedProfile)
        _profile.value = syncedProfile

        val firebaseUser = getCurrentFirebaseUserSafely()
        if (firebaseUser != null) {
            viewModelScope.launch {
                resolveFirestoreRepository()?.let { repo ->
                    repo.saveUserProfile(firebaseUser.uid, syncedProfile)
                    if (_adminVerificationState.value is AdminVerificationState.Authorized) {
                        repo.saveInstitutionConfig(updated)
                    }
                }
            }
        }

        _adminActionFeedback.value = "Identitas lembaga, nomor Gudep, dan logo aplikasi berhasil diperbarui."
    }

    fun addMaterial(
        title: String,
        category: String,
        summary: String,
        content: String,
        details: List<String> = listOf("Ditambahkan oleh Pembina melalui Dashboard Admin")
    ) {
        val newMat = ScoutMaterial(
            id = "mat_${System.currentTimeMillis()}",
            title = title,
            category = category,
            summary = summary,
            content = content,
            details = details.ifEmpty { listOf("Ditambahkan oleh Pembina melalui Dashboard Admin") },
            iconKey = "book"
        )
        _materials.update { list ->
            (listOf(newMat) + list).also { prefs.saveMaterials(it) }
        }
        if (getCurrentFirebaseUserSafely() != null &&
            _adminVerificationState.value is AdminVerificationState.Authorized
        ) {
            viewModelScope.launch {
                resolveFirestoreRepository()?.upsertMaterial(newMat)
            }
        }
        _adminActionFeedback.value = "Materi baru \"$title\" berhasil diterbitkan."
    }

    fun updateMaterial(
        id: String,
        title: String,
        category: String,
        summary: String,
        content: String,
        details: List<String>
    ) {
        var updatedMaterial: ScoutMaterial? = null
        _materials.update { list ->
            list.map { mat ->
                if (mat.id == id) {
                    mat.copy(
                        title = title,
                        category = category,
                        summary = summary,
                        content = content,
                        details = details
                    ).also { updatedMaterial = it }
                } else mat
            }.also { prefs.saveMaterials(it) }
        }
        val target = updatedMaterial
        if (target != null && getCurrentFirebaseUserSafely() != null &&
            _adminVerificationState.value is AdminVerificationState.Authorized
        ) {
            viewModelScope.launch {
                resolveFirestoreRepository()?.upsertMaterial(target)
            }
        }
        _adminActionFeedback.value = "Materi \"$title\" berhasil diperbarui."
    }

    fun addSyllabus(
        semester: String,
        rank: ScoutRank,
        programTitle: String,
        weekRange: String,
        competencyTarget: String,
        method: String,
        status: String
    ) {
        val newItem = SemesterSyllabus(
            id = "syl_${System.currentTimeMillis()}",
            semester = semester.trim(),
            rank = rank,
            programTitle = programTitle.trim(),
            weekRange = weekRange.trim(),
            competencyTarget = competencyTarget.trim(),
            method = method.trim(),
            status = status
        )
        _syllabi.update { list ->
            (listOf(newItem) + list).also { prefs.saveSyllabi(it) }
        }
        if (getCurrentFirebaseUserSafely() != null &&
            _adminVerificationState.value is AdminVerificationState.Authorized
        ) {
            viewModelScope.launch {
                resolveFirestoreRepository()?.upsertSyllabus(newItem)
            }
        }
        _adminActionFeedback.value = "Program kerja / silabus \"$programTitle\" berhasil ditambahkan."
    }

    fun updateSyllabus(
        id: String,
        semester: String,
        rank: ScoutRank,
        programTitle: String,
        weekRange: String,
        competencyTarget: String,
        method: String,
        status: String
    ) {
        var updatedSyllabus: SemesterSyllabus? = null
        _syllabi.update { list ->
            list.map { item ->
                if (item.id == id) {
                    item.copy(
                        semester = semester.trim(),
                        rank = rank,
                        programTitle = programTitle.trim(),
                        weekRange = weekRange.trim(),
                        competencyTarget = competencyTarget.trim(),
                        method = method.trim(),
                        status = status
                    ).also { updatedSyllabus = it }
                } else item
            }.also { prefs.saveSyllabi(it) }
        }
        val target = updatedSyllabus
        if (target != null && getCurrentFirebaseUserSafely() != null &&
            _adminVerificationState.value is AdminVerificationState.Authorized
        ) {
            viewModelScope.launch {
                resolveFirestoreRepository()?.upsertSyllabus(target)
            }
        }
        _adminActionFeedback.value = "Program kerja / silabus \"$programTitle\" berhasil diperbarui."
    }

    fun addActivity(
        title: String,
        category: String,
        date: String,
        time: String,
        location: String,
        leader: String,
        quota: Int,
        description: String
    ) {
        val newAct = ScoutActivity(
            id = "act_${System.currentTimeMillis()}",
            title = title,
            category = category,
            date = date,
            time = time,
            location = location,
            leader = leader,
            quota = quota,
            registeredCount = 1,
            status = "Akan Datang",
            description = description,
            isUserRegistered = false
        )
        _activities.update { list ->
            (listOf(newAct) + list).also { prefs.saveActivities(it) }
        }
        if (getCurrentFirebaseUserSafely() != null &&
            _adminVerificationState.value is AdminVerificationState.Authorized
        ) {
            viewModelScope.launch {
                resolveFirestoreRepository()?.upsertActivity(newAct)
            }
        }
    }

    fun addAnnouncement(title: String, content: String, priority: String) {
        val newAnn = ScoutAnnouncement(
            id = "ann_${System.currentTimeMillis()}",
            title = title,
            content = content,
            date = "Baru Saja",
            priority = priority,
            author = "Admin Pembina"
        )
        _announcements.update { list ->
            (listOf(newAnn) + list).also { prefs.saveAnnouncements(it) }
        }
        if (getCurrentFirebaseUserSafely() != null &&
            _adminVerificationState.value is AdminVerificationState.Authorized
        ) {
            viewModelScope.launch {
                resolveFirestoreRepository()?.upsertAnnouncement(newAnn)
            }
        }
    }

    fun addMember(
        name: String,
        school: String,
        gudep: String,
        rank: ScoutRank,
        skuLevel: String,
        phone: String
    ) {
        val newMember = ScoutMember(
            id = "mem_${System.currentTimeMillis()}",
            name = name,
            school = school,
            gudep = gudep,
            rank = rank,
            skuLevel = skuLevel,
            joinDate = "Hari Ini",
            phone = phone,
            verified = true
        )
        _members.update { list ->
            (listOf(newMember) + list).also { prefs.saveMembers(it) }
        }
        if (getCurrentFirebaseUserSafely() != null &&
            _adminVerificationState.value is AdminVerificationState.Authorized
        ) {
            viewModelScope.launch {
                resolveFirestoreRepository()?.upsertMember(newMember)
            }
        }
    }

    fun toggleMemberVerified(memberId: String) {
        var updatedMember: ScoutMember? = null
        _members.update { list ->
            list.map {
                if (it.id == memberId) it.copy(verified = !it.verified).also { m -> updatedMember = m } else it
            }.also { prefs.saveMembers(it) }
        }
        val target = updatedMember
        if (target != null && getCurrentFirebaseUserSafely() != null &&
            _adminVerificationState.value is AdminVerificationState.Authorized
        ) {
            viewModelScope.launch {
                resolveFirestoreRepository()?.upsertMember(target)
            }
        }
    }

    fun toggleRegisterActivity(activityId: String) {
        var changedRegistration: Boolean? = null
        _activities.update { list ->
            list.map { act ->
                if (act.id == activityId) {
                    val wasRegistered = act.isUserRegistered
                    if (!wasRegistered && (act.status == "Selesai" || act.registeredCount >= act.quota)) {
                        act
                    } else {
                        val newCount = if (wasRegistered) {
                            (act.registeredCount - 1).coerceAtLeast(0)
                        } else {
                            (act.registeredCount + 1).coerceAtMost(act.quota)
                        }
                        val nextReg = !wasRegistered
                        changedRegistration = nextReg
                        act.copy(isUserRegistered = nextReg, registeredCount = newCount)
                    }
                } else act
            }.also { prefs.saveActivities(it) }
        }
        val regState = changedRegistration
        val firebaseUser = getCurrentFirebaseUserSafely()
        if (regState != null && firebaseUser != null) {
            viewModelScope.launch {
                resolveFirestoreRepository()?.setActivityRegistration(firebaseUser.uid, activityId, regState)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopFirestoreSync()
    }

    private companion object {
        const val TAG = "ScoutViewModel"
    }
}
