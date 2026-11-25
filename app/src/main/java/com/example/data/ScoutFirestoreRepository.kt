package com.example.data

import android.content.Context
import com.example.R
import com.example.model.InstitutionConfig
import com.example.model.ScoutActivity
import com.example.model.ScoutAnnouncement
import com.example.model.ScoutMaterial
import com.example.model.ScoutMember
import com.example.model.ScoutProfile
import com.example.model.ScoutRank
import com.example.model.SemesterSyllabus
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ScoutFirestoreRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        db = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        auth = Firebase.auth
    )

    private fun requireUid(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Pengguna harus masuk dengan Google (Firebase Auth) sebelum sinkronisasi.")
    }

    // --- USER PROFILE (/users/{uid}/profile/main) ---
    fun observeUserProfile(uid: String): Flow<ScoutProfile?> {
        return db.collection("users").document(uid)
            .collection("profile").document(DOC_PROFILE_MAIN)
            .snapshots()
            .map { snapshot ->
                if (!snapshot.exists()) null else snapshotToProfile(snapshot)
            }
    }

    suspend fun getUserProfileOnce(uid: String): Result<ScoutProfile?> {
        val docRef = db.collection("users").document(uid)
            .collection("profile").document(DOC_PROFILE_MAIN)
        return try {
            val snap = docRef.get().await()
            Result.success(if (snap.exists()) snapshotToProfile(snap) else null)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path, auth)
            Result.failure(e)
        }
    }

    suspend fun saveUserProfile(uid: String, profile: ScoutProfile): Result<Unit> {
        val docRef = db.collection("users").document(uid)
            .collection("profile").document(DOC_PROFILE_MAIN)
        val payload = hashMapOf<String, Any>(
            "userId" to uid,
            "name" to profile.name.trim().ifBlank { "Anggota Pramuka" }.take(120),
            "school" to profile.school.trim().ifBlank { "Pangkalan Garuda" }.take(160),
            "gudep" to profile.gudep.trim().ifBlank { "01.001 - 01.002" }.take(80),
            "rank" to profile.rank.name,
            "phone" to profile.phone.trim().ifBlank { "-" }.take(40),
            "avatarId" to profile.avatarId.coerceIn(1, 20),
            "customPhotoPath" to profile.customPhotoPath.take(500),
            "npa" to profile.npa.trim().ifBlank { "PG-2026-0001" }.take(60),
            "skuProgress" to profile.skuProgress.coerceIn(0, 100),
            "tkkCount" to profile.tkkCount.coerceIn(0, 1000),
            "isGaruda" to profile.isGaruda
        )
        return upsertPreservingCreatedAt(docRef, payload, OperationType.WRITE)
    }

    // --- USER ACTIVITY REGISTRATIONS (/users/{uid}/activityRegistrations/{activityId}) ---
    fun observeActivityRegistrations(uid: String): Flow<Map<String, Boolean>> {
        return db.collection("users").document(uid)
            .collection("activityRegistrations")
            .snapshots()
            .map { querySnap ->
                querySnap.documents.associate { doc ->
                    doc.id to (doc.getBoolean("registered") ?: false)
                }
            }
    }

    suspend fun setActivityRegistration(uid: String, activityId: String, registered: Boolean): Result<Unit> {
        val docRef = db.collection("users").document(uid)
            .collection("activityRegistrations").document(activityId)
        val payload = hashMapOf<String, Any>(
            "userId" to uid,
            "activityId" to activityId,
            "registered" to registered
        )
        return upsertPreservingCreatedAt(docRef, payload, OperationType.WRITE)
    }

    // --- INSTITUTION CONFIG (/pangkalan/institution) ---
    fun observeInstitutionConfig(): Flow<InstitutionConfig?> {
        return db.collection("pangkalan").document(DOC_INSTITUTION)
            .snapshots()
            .map { snap ->
                if (!snap.exists()) null
                else InstitutionConfig(
                    name = snap.getString("name") ?: "Pangkalan SMP Negeri 1 Garuda",
                    gudep = snap.getString("gudep") ?: "01.001 - 01.002",
                    logoPath = snap.getString("logoPath") ?: ""
                )
            }
    }

    suspend fun saveInstitutionConfig(config: InstitutionConfig): Result<Unit> {
        val uid = requireUid()
        val docRef = db.collection("pangkalan").document(DOC_INSTITUTION)
        val payload = hashMapOf<String, Any>(
            "name" to config.name.trim().ifBlank { "Pangkalan SMP Negeri 1 Garuda" }.take(160),
            "gudep" to config.gudep.trim().ifBlank { "01.001 - 01.002" }.take(80),
            "logoPath" to config.logoPath.take(500),
            "updatedBy" to uid
        )
        return upsertPreservingCreatedAt(docRef, payload, OperationType.WRITE)
    }

    // --- MATERIALS (/materials/{materialId}) ---
    fun observeMaterials(): Flow<List<ScoutMaterial>> {
        return db.collection("materials")
            .snapshots()
            .map { querySnap ->
                querySnap.documents.mapNotNull { doc ->
                    val title = doc.getString("title") ?: return@mapNotNull null
                    val rawDetails = (doc.get("details") as? List<*>)
                        ?.mapNotNull { it as? String }
                        ?: emptyList()
                    ScoutMaterial(
                        id = doc.id,
                        title = title,
                        category = doc.getString("category") ?: "Materi Umum",
                        summary = doc.getString("summary") ?: "",
                        content = doc.getString("content") ?: "",
                        details = rawDetails,
                        iconKey = doc.getString("iconKey") ?: "book"
                    )
                }
            }
    }

    suspend fun upsertMaterial(material: ScoutMaterial): Result<Unit> {
        val uid = requireUid()
        val docRef = db.collection("materials").document(material.id)
        val payload = hashMapOf<String, Any>(
            "title" to material.title.trim().take(200),
            "category" to material.category.trim().ifBlank { "Materi Umum" }.take(80),
            "summary" to material.summary.trim().take(500),
            "content" to material.content.trim().take(10000),
            "details" to material.details.take(50),
            "iconKey" to material.iconKey.trim().ifBlank { "book" }.take(40),
            "updatedBy" to uid
        )
        return upsertPreservingCreatedAt(docRef, payload, OperationType.WRITE)
    }

    // --- SYLLABI (/syllabi/{syllabusId}) ---
    fun observeSyllabi(): Flow<List<SemesterSyllabus>> {
        return db.collection("syllabi")
            .snapshots()
            .map { querySnap ->
                querySnap.documents.mapNotNull { doc ->
                    val title = doc.getString("programTitle") ?: return@mapNotNull null
                    SemesterSyllabus(
                        id = doc.id,
                        semester = doc.getString("semester") ?: "Semester Ganjil 2026/2027",
                        rank = ScoutRank.fromString(doc.getString("rank") ?: ScoutRank.PENGGALANG.name),
                        programTitle = title,
                        weekRange = doc.getString("weekRange") ?: "Minggu 1 - 4",
                        competencyTarget = doc.getString("competencyTarget") ?: "",
                        method = doc.getString("method") ?: "Praktik Lapangan",
                        status = doc.getString("status") ?: "Terjadwal"
                    )
                }
            }
    }

    suspend fun upsertSyllabus(syllabus: SemesterSyllabus): Result<Unit> {
        val uid = requireUid()
        val docRef = db.collection("syllabi").document(syllabus.id)
        val payload = hashMapOf<String, Any>(
            "semester" to syllabus.semester.trim().take(80),
            "rank" to syllabus.rank.name,
            "programTitle" to syllabus.programTitle.trim().take(200),
            "weekRange" to syllabus.weekRange.trim().ifBlank { "Minggu 1 - 4" }.take(80),
            "competencyTarget" to syllabus.competencyTarget.trim().take(1000),
            "method" to syllabus.method.trim().ifBlank { "Praktik Lapangan" }.take(200),
            "status" to syllabus.status.trim().ifBlank { "Terjadwal" }.take(40),
            "updatedBy" to uid
        )
        return upsertPreservingCreatedAt(docRef, payload, OperationType.WRITE)
    }

    // --- ACTIVITIES (/activities/{activityId}) ---
    fun observeActivities(): Flow<List<ScoutActivity>> {
        return db.collection("activities")
            .snapshots()
            .map { querySnap ->
                querySnap.documents.mapNotNull { doc ->
                    val title = doc.getString("title") ?: return@mapNotNull null
                    ScoutActivity(
                        id = doc.id,
                        title = title,
                        category = doc.getString("category") ?: "Latihan Rutin",
                        date = doc.getString("date") ?: "-",
                        time = doc.getString("time") ?: "-",
                        location = doc.getString("location") ?: "-",
                        leader = doc.getString("leader") ?: "Pembina",
                        quota = (doc.getLong("quota") ?: 50L).toInt(),
                        registeredCount = (doc.getLong("registeredCount") ?: 0L).toInt(),
                        status = doc.getString("status") ?: "Akan Datang",
                        description = doc.getString("description") ?: "",
                        isUserRegistered = false
                    )
                }
            }
    }

    suspend fun upsertActivity(activity: ScoutActivity): Result<Unit> {
        val uid = requireUid()
        val docRef = db.collection("activities").document(activity.id)
        val payload = hashMapOf<String, Any>(
            "title" to activity.title.trim().take(200),
            "category" to activity.category.trim().ifBlank { "Latihan Rutin" }.take(80),
            "date" to activity.date.trim().ifBlank { "-" }.take(80),
            "time" to activity.time.trim().ifBlank { "-" }.take(80),
            "location" to activity.location.trim().ifBlank { "-" }.take(200),
            "leader" to activity.leader.trim().ifBlank { "Pembina" }.take(120),
            "quota" to activity.quota.coerceIn(1, 5000),
            "registeredCount" to activity.registeredCount.coerceIn(0, 5000),
            "status" to activity.status.trim().ifBlank { "Akan Datang" }.take(40),
            "description" to activity.description.trim().take(2000),
            "updatedBy" to uid
        )
        return upsertPreservingCreatedAt(docRef, payload, OperationType.WRITE)
    }

    // --- ANNOUNCEMENTS (/announcements/{announcementId}) ---
    fun observeAnnouncements(): Flow<List<ScoutAnnouncement>> {
        return db.collection("announcements")
            .snapshots()
            .map { querySnap ->
                querySnap.documents.mapNotNull { doc ->
                    val title = doc.getString("title") ?: return@mapNotNull null
                    ScoutAnnouncement(
                        id = doc.id,
                        title = title,
                        content = doc.getString("content") ?: "",
                        date = doc.getString("date") ?: "Hari Ini",
                        priority = doc.getString("priority") ?: "Penting",
                        author = doc.getString("author") ?: "Kwartir Ranting"
                    )
                }
            }
    }

    suspend fun upsertAnnouncement(announcement: ScoutAnnouncement): Result<Unit> {
        val uid = requireUid()
        val docRef = db.collection("announcements").document(announcement.id)
        val payload = hashMapOf<String, Any>(
            "title" to announcement.title.trim().take(200),
            "content" to announcement.content.trim().take(4000),
            "date" to announcement.date.trim().ifBlank { "Hari Ini" }.take(80),
            "priority" to announcement.priority.trim().ifBlank { "Penting" }.take(40),
            "author" to announcement.author.trim().ifBlank { "Admin Pembina" }.take(120),
            "updatedBy" to uid
        )
        return upsertPreservingCreatedAt(docRef, payload, OperationType.WRITE)
    }

    // --- MEMBERS (/members/{memberId}) - Strictly Admin Read/Write ---
    fun observeMembers(): Flow<List<ScoutMember>> {
        return db.collection("members")
            .snapshots()
            .map { querySnap ->
                querySnap.documents.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    ScoutMember(
                        id = doc.id,
                        name = name,
                        school = doc.getString("school") ?: "Gudep Garuda",
                        gudep = doc.getString("gudep") ?: "01.001",
                        rank = ScoutRank.fromString(doc.getString("rank") ?: ScoutRank.PENGGALANG.name),
                        skuLevel = doc.getString("skuLevel") ?: "Penggalang",
                        joinDate = doc.getString("joinDate") ?: "Hari Ini",
                        phone = doc.getString("phone") ?: "-",
                        verified = doc.getBoolean("verified") ?: true
                    )
                }
            }
    }

    suspend fun upsertMember(member: ScoutMember): Result<Unit> {
        val uid = requireUid()
        val docRef = db.collection("members").document(member.id)
        return try {
            val existingSnap = docRef.get().await()
            val payload = hashMapOf<String, Any>(
                "name" to member.name.trim().take(120),
                "school" to member.school.trim().ifBlank { "Gudep Garuda" }.take(160),
                "gudep" to member.gudep.trim().ifBlank { "01.001" }.take(80),
                "rank" to member.rank.name,
                "skuLevel" to member.skuLevel.trim().ifBlank { member.rank.title }.take(100),
                "joinDate" to member.joinDate.trim().ifBlank { "Hari Ini" }.take(60),
                "phone" to member.phone.trim().ifBlank { "-" }.take(40),
                "verified" to member.verified,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (existingSnap.exists()) {
                val createdAt = existingSnap.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                val createdBy = existingSnap.getString("createdBy") ?: uid
                payload["createdBy"] = createdBy
                if (createdAt != null) {
                    payload["createdAt"] = createdAt
                } else {
                    payload["createdAt"] = FieldValue.serverTimestamp()
                }
            } else {
                payload["createdBy"] = uid
                payload["createdAt"] = FieldValue.serverTimestamp()
            }
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
            Result.failure(e)
        }
    }

    suspend fun syncFullAdminCatalog(
        institutionConfig: InstitutionConfig,
        materials: List<ScoutMaterial>,
        syllabi: List<SemesterSyllabus>,
        activities: List<ScoutActivity>,
        announcements: List<ScoutAnnouncement>,
        members: List<ScoutMember>
    ): Result<Int> {
        var syncedCount = 0
        val instResult = saveInstitutionConfig(institutionConfig)
        if (instResult.isFailure) return Result.failure(instResult.exceptionOrNull()!!)
        syncedCount += 1

        materials.forEach { item ->
            val r = upsertMaterial(item)
            if (r.isFailure) return Result.failure(r.exceptionOrNull()!!)
            syncedCount += 1
        }
        syllabi.forEach { item ->
            val r = upsertSyllabus(item)
            if (r.isFailure) return Result.failure(r.exceptionOrNull()!!)
            syncedCount += 1
        }
        activities.forEach { item ->
            val r = upsertActivity(item)
            if (r.isFailure) return Result.failure(r.exceptionOrNull()!!)
            syncedCount += 1
        }
        announcements.forEach { item ->
            val r = upsertAnnouncement(item)
            if (r.isFailure) return Result.failure(r.exceptionOrNull()!!)
            syncedCount += 1
        }
        members.forEach { item ->
            val r = upsertMember(item)
            if (r.isFailure) return Result.failure(r.exceptionOrNull()!!)
            syncedCount += 1
        }
        return Result.success(syncedCount)
    }

    private suspend fun upsertPreservingCreatedAt(
        docRef: DocumentReference,
        fields: MutableMap<String, Any>,
        operationType: OperationType
    ): Result<Unit> {
        return try {
            val snap = docRef.get().await()
            val createdAt = if (snap.exists()) {
                snap.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else null
            fields["createdAt"] = createdAt ?: FieldValue.serverTimestamp()
            fields["updatedAt"] = FieldValue.serverTimestamp()
            docRef.set(fields).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, operationType, docRef.path, auth)
            Result.failure(e)
        }
    }

    private fun snapshotToProfile(snap: DocumentSnapshot): ScoutProfile {
        return ScoutProfile(
            name = snap.getString("name") ?: "Kakak Pramuka",
            school = snap.getString("school") ?: "Pangkalan Gugus Depan Garuda",
            gudep = snap.getString("gudep") ?: "01.001 - 01.002",
            rank = ScoutRank.fromString(snap.getString("rank") ?: ScoutRank.PENGGALANG.name),
            phone = snap.getString("phone") ?: "-",
            avatarId = (snap.getLong("avatarId") ?: 1L).toInt().coerceIn(1, 20),
            customPhotoPath = snap.getString("customPhotoPath") ?: "",
            npa = snap.getString("npa") ?: "PG-2026-0891",
            skuProgress = (snap.getLong("skuProgress") ?: 75L).toInt().coerceIn(0, 100),
            tkkCount = (snap.getLong("tkkCount") ?: 8L).toInt().coerceIn(0, 1000),
            isGaruda = snap.getBoolean("isGaruda") ?: true
        )
    }

    companion object {
        const val DOC_PROFILE_MAIN = "main"
        const val DOC_INSTITUTION = "institution"
    }
}
