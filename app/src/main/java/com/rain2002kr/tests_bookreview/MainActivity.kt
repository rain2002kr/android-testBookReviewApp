package com.rain2002kr.tests_bookreview

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.rain2002kr.tests_bookreview.Adapter.BookAdapter
import com.rain2002kr.tests_bookreview.Api.BookService
import com.rain2002kr.tests_bookreview.Model.BestSellerDto
import com.rain2002kr.tests_bookreview.Model.Book
import com.rain2002kr.tests_bookreview.Model.History
import com.rain2002kr.tests_bookreview.Model.SearchBookDto
import com.rain2002kr.tests_bookreview.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bookAdapter: BookAdapter
    private lateinit var searchBookAdapter: BookAdapter
    private lateinit var bookService: BookService
    val list: List<Book> =
        listOf(Book(1, "찾아라", "설명문", 18000, "http://com.rain2002kr", "http://com.rain2002kr"))

    private lateinit var db : AppDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewBinding()
        initBookOpenApi()
        initRecyclerView()
        searchBook()
        db = getAppDatabase(this)

        saveSearchHistory()
        showSearchHistory()


    }

    @SuppressLint("ClickableViewAccessibility")
    private fun saveSearchHistory() {
        binding.deleteButton.setOnClickListener{
            try{ Thread{ db.historyDao().deleteAll() }.start()
            }catch (e:Exception){e.printStackTrace()}
        }

        binding.bookEditTextView.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == MotionEvent.ACTION_DOWN){
                val search  = binding.bookEditTextView.text
                Log.d(TAG, "엔터 처리완료 : $search")
                try{
                    Thread{
                        val history = History( keyword = search.toString())
                        db.historyDao().insertHistory(history)
                    }.start()
                }catch (e : Exception){ e.printStackTrace() }

                return@setOnKeyListener true // 실제 이벤트를 처리 했음을 의미
            }
            return@setOnKeyListener false // 다른 이벤트가 처리 되어야함.
        }
    }

    private fun showSearchHistory() {

    }

    private fun initViewBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

    private fun initRecyclerView() {
        bookAdapter = BookAdapter {}
        searchBookAdapter = BookAdapter {}

        binding.bestBookRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.bestBookRecyclerView.adapter = bookAdapter
        binding.searchBookRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.searchBookRecyclerView.adapter = searchBookAdapter
   }

    private fun initBookOpenApi() {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        bookService = retrofit.create(BookService::class.java)
        bookService.getBestSellerBooks(getString(R.string.OPEN_API_KEY))
            .enqueue(object : Callback<BestSellerDto> {
                override fun onResponse(
                    call: Call<BestSellerDto>,
                    response: Response<BestSellerDto>
                ) {
                    if (!response.isSuccessful) {
                        binding.progressBar.visibility = View.VISIBLE
                        return
                    }
                    successLoadOpenAPI( response,bookAdapter)
                }

                override fun onFailure(call: Call<BestSellerDto>, t: Throwable) {
                    failLoadOpenAPI()
                }
            })

    }

    private fun successLoadOpenAPI( dto: Response<BestSellerDto>,adapter: BookAdapter){
        dto.body()?.let {
            Toast.makeText(applicationContext, "책 정보를 가져왔습니다.", Toast.LENGTH_LONG).show()
            binding.progressBar.visibility = View.GONE
            adapter.submitList(it.books)
        }
    }

    private fun failLoadOpenAPI(){
        Toast.makeText(applicationContext, "책 정보를 가져오는데 실패 했습니다.", Toast.LENGTH_LONG)
            .show()
    }

    private fun searchBook(){


    }

    companion object {
        const val TAG = "all"
        private const val BASE_URL = "https://book.interpark.com/"

    }
}