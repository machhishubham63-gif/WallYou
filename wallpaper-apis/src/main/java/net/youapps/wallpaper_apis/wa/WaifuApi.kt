package net.youapps.wallpaper_apis.wa

import kotlinx.serialization.Serializable
import net.youapps.wallpaper_apis.RetrofitHelper
import net.youapps.wallpaper_apis.Wallpaper
import net.youapps.wallpaper_apis.WallpaperApi
import retrofit2.http.GET
import retrofit2.http.Query

// 1. The JSON Data Shape (How Waifu.im sends data)
@Serializable
data class WaifuResponse(val images: List<WaifuImage>)

@Serializable
data class WaifuImage(
    val url: String,
    val width: Int,
    val height: Int
)

// 2. The Network Command (Asking for SFW, multiple images)
interface WaifuService {
    @GET("search")
    suspend fun getImages(
        @Query("is_nsfw") isNsfw: Boolean = false,
        @Query("many") many: Boolean = true
    ): WaifuResponse
}

// 3. The Adapter Logic (Translating it for WallYou)
class WaifuApi : WallpaperApi() {
    override val name = "Anime (Waifu.im)"
    override val baseUrl = "https://api.waifu.im/"

    // Initialize the network connection
    val api = RetrofitHelper.create<WaifuService>(baseUrl)

    override suspend fun getWallpapers(page: Int): List<Wallpaper> {
        return try {
            // Fetch the images from the Waifu.im server
            val response = api.getImages()

            // Convert them into WallYou's standard format
            response.images.map { image ->
                Wallpaper(
                    imgSrc = image.url,
                    title = "Anime Art",
                    thumb = image.url, 
                    resolution = "${image.width}x${image.height}"
                )
            }
        } catch (e: Exception) {
            emptyList() // If the network fails, return an empty list safely
        }
    }

    override suspend fun getRandomWallpaperUrl(): String? = getWallpapers(1).randomOrNull()?.imgSrc
}
