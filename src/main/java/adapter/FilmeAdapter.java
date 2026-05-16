package adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filmes.R;
import com.example.filmes.Filme;

import java.util.List;
import java.util.Locale;


public class FilmeAdapter extends RecyclerView.Adapter<FilmeAdapter.FilmeViewHolder> {

    // Interfaces de callback: a Activity decide o que fazer em cada clique.
    public interface OnItemClickListener {
        void onItemClick(Filme filme);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Filme filme);
    }

    private final List<Filme> listaFilmes;
    private final OnItemClickListener clickListener;
    private final OnItemLongClickListener longClickListener;

    public FilmeAdapter(List<Filme> listaFilmes,
                        OnItemClickListener clickListener,
                        OnItemLongClickListener longClickListener) {
        this.listaFilmes = listaFilmes;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public FilmeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_filme, parent, false);
        return new FilmeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilmeViewHolder holder, int position) {
        Filme filme = listaFilmes.get(position);

        holder.txtNome.setText(filme.getNome());

        // Monta a linha "Tipo - Genero - Ano".
        String detalhes = filme.getTipo() + "  -  " + filme.getGenero()
                + "  -  " + filme.getAnoLancamento();
        holder.txtDetalhes.setText(detalhes);

        // Nota pessoal formatada com uma casa decimal.
        holder.txtNota.setText(String.format(Locale.getDefault(),
                "Nota: %.1f", filme.getNotaPessoal()));

        // Mostra se a obra ja foi assistida.
        holder.txtAssistido.setText(filme.isAssistido()
                ? "Assistido" : "Nao assistido");

        // Clique curto -> editar.
        holder.itemView.setOnClickListener(v -> clickListener.onItemClick(filme));

        // Clique longo -> excluir.
        holder.itemView.setOnLongClickListener(v -> {
            longClickListener.onItemLongClick(filme);
            return true; // consome o evento
        });
    }

    @Override
    public int getItemCount() {
        return listaFilmes.size();
    }

    /** ViewHolder: guarda as referencias das views de cada item da lista. */
    static class FilmeViewHolder extends RecyclerView.ViewHolder {
        TextView txtNome, txtDetalhes, txtNota, txtAssistido;

        FilmeViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNome = itemView.findViewById(R.id.txtNome);
            txtDetalhes = itemView.findViewById(R.id.txtDetalhes);
            txtNota = itemView.findViewById(R.id.txtNota);
            txtAssistido = itemView.findViewById(R.id.txtAssistido);
        }
    }
}
