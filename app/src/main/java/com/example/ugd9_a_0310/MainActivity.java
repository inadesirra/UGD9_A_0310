package com.example.ugd9_a_0310;

import static com.android.volley.Request.Method.DELETE;
import static com.android.volley.Request.Method.GET;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.ugd9_a_0310.adapters.ProdukAdapter;
import com.example.ugd9_a_0310.api.ProdukApi;
import com.example.ugd9_a_0310.models.ProdukResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final int LAUNCH_ADD_ACTIVITY = 123;

    private SwipeRefreshLayout srProduk;
    private ProdukAdapter adapter;
    private SearchView svProduk;
    private LinearLayout layoutLoading;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Pendeklarasian request queue  -> Volley pake ini
        queue = Volley.newRequestQueue(this);

        layoutLoading = findViewById(R.id.layout_loading);
        srProduk = findViewById(R.id.sr_produk);
        svProduk = findViewById(R.id.sv_produk);

        srProduk.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAllProduk();
            }
        });

        svProduk.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.getFilter().filter(s);
                return false;
            }
        });

        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, AddEditActivity.class);
                startActivityForResult(i, LAUNCH_ADD_ACTIVITY);
            }
        });

        RecyclerView rvProduk = findViewById(R.id.rv_produk);
        adapter = new ProdukAdapter(new ArrayList<>(), this);

        // TODO: Atur layout manager adapter untuk menampilkan 2 item per baris ketika orientasi
        //  potrait dan menampilkan 4 item perbaris ketika orientasi landscape. (hint: GridLayout)

        if((MainActivity.this).getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            int numberOfColumns = 2;
            rvProduk.setLayoutManager(new GridLayoutManager(MainActivity.this, numberOfColumns));
        }else{
            int numberOfColumns = 4;
            rvProduk.setLayoutManager(new GridLayoutManager(MainActivity.this, numberOfColumns));
        }
        rvProduk.setAdapter(adapter);

        getAllProduk();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LAUNCH_ADD_ACTIVITY && resultCode == Activity.RESULT_OK)
            getAllProduk();
    }

    private void getAllProduk() {
        // TODO: Tambahkan fungsi untuk menampilkan seluruh data buku.
        srProduk.setRefreshing(true);
        StringRequest stringRequest = new StringRequest(GET, ProdukApi.GET_ALL_URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Gson gson = new Gson();
                 /* Deserialiasai data dari response JSON dari API
                 menjadi java object ProdukResponse menggunakan Gson */
                ProdukResponse produkResponse =
                        gson.fromJson(response, ProdukResponse.class);
                adapter.setProdukList(produkResponse.getProdukList());
                adapter.getFilter().filter(svProduk.getQuery());
                Toast.makeText(MainActivity.this,
                        produkResponse.getMessage(), Toast.LENGTH_SHORT).show();
                srProduk.setRefreshing(false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                srProduk.setRefreshing(false);
                try {
                    String responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                    JSONObject errors = new JSONObject(responseBody);
                    Toast.makeText(MainActivity.this,
                            errors.getString("message"), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        })

        { // Menambahkan header pada request
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");
                return headers;
            }
        };
        // Menambahkan request ke request queue
        queue.add(stringRequest);
    }

    public void deleteProduk(long id) {
        // TODO: Tambahkan fungsi untuk menghapus data buku.
        setLoading(true);
        StringRequest stringRequest = new StringRequest(DELETE, ProdukApi.DELETE_URL + id, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Gson gson = new Gson();
                 /* Deserialiasai data dari response JSON dari API
                 menjadi java object ProdukResponse menggunakan Gson */
                ProdukResponse produkResponse =
                        gson.fromJson(response, ProdukResponse.class);
                setLoading(false);
                Toast.makeText(MainActivity.this,
                        produkResponse.getMessage(), Toast.LENGTH_SHORT).show();
                getAllProduk();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                setLoading(false);
                try {
                    String responseBody = new String(error.networkResponse.data,
                            StandardCharsets.UTF_8);
                    JSONObject errors = new JSONObject(responseBody);
                    Toast.makeText(MainActivity.this,
                            errors.getString("message"), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        })
        {
            // Menambahkan header pada request
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");
                return headers;
            }
        };
        // Menambahkan request ke request queue
        queue.add(stringRequest);
    }

    // Fungsi ini digunakan menampilkan layout loading
    private void setLoading(boolean isLoading) {
        if (isLoading) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            layoutLoading.setVisibility(View.VISIBLE);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            layoutLoading.setVisibility(View.GONE);
        }
    }
}