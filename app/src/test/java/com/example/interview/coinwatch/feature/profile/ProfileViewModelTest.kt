package com.example.interview.coinwatch.feature.profile

import app.cash.turbine.test
import com.example.interview.coinwatch.domain.model.UserProfile
import com.example.interview.coinwatch.domain.repository.ProfileRepository
import com.example.interview.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertIs

/**
 * Unit tests for [ProfileViewModel].
 *
 * Design decisions
 * ----------------
 * • [ProfileRepository] is replaced by a MockK stub so every test is fully hermetic.
 * • [MainDispatcherRule] installs a [kotlinx.coroutines.test.StandardTestDispatcher] as
 *   [kotlinx.coroutines.Dispatchers.Main].  The same dispatcher is injected as the
 *   `@IoDispatcher` so [androidx.lifecycle.viewModelScope] and the coroutine under test share
 *   one scheduler — no wall-clock time passes and execution is deterministic.
 * • [runTest] advances virtual time automatically; Turbine's [test] drives Flow collection.
 * • Every test verifies exactly one logical invariant to keep failure messages unambiguous.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: ProfileRepository
    private lateinit var viewModel: ProfileViewModel

    private val fakeProfile = UserProfile(
        name = "Lal Laadle",
        email = "laadle@gmail.com",
        bio = "hat ja laadle",
        avatarUrl = "https://i.pravatar.cc/300",
    )

    @Before
    fun setUp() {
        repository = mockk()
        viewModel = ProfileViewModel(
            profileRepository = repository,
            ioDispatcher = mainDispatcherRule.testDispatcher,
        )
    }

    // ── Initial state ──────────────────────────────────────────────────────────

    @Test
    fun `initial ui state is Idle`() = runTest(mainDispatcherRule.testDispatcher) {
        assertEquals(ProfileUiState.Idle, viewModel.uiState.value)
    }

    // ── Happy path ─────────────────────────────────────────────────────────────

    @Test
    fun `fetchProfile emits Loading then Success when repository returns a profile`() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { repository.getUserProfile() } returns fakeProfile

            viewModel.uiState.test {
                assertEquals(ProfileUiState.Idle, awaitItem())

                viewModel.fetchProfile()

                assertEquals(ProfileUiState.Loading, awaitItem())
                assertEquals(ProfileUiState.Success(fakeProfile), awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `success state carries the exact profile returned by the repository`() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { repository.getUserProfile() } returns fakeProfile

            viewModel.fetchProfile()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertIs<ProfileUiState.Success>(state)
            assertEquals(fakeProfile.name, state.profile.name)
            assertEquals(fakeProfile.email, state.profile.email)
            assertEquals(fakeProfile.bio, state.profile.bio)
            assertEquals(fakeProfile.avatarUrl, state.profile.avatarUrl)
        }

    // ── Error path ─────────────────────────────────────────────────────────────

    @Test
    fun `fetchProfile emits Loading then Error when repository throws`() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { repository.getUserProfile() } throws Exception("Network error")

            viewModel.uiState.test {
                assertEquals(ProfileUiState.Idle, awaitItem())

                viewModel.fetchProfile()

                assertEquals(ProfileUiState.Loading, awaitItem())
                val error = awaitItem()
                assertIs<ProfileUiState.Error>(error)
                assertEquals("Network error", error.message)
                assertEquals(1, error.errorCount)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `error message defaults to 'Unknown error' when exception message is null`() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { repository.getUserProfile() } throws Exception()

            viewModel.fetchProfile()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertIs<ProfileUiState.Error>(state)
            assertEquals("Unknown error", state.message)
        }

    // ── Error count accumulation ───────────────────────────────────────────────

    @Test
    fun `errorCount increments by one for each consecutive failed fetch`() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { repository.getUserProfile() } throws Exception("error")

            viewModel.uiState.test {
                assertEquals(ProfileUiState.Idle, awaitItem())

                repeat(3) { iteration ->
                    viewModel.fetchProfile()
                    assertEquals(ProfileUiState.Loading, awaitItem())
                    val error = awaitItem()
                    assertIs<ProfileUiState.Error>(error)
                    assertEquals(iteration + 1, error.errorCount)
                }

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `errorCount persists across a success and continues incrementing on the next error`() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { repository.getUserProfile() } throws Exception("first error")

            viewModel.uiState.test {
                assertEquals(ProfileUiState.Idle, awaitItem())

                viewModel.fetchProfile()
                assertEquals(ProfileUiState.Loading, awaitItem())
                val firstError = awaitItem()
                assertIs<ProfileUiState.Error>(firstError)
                assertEquals(1, firstError.errorCount)

                coEvery { repository.getUserProfile() } returns fakeProfile
                viewModel.fetchProfile()
                assertEquals(ProfileUiState.Loading, awaitItem())
                assertEquals(ProfileUiState.Success(fakeProfile), awaitItem())

                coEvery { repository.getUserProfile() } throws Exception("second error")
                viewModel.fetchProfile()
                assertEquals(ProfileUiState.Loading, awaitItem())
                val secondError = awaitItem()
                assertIs<ProfileUiState.Error>(secondError)
                assertEquals(2, secondError.errorCount)

                cancelAndIgnoreRemainingEvents()
            }
        }

    // ── Retry and refresh flows ────────────────────────────────────────────────

    @Test
    fun `retry after error transitions through Loading and resolves to Success`() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { repository.getUserProfile() } throws Exception("transient error")

            viewModel.uiState.test {
                assertEquals(ProfileUiState.Idle, awaitItem())

                viewModel.fetchProfile()
                assertEquals(ProfileUiState.Loading, awaitItem())
                assertIs<ProfileUiState.Error>(awaitItem())

                coEvery { repository.getUserProfile() } returns fakeProfile
                viewModel.fetchProfile()
                assertEquals(ProfileUiState.Loading, awaitItem())
                assertEquals(ProfileUiState.Success(fakeProfile), awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `refresh after success transitions through Loading and resolves to the updated profile`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val updatedProfile = fakeProfile.copy(name = "Updated Name")
            coEvery { repository.getUserProfile() } returns fakeProfile

            viewModel.uiState.test {
                assertEquals(ProfileUiState.Idle, awaitItem())

                viewModel.fetchProfile()
                assertEquals(ProfileUiState.Loading, awaitItem())
                assertEquals(ProfileUiState.Success(fakeProfile), awaitItem())

                coEvery { repository.getUserProfile() } returns updatedProfile
                viewModel.fetchProfile()
                assertEquals(ProfileUiState.Loading, awaitItem())
                assertEquals(ProfileUiState.Success(updatedProfile), awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
        }

    // ── Repository interaction verification ───────────────────────────────────

    @Test
    fun `repository is invoked exactly once per fetchProfile call`() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { repository.getUserProfile() } returns fakeProfile

            viewModel.fetchProfile()
            advanceUntilIdle()

            viewModel.fetchProfile()
            advanceUntilIdle()

            coVerify(exactly = 2) { repository.getUserProfile() }
        }

    // ── Concurrent fetch behaviour ─────────────────────────────────────────────

    /**
     * When [fetchProfile] is called a second time before the first coroutine has been dispatched,
     * both coroutines are enqueued on the same [kotlinx.coroutines.test.StandardTestDispatcher].
     * [advanceUntilIdle] drains them in FIFO order; the final observable state is the result of
     * the last coroutine that writes to [_uiState], which is the second launch.
     *
     * This test documents the current (no-debounce) contract so that any future change —
     * e.g. adding job cancellation or a mutex — surfaces as a deliberate test update.
     */
    @Test
    fun `two rapid fetchProfile calls each invoke the repository once`() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { repository.getUserProfile() } returns fakeProfile

            viewModel.fetchProfile()
            viewModel.fetchProfile()
            advanceUntilIdle()

            coVerify(exactly = 2) { repository.getUserProfile() }
        }

    @Test
    fun `two rapid fetchProfile calls both complete and the final state is Success`() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { repository.getUserProfile() } returns fakeProfile

            viewModel.fetchProfile()
            viewModel.fetchProfile()
            advanceUntilIdle()

            assertIs<ProfileUiState.Success>(viewModel.uiState.value)
        }

    // ── State does not regress after terminal states ───────────────────────────

    @Test
    fun `state does not revert to Idle after a successful fetch`() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { repository.getUserProfile() } returns fakeProfile

            viewModel.fetchProfile()
            advanceUntilIdle()

            assertIs<ProfileUiState.Success>(viewModel.uiState.value)
        }

    @Test
    fun `state does not revert to Idle after a failed fetch`() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { repository.getUserProfile() } throws Exception("error")

            viewModel.fetchProfile()
            advanceUntilIdle()

            assertIs<ProfileUiState.Error>(viewModel.uiState.value)
        }
}
