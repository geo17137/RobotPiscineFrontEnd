package com.dt.robotpiscine;


import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LogsActivity extends AppCompatActivity {
  private EditText editTextLogs;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_logs);

    ActionBar actionBar = getSupportActionBar();
    assert actionBar != null;
    ((ActionBar) actionBar).setDisplayHomeAsUpEnabled(true);

    editTextLogs = findViewById(R.id.id_editTextMultiLine);
    Unic.getInstance().setEditTextLog(editTextLogs);
    Unic.getInstance().getMainActivity().readLogs();
    Button deleteButton = findViewById(R.id.id_button_clear);
    deleteButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Unic.getInstance().getMainActivity().clearLogs();
        editTextLogs.setText("");
      }
    });
  }
  public boolean onOptionsItemSelected(MenuItem item){
    if (item.getItemId() == android.R.id.home) {
        finish();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }
}