package com.rain2002kr.tests_bookreview

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.rain2002kr.tests_bookreview.Adapter.BookAdapter
import com.rain2002kr.tests_bookreview.Adapter.HistoryAdapter
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
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var bookService: BookService
    val list: List<Book> =
        listOf(Book(1, "찾아라", "설명문", 18000, "http://com.rain2002kr", "http://com.rain2002kr"))

    private lateinit var db : AppDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = getAppDatabase(this)

        initMainActivityViewBinding()
        // todo 베스트 셀러 책정보 가져오기
        initBookRecyclerView()
        initgetBestBooksOpenApi()

        initHistoryRecyclerView()
        initSearchEditText()


        binding.deleteButton.setOnClickListener{
            try{ Thread{ db.historyDao().deleteAll() }.start()
                showHistoryView()
            }catch (e:Exception){e.printStackTrace()}
        }


    }

    @SuppressLint("ClickableViewAccessibility")
    private fun saveSearchHistory(keyword: String) {
        try{
            Thread{ db.historyDao().insertHistory(History(null,keyword)) }.start()
        }catch (e:Exception){e.printStackTrace()}


    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initSearchEditText() {
        binding.bookEditTextView.setOnKeyListener { v, keyCode, event ->
            // 터치 입력되면 계속해서 search 함수를 호출하여, 인터파크로 부터 정보를 검색해온다.
            if(keyCode == KeyEvent.KEYCODE_ENTER && event.action == MotionEvent.ACTION_DOWN){
                val keyword = binding.bookEditTextView.text.toString()
                if (keyword.isNotEmpty() or keyword.isNotBlank()){
                    searchBook(keyword)
                } else {
                    initgetBestBooksOpenApi()
                    hideHistoryRecyclerView()
                }

                return@setOnKeyListener true // 실제 이벤트 처리 했음.

            }
            return@setOnKeyListener false //다른 이벤트가 처리 되어야함.
        }

        // todo 에디트 텍스트뷰 터치 하면 히스토리 내역 보여주기
        binding.bookEditTextView.setOnTouchListener { v, event ->

            if(event.action == MotionEvent.ACTION_DOWN) {
                showHistoryView()
                //return@setOnTouchListener true
            }
            return@setOnTouchListener false
        }


    }

    private fun showHistoryView() {
        try {
            Thread{
                val keywords = db.historyDao().getAll().reversed()
                // todo history recyclerview UI
                runOnUiThread {
                    historyAdapter.submitList(keywords.orEmpty())
                    showHistoryRecyclerView()
                }
            }.start()
            showHistoryRecyclerView()
        }catch (e:Exception){e.printStackTrace()}

    }

    private fun showHistoryRecyclerView(){
        binding.historyRecyclerView.isVisible = true
        hideBookRecyclerView()
    }
    private fun hideHistoryRecyclerView(){
        binding.historyRecyclerView.isVisible = false
        showBookRecyclerView()
    }
    private fun showBookRecyclerView(){
        binding.bestBookRecyclerView.isVisible = true
    }
    private fun hideBookRecyclerView(){
        binding.bestBookRecyclerView.isVisible = false
    }

    private fun initMainActivityViewBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun initBookRecyclerView() {
        bookAdapter = BookAdapter( onClickListener = {
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("bookModel", it)
            startActivity(intent)
        })

        binding.bestBookRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.bestBookRecyclerView.adapter = bookAdapter
   }

    private fun initHistoryRecyclerView(){
        historyAdapter = HistoryAdapter(historyDeleteClickedListener = {
            deleteSearchKeyword(it)
        }, historyClickedListener = {
            loadSeachKeyword(it)
        })
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.historyRecyclerView.adapter = historyAdapter

    }

    private fun deleteSearchKeyword(keyword: String) {
        try {
            Thread{db.historyDao().delete(keyword)}.start()
            showHistoryView()
        }catch (e:Exception){e.printStackTrace()}
    }
    private fun loadSeachKeyword(keyword: String){
        try {
            Toast.makeText(this, "$keyword",Toast.LENGTH_LONG).show()
            binding.bookEditTextView.setText(keyword)

        }catch (e:Exception){e.printStackTrace()}
    }

    private fun initgetBestBooksOpenApi() {

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

                        // 프로그래스바
                        binding.progressBar.visibility = View.VISIBLE
                        return
                    }
                    // 베스트셀러 책정보 성공
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

    // todo keyword 입력되면, 인터파크로부터 정보 받아오기
    private fun searchBook(keyword : String){
        bookService.getBookByName(getString(R.string.OPEN_API_KEY),keyword)
            .enqueue(object :Callback<SearchBookDto>{
                override fun onResponse(
                    call: Call<SearchBookDto>,
                    response: Response<SearchBookDto>
                ) {
                    hideHistoryRecyclerView()
                    saveSearchHistory(keyword)

                    if(!response.isSuccessful){
                        //Toast.makeText(applicationContext,"책검색 실패",Toast.LENGTH_SHORT).show()
                        return
                    }
                    response.body()?.let{
                        bookAdapter.submitList(it.books)
                    }
                    showBookRecyclerView()
                }

                override fun onFailure(call: Call<SearchBookDto>, t: Throwable) {
                    hideHistoryRecyclerView()
                }
            })
    }



    companion object {
        const val TAG = "all"
        private const val BASE_URL = "https://book.interpark.com/"

    }
}