package gz.cm.modelo;

import java.util.ArrayList;
import java.util.List;

public class Campo{

	private final int linha, coluna;
	private boolean minado = false, aberto = false, marcado = false;
	private List<Campo> vizinhos = new ArrayList<>();
	private List<CampoObserver> observadores = new ArrayList<>();
	
	Campo(int linha, int coluna){
		
		this.linha = linha;
		this.coluna = coluna;
		
	}
	
	public void registrarObservador(CampoObserver observador) {
		
		observadores.add(observador);
		
	}
	
	private void notificarObservadores(CampoEvento evento) {
		observadores.stream().forEach(o -> o.eventoOcorreu(this, evento));
	}
	
	boolean adicionarVizinho(Campo vizinho) {
		boolean linhaDiferente = linha != vizinho.linha;
		boolean colunaDiferente = coluna != vizinho.coluna;
		boolean diagonal = linhaDiferente && colunaDiferente;
		
		int deltaLinha = Math.abs(linha-vizinho.linha);
		int deltaColuna = Math.abs(coluna-vizinho.coluna);
		int deltaGeral = deltaColuna + deltaLinha;
		
		if(deltaGeral==1 && !diagonal) {
			
			vizinhos.add(vizinho);
			return true;
			
		}else if(deltaGeral==2 && diagonal) {
			
			vizinhos.add(vizinho);
			return true;
			
		}else {
		
			return false;
			
		}
	}
	
	public void alternarMarcacao() {
		if(!aberto) {
			
			marcado = !marcado;
			
			if(marcado) {
				notificarObservadores(CampoEvento.MARCAR);
			}else {
				notificarObservadores(CampoEvento.DESMARCAR);
			}
			
		}
	}
	
	public boolean abrir() {
		
		if(!aberto && !marcado) {
			
			if(minado) {
				notificarObservadores(CampoEvento.EXPLODIR);
				return true;
			}
			
			setAberto(true);
			
			if(vizinhancaSegura()) {
				vizinhos.forEach(v -> v.abrir());
			}
			
			return true;
		}else {
		
			return false;
		
		}
	}
	
	public boolean vizinhancaSegura() {
		
		return vizinhos.stream().noneMatch(v -> v.minado);
		
	}
	
	void minar() {
		minado = true;
	}
	
	public boolean isMarcado() {
		
		return marcado;
		
	}
	
	public boolean isAberto() {
		
		return aberto;
		
	}
	
	public int getLinha() {
		
		return linha;
		
	}
	
	public int getColuna() {
		
		return coluna;
		
	}
	
	public boolean isMinado() {
		
		return minado;
		
	}
	
	public void setAberto(boolean aberto) {
		this.aberto = aberto;
		
		if(aberto) {
			notificarObservadores(CampoEvento.ABRIR);
		}
	}
	
	boolean objetivoAlcancado() {
		
		boolean desvendado = !minado && aberto, protegido = minado && marcado;
		
		return desvendado || protegido;
		
	}
	
	public int minasNaVizinhanca() {
		
		return (int) vizinhos.parallelStream().filter(v -> v.minado).count();
		
	}
	
	void reiniciar() {
		
		aberto = false;
		marcado = false;
		minado = false;
		notificarObservadores(CampoEvento.REINICIAR);
		
	}
}
