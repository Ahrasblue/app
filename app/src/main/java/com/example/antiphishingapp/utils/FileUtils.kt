package com.example.antiphishingapp.utils

import android.graphics.Bitmap
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File

fun bitmapToMultipart(bitmap: Bitmap, paramName: String = "file"): MultipartBody.Part {
    val stream = ByteArrayOutputStream()
    try {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val byteArray = stream.toByteArray()
        val requestBody = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(paramName, "upload.jpg", requestBody)
    } finally {
        stream.close() // ✅ 경고 제거
    }
}

fun audioToMultipart(file: File, paramName: String = "media"): MultipartBody.Part {
    val requestBody = file.asRequestBody("audio/*".toMediaTypeOrNull()) // ✅ 확장함수 import 필요
    return MultipartBody.Part.createFormData(paramName, file.name, requestBody)
}