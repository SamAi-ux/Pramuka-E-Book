package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserAccount(
    @DocumentId val id: String = "",
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: String = ROLE_ANGGOTA,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    companion object {
        const val ROLE_ANGGOTA = "anggota"
        const val ROLE_ADMIN = "admin"
    }
}

@IgnoreExtraProperties
data class AdminRole(
    @DocumentId val id: String = "",
    val adminUid: String = "",
    val email: String = "",
    val role: String = UserAccount.ROLE_ADMIN,
    val grantedBy: String = "",
    val active: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

sealed interface AdminVerificationState {
    data object Unauthenticated : AdminVerificationState
    data object Checking : AdminVerificationState
    data class Authorized(
        val uid: String,
        val email: String,
        val displayName: String
    ) : AdminVerificationState
    data class Denied(
        val uid: String,
        val email: String,
        val assignedRole: String,
        val reason: String
    ) : AdminVerificationState
    data class Error(val message: String) : AdminVerificationState
}
