package com.dt.robotpiscine;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;


public class ParamActivity extends AppCompatActivity implements View.OnKeyListener, SeekBar.OnSeekBarChangeListener {
  private EditText editTextServerAddr;
  static EditText editTextMqttUser;
  static EditText editTextMqttPassword;

  private TextView textViewMinRandomAV;
  private TextView textViewMaxRandomAV;
  private TextView textViewMinRandomAR;
  private TextView textViewMaxRandomAR;
  private TextView textViewNbCycle;
  private TextView textViewDureeCycles;
  private TextView textViewLogin;
  private TextView textViewPasswd;
  private SeekBar seekBarMinRandom_av;
  private SeekBar seekBarMaxRandom_av;
  private SeekBar seekBarMinRandom_ar;
  private SeekBar seekBarMaxRandom_ar;
  private SeekBar seekBarNbCycles;
  private SeekBar seekBarCleanTime;
  private Switch switchProg = null;
  private Switch switchLog = null;
  private Switch switchReverse = null;
  private TimePicker timePickerProg;

  static CheckBox checkBoxPrivate;
  private boolean isChangeParamAdr;
  private String logStatus;
  private boolean listenerLockSwitch;
  private int clickCount;
  private String param;

  final static int SCHEDULED_ENABLE = 0;
  final static int SCHEDULED_TIME_H = 1;
  final static int SCHEDULED_TIME_M = 2;
  private final int MIN_RANDOM_AV    = 3;
  private final int MAX_RANDOM_AV    = 4;
  private final int MIN_RANDOM_AR    = 5;
  private final int MAX_RANDOM_AR    = 6;
  private final int REVERSE          = 7;
  private final int N_CYCLES         = 8;
  private final int ACTIVE_TIME      = 9;
  private final int LOG_STATUS       = 10;
  private final int PARAM_LEN        = 11;

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
    textViewMinRandomAV = findViewById(R.id.textViewMinRandomAV);
    textViewMaxRandomAV = findViewById(R.id.textViewMaxRandomAV);
    textViewMinRandomAR = findViewById(R.id.textViewMinRandomAR);
    textViewMaxRandomAR = findViewById(R.id.textViewMaxRandomAR);
    textViewNbCycle = findViewById(R.id.textViewNbCycle);
    textViewDureeCycles = findViewById(R.id.textViewCycleTime);
    textViewLogin =  findViewById(R.id.textViewLogin);
    textViewPasswd =  findViewById(R.id.textViewPasswd);

