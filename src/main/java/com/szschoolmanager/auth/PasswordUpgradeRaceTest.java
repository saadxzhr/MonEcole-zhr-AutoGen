// package com.szschoolmanager.auth;

// import com.szschoolmanager.auth.model.Utilisateur;
// import com.szschoolmanager.auth.repository.UtilisateurRepository;
// import com.szschoolmanager.auth.service.UtilisateurService;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.transaction.annotation.Transactional;

// import java.util.concurrent.CountDownLatch;
// import java.util.concurrent.ExecutorService;
// import java.util.concurrent.Executors;
// import java.util.concurrent.atomic.AtomicInteger;

// import static org.assertj.core.api.Assertions.assertThat;

// @SpringBootTest
// @Transactional
// class PasswordUpgradeRaceTest {

//     @Autowired
//     private UtilisateurService utilisateurService;

//     @Autowired
//     private UtilisateurRepository repository;

//     @Test
//     void shouldHandleConcurrentPasswordUpgrade() throws InterruptedException {
//         // Setup: Create user with plaintext password
//         Utilisateur user = Utilisateur.builder()
//             .username("testuser")
//             .password("plaintext123")
//             .role("USER")
//             .cin("TEST123")
//             .forceChangePassword(false)
//             .build();
//         repository.save(user);

//         // Simulate 10 concurrent logins
//         int threads = 10;
//         ExecutorService executor = Executors.newFixedThreadPool(threads);
//         CountDownLatch latch = new CountDownLatch(threads);
//         AtomicInteger successCount = new AtomicInteger(0);

//         for (int i = 0; i < threads; i++) {
//             executor.submit(() -> {
//                 try {
//                     boolean upgraded = utilisateurService
//                         .upgradePasswordIfNeeded("testuser", "plaintext123");
//                     if (upgraded) {
//                         successCount.incrementAndGet();
//                     }
//                 } finally {
//                     latch.countDown();
//                 }
//             });
//         }

//         latch.await();
//         executor.shutdown();

//         // Verify: Only ONE thread actually upgraded
//         assertThat(successCount.get()).isEqualTo(1);

//         // Verify: Password is now encoded
//         Utilisateur updated = repository.findByUsername("testuser").get();
//         assertThat(updated.getPassword()).startsWith("$2a$");
//     }
// }
// ```

// ---

// ## **Summary of Changes**

// | File | Action | Lines Affected |
// |------|--------|---------------|
// | `UtilisateurRepository.java` | **ADD** 4 lines | After line 18 |
// | `UtilisateurService.java` | **ADD** 18 lines | After line 33 |
// | `AuthenticationController.java` | **REPLACE** 6 lines | Lines 72-76 → 3 lines |

// **Total Impact**: +25 lines, -3 lines = **22 net lines added**

// ---

// ## **How It Works**

// ### **Before (Race Condition)**
// ```
// Thread A: Check encoded → FALSE
// Thread B: Check encoded → FALSE  ⚠️
// Thread A: Save encoded password
// Thread B: Save encoded password  ⚠️ (different hash!)
// ```

// ### **After (Thread-Safe)**
// ```
// Thread A: SELECT ... FOR UPDATE (locks row)
// Thread B: SELECT ... FOR UPDATE (blocks, waits...)
// Thread A: Check encoded → FALSE → upgrade → commit (releases lock)
// Thread B: SELECT ... FOR UPDATE (lock acquired)
// Thread B: Check encoded → TRUE → skip upgrade → commit