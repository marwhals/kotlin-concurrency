package advanced_tools.socialApp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue


/**
 * Track progress of coroutines
 * - Manipulate time
 * - Result coroutines manually
 * - Wait until idle time
 * ---> Very important for mobile developers
 *
 * TODO - MVC ---- M-V-VM
 */

@OptIn(ExperimentalCoroutinesApi::class)
class UserProfileViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    // code under test
    private val fakeUserRepo: UserRepository = object : UserRepository {
        private val profileMap = mutableMapOf(
            "1" to UserProfile("1", "Daniel", 99), "2" to UserProfile("2", "Batman", 34)
        )

        override suspend fun fetchProfile(userId: String): UserProfile? {
            delay(1000) // simulate network delay
            return profileMap[userId]
        }

        override suspend fun updateProfile(userProfile: UserProfile): Boolean {
            delay(500)
            if (userProfile.id in profileMap) {
                profileMap[userProfile.id] = userProfile
                return true
            }

            return false
        }
    }
    private val viewModel = UserProfileViewModel(fakeUserRepo, testScope)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load user profile should update user profile and loading status`() = testScope.runTest {
        // can run coroutines
        viewModel.loadUserProfile("1")
        runCurrent() // runs all pending tasks in the coroutine dispatcher

        assertTrue(viewModel.loading.value) // true at this point
        assertNull(viewModel.profile.value) // no profile loaded yet

        advanceTimeBy(1000) // moves the internal clock of the dispatcher
        runCurrent() // the coroutine is finished

        assertFalse(viewModel.loading.value) // screen has finished loading
        assertEquals(viewModel.profile.value?.name, "Daniel")
        assertEquals(viewModel.profile.value?.age, 99)
    }

    @Test
    fun `updateUserProfile should modify user profile and loading state`() = testScope.runTest {
        viewModel.loadUserProfile("1")
        advanceTimeBy(1000)

        viewModel.updateUserProfile("Name here", 99)

        runCurrent()

        assertTrue(viewModel.loading.value)

        advanceTimeBy(500)
        runCurrent()

        assertFalse(viewModel.loading.value) // screen has finished loading
        assertEquals(viewModel.profile.value?.name, "Name here")
        assertEquals(viewModel.profile.value?.age, 99)
    }

    @Test
    fun `load user profile and update user profile`() = testScope.runTest {
        viewModel.loadUserProfile("1")
        advanceUntilIdle()
        viewModel.updateUserProfile("Name here", 99)
        advanceUntilIdle()
        runCurrent()

        assertFalse(viewModel.loading.value) // screen has finished loading
        assertEquals(viewModel.profile.value?.name, "Name here")
        assertEquals(viewModel.profile.value?.age, 99)
    }
}