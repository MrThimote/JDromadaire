package evaluator;

import java.util.ArrayList;

import main.EntryPoint;
import parser.Node;
import parser.nodes.ReturnNode;
import parser.nodes.innerreturn.InnerRNode;
import variables.VariableContext;

public class Evaluator {

	public static Object evaluate(ArrayList<Node> nodes, VariableContext context, boolean printEq) {
		for(Node n:nodes) {
			if (n != null) {
				EntryPoint.setStackDat(n.col, n.line);
				Object value = n.evaluate(context);
				if(value != null) {
					if (value instanceof ReturnNode) {
						return ((ReturnNode)value);
					}
					
					if (value instanceof InnerRNode) {
						return value;
					}
					if(printEq) {
						System.out.println(value);
					}
				}
			}
		}
		return null;
	}
	
}
