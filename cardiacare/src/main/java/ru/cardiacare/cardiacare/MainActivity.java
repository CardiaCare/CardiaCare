package ru.cardiacare.cardiacare;

/* Главный экран */

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.petrsu.cardiacare.smartcare.SmartCareLibrary;
import com.petrsu.cardiacare.smartcare.servey.Feedback;

import ru.cardiacare.cardiacare.bluetooth.BluetoothFindActivity;
import ru.cardiacare.cardiacare.ecgviewer.ECGActivity;
import ru.cardiacare.cardiacare.hisdocuments.DocumentsActivity;
import ru.cardiacare.cardiacare.location.GPSLoad;
import ru.cardiacare.cardiacare.location.LocationService;
import ru.cardiacare.cardiacare.servey.QuestionnaireHelper;
import ru.cardiacare.cardiacare.user.AccountStorage;
import ru.cardiacare.cardiacare.user.Login;
import ru.cardiacare.cardiacare.user.Userdata;

public class MainActivity extends AppCompatActivity {

    public Context context = this;
    private static Context mContext;

    Button btnCont;
    Button nextButton;
    Button btnDisconnect;
    static public Button alarmButton;
    static public ImageButton serveyButton;
    EditText etFirstName;
    EditText etSecondName;
    ListView connectListView;

    private ArrayAdapter<String> connectListArrayAdapter;

    static public SmartCareLibrary smart;
    static public long nodeDescriptor = -1;
    static public String patientUri;
    static public String locationUri;
    static protected String alarmUri;
    static public String feedbackUri;
    static public String alarmFeedbackUri;

    public static boolean connectedState = false;
    public static boolean loginState = false; // Авторизирован ли пользователь, true - авторизирован / false - неавторизирован

