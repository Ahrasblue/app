package com.example.antiphishingapp.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow

// ==========================================
// 1. 데이터 모델 (Entity) - 저장할 문자 형태
// ==========================================
@Entity(tableName = "sms_table")
data class SmsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String,
    val content: String,
    val receivedDate: Long,
    val riskScore: Int,
    val keywords: List<String>
)

// ==========================================
// 2. 리스트 변환기 (Converters) - 리스트를 문자열로 저장하기 위함
// ==========================================
class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String = Gson().toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }
}

// ==========================================
// 3. 데이터 접속 함수 (Dao) - 넣고 빼는 기능
// ==========================================
@Dao
interface SmsDao {
    // 덮어쓰기 모드: 같은 ID면 덮어씀 (여기선 ID 자동생성이라 계속 추가됨)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSms(sms: SmsEntity)

    // 최신 날짜 순으로 정렬해서 가져오기 (Flow를 써서 실시간 반영)
    @Query("SELECT * FROM sms_table ORDER BY receivedDate DESC")
    fun getAllRiskySms(): Flow<List<SmsEntity>>
}

// ==========================================
// 4. 데이터베이스 본체 (Database) - 설정 파일
// ==========================================
@Database(entities = [SmsEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun smsDao(): SmsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "antiphishing_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}