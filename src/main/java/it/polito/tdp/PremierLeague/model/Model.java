package it.polito.tdp.PremierLeague.model;

import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.DeltaSteppingShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.PremierLeague.db.PremierLeagueDAO;

public class Model {
	
	
	private Graph<Player, DefaultWeightedEdge> grafo ;
	PremierLeagueDAO dao ;
	private Map<Integer, Player> idMapPlayers ;
	private List<Match> matches ; 
	private List<Player> playersList ;
	
	public Model() {

		grafo = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class) ;
		dao = new PremierLeagueDAO() ;
		idMapPlayers = new HashMap<>() ;
		matches = new ArrayList<>(dao.listAllMatches()) ;
		this.playersList = new ArrayList<>(dao.listAllPlayers()) ;
		this.getMatches() ;
	}
	
	
	public String creaGrafo(Match m) {
		
		grafo = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class) ;
		
		for( Player p: dao.listAllPlayers())
			idMapPlayers.put(p.getPlayerID(), p);
		
		// Players of Match selected
		List<Player> result = dao.listPlayersOfMatch(m, this.idMapPlayers) ;  
		
		Graphs.addAllVertices(this.grafo, result) ;
		
		
		/*for(Player p: result) {
			for(Player p1 : result)
				if( p.getPlayerID() < p1.getPlayerID() && p.getEfficienza() < p1.getEfficienza())
					Graphs.addEdge(this.grafo, p, p1, p1.getEfficienza() - p.getEfficienza()) ;
		}*/
		
		/*for(Adiacenza a: dao.getAdiacenze(m, this.idMapPlayers)) {
			if(this.grafo.getEdge(a.getP1(), a.getP2()) == null) {
				Graphs.addEdge(this.grafo, a.getP1(), a.getP2(), a.getPeso()) ;
			}
		}*/
		
		for(Adiacenza a : dao.getAdiacenze(m, this.idMapPlayers)) {
			if(a.getPeso() >= 0) {
				//p1 meglio di p2
				if(grafo.containsVertex(a.getP1()) && grafo.containsVertex(a.getP2())) {
					Graphs.addEdgeWithVertices(this.grafo, a.getP1(), 
							a.getP2(), a.getPeso());
				}
			} else {
				//p2 meglio di p1
				if(grafo.containsVertex(a.getP1()) && grafo.containsVertex(a.getP2())) {
					Graphs.addEdgeWithVertices(this.grafo, a.getP2(), 
							a.getP1(), (-1) * a.getPeso());
				}
			}
		}
		
		return String.format("Grafo creato\n"
							+ "#VERTICI: %d\n"
							+ "#ARCHI: %d", grafo.vertexSet().size(), grafo.edgeSet().size()) ;
		
		
	}
	
	public List<Match> getMatches() {
		
		this.matches = new ArrayList<>(dao.listAllMatches()) ;
		Collections.sort(matches);
		return matches;
	}
	
	public String getGiocatoreMigliore() {
		
		double effMax = 0.0 ;
		Player player = null ;
		
		for( Player p: this.grafo.vertexSet()) {
			double eff = 0.0 ;
			for(DefaultWeightedEdge e : this.grafo.outgoingEdgesOf(p))
				eff += this.grafo.getEdgeWeight(e) ;
			for(DefaultWeightedEdge e : this.grafo.incomingEdgesOf(p))
				eff -=  this.grafo.getEdgeWeight(e) ;
			
			if( eff > effMax) {
				effMax = eff;
				player = p;
			}
			
		}
		return player.getPlayerID()+"-"+player.getName() + ", delta efficienza: "+ effMax ; 
		
	}
}