    static public String TAG = "SS-main";
    static public AccountStorage storage;
    static public Feedback feedback;
    static public Feedback alarmFeedback;
    static public LocationService gps;
    static public boolean alarmButtonFlag = false; // Была ли нажата кнопка SOS, 1 - была нажата / 0 - не была
    static public int gpsEnabledFlag = 1; // Включена ли передача геоданных, 1 - вкл / 0 - выкл
    static public int sibConnectedFlag = 0; // Установлено ли соединение с SIB'ом, 1 - установлено / 0 - не установлено
    static public int backgroundFlag = 0; // Добровольное ли закрытие активности (инициировано из приложения),
    // 1 - добровольное / 0 - недобровольное
    // Перед каждым переходом на другую активность устанавливаем флаг = 1
    static public int patientUriFlag = -1; // Статус пользователя, -1 - первый запуск приложения / 1 - зарегистрированный пользователь / 0 - незарегистрированный пользователь
    static public int netFlag = 0; // Установлено ли соединение с интернетом, 1 - установлено / 0 - не установлено

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        Log.d(TAG, "onCreate Main Activity");
        super.onCreate(savedInstanceState);
        // Установка ТОЛЬКО вертикальной ориентации
        // Такая строка должна быть прописана в КАЖДОЙ активности
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        smart = new SmartCareLibrary();
        setLoadingScreen();
//        if (connectedState == false) {
//            setRegisteredScreen();
//        } else {
//            // Стартовое окно при подключенном bluetooth-устройстве
//            // FIXME не работает
//            setConnectedToDriverState();
//        }
        mContext = this;
    }

    // Загрузочный экран.
    // Осуществляется подготовка к работе
    public void setLoadingScreen() {
        setContentView(R.layout.screen_main_loading);

        ProgressBar mLoadingProgressBar;
        mLoadingProgressBar = (ProgressBar) findViewById(R.id.loadingProgressBar);
        assert mLoadingProgressBar != null;
        mLoadingProgressBar.setVisibility(View.VISIBLE);

        final Button WifiButton = (Button) findViewById(R.id.WifiButton);
        assert WifiButton != null;
        WifiButton.setVisibility(View.INVISIBLE);
        WifiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLoadingScreen();
            }
        });

        if (isNetworkAvailable(this)) {
            netFlag = 1;
            storage = new AccountStorage();
            storage.sPref = getSharedPreferences(AccountStorage.ACCOUNT_PREFERENCES, MODE_PRIVATE);
            if (ConnectToSmartSpace()) {
                GPSLoad gpsLoad = new GPSLoad(context);
                gpsLoad.execute();

                if (storage.getAccountFirstName().isEmpty() || storage.getAccountSecondName().isEmpty()) {
                    setUnregisteredScreen();
                } else {
                    setRegisteredScreen();
                }
            } else {
                android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(this);
                alertDialog.setTitle(R.string.dialog_ss_title);
                alertDialog.setMessage(R.string.dialog_ss_message);
                alertDialog.setPositiveButton(R.string.dialog_ss_positive_button,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                setLoadingScreen();
                            }
                        });
                alertDialog.setNegativeButton(R.string.dialog_ss_negative_button,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                alertDialog.show();
            }
        } else {
            netFlag = 0;
            android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(this);
            alertDialog.setTitle(R.string.dialog_wifi_title);
            alertDialog.setMessage(R.string.dialog_wifi_message);
            alertDialog.setPositiveButton(R.string.dialog_wifi_positive_button,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                            WifiButton.setVisibility(View.VISIBLE);
                        }
                    });
            alertDialog.setNegativeButton(R.string.dialog_wifi_negative_button,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            setLoadingScreen();
                        }
                    });
            alertDialog.show();
        }
    }

    // Интерфейс для незарегистрированного пользователя
    public void setUnregisteredScreen() {
        setContentView(R.layout.screen_main_unregistered);
//        Log.i(TAG, "setUnregisteredActivity see");
        patientUriFlag = 0;
        if (patientUri == null) {
            return;
        }

        etFirstName = (EditText) findViewById(R.id.etFirstName);
        etSecondName = (EditText) findViewById(R.id.etSecondName);

        etSecondName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_NULL) {
                    registration(etFirstName.getText().toString(), etSecondName.getText().toString());
                    return true;
                }
                return false;
            }
        });

        nextButton = (Button) findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registration(etFirstName.getText().toString(), etSecondName.getText().toString());
            }
        });
    }

    // Регистрация
    public void registration(String first, String second) {
        if (first.isEmpty() || second.isEmpty()) {
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this, R.style.AppBaseTheme);
            builder.setTitle(R.string.dialog_title);
            builder.setMessage(R.string.dialog_message);
            builder.setPositiveButton(R.string.dialog_ok, null);
            builder.setNegativeButton(R.string.dialog_cancel, null);
            builder.show();
        } else {
            storage.setAccountPreferences(patientUri, first, second, "", "", "", "", "", "0");
            setRegisteredScreen();
        }
    }

    // Интерфейс для зарегистрированного пользователя
    public void setRegisteredScreen() {
        setContentView(R.layout.main);
        patientUriFlag = 1;
//        registerReceiver(connectReceiver, new IntentFilter(???));
//        btnStart = (Button) findViewById(R.id.start);
//        btnStart.setOnClickListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_activity_toolbar);
        setSupportActionBar(toolbar);

        connectListView = (ListView) findViewById(R.id.ConnectListView);

        connectListArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        connectListView.setAdapter(connectListArrayAdapter);
        connectListArrayAdapter.add("Alive Bluetooth Monitor");
        connectListArrayAdapter.add("ECG-BTLE");

                connectListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        backgroundFlag = 1;
                        //TODO выбор способа подключения
                        Intent intentBluetoothFind = new Intent(getApplicationContext(), BluetoothFindActivity.class);
                        intentBluetoothFind.putExtra("deviceType", id);
                        //TODO change methods
                        startActivity(intentBluetoothFind);
                    }
                });

        serveyButton = (ImageButton) findViewById(R.id.serveyButton);
        serveyButton.setOnClickListener(new ImageButton.OnClickListener() {
            public void onClick(View v) {
                if (isNetworkAvailable(context)) {
                    backgroundFlag = 1;
                    QuestionnaireHelper.showQuestionnaire(context);
                    serveyButton.setEnabled(false);
                } else {
                    setLoadingScreen();
                }
            }
        });

        ImageButton docsButton = (ImageButton) findViewById(R.id.docsButton);
        assert docsButton != null;
        docsButton.setOnClickListener(new ImageButton.OnClickListener() {
            public void onClick(View v) {
                if (isNetworkAvailable(context)) {
                    backgroundFlag = 1;
                    startActivity(new Intent(getApplicationContext(), DocumentsActivity.class));
                } else {
                    setLoadingScreen();
                }
            }
        });

        alarmButton = (Button) findViewById(R.id.alarmButton);
