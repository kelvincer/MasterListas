package org.kcervan.masterlistas;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Kelvin on 12/11/2017.
 */

public class ListaAdapter extends RecyclerView.Adapter<ListaAdapter.ListaViewHolder>{

    private List<Lista> items;

    public ListaAdapter(List<Lista> items) {
        this.items = items;
    }

    @Override
    public ListaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.elemento_lista, parent, false);
        return new ListaViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ListaViewHolder viewHolder, int i) {

        viewHolder.imagen.setImageResource(items.get(i).getImagen());
        viewHolder.nombre.setText(items.get(i).getNombre());
        viewHolder.elementos.setText("Elementos:"
                +String.valueOf(items.get(i).getElementos()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ListaViewHolder extends RecyclerView.ViewHolder {

        public ImageView imagen;
        public TextView nombre;
        public TextView elementos;

        public ListaViewHolder(View v) {
            super(v);
            imagen = (ImageView) v.findViewById(R.id.imagen);
            nombre = (TextView) v.findViewById(R.id.nombre);
            elementos = (TextView) v.findViewById(R.id.elementos);
        }
    }
}