    seekBarMaxRandom_av = findViewById(R.id.seekBarMaxRandomAV);
    seekBarMaxRandom_av.setOnSeekBarChangeListener(this);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      seekBarMaxRandom_av.setMin(MainActivity.SEEK_BAR_MAX_RANDOM_MIN);
      seekBarMaxRandom_av.setMax(MainActivity.SEEK_BAR_MAX_RANDOM_MAX);
    }

    seekBarMinRandom_av = findViewById(R.id.seekBarMinRandomAv);
    seekBarMinRandom_av.setOnSeekBarChangeListener(this);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      seekBarMinRandom_av.setMin(MainActivity.SEEK_BAR_MIN_RANDOM_MIN);
      seekBarMinRandom_av.setMax(MainActivity.SEEK_BAR_MIN_RANDOM_MAX);
    }

    seekBarMaxRandom_ar = findViewById(R.id.seekBarMaxRandomAR);
    seekBarMaxRandom_ar.setOnSeekBarChangeListener(this);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      seekBarMaxRandom_ar.setMin(MainActivity.SEEK_BAR_MAX_RANDOM_MIN);
      seekBarMaxRandom_ar.setMax(MainActivity.SEEK_BAR_MAX_RANDOM_MAX);
    }

    seekBarMinRandom_ar = findViewById(R.id.seekBarMinRandomAR);
    seekBarMinRandom_ar.setOnSeekBarChangeListener(this);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      seekBarMinRandom_ar.setMin(MainActivity.SEEK_BAR_MIN_RANDOM_MIN);
      seekBarMinRandom_ar.setMax(MainActivity.SEEK_BAR_MIN_RANDOM_MAX);
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
    seekBarMinRandom_av.setProgress(tabParam[MIN_RANDOM_AV], true);
    seekBarMaxRandom_av.setProgress(tabParam[MAX_RANDOM_AV], true);
    seekBarMinRandom_ar.setProgress(tabParam[MIN_RANDOM_AR], true);
    seekBarMaxRandom_ar.setProgress(tabParam[MAX_RANDOM_AR], true);
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

    switchReverse = findViewById(R.id.switchReverse);
    switchReverse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        tabParam[REVERSE] = isChecked ? 1 : 0;
      }
    });
    switchReverse.setChecked(tabParam[REVERSE] == 1);
    editTextServerAddr = findViewById(R.id.editTextServerAddr);
    String adrPort = Unic.getInstance().getBrockerAddress();
    int from = adrPort.indexOf(":", 6);
    String adr = adrPort.substring(0, from);
    String port = adrPort.substring(from+1);
    editTextServerAddr.setText(adr+":"+port);
    editTextServerAddr.setOnKeyListener(this);

    editTextMqttUser = findViewById(R.id.editTextLogin);
    editTextMqttPassword =findViewById(R.id.editTextPassword);
    checkBoxPrivate = findViewById(R.id.checkBoxPrivBroker);
    checkBoxPrivate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (checkBoxPrivate.isChecked()) {
              textViewLogin.setVisibility(View.VISIBLE);
              textViewPasswd.setVisibility(View.VISIBLE);
              editTextMqttUser.setVisibility(View.VISIBLE);
              editTextMqttPassword.setVisibility(View.VISIBLE);
              Secret.privateBrocker = true;
            }
            else {
              textViewLogin.setVisibility(View.INVISIBLE);
              textViewPasswd.setVisibility(View.INVISIBLE);
              editTextMqttUser.setVisibility(View.INVISIBLE);
              editTextMqttPassword.setVisibility(View.INVISIBLE);
              Secret.privateBrocker = false;
            }
        }
      }
    );
    Unic.getInstance().getMainActivity().initParamActivityControls();
  }

  public boolean onOptionsItemSelected(MenuItem item){

    if (item.getItemId() == android.R.id.home) {
      if (checkBoxPrivate.isChecked()) {
        String loginName = editTextMqttUser.getText().toString();
        MqttHelper.userName = loginName;
        String passwd = editTextMqttPassword.getText().toString();
        MqttHelper.password = passwd;
        Unic.getInstance().getMainActivity().saveMqttUser(loginName, passwd);
      }
      Unic.getInstance().getMainActivity().saveCheckboxPrivateState(checkBoxPrivate.isChecked());

      String adr = editTextServerAddr.getText().toString();
      Unic.getInstance().getMainActivity().saveBrockerAddress(adr);

      tabParam[SCHEDULED_ENABLE] = switchProg.isChecked() ? 1 : 0;
      tabParam[SCHEDULED_TIME_H] = timePickerProg.getHour();
      tabParam[SCHEDULED_TIME_M] = timePickerProg.getMinute();
      tabParam[MIN_RANDOM_AV] = seekBarMinRandom_av.getProgress();
      tabParam[MAX_RANDOM_AV] = seekBarMaxRandom_av.getProgress();
      tabParam[MIN_RANDOM_AR] = seekBarMinRandom_ar.getProgress();
      tabParam[MAX_RANDOM_AR] = seekBarMaxRandom_ar.getProgress();
      tabParam[REVERSE] = switchReverse.isChecked() ? 1 : 0;
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
    if (seekBar == seekBarMinRandom_av) {
      textViewMinRandomAV.setText("Min rnd AV " + i);
//      Log.d("seekbar", "seekBarMinRandom " + i);
    }
    else if (seekBar == seekBarMaxRandom_av) {
      textViewMaxRandomAV.setText("Max rdn AV " + i);
//      Log.d("seekbar", "seekBarMaxRandom " + i);
    }
    else if (seekBar == seekBarMinRandom_ar) {
      textViewMinRandomAR.setText("Min rdn AR " + i);
//      Log.d("seekbar", "seekBarMaxRandom " + i);
    }
    else if (seekBar == seekBarMaxRandom_ar) {
      textViewMaxRandomAR.setText("Max rdn AR " + i);
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