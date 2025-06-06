package com.dt.robotpiscine;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Source code for Secret.java
   public class Secret {
   // For private brocker
   static String mqttUser = "xxxxx";
   static String mqttPasswd = "yyyyyy";
   // true for private broker
   static boolean privateBrocker = true;
   static final String ADDRESS = "tcp://xxxxx:1883";
   static final String PREFIX = "_X";
  }
 */

public class MainActivity extends AppCompatActivity {
  final String PREFIX = Secret.PREFIX;

  static final int SEEK_BAR_MIN_RANDOM_MIN = 10;
  static final int SEEK_BAR_MIN_RANDOM_MAX = 40;
  static final int SEEK_BAR_MAX_RANDOM_MIN = 20;
  static final int SEEK_BAR_MAX_RANDOM_MAX = 100;
  private static final String TAG = "mqtt";

  //-------------------------------Publications-------------------------------------------------
  private final String TOPIC_SET_PARAM      = PREFIX + "robot/param_set";
  private final String TOPIC_GET_PARAM      = PREFIX + "robot/param_get";
  private final String TOPIC_GET_VERSION    = PREFIX + "robot/versionGet";
  private final String TOPIC_GET_LOGS       = PREFIX + "robot/logsGet";
  private final String TOPIC_CLEAR_LOGS     = PREFIX + "robot/logsGet";
  private final String TOPIC_GET_STATUS     = PREFIX + "robot/getStatus";
  private final String TOPIC_START          = PREFIX + "robot/start";
  private final String TOPIC_MANUAL         = PREFIX + "robot/manual";
  private final String TOPIC_DELETE_LOGS    = PREFIX + "robot/logsDelete";
  private final String TOPIC_RESET          = PREFIX + "robot/reset";
  //-------------------------------Abonnements-------------------------------------------------
  private final String TOPIC_PARAM          = PREFIX + "robot/param";
  private final String TOPIC_READ_VERSION   = PREFIX + "robot/readVersion";
  private final String TOPIC_READ_LOGS      = PREFIX + "robot/readLogs";
  private final String TOPIC_STATUS         = PREFIX + "robot/status";
  private final String TOPIC_RESET_CYCLE    = PREFIX + "robot/reset_cycle";
  private final String TOPIC_SCHEDULED      = PREFIX + "robot/scheduled";
  private final String TOPIC_CYCLE_TIME     = PREFIX + "robot/cycle_time";
  private final String ON  = "ON";
  private final String OFF  = "OFF";

  private ImageButton buttonStartCycle;
  private ImageButton buttonStopCycle;
  private ImageButton buttonForward;
  private ImageButton buttonReverse;
  private ImageButton buttonStop;
  private TextView textViewCnxStatus;
  private TextView texViewButtonStatus;
  private TextView texViewCommandStatus;
  private TextView texViewGeneralStatus;
  private TextView textViewProg;
  private TextView textViewDuree;
  private boolean init;
  private Handler handler;
  private Runnable runnable;
  private MqttHelper mqttHelper;
  private MqttAndroidClient mqttAndroidClient;

