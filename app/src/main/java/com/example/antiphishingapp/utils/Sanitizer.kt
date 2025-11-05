package com.example.antiphishingapp.utils

import java.security.MessageDigest
import java.util.regex.Pattern

object Sanitizer {

    // 🔹 URL 추출용 정규식 (한글 도메인, https/http, www 지원)
    private val URL_PATTERN: Pattern = Pattern.compile(
        // (?i) 대소문자 무시, (?:https?://|www\.) 로 시작, 그 뒤 허용 문자들
        """(?i)\b(?:https?://|www\.)[\w\-.~:/?\[\]#@!$&'()*+,;=%\p{L}\p{N}]+""",
        Pattern.UNICODE_CHARACTER_CLASS
    )

    /** 📍 문자에서 URL 목록 추출 */
    fun extractUrls(text: String): List<String> {
        val matcher = URL_PATTERN.matcher(text)
        val urls = mutableListOf<String>()
        while (matcher.find()) {
            val url = matcher.group()?.trim()
            if (!url.isNullOrBlank()) urls.add(url)
        }
        return urls.distinct()
    }

    /** 📍 URL을 제거한 나머지 텍스트 반환 */
    fun removeUrls(text: String): String {
        return URL_PATTERN.matcher(text).replaceAll(" ").replace("\\s+".toRegex(), " ").trim()
    }

    /** 📍 SHA-256 해시 (단말 고유 salt 포함) */
    fun sha256Hash(input: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt.toByteArray(Charsets.UTF_8))
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    /** 📍 문장 단위 분리 (기본적인 한국어/영문 문장 구분) */
    fun splitToSentences(text: String): List<String> {
        return text.split(Regex("(?<=[.!?]|\\n|\\r|\\r\\n)\\s+"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
}
