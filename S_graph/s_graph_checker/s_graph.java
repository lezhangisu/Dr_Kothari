//This is an Eclipse plugin program
//It helps to check if a function is s-graph
//by Le Zhang

//S-graph:
//1.A directed acyclic graph
//2.Has unique entry and unique exit
//3.Three kind of nodes:
//	with at most one outgoing edge
//	with at most two outgoint edge
//	function nodes
//4.For every nodes B, it has a subgraph for which the only entry is B itself, and have only one exit.


package s_graph;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.c.commons.analysis.CommonQueries;

public class s_graph {
	public static boolean fun_isSgraph(String fun) {
		Q cfg=CommonQueries.cfg(CommonQueries.functions(fun)); //Control flow graph
		Q cfbe=cfg.edges(XCSG.ControlFlowBackEdge); //Control flow back edge
		Q dag=cfg.differenceEdges(cfbe); //Directed Acyclic Graph (DFBE removed)
		
		
		
		// record all nodes that connect to CFBE
		Set<Node> legal_exit_nodes = new HashSet<Node>();
		for(Edge e : cfbe.eval().edges()) {
			for(Node n : cfg.predecessorsOn(Common.toQ(e)).eval().nodes()) {
				legal_exit_nodes.add(n);
			}
		}
		for(Node n : cfg.leaves().eval().nodes()) {
			legal_exit_nodes.add(n);
		}
		
		// check if S-graph
		for(Node n : dag.eval().nodes()) {
			if(dag.forward(Common.toQ(n)).eval().roots().size()!=1) {
				return false;
			}
			for(Node p : dag.forward(Common.toQ(n)).leaves().eval().nodes()) {
				if(!legal_exit_nodes.contains(p)) {
					return false;
				}
			}

		}	
		return true;
	}
	
	public static void proj_isSgraph(String proj) {
		Q classes=Common.universe().project(proj).contained().nodes(XCSG.Function);
		String name="";
		String res= "";
		boolean status;
		for(Node n:classes.eval().nodes()) {
			name = n.getAttr(XCSG.name).toString();
			status = fun_isSgraph(name);
			if(!status)
				res += name+","+(status? "true":"false")+"\n";
			
		}
		if(res=="")
			res+="None";
		
		try {
			PrintWriter writer = new PrintWriter("/home/leo/test-Sgraph-"+proj+".csv", "UTF-8");
			writer.print(res);
			writer.close();
		}catch(Exception e) {
			
		}
	}
	
}
