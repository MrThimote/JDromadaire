package parser.nodes;

import main.EntryPoint;
import parser.Node;
import parser.operators.ListOperator;
import variables.VariableContext;

public class ListSetterNode extends Node {

	private Node left;
	private Node index;
	public Node expr;
	public boolean isGlobalContext = false;
	
	public ListSetterNode(int col, int line, Node left, Node index) {
		super(col, line);
		this.left = left;
		this.index = index;
	}

	public Object evaluate(VariableContext context) {
		Object index = this.index.evaluate(context);
		Object evaluated;
		if (isGlobalContext) {
			evaluated = this.left.evaluate(EntryPoint.globalContext);
		} else {
			evaluated = this.left.evaluate(context);
		}
		
		if ( expr == null ) {
			
			if (evaluated instanceof ListOperator) {
				ListOperator op = (ListOperator)evaluated;
				if(index instanceof NumberNode) {
					if(((NumberNode) index).isInt() &&
							((NumberNode)index).isIntegerRange() &&
							(Integer)((NumberNode) index).getNumber().intValue() >= 0 && (Integer)((NumberNode) index).getNumber().intValue() < op.length()) {
						return op.get((NumberNode) index);
					} else {
						if(!(((NumberNode) index).isInt() && ((NumberNode) index).isIntegerRange())) {
							EntryPoint.raiseErr("Integer Object needed, received Float/Double");
							return null;
						}
						EntryPoint.raiseErr("Index out of range exception");
						return null;
					}
				} else if (index instanceof StringNode) {
					return op.get((StringNode) index);
				}
				EntryPoint.raiseErr("List only support usage with numbers or strings");
			}
		} else {
			Object expr = this.expr.evaluate(context);
			if (evaluated instanceof ListOperator) {
				ListOperator op = (ListOperator)evaluated;
				if(index instanceof NumberNode) {
					if(((NumberNode) index).isInt() &&
							((NumberNode)index).isIntegerRange() &&
							(Integer)((NumberNode) index).getNumber().intValue() >= 0 && (Integer)((NumberNode) index).getNumber().intValue() < op.length()) {
						op.set((NumberNode) index, expr);
						return null;
					} else {
						if(!(((NumberNode) index).isInt() && ((NumberNode) index).isIntegerRange())) {
							EntryPoint.raiseErr("Integer Object needed, received Float/Double");
							return null;
						}
						EntryPoint.raiseErr("Index out of range exception");
						return null;
					}
				}else if (index instanceof StringNode) {
					op.set((StringNode) index, expr);
					return null;
				}
				EntryPoint.raiseErr("List only support usage with numbers or strings");
			}
		}
		
		return null;
		
	}
	
}
