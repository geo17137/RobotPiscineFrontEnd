package com.dt.robotpiscine;

import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

class Unic {
  final String version = "2025.02.27 [D.T.]";
  private MainActivity mainActivity;
  private String  param;

  public String getaPropos() {
    return aPropos;
  }

  public void setaPropos(String aPropos) {
    this.aPropos = aPropos;
  }

  private String aPropos;

  public String getLogs() {
    return logs;
  }

  public void setLogs(String logs) {
    this.logs = logs;
  }

  private String logs;
  private String serverAddress;
  private EditText editTextLogs;
  public MainActivity getMainActivity() {
    return mainActivity;
  }
  public void setMainActivity(MainActivity _this) {
    this.mainActivity = _this;
  }
  public String getParam() { return param; }
  public void setParam(String param) { this.param = param; }
  private static final Unic unic = new Unic();
  static Unic getInstance() {
    return unic;
  }
  private Unic() {
  }

  public void setServerAddress(String serverAddress) {
    this.serverAddress = serverAddress;
  }

  public String getServerGarageAddress() {
    return serverAddress;
  }

    public void setEditTextLog(EditText editTextLogs) {
      this.editTextLogs = editTextLogs;
    }

  public EditText getEditTextLog() {
    return this.editTextLogs;
  }
}
