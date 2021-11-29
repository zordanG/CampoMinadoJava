package gz.cm.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Tabuleiro implements CampoObserver{

	private int linhas, colunas, minas;
	
	private final List<Campo> campos = new ArrayList<>();
	private List <Consumer<ResultadoEvento>> observadores = new ArrayList<>();
	
	public Tabuleiro(int linhas, int colunas, int minas) {
		this.linhas = linhas;
		this.colunas = colunas;
		this.minas = minas;
		
		gerarCampos();
		associarVizinhos();
		sortearMinas();
	}
	
	public void registrarObservador(Consumer<ResultadoEvento> observador) {
		
		observadores.add(observador);
		
	}
	
	public void notificarObservadores(boolean resultado) {
		
		observadores.stream().forEach(o -> o.accept(new ResultadoEvento(resultado)));
		
	}
	
	public void paraCada(Consumer<Campo> funcao) {
		campos.forEach(funcao);
	}
	
	public int getLinhas() {
		return linhas;
	}

	public void setLinhas(int linhas) {
		this.linhas = linhas;
	}

	public int getColunas() {
		return colunas;
	}

	public void setColunas(int colunas) {
		this.colunas = colunas;
	}

	public void abrir(int linha, int coluna) {
	
		campos.parallelStream().filter(c -> c.getLinha()==linha && c.getColuna()==coluna).findFirst().ifPresent(c -> c.abrir());
		
	}
	
	private void mostrarMinas() {
		campos.stream().filter(c -> c.isMinado()).forEach(c -> c.setAberto(true));
	}
	
	public void alterarMarcacao(int linha, int coluna) {
		
		campos.parallelStream().filter(c -> c.getLinha()==linha && c.getColuna()==coluna).findFirst().ifPresent(c -> c.alternarMarcacao());
		
	}

	private void gerarCampos() {
		for(int i=0; i < linhas; i++) {
			for(int j=0; j < colunas; j++) {
				
				Campo campo = new Campo(i,j);
				campo.registrarObservador(this);
				campos.add(campo);
				
			}
		}
	}

	private void associarVizinhos() {
		
		for(Campo c1: campos) {
			for(Campo c2: campos) {
				c1.adicionarVizinho(c2);
			}
		}
		
	}
	
	private void sortearMinas() {
		long minasArmadas = 0;
		
		do {
			
			int aleatorio = (int) (Math.random() * campos.size());
			campos.get(aleatorio).minar();
			minasArmadas = campos.stream().filter(c -> c.isMinado()).count();
			
		}while(minasArmadas < minas);
	}
	
	public boolean objetivoAlcancado() {
		
		return campos.stream().allMatch(c -> c.objetivoAlcancado());
				
	}
	
	public void reiniciar() {
		
		campos.parallelStream().forEach(c -> c.reiniciar());
		sortearMinas();
		
	}

	@Override
	public void eventoOcorreu(Campo campo, CampoEvento evento) {
		if(evento == CampoEvento.EXPLODIR) {
			mostrarMinas();
			notificarObservadores(false);
		}else if(objetivoAlcancado()){
			notificarObservadores(true);
		}
	}
	
}
