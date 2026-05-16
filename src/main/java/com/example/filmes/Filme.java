package com.example.filmes;

import com.google.firebase.firestore.Exclude;

public class Filme {


    @Exclude
    private String id;

    private String nome;
    private String tipo;
    private String genero;
    private int anoLancamento;
    private double notaPessoal;
    private boolean assistido;

    public Filme() {
    }

    public Filme(String nome, String tipo, String genero,
                 int anoLancamento, double notaPessoal, boolean assistido) {
        this.nome = nome;
        this.tipo = tipo;
        this.genero = genero;
        this.anoLancamento = anoLancamento;
        this.notaPessoal = notaPessoal;
        this.assistido = assistido;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public int getAnoLancamento() {
        return anoLancamento;
    }

    public void setAnoLancamento(int anoLancamento) {
        this.anoLancamento = anoLancamento;
    }

    public double getNotaPessoal() {
        return notaPessoal;
    }

    public void setNotaPessoal(double notaPessoal) {
        this.notaPessoal = notaPessoal;
    }

    public boolean isAssistido() {
        return assistido;
    }

    public void setAssistido(boolean assistido) {
        this.assistido = assistido;
    }
}
