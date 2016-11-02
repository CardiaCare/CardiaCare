package ru.cardiacare.cardiacare.bluetooth;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import ru.cardiacare.cardiacare.ecgviewer.ECGActivity;
import ru.cardiacare.cardiacare.MainActivity;
import ru.cardiacare.cardiacare.R;

/**
 * created by Yulia Zavyalova
 */
public class BluetoothFindActivity extends ActionBarActivity {

    ProgressDialog dialog;
    private static final int REQUEST_ENABLE_BT = 1;
    public static final int MESSAGE_STATE_CHANGE = 0;
    public static final int MESSAGE_DEVICE_NAME = 0;
    public static final String DEVICE_NAME = null;
    public static final int MESSAGE_READ = 0;
    private Button onBtn;
    //private Button offBtn;
    //private Button listBtn;
    private Button findBtn;
    private TextView text;
    private BluetoothAdapter myBluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private ListView myListView;
    private ArrayAdapter<String> BTArrayAdapter;
    private BluetoothService mBluetoothService = null;

    private BluetoothSocket mBluetoothSocket;
    private static final String TAG = "BluetoothFindActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate BluetoothFindActivity Activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetoothfind);

        Toolbar toolbar = (Toolbar) findViewById(R.id.bt_find_activity_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.title_activity_bluetooth_find));

        // кнопка назад в ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //TODO http://developer.alexanderklimov.ru/android/theory/progressdialog.php - связать его хендлером с потоком
        dialog = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
        dialog.setMessage(getString(R.string.bluetoothSearching));
        //dialog.setCancelable(false);
        //dialog.show();
        //убрать диалог
        //dialog.dismiss();

        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(myBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"Your device does not support Bluetooth",
                    Toast.LENGTH_LONG).show();
        } else {
            on();
            myTimerExecute();

            //TODO все включилось, можно писать поиск
            // перенесен в onActivityResult()
            //find();
            myListView = (ListView)findViewById(R.id.mListView);

            // create the arrayAdapter that contains the BTDevices, and set it to the ListView
            BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
            myListView.setAdapter(BTArrayAdapter);
            myListView.setOnItemClickListener(new OnItemClickListener() {


                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    // TODO Auto-generated method stub
                    // Cancel discovery because it's costly and we're about to connect
                    myBluetoothAdapter.cancelDiscovery();
                    // Get the device MAC address, which is the last 17 chars in the View
                    String info = ((TextView) view).getText().toString();
                    String adress = info.substring(info.length() -17);

                    MainActivity.connectedState = true;
                    Handler handler = new Handler(Looper.getMainLooper());

                    Intent intent = new Intent();
                    intent.putExtra("adress", adress);
                    setResult(RESULT_OK, intent);
                    //finish();

                    mBluetoothService = new BluetoothService(getApplicationContext(), handler);
                    mBluetoothService.connect(adress);

                    startActivity(new Intent(getApplicationContext(), ECGActivity.class));
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bluetoothfind, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*if ( item.getItemId() == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);*/
        switch (item.getItemId()) {
            case R.id.refreshList:
                //TODO чистим список и заполняем заново в потоке
                /*dialog.show();*/
                refresh();
                break;
            case android.R.id.home:
                finish();
                //NavUtils.navigateUpFromSameTask(this);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refresh() {
        myBluetoothAdapter.cancelDiscovery();
        BTArrayAdapter.clear();
        myBluetoothAdapter.startDiscovery();
        dialog.show();
        myTimerExecute();
        registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    }

    private void myTimerExecute() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        };
        timer.schedule(task, 11000);
    }

    public void on(){
        if (!myBluetoothAdapter.isEnabled()) {
//            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);

//            Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
//            startActivity(intent);

            android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(this);
            alertDialog.setTitle("Выключен Bluetooth");
            alertDialog.setMessage("Часть функций может быть не доступна. Желаете перейти к настройкам, чтобы включить его?");
            alertDialog.setPositiveButton("Настройки",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                        }
                    });

            alertDialog.setNegativeButton("Отмена",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }
                    });

            alertDialog.show();

            Toast.makeText(getApplicationContext(), "Bluetooth turned on",
                           Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(),"Bluetooth is already on",
                           Toast.LENGTH_LONG).show();
            myBluetoothAdapter.startDiscovery();
            dialog.show();
            //Если вернуться стрелочкой "назад" на главный экран с экрана посика устройств,
            //то приложение не упадёт, но выдаст ошибку, указывая на строчку ниже
            registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("BT", "onActivityResultonActivityResultonActivityResult");
        // TODO Auto-generated method stub
        if(requestCode == REQUEST_ENABLE_BT){
            if(myBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "Status: enabled",
                        Toast.LENGTH_LONG).show();
                find();
            } else {
                Toast.makeText(getApplicationContext(), "Status: disabled",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name and the MAC address of the object to the arrayAdapter
                BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                BTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    public void find() {
        if (myBluetoothAdapter.isDiscovering()) {
            // the button is pressed when it discovers, so cancel the discovery
            myBluetoothAdapter.cancelDiscovery();
        } else {
            BTArrayAdapter.clear();
            myBluetoothAdapter.startDiscovery();
            dialog.show();

            registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy BluetoothFindActivity Activity");
        // TODO Auto-generated method stub
        super.onDestroy();
//        Error!!! Зачем нужно?
//        unregisterReceiver(bReceiver);
        registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

    }
}