//        Display display = getWindowManager().getDefaultDisplay();
//        DisplayMetrics metricsB = new DisplayMetrics();
//        display.getMetrics(metricsB);

        alarmButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable(context)) {
                    backgroundFlag = 1;
                    if (!gps.canGetLocation()) {
                        alarmButtonFlag = true;
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                        alertDialog.setTitle(R.string.dialog_sos_title);
                        alertDialog.setMessage(R.string.dialog_sos_message);
                        alertDialog.setPositiveButton(R.string.dialog_sos_positive_button, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Переход к настройкам GPS
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                context.startActivity(intent);
                            }
                        });
                        alertDialog.setNegativeButton(R.string.dialog_sos_negative_button, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                GPSLoad gpsLoad2 = new GPSLoad(context);
                                gpsLoad2.execute();
                                dialog.cancel();
                            }
                        });
                        alertDialog.show();
                    } else {
                        alarmButton.setEnabled(false);
                        alarmButton.setBackgroundColor(0x77a71000);
                        alarmUri = smart.sendAlarm(nodeDescriptor, patientUri);
                        alarmButtonFlag = false;
                        QuestionnaireHelper.showAlarmQuestionnaire(context);
                    }
                } else {
                    setLoadingScreen();
                }
            }
        });
        SmartCareLibrary.insertPersonName(nodeDescriptor, patientUri, storage.getAccountFirstName() + " " + storage.getAccountSecondName());
    }

    // Древняя функция. Не используется
    public void setConnectedToDriverState() {
        setContentView(R.layout.screen_main_bluetooth_connected);
        btnDisconnect = (Button) findViewById(R.id.disconnect);
        btnDisconnect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                connectedState = false;
                setRegisteredScreen();
            }
        });

        btnCont = (Button) findViewById(R.id.continueConnection);
        btnCont.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                backgroundFlag = 1;
                Intent intentECG = new Intent(context, ECGActivity.class);
                // TODO change methods
                startActivity(intentECG);
