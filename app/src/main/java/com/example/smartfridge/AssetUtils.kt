package com.example.smartfridge

import android.content.Context
import java.io.File
import java.io.FileOutputStream

fun Context.assetFilePath(name: String): String {
    val file = File(filesDir, name)
    if (!file.exists()) {
        assets.open(name).use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
    }
    return file.absolutePath
}
