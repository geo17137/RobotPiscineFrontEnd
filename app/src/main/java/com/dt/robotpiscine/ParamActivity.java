package com.dt.robotpiscine;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;


public class ParamActivity extends AppCompatActivity implements View.OnKeyListener, SeekBar.OnSeekBarChangeListener {
  private EditText editTextServerAddr;

  private TextView textViewMinRandom;
  private TextView textViewMaxRandom;
  private TextView textViewNbCycle;
  private TextView textViewDureeCycles;
  private SeekBar seekBarMinRandom;
  private SeekBar seekBarMaxRandom;
  private SeekBar seekBarNbCycles;
  private SeekBar seekBarCleanTime;
  private Switch switchProg = null;
  private Switch switchLog = null;
  private TimePicker timePickerProg;
  private boolean isChangeParamAdr;
  private String logStatus;
  private boolean listenerLockSwitch;
  private int clickCount;
  private String param;

  final static int SCHEDULED_ENABLE = 0;
  final static int SCHEDULED_TIME_H = 1;
  final static int SCHEDULED_TIME_M = 2;
  private final int MIN_RANDOM       = 3;
  private final int MAX_RANDOM       = 4;
  private final int N_CYCLES         = 5;
  private final int ACTIVE_TIME      = 6;
  private final int LOG_STATUS       = 7;
  private final int PARAM_LEN        = 8;

  private int[] tabParam;
  private String[] sTabParam;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_param);
    ActionBar actionBar = getSupportActionBar();
    assert actionBar != null;
    ((ActionBar) actionBar).setDisplayHomeAsUpEnabled(true);
