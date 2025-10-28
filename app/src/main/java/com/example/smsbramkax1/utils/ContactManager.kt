package com.example.smsbramkax1.utils

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import com.example.smsbramkax1.data.Contact
import com.example.smsbramkax1.storage.SmsDatabase
import com.example.smsbramkax1.storage.ContactDao
import com.example.smsbramkax1.utils.LogManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ContactManager private constructor(private val context: Context) {
    
    private val database: SmsDatabase = SmsDatabase.getDatabase(context)
    private val contactDao: ContactDao = database.contactDao()
    
    companion object {
        @Volatile
        private var INSTANCE: ContactManager? = null
        
        fun getInstance(context: Context): ContactManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ContactManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    fun getAllContacts() = contactDao.getAllContacts()
    
    suspend fun syncContacts(): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                if (!PermissionsManager(context).hasContactsPermission()) {
                    return@withContext Result.failure(SecurityException("Contacts permission not granted"))
                }
                
                val contacts = readSystemContacts()
                contactDao.deleteAllContacts()
                contactDao.insertContacts(contacts)
                
                LogManager.log("INFO", "ContactManager", "Contacts synchronized: ${contacts.size} contacts")
                Result.success(contacts.size)
            } catch (e: Exception) {
                LogManager.log("ERROR", "ContactManager", "Failed to sync contacts: ${e.message}")
                Result.failure(e)
            }
        }
    }
    
    private fun readSystemContacts(): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val contentResolver = context.contentResolver
        
        // Query for contacts with phone numbers
        val cursor: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
        
        cursor?.use { c ->
            val idColumn = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameColumn = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneColumn = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            
            while (c.moveToNext()) {
                val contactId = c.getString(idColumn)
                val name = c.getString(nameColumn)
                val phoneNumber = c.getString(phoneColumn)
                
                // Clean phone number
                val cleanPhone = cleanPhoneNumber(phoneNumber)
                
                if (cleanPhone.isNotEmpty()) {
                    val contact = Contact(
                        contactId = contactId,
                        name = name,
                        phoneNumber = cleanPhone
                    )
                    contacts.add(contact)
                }
            }
        }
        
        // Remove duplicates (same contactId, keep first occurrence)
        return contacts.distinctBy { it.contactId }
    }
    
    suspend fun searchContacts(query: String): List<Contact> {
        return try {
            if (query.isBlank()) {
                contactDao.getAllContacts().first()
            } else {
                val searchPattern = "%$query%"
                contactDao.searchContacts(searchPattern).first()
            }
        } catch (e: Exception) {
            LogManager.log("ERROR", "ContactManager", "Failed to search contacts: ${e.message}")
            emptyList()
        }
    }
    
    suspend fun getContactsWithPhone(): List<Contact> {
        return try {
            contactDao.getContactsWithPhone().first()
        } catch (e: Exception) {
            LogManager.log("ERROR", "ContactManager", "Failed to get contacts with phone: ${e.message}")
            emptyList()
        }
    }
    
    suspend fun getContactByPhoneNumber(phoneNumber: String): Contact? {
        return try {
            val cleanPhone = cleanPhoneNumber(phoneNumber)
            contactDao.getContactByPhoneNumber(cleanPhone)
        } catch (e: Exception) {
            LogManager.log("ERROR", "ContactManager", "Failed to get contact by phone: ${e.message}")
            null
        }
    }
    
    suspend fun getContactCount(): Int {
        return try {
            contactDao.getContactCount()
        } catch (e: Exception) {
            LogManager.log("ERROR", "ContactManager", "Failed to get contact count: ${e.message}")
            0
        }
    }
    
    suspend fun getContactsWithPhoneCount(): Int {
        return try {
            contactDao.getContactsWithPhoneCount()
        } catch (e: Exception) {
            LogManager.log("ERROR", "ContactManager", "Failed to get contacts with phone count: ${e.message}")
            0
        }
    }
    
    suspend fun cleanupOldContacts() {
        try {
            val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            contactDao.deleteOldContacts(thirtyDaysAgo)
            LogManager.log("INFO", "ContactManager", "Cleaned up old contacts")
        } catch (e: Exception) {
            LogManager.log("ERROR", "ContactManager", "Failed to cleanup old contacts: ${e.message}")
        }
    }
    
    private fun cleanPhoneNumber(phoneNumber: String?): String {
        if (phoneNumber.isNullOrBlank()) return ""
        
        return phoneNumber
            .replace(Regex("[^0-9+]"), "") // Keep only digits and +
            .replace(Regex("^00"), "+") // Replace 00 with +
            .replace(Regex("^0"), "") // Remove leading 0 for international format
            .take(15) // Limit to 15 characters
    }
    
    fun validatePhoneNumber(phoneNumber: String): Boolean {
        val cleanPhone = cleanPhoneNumber(phoneNumber)
        return cleanPhone.matches(Regex("^\\+?[0-9]{9,15}$"))
    }
    
    fun formatPhoneNumber(phoneNumber: String): String {
        val cleanPhone = cleanPhoneNumber(phoneNumber)
        if (cleanPhone.isEmpty()) return phoneNumber
        
        // Basic formatting for Polish numbers
        return when {
            cleanPhone.startsWith("+48") && cleanPhone.length == 12 -> {
                "${cleanPhone.substring(0, 3)} ${cleanPhone.substring(3, 6)} ${cleanPhone.substring(6, 9)} ${cleanPhone.substring(9)}"
            }
            cleanPhone.startsWith("48") && cleanPhone.length == 11 -> {
                "+${cleanPhone.substring(0, 2)} ${cleanPhone.substring(2, 5)} ${cleanPhone.substring(5, 8)} ${cleanPhone.substring(8)}"
            }
            cleanPhone.length == 9 -> {
                "+48 ${cleanPhone.substring(0, 3)} ${cleanPhone.substring(3, 6)} ${cleanPhone.substring(6, 9)}"
            }
            else -> cleanPhone
        }
    }
}