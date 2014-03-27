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
package com.pces.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.graph.elements.edge.EdgeElement;
import com.graph.elements.vertex.VertexElement;
import com.graph.graphcontroller.Gcontroller;
import com.graph.graphcontroller.impl.GcontrollerImpl;
import com.graph.path.PathElement;
import com.graph.path.algorithms.constraints.Constraint;
import com.graph.path.algorithms.constraints.impl.SimplePathComputationConstraint;
import com.pces.gurobi.GurobiModel;
import com.pces.gurobi.ImportTopology;
import com.pces.gurobi.PathsComputation;

/**
 * @author Fran Carpio
 */
public class Servlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	Gcontroller graph;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Servlet() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		graph = new GcontrollerImpl();
		String path = getServletConfig().getServletContext().getRealPath("/");
		ImportTopology.start(graph, path + "atlanta.txt");

		PrintWriter out = response.getWriter();

		out.println("<html>");
		out.println("<head><script type='text/javascript'"
				+ " src='http://code.jquery.com/jquery-latest.min.js'></script>\n"
				+ "<link href='tNetwork.css' rel='stylesheet' type='text/css' />"
				+ "\n<script src='tNetwork.js' type='text/javascript'></script>"
				+ "</head>");
		out.println("<div style='opacity:0.1;position:absolute;width:545px;border-radius:3px;height:115px;background-color:#20B2AA'></div>");
		out.println("<div style='font-family:verdana;position:absolute;padding:20px;width:500px; border-radius:3px;border:5px solid #20B2AA;'>");
		out.println("<form>");
		out.println("<div>");
		out.println("Bandwidth (Gbps): <input id='inputBW' size='1' type='text' value='1'>");
		out.println("Delay (ms): <input id='inputDelay' size='1' type='text' value='20'>");
		out.println("</div>");
		out.println("<div>");
		out.println("Source: <input id='input1' type='text' size='13' value='192.169.2.13'>");
		out.println("Destination: <input id='input2' type='text' size='13' value='192.169.2.6'>");
		out.println("<button type='button' onclick='myFunction()'>Send</button>");
		out.println("</div>");
		out.println("</form>");
		out.println("</div>");

		out.println("<body onload='mainLoad();'>\n<div style='font-family:verdana;border: 2px solid; height:800px;position:relative;top:120px;'>");
		out.println("<svg id='mySVG' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink' height='100%' width='100%'>");
		out.println("<defs id='myDefs'>");
		out.println("<pattern id='gridPattern' width='10' height='10'");
		out.println("patternUnits='userSpaceOnUse'><path d='M10,0 H0 V10' fill='none' stroke='gray' stroke-width='0.25'/></pattern></defs>");

		out.println("<rect id='grid' width='100%' height='100%' fill='url(#gridPattern)' fill-opacity='0.7' stroke='gray' stroke-width='0.25'/>");

		out.println("</svg></div>");
		out.println("</body>");
		out.println("</html>");

		out.println("<script type='text/javascript'>");
		out.println("\nvar net;");
		out.println("function  mainLoad(){");
		out.println("net=new tNetwork.Network();");
		out.println("net.addSVG('mySVG');");
		out.println("console.log('B.AAM');");

		for (VertexElement n : graph.getVertexSet()) {
			out.println("net.addNode(" + n.getDrawName() + ");");
		}
		for (EdgeElement l : graph.getEdgeSet()) {
			out.println("net.addLink(" + l.getEdgeParams().getDrawName() + ");");
		}

		out.println("net.globalTransformMatrix = 'matrix(1.0 0 0 1.0 0 0)';");
		out.println("net.setLinkEventHandler('click', function(evt) {"
				+ "alert ('clicked link with id '+evt.target.id);});");
		out.println("setTimeout('net.drawNetwork()',300);"
				+ "setTimeout('net.startEditMode()',500);}");

		out.println("function myFunction(){");
		out.println("var src = $('#input1').val(); var dst = $('#input2').val(); var bw = $('#inputBW').val(); var delay = $('#inputDelay').val();"
				+ "$.ajax({"
				+ "type: 'POST'"
				+ ",url: 'Servlet'"
				+ ",data: { source : src, destination : dst, bandwidth : bw, delay : delay},"
				+ "success : function(data){"
				+ "var res = data.split(' ');"
				+ "myFunction3(res);}" + " });");
		out.println("}");

		out.println("function myFunction3(s){");
		out.println("net.highlightAll(false)");
		out.println("for(var i=0;i<s.length;i++){");
		out.println("net.toggleHighlight('node', s[i]);}");
		out.println("net.drawNetwork();");
		out.println("for(var i=0;i<s.length;i++){");
		out.println("net.toggleHighlight('link',s[i]+':'+s[i+1]);}");
		out.println("}");

		out.println("</script>");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String source = request.getParameter("source");
		String destination = request.getParameter("destination");
		double bw = Double.valueOf(request.getParameter("bandwidth"));
		double delay = Double.valueOf(request.getParameter("delay"));

		VertexElement sourceNode = null;
		VertexElement destNode = null;
		for (VertexElement n : graph.getVertexSet()) {
			if (n.getVertexID().equals(source))
				sourceNode = n;
			else if (n.getVertexID().equals(destination))
				destNode = n;
		}

		PathsComputation pathsComputation = new PathsComputation(graph, 2,
				false);
		Constraint c = new SimplePathComputationConstraint(sourceNode,
				destNode, bw, delay);
		List<Constraint> constraintsList = new ArrayList<Constraint>();
		constraintsList.add(c);

		GurobiModel gurobiModel = new GurobiModel();
		gurobiModel.start(graph, constraintsList, pathsComputation);

		PathElement optimalPath = gurobiModel
				.getOptimalPath(new SimplePathComputationConstraint(sourceNode,
						destNode));

		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		String pathnodes = "";

		if (optimalPath != null) {
			for (VertexElement n : optimalPath.getTraversedVertices()) {
				pathnodes += n.getVertexID() + " ";
			}
		}

		response.getWriter().write(pathnodes);
	}
}