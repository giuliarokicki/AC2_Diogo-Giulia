package com.example.filmes;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import adapter.FilmeAdapter;
import com.example.filmes.Filme;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity {


    private EditText editNome, editAno, editNota;
    private Spinner spinnerTipo, spinnerGenero;
    private CheckBox checkAssistido;
    private Button btnSalvar, btnLimpar, btnOrdenarNota;


    private Spinner spinnerFiltroTipo;
    private EditText editBusca;
    private RecyclerView recyclerView;

    private FirebaseFirestore db;
    private static final String COLECAO = "filmes";

    private final List<Filme> listaCompleta = new ArrayList<>();
    private final List<Filme> listaExibida = new ArrayList<>();
    private FilmeAdapter adapter;

    private String idEmEdicao = null;

    private boolean ordemNotaDecrescente = true;

    private final String[] tipos =
            {"Selecione o tipo", "Filme", "Serie", "Documentario", "Animacao"};
    private final String[] generos =
            {"Selecione o genero", "Acao", "Comedia", "Drama", "Terror", "Ficcao Cientifica"};
    private final String[] filtrosTipo =
            {"Todos", "Filme", "Serie", "Documentario", "Animacao"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        vincularComponentes();
        configurarSpinners();
        configurarRecyclerView();
        configurarEventos();


        carregarFilmes();
    }


    private void vincularComponentes() {
        editNome = findViewById(R.id.editNome);
        editAno = findViewById(R.id.editAno);
        editNota = findViewById(R.id.editNota);
        spinnerTipo = findViewById(R.id.spinnerTipo);
        spinnerGenero = findViewById(R.id.spinnerGenero);
        checkAssistido = findViewById(R.id.checkAssistido);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnLimpar = findViewById(R.id.btnLimpar);
        btnOrdenarNota = findViewById(R.id.btnOrdenarNota);

        spinnerFiltroTipo = findViewById(R.id.spinnerFiltroTipo);
        editBusca = findViewById(R.id.editBusca);
        recyclerView = findViewById(R.id.recyclerViewFilmes);
    }

    private void configurarSpinners() {
        spinnerTipo.setAdapter(criarAdapterSpinner(tipos));
        spinnerGenero.setAdapter(criarAdapterSpinner(generos));
        spinnerFiltroTipo.setAdapter(criarAdapterSpinner(filtrosTipo));
    }

    private ArrayAdapter<String> criarAdapterSpinner(String[] dados) {
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, dados);
        adapterSpinner.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        return adapterSpinner;
    }

    private void configurarRecyclerView() {
        adapter = new FilmeAdapter(
                listaExibida,
                this::carregarParaEdicao,
                this::confirmarExclusao);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void configurarEventos() {
        btnSalvar.setOnClickListener(v -> salvarFilme());
        btnLimpar.setOnClickListener(v -> limparFormulario());
        btnOrdenarNota.setOnClickListener(v -> ordenarPorNota());

        editBusca.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                aplicarFiltros();
            }
        });

        spinnerFiltroTipo.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override public void onItemSelected(android.widget.AdapterView<?> parent,
                                                         android.view.View view,
                                                         int position, long id) {
                        aplicarFiltros();
                    }
                    @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                });
    }

    private boolean validarCampos() {
        String nome = editNome.getText().toString().trim();
        String ano = editAno.getText().toString().trim();
        String nota = editNota.getText().toString().trim();

        if (nome.isEmpty()) {
            editNome.setError("Informe o nome");
            editNome.requestFocus();
            return false;
        }
        if (spinnerTipo.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Selecione o tipo", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (spinnerGenero.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Selecione o genero", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (ano.isEmpty()) {
            editAno.setError("Informe o ano de lancamento");
            editAno.requestFocus();
            return false;
        }
        if (nota.isEmpty()) {
            editNota.setError("Informe a nota pessoal");
            editNota.requestFocus();
            return false;
        }

        double valorNota = Double.parseDouble(nota);
        if (valorNota < 0 || valorNota > 10) {
            editNota.setError("A nota deve estar entre 0 e 10");
            editNota.requestFocus();
            return false;
        }
        return true;
    }

    private void salvarFilme() {
        if (!validarCampos()) {
            return;
        }

        Filme filme = new Filme(
                editNome.getText().toString().trim(),
                spinnerTipo.getSelectedItem().toString(),
                spinnerGenero.getSelectedItem().toString(),
                Integer.parseInt(editAno.getText().toString().trim()),
                Double.parseDouble(editNota.getText().toString().trim()),
                checkAssistido.isChecked()
        );

        if (idEmEdicao == null) {
            db.collection(COLECAO)
                    .add(filme)
                    .addOnSuccessListener(ref -> {
                        Toast.makeText(this, "Cadastrado com sucesso",
                                Toast.LENGTH_SHORT).show();
                        limparFormulario();
                        carregarFilmes();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Erro ao cadastrar: "
                                    + e.getMessage(), Toast.LENGTH_LONG).show());
        } else {
            db.collection(COLECAO)
                    .document(idEmEdicao)
                    .set(filme)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Atualizado com sucesso",
                                Toast.LENGTH_SHORT).show();
                        limparFormulario();
                        carregarFilmes();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Erro ao atualizar: "
                                    + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }

    private void carregarFilmes() {
        db.collection(COLECAO)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    listaCompleta.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Filme filme = doc.toObject(Filme.class);
                        filme.setId(doc.getId()); // guarda o id do documento
                        listaCompleta.add(filme);
                    }
                    aplicarFiltros();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao carregar dados: "
                                + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void aplicarFiltros() {
        String tipoSelecionado = spinnerFiltroTipo.getSelectedItem().toString();
        String termoBusca = editBusca.getText().toString().trim().toLowerCase();

        listaExibida.clear();
        for (Filme filme : listaCompleta) {
            boolean passaTipo = tipoSelecionado.equals("Todos")
                    || filme.getTipo().equals(tipoSelecionado);

            boolean passaBusca = termoBusca.isEmpty()
                    || filme.getNome().toLowerCase().contains(termoBusca);

            if (passaTipo && passaBusca) {
                listaExibida.add(filme);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void ordenarPorNota() {
        if (ordemNotaDecrescente) {
            Collections.sort(listaExibida, (a, b) ->
                    Double.compare(b.getNotaPessoal(), a.getNotaPessoal()));
            btnOrdenarNota.setText("Nota: maior -> menor");
        } else {
            Collections.sort(listaExibida, (a, b) ->
                    Double.compare(a.getNotaPessoal(), b.getNotaPessoal()));
            btnOrdenarNota.setText("Nota: menor -> maior");
        }
        ordemNotaDecrescente = !ordemNotaDecrescente;
        adapter.notifyDataSetChanged();
    }
    private void confirmarExclusao(@NonNull Filme filme) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir registro")
                .setMessage("Deseja realmente excluir \"" + filme.getNome() + "\"?")
                .setPositiveButton("Excluir", (dialog, which) -> excluirFilme(filme))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void excluirFilme(@NonNull Filme filme) {
        db.collection(COLECAO)
                .document(filme.getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Registro excluido",
                            Toast.LENGTH_SHORT).show();
                    if (filme.getId().equals(idEmEdicao)) {
                        limparFormulario();
                    }
                    carregarFilmes();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao excluir: "
                                + e.getMessage(), Toast.LENGTH_LONG).show());
    }
    private void carregarParaEdicao(@NonNull Filme filme) {
        idEmEdicao = filme.getId();

        editNome.setText(filme.getNome());
        editAno.setText(String.valueOf(filme.getAnoLancamento()));
        editNota.setText(String.valueOf(filme.getNotaPessoal()));
        checkAssistido.setChecked(filme.isAssistido());

        spinnerTipo.setSelection(posicaoNoArray(tipos, filme.getTipo()));
        spinnerGenero.setSelection(posicaoNoArray(generos, filme.getGenero()));

        btnSalvar.setText("Atualizar");
        Toast.makeText(this, "Editando: " + filme.getNome(),
                Toast.LENGTH_SHORT).show();
    }

    private int posicaoNoArray(String[] array, String valor) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(valor)) {
                return i;
            }
        }
        return 0;
    }

    private void limparFormulario() {
        idEmEdicao = null;
        editNome.setText("");
        editAno.setText("");
        editNota.setText("");
        checkAssistido.setChecked(false);
        spinnerTipo.setSelection(0);
        spinnerGenero.setSelection(0);
        btnSalvar.setText("Salvar");
        editNome.requestFocus();
    }
}
