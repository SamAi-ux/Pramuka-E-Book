package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.example.model.InstitutionConfig
import com.example.model.ScoutActivity
import com.example.model.ScoutAnnouncement
import com.example.model.ScoutMaterial
import com.example.model.ScoutMember
import com.example.model.ScoutProfile
import com.example.model.ScoutRank
import com.example.model.SemesterSyllabus
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class ScoutFirestoreRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun member_canSaveAndObserveOwnProfileAndActivityRegistration_andCannotReadOtherUserProfile() = runBlocking {
        val aliceEmail = "alice-sync-${UUID.randomUUID()}@test.com"
        val aliceUid = signInTestUser(aliceEmail)
        val repo = ScoutFirestoreRepository(firestore, auth)

        val aliceProfile = ScoutProfile(
            name = "Alice Garuda",
            school = "SMPN 1 Nusantara",
            gudep = "03.001 - 03.002",
            rank = ScoutRank.PENGGALANG,
            phone = "081234567890",
            avatarId = 2,
            npa = "PG-2026-9911",
            skuProgress = 80,
            tkkCount = 9,
            isGaruda = true
        )

        val saveRes = withTimeout(DEFAULT_TIMEOUT_MS) { repo.saveUserProfile(aliceUid, aliceProfile) }
        assertTrue("Profile save should succeed: ${saveRes.exceptionOrNull()}", saveRes.isSuccess)

        val loaded = withTimeout(DEFAULT_TIMEOUT_MS) { repo.getUserProfileOnce(aliceUid) }.getOrNull()
        assertNotNull(loaded)
        assertEquals("Alice Garuda", loaded?.name)
        assertEquals(ScoutRank.PENGGALANG, loaded?.rank)

        val regRes = withTimeout(DEFAULT_TIMEOUT_MS) { repo.setActivityRegistration(aliceUid, "act_1", true) }
        assertTrue("Activity registration save should succeed: ${regRes.exceptionOrNull()}", regRes.isSuccess)
        val regs = withTimeout(DEFAULT_TIMEOUT_MS) { repo.observeActivityRegistrations(aliceUid).first() }
        assertEquals(true, regs["act_1"])

        // Switch to Bob and verify Bob cannot read Alice's profile
        val bobEmail = "bob-sync-${UUID.randomUUID()}@test.com"
        signInTestUser(bobEmail)
        val bobReadAlice = withTimeout(DEFAULT_TIMEOUT_MS) { repo.getUserProfileOnce(aliceUid) }
        assertTrue(bobReadAlice.isFailure)
        val fsEx = bobReadAlice.exceptionOrNull()?.unwrapFirestoreException()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
    }

    @Test
    fun verifiedAdmin_canSyncCatalogAndMembers_whileRegularMemberCanReadCatalogOnly() = runBlocking {
        val adminEmail = "pembina-sync-${UUID.randomUUID()}@test.com"
        val adminUid = signInTestUser(adminEmail)
        seedAdminDocumentBypassingRules(adminUid, adminEmail)

        val adminRepo = ScoutFirestoreRepository(firestore, auth)
        val syncResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            adminRepo.syncFullAdminCatalog(
                institutionConfig = InstitutionConfig("SMPN 1 Garuda", "01.001 - 01.002", ""),
                materials = listOf(
                    ScoutMaterial(
                        id = "mat_sync_1",
                        title = "Sandi Morse Cloud",
                        category = "Sandi & Morse",
                        summary = "Ringkasan",
                        content = "Konten lengkap",
                        details = listOf("Titik", "Garis")
                    )
                ),
                syllabi = listOf(
                    SemesterSyllabus(
                        id = "syl_sync_1",
                        semester = "Semester Ganjil 2026/2027",
                        rank = ScoutRank.PENGGALANG,
                        programTitle = "Pionering Terpadu",
                        weekRange = "Minggu 1 - 4",
                        competencyTarget = "Kuasai simpul dasar",
                        method = "Praktik"
                    )
                ),
                activities = listOf(
                    ScoutActivity(
                        id = "act_sync_1",
                        title = "Persami Nusantara",
                        category = "Perkemahan",
                        date = "25 Nov 2026",
                        time = "14:00",
                        location = "Bumi Perkemahan",
                        leader = "Kak Pembina",
                        quota = 80,
                        registeredCount = 5,
                        status = "Akan Datang",
                        description = "Kegiatan rutin"
                    )
                ),
                announcements = listOf(
                    ScoutAnnouncement(
                        id = "ann_sync_1",
                        title = "Warta Cloud",
                        content = "Latihan hari Sabtu.",
                        date = "Hari Ini",
                        priority = "Penting",
                        author = "Pembina"
                    )
                ),
                members = listOf(
                    ScoutMember(
                        id = "mem_sync_1",
                        name = "Sinta Ramadhani",
                        school = "SMPN 1 Garuda",
                        gudep = "01.001 - 01.002",
                        rank = ScoutRank.PENGGALANG,
                        skuLevel = "Penggalang Rakit",
                        joinDate = "Hari Ini",
                        phone = "081299990000",
                        verified = true
                    )
                )
            )
        }
        assertTrue("Full catalog sync should succeed: ${syncResult.exceptionOrNull()}", syncResult.isSuccess)
        assertEquals(6, syncResult.getOrNull())

        val adminMembers = withTimeout(DEFAULT_TIMEOUT_MS) { adminRepo.observeMembers().first() }
        assertTrue(adminMembers.any { it.id == "mem_sync_1" })

        // Regular member signs in
        val memberEmail = "anggota-catalog-${UUID.randomUUID()}@test.com"
        signInTestUser(memberEmail)
        val memberRepo = ScoutFirestoreRepository(firestore, auth)

        // Member can read catalog collections
        val inst = withTimeout(DEFAULT_TIMEOUT_MS) { memberRepo.observeInstitutionConfig().first() }
        assertEquals("SMPN 1 Garuda", inst?.name)
        val mats = withTimeout(DEFAULT_TIMEOUT_MS) { memberRepo.observeMaterials().first() }
        assertTrue(mats.any { it.id == "mat_sync_1" })

        // Member CANNOT write material or read /members
        val writeMatDenied = withTimeout(DEFAULT_TIMEOUT_MS) {
            memberRepo.upsertMaterial(
                ScoutMaterial("mat_illegal", "Hack", "Cat", "Sum", "Content")
            )
        }
        assertTrue(writeMatDenied.isFailure)
        assertEquals(
            FirebaseFirestoreException.Code.PERMISSION_DENIED,
            writeMatDenied.exceptionOrNull()?.unwrapFirestoreException()?.code
        )
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MS = 5000L
    }
}