//    Unic.getInstance().setParamActivity(this);
    Unic.getInstance().getMainActivity().getIOTLogStatus();
    tabParam = new int[PARAM_LEN];
    setParam();
    textViewMinRandom = findViewById(R.id.textViewMinRandom);
    textViewMaxRandom = findViewById(R.id.textViewMaxRandom);
    textViewNbCycle = findViewById(R.id.textViewNbCycle);
    textViewDureeCycles = findViewById(R.id.textViewCycleTime);



    seekBarMaxRandom = findViewById(R.id.seekBarMaxRandom);
    seekBarMaxRandom.setOnSeekBarChangeListener(this);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      seekBarMaxRandom.setMin(MainActivity.SEEK_BAR_MIN_RANDOM_MAX);
      seekBarMaxRandom.setMax(MainActivity.SEEK_BAR_MAX_RANDOM_MAX);
    }

    seekBarMinRandom = findViewById(R.id.seekBarMinRandom);
    seekBarMinRandom.setOnSeekBarChangeListener(this);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      seekBarMinRandom.setMin(MainActivity.SEEK_BAR_MIN_RANDOM_MIN);
      seekBarMinRandom.setMax(MainActivity.SEEK_BAR_MIN_RANDOM_MAX);
    }

    seekBarNbCycles = findViewById(R.id.seekBarNbCycles);
    seekBarNbCycles.setOnSeekBarChangeListener(this);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      seekBarNbCycles.setMin(100);
      seekBarNbCycles.setMax(200);
    }
    seekBarCleanTime = findViewById(R.id.seekBarCleanTime);
    seekBarCleanTime.setOnSeekBarChangeListener(this);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      seekBarCleanTime.setMin(120);
      seekBarCleanTime.setMax(120*4);
    }
    seekBarMinRandom.setProgress(tabParam[MIN_RANDOM], true);
    seekBarMaxRandom.setProgress(tabParam[MAX_RANDOM], true);
    seekBarNbCycles.setProgress(tabParam[N_CYCLES], true);
    seekBarCleanTime.setProgress(tabParam[ACTIVE_TIME], true);

    timePickerProg = findViewById(R.id.timePickerProg);
    timePickerProg.setIs24HourView(true);
    timePickerProg.setHour(tabParam[SCHEDULED_TIME_H]);
    timePickerProg.setMinute(tabParam[SCHEDULED_TIME_M]);


    switchProg = findViewById(R.id.switchProg);
    switchProg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        switchProg.setText(isChecked ? R.string.mode_prog:  R.string.mode_nprog);
        tabParam[SCHEDULED_ENABLE] = isChecked ? 1 : 0;
      }
    });
    switchProg.setChecked(tabParam[SCHEDULED_ENABLE] == 1);
    switchProg.setText(tabParam[SCHEDULED_ENABLE] == 1 ? R.string.mode_prog : R.string.mode_nprog);

    switchLog = findViewById(R.id.switchLog);
    switchLog.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        switchLog.setText(isChecked ? R.string.log_en:  R.string.log_dis);
        tabParam[LOG_STATUS] = isChecked ? 1 : 0;
      }
    });
    switchLog.setChecked(tabParam[LOG_STATUS] == 1);
    switchLog.setText(tabParam[LOG_STATUS] == 1 ? R.string.log_en:  R.string.log_dis);

    editTextServerAddr = findViewById(R.id.editTextServerAddr);
    String adrPort = Unic.getInstance().getServerGarageAddress();
    int from = adrPort.indexOf(":", 6);
    String adr = adrPort.substring(0, from);
    String port = adrPort.substring(from+1);
    editTextServerAddr.setText(adr+":"+port);
    editTextServerAddr.setOnKeyListener(this);
  }

  public boolean onOptionsItemSelected(MenuItem item){
    if (item.getItemId() == android.R.id.home) {
      if (isChangeParamAdr) {
        String adr = editTextServerAddr.getText().toString();
        Unic.getInstance().getMainActivity().getAddress(true, adr);
//        SharedPreferences prefs;
//        prefs = getPreferences(Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putString("brocker", editTextServerAddr.getText().toString());
//        editor.commit();
//        Unic.getInstance().getMainActivity().getAddress(false);
      }
      tabParam[SCHEDULED_ENABLE] = switchProg.isChecked() ? 1 : 0;
      tabParam[SCHEDULED_TIME_H] = timePickerProg.getHour();
      tabParam[SCHEDULED_TIME_M] = timePickerProg.getMinute();
      tabParam[MIN_RANDOM] = seekBarMinRandom.getProgress();
      tabParam[MAX_RANDOM] = seekBarMaxRandom.getProgress();
      tabParam[N_CYCLES] = seekBarNbCycles.getProgress();
      tabParam[ACTIVE_TIME] = seekBarCleanTime.getProgress();
      tabParam[LOG_STATUS] = switchLog.isChecked() ? 1 : 0;
      StringBuffer sb = new StringBuffer();
      sb.append("" + tabParam[0]);
      for (int i = 1; i < PARAM_LEN; i++) {
        sb.append(":" + tabParam[i]);
      }
      Unic.getInstance().getMainActivity().writeParam(sb.toString());
      Unic.getInstance().getMainActivity().setTextViewProg(tabParam[SCHEDULED_ENABLE],
              String.format("%02d:%02d", tabParam[SCHEDULED_TIME_H], tabParam[SCHEDULED_TIME_M]) );
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean onKey(View v, int keyCode, KeyEvent event) {
    isChangeParamAdr = true;
    return false;
  }

  @Override
  public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
    if (seekBar == seekBarMinRandom) {
      textViewMinRandom.setText("Min rnd " + i);
//      Log.d("seekbar", "seekBarMinRandom " + i);
    }
    else if (seekBar == seekBarMaxRandom) {
      textViewMaxRandom.setText("Max rdn " + i);
//      Log.d("seekbar", "seekBarMaxRandom " + i);
    }
    else if (seekBar == seekBarNbCycles) {
      textViewNbCycle.setText("Nb cycles " + i);
//      Log.d("seekbar", "seekBarNbCycles " + i);
    }
    else if (seekBar == seekBarCleanTime) {
      textViewDureeCycles.setText("Durée nettoyage " + i + " mn");
//      Log.d("seekbar", "seekBarCleanTime " + i);
    }
  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
  }

  public void setParam() {
    param = Unic.getInstance().getParam();
    if (param==null)
      return;
    sTabParam = param.split(":");
    for (int i = 0; i < sTabParam.length; i++) {
      tabParam[i] = Integer.parseInt(sTabParam[i]);
    }
  }
}