package com.sa.assignment.bus;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.sa.assignment.orm.Planet;
import com.sa.assignment.orm.Route;
import com.sa.assignment.repository.PlanetRepository;
import com.sa.assignment.repository.RouteRepository;


 
@Service
@Lazy
public class ShortestPathService implements DijkstraAlgorithm  {
	

	static final Logger LOG = org.apache.log4j.Logger.getLogger(ShortestPathService.class);
	
	@Autowired
	private PlanetRepository planetRepo;

	@Autowired
	private RouteRepository routesRepo;

	private SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(
			DefaultWeightedEdge.class);
		
	 
	@PostConstruct
	private void initWeightedGraph(){
		addVertex();
		addEdges();
	}

	 
	public void addVertex(){
		for (Planet planet : planetRepo.findAll()) {
			this.graph.addVertex(planet.getPlanetID());
		}
	}

	 
	private void addEdges(){
		DefaultWeightedEdge edge = null;
		for (Route route : routesRepo.findAll()) {
			String source  = route.getSource().getPlanetID();
			String destination = route.getDest().getPlanetID();
			if(source != destination){
				edge = this.graph.addEdge(source,destination);
			}
			addWeight(edge, route.getDistance());
		}
	}

	 
	private void addWeight(DefaultWeightedEdge edge, float weight) {
		this.graph.setEdgeWeight(edge, weight);
	}

	 
	@Override
	public String findShortestPath(String source, String destination) {
		return DijkstraShortestPath.findPathBetween(this.graph, source,destination).toString();
	}

	 
	@PreDestroy
	public void cleanup(){
		this.graph = null;
	}
}