//                 startActivityForResult(intent,1);
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_activity_toolbar_connected);
        setSupportActionBar(toolbar);
    }

    // Древняя функция. Не используется
    final BroadcastReceiver connectReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            connectListArrayAdapter.add("Alive Bluetooth Monitor");

            connectListArrayAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Тулбар
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if ( item.getItemId() == R.id.action_settings) {
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.ecg:
                backgroundFlag = 1;
                Intent intent4 = new Intent(this, ECGActivity.class);
                startActivity(intent4);
                break;
            case R.id.menuAbout:
                backgroundFlag = 1;
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                break;
            case R.id.passSurvey:
                if (isNetworkAvailable(context)) {
                    backgroundFlag = 1;
                    QuestionnaireHelper.showQuestionnaire(context);
                } else {
                    setLoadingScreen();
                }
                break;
            case R.id.exitAccount:
                if (isNetworkAvailable(context)) {
                    backgroundFlag = 0;
                    patientUriFlag = -1;
                    storage.setAccountPreferences("", "", "", "", "", "", "", "", "0");
                    DisconnectFromSmartSpace();
                    setLoadingScreen();
                    deleteFile("feedback.json");
                    deleteFile("alarmFeedback.json");
                } else {
                    setLoadingScreen();
                }
                break;
            case R.id.menuHelp:
                backgroundFlag = 1;
                Intent intent2 = new Intent(this, Help.class);
                startActivity(intent2);
                break;
            case R.id.documentsData:
                if (isNetworkAvailable(context)) {
                    backgroundFlag = 1;
                    startActivity(new Intent(this, DocumentsActivity.class));
                } else {
                    setLoadingScreen();
                }
                break;
            case R.id.menuUserData:
                if (isNetworkAvailable(context)) {
                    backgroundFlag = 1;
                    //TODO Переделать (откуда берутся настройки юзера БД?)
                    if (!loginState) {
                        Intent intent3 = new Intent(this, Login.class);
                        startActivity(intent3);
                    } else {
                        startActivity(new Intent(this, Userdata.class));
                    }
                } else {
                    setLoadingScreen();
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // Старинная функция. Не используется
//    public static void setLoginState(boolean state) {
//        loginState = state;
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Log.e("TAG", "onActivityResult ");
        if (data == null) {
            return;
        }
        String adress = data.getStringExtra("adress");
//        Log.i("TAG", "adress " + adress);
    }

    // Проверка подключения к интернету
    // Если подключение установлено, возвращает True, иначе False
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    // Подключение к интеллектуальному пространству
    static public boolean ConnectToSmartSpace() {
        //Если есть доступ к интернету и соединение с SIB'ом не установлено, то устанавливаем его
        if ((netFlag == 1) && (sibConnectedFlag != 1)) {
//            Log.i(TAG,"ПОДКЛЮЧАЕМСЯ К СИБУ");
            nodeDescriptor = smart.connectSmartSpace("X", "78.46.130.194", 10010);
            if (nodeDescriptor == -1) {
                Intent intent = new Intent(mContext, MainActivity.class);
                mContext.startActivity(intent);
                return false;
            } else {
                // Если удалось подключиться к SIB'у, то устанавливаем соответствующий флаг
                sibConnectedFlag = 1;
            }
            InitObjects();
        }
        return true;
    }

    // Инициализация объектов в интеллектуальном пространстве
    static public boolean InitObjects() {
        if (backgroundFlag == 0) {
//            Log.i(TAG, "СОЗДАНИЕ НОВЫХ ОБЪЕКТОВ");
            feedbackUri = SmartCareLibrary.initFeedback();
            feedback = new Feedback(feedbackUri, "Student", "feedback");
            alarmFeedbackUri = SmartCareLibrary.initFeedback();
            alarmFeedback = new Feedback(alarmFeedbackUri, "Student", "alarmFeedback");

            if (storage.getAccountFirstName().isEmpty() || storage.getAccountSecondName().isEmpty()) {
                patientUri = smart.initPatient(nodeDescriptor);
            } else {
                if ((patientUriFlag == 1) || (patientUriFlag == -1)) {
                    patientUri = storage.getAccountId();
                    smart.initPatientWithId(nodeDescriptor, patientUri);
                    SmartCareLibrary.insertPersonName(nodeDescriptor, patientUri, storage.getAccountFirstName() + " " + storage.getAccountSecondName());
                }
            }
            locationUri = smart.initLocation(nodeDescriptor, patientUri);
        }
        backgroundFlag = 0;
        return true;
    }

    // Отключение от интеллектуального пространства
    static public boolean DisconnectFromSmartSpace() {
        // Разрываем соединение, если оно было установлено ранее
        if (sibConnectedFlag == 1) {
//            Log.i(TAG, "РАЗРЫВАЕМ СОЕДИНЕНИЕ");
            smart.removeIndividual(nodeDescriptor, locationUri);
            smart.removeIndividual(nodeDescriptor, patientUri);
            smart.removeIndividual(nodeDescriptor, feedbackUri);
            smart.removeIndividual(nodeDescriptor, alarmUri);
            smart.removeIndividual(nodeDescriptor, alarmFeedbackUri);
            smart.disconnectSmartSpace(nodeDescriptor);
            nodeDescriptor = -1;
            sibConnectedFlag = -1;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        backgroundFlag = 0;
        Intent intent = getIntent();

        // Проверяем каким способом запущено приложение (обычным или через виджет)
        if (intent.getAction().equalsIgnoreCase(SosWidget.ACTION_WIDGET)) {
            int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
            Bundle extras = intent.getExtras();
            if (extras != null) {
                mAppWidgetId = extras.getInt(
                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID);

            }
            // Если приложение запустили с помощью виджета "ТРЕВОГА"
            // то отправляем сигнал SOS и открываем экстренный опросник
            if (mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                setLoadingScreen();
                if (patientUriFlag == 1) {
                    if ((isNetworkAvailable(context)) && (ConnectToSmartSpace()) && (gps.canGetLocation())) {
                        alarmUri = MainActivity.smart.sendAlarm(MainActivity.nodeDescriptor, MainActivity.patientUri);
                        alarmButtonFlag = false;
                        backgroundFlag = 1;
                        QuestionnaireHelper.showAlarmQuestionnaire(context);
                    }
                } else {
                    Toast toast = Toast.makeText(mContext,
                            R.string.unregistered_user_toast, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
            // Если приложение запустили обычным способом
        } else {
            // Условие выполняется только для авторизированного пользователя
            if (patientUriFlag == 1) {
                // Если с момента последнего прохождения периодического опроса прошла минута, то
                // делаем иконку опроса красной. Короткий промежуток времени (1 минута) - для демонстрации
                Long timestamp = System.currentTimeMillis() / 1000;
                String ts = timestamp.toString();
                Integer time = Integer.parseInt(ts) - Integer.parseInt(storage.getLastQuestionnairePassDate());
                if (time >= 60) {
                    serveyButton.setBackgroundResource(R.drawable.servey);
                } else {
                    serveyButton.setBackgroundResource(R.drawable.servey_white);
                }
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        ConnectToSmartSpace();
    }

    @Override
    public void onStop() {
        super.onStop();
        // Если активность закрывается не из приложения, то разрываем соединение с SIB'ом
        if (backgroundFlag == 0) {
            DisconnectFromSmartSpace();
        }
        backgroundFlag = 0;
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy Main Activity");
        // Старинный комментарий: TODO unregisterReceiver(connectReceiver);
        if (backgroundFlag == 0) {
            DisconnectFromSmartSpace();
        }
        backgroundFlag = 0;
        patientUriFlag = -1;
        super.onDestroy();
    }
}