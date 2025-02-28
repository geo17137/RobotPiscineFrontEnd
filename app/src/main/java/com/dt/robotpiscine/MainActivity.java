/*
 * Commande de porte de garage
 * Version 2.0
 * Cette version contrairement à la version 1.x
 * utilise un capteur en position ouverte et
 * un capteur en position fermée.
 * Cela permet d'être certain que mécanisme a bien fermé
 * la porte et permet de savoir si la porte n'est pas entre deux positions.
 */
/*
 * Pilotage porte garage V4.0
 * V4.0 Utilise le protocole MQTT à la place de Apache/php
 * Utilise un fichier pour paramètrer les alertes
 * Ajout de version 3.1 : 2 fermetures automatiques programmables
 * Ajout de version 3.2 : 2 pas d'authentification si wifi local connecté
 */
/* TODO gérer les activations des menus */
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
import android.support.v7.app.ActionBar;
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

public class MainActivity extends AppCompatActivity {
  final static boolean isPublicServeur = Secret.isPublicServeur;
  static final String ADDRESS = Secret.ADDRESS;
  final String PREFIX = Secret.PREFIX;
  static final int SEEK_BAR_MIN_RANDOM_MIN = 10;
  static final int SEEK_BAR_MIN_RANDOM_MAX = 40;
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
  private boolean mqttServerConnected;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    init_();
  }

  /**
   * Appelée lorsque que l’activité est arrêtée.
   * Vider  le cache de l'application
   */
  @Override
  public void onDestroy() {
    super.onDestroy();
  }
  @SuppressLint("MissingInflatedId")
  private void startApp() {
    String serverAddress = null;
    serverAddress = Secret.ADDRESS;
    Unic.getInstance().setServerAddress(serverAddress);
    if (serverAddress == null)
      System.exit(1);
    setContentView(R.layout.activity_main);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    Unic.getInstance().setMainActivity(this);
    setTitle("Robot piscine");
    mqttHelper = new MqttHelper(this, serverAddress);
    paramActivity = new ParamActivity();
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
    buttonStop       = findViewById(R.id.imageButtonStop);
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
        if (!mqttServerConnected) {
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
    item_menu_a_propos.setEnabled(val);
    item_menu_param.setEnabled(val);
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
          if (!isScheduled)
            texViewButtonStatus.setText(getString(R.string.cycle_running));
          else
            texViewButtonStatus.setText(getString(R.string.prog_cycle_running));
          texViewButtonStatus.setTextColor(Color.GREEN);
        }
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
        texViewButtonStatus.setText("Cycle à l'arrêt");
        texViewButtonStatus.setTextColor(Color.RED);
        texViewCommandStatus.setText("Arrêt commande manuelle");
        texViewCommandStatus.setTextColor(Color.RED);
        texViewGeneralStatus.setText("Pas de cycle en cours");
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
    mqttServerConnected = true;
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