package com.dt.robotpiscine;


import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

public class AProposActivity extends AppCompatActivity {
  private TextView textViewVersionLog;
  private TextView textViewIP;
  private TextView VersionServeurLog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_apropos);
    ActionBar actionBar = getSupportActionBar();
    assert actionBar != null;
    ((ActionBar) actionBar).setDisplayHomeAsUpEnabled(true);

    textViewVersionLog = findViewById(R.id.idVersionLog);
    textViewVersionLog.setText(Unic.getInstance().version);
    VersionServeurLog = findViewById(R.id.idVersionClient);
    textViewIP = findViewById(R.id.idTextViewIP);
    String clientVersAndIP = Unic.getInstance().getaPropos();
    if (clientVersAndIP != null) {
      VersionServeurLog.setText(clientVersAndIP.split(";")[0]);
      textViewIP.setText(clientVersAndIP.split(";")[1]);
    }
  }
  public boolean onOptionsItemSelected(MenuItem item){
    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  public void printVersion(String version) {
    String[] info = version.split("\n");
    VersionServeurLog.setText(info[0]);
    textViewIP.setText(info[1]);
  }
}