package com.example.apporderfood.Activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apporderfood.R;
import com.example.apporderfood.adapter.TableManageAdapter;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class TableManageActivity extends AppCompatActivity {

    private RecyclerView rvTableList;
    private TableManageAdapter adapter;
    private MaterialButton btnAddTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_ban);

        rvTableList = findViewById(R.id.rvTableList);
        btnAddTable = findViewById(R.id.btn_add_table);

        rvTableList.setLayoutManager(new LinearLayoutManager(this));

        List<TableManageAdapter.TableItem> tableList = new ArrayList<>();
        tableList.add(new TableManageAdapter.TableItem("B01", "HOẠT ĐỘNG", "Tầng 1", 4, "12/10/2023"));
        tableList.add(new TableManageAdapter.TableItem("VIP01", "ĐANG KHÓA", "Phòng VIP", 10, "15/10/2023"));
        tableList.add(new TableManageAdapter.TableItem("B05", "BẢO TRÌ", "Sân vườn", 2, "20/10/2023"));
        tableList.add(new TableManageAdapter.TableItem("B02", "HOẠT ĐỘNG", "Tầng 1", 4, "12/10/2023"));
        tableList.add(new TableManageAdapter.TableItem("B03", "HOẠT ĐỘNG", "Tầng 2", 6, "14/10/2023"));

        adapter = new TableManageAdapter(tableList);
        rvTableList.setAdapter(adapter);

        btnAddTable.setOnClickListener(v -> {
            Intent intent = new Intent(TableManageActivity.this, ThemBanMoiActivity.class);
            startActivity(intent);
        });
    }
}
