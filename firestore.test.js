const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_101";
const BOB_UID = "bob_uid_202";
const CHARLIE_UID = "charlie_uid_303";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validElevenPlayers() {
  return [
    "gk_1",
    "def_1",
    "def_2",
    "def_3",
    "def_4",
    "mid_1",
    "mid_2",
    "mid_3",
    "fwd_1",
    "fwd_2",
    "fwd_3",
  ];
}

function makeClubPayload(uid, overrides = {}) {
  const now = new Date(Date.now() - 5000);
  return {
    ownerId: uid,
    clubName: "Apex Rovers",
    managerAlias: "Coach Alex",
    formation: "4-3-3",
    crestColorHex: "#10B981",
    playerIds: [],
    budgetCap: 1000,
    budgetSpent: 0,
    budgetRemaining: 1000,
    overallRating: 0,
    attackRating: 0,
    midfieldRating: 0,
    defenseRating: 0,
    ready: false,
    wins: 0,
    draws: 0,
    losses: 0,
    points: 0,
    goalsFor: 0,
    goalsAgainst: 0,
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

function makeMatchPayload(homeUid, awayUid, overrides = {}) {
  const now = new Date(Date.now() - 2000);
  return {
    homeOwnerId: homeUid,
    awayOwnerId: awayUid,
    participantIds: [homeUid, awayUid],
    homeClubName: "Apex Rovers",
    awayClubName: "Metro United",
    homeScore: 2,
    awayScore: 1,
    homePenalties: 0,
    awayPenalties: 0,
    penaltiesUsed: false,
    winnerOwnerId: homeUid,
    winnerClubName: "Apex Rovers",
    manOfTheMatch: "Mateo Silva",
    homePossession: 56,
    homeShots: 12,
    awayShots: 8,
    keyEvents: [
      "18' GOAL! Mateo Silva fires into the bottom corner.",
      "54' GOAL! Metro United equalize from a set piece.",
      "81' GOAL! Julian Vance curls the winner!",
    ],
    status: "completed",
    createdAt: now,
    ...overrides,
  };
}

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

test("Unauthenticated user cannot read or write clubs or matches", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("clubs").where("ready", "==", true).get());
  await assertFails(unauthDb.collection("clubs").doc(ALICE_UID).set(makeClubPayload(ALICE_UID)));
  await assertFails(unauthDb.collection("matches").where("participantIds", "array-contains", ALICE_UID).get());
});

test("Club create, squad update, and result update succeed for owner; invalid payloads fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  // Valid create (no @DocumentId `id` field)
  await assertSucceeds(
    aliceDb.collection("clubs").doc(ALICE_UID).set(makeClubPayload(ALICE_UID))
  );

  // Reject create with @DocumentId `id` field
  await assertFails(
    aliceDb.collection("clubs").doc(BOB_UID).set({
      id: BOB_UID,
      ...makeClubPayload(BOB_UID),
    })
  );

  // Reject ready=true with fewer than 11 players
  await assertFails(
    aliceDb.collection("clubs").doc(ALICE_UID).update({
      ready: true,
      playerIds: ["gk_1", "def_1"],
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Valid squad update with 11 players & balanced budget
  await assertSucceeds(
    aliceDb.collection("clubs").doc(ALICE_UID).update({
      clubName: "Apex Rovers FC",
      formation: "4-4-2",
      playerIds: validElevenPlayers(),
      budgetSpent: 845,
      budgetRemaining: 155,
      overallRating: 84,
      attackRating: 85,
      midfieldRating: 83,
      defenseRating: 84,
      ready: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Valid match result update (Action 2)
  await assertSucceeds(
    aliceDb.collection("clubs").doc(ALICE_UID).update({
      wins: 1,
      draws: 0,
      losses: 0,
      points: 3,
      goalsFor: 2,
      goalsAgainst: 1,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject shadow field or immutable createdAt tampering
  await assertFails(
    aliceDb.collection("clubs").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 200),
    })
  );
  await assertFails(
    aliceDb.collection("clubs").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 100),
      updatedAt: new Date(Date.now() - 100),
    })
  );
});

test("Club isolation and query alignment", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("clubs").doc(ALICE_UID).set(
      makeClubPayload(ALICE_UID, {
        playerIds: validElevenPlayers(),
        budgetSpent: 800,
        budgetRemaining: 200,
        overallRating: 83,
        attackRating: 84,
        midfieldRating: 82,
        defenseRating: 83,
        ready: true,
      })
    );
    await db.collection("clubs").doc(BOB_UID).set(
      makeClubPayload(BOB_UID, { clubName: "Private Draft", ready: false })
    );
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  // Ready clubs query succeeds
  await assertSucceeds(aliceDb.collection("clubs").where("ready", "==", true).get());
  // Unfiltered clubs query fails
  await assertFails(aliceDb.collection("clubs").get());

  // Alice cannot read Bob's non-ready draft club, nor update Bob's club
  await assertFails(aliceDb.collection("clubs").doc(BOB_UID).get());
  await assertFails(
    aliceDb.collection("clubs").doc(BOB_UID).update({
      clubName: "Hacked FC",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Bob can read his own draft club
  await assertSucceeds(bobDb.collection("clubs").doc(BOB_UID).get());
});

test("MatchRecord creation, participant scoping, and immutability", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("clubs").doc(ALICE_UID).set(
      makeClubPayload(ALICE_UID, {
        playerIds: validElevenPlayers(),
        budgetSpent: 800,
        budgetRemaining: 200,
        overallRating: 84,
        ready: true,
      })
    );
    await db.collection("clubs").doc(BOB_UID).set(
      makeClubPayload(BOB_UID, {
        clubName: "Metro United",
        playerIds: validElevenPlayers(),
        budgetSpent: 780,
        budgetRemaining: 220,
        overallRating: 82,
        ready: true,
      })
    );
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();

  // Valid match creation by Alice against Bob
  await assertSucceeds(
    aliceDb.collection("matches").doc("match_1").set(makeMatchPayload(ALICE_UID, BOB_UID))
  );

  // Reject self-match
  await assertFails(
    aliceDb.collection("matches").doc("match_self").set(
      makeMatchPayload(ALICE_UID, ALICE_UID, { winnerOwnerId: ALICE_UID })
    )
  );

  // Reject match against nonexistent rival club
  await assertFails(
    aliceDb.collection("matches").doc("match_ghost").set(
      makeMatchPayload(ALICE_UID, "nonexistent_uid")
    )
  );

  // Participants can query their matches; non-participant cannot get; unfiltered fails
  await assertSucceeds(
    aliceDb.collection("matches").where("participantIds", "array-contains", ALICE_UID).get()
  );
  await assertSucceeds(
    bobDb.collection("matches").where("participantIds", "array-contains", BOB_UID).get()
  );
  await assertFails(charlieDb.collection("matches").doc("match_1").get());
  await assertFails(aliceDb.collection("matches").get());

  // Terminal state lock: cannot update or delete completed match
  await assertFails(aliceDb.collection("matches").doc("match_1").update({ homeScore: 9 }));
  await assertFails(aliceDb.collection("matches").doc("match_1").delete());
});
