package mx.itesm.edu.earthone.spherochallenge;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.orbotix.ConvenienceRobot;
import com.orbotix.DualStackDiscoveryAgent;
import com.orbotix.command.RollCommand;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.ResponseListener;
import com.orbotix.common.Robot;
import com.orbotix.common.RobotChangedStateListener;
import com.orbotix.common.internal.AsyncMessage;
import com.orbotix.common.internal.DeviceResponse;
import com.orbotix.macro.MacroObject;
import com.orbotix.macro.cmd.Delay;
import com.orbotix.macro.cmd.LoopEnd;
import com.orbotix.macro.cmd.LoopStart;
import com.orbotix.macro.cmd.Roll;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements RobotChangedStateListener, ResponseListener{

    private Button go, stop, back, left, right, listen;
    private TextView stt;
    private final int REQUEST_PERMISSION = 42;    //Pedir permiso
    private float ROBOT_SPEED = 0.2f;   //Velocidad de robot
    private ConvenienceRobot convenienceRobot;
    private int direction;
    private final int CHECK_STT = 1007;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        go = (Button) findViewById(R.id.go);
        stop = (Button) findViewById(R.id.stop);
        back = (Button) findViewById(R.id.back);
        left = (Button) findViewById(R.id.left);
        right = (Button) findViewById(R.id.right);
        listen = (Button) findViewById(R.id.talkButton);

        stt = (TextView) findViewById(R.id.voiceText);

        listen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                if(intent.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(intent, CHECK_STT);
                }else{
                    Toast.makeText(getApplicationContext(), "You do not have Speech To Text", Toast.LENGTH_LONG).show();
                }
            }
        });

        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                convenienceRobot.setLed(0.0f, 1.0f, 1.0f);
                direction = 180;
                convenienceRobot.drive(direction, ROBOT_SPEED);
            }
        });


        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                convenienceRobot.setLed(1.0f, 0.0f, 0.0f);
                convenienceRobot.stop();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                convenienceRobot.setLed(1.0f, 0.0f, 1.0f);
                direction = 0;
                convenienceRobot.drive(direction, ROBOT_SPEED);
            }
        });

        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                convenienceRobot.setLed(1.0f, 1.0f, 0.0f);
                direction = 90;
                convenienceRobot.drive(direction, ROBOT_SPEED);
            }
        });

        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                convenienceRobot.setLed(1.0f, 1.0f, 0.0f);
                direction = 270;
                convenienceRobot.drive(direction, ROBOT_SPEED);
            }
        });
        DualStackDiscoveryAgent.getInstance().addRobotStateListener(this);
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            int hasLocationPermission = checkSelfPermission( Manifest.permission.ACCESS_COARSE_LOCATION );
            if( hasLocationPermission != PackageManager.PERMISSION_GRANTED ) {
                Log.e( "Sphero", "Location permission has not already been granted" );
                List<String> permissions = new ArrayList<String>();
                permissions.add( Manifest.permission.ACCESS_COARSE_LOCATION);
                requestPermissions(permissions.toArray(new String[permissions.size()] ), REQUEST_PERMISSION );
            } else {
                Log.d( "Sphero", "Location permission already granted" );
            }
        }
    }

    @Override
    public void handleResponse(DeviceResponse deviceResponse, Robot robot) {

    }

    @Override
    public void handleStringResponse(String s, Robot robot) {

    }

    @Override
    public void handleAsyncMessage(AsyncMessage asyncMessage, Robot robot) {

    }

    @Override
    public void handleRobotChangedState(Robot robot, RobotChangedStateNotificationType robotChangedStateNotificationType) {
        switch (robotChangedStateNotificationType){
            case Online:
                convenienceRobot = new ConvenienceRobot(robot);
                convenienceRobot.addResponseListener(this);
                convenienceRobot.enableCollisions(true);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch ( requestCode ) {
            case REQUEST_PERMISSION: {
                for( int i = 0; i < permissions.length; i++ ) {
                    if( grantResults[i] == PackageManager.PERMISSION_GRANTED ) {
                        startDiscovery();
                        Log.d( "Permissions", "Permission Granted: " + permissions[i] );
                    } else if( grantResults[i] == PackageManager.PERMISSION_DENIED ) {
                        Log.d( "Permissions", "Permission Denied: " + permissions[i] );
                    }
                }
            }
            break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        startDiscovery();

    }

    private void startDiscovery() {
        if( !DualStackDiscoveryAgent.getInstance().isDiscovering() ) {
            try {
                DualStackDiscoveryAgent.getInstance().startDiscovery( this );
            } catch (DiscoveryException e) {
                Log.e("Sphero", "DiscoveryException: " + e.getMessage());
            }
        }
    }

    @Override
    protected void onStop() {
        if( DualStackDiscoveryAgent.getInstance().isDiscovering() ) {
            DualStackDiscoveryAgent.getInstance().stopDiscovery();
        }
        if( convenienceRobot != null ) {
            convenienceRobot.disconnect();
            convenienceRobot = null;
        }

        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK && data != null){
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            stt.setText(result.get(0));
            switch (result.get(0)){
                case "avanza":
                    convenienceRobot.setLed(0.0f, 1.0f, 1.0f);
                    direction = 180;
                    convenienceRobot.drive(direction, ROBOT_SPEED);
                    break;
                case "Gira a la derecha":
                    convenienceRobot.setLed(1.0f, 1.0f, 0.0f);
                    direction = 270;
                    convenienceRobot.drive(direction, ROBOT_SPEED);
                    break;
                case "Gira a la izquierda":
                    convenienceRobot.setLed(1.0f, 1.0f, 0.0f);
                    direction = 90;
                    convenienceRobot.drive(direction, ROBOT_SPEED);
                    break;
                case "retrocede":
                    convenienceRobot.setLed(1.0f, 0.0f, 1.0f);
                    direction = 0;
                    convenienceRobot.drive(direction, ROBOT_SPEED);
                    break;
                case "toda velocidad":
                    convenienceRobot.setLed(1.0f, 1.0f, 1.0f);
                    direction = 180;
                    convenienceRobot.drive(direction, 100);
                    break;
                case "mata":
                    convenienceRobot.setLed(1.0f, 0.0f, 0.0f);
                    direction = 180;
                    MacroObject macroObject = new MacroObject();
                    macroObject.addCommand(new LoopStart(1));
                    macroObject.addCommand(new Roll(0.1f, 0, 0));
                    macroObject.addCommand(new Delay(2000));
                    macroObject.addCommand(new Roll(1.0f, 180, 2000));
                    macroObject.addCommand(new Delay(2000));
                    macroObject.addCommand(new Roll(0.0f, 0, 0));
                    macroObject.addCommand(new LoopEnd());
                    macroObject.setMode(MacroObject.MacroObjectMode.Normal);
                    macroObject.setRobot(convenienceRobot.getRobot());
                    macroObject.playMacro();
                    break;
                default:
                    convenienceRobot.setLed(1.0f, 0.0f, 0.0f);
                    break;
            }
        }
    }
}
