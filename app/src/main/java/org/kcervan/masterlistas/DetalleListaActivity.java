package org.kcervan.masterlistas;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

public class DetalleListaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_lista);

        int numeroLista = (int) getIntent().getExtras().getSerializable("numeroLista");
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.textWhite));
        toolbar.setTitle("");
        ImageView imageView = (ImageView) findViewById(R.id.imagen);
        if (numeroLista == 0) {
            toolbar.setTitle(R.string.trabajo_titulo);
            imageView.setImageResource(R.drawable.trabajo);
        } else {
            toolbar.setTitle(R.string.personal_titulo);
            imageView.setImageResource(R.drawable.casa);
        }
    }
}
