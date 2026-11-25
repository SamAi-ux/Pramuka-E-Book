package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.example.model.AdminVerificationState
import com.example.model.UserAccount
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class AdminAuthRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun verifyCurrentUserAdminRole_unauthenticated_returnsUnauthenticated() = runBlocking {
        auth.signOut()
        val repo = AdminAuthRepository(firestore, auth)

        val state = withTimeout(DEFAULT_TIMEOUT_MS) { repo.verifyCurrentUserAdminRole() }
        assertTrue(state is AdminVerificationState.Unauthenticated)
    }

    @Test
    fun verifyCurrentUserAdminRole_regularMember_createsAnggotaProfileAndReturnsDenied() = runBlocking {
        val memberEmail = "anggota-${UUID.randomUUID()}@test.com"
        val memberUid = signInTestUser(memberEmail)
        val repo = AdminAuthRepository(firestore, auth)

        val state = withTimeout(DEFAULT_TIMEOUT_MS) { repo.verifyCurrentUserAdminRole() }
        assertTrue("Expected Denied for regular member, got $state", state is AdminVerificationState.Denied)
        val denied = state as AdminVerificationState.Denied
        assertEquals(UserAccount.ROLE_ANGGOTA, denied.assignedRole)

        val storedRole = withTimeout(DEFAULT_TIMEOUT_MS) { repo.readAssignedRole(memberUid) }
        assertEquals(UserAccount.ROLE_ANGGOTA, storedRole)
    }

    @Test
    fun nonAdmin_cannotSelfEscalateToAdminInUsersOrAdminsCollection() = runBlocking {
        val memberEmail = "escalate-${UUID.randomUUID()}@test.com"
        val memberUid = signInTestUser(memberEmail)
        val repo = AdminAuthRepository(firestore, auth)

        // Create normal anggota profile first
        withTimeout(DEFAULT_TIMEOUT_MS) { repo.verifyCurrentUserAdminRole() }

        // Attempt to grant self admin via /admins/{uid} -> must fail with PERMISSION_DENIED
        val adminGrantResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.grantAdminRole(memberUid, memberEmail)
        }
        assertTrue(adminGrantResult.isFailure)
        val fsEx1 = adminGrantResult.exceptionOrNull()?.unwrapFirestoreException()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx1?.code)

        // Attempt to update own /users/{uid}.role = "admin" -> must fail with PERMISSION_DENIED
        var roleUpdateDenied = false
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("users").document(memberUid)
                    .update(
                        mapOf(
                            "role" to UserAccount.ROLE_ADMIN,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
            }
        } catch (e: Exception) {
            val fsEx2 = e.unwrapFirestoreException()
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx2?.code)
            roleUpdateDenied = true
        }
        assertTrue("Expected PERMISSION_DENIED on role self-escalation", roleUpdateDenied)
    }

    @Test
    fun verifiedAdmin_receivesAuthorizedAndCanGrantAdminToAnotherMember() = runBlocking {
        val adminEmail = "pembina-admin-${UUID.randomUUID()}@test.com"
        val adminUid = signInTestUser(adminEmail)
        seedAdminDocumentBypassingRules(adminUid, adminEmail)

        val adminRepo = AdminAuthRepository(firestore, auth)
        val adminState = withTimeout(DEFAULT_TIMEOUT_MS) { adminRepo.verifyCurrentUserAdminRole() }
        assertTrue("Expected Authorized for admin, got $adminState", adminState is AdminVerificationState.Authorized)

        // Create another regular member
        val targetEmail = "calon-admin-${UUID.randomUUID()}@test.com"
        val targetUid = signInTestUser(targetEmail)
        val memberRepo = AdminAuthRepository(firestore, auth)
        val beforeGrant = withTimeout(DEFAULT_TIMEOUT_MS) { memberRepo.verifyCurrentUserAdminRole() }
        assertTrue(beforeGrant is AdminVerificationState.Denied)

        // Switch back to admin and grant admin role to targetUid
        signInTestUser(adminEmail)
        val grantResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            adminRepo.grantAdminRole(targetUid, targetEmail)
        }
        assertTrue("Expected admin grant to succeed: ${grantResult.exceptionOrNull()}", grantResult.isSuccess)

        // Target member signs in and is now Authorized
        signInTestUser(targetEmail)
        val afterGrant = withTimeout(DEFAULT_TIMEOUT_MS) { memberRepo.verifyCurrentUserAdminRole() }
        assertTrue("Expected Authorized after admin grant, got $afterGrant", afterGrant is AdminVerificationState.Authorized)
    }

    @Test
    fun nonAdmin_cannotReadAnotherUserDocument() = runBlocking {
        val aliceEmail = "alice-${UUID.randomUUID()}@test.com"
        val aliceUid = signInTestUser(aliceEmail)
        val aliceRepo = AdminAuthRepository(firestore, auth)
        withTimeout(DEFAULT_TIMEOUT_MS) { aliceRepo.verifyCurrentUserAdminRole() }

        val bobEmail = "bob-${UUID.randomUUID()}@test.com"
        signInTestUser(bobEmail)

        var denied = false
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("users").document(aliceUid).get().await()
            }
        } catch (e: Exception) {
            val fsEx = e.unwrapFirestoreException()
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
            denied = true
        }
        assertTrue("Expected PERMISSION_DENIED when reading another user's profile", denied)
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MS = 5000L
    }
}
