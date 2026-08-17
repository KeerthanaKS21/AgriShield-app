package com.agrishield.app.data.firebase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File

class FirebaseStorageManager {

    private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    suspend fun uploadDiagnosisImage(userId: String, imageUri: Uri): Result<String> {
        return try {
            val filename = "diag_${System.currentTimeMillis()}.jpg"
            val storageRef = storage.reference
                .child("users")
                .child(userId)
                .child("diagnoses")
                .child(filename)

            val uploadTask = storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadLocalFile(userId: String, localFilePath: String): Result<String> {
        return try {
            val file = File(localFilePath)
            if (!file.exists()) {
                return Result.failure(Exception("File does not exist: $localFilePath"))
            }
            val filename = file.name
            val storageRef = storage.reference
                .child("users")
                .child(userId)
                .child("diagnoses")
                .child(filename)

            storageRef.putFile(Uri.fromFile(file)).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
