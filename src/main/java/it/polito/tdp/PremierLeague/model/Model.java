package it.polito.tdp.PremierLeague.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.PremierLeague.db.PremierLeagueDAO;

public class Model {
	private PremierLeagueDAO dao;
	private Graph<Player, DefaultWeightedEdge> grafo;
	private Map<Integer, Player> idMap;
	
	
	public Model() {
		this.dao= new PremierLeagueDAO();
		this.idMap= new HashMap<Integer,Player>();
		this.dao.loadListAllPlayers(idMap);
	}
	
	public void creaGrafo(Match m) {
		this.grafo= new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		
		//aggiungo i vertici (tutti i giocatori che hanno partecipato al match m)
		Graphs.addAllVertices(this.grafo, this.dao.getVertici(m,idMap));
		
		//aggiungo archi 
		for(Adiacenza a:dao.getAdiacenze(m, idMap)) {
			if(a.getPeso()>=0) {
				//p1 meglio di p2
				if(this.grafo.containsVertex(a.getP1()) && this.grafo.containsVertex(a.getP2()))
					Graphs.addEdge(this.grafo, a.getP1(), a.getP2(), a.getPeso());
			} else {
				//p2 meglio di p1
				if(this.grafo.containsVertex(a.getP1()) && this.grafo.containsVertex(a.getP2()))
					Graphs.addEdge(this.grafo, a.getP2(), a.getP1(), Math.abs(a.getPeso())); //potevo anche moltiplicare il peso per -1
			}
		}
		
	}
	
	public GiocatoreMigliore getMigliore() {
		if(grafo==null)
			return null;
		Player best=null;
		//ricerca del massimo
		Double maxDelta=(double)Integer.MIN_VALUE;
		
		for(Player p: this.grafo.vertexSet()) {
			//differenza somma pesi archi uscenti e somma pesi archi entranti
			
			//somma pesi archi uscenti
			double pesoUscente=0;
			for(DefaultWeightedEdge edge: this.grafo.outgoingEdgesOf(p)) {
				pesoUscente+=this.grafo.getEdgeWeight(edge);
			}
			//somma pesi archi entranti
			double pesoEntrante=0;
			for(DefaultWeightedEdge edge: this.grafo.incomingEdgesOf(p)) {
				pesoEntrante+=this.grafo.getEdgeWeight(edge);
			}
			
			double delta=pesoUscente-pesoEntrante;
			if(delta>maxDelta) {
				best=p;
				maxDelta=delta;
			}
		}
		return new GiocatoreMigliore(best,maxDelta);
	}
	
	public int getNVertici() {
		return this.grafo.vertexSet().size();
	}
	
	public int getNArchi() {
		return this.grafo.edgeSet().size();
	}
	
	public Graph<Player, DefaultWeightedEdge> getGrafo(){
		return this.grafo;
	} 
	
	public List<Match> getAllMatches() {
		List<Match> matches=dao.listAllMatches();
		Collections.sort(matches, new Comparator<Match>() {

			@Override
			public int compare(Match o1, Match o2) {
				return o1.getMatchID().compareTo(o2.matchID);
			}
			
		});
		//matches.sort(Comparator.comparing(Match :: getMatchID));
		return matches;
	}
	
}
