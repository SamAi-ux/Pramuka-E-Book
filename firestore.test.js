const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");
const serverTimestamp = () => new Date(Date.now() - 2000);

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ADMIN_EMAIL = "arifwir6@gmail.com";
const ADMIN_UID = "super_admin_uid";
const GRANTED_ADMIN_UID = "granted_admin_uid";
const MEMBER_UID = "member_uid_123";
const OTHER_UID = "other_uid_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(MEMBER_UID).get());
  await assertFails(unauthDb.collection("admins").doc(MEMBER_UID).get());
  await assertFails(unauthDb.collection("pangkalan").doc("institution").get());
  await assertFails(unauthDb.collection("materials").get());
  await assertFails(unauthDb.collection("syllabi").get());
  await assertFails(unauthDb.collection("activities").get());
  await assertFails(unauthDb.collection("announcements").get());
  await assertFails(unauthDb.collection("members").get());
  await assertFails(
    unauthDb.collection("users").doc(MEMBER_UID).set({
      userId: MEMBER_UID,
      email: "anon@test.com",
      displayName: "Anon",
      role: "anggota",
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );
});

test("Member: can create own profile as 'anggota' but cannot self-assign 'admin' role", async () => {
  const memberDb = testEnv.authenticatedContext(MEMBER_UID, {
    email: "anggota@test.com",
    email_verified: true,
  }).firestore();

  await assertSucceeds(
    memberDb.collection("users").doc(MEMBER_UID).set({
      userId: MEMBER_UID,
      email: "anggota@test.com",
      displayName: "Anggota Biasa",
      role: "anggota",
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  // Attempt privilege escalation on create
  await assertFails(
    memberDb.collection("users").doc(OTHER_UID).set({
      userId: OTHER_UID,
      email: "other@test.com",
      displayName: "Hacker",
      role: "admin",
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );
});

test("Member: cannot escalate own role to 'admin' on update or inject ghost fields", async () => {
  const memberDb = testEnv.authenticatedContext(MEMBER_UID, {
    email: "anggota@test.com",
    email_verified: true,
  }).firestore();

  await assertSucceeds(
    memberDb.collection("users").doc(MEMBER_UID).set({
      userId: MEMBER_UID,
      email: "anggota@test.com",
      displayName: "Anggota Biasa",
      role: "anggota",
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  await assertSucceeds(
    memberDb.collection("users").doc(MEMBER_UID).update({
      displayName: "Anggota Teladan",
      updatedAt: serverTimestamp(),
    })
  );

  await assertFails(
    memberDb.collection("users").doc(MEMBER_UID).update({
      role: "admin",
      updatedAt: serverTimestamp(),
    })
  );

  await assertFails(
    memberDb.collection("users").doc(MEMBER_UID).update({
      isVerified: true,
      updatedAt: serverTimestamp(),
    })
  );
});

test("User Scout Profile & Activity Registration: owner-isolated and rejects invalid bounds", async () => {
  const memberDb = testEnv.authenticatedContext(MEMBER_UID, {
    email: "anggota@test.com",
    email_verified: true,
  }).firestore();
  const otherDb = testEnv.authenticatedContext(OTHER_UID, {
    email: "other@test.com",
    email_verified: true,
  }).firestore();

  const profileRef = memberDb.collection("users").doc(MEMBER_UID).collection("profile").doc("main");
  await assertSucceeds(
    profileRef.set({
      userId: MEMBER_UID,
      name: "Adit Pramuka",
      school: "SMPN 1 Garuda",
      gudep: "01.001 - 01.002",
      rank: "PENGGALANG",
      phone: "08123456789",
      avatarId: 1,
      customPhotoPath: "",
      npa: "PG-2026-1001",
      skuProgress: 75,
      tkkCount: 8,
      isGaruda: true,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  // Other user cannot read or overwrite member's PII profile
  await assertFails(otherDb.collection("users").doc(MEMBER_UID).collection("profile").doc("main").get());
  await assertFails(
    otherDb.collection("users").doc(MEMBER_UID).collection("profile").doc("main").update({
      name: "Diubah Orang Lain",
      updatedAt: serverTimestamp(),
    })
  );

  // Reject out-of-range skuProgress
  await assertFails(
    profileRef.update({
      skuProgress: 150,
      updatedAt: serverTimestamp(),
    })
  );

  // Owner can register/unregister for an activity
  const regRef = memberDb.collection("users").doc(MEMBER_UID).collection("activityRegistrations").doc("act_1");
  await assertSucceeds(
    regRef.set({
      userId: MEMBER_UID,
      activityId: "act_1",
      registered: true,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );
  await assertFails(otherDb.collection("users").doc(MEMBER_UID).collection("activityRegistrations").doc("act_1").get());
});

test("Member: cannot read another user's profile or write /admins", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("users").doc(OTHER_UID).set({
      userId: OTHER_UID,
      email: "other@test.com",
      displayName: "Other Scout",
      role: "anggota",
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    });
  });

  const memberDb = testEnv.authenticatedContext(MEMBER_UID, {
    email: "anggota@test.com",
    email_verified: true,
  }).firestore();

  await assertFails(memberDb.collection("users").doc(OTHER_UID).get());
  await assertFails(
    memberDb.collection("admins").doc(MEMBER_UID).set({
      adminUid: MEMBER_UID,
      email: "anggota@test.com",
      role: "admin",
      grantedBy: MEMBER_UID,
      active: true,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );
});

test("Shared catalog (pangkalan, materials, syllabi, activities, announcements) & /members PII isolation", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();

  // Seed granted admin & shared docs using bootstrapped admin
  await assertSucceeds(
    adminDb.collection("admins").doc(GRANTED_ADMIN_UID).set({
      adminUid: GRANTED_ADMIN_UID,
      email: "pembina@test.com",
      role: "admin",
      grantedBy: ADMIN_UID,
      active: true,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  const grantedAdminDb = testEnv.authenticatedContext(GRANTED_ADMIN_UID, {
    email: "pembina@test.com",
    email_verified: true,
  }).firestore();

  // Granted admin updates institution, material, syllabus, activity, announcement, member
  await assertSucceeds(
    grantedAdminDb.collection("pangkalan").doc("institution").set({
      name: "SMP Negeri 1 Garuda",
      gudep: "01.001 - 01.002",
      logoPath: "",
      updatedBy: GRANTED_ADMIN_UID,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  await assertSucceeds(
    grantedAdminDb.collection("materials").doc("mat_1").set({
      title: "Sandi Morse",
      category: "Sandi & Morse",
      summary: "Dasar titik garis",
      content: "Isi lengkap materi Morse",
      details: ["Titik = 1 ketuk", "Garis = 3 ketuk"],
      iconKey: "book",
      updatedBy: GRANTED_ADMIN_UID,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  await assertSucceeds(
    grantedAdminDb.collection("syllabi").doc("syl_1").set({
      semester: "Semester Ganjil 2026/2027",
      rank: "PENGGALANG",
      programTitle: "Pionering Dasar",
      weekRange: "Minggu 1 - 4",
      competencyTarget: "Menguasai simpul dasar",
      method: "Praktik Lapangan",
      status: "Berjalan",
      updatedBy: GRANTED_ADMIN_UID,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  await assertSucceeds(
    grantedAdminDb.collection("activities").doc("act_1").set({
      title: "Persami Garuda",
      category: "Perkemahan",
      date: "20 Nov 2026",
      time: "14:00 WIB",
      location: "Bumi Perkemahan",
      leader: "Kak Budi",
      quota: 100,
      registeredCount: 12,
      status: "Akan Datang",
      description: "Perkemahan Sabtu Minggu",
      updatedBy: GRANTED_ADMIN_UID,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  await assertSucceeds(
    grantedAdminDb.collection("announcements").doc("ann_1").set({
      title: "Jadwal Latihan Rutin",
      content: "Latihan dimulai pukul 14:00 WIB.",
      date: "Hari Ini",
      priority: "Penting",
      author: "Pembina Gudep",
      updatedBy: GRANTED_ADMIN_UID,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  await assertSucceeds(
    grantedAdminDb.collection("members").doc("mem_1").set({
      name: "Bima Sakti",
      school: "SMP Negeri 1 Garuda",
      gudep: "01.001 - 01.002",
      rank: "PENGGALANG",
      skuLevel: "Penggalang Ramu",
      joinDate: "Hari Ini",
      phone: "081234567890",
      verified: true,
      createdBy: GRANTED_ADMIN_UID,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  // Granted admin can list /members
  await assertSucceeds(grantedAdminDb.collection("members").get());

  // Regular member can read shared catalog, CANNOT write shared catalog, and CANNOT read/list /members PII
  const memberDb = testEnv.authenticatedContext(MEMBER_UID, {
    email: "anggota@test.com",
    email_verified: true,
  }).firestore();

  await assertSucceeds(memberDb.collection("pangkalan").doc("institution").get());
  await assertSucceeds(memberDb.collection("materials").get());
  await assertSucceeds(memberDb.collection("syllabi").get());
  await assertSucceeds(memberDb.collection("activities").get());
  await assertSucceeds(memberDb.collection("announcements").get());

  await assertFails(
    memberDb.collection("materials").doc("mat_hack").set({
      title: "Hack",
      category: "Sandi",
      summary: "x",
      content: "x",
      details: [],
      iconKey: "book",
      updatedBy: MEMBER_UID,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );
  await assertFails(
    memberDb.collection("pangkalan").doc("institution").update({
      name: "Sabotase",
      updatedBy: MEMBER_UID,
      updatedAt: serverTimestamp(),
    })
  );
  await assertFails(memberDb.collection("members").doc("mem_1").get());
  await assertFails(memberDb.collection("members").get());
});

test("Email spoofing test: unverified admin email is rejected", async () => {
  const spoofedDb = testEnv.authenticatedContext("spoof_uid", {
    email: ADMIN_EMAIL,
    email_verified: false,
  }).firestore();

  await assertFails(
    spoofedDb.collection("admins").doc("spoof_uid").set({
      adminUid: "spoof_uid",
      email: ADMIN_EMAIL,
      role: "admin",
      grantedBy: "spoof_uid",
      active: true,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );
});
