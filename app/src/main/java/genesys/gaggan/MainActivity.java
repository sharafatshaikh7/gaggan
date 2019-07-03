package genesys.gaggan;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.github.petr_s.nmea.basic.BasicNMEAHandler;
import com.github.petr_s.nmea.basic.BasicNMEAParser;

import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements LocationListener, GpsStatus.Listener, GpsStatus.NmeaListener {
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVER_BT = 1;

    TextView mStatusBlueTv, mPairedTv;
    ImageView mBlueIv;
    Button mOnBtn, mOffBtn, mDiscoverBtn, mPairedBtn;

    BluetoothAdapter mBlueAdapter;

    LocationManager lm;

    TextView tv_longitude;
    TextView tv_latitude;
    TextView tv_rmc;
    BasicNMEAHandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        tv_longitude=findViewById(R.id.tv_longitude);
        tv_latitude=findViewById(R.id.tv_latitude);
        tv_rmc=findViewById(R.id.tv_rmc);


        mStatusBlueTv = findViewById(R.id.statusBluetoothTv);
        mPairedTv = findViewById(R.id.pairedTv);
        mBlueIv = findViewById(R.id.bluetoothIv);
        mOnBtn = findViewById(R.id.onBtn);
        mOffBtn = findViewById(R.id.offBtn);
        mDiscoverBtn = findViewById(R.id.discoverableBtn);
        mPairedBtn = findViewById(R.id.pairedBtn);

        //adapter
        mBlueAdapter = BluetoothAdapter.getDefaultAdapter();

        //check if bluetooth is available or not
        if (mBlueAdapter == null) {
            mStatusBlueTv.setText("Bluetooth is not available");
        } else {
            mStatusBlueTv.setText("Bluetooth is available");
        }

        //set image according to bluetooth status(on/off)
        if (mBlueAdapter.isEnabled()) {
            mBlueIv.setImageResource(R.mipmap.ic_launcher);
        } else {
            mBlueIv.setImageResource(R.mipmap.ic_launcher);
        }

        //on btn click
        mOnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBlueAdapter.isEnabled()) {
                    showToast("Turning On Bluetooth...");
                    //intent to on bluetooth
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);
                } else {
                    showToast("Bluetooth is already on");
                }
            }
        });
        //discover bluetooth btn click
        mDiscoverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBlueAdapter.isDiscovering()) {
                    showToast("Making Your Device Discoverable");
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivityForResult(intent, REQUEST_DISCOVER_BT);
                }
            }
        });
        //off btn click
        mOffBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBlueAdapter.isEnabled()) {
                    mBlueAdapter.disable();
                    showToast("Turning Bluetooth Off");
                    mBlueIv.setImageResource(R.mipmap.ic_launcher);
                } else {
                    showToast("Bluetooth is already off");
                }
            }
        });
        //get paired devices btn click
        mPairedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBlueAdapter.isEnabled()) {
                    mPairedTv.setText("Paired Devices");
                    Set<BluetoothDevice> devices = mBlueAdapter.getBondedDevices();
                    for (BluetoothDevice device : devices) {
                        mPairedTv.append("\nDevice: " + device.getName() + ", " + device);
                    }
                } else {
                    //bluetooth is off so can't get paired devices
                    showToast("Turn on bluetooth to get paired devices");
                }
            }
        });


        lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 10, this);
        final Location location=lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        lm.addNmeaListener(this);


            handler=new BasicNMEAHandler() {
            @Override
            public void onStart() {

            }

            @Override
            public void onRMC(long date, long time, double latitude, double longitude, float speed, float direction) {

               Toast.makeText(getApplicationContext(),""+latitude+":::"+longitude,Toast.LENGTH_SHORT).show();
                tv_longitude.setText(""+longitude);
                tv_latitude.setText(""+latitude);
            }

            @Override
            public void onGGA(long time, double latitude, double longitude, float altitude, FixQuality quality, int satellites, float hdop) {

            }

            @Override
            public void onGSV(int satellites, int index, int prn, float elevation, float azimuth, int snr) {

            }

            @Override
            public void onGSA(FixType type, Set<Integer> prns, float pdop, float hdop, float vdop) {

            }

            @Override
            public void onUnrecognized(String sentence) {

            }

            @Override
            public void onBadChecksum(int expected, int actual) {

            }

            @Override
            public void onException(Exception e) {

            }

            @Override
            public void onFinished() {

            }
        };
        BasicNMEAParser parser = new BasicNMEAParser(handler);
        parser.parse("$GPRMC,114951.00,A,1907.62477,N,07252.57268,E,0.018,305.63,120319,,,D,V*19");



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK){
                    //bluetooth is on
                    mBlueIv.setImageResource(R.mipmap.ic_launcher);
                    showToast("Bluetooth is on");
                }
                else {
                    //user denied to turn bluetooth on
                    showToast("could't on bluetooth");
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //toast message function
    private void showToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onGpsStatusChanged(int i) {

    }

    @Override
    public void onNmeaReceived(long l, String s) {

        Log.v("NMEA Sentance",s);
        Log.v("long l :",""+l);
        try {
            if (s.contains("$GPRMC")) {
                String [] a=s.split(",");
             //   Toast.makeText(getApplicationContext(),""+a[2],Toast.LENGTH_SHORT).show();
                //Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
                if(a[2].equals("A")) {

                    String s1=s;

                   parseNmea(s1);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    @Override
    public void onLocationChanged(Location location) {


    }


    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {


    }
    @Override
    public void onProviderEnabled(String s) {

    }
    @Override
    public void onProviderDisabled(String s) {

    }
    public void parseNmea(final String data)
    {
        tv_rmc.setText(data);
        BasicNMEAParser parser = new BasicNMEAParser(handler);
        parser.parse(data.trim());
    }






}