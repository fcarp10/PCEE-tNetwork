package com.pces.gurobi;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.graph.elements.edge.EdgeElement;
import com.graph.elements.edge.params.EdgeParams;
import com.graph.elements.edge.params.impl.BasicEdgeParams;
import com.graph.elements.vertex.VertexElement;
import com.graph.graphcontroller.Gcontroller;

public class ImportTopology {

	public static void start(Gcontroller graph, String filename) {
		// add vertices to the graph
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String temp;
			VertexElement vertex1, vertex2;
			String vertex1NameToDraw, vertex2NameToDraw;

			// Read till we get to Node definition)
			while ((temp = reader.readLine()).trim().compareTo("NODES (") != 0) {
			}

			// read till we reach the end of node definitions
			while ((temp = reader.readLine()) != null) {
				temp = temp.trim();
				if (temp.trim().compareTo(")") == 0) {
					break;
				}

				Pattern p;
				Matcher m;

				String sourceID = "";
				p = Pattern.compile("[a-zA-Z0-9\\.]+");
				m = p.matcher(temp);
				if (m.find()) {
					sourceID = m.group(0);
				}

				double[] temp1 = new double[2];
				int count = 0;
				while (m.find()) {
					temp1[count] = Double.parseDouble(m.group(0));
					count++;
					if (count == 2)
						break;
				}

				double XCoord = temp1[0] * 1.6;
				double YCoord = temp1[1] * 1.6;

				vertex1NameToDraw = "\"" + sourceID + "\"," + XCoord + ","
						+ YCoord + ",\"\",\"\",\"\",\"" + sourceID + "\"";

				vertex1 = new VertexElement(sourceID, graph, temp1[0],
						temp1[1], vertex1NameToDraw);
				graph.addVertex(vertex1);
			}

			// Read till we get to Edge definition)
			while ((temp = reader.readLine()).trim().compareTo("LINKS (") != 0) {
			}

			// read till we reach the end of the edge definition
			while ((temp = reader.readLine()) != null) {
				temp = temp.trim();
				if (temp.length() == 1) {
					break;
				}

				Pattern p;
				Matcher m;

				p = Pattern.compile("[a-zA-Z0-9\\.]+");
				m = p.matcher(temp);
				String[] temp1 = new String[3];
				int count = 0;
				while (m.find()) {
					temp1[count] = m.group(0);
					count++;
					if (count == 3)
						break;
				}

				vertex1 = graph.getVertex(temp1[1]);
				vertex2 = graph.getVertex(temp1[2]);

				vertex1NameToDraw = "\"" + temp1[1] + "\"";
				vertex2NameToDraw = "\"" + temp1[2] + "\"";

				EdgeElement edge1 = new EdgeElement(temp1[0] + ".1", vertex1,
						vertex2, graph);
				EdgeElement edge2 = new EdgeElement(temp1[0] + ".2", vertex2,
						vertex1, graph);

				double distance = Math
						.sqrt(Math.pow(
								vertex1.getXCoord() - vertex2.getXCoord(), 2)
								+ Math.pow(
										vertex1.getYCoord()
												- vertex2.getYCoord(), 2));

				double delay = distance / 29.9792458; // (in ms)

				String edge1NameToDraw = vertex1NameToDraw + ","
						+ vertex2NameToDraw + ",\"\",\"\",\"\","
						+ "\"40 Gbps / " + Math.rint(delay * 100) / 100
						+ "ms\"";
				String edge2NameToDraw = vertex2NameToDraw + ","
						+ vertex1NameToDraw;
				EdgeParams params1 = new BasicEdgeParams(edge1, delay, 1, 40,
						edge1NameToDraw);
				EdgeParams params2 = new BasicEdgeParams(edge2, delay, 1, 40,
						edge2NameToDraw);
				edge1.setEdgeParams(params1);
				edge2.setEdgeParams(params2);
				graph.addEdge(edge1);
				graph.addEdge(edge2);
			}
			reader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
