/**
 * Copyright 2013-2014 Marcos Ferreira and Elthon Oliveira
 * 
 * This file is part of SupervisorD for Healthcare Professional software.
 * 
 *  SupervisorD for Healthcare Professional is free software: you can 
 *  redistribute it and/or modify it under the terms of the GNU General 
 *  Public License as published by the Free Software Foundation, either 
 *  version 3 of the License, or (at your option) any later version.
 *  
 *  SupervisorD for Healthcare Professional is distributed in the hope that
 *  it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See 
 *  the GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License
 *  along with SupervisorD for Healthcare Professional. 
 *  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  Contact: el7hon at gmail dot com
 */
package br.edu.ufcg.supervisor.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.internal.StringMap;


/**
 * Classe que representa o autômato de uma variável. 
 * @author Elthon Oliveira, Marcos José 
 *
 */
public class Automaton {
	public static final String NOME = "1";//"nome";
	public static final String ARRAY_TRANSICOES = "2";//"transitionsArray";
	public static final String ARRAY_TRANSICOES_P1 = "5";//"transitionsArray";
	public static final String ARRAY_TRANSICOES_P2 = "6";//"transitionsArray";
	public static final String ARRAY_ESTADOS = "3";//"arrayEstados";
	public static final String ARRAY_ESTADOS_P1 = "7";
	public static final String ARRAY_ESTADOS_P2 = "8";
	public static final String ARRAY_ESTADOS_ACEITOS = "4";// "acceptableStatesArray";

	private Vector<State> statesVector;
	private ArrayList<State> acceptableStatesArray;
	private ArrayList<String> labelsArray;
	private ArrayList<Transition> transitionsArray;
	private String nome;

	/**
	 * Construtor padrão.
	 */
	public Automaton(){ init(); }

	//TODO SAIR
	@SuppressWarnings("unchecked")
	public Automaton (Automaton a){
		init();
		statesVector = (Vector<State>) a.getVectorEstados().clone();
		transitionsArray = (ArrayList<Transition>) a.getArrayTransicoes().clone();
		labelsArray = (ArrayList<String>) a.getListaDeRotulos().clone();
		nome = a.getNome();
	}

	/**
	 * Construtor.
	 * @param automatoJson	Uma String contendo o automato no formato JSON.
	 */
	@SuppressWarnings("unchecked")
	public Automaton(String automatoJson){
		init();
		HashMap<String,Object> map = (HashMap<String,Object>) new Gson().fromJson(automatoJson,HashMap.class);
		this.nome = (String)map.get(NOME);
		ArrayList<StringMap<String>> arrayEstadosMap = (ArrayList<StringMap<String>>) map.get(ARRAY_ESTADOS);
		for (StringMap<String> sm : arrayEstadosMap){
			State e = new State(sm);
			this.statesVector.add(e);
		}
		ArrayList<Double> arrayEstadosAceitosInteger = (ArrayList<Double>) map.get(ARRAY_ESTADOS_ACEITOS);
		for (Double i : arrayEstadosAceitosInteger) this.acceptableStatesArray.add(statesVector.get(i.intValue()));
		ArrayList<StringMap<Object>> arrayTransicoesMap = (ArrayList<StringMap<Object>>) map.get(ARRAY_TRANSICOES);
		for (StringMap<Object> sm : arrayTransicoesMap){
			Transition t = new Transition(statesVector, sm);
			this.transitionsArray.add(t);
		}
		for (Transition transicao : transitionsArray) transicao.addTransicaoNoEstadoOrigem();
	}

	
	/**
	 * Retorna o rótulo das transições existentes entre dois estados. 
	 * @param origem	O Estado de origem.
	 * @param destino	O Estado de destino.
	 * @return			Uma String com os rótulos das transições, indicando o caminho de um estado ao outro.
	 */
	public String getRotulosDasTransicoesEntreDoisEstadosQuaisquer(State origem, State destino){
		String resultado = "";
		String nomeDestino = destino.getNome();
		for (Transition t : origem.getTransicoes()) {
			if ( (nomeDestino.equals(t.getEstadoDestino().getNome()))){
				if (resultado.equals("")) resultado += t.getRotulo();
				else resultado += "_ou_" + t.getRotulo();
			}
		}
		return resultado;
	}
	
	/**
	 * Retorna a mensagem da transição existente entre dois estados. 
	 * @param origem	O Estado de origem.
	 * @param destino	O Estado de destino.
	 * @return			Uma String com as mensagens das transições.
	 */
	public String getMensagemDasTransicoesEntreDoisEstadosQuaisquer(State origem, State destino){
		String resultado = "";
//		String nomeDestino = destino.getNome();
//		System.out.println("#nomeDestiono: "+nomeDestino);
		for (Transition t : origem.getTransicoes()) {
//			if ((nomeDestino.equals(t.getEstadoDestino().getNome()))){
			if ((destino.equals(t.getEstadoDestino()))){
				if (resultado.equals("")) resultado += t.getMensagem();
				else resultado += " | " + t.getMensagem();
			}
		}
		return resultado;
	}

