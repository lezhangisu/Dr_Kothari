//This is an Eclipse plugin program
//Counting paths of C programs
//by Le Zhang

package cntPaths;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.io.PrintWriter;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.c.commons.analysis.CommonQueries;

public class paths {
	public static long countByFunction(String fun) {//Count paths by function name
		Map<Node, Integer> pathCnt = new HashMap<Node, Integer>();
		//mapping of nodes with more than one incoming edge
		Map<Node, Long> multi_inc_edge_nodes = new HashMap<Node, Long>();  
		
		Q cfg=CommonQueries.cfg(CommonQueries.functions(fun)); //Control flow graph
		Q cfbe=cfg.edges(XCSG.ControlFlowBackEdge); //Control flow back edge
		Q dag=cfg.differenceEdges(cfbe); //Directed Acyclic Graph (DFBE removed)
		Q roots= dag.roots(); 
		Q leaves=dag.leaves();
		
		AtlasSet<Node> A_nodes = dag.eval().nodes(); //Set of all nodes
		AtlasSet<Node> A_roots = roots.eval().nodes(); // Set of root nodes
		
		long num_inc;
		for(Node n: A_nodes) {
			num_inc = dag.predecessors(Common.toQ(n)).eval().nodes().size();
			if(num_inc>1) {
				multi_inc_edge_nodes.put(n, num_inc); //record nodes with more than one incoming edges
			}
			pathCnt.put(n, A_roots.contains(n)? 1:0); //assign 1 to roots and 0 to others
		}
		
		AtlasSet<Node> layer = A_roots;
		Set<Node> update = new HashSet<Node>();
		
		while(!layer.isEmpty()) { //continue if this layer is not empty
			for(Node n : layer) {
				for(Node p : dag.successors(Common.toQ(n)).eval().nodes()) {
					pathCnt.put(p, pathCnt.get(p)+pathCnt.get(n));//propagate number of paths
					
					if(multi_inc_edge_nodes.keySet().contains(p)) {
						if(multi_inc_edge_nodes.get(p)>1) {
							multi_inc_edge_nodes.put(p, multi_inc_edge_nodes.get(p)-1);
							continue;
							//if this node is not reached by all its predecessors, hold
							//don't add it for further update until the last predecessors reaches it
						}
					}
					update.add(p);//add new layer nodes to candidate list
				}
				
			}
			layer.clear();
			layer.addAll(update);
			update.clear();
			//update the next layer to propagate
		}
		
		long sum=0;
		for(Node n : leaves.eval().nodes()) {
			sum+=pathCnt.get(n);//gather all paths reached leaves
		}
		
		return sum;
		
	}
	
	public static void countByProject(String proj) {//Count paths by project name
		Q classes=Common.universe().project(proj).contained().nodes(XCSG.Function);
		String name="";
		String res= "";
		for(Node n:classes.eval().nodes()) {
			name = n.getAttr(XCSG.name).toString();
			res += name+","+countByFunction(name)+"\n";
			
		}
		
		try {
			PrintWriter writer = new PrintWriter("/home/leo/test-edge-"+proj+".csv", "UTF-8");
			writer.print(res);
			writer.close();
		}catch(Exception e) {
			
		}
    
	}
  
}
