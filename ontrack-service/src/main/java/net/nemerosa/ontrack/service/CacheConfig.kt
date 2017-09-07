package net.nemerosa.ontrack.service

import com.github.benmanes.caffeine.cache.Caffeine
import net.nemerosa.ontrack.common.Caches
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
@EnableCaching
class CacheConfig {

    @Bean
    @Throws(Exception::class)
    fun cacheManager(): CacheManager {
        val o = SimpleCacheManager()
        o.setCaches(
                listOf(
                        // Cache for settings
                        CaffeineCache(
                                Caches.SETTINGS,
                                Caffeine.newBuilder()
                                        .maximumSize(1)
                                        .expireAfterAccess(10, TimeUnit.HOURS)
                                        .build()
                        ),
                        // Cache for projects
                        CaffeineCache(
                                Caches.PROJECTS,
                                Caffeine.newBuilder()
                                        // TODO Configurable
                                        .maximumSize(20)
                                        .build()
                        )
                )
        )
        return o
    }
}
