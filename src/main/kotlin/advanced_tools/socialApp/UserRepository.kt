package advanced_tools.socialApp

interface UserRepository {
    suspend fun fetchProfile(userId: String): UserProfile?
    suspend fun updateProfile(userProfile: UserProfile): Boolean
}
