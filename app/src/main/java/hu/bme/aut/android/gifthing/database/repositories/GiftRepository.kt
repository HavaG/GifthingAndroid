package hu.bme.aut.android.gifthing.database.repositories

import android.app.Application
import androidx.lifecycle.LiveData
import hu.bme.aut.android.gifthing.database.AppDatabase
import hu.bme.aut.android.gifthing.database.dao.GiftDao
import hu.bme.aut.android.gifthing.database.entities.Gift


class GiftRepository(application: Application) {
    private val mGiftDao: GiftDao
    private val mAllGifts: LiveData<List<Gift>>

    init {
        val db: AppDatabase = AppDatabase.getDatabase(application)
        mGiftDao = db.giftDao()
        mAllGifts = mGiftDao.getAll()
    }

    fun getAllGifts(): LiveData<List<Gift>> {
        return mAllGifts
    }

    fun getById(id: Int): LiveData<Gift> {
        return mGiftDao.getById(id)
    }

    fun insert(gift: Gift) {
        AppDatabase.databaseWriteExecutor.execute { mGiftDao.insert(gift) }
    }
}