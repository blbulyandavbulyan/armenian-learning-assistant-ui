package com.blbulyandavbulyan.larm.kmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
