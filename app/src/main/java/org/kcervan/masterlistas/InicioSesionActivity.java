package org.kcervan.masterlistas;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class InicioSesionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_sesion);

        Button buttonBloqueo = (Button) findViewById(R.id.boton_facebook);
        buttonBloqueo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                incrementaIndiceDeBloqueo(view);
            }
        });
        Button buttonANR = (Button) findViewById(R.id.boton_google);
        buttonANR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                incrementaIndiceDeANR(view);

            }
        });

        MobileAds.initialize(this, "ca-app-pub-9780467258849720~7403996935");

        extraerKeyHash();
    }

    public void loguearCheckbox(View v) {
        CheckBox recordarme = (CheckBox) findViewById(R.id.recordarme);
        String s = getString(R.string.recordar_datos) +
                (recordarme.isChecked() ? getString(R.string.si) : getString(R.string.no));
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    public void mostrarContrasenia(View v) {
        EditText contraseña = (EditText) findViewById(R.id.contraseña);
        CheckBox mostrar = (CheckBox) findViewById(R.id.mostrar_contraseña);
        if (mostrar.isChecked()) {
            contraseña.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_NORMAL);
        } else {
            contraseña.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
    }

    public void acceder(View view) {
        Intent intent = new Intent(this, ListasActivity.class);
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    public void borrarCampos(View view) {
        EditText usuario = (EditText) findViewById(R.id.usuario);
        EditText contraseña = (EditText) findViewById(R.id.contraseña);
        usuario.setText("");
        contraseña.setText("");
        usuario.requestFocus();
    }

    public void registrar(View view) {

        EditText usuario = (EditText) findViewById(R.id.usuario);

        Intent intent = new Intent(this, RegistroActivity.class);
        if (!usuario.getText().toString().isEmpty())
            intent.putExtra("email", usuario.getText().toString());

        startActivity(intent);
    }

    private ArrayList bloqueo;

    public void incrementaIndiceDeBloqueo(View view) {
        bloqueo.add(null);
    }

    public void incrementaIndiceDeANR(View view) {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void extraerKeyHash() {
        String packageName = getPackageName();
        try {
            PackageInfo info = this.getPackageManager()
                    .getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyLog", "KeyHash:" +
                        Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {
        }
    }
}
