package org.kcervan.masterlistas;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class RegistroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        EditText email = findViewById(R.id.correo);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String correo = bundle.getString("email", null);
            if (correo != null)
                email.setText(correo);
        }
    }

    public void registroSatisfactorio(View view) {

        EditText username = findViewById(R.id.username);
        if (username.getText().toString().isEmpty()) {
            Toast.makeText(this, R.string.ingresar_nombre_usuario, Toast.LENGTH_SHORT).show();
            return;
        }

        EditText correo = findViewById(R.id.correo);
        if (correo.getText().toString().isEmpty()) {
            Toast.makeText(this, R.string.ingresar_correo, Toast.LENGTH_SHORT).show();
            return;
        } else {
            if (correo.getText().toString().length() < 3) {
                Toast.makeText(this, R.string.numero_caracteres_correo, Toast.LENGTH_SHORT).show();
                return;
            }
        }


        EditText contraseña = findViewById(R.id.contraseña);
        if (contraseña.getText().toString().isEmpty()) {
            Toast.makeText(this, R.string.necesitas_ingresar_contrasenia, Toast.LENGTH_SHORT).show();
            return;
        } else {
            if (contraseña.getText().toString().length() < 8) {
                Toast.makeText(this, R.string.caracteres_contra_menor, Toast.LENGTH_SHORT).show();
                return;
            }
        }


        Toast.makeText(this, R.string.registro_satisfactorio, Toast.LENGTH_SHORT).show();
        finish();
    }
}
