package com.example.datastoredemo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StoreManager(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("store_sample")

        val STRING_KEY = stringPreferencesKey("STRING_KEY")
        val DOUBLE_KEY = doublePreferencesKey("DOUBLE_KEY")
        val FLOAT_KEY = floatPreferencesKey("FLOAT_KEY")
        val BOOLEAN_KEY = booleanPreferencesKey("BOOLEAN_KEY")
        val INT_KEY = intPreferencesKey("INT_KEY")
    }

    //存储字符串数据
    suspend fun saveString(str: String) {
        //因为我们已经对Context进行了扩展 所以我们可以直接使用
        context.dataStore.edit { preferences ->
            preferences[STRING_KEY] = str
        }
    }

    //读取字符串数据
    val stringData: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[STRING_KEY] ?: ""
    }

    //存储Double数据
    suspend fun saveDouble(d: Double) {
        context.dataStore.edit {
            it[DOUBLE_KEY] = d
        }
    }

    val doubleData: Flow<Double> = context.dataStore.data.map {
        it[DOUBLE_KEY] ?: 0.0
    }

    //存储Float数据
    suspend fun saveFloat(f: Float) {
        context.dataStore.edit {
            it[FLOAT_KEY] = f
        }
    }

    val floatData: Flow<Float> = context.dataStore.data.map {
        (it[FLOAT_KEY] ?: 0) as Float
    }

    //存储Int数据
    suspend fun saveInt(i: Int) {
        context.dataStore.edit {
            it[INT_KEY] = i
        }
    }

    val intData: Flow<Int> = context.dataStore.data.map {
        it[INT_KEY] ?: 0
    }

    //存储Boolean数据
    suspend fun saveBoolean(b: Boolean) {
        context.dataStore.edit {
            it[BOOLEAN_KEY] = b
        }
    }

    val booleanData: Flow<Boolean> = context.dataStore.data.map {
        it[BOOLEAN_KEY] ?: false
    }
}