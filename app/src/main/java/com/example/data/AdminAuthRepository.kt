package com.example.data

import android.content.Context
import com.example.R
import com.example.model.AdminRole
import com.example.model.AdminVerificationState
import com.example.model.UserAccount
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class AdminAuthRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        db = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        auth = Firebase.auth
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Pengguna harus masuk dengan Google (Firebase Auth) terlebih dahulu.")
    }

    /**
     * Synchronizes the signed-in Firebase user into /users/{uid} (strictly as role='anggota'
     * if creating for the first time) and verifies whether the user holds the 'admin' role
     * via /admins/{uid} or verified bootstrapped admin credentials.
     */
    suspend fun verifyCurrentUserAdminRole(): AdminVerificationState {
        val currentUser = auth.currentUser ?: return AdminVerificationState.Unauthenticated
        val uid = currentUser.uid
        val email = (currentUser.email ?: "").trim()
        val displayName = (currentUser.displayName ?: email.substringBefore('@')).ifBlank { "Anggota Pramuka" }

        // 1. Ensure user document exists (never self-escalates role to 'admin')
        syncSignedInUserProfile(currentUser, email, displayName)

        // 2. Check authoritative /admins/{uid} record in Firestore
        val adminDocPath = "admins/$uid"
        val isListedAdmin = try {
            val adminSnap = db.collection("admins").document(uid).get().await()
            if (adminSnap.exists()) {
                val adminRole = adminSnap.toObject(
                    AdminRole::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
                adminRole?.active == true && adminRole.role == UserAccount.ROLE_ADMIN
            } else {
                false
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, adminDocPath, auth)
            val fsEx = generateSequence<Throwable>(e) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
            if (fsEx != null && fsEx.code != FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                return AdminVerificationState.Error(fsEx.localizedMessage ?: "Gagal memverifikasi peran admin.")
            }
            false
        }

        // 3. Verified bootstrapped super-admin check (must be emailVerified == true)
        val isBootstrappedAdmin = email.equals(BOOTSTRAPPED_ADMIN_EMAIL, ignoreCase = true) &&
            currentUser.isEmailVerified

        if (isListedAdmin || isBootstrappedAdmin) {
            // If bootstrapped admin does not yet have an /admins/{uid} document, register it safely
            if (isBootstrappedAdmin && !isListedAdmin) {
                ensureBootstrappedAdminRecord(uid, email)
            }
            return AdminVerificationState.Authorized(
                uid = uid,
                email = email,
                displayName = displayName
            )
        }

        // 4. Read assigned role in /users/{uid} for clear user feedback
        val assignedRole = readAssignedRole(uid) ?: UserAccount.ROLE_ANGGOTA
        if (assignedRole == UserAccount.ROLE_ADMIN) {
            // Even if user doc says admin, verify against /admins/{uid} succeeded or fail safely
            return AdminVerificationState.Denied(
                uid = uid,
                email = email,
                assignedRole = assignedRole,
                reason = "Akun belum terdaftar pada koleksi otoritas /admins di Firestore."
            )
        }

        return AdminVerificationState.Denied(
            uid = uid,
            email = email,
            assignedRole = assignedRole,
            reason = "Akun '$email' memiliki peran '$assignedRole'. Hanya akun dengan peran 'admin' yang dapat membuka Dashboard Admin."
        )
    }

    private suspend fun syncSignedInUserProfile(
        currentUser: FirebaseUser,
        email: String,
        displayName: String
    ) {
        val userDocRef = db.collection("users").document(currentUser.uid)
        try {
            val snap = userDocRef.get().await()
            if (!snap.exists()) {
                val safeEmail = email.ifBlank { "${currentUser.uid}@firebase.user" }
                val payload = mapOf(
                    "userId" to currentUser.uid,
                    "email" to safeEmail,
                    "displayName" to displayName.take(100),
                    "role" to UserAccount.ROLE_ANGGOTA,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                userDocRef.set(payload).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, userDocRef.path, auth)
        }
    }

    private suspend fun ensureBootstrappedAdminRecord(uid: String, email: String) {
        val adminRef = db.collection("admins").document(uid)
        try {
            val existingSnap = adminRef.get().await()
            val createdAt = if (existingSnap.exists()) {
                existingSnap.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else {
                null
            }
            val payload = mapOf(
                "adminUid" to uid,
                "email" to email,
                "role" to UserAccount.ROLE_ADMIN,
                "grantedBy" to uid,
                "active" to true,
                "createdAt" to (createdAt ?: FieldValue.serverTimestamp()),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            adminRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, adminRef.path, auth)
        }
    }

    suspend fun readAssignedRole(uid: String): String? {
        val userRef = db.collection("users").document(uid)
        return try {
            val snap = userRef.get().await()
            snap.getString("role")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, userRef.path, auth)
            null
        }
    }

    /**
     * Grants admin access to a target UID. Enforced by Firestore security rules
     * so only an existing verified admin can invoke this successfully.
     */
    suspend fun grantAdminRole(targetUid: String, targetEmail: String): Result<Unit> {
        val callerUid = requireUserId()
        val cleanTargetUid = targetUid.trim()
        val cleanEmail = targetEmail.trim()
        require(cleanTargetUid.matches(VALID_ID_REGEX)) {
            "UID target harus 1–128 karakter (huruf, angka, '-' atau '_')."
        }
        require(cleanEmail.length in 3..200 && cleanEmail.contains("@")) {
            "Email target tidak valid."
        }

        val adminRef = db.collection("admins").document(cleanTargetUid)
        return try {
            val existingSnap = adminRef.get().await()
            val createdAt = if (existingSnap.exists()) {
                existingSnap.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else {
                null
            }
            val payload = mapOf(
                "adminUid" to cleanTargetUid,
                "email" to cleanEmail,
                "role" to UserAccount.ROLE_ADMIN,
                "grantedBy" to callerUid,
                "active" to true,
                "createdAt" to (createdAt ?: FieldValue.serverTimestamp()),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            adminRef.set(payload).await()

            val userRef = db.collection("users").document(cleanTargetUid)
            runCatching {
                val userSnap = userRef.get().await()
                if (userSnap.exists()) {
                    userRef.update(
                        mapOf(
                            "role" to UserAccount.ROLE_ADMIN,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, adminRef.path, auth)
            Result.failure(e)
        }
    }

    suspend fun getAdminRecords(): Result<List<AdminRole>> {
        val callerUid = requireUserId()
        return try {
            val user = auth.currentUser
            val list = if (user?.email.equals(BOOTSTRAPPED_ADMIN_EMAIL, ignoreCase = true) && user?.isEmailVerified == true) {
                db.collection("admins").get().await()
                    .toObjects(AdminRole::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else {
                val grantedList = db.collection("admins")
                    .whereEqualTo("grantedBy", callerUid)
                    .get().await()
                    .toObjects(AdminRole::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                val ownSnap = db.collection("admins").document(callerUid).get().await()
                val ownRole = if (ownSnap.exists()) {
                    ownSnap.toObject(AdminRole::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } else null
                (listOfNotNull(ownRole) + grantedList).distinctBy { it.adminUid.ifBlank { it.id } }
            }
            Result.success(list)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, "admins", auth)
            Result.failure(e)
        }
    }

    companion object {
        const val BOOTSTRAPPED_ADMIN_EMAIL = "arifwir6@gmail.com"
        private val VALID_ID_REGEX = Regex("^[a-zA-Z0-9_\\-]{1,128}$")
    }
}