	/**
	 * Retorna uma String que representa o automato.
	 * @return	Uma String que representa o automato no formato JSON.
	 */
	public String toJson(){
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(NOME, nome); 
		ArrayList<String> arrayStringTransicao = new ArrayList<String>();
		for(Transition t : transitionsArray) arrayStringTransicao.add(t.toJson(statesVector));//TO)DO MODIFICADO
		map.put(ARRAY_TRANSICOES, arrayStringTransicao.toString());
		ArrayList<Integer> arrayStringEstadosAceitos = new ArrayList<Integer>();
		ArrayList<String> arrayStringEstado = new ArrayList<String>();
		for(State e : statesVector){
			arrayStringEstado.add(e.toJson());
			if (e.getClassificacao() == State.INT_CL_ACEITACAO)
				arrayStringEstadosAceitos.add(statesVector.indexOf(e));
		}
		map.put(ARRAY_ESTADOS, arrayStringEstado.toString());
		map.put(ARRAY_ESTADOS_ACEITOS, arrayStringEstadosAceitos.toString());
		return map.toString();
	}

	/**
	 * Retorna os estados do automato.
	 * @return	Um Vector<Estado> contendo todos os estados do automato.
	 */
	public Vector<State> getVectorEstados(){ return statesVector; }

	/**
	 * Seta uma lista com os estados do automato. 
	 * @param arrayEstados	Um Vector<Estado> contendo os estados do automato.
	 */
	public void setVectorEstados(Vector<State> arrayEstados){ this.statesVector = arrayEstados; }

	/**
	 * Retorna as transições do automato.
	 * @return	Um ArrayList<Transicao> contendo todas as transições do automato.
	 */
	public ArrayList<Transition> getArrayTransicoes(){ return transitionsArray; }

	/**
	 * Seta a lista de transições do automato.
	 * @param transitionsArray	Um ArrayList<Transicao> contendo as transições do automato.
	 */
	public void setArrayTransicoes(ArrayList<Transition> arrayTransicoes){ 
		this.transitionsArray = arrayTransicoes;
	}

	/**
	 * Retorna os estados aceitos do automato.
	 * @return	Um ArrayList<Estado> contendo todos os estados de aceitação.
	 */
	public ArrayList<State> getArrayEstadosAceitos() { return acceptableStatesArray; }

	/**
	 * Adiciona um estado no automato.
	 * @param estado	Um Estado pertencente ao automato.
	 */
	public void addEstado(State estado){ statesVector.add(estado); }

	/**
	 * Adiciona uma transição ao atomato.
	 * @param transicao	Uma Transicao pertencente ao automato.
	 */
	public void addTransicao(Transition transicao){
		if (!transitionsArray.contains(transicao)){
			transitionsArray.add(transicao);
			if (!labelsArray.contains(transicao.getRotulo())) labelsArray.add(transicao.getRotulo());
		}
	}

	/**
	 * Seta o nome do automato.
	 * @param nome	Uma String contendo o nome do automato.
	 */
	public void setNome(String nome){ this.nome = nome; }

	/**
	 * Retorna o nome do automato.
	 * @return	Uma String contendo o nome do estado.
	 */
	public String getNome(){ return this.nome; }

	/**
	 * Retorna a lista de rótulos.
	 * @return	Um ArrayList<String> contendo os rótulos das transições.
	 */
	public ArrayList<String> getListaDeRotulos(){ return labelsArray; }

	/**
	 * Busca o estado correspondente aos valores das variáveis monitoradas.
	 * @param map	Um HashMap<Integer, Float> contendo o identificador da 
	 * 					variável e o seu valor, respectivamente.
	 * @return 		O Estado correspondente para os valores das variáveis monitoradas. 
	 * @throws Exception 
	 */
	public State buscaEstadoCorrespondente(HashMap<Integer, Float> map) throws Exception{
		for (State estado : statesVector ){
			if (estado.verificaIntervalos(map)) return estado;
		}
		throw new Exception("Value not monitored");
	}

	/*Inicializa os atributos do automato */
	private void init(){
		statesVector = new Vector<State>();
		transitionsArray = new ArrayList<Transition>();
		labelsArray = new ArrayList<String>();
		acceptableStatesArray = new ArrayList<State>();
		nome = "";
	}
}