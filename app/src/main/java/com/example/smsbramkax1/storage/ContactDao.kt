package com.example.smsbramkax1.storage

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.example.smsbramkax1.data.Contact

@Dao
interface ContactDao {
    
    @Query("SELECT * FROM contacts_cache ORDER BY name ASC NULLS LAST")
    fun getAllContacts(): Flow<List<Contact>>
    
    @Query("SELECT * FROM contacts_cache WHERE name LIKE :searchPattern OR phoneNumber LIKE :searchPattern ORDER BY name ASC NULLS LAST")
    fun searchContacts(searchPattern: String): Flow<List<Contact>>
    
    @Query("SELECT * FROM contacts_cache WHERE contactId = :contactId")
    suspend fun getContactByContactId(contactId: String): Contact?
    
    @Query("SELECT * FROM contacts_cache WHERE id = :id")
    suspend fun getContactById(id: Long): Contact?
    
    @Query("SELECT * FROM contacts_cache WHERE phoneNumber = :phoneNumber")
    suspend fun getContactByPhoneNumber(phoneNumber: String): Contact?
    
    @Query("SELECT COUNT(*) FROM contacts_cache")
    suspend fun getContactCount(): Int
    
    @Query("SELECT COUNT(*) FROM contacts_cache WHERE phoneNumber IS NOT NULL AND phoneNumber != ''")
    suspend fun getContactsWithPhoneCount(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<Contact>)
    
    @Update
    suspend fun updateContact(contact: Contact)
    
    @Delete
    suspend fun deleteContact(contact: Contact)
    
    @Query("DELETE FROM contacts_cache WHERE contactId = :contactId")
    suspend fun deleteContactByContactId(contactId: String)
    
    @Query("DELETE FROM contacts_cache")
    suspend fun deleteAllContacts()
    
    @Query("DELETE FROM contacts_cache WHERE lastUpdated < :beforeTime")
    suspend fun deleteOldContacts(beforeTime: Long)
    
    @Query("SELECT DISTINCT name FROM contacts_cache WHERE name IS NOT NULL AND name != '' ORDER BY name ASC")
    fun getUniqueNames(): Flow<List<String>>
    
    @Query("SELECT * FROM contacts_cache WHERE phoneNumber IS NOT NULL AND phoneNumber != '' ORDER BY name ASC NULLS LAST")
    fun getContactsWithPhone(): Flow<List<Contact>>
}