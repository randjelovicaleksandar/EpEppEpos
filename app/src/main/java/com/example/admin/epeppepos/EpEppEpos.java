package com.example.admin.epeppepos;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Spinner;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Toast;


public class EpEppEpos extends AppCompatActivity {


    Integer [] frekvencije= {8000,11025,16000,22050,32000,37800,44100};
    ImageButton startSnim, zavrsiSnim, pustiPonovo;
    Boolean snimanje;
    Spinner spinFrek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);


        setContentView(R.layout.content_ep_epp_epos);


        spinFrek = (Spinner) findViewById(R.id.spinFrek);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.skup_frekvencija, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinFrek.setAdapter(adapter);
        startSnim = (ImageButton)findViewById(R.id.startSnim);
        zavrsiSnim = (ImageButton)findViewById(R.id.zavrsiSnim);
        pustiPonovo = (ImageButton)findViewById(R.id.pustiPonovo);

        startSnim.setOnClickListener(startSnimOnClickListener);
        zavrsiSnim.setOnClickListener(zavrsiSnimOnClickListener);
        pustiPonovo.setOnClickListener(pustiPonovoOnClickListener);
        zavrsiSnim.setEnabled(false);

    }

OnClickListener startSnimOnClickListener
        = new OnClickListener(){

    @Override
    public void onClick(View arg0) {

        Thread recordThread = new Thread(new Runnable(){

            @Override
            public void run() {
                snimanje = true;
                startRecord();
            }

        });

        recordThread.start();
        startSnim.setEnabled(false);
        startSnim.setImageResource(R.drawable.recordcrno);
        zavrsiSnim.setEnabled(true);
        zavrsiSnim.setImageResource(R.drawable.stopcrveno);
    }};

OnClickListener zavrsiSnimOnClickListener
        = new OnClickListener(){

    @Override
    public void onClick(View arg0) {
        snimanje = false;
        startSnim.setEnabled(true);
        startSnim.setImageResource(R.drawable.recordcrveno);
        zavrsiSnim.setEnabled(false);
        zavrsiSnim.setImageResource(R.drawable.stopcrno);
    }};

OnClickListener pustiPonovoOnClickListener
        = new OnClickListener(){

    @Override
    public void onClick(View v) {
        playRecord();
    }

};

    private void startRecord(){

        File file = new File(Environment.getExternalStorageDirectory(), "test.pcm");

        int izabranaPozicija = spinFrek.getSelectedItemPosition();
        int frekvencija = frekvencije[izabranaPozicija];

        final String promptStartRecord =
                "Snimanje zapoceto \n"
                        + file.getAbsolutePath() + "\n"
                        + (String)spinFrek.getSelectedItem();

        runOnUiThread(new Runnable(){

            @Override
            public void run() {
                Toast.makeText(EpEppEpos.this,
                        promptStartRecord,
                        Toast.LENGTH_LONG).show();
            }});

        try {
            file.createNewFile();

            OutputStream outputStream = new FileOutputStream(file);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);

            int minBufferSize = AudioRecord.getMinBufferSize(frekvencija,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);

            short[] audioData = new short[minBufferSize];

            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    frekvencija,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufferSize);

            audioRecord.startRecording();

            while(snimanje){
                int numberOfShort = audioRecord.read(audioData, 0, minBufferSize);
                for(int i = 0; i < numberOfShort; i++){
                    dataOutputStream.writeShort(audioData[i]);
                }
            }

            audioRecord.stop();
            dataOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void playRecord(){

        File file = new File(Environment.getExternalStorageDirectory(), "test.pcm");

        int shortSizeInBytes = Short.SIZE/Byte.SIZE;

        int bufferSizeInBytes = (int)(file.length()/shortSizeInBytes);
        short[] audioData = new short[bufferSizeInBytes];

        try {
            InputStream inputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);

            int i = 0;
            while(dataInputStream.available() > 0){
                audioData[i] = dataInputStream.readShort();
                i++;
            }

            dataInputStream.close();

            int selectedPos = spinFrek.getSelectedItemPosition();
            int sampleFreq = frekvencije[selectedPos];

            final String promptPlayRecord =
                    "Pusti Snimak\n"
                            + file.getAbsolutePath() + "\n"
                            + (String)spinFrek.getSelectedItem();

            Toast.makeText(EpEppEpos.this,
                    promptPlayRecord,
                    Toast.LENGTH_LONG).show();

            AudioTrack audioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleFreq,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSizeInBytes,
                    AudioTrack.MODE_STREAM);

            audioTrack.play();
            audioTrack.write(audioData, 0, bufferSizeInBytes);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }
}
