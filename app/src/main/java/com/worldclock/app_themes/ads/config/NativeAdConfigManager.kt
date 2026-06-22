package com.worldclock.app_themes.ads.config

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import com.worldclock.app_themes.BuildConfig
import com.worldclock.app_themes.ads.config.models.NativeAdColorConfig
import com.worldclock.app_themes.ads.config.models.NativeAdSettingsConfig
import com.worldclock.app_themes.ads.config.models.NativeAdUnitConfig
import com.worldclock.app_themes.ads.config.models.NativePlacement
import com.worldclock.app_themes.ads.config.models.RemoteNativeConfigWrapper
import timber.log.Timber

class NativeAdConfigManager(
    firebaseRemoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
) : BaseRemoteConfigManager<RemoteNativeConfigWrapper>(firebaseRemoteConfig, "Config_v7") {
    private val configKey = "Config_v7"
    companion object {
        private val jsonParser by lazy {
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
        }
    }

    init {
        configData = RemoteNativeConfigWrapper()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0 else 300)
            .build()

        firebaseRemoteConfig.setConfigSettingsAsync(configSettings)
            .addOnCompleteListener { /*fetchRemoteConfig()*/ }
            .addOnFailureListener {
                Timber.e(it, "Remote Config settings init failed")
            }
    }

    fun fetchNativeConfig() {
        fetchConfig()
    }

    override fun parseJson(json: String): RemoteNativeConfigWrapper? {
        return try {
            val rootObj = jsonParser.parseToJsonElement(json).jsonObject
            val nativeJsonRaw = rootObj["native"] ?: rootObj["ads"]?.jsonObject?.get("native")
            val nativeJson = nativeJsonRaw?.jsonObject?.let { normalizeNativeJson(it) }
                ?: return RemoteNativeConfigWrapper()
            val native = nativeJson?.let {
                jsonParser.decodeFromString<NativeAdSettingsConfig>(it.toString())
            }
                ?: return RemoteNativeConfigWrapper()
            RemoteNativeConfigWrapper(native_ads = native)
        } catch (e: Exception) {
            Timber.e(e, "NativeConfig JSON parse error")
            null
        }
    }

    fun isNativeVisible(target: String, position: String): Boolean {
        if (position.equals("top", ignoreCase = true) && !isSplashScreen(target)) return false
        val config = getResolvedNativeConfig() ?: return false
        val placementUnit = config.resolvePlacementUnit(target, position) ?: return false
        val unit = config.default_config.mergeWith(placementUnit)
        val size = unit.size ?: config.default_config.size ?: 1
        val unitEnabled = unit.enabled ?: if (size > 0) 1 else 0
        return config.enabled == 1 && unitEnabled == 1 && size > 0
    }

    fun shouldNativePreload(target: String, position: String): Boolean {
        if (position.equals("top", ignoreCase = true) && !isSplashScreen(target)) return false
        val config = getResolvedNativeConfig() ?: return false
        return config.preload == 1 && config.resolveUnit(target, position).preload == 1
    }

    fun getNativeAdSize(target: String, position: String): Int {
        if (position.equals("top", ignoreCase = true) && !isSplashScreen(target)) return 0
        if (isIntroScreen(target) && (position.equals("fullScreen", ignoreCase = true) || position.startsWith("full_screen", ignoreCase = true))) {
            return 1
        }
        val config = getResolvedNativeConfig() ?: return 0
        val placementUnit = config.resolvePlacementUnit(target, position)
        val resolvedSize = config.resolveUnit(target, position).size
        if (placementUnit?.size == null && isIntroScreen(target)) {
            return 1
        }
        return resolvedSize ?: 2
    }

    fun getNativeAdColorConfig(target: String, position: String): NativeAdColorConfig? {
        val config = getResolvedNativeConfig() ?: return null
        return config.resolveUnit(target, position).color_config
    }

    fun getNativeAdFrequency(target: String, position: String): Int {
        val config = getResolvedNativeConfig() ?: return 3
        return config.resolveUnit(target, position).showAfter ?: 3
    }

    fun getNativeAdLimit(target: String, position: String): Int {
        val config = getResolvedNativeConfig() ?: return 2
        return config.resolveUnit(target, position).nativeLimit ?: 2
    }

    fun getNativeAdNetwork(target: String, position: String): Int {
        val config = getResolvedNativeConfig() ?: return 1
        return config.resolveUnit(target, position).network ?: 1
    }

    fun isNativeWaterfallEnabled(): Boolean {
        return getResolvedNativeConfig()?.waterfall == 1
    }

    private fun getResolvedNativeConfig(): NativeAdSettingsConfig? {
        getConfig()?.native_ads?.let { return it }

        // Fallback path: parse already-activated value synchronously so first render
        // doesn't lose style when async fetch/parsing hasn't completed yet.
        return runCatching {
            val json = firebaseRemoteConfig.getString(configKey)
            if (json.isBlank()) return@runCatching null
            parseJson(json)?.also { configData = it }?.native_ads
        }.getOrNull()
    }

    private fun NativeAdSettingsConfig.resolveUnit(
        target: String,
        position: String
    ): NativeAdUnitConfig {
        return default_config.mergeWith(resolvePlacementUnit(target, position))
    }

    private fun NativeAdSettingsConfig.resolvePlacementUnit(
        target: String,
        position: String
    ): NativeAdUnitConfig? {
        val placement = findNativePlacement(target)
            ?: findFlatNativePlacement(target, position)
        val unitValue = when (position) {
            "top" -> placement?.top
            "bottom" -> placement?.bottom
            "center" -> placement?.center
            "fullScreen", "full_screen" -> placement?.fullScreen
            "recycler" -> placement?.recycler
            else -> null
        } ?: placement?.value
        return unitValue?.let {
            NativeAdUnitConfig(
                enabled = if (it > 0) 1 else 0,
                size = if (it > 0) it else null,
                network = placement?.network
            )
        }?.copy(color_config = placement?.style?.normalized())
    }

    private fun NativeAdSettingsConfig.findNativePlacement(screenName: String): NativePlacement? {
        for (candidate in screenCandidates(screenName)) {
            placements[candidate]?.let { return it }
        }
        return null
    }

    private fun NativeAdSettingsConfig.findFlatNativePlacement(
        screenName: String,
        position: String
    ): NativePlacement? {
        val candidates = linkedSetOf<String>()
        screenCandidates(screenName).forEach { candidate ->
            val normalized = normalizeKey(candidate.removeSuffix("FragmentScreen").removeSuffix("Screen"))
            candidates += "${normalized}_${normalizeKey(position)}"
        }
        for (candidate in candidates) {
            placements[candidate]?.let { return it }
        }
        val normalizedCandidates = candidates.map { candidate -> normalizeKey(candidate) }.toSet()
        placements.forEach { (key, value) ->
            if (normalizeKey(key) in normalizedCandidates) return value
        }
        return null
    }

    private fun screenCandidates(screenName: String): List<String> {
        val candidates = linkedSetOf(screenName)
        if (screenName.endsWith("FragmentScreen")) {
            candidates += screenName.replace("FragmentScreen", "Screen")
            candidates += screenName.removeSuffix("FragmentScreen")
        }
        if (screenName == "OnBoardingScreen") {
            candidates += RemoteScreens.INTRO_SCREEN
        }
        if (screenName == "DescribeScreen") {
            candidates += "ApplyScreen"
        }
        if (screenName == "LiveWallpaperScreen") {
            candidates += "LiveScreen"
        }
        when (screenName) {
            "HomeFragmentScreen", "DashboardFragmentScreen" -> candidates += "MainScreen"
            "MenuScreen", "MenuFragmentScreen" -> candidates += "SettingsScreen"
        }
        return candidates.toList()
    }

    private fun isIntroScreen(screenName: String): Boolean {
        return screenCandidates(screenName).any { candidate ->
            candidate == "OnBoardingScreen" || candidate == RemoteScreens.INTRO_SCREEN
        }
    }

    private fun isSplashScreen(screenName: String): Boolean {
        return screenCandidates(screenName).any { candidate ->
            candidate == RemoteScreens.SPLASH_SCREEN || candidate == "SplashScreen"
        }
    }

    private fun normalizeKey(value: String): String {
        return value
            .replace(Regex("([a-z0-9])([A-Z])"), "$1_$2")
            .replace("-", "_")
            .replace(" ", "_")
            .lowercase()
    }

    private fun NativeAdUnitConfig?.mergeWith(override: NativeAdUnitConfig?): NativeAdUnitConfig {
        if (override == null) return this ?: NativeAdUnitConfig()
        return NativeAdUnitConfig(
            enabled = override.enabled ?: this?.enabled,
            size = override.size ?: this?.size,
            network = override.network ?: this?.network,
            showAfter = override.showAfter ?: this?.showAfter,
            nativeLimit = override.nativeLimit ?: this?.nativeLimit,
            color_config = this?.color_config.mergeWith(override.color_config)
        )
    }

    private fun NativeAdColorConfig?.mergeWith(override: NativeAdColorConfig?): NativeAdColorConfig? {
        if (this == null && override == null) return null
        if (this == null) return override
        if (override == null) return this

        return NativeAdColorConfig(
            mode = override.mode ?: mode,
            backgroundColorHex = override.normalized().backgroundColorHex
                ?: normalized().backgroundColorHex,
            cornerRadiusDp = override.normalized().cornerRadiusDp ?: normalized().cornerRadiusDp,
            strokeWidthDp = override.normalized().strokeWidthDp ?: normalized().strokeWidthDp,
            strokeColorHex = override.normalized().strokeColorHex ?: normalized().strokeColorHex,
            headlineColorHex = override.normalized().headlineColorHex
                ?: normalized().headlineColorHex,
            bodyTextColorHex = override.normalized().bodyTextColorHex
                ?: normalized().bodyTextColorHex,
            ctaBackgroundColorHex = override.normalized().ctaBackgroundColorHex
                ?: normalized().ctaBackgroundColorHex,
            ctaTextColorHex = override.normalized().ctaTextColorHex ?: normalized().ctaTextColorHex,
            ctaCornerRadiusDp = override.normalized().ctaCornerRadiusDp
                ?: normalized().ctaCornerRadiusDp,
            ctaTextSizeSp = override.normalized().ctaTextSizeSp ?: normalized().ctaTextSizeSp
        )
    }

    private fun NativeAdColorConfig.normalized(): NativeAdColorConfig {
        return NativeAdColorConfig(
            mode = mode,
            backgroundColorHex = backgroundColorHex ?: darkBackgroundColorHex
            ?: lightBackgroundColorHex,
            cornerRadiusDp = cornerRadiusDp ?: cornerRadius,
            strokeWidthDp = strokeWidthDp ?: strokeWidth,
            strokeColorHex = strokeColorHex ?: darkStrokeColorHex ?: lightStrokeColorHex,
            headlineColorHex = headlineColorHex ?: darkHeadlineColorHex ?: lightHeadlineColorHex,
            bodyTextColorHex = bodyTextColorHex ?: darkBodyColorHex ?: lightBodyColorHex,
            ctaBackgroundColorHex = ctaBackgroundColorHex
                ?: ctaBgColorHex
                ?: darkCtaBackgroundColorHex
                ?: lightCtaBackgroundColorHex,
            ctaTextColorHex = ctaTextColorHex ?: darkCtaTextColorHex ?: lightCtaTextColorHex,
            ctaCornerRadiusDp = ctaCornerRadiusDp ?: ctaRadius,
            ctaTextSizeSp = ctaTextSizeSp ?: ctaTextSize
        )
    }

    private fun normalizeNativeJson(nativeObj: JsonObject): JsonObject {
        val defaultStyle = nativeObj["default_style"]?.jsonObject
        val normalizedDefault = defaultStyle?.let { style ->
            if ("color_config" in style) {
                style
            } else {
                buildJsonObject {
                    style.forEach { (k, v) -> put(k, v) }
                    val colorConfig = buildJsonObject {
                        style.forEach { (k, v) -> put(k, v) }
                    }
                    put("color_config", colorConfig)
                }
            }
        }

        val placements = nativeObj["placements"]?.jsonObject?.let { placementsObj ->
            buildJsonObject {
                placementsObj.forEach { (key, value) ->
                    val obj = value.jsonObject
                    val style = obj["style"]?.jsonObject
                    val normalizedPlacement = if (style == null) {
                        obj
                    } else {
                        buildJsonObject {
                            obj.forEach { (k, v) ->
                                if (k != "style") put(k, v)
                            }
                            // add normalized style keys that our model expects directly
                            put("style", buildJsonObject {
                                style.forEach { (k, v) -> put(k, v) }
                                style["cta_bg_color"]?.let { put("cta_background_color", it) }
                                style["cta_text_size"]?.let { put("cta_text_size_sp", it) }
                                style["cta_radius"]?.let { put("cta_corner_radius_dp", it) }
                                style["corner_radius"]?.let { put("corner_radius_dp", it) }
                                style["stroke_width"]?.let { put("stroke_width_dp", it) }
                            })
                        }
                    }
                    put(key, normalizedPlacement)
                }
            }
        }

        return buildJsonObject {
            nativeObj.forEach { (k, v) ->
                when (k) {
                    "default_style" -> {
                        if (normalizedDefault != null) put(k, normalizedDefault)
                    }
                    "placements" -> {
                        if (placements != null) put(k, placements)
                    }
                    else -> put(k, v)
                }
            }
        }
    }
}

@Serializable
private data class NativeConfigEnvelope(
    @SerialName("ads") val ads: NativeAdsBlock? = null
)

@Serializable
private data class NativeAdsBlock(
    @SerialName("native") val native: NativeAdSettingsConfig? = null
)
