package com.focusstart.fsroom.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.focusstart.fsroom.R
import com.focusstart.fsroom.room.entity.Contact

class ContactsDataAdapter(private val contacts: List<Contact>):
    RecyclerView.Adapter<ContactsDataAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact: Contact = contacts[position]
        holder.bind(contact)
    }

    override fun getItemCount(): Int = contacts.size

    class ViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.contacts_item, parent, false)) {
        private val nameView: TextView = itemView.findViewById(R.id.nameView)
        private val phoneView: TextView = itemView.findViewById(R.id.phoneView)

        fun bind(contact: Contact) {
            nameView.text = contact.name
            phoneView.text = contact.phone
        }
    }
}