  private  StringBuffer logBuffer;
  private boolean isClientConnected;
//  private boolean isRunningCycle;
  private int timer;
  private ParamActivity paramActivity;
  private MenuItem item_menu_logs;
  private MenuItem item_menu_exit;
  private MenuItem item_menu_a_propos;
  private MenuItem item_menu_param;
  private MenuItem item_menu_reboot;
  private boolean isScheduled;
  private boolean cycleEncours;
  private SharedPreferences prefs;
  private SharedPreferences.Editor editor;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    init_();
  }

  /**
   * Appelée lorsque que l’activité est arrêtée.
   * Vide le cache de l'application
   */
  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  public String getAddress() {
    String checkboxPrivateState = prefs.getString("checkbox_private", null);
    boolean isPrivate = "1".equals(checkboxPrivateState);
    Secret.privateBrocker =  isPrivate;
    if (isPrivate) {
      String mqttUser = prefs.getString("mqtt_user", null);
      MqttHelper.userName = mqttUser;
      String mqttPasswd = prefs.getString("mqtt_passwd", null);
      MqttHelper.password = mqttPasswd;
    }
    String brocker = prefs.getString("brocker", null);
    return brocker;
  }
  public void saveBrockerAddress(String address) {
    editor.putString("brocker", address);
    editor.commit();
  }
  public void initParamActivityControls() {
    String mqttUser = prefs.getString("mqtt_user", null);
    ParamActivity.editTextMqttUser.setText(mqttUser);
    String mqttPasswd = prefs.getString("mqtt_passwd", null);
    ParamActivity.editTextMqttPassword.setText(mqttPasswd);
    String checkboxPrivateState = prefs.getString("checkbox_private", null);
    ParamActivity.checkBoxPrivate.setChecked("1".equals(checkboxPrivateState));
  }
  public void saveMqttUser(String mqttUser, String passwd) {
    editor.putString("mqtt_user", mqttUser);
    editor.commit();
    editor.putString("mqtt_passwd", passwd);
    editor.commit();
  }

  public void saveCheckboxPrivateState(boolean isChecked) {
    editor.putString("checkbox_private", isChecked ? "1" : "0");
    editor.commit();
  }

  public boolean initLocalPermanentData(String topic, String value) {
    String data =  prefs.getString(topic, null);
    if (data == null) {
      editor.putString(topic, value);
      editor.commit();
      return true;
    }
    return false;
  }
  @SuppressLint("MissingInflatedId")
  private void startApp() {
    setContentView(R.layout.activity_main);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    Unic.getInstance().setMainActivity(this);
    setTitle("Robot piscine");
    textViewCnxStatus   = findViewById(R.id.textViewCnxStatus);
    texViewButtonStatus = findViewById(R.id.textViewButtonStatus);
    texViewButtonStatus.setText(getString(R.string.cycle_stopping));
    texViewButtonStatus.setTextColor(Color.RED);
    texViewCommandStatus = findViewById(R.id.textViewCommandStatus);
    texViewCommandStatus.setText(getString(R.string.manual_stop));
    texViewCommandStatus.setTextColor(Color.RED);
    texViewGeneralStatus = findViewById(R.id.textViewGeneralStatus);
    texViewGeneralStatus.setText(getString(R.string.no_cycle_running));
    textViewDuree = findViewById(R.id.textViewDuree);
    textViewDuree.setText(getString(R.string.text_duree) + "   0 s");
    textViewProg = findViewById(R.id.textViewProg);
    buttonStartCycle = findViewById(R.id.imageButtonStartCycle);
    buttonStartCycle.setEnabled(true);
    buttonStartCycle.setVisibility(View.VISIBLE);
    buttonStartCycle.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mqttHelper.publish(TOPIC_START, ON.getBytes());
        texViewButtonStatus.setText(getString(R.string.cycle_running));
        texViewButtonStatus.setTextColor(Color.GREEN);
        texViewGeneralStatus.setTextColor(Color.GREEN);
        buttonStartCycle.setVisibility(View.INVISIBLE);
        buttonStopCycle.setVisibility(View.VISIBLE);
        buttonForward.setVisibility(View.INVISIBLE);
        buttonReverse.setVisibility(View.INVISIBLE);
        texViewCommandStatus.setText("Pause/reprise");
        cycleEncours = true;
      }
    });
    buttonStopCycle  = findViewById(R.id.imageButtonStopCycle);
    buttonStopCycle.setEnabled(true);
    buttonStopCycle.setVisibility(View.INVISIBLE);
    buttonStopCycle.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mqttHelper.publish(TOPIC_START, OFF.getBytes());
        texViewButtonStatus.setText(getString(R.string.cycle_stopping));
        texViewGeneralStatus.setTextColor(Color.RED);
        texViewButtonStatus.setTextColor(Color.RED);
        buttonStartCycle.setVisibility(View.VISIBLE);
        buttonStopCycle.setVisibility(View.INVISIBLE);
        buttonForward.setVisibility(View.VISIBLE);
        buttonReverse.setVisibility(View.VISIBLE);
        texViewCommandStatus.setText(getString(R.string.manual_stop));
        cycleEncours = false;
      }
    });
    buttonForward = findViewById(R.id.imageButtonForward);
    buttonForward.setEnabled(true);
    buttonForward.setVisibility(View.VISIBLE);
    buttonForward.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mqttHelper.publish(TOPIC_MANUAL, ON.getBytes());
        texViewCommandStatus.setText(getString(R.string.forward));
        texViewCommandStatus.setTextColor(Color.GREEN);
        texViewButtonStatus.setText(getString(R.string.cycle_stopping));
        texViewButtonStatus.setTextColor(Color.RED);
        buttonStartCycle.setVisibility(View.VISIBLE);
        buttonStopCycle.setVisibility(View.INVISIBLE);
      }
    });
    buttonReverse    = findViewById(R.id.imageButtonReverse);
    buttonReverse.setEnabled(true);
    buttonReverse.setVisibility(View.VISIBLE);
    buttonReverse.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mqttHelper.publish(TOPIC_MANUAL, OFF.getBytes());
        texViewCommandStatus.setText(getString(R.string.reverse));
        texViewCommandStatus.setTextColor(Color.BLUE);
        texViewButtonStatus.setText(getString(R.string.cycle_stopping));
        texViewButtonStatus.setTextColor(Color.RED);
        buttonStartCycle.setVisibility(View.VISIBLE);
        buttonStopCycle.setVisibility(View.INVISIBLE);
      }
    });
    buttonStop = findViewById(R.id.imageButtonStop);
    buttonStop.setEnabled(true);
    buttonStop.setVisibility(View.VISIBLE);
    buttonStop.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mqttHelper.publish(TOPIC_MANUAL, "STOP".getBytes());
        texViewCommandStatus.setText(getString(R.string.manual_stop));
        texViewCommandStatus.setTextColor(Color.RED);
        texViewButtonStatus.setText(getString(R.string.cycle_stopping));
        texViewButtonStatus.setTextColor(Color.RED);
        buttonStartCycle.setVisibility(View.VISIBLE);
        buttonStopCycle.setVisibility(View.INVISIBLE);
      if (cycleEncours)
          texViewCommandStatus.setText("Pause/reprise");
      }
    });

    /*
     * Tâche de surveillance
     * Attention il ne doit y avoir aucun acces concurrent avec populate()
     */
    handler = new Handler();
    handler.postDelayed(runnable = new Runnable() {
      private boolean first = true;
      @Override
      public void run() {
        if (!mqttHelper.isConnected()) {
          textViewCnxStatus.setText(getString(R.string.mqtt_nok));
          textViewCnxStatus.setTextColor(Color.RED);
        }
        else {
          textViewCnxStatus.setText(getString(R.string.mqtt_ok));
          textViewCnxStatus.setTextColor(Color.GREEN);
          mqttHelper.publish(TOPIC_GET_STATUS, ("").getBytes());
          if (isClientConnected && timer++ < 4) {
            if (first) {
              mqttHelper.publish(TOPIC_GET_PARAM, "".getBytes());
              mqttHelper.publish(TOPIC_GET_VERSION, "".getBytes());
              first = false;
            }
            textViewCnxStatus.setText(getString(R.string.cnx_ok));
            textViewCnxStatus.setTextColor(Color.GREEN);
            activeItemMenus(true);
          }
          else {
            textViewCnxStatus.setText(getString(R.string.cnx_nok));
            textViewCnxStatus.setTextColor(Color.RED);
            activeItemMenus(false);
          }
        }
        handler.postDelayed(this, 1000);
      }
    }, 500);

    prefs = getPreferences(Context.MODE_PRIVATE);
    editor = prefs.edit();
    initLocalPermanentData("checkbox_private", Secret.privateBrocker ? "1":"0");
    initLocalPermanentData("mqtt_user", Secret.mqttUser);
    initLocalPermanentData("mqtt_passwd", Secret.mqttPasswd);
    initLocalPermanentData("brocker", Secret.ADDRESS);
    String serverAddress = getAddress();
    Unic.getInstance().setBrockerAddress(serverAddress);
    mqttHelper = new MqttHelper(this, serverAddress);
    paramActivity = new ParamActivity();
    init = true;
  }

  private void init_() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
      // Ces requètes se font en arrière plan, il est important lancer l'appli (startApp) effective dans l'écouteur onRequestPermissionsResult
      ActivityCompat.requestPermissions(this, new String[]
              {
              Manifest.permission.INTERNET
              }, 1); // code permettant de différentier les différents blocs if (checkSelfPermission...)
    } else {
      startApp();
    }
  }

  @SuppressWarnings("IfStatementMissingBreakInLoop")
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    boolean permissionOK = true;
//    switch (requestCode) {
//      case 1:
    for (int i = 0; i < permissions.length; i++) {
      if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
        permissionOK = false;
      }
    }
    if (permissionOK)
      startApp();
    else
      System.exit(0);
  }

  /**
   * Appelée lorsque que l’activité est suspendue.
   * Stoppez les actions qui consomment des ressources.
   * L’activité va passer en arrière-plan.
   */
  @Override
  public void onPause() {
    if (init) {
      handler.removeCallbacks(runnable);
    }
    super.onPause();
  }

  /**
   * Appelée après le démarrage ou une pause.
   * Relancez les opérations arrêtées (threads). Mettez à
   * jour votre application et vérifiez vos écouteurs.
   */
  @Override
  public void onResume() {
    super.onResume();
    if (init)
      runnable.run();
  }

  @Override
  protected void onStart() {
    super.onStart();
  }

  public void writeParam(String param) {
    Unic.getInstance().setParam(param);
    mqttHelper.publish(TOPIC_SET_PARAM, param.getBytes());
  }
  private void init() {
    logBuffer = new StringBuffer();
  }

  private void activeItemMenus(boolean val) {
    item_menu_logs.setEnabled(val);
    item_menu_a_propos.setEnabled(true);
    item_menu_param.setEnabled(true);
    item_menu_reboot.setEnabled(val);
  }
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_main, menu);
    item_menu_logs = menu.findItem(R.id.id_action_logs);
    item_menu_exit = menu.findItem(R.id.id_action_exit);
    item_menu_exit.setEnabled(true);
    item_menu_a_propos = menu.findItem(R.id.id_a_propos);
    item_menu_param = menu.findItem(R.id.id_action_param);
    item_menu_reboot = menu.findItem(R.id.id_action_reboot);
    return true;
  }
  @SuppressLint("NonConstantResourceId")
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.id_action_exit:
        System.exit(0);
        break;
      case R.id.id_action_reboot:
        mqttHelper.publish(TOPIC_RESET, ("").getBytes());
        break;
      case R.id.id_a_propos:
        Intent intentAProposActivity = new Intent(MainActivity.this, AProposActivity.class);
        startActivity(intentAProposActivity);
        break;
      case R.id.id_action_param:
        Intent intentParam = new Intent(MainActivity.this, ParamActivity.class);
        startActivity(intentParam);
        break;
      case R.id.id_action_logs:
        Intent intentLogActivity = new Intent(MainActivity.this, LogsActivity.class);
        startActivity(intentLogActivity);
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  public void connectComplete(MqttAndroidClient mqttAndroidClient) {
    try {
      //Successful connection requires all client subscription relationships to be uploaded
      this.mqttAndroidClient = mqttAndroidClient;
      mqttAndroidClient.subscribe(TOPIC_PARAM, 0);
      mqttAndroidClient.subscribe(TOPIC_STATUS, 0);
      mqttAndroidClient.subscribe(TOPIC_READ_VERSION, 0);
      mqttAndroidClient.subscribe(TOPIC_READ_LOGS, 0);
      mqttAndroidClient.subscribe(TOPIC_RESET_CYCLE, 0);
      mqttAndroidClient.subscribe(TOPIC_SCHEDULED, 0);
      mqttAndroidClient.subscribe(TOPIC_CYCLE_TIME, 0);

      init();
    } catch (MqttException e) {
      Log.d(TAG, "subscribe ex" );
    }
  }

  public void connectionLost() {
  }

  public void messageArrived(String topic, MqttMessage message) {
//  Log.d("MQTT", topic + ":" + message);
    String reponse = message.toString();
    switch (topic) {
      case TOPIC_PARAM:
        Unic.getInstance().setParam(reponse);
        String[] tabParam = reponse.split(":");

        if ("1".equals(tabParam[ParamActivity.SCHEDULED_ENABLE]))
            setTextViewProg(
                    1,
                      tabParam[ParamActivity.SCHEDULED_TIME_H]
                    + ":"
                    + tabParam[ParamActivity.SCHEDULED_TIME_M]);
        else
          setTextViewProg(0, "");
        return;
      case TOPIC_STATUS:
        isClientConnected = true;
        timer = 0;
        texViewGeneralStatus.setText(reponse.split("#")[0]);
        boolean isRunningCycle = "1".equals(reponse.split("#")[1]);
        if (isRunningCycle) {
          buttonStartCycle.setVisibility(View.INVISIBLE);
          buttonStopCycle.setVisibility(View.VISIBLE);
          buttonForward.setVisibility(View.INVISIBLE);
          buttonReverse.setVisibility(View.INVISIBLE);
          texViewCommandStatus.setText("Pause/reprise");
          cycleEncours = true;
          if (!isScheduled)
            texViewButtonStatus.setText(getString(R.string.cycle_running));
          else
            texViewButtonStatus.setText(getString(R.string.prog_cycle_running));
          texViewButtonStatus.setTextColor(Color.GREEN);
        }
//        else {
//          buttonForward.setVisibility(View.VISIBLE);
//          buttonReverse.setVisibility(View.VISIBLE);
//          texViewCommandStatus.setText(getString(R.string.manual_stop));
//          cycleEncours = false;
//        }
        return;
      case TOPIC_READ_VERSION:
        Unic.getInstance().setaPropos(reponse);
        return;
      case TOPIC_READ_LOGS:
        String msg = message.toString();
        if (!"#####".equals(msg)) {
          logBuffer.append(msg);
          return;
        }
        String[] tReponse = logBuffer.toString().split("\n");
        ArrayList<String> al = new ArrayList<>();
        for (String line : tReponse)
          al.add(line+"\n");
        Collections.reverse(al);
        reponse = al.toString().substring(1);
        reponse = reponse.replace("\n", "&lt;br&gt;");
        reponse = reponse.replace(",", "");
        Unic.getInstance().setLogs(reponse);
        logBuffer.delete(0, logBuffer.length()-1);
        Unic.getInstance().getEditTextLog().setText((Html.fromHtml(Html.fromHtml(reponse).toString())));
        return;
      case TOPIC_RESET_CYCLE:
        texViewButtonStatus.setText(getString(R.string.cycle_stop));
        texViewButtonStatus.setTextColor(Color.RED);
        texViewCommandStatus.setText(getString(R.string.manual_stop));
        texViewCommandStatus.setTextColor(Color.RED);
        texViewGeneralStatus.setText(getString(R.string.no_cycle));
        texViewGeneralStatus.setTextColor(Color.RED);
        buttonStartCycle.setVisibility(View.VISIBLE);
        buttonStopCycle.setVisibility(View.INVISIBLE);
        isScheduled = false;
        return;
      case TOPIC_SCHEDULED:
//        texViewButtonStatus.setText("Départ cycle programmé à " + reponse);
        texViewButtonStatus.setTextColor(Color.GREEN);
        texViewGeneralStatus.setTextColor(Color.GREEN);
        isScheduled = true;
        return;
      case TOPIC_CYCLE_TIME:
        textViewDuree.setText(getString(R.string.text_duree) + " " + message + " s");
    }
  }

  public void deliveryComplete() {
  }

  public void connected() {

  }

  public void onFailure(IMqttToken asyncActionToken) {
  }

  public void readVersion() {
    mqttHelper.publish(TOPIC_GET_VERSION, "".getBytes());
  }

  public void readLogs() {
    mqttHelper.publish(TOPIC_GET_LOGS, "".getBytes());
  }

  public void clearLogs() {
    mqttHelper.publish(TOPIC_DELETE_LOGS, "".getBytes());
  }

  public void getIOTLogStatus() {
  }

  public void getLockStatus() {
  }

  public void setTextViewProg(int i, String time) {
    if (i == 1) {
      textViewProg.setText(getString(R.string.cycle_prog) + " " + time);
      textViewProg.setTextColor(Color.GREEN);
    }
    else {
      textViewProg.setText(getString(R.string.cycle_n_prog));
      textViewProg.setTextColor(Color.BLUE);
    }
  }
}