package com.focusstart.fsroom.ui.main

import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.focusstart.fsroom.room.entity.Contact
import com.focusstart.fsroom.recycler.ContactsDataAdapter
import com.focusstart.fsroom.room.ContactsDatabase
import com.focusstart.fsroom.R
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.main_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val contacts: ArrayList<Contact> = ArrayList()

        // Инициализация RecyclerView
        contactsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = ContactsDataAdapter(contacts)
        }

        // Запрос на получение прав
        val requestReadContactsPermission =
                registerForActivityResult(
                        ActivityResultContracts.RequestPermission()
                ) { isGranted: Boolean ->
                    if (isGranted) {
                        // Разрешение получено, загружаем контакты
                        loadContactsToRecyclerView(contacts)
                    } else {
                        // Разрешение не получено, поясняем, почему фича не работает
                        Toast.makeText(
                                requireContext(),
                                "Разрешение на получение контактов не дано",
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                }

        val database = ContactsDatabase.getInstance(requireContext())

        // Получение списка контактов с предварительной проверкой разрешения
        getContactsButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                loadContactsToRecyclerView(contacts)
            }
            else {
                requestReadContactsPermission.launch(Manifest.permission.READ_CONTACTS)
            }
        }

        clearRecyclerButton.setOnClickListener {
            contacts.clear()
            contactsRecyclerView.adapter?.notifyDataSetChanged()
            Toast.makeText(requireContext(),
                "Список очищен",
                Toast.LENGTH_SHORT).show()
        }

        deleteAllButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                database.userDao().deleteAll()

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(),
                        "БД очищена",
                        Toast.LENGTH_SHORT).show()
                }
            }

        }

        saveContactsButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                database.userDao().insertAll(contacts)

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(),
                        "Контакты сохранены в БД",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }

        getContactsFromDbButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                contacts.apply {
                    clear()
                    addAll(database.userDao().getAll())
                }

                withContext(Dispatchers.Main) {
                    contactsRecyclerView.adapter?.notifyDataSetChanged()
                    Toast.makeText(requireContext(),
                        "Контакты загружены из БД",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Загрузка контактов из ContentResolver в RecyclerView
    // с использованием корутин
    private fun loadContactsToRecyclerView(contacts: ArrayList<Contact>) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                contacts.apply {
                    clear()
                    addAll(getContacts())
                }
            }
            contactsRecyclerView.adapter?.notifyDataSetChanged()
            Toast.makeText(requireContext(),
                "Загружены контакты из ContentResolver",
                Toast.LENGTH_SHORT).show()
        }
    }

    // Метод получения контактов на телефоне
    // с помощью ContentResolver
    private fun getContacts(): List<Contact> {
        val items: ArrayList<Contact> = ArrayList()

        val resolver: ContentResolver = requireContext().contentResolver
        resolver.query(
            ContactsContract.Contacts.CONTENT_URI, null,
            null, null, null
        )?.use { cursor ->
            if (cursor.count > 0) {
                while (cursor.moveToNext()) {
                    val id = cursor.getString(
                        cursor.getColumnIndex(
                            ContactsContract.Contacts._ID
                        )
                    )
                    val name = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    )
                    val phoneNumber = (cursor.getString(
                        cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                    )).toInt()

                    if (phoneNumber > 0) {
                        resolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                            arrayOf(id), null
                        )?.use { cursorPhone ->
                            if (cursorPhone.count > 0) {
                                while (cursorPhone.moveToNext()) {
                                    val phone = cursorPhone.getString(
                                        cursorPhone.getColumnIndex(
                                            ContactsContract.CommonDataKinds.Phone.NUMBER
                                        )
                                    )
                                    // Заполнение массива контактов
                                    items.add(Contact(name = name, phone = phone))
                                }
                            }
                        }
                    }
                }
            }
        }

        return items
    }

}