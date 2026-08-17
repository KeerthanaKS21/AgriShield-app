package com.agrishield.app.data.firebase

import com.agrishield.app.data.model.AlertItem
import com.agrishield.app.data.model.CareTask
import com.agrishield.app.data.model.Diagnosis
import com.agrishield.app.data.model.SoilData
import com.agrishield.app.data.model.User
import com.agrishield.app.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirestoreManager {

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // USER PROFILE
    suspend fun saveUserProfile(user: User): Result<Unit> {
        return try {
            db.collection(Constants.COLLECTION_USERS)
                .document(user.uid)
                .set(user)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(userId: String): Result<User?> {
        return try {
            val doc = db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get()
                .await()
            val user = doc.toObject(User::class.java)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // DIAGNOSES
    suspend fun saveDiagnosis(userId: String, diagnosis: Diagnosis): Result<Unit> {
        return try {
            db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_DIAGNOSES)
                .document(diagnosis.id)
                .set(diagnosis)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecentDiagnoses(userId: String, limit: Long = 10): Result<List<Diagnosis>> {
        return try {
            val snapshot = db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_DIAGNOSES)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            val list = snapshot.toObjects(Diagnosis::class.java)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // SOIL TESTS
    suspend fun saveSoilTest(userId: String, soilData: SoilData): Result<Unit> {
        return try {
            db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_SOIL)
                .document(soilData.id)
                .set(soilData)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLatestSoilTest(userId: String): Result<SoilData?> {
        return try {
            val snapshot = db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_SOIL)
                .orderBy("testedDate", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
            val item = snapshot.documents.firstOrNull()?.toObject(SoilData::class.java)
            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // CARE TASKS
    suspend fun saveCareTask(userId: String, task: CareTask): Result<Unit> {
        return try {
            db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_TIMELINE)
                .document(task.id)
                .set(task)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCareTasks(userId: String): Result<List<CareTask>> {
        return try {
            val snapshot = db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_TIMELINE)
                .orderBy("dueDateEpoch", Query.Direction.ASCENDING)
                .get()
                .await()
            val list = snapshot.toObjects(CareTask::class.java)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ALERTS
    suspend fun saveAlert(userId: String, alert: AlertItem): Result<Unit> {
        return try {
            db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_ALERTS)
                .document(alert.id)
                .set(alert)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAlerts(userId: String): Result<List<AlertItem>> {
        return try {
            val snapshot = db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_ALERTS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .await()
            val list = snapshot.toObjects(AlertItem::class.java)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
