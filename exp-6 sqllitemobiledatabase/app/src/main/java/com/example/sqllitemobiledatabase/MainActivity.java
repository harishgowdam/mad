package com.example.sqllitemobiledatabase;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    EditText etRollNo, etName, etMarks;
    TextView tvResult;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etRollNo = findViewById(R.id.etRollNo);
        etName = findViewById(R.id.etName);
        etMarks = findViewById(R.id.etMarks);
        tvResult = findViewById(R.id.tvResult);
        dbHelper = new DatabaseHelper(this);

        Button btnInsert = findViewById(R.id.btnInsert);
        Button btnDelete = findViewById(R.id.btnDelete);
        Button btnUpdate = findViewById(R.id.btnUpdate);
        Button btnView = findViewById(R.id.btnView);
        Button btnViewAll = findViewById(R.id.btnViewAll);

        btnInsert.setOnClickListener(v -> insertStudent());
        btnDelete.setOnClickListener(v -> deleteStudent());
        btnUpdate.setOnClickListener(v -> updateStudent());
        btnView.setOnClickListener(v -> viewStudent());
        btnViewAll.setOnClickListener(v -> viewAllStudents());
    }

    private void insertStudent() {
        String rollNoStr = etRollNo.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String marksStr = etMarks.getText().toString().trim();

        if (rollNoStr.isEmpty() || name.isEmpty() || marksStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int rollNo = Integer.parseInt(rollNoStr);
        int marks = Integer.parseInt(marksStr);

        boolean inserted = dbHelper.insertStudent(rollNo, name, marks);
        if (inserted) {
            Toast.makeText(this, "Student inserted successfully", Toast.LENGTH_SHORT).show();
            clearFields();
        } else {
            Toast.makeText(this, "Failed to insert (Roll No may already exist)", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteStudent() {
        String rollNoStr = etRollNo.getText().toString().trim();
        if (rollNoStr.isEmpty()) {
            Toast.makeText(this, "Please enter Roll No to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        int rollNo = Integer.parseInt(rollNoStr);
        boolean deleted = dbHelper.deleteStudent(rollNo);
        if (deleted) {
            Toast.makeText(this, "Student deleted successfully", Toast.LENGTH_SHORT).show();
            clearFields();
        } else {
            Toast.makeText(this, "Student not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateStudent() {
        String rollNoStr = etRollNo.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String marksStr = etMarks.getText().toString().trim();

        if (rollNoStr.isEmpty() || name.isEmpty() || marksStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int rollNo = Integer.parseInt(rollNoStr);
        int marks = Integer.parseInt(marksStr);

        boolean updated = dbHelper.updateStudent(rollNo, name, marks);
        if (updated) {
            Toast.makeText(this, "Student updated successfully", Toast.LENGTH_SHORT).show();
            clearFields();
        } else {
            Toast.makeText(this, "Student not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void viewStudent() {
        String rollNoStr = etRollNo.getText().toString().trim();
        if (rollNoStr.isEmpty()) {
            Toast.makeText(this, "Please enter Roll No to view", Toast.LENGTH_SHORT).show();
            return;
        }

        int rollNo = Integer.parseInt(rollNoStr);
        Cursor cursor = dbHelper.getStudent(rollNo);
        if (cursor.moveToFirst()) {
            String name = cursor.getString(1);
            int marks = cursor.getInt(2);
            tvResult.setText("Roll No: " + rollNo + "\nName: " + name + "\nMarks: " + marks);
        } else {
            tvResult.setText("Student not found");
        }
        cursor.close();
    }

    private void viewAllStudents() {
        Cursor cursor = dbHelper.getAllStudents();
        if (cursor.getCount() == 0) {
            tvResult.setText("No records found");
            cursor.close();
            return;
        }

        StringBuilder sb = new StringBuilder();
        while (cursor.moveToNext()) {
            sb.append("Roll No: ").append(cursor.getInt(0))
              .append(" | Name: ").append(cursor.getString(1))
              .append(" | Marks: ").append(cursor.getInt(2))
              .append("\n");
        }
        tvResult.setText(sb.toString());
        cursor.close();
    }

    private void clearFields() {
        etRollNo.setText("");
        etName.setText("");
        etMarks.setText("");
    }
}
