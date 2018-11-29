package com.example.hal_9000.igor.adapters

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.hal_9000.igor.R
import com.example.hal_9000.igor.model.Book
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestoreException
import java.text.SimpleDateFormat
import java.util.*

class BooksListAdapter(options: FirestoreRecyclerOptions<Book>, private val itemClickListener: (Book) -> Unit) : FirestoreRecyclerAdapter<Book, BooksListAdapter.BookViewHolder>(options) {
    private val TAG = "BooksListAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.book_item, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int, model: Book) {
        holder.setBookTitle(model.title)
        holder.setBookUploader(model.uploader)
        holder.setBookUploadDate(model.uploaded_at)
        holder.setClickListener(model, itemClickListener)
    }

    override fun onError(e: FirebaseFirestoreException) {
        Log.e(TAG, "Error: $e.message")
    }

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun setBookTitle(bookTitle: String) {
            val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
            tvTitle.text = bookTitle
        }

        fun setBookUploader(uploader: String) {
            val tvCreator: TextView = itemView.findViewById(R.id.tv_creator)
            tvCreator.text = uploader
        }

        fun setBookUploadDate(date: Long) {
            val tvDate: TextView = itemView.findViewById(R.id.tv_date)
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = date
            tvDate.text = SimpleDateFormat("dd/MM/YY").format(calendar.time)
        }

        fun setClickListener(book: Book, clickListener: (Book) -> Unit) {
            itemView.setOnClickListener { clickListener(book) }
        }
    }
}