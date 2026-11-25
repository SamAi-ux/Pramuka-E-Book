package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AdminConfigProfile
import com.example.model.InstitutionConfig
import com.example.model.ScoutActivity
import com.example.model.ScoutAnnouncement
import com.example.model.ScoutMaterial
import com.example.model.ScoutMember
import com.example.model.ScoutProfile
import com.example.model.ScoutRank
import com.example.model.SemesterSyllabus
import org.json.JSONArray
import org.json.JSONObject

class ScoutPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("pramuka_garuda_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_REGISTERED = "key_is_registered"
        private const val KEY_ADMIN_ACCESS = "key_admin_access"
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_USER_EMAIL = "key_user_email"
        private const val KEY_SAVED_GMAIL_ACCOUNTS = "key_saved_gmail_accounts"

        // Profile keys
        private const val KEY_PROFILE_NAME = "key_profile_name"
        private const val KEY_PROFILE_SCHOOL = "key_profile_school"
        private const val KEY_PROFILE_GUDEP = "key_profile_gudep"
        private const val KEY_PROFILE_RANK = "key_profile_rank"
        private const val KEY_PROFILE_PHONE = "key_profile_phone"
        private const val KEY_PROFILE_AVATAR = "key_profile_avatar"
        private const val KEY_PROFILE_CUSTOM_PHOTO = "key_profile_custom_photo"
        private const val KEY_PROFILE_NPA = "key_profile_npa"
        private const val KEY_PROFILE_SKU_PROGRESS = "key_profile_sku_progress"
        private const val KEY_PROFILE_TKK_COUNT = "key_profile_tkk_count"
        private const val KEY_PROFILE_IS_GARUDA = "key_profile_is_garuda"

        // Local catalog & activity persistence keys
        private const val KEY_MATERIALS_JSON = "key_materials_json"
        private const val KEY_SYLLABI_JSON = "key_syllabi_json"
        private const val KEY_ACTIVITIES_JSON = "key_activities_json"
        private const val KEY_ANNOUNCEMENTS_JSON = "key_announcements_json"
        private const val KEY_MEMBERS_JSON = "key_members_json"

        // Institution keys (editable by Admin)
        private const val KEY_INSTITUTION_NAME = "key_institution_name"
        private const val KEY_INSTITUTION_GUDEP = "key_institution_gudep"
        private const val KEY_INSTITUTION_LOGO = "key_institution_logo"

        // Admin onboarding & PIN & Pro subscription keys
        private const val KEY_ADMIN_INITIALIZED = "key_admin_initialized"
        private const val KEY_ADMIN_FULL_NAME = "key_admin_full_name"
        private const val KEY_ADMIN_RANK = "key_admin_rank"
        private const val KEY_ADMIN_GUDEP = "key_admin_gudep"
        private const val KEY_ADMIN_INSTITUTION = "key_admin_institution"
        private const val KEY_ADMIN_PHOTO = "key_admin_photo"
        private const val KEY_ADMIN_PIN = "key_admin_pin"
        private const val KEY_ADMIN_PRO_PAID = "key_admin_pro_paid"
        private const val KEY_ADMIN_PRO_SEMESTER = "key_admin_pro_semester"

        fun isGmailAddress(email: String): Boolean {
            val normalized = email.trim().lowercase()
            return normalized.endsWith("@gmail.com") || normalized.endsWith("@googlemail.com")
        }
    }

    var isRegistered: Boolean
        get() = prefs.getBoolean(KEY_IS_REGISTERED, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_REGISTERED, value).apply()

    var adminAccess: Boolean
        get() = prefs.getBoolean(KEY_ADMIN_ACCESS, false)
        set(value) = prefs.edit().putBoolean(KEY_ADMIN_ACCESS, value).apply()

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    var userEmail: String
        get() = prefs.getString(KEY_USER_EMAIL, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_USER_EMAIL, value.trim()).apply()

    fun getSavedGmailAccounts(): List<String> {
        val savedSet = prefs.getStringSet(KEY_SAVED_GMAIL_ACCOUNTS, emptySet()).orEmpty().toMutableSet()
        if (isRegistered) {
            val currentEmail = userEmail
            if (isGmailAddress(currentEmail)) {
                savedSet.add(currentEmail.lowercase())
            }
        }
        return savedSet.sorted()
    }

    fun addSavedGmailAccount(email: String) {
        val normalized = email.trim().lowercase()
        if (!isGmailAddress(normalized)) return
        val updated = prefs.getStringSet(KEY_SAVED_GMAIL_ACCOUNTS, emptySet()).orEmpty().toMutableSet()
        updated.add(normalized)
        prefs.edit().putStringSet(KEY_SAVED_GMAIL_ACCOUNTS, updated).apply()
    }

    fun hasSavedGmailAccount(): Boolean {
        return getSavedGmailAccounts().isNotEmpty()
    }

    fun isGmailAccountSaved(email: String): Boolean {
        val normalized = email.trim().lowercase()
        if (!isGmailAddress(normalized)) return false
        return getSavedGmailAccounts().contains(normalized)
    }

    fun saveProfile(profile: ScoutProfile) {
        prefs.edit()
            .putString(KEY_PROFILE_NAME, profile.name)
            .putString(KEY_PROFILE_SCHOOL, profile.school)
            .putString(KEY_PROFILE_GUDEP, profile.gudep)
            .putString(KEY_PROFILE_RANK, profile.rank.name)
            .putString(KEY_PROFILE_PHONE, profile.phone)
            .putInt(KEY_PROFILE_AVATAR, profile.avatarId)
            .putString(KEY_PROFILE_CUSTOM_PHOTO, profile.customPhotoPath)
            .putString(KEY_PROFILE_NPA, profile.npa)
            .putInt(KEY_PROFILE_SKU_PROGRESS, profile.skuProgress)
            .putInt(KEY_PROFILE_TKK_COUNT, profile.tkkCount)
            .putBoolean(KEY_PROFILE_IS_GARUDA, profile.isGaruda)
            .putBoolean(KEY_IS_REGISTERED, true) // Persist registration permanently
            .apply()
    }

    fun getProfile(): ScoutProfile {
        val rankName = prefs.getString(KEY_PROFILE_RANK, ScoutRank.PENGGALANG.name) ?: ScoutRank.PENGGALANG.name
        val rank = runCatching { ScoutRank.valueOf(rankName) }.getOrDefault(ScoutRank.PENGGALANG)
        val fallbackName = userEmail
            .takeIf { it.isNotBlank() && it.contains("@") }
            ?.substringBefore("@")
            ?.replace(".", " ")
            ?.split(" ")
            ?.filter { it.isNotBlank() }
            ?.joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
            ?: "Kakak Pramuka"

        return ScoutProfile(
            name = prefs.getString(KEY_PROFILE_NAME, fallbackName) ?: fallbackName,
            school = prefs.getString(KEY_PROFILE_SCHOOL, "SMP Negeri 1 Garuda") ?: "SMP Negeri 1 Garuda",
            gudep = prefs.getString(KEY_PROFILE_GUDEP, "01.001 - 01.002 Pangeran Diponegoro") ?: "01.001 - 01.002",
            rank = rank,
            phone = prefs.getString(KEY_PROFILE_PHONE, "0812-3456-7890") ?: "0812-3456-7890",
            avatarId = prefs.getInt(KEY_PROFILE_AVATAR, 1),
            customPhotoPath = prefs.getString(KEY_PROFILE_CUSTOM_PHOTO, "").orEmpty(),
            npa = prefs.getString(KEY_PROFILE_NPA, "PG-2026-0891") ?: "PG-2026-0891",
            skuProgress = prefs.getInt(KEY_PROFILE_SKU_PROGRESS, 75),
            tkkCount = prefs.getInt(KEY_PROFILE_TKK_COUNT, 8),
            isGaruda = prefs.getBoolean(KEY_PROFILE_IS_GARUDA, true)
        )
    }

    fun saveInstitutionConfig(config: InstitutionConfig) {
        prefs.edit()
            .putString(KEY_INSTITUTION_NAME, config.name)
            .putString(KEY_INSTITUTION_GUDEP, config.gudep)
            .putString(KEY_INSTITUTION_LOGO, config.logoPath)
            .apply()
    }

    fun getInstitutionConfig(): InstitutionConfig {
        val defaultName = prefs.getString(KEY_PROFILE_SCHOOL, null)
            ?.takeIf { it.isNotBlank() }
            ?: "Pangkalan SMP Negeri 1 Garuda"
        val defaultGudep = prefs.getString(KEY_PROFILE_GUDEP, null)
            ?.takeIf { it.isNotBlank() }
            ?: "01.001 - 01.002"
        return InstitutionConfig(
            name = prefs.getString(KEY_INSTITUTION_NAME, defaultName) ?: defaultName,
            gudep = prefs.getString(KEY_INSTITUTION_GUDEP, defaultGudep) ?: defaultGudep,
            logoPath = prefs.getString(KEY_INSTITUTION_LOGO, "").orEmpty()
        )
    }

    fun saveAdminConfig(config: AdminConfigProfile) {
        prefs.edit()
            .putBoolean(KEY_ADMIN_INITIALIZED, config.isInitialized)
            .putString(KEY_ADMIN_FULL_NAME, config.fullName)
            .putString(KEY_ADMIN_RANK, config.rank.name)
            .putString(KEY_ADMIN_GUDEP, config.gudep)
            .putString(KEY_ADMIN_INSTITUTION, config.institutionName)
            .putString(KEY_ADMIN_PHOTO, config.photoPath)
            .putString(KEY_ADMIN_PIN, config.pin)
            .putBoolean(KEY_ADMIN_PRO_PAID, config.isProSemesterPaid)
            .putString(KEY_ADMIN_PRO_SEMESTER, config.proSemesterLabel)
            .apply()
    }

    fun getAdminConfig(): AdminConfigProfile {
        val rankName = prefs.getString(KEY_ADMIN_RANK, ScoutRank.PEMBINA.name) ?: ScoutRank.PEMBINA.name
        val rank = runCatching { ScoutRank.valueOf(rankName) }.getOrDefault(ScoutRank.PEMBINA)
        return AdminConfigProfile(
            fullName = prefs.getString(KEY_ADMIN_FULL_NAME, "").orEmpty(),
            rank = rank,
            gudep = prefs.getString(KEY_ADMIN_GUDEP, "").orEmpty(),
            institutionName = prefs.getString(KEY_ADMIN_INSTITUTION, "").orEmpty(),
            photoPath = prefs.getString(KEY_ADMIN_PHOTO, "").orEmpty(),
            pin = prefs.getString(KEY_ADMIN_PIN, "").orEmpty(),
            isInitialized = prefs.getBoolean(KEY_ADMIN_INITIALIZED, false),
            isProSemesterPaid = prefs.getBoolean(KEY_ADMIN_PRO_PAID, false),
            proSemesterLabel = prefs.getString(KEY_ADMIN_PRO_SEMESTER, "").orEmpty()
        )
    }

    fun saveMaterials(materials: List<ScoutMaterial>) {
        val arr = JSONArray()
        materials.forEach { m ->
            val detailsArr = JSONArray()
            m.details.forEach { detailsArr.put(it) }
            arr.put(
                JSONObject()
                    .put("id", m.id)
                    .put("title", m.title)
                    .put("category", m.category)
                    .put("summary", m.summary)
                    .put("content", m.content)
                    .put("details", detailsArr)
                    .put("iconKey", m.iconKey)
            )
        }
        prefs.edit().putString(KEY_MATERIALS_JSON, arr.toString()).apply()
    }

    fun getMaterials(fallback: List<ScoutMaterial>): List<ScoutMaterial> {
        val raw = prefs.getString(KEY_MATERIALS_JSON, null) ?: return fallback
        return runCatching {
            val arr = JSONArray(raw)
            if (arr.length() == 0) return fallback
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                val detailsArr = obj.optJSONArray("details")
                val details = if (detailsArr != null) {
                    (0 until detailsArr.length()).map { j -> detailsArr.optString(j) }
                } else {
                    emptyList()
                }
                ScoutMaterial(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    category = obj.getString("category"),
                    summary = obj.optString("summary"),
                    content = obj.optString("content"),
                    details = details,
                    iconKey = obj.optString("iconKey", "book")
                )
            }
        }.getOrDefault(fallback)
    }

    fun saveSyllabi(syllabi: List<SemesterSyllabus>) {
        val arr = JSONArray()
        syllabi.forEach { s ->
            arr.put(
                JSONObject()
                    .put("id", s.id)
                    .put("semester", s.semester)
                    .put("rank", s.rank.name)
                    .put("programTitle", s.programTitle)
                    .put("weekRange", s.weekRange)
                    .put("competencyTarget", s.competencyTarget)
                    .put("method", s.method)
                    .put("status", s.status)
            )
        }
        prefs.edit().putString(KEY_SYLLABI_JSON, arr.toString()).apply()
    }

    fun getSyllabi(fallback: List<SemesterSyllabus>): List<SemesterSyllabus> {
        val raw = prefs.getString(KEY_SYLLABI_JSON, null) ?: return fallback
        return runCatching {
            val arr = JSONArray(raw)
            if (arr.length() == 0) return fallback
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                SemesterSyllabus(
                    id = obj.getString("id"),
                    semester = obj.getString("semester"),
                    rank = ScoutRank.fromString(obj.optString("rank")),
                    programTitle = obj.getString("programTitle"),
                    weekRange = obj.optString("weekRange"),
                    competencyTarget = obj.optString("competencyTarget"),
                    method = obj.optString("method"),
                    status = obj.optString("status", "Terjadwal")
                )
            }
        }.getOrDefault(fallback)
    }

    fun saveActivities(activities: List<ScoutActivity>) {
        val arr = JSONArray()
        activities.forEach { a ->
            arr.put(
                JSONObject()
                    .put("id", a.id)
                    .put("title", a.title)
                    .put("category", a.category)
                    .put("date", a.date)
                    .put("time", a.time)
                    .put("location", a.location)
                    .put("leader", a.leader)
                    .put("quota", a.quota)
                    .put("registeredCount", a.registeredCount)
                    .put("status", a.status)
                    .put("description", a.description)
                    .put("isUserRegistered", a.isUserRegistered)
            )
        }
        prefs.edit().putString(KEY_ACTIVITIES_JSON, arr.toString()).apply()
    }

    fun getActivities(fallback: List<ScoutActivity>): List<ScoutActivity> {
        val raw = prefs.getString(KEY_ACTIVITIES_JSON, null) ?: return fallback
        return runCatching {
            val arr = JSONArray(raw)
            if (arr.length() == 0) return fallback
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                ScoutActivity(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    category = obj.getString("category"),
                    date = obj.optString("date"),
                    time = obj.optString("time"),
                    location = obj.optString("location"),
                    leader = obj.optString("leader"),
                    quota = obj.optInt("quota", 50),
                    registeredCount = obj.optInt("registeredCount", 0),
                    status = obj.optString("status", "Akan Datang"),
                    description = obj.optString("description"),
                    isUserRegistered = obj.optBoolean("isUserRegistered", false)
                )
            }
        }.getOrDefault(fallback)
    }

    fun saveAnnouncements(announcements: List<ScoutAnnouncement>) {
        val arr = JSONArray()
        announcements.forEach { ann ->
            arr.put(
                JSONObject()
                    .put("id", ann.id)
                    .put("title", ann.title)
                    .put("content", ann.content)
                    .put("date", ann.date)
                    .put("priority", ann.priority)
                    .put("author", ann.author)
            )
        }
        prefs.edit().putString(KEY_ANNOUNCEMENTS_JSON, arr.toString()).apply()
    }

    fun getAnnouncements(fallback: List<ScoutAnnouncement>): List<ScoutAnnouncement> {
        val raw = prefs.getString(KEY_ANNOUNCEMENTS_JSON, null) ?: return fallback
        return runCatching {
            val arr = JSONArray(raw)
            if (arr.length() == 0) return fallback
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                ScoutAnnouncement(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    content = obj.optString("content"),
                    date = obj.optString("date"),
                    priority = obj.optString("priority", "Penting"),
                    author = obj.optString("author", "Kwartir Ranting")
                )
            }
        }.getOrDefault(fallback)
    }

    fun saveMembers(members: List<ScoutMember>) {
        val arr = JSONArray()
        members.forEach { m ->
            arr.put(
                JSONObject()
                    .put("id", m.id)
                    .put("name", m.name)
                    .put("school", m.school)
                    .put("gudep", m.gudep)
                    .put("rank", m.rank.name)
                    .put("skuLevel", m.skuLevel)
                    .put("joinDate", m.joinDate)
                    .put("phone", m.phone)
                    .put("verified", m.verified)
            )
        }
        prefs.edit().putString(KEY_MEMBERS_JSON, arr.toString()).apply()
    }

    fun getMembers(fallback: List<ScoutMember>): List<ScoutMember> {
        val raw = prefs.getString(KEY_MEMBERS_JSON, null) ?: return fallback
        return runCatching {
            val arr = JSONArray(raw)
            if (arr.length() == 0) return fallback
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                ScoutMember(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    school = obj.optString("school"),
                    gudep = obj.optString("gudep"),
                    rank = ScoutRank.fromString(obj.optString("rank")),
                    skuLevel = obj.optString("skuLevel"),
                    joinDate = obj.optString("joinDate"),
                    phone = obj.optString("phone"),
                    verified = obj.optBoolean("verified", true)
                )
            }
        }.getOrDefault(fallback)
    }

    /**
     * Logout handler:
     * Sets isLoggedIn = false,
     * BUT explicitly DOES NOT reset isRegistered, saved Gmail accounts, or admin onboarding data!
     */
    fun logout() {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, false).apply()
    }
}
