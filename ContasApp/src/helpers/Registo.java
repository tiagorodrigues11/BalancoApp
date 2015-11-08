package helpers;

import java.io.Serializable;

public class Registo implements Serializable {
	private static final long serialVersionUID = -8684067370363570994L;
	
	private String descricao;
	private int valor;
	
	public Registo(String descricao, int valor) {
		this.descricao = descricao;
		this.valor = valor;
	}
	
	public String getDescricao() {
		return descricao;
	}
	
	public int getValor() {
		return valor;
	}
	
}
