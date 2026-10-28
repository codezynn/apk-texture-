package com.zyn.codmflat;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

public class MainActivity extends Activity {
    private static final String PKG = "com.garena.game.codm";
    private static final String DIR = "/data/data/" + PKG + "/files/";
    private TextView status, log;
    private RadioGroup lookGroup, strGroup;
    private boolean hasRoot = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        status = findViewById(R.id.status);
        log = findViewById(R.id.log);
        lookGroup = findViewById(R.id.lookGroup);
        strGroup = findViewById(R.id.strGroup);
        Button apply = findViewById(R.id.apply);

        checkRoot();
        apply.setOnClickListener(v -> applySettings());
    }

    private void checkRoot() {
        new Thread(() -> {
            String out = exec("id -u");
            hasRoot = out != null && out.trim().equals("0");
            runOnUiThread(() -> status.setText(hasRoot ? "Root: detected" : "Root: not detected"));
        }).start();
    }

    private void applySettings() {
        if (!hasRoot) {
            Toast.makeText(this, "Root required", Toast.LENGTH_SHORT).show();
            return;
        }
        int look = lookGroup.getCheckedRadioButtonId();
        int str = strGroup.getCheckedRadioButtonId();
        int flat = 0;
        if (look == R.id.flat) {
            if (str == R.id.normal) flat = 1;
            else if (str == R.id.medium) flat = 2;
            else if (str == R.id.extreme) flat = 3;
            else flat = 2;
        }
        final int f = flat;
        log.setText("Writing settings…");
        new Thread(() -> {
            String cmd = "mkdir -p " + DIR + " && " +
                         "printf '%d\\n' " + f + " > " + DIR + "flat.txt && " +
                         "chmod 666 " + DIR + "flat.txt && " +
                         "am force-stop " + PKG + " && " +
                         "sleep 1 && " +
                         "am start -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -p " + PKG;
            String res = exec(cmd);
            runOnUiThread(() -> {
                log.setText(res != null ? res : "done");
                Toast.makeText(MainActivity.this, "Applied. CODM reopened.", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private String exec(String cmd) {
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
            p.waitFor();
            return sb.toString();
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
