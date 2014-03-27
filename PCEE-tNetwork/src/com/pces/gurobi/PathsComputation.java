/**
 *  This file is part of Path Computation Element Emulator (PCEE).
 *
 *  PCEE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  PCEE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with PCEE.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.pces.gurobi;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.graph.elements.vertex.VertexElement;
import com.graph.graphcontroller.Gcontroller;
import com.graph.path.PathElement;
import com.graph.path.pathelementimpl.PathElementImpl;

/**
 * Computation of all possible paths in the network
 * 
 * @author Fran Carpio
 */
public class PathsComputation {

	protected List<PathElement> setOfPaths;

	public PathsComputation(Gcontroller graph, int extraLenght, boolean fromFile){

		if (!fromFile)
			setOfPaths = computeAllPaths(graph, extraLenght);
		else
			try {
				setOfPaths = readPathsfromFile(graph, "65nodes(+2).txt");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	public List<PathElement> getPathsFromStoD(VertexElement n, VertexElement m,
			double maxDelay) {

		List<PathElement> selectedPaths = new ArrayList<PathElement>();

		for (PathElement p : setOfPaths) {
			if (p.getSource().getVertexID().equals(n.getVertexID())
					&& p.getDestination().getVertexID()
							.equals((m.getVertexID())))
				if (p.getPathParams().getPathDelay() < maxDelay)
					selectedPaths.add(p);
		}
		return selectedPaths;
	}

	public List<PathElement> computeAllPaths(Gcontroller graph, int extraLenght) {

		int minimumLenght;
		List<PathElement> setOfPaths = new ArrayList<PathElement>();
		ComputeAllPathsFromStoD computeAllPathsFromStoD;

		for (VertexElement n : graph.getVertexSet()) {
			for (VertexElement m : graph.getVertexSet()) {
				if (n.equals(m))
					continue;
				System.out.println(n.getVertexID() + "-->" + m.getVertexID());
				computeAllPathsFromStoD = new ComputeAllPathsFromStoD(n, m);
				computeAllPathsFromStoD.start(graph, n, m);

				minimumLenght = Integer.MAX_VALUE;

				for (PathElement p : computeAllPathsFromStoD.getPaths()) {
					if (p.getTraversedVertices().size() < minimumLenght)
						minimumLenght = p.getTraversedVertices().size();
				}

				for (PathElement p : computeAllPathsFromStoD.getPaths()) {
					if (p.getTraversedVertices().size() <= minimumLenght
							+ extraLenght) {
						setOfPaths.add(p);
					}
				}
			}
		}
		return setOfPaths;
	}

	public List<PathElement> computePathsFromStoD(Gcontroller graph,
			int extraLenght, VertexElement n, VertexElement m) {

		List<PathElement> setOfPaths = new ArrayList<PathElement>();
		ComputeAllPathsFromStoD computeAllPathsFromStoD = new ComputeAllPathsFromStoD(
				n, m);
		computeAllPathsFromStoD.start(graph, n, m);

		int minimumLenght = Integer.MAX_VALUE;

		for (PathElement p : computeAllPathsFromStoD.getPaths()) {
			if (p.getTraversedVertices().size() < minimumLenght)
				minimumLenght = p.getTraversedVertices().size();
		}

		for (PathElement p : computeAllPathsFromStoD.getPaths()) {
			if (p.getTraversedVertices().size() <= minimumLenght + extraLenght) {
				setOfPaths.add(p);
			}
		}
		return setOfPaths;
	}

	public class ComputeAllPathsFromStoD {

		private VertexElement nodeS;
		private VertexElement nodeD;
		private List<PathElement> paths = new ArrayList<PathElement>();
		private List<String> onPath = new ArrayList<String>();
		private Stack<VertexElement> pathNodes = new Stack<VertexElement>();

		public ComputeAllPathsFromStoD(VertexElement nodeS, VertexElement nodeD) {
			this.nodeS = nodeS;
			this.nodeD = nodeD;
		}

		public void start(Gcontroller graph, VertexElement sourceNode,
				VertexElement destNode) {

			pathNodes.push(sourceNode);
			onPath.add(sourceNode.getVertexID());

			if (sourceNode.equals(destNode)) {

				PathElementImpl path = new PathElementImpl(graph, nodeS, nodeD);

				ArrayList<VertexElement> pathNodesList = new ArrayList<VertexElement>();
				for (Object o : pathNodes)
					pathNodesList.add((VertexElement) o);
				path.setVertices(pathNodesList);
				path.setEdges(pathNodes);
				paths.add(path);

			} else {
				for (VertexElement n : sourceNode.getNeighbouringVertices()) {
					if (!onPath.contains(n.getVertexID()))
						if (onPath.size() < graph.getVertexSet().size()) {
							start(graph, n, destNode);
						}
				}
			}

			pathNodes.pop();
			onPath.remove(sourceNode.getVertexID());
		}

		public List<PathElement> getPaths() {
			return paths;
		}
	}

	public List<PathElement> readPathsfromFile(Gcontroller graph,
			String filename) throws IOException {

		new ReadFromFile(filename);
		String line;
		List<PathElement> setOfPaths = new ArrayList<PathElement>();
		Set<VertexElement> setOfNodes = graph.getVertexSet();

		while ((line = ReadFromFile.readLine()) != null) {
			line = line.trim();

			String[] pathNodesString = line.substring(line.indexOf("-") + 1)
					.trim().split("-");

			Stack<VertexElement> pathNodes = new Stack<VertexElement>();

			for (int i = 0; i < pathNodesString.length; i++) {
				for (VertexElement n : setOfNodes) {
					if (n.getVertexID().equals(pathNodesString[i])) {
						pathNodes.push(n);
					}
				}

			}

			PathElementImpl path = new PathElementImpl(graph, pathNodes.get(0),
					pathNodes.get(pathNodes.size() - 1));

			ArrayList<VertexElement> pathNodesList = new ArrayList<VertexElement>();
			for (Object o : pathNodes)
				pathNodesList.add((VertexElement) o);
			path.setVertices(pathNodesList);
			path.setEdges(pathNodes);
			setOfPaths.add(path);
		}
		return setOfPaths;
	}

	public static class ReadFromFile {

		static BufferedReader f;

		public ReadFromFile(String filename) {
			try {
				f = new BufferedReader(new FileReader(filename));
			} catch (FileNotFoundException e) {
				System.out.println("The file " + filename
						+ " does not exist or could not be opened.");
			}
		}

		public static String readLine() throws IOException {

			String line = f.readLine();

			return line;
		}
	}

}
