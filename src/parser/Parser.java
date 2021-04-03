package parser;

import java.util.ArrayList;
import java.util.List;

import main.EntryPoint;
import main.Token;
import main.TokenType;
import parser.nodes.ArrayNode;
import parser.nodes.BooleanNode;
import parser.nodes.ForNode;
import parser.nodes.FuncGetterNode;
import parser.nodes.FunctionNode;
import parser.nodes.GetterNode;
import parser.nodes.IfNode;
import parser.nodes.ListGetterNode;
import parser.nodes.ListSetterNode;
import parser.nodes.NumberNode;
import parser.nodes.ReturnGetterNode;
import parser.nodes.StringNode;
import parser.nodes.innerreturn.BreakNode;
import parser.nodes.innerreturn.ContinueNode;

public class Parser {
	
	private List<Token> tokens;
	private int tok_id = 0;
	private int length;
	public Token current_token;
	public boolean advanceResult;
	
	public Token advance() {
		this.tok_id += 1;
		this.advanceResult = false;
		if (this.tok_id < this.length) {
			this.advanceResult = true;
			this.current_token = this.tokens.get(this.tok_id);
		}
		return this.current_token;
	}
	public Token next() {
		if (this.tok_id + 1 < this.tokens.size()) {
			return this.tokens.get(this.tok_id + 1);
		}
		return null;
	}
	
	private int iModifier = -1;
	public ArrayList<Node> parse(List<Token> tokens, TokenType end) {
		ArrayList<Node> parentNodes = new ArrayList<>();
		
		parentNodes.add(this.parseToken(tokens, false));
		int last_eof_id = this.tok_id;
		// IModify
		if (this.current_token.type == end) {
			return parentNodes;
		}
		if (this.current_token.type != TokenType.EOF) {
			System.out.println("Syntax Error, Expression invalid");
			EntryPoint.raiseToken(tokens.get(last_eof_id));
			return null; 
		}
		if (this.next() != null && this.next().type == end) {
			return parentNodes;
		}
		
		for(int i = last_eof_id + 1; i < tokens.size(); i++) {
			if (tokens.get(i).type == end) {
				this.tok_id = i - 1;
				this.advance();
				return parentNodes;
			}
			
			if (tokens.get(i).type == TokenType.EOF) {
				this.tok_id = last_eof_id;
				this.advance();
				if(this.current_token.type == TokenType.EOF) {
					last_eof_id += 1;
					continue;
				}
				
				iModifier = -1;
				parentNodes.add(this.parseToken(tokens, last_eof_id + 1, i));
				this.length = this.tokens.size();
				if(iModifier != -1) {
					i = iModifier;
				}
				last_eof_id = i;
				if (tokens.get(this.tok_id).type != TokenType.EOF) {
					System.out.println("Syntax Error, Line invalid");
					EntryPoint.raiseToken(tokens.get(this.tok_id));
					return null;
				}
				if (this.next() != null && this.next().type == end) {
					return parentNodes;
				}
			}
		}
		
		return parentNodes;
	}
	
	private Node parseToken(List<Token> tokens, int begin, int length) {
		this.tokens = tokens;
		this.length = this.tokens.size();
		this.tok_id = begin - 1;
		this.length = length;
		this.advance();
		return parseChoice();
	}
	
	private Node parseChoice() {
		if (this.current_token.type == TokenType.EOF) {
			return null;
		}
		if(this.current_token.type == TokenType.RETURN) {
			return this.parseReturn();
		}
		if(this.current_token.type == TokenType.BREAK) {
			return this.parseBreak();
		}
		if(this.current_token.type == TokenType.CONTINUE) {
			return this.parseContinue();
		}
		if (this.current_token.type == TokenType.FUNCTION) {
			return this.parseFunction();
		}
		if (this.current_token.type == TokenType.IF) {
			return this.parseIf();
		}
		if (this.current_token.type == TokenType.FOR) {
			return this.parseFor();
		}
		return this.parseNode();
	}
	
	private Node parseBreak() {
		Token t = this.current_token;
		this.advance();
		int i = 1;
		if(this.current_token.type == TokenType.LPAREN) {
			this.advance();
			Node ex = this.expr();
			try {
				Object v = ex.evaluate(null);
				if (v instanceof NumberNode && ((NumberNode)v).getValue() instanceof Integer) {
					i = (int) ((NumberNode)v).getValue();
				}else {
					System.out.println("Break only supports integers");
					EntryPoint.raiseToken(t);
					return null;
				}
			} catch(Exception e) {
				System.out.println("Break does not support dynamic expression");
				EntryPoint.raiseToken(t);
				return null;
			}
			if (this.current_token.type != TokenType.RPAREN) {
				System.out.println("Missing right parenthesies of break");
				EntryPoint.raiseToken(t);
				return null;
			}
			this.advance();
		}
		
		if (i<=0) {
			System.out.println("Break only supports positive integers");
			EntryPoint.raiseToken(t);
			return null;
		}
		
		return new BreakNode(t.col, t.line, i);
	}
	
	private Node parseContinue() {
		Token t = this.current_token;
		this.advance();
		int i = 1;
		int j = 1;
		if(this.current_token.type == TokenType.LPAREN) {
			this.advance();
			Node ex = this.expr();
			try {
				Object v = ex.evaluate(null);
				if (v instanceof NumberNode && ((NumberNode)v).getValue() instanceof Integer) {
					i = (int) ((NumberNode)v).getValue();
				}else {
					System.out.println("Continue count only supports integers");
					EntryPoint.raiseToken(t);
					return null;
				}
			} catch(Exception e) {
				System.out.println("Continue count does not support dynamic expression");
				EntryPoint.raiseToken(t);
				return null;
			}
			if (this.current_token.type == TokenType.COMMA) {
				this.advance();
				Node ex2 = this.expr();
				try {
					Object v = ex2.evaluate(null);
					if (v instanceof NumberNode && ((NumberNode)v).getValue() instanceof Integer) {
						j = (int) ((NumberNode)v).getValue();
					}else {
						System.out.println("Continue jump count only supports integers");
						EntryPoint.raiseToken(t);
						return null;
					}
				} catch(Exception e) {
					System.out.println("Continue jump count does not support dynamic expression");
					EntryPoint.raiseToken(t);
					return null;
				}
				
			}
			
			if (this.current_token.type != TokenType.RPAREN) {
				System.out.println("Missing right parenthesies of continue");
				EntryPoint.raiseToken(t);
				return null;
			}
			this.advance();
		}
		
		if (i<=0) {
			System.out.println("Continue count only supports positive integers");
			EntryPoint.raiseToken(t);
			return null;
		}
		if (j<=0) {
			System.out.println("Continue jump count only supports positive integers");
			EntryPoint.raiseToken(t);
			return null;
		}
		
		return new ContinueNode(t.col, t.line, i, j);
	}
	
	public Node parseReturn() {
		Token t = this.current_token;
		this.advance();
		Node dat = this.parseToken(tokens, this.tok_id, this.length);
		return new ReturnGetterNode(t.col, t.line, dat);
	}
	
	private Node parseFunction() {
		this.advance();
		
		if(this.current_token.type == TokenType.NAME) {
			String name = (String) this.current_token.value;
			
			this.advance();
			if(this.current_token.type != TokenType.LPAREN) {
				return null;
			}
			this.advance();
			
			ArrayList<StringNode> args = new ArrayList<>();
			TokenType lastType = this.current_token.type;
			while(this.advanceResult && lastType != TokenType.RPAREN
					&& this.current_token.type == TokenType.NAME) {
				Token a = this.current_token;
				this.advance();
				
				lastType = this.current_token.type;
				
				if(lastType != TokenType.RPAREN) {
					this.advance();
				}
				args.add(new StringNode(a.col, a.line, (String) a.value));
			}
			
			if(this.current_token.type != TokenType.RPAREN) {
				return null;
			}
			this.advance();
			if(this.current_token.type != TokenType.LCURLYBRACKET) {
				return null;
			}
			this.advance();
			
			Parser p = new Parser();
			p.tokens = this.tokens;
			p.tok_id = this.tok_id - 1;
			p.length = this.tokens.size();
			p.advance();
			ArrayList<Node> nodes = p.parse(this.tokens, TokenType.RCURLYBRACKET);
			
			FunctionNode n = new FunctionNode(0, 0);
			n.evaluators = nodes;
			n.arguments = args;
			
			p.advance();
			if (p.current_token.type != TokenType.RCURLYBRACKET) {
				return null;
			}
			
			p.advance();
			EntryPoint.globalContext.setValue(name, n);
			this.iModifier = p.tok_id;
			this.tok_id = p.tok_id - 1;
			this.advance();
			return n;
		}
		
		return null;
	}
	private Node parseIf() {
		this.advance();

		Token t = this.current_token;
		if(this.current_token.type == TokenType.LPAREN) {
			Node expr = this.bin();
			this.tok_id -= 2;
			this.advance();
			if(this.current_token.type != TokenType.RPAREN) {
				System.out.println("Syntax error, missing right parenthesies");
				EntryPoint.raiseToken(t);
				return null;
			}
			
			this.advance();
			if(this.current_token.type != TokenType.LCURLYBRACKET) {
				System.out.println("Syntax error, missing left curly bracket \"{\"");
				EntryPoint.raiseToken(t);
				return null;
			}
			this.advance();
			
			Parser p = new Parser();
			p.tokens = this.tokens;
			p.tok_id = this.tok_id - 1;
			p.length = this.tokens.size();
			p.advance();
			ArrayList<Node> nodes = p.parse(this.tokens, TokenType.RCURLYBRACKET);
			
			FunctionNode n = new FunctionNode(0, 0);
			n.evaluators = nodes;
			n.arguments = new ArrayList<>();
			
			p.advance();
			if (p.current_token.type != TokenType.RCURLYBRACKET) {
				return null;
			}
			p.advance();
			
			this.iModifier = p.tok_id;
			this.tok_id = p.tok_id - 1;
			this.advance();
			
			return new IfNode(t.col, t.line, n, expr);
		}
		
		return null;
	}
	
	private Node parseFor() {
		int cp_tok_id = this.tok_id;
		this.advance();

		Token t = this.current_token;
		if(this.current_token.type == TokenType.LPAREN) {
			this.advance();
			Node exprSet = this.parseToken(this.tokens, this.tok_id, this.tokens.size());
			if(this.current_token.type != TokenType.EOF) {
				System.out.println("Syntax error, missing ;");
				EntryPoint.raiseToken(t);
				return null;
			}
			this.advance();
			Node exprComp = this.bin();
			if(this.current_token.type != TokenType.EOF) {
				System.out.println("Syntax error, missing ;");
				EntryPoint.raiseToken(t);
				return null;
			}
			this.advance();
			Node exprAdv = this.parseToken(this.tokens, this.tok_id, this.tokens.size());
			
			if(this.current_token.type != TokenType.RPAREN) {
				System.out.println("Syntax error, missing right parenthesies");
				EntryPoint.raiseToken(t);
				return null;
			}
			
			this.advance();
			if(this.current_token.type != TokenType.LCURLYBRACKET) {
				System.out.println("Syntax error, missing left curly bracket \"{\"");
				EntryPoint.raiseToken(t);
				return null;
			}
			this.advance();
			
			Parser p = new Parser();
			p.tokens = this.tokens;
			p.tok_id = this.tok_id - 1;
			p.length = this.tokens.size();
			p.advance();
			ArrayList<Node> nodes = p.parse(this.tokens, TokenType.RCURLYBRACKET);
			
			FunctionNode n = new FunctionNode(0, 0);
			n.evaluators = nodes;
			n.arguments = new ArrayList<>();
			
			p.advance();
			if (p.current_token.type != TokenType.RCURLYBRACKET) {
				return null;
			}
			p.advance();
			
			this.iModifier = p.tok_id;
			this.tok_id = p.tok_id - 1;
			this.advance();
			
			return new ForNode(t.col, t.line, n, exprSet, exprComp, exprAdv);
		}
		
		return null;
	}
	
	private Node parseToken(List<Token> tokens, boolean reset) {
		this.tokens = tokens;
		this.length = this.tokens.size();
		if (reset) {
			this.tok_id = -1;
			this.advance();
		} else {
			this.tok_id -= 1;
			this.advance();
		}
		return parseChoice();
	}
	
	private boolean contains(TokenType ty, TokenType[] types) {
		for (TokenType t:types) {
			if (ty == t) {
				return true;
			}
		}
		return false;
	}
	
	private Node parseNode() {
		int cp_tok_id = this.tok_id;
		Token cur_tok = this.current_token;
		
		if(this.current_token.type == TokenType.NAME) {
			this.advance();
			
			if(this.current_token.type == TokenType.SET) {
				this.advance();
				Node n = this.bin();
				if (n == null) {
					return null;
				}
				return new SetterNode(n, (String) cur_tok.value, cur_tok.col, cur_tok.line);
			} else if(this.current_token.type == TokenType.LHOOK) {
				Node listSet = this.listSetExpr(new GetterNode((String) cur_tok.value, cur_tok.col, cur_tok.line));
				if (this.current_token.type == TokenType.SET) {
					this.advance();
					Node n = this.bin();
					if (n == null) {
						return null;
					}
					if (listSet instanceof ListSetterNode) {
						
						ListSetterNode dat = ((ListSetterNode)listSet);
						dat.expr = n;
						return dat;
						
					} else {
						return null;
					}
				}
			}
		}
		
		this.tok_id = cp_tok_id - 1;
		this.advance();
		
		return this.bin();
	}
	
	private Node bin() {
		Node left = this.comp();
		
		TokenType[] types = new TokenType[] {
			TokenType.OR, TokenType.AND
		};
		while (contains(this.current_token.type, types)) {
			Token oper = this.current_token;
			TokenType type = this.current_token.type;
			this.advance();
			Node right = this.comp();
			left = new OpNode(type, left, right, oper.col, oper.line);
		}
		
		return left;
	}
	
	private Node comp() {
		Node left = this.expr();
		
		TokenType[] types = new TokenType[] {
			TokenType.EQ, TokenType.INF, TokenType.NOTEQ, TokenType.SUP,
			TokenType.SUPEQ, TokenType.INFEQ
		};
		while (contains(this.current_token.type, types)) {
			Token oper = this.current_token;
			TokenType type = this.current_token.type;
			this.advance();
			Node right = this.expr();
			left = new OpNode(type, left, right, oper.col, oper.line);
		}
		
		return left;
	}
	
	private Node expr() {
		Node left = this.term();
		
		TokenType[] types = new TokenType[] {
			TokenType.PLUS, TokenType.MINUS
		};
		while (contains(this.current_token.type, types)) {
			Token oper = this.current_token;
			TokenType type = this.current_token.type;
			this.advance();
			Node right = this.term();
			left = new OpNode(type, left, right, oper.col, oper.line);
		}
		
		return left;
	}
	
	private Node term() {
		Node left = this.factor();

		TokenType[] types = new TokenType[] {
			TokenType.MUL, TokenType.DIV, TokenType.POW
		};
		while (contains(this.current_token.type, types)) {
			Token oper = this.current_token;
			TokenType type = this.current_token.type;
			this.advance();
			Node right = this.factor();
			left = new OpNode(type, left, right, oper.col, oper.line);
		}
		
		return left;
	}
	
	private Node factor() {
		Token tok = this.current_token;
		
		if (tok.type == TokenType.NUMBER) {
			this.advance();
			return new NumberNode(tok.value, tok.col, tok.line);
		} else if (tok.type == TokenType.NAME) {
			this.advance();
			return this.listExpr(new GetterNode((String)tok.value, tok.col, tok.line));
	    } else if (tok.type == TokenType.MINUS) {
			this.advance();
			return new UnaryOpNode(tok.type, this.factor(), tok.col, tok.line);
		} else if (tok.type == TokenType.NOT) {
			this.advance();
			return new UnaryOpNode(tok.type, this.factor(), tok.col, tok.line);
		} else if (tok.type == TokenType.STRING) {
			this.advance();
			return this.listExpr(new StringNode(tok.col, tok.line, (String) tok.value));
		} else if (tok.type == TokenType.LPAREN) {
			this.advance();
			Node expr = this.bin();
			if (this.current_token.type == TokenType.RPAREN) {
				this.advance();
				return expr;
			} else {
				System.out.println("Missing right parenthesis");
				EntryPoint.raiseToken(tok);
				return null;
			}
		} else if (tok.type == TokenType.LHOOK) {
			this.advance();
			return this.buildArray();
		} else if (tok.type == TokenType.TRUE) {
			this.advance();
			return new BooleanNode(tok.col, tok.line, true);
		} else if (tok.type == TokenType.FALSE) {
			this.advance();
			return new BooleanNode(tok.col, tok.line, false);
		}
		
		return null;
	}
	
	private Node buildArray() {
		ArrayNode node = new ArrayNode(this.current_token.col, this.current_token.line);
		
		TokenType lastType = this.current_token.type;
		while(this.advanceResult && lastType != TokenType.RHOOK) {
			Node expr = this.bin();
			lastType = this.current_token.type;
			
			this.advance();
			node.add(expr.evaluate(EntryPoint.globalContext));
		}
		
		return node;
	}
	
	private Node listExpr(Node node) {
		Token t = this.current_token;
		node = this.funcExpr(node, t);
		while (this.current_token != null && this.current_token.type == TokenType.LHOOK) {
			this.advance();
			
			Node n = this.bin();
			if (this.current_token.type != TokenType.RHOOK) {
				System.out.println("Syntax error: Missing right hook");
				EntryPoint.raiseToken(t);
				return null;
			}
			if (n == null) {
				System.out.println("Syntax error: Empty expression");
				EntryPoint.raiseToken(t);
				return null;
			}
			this.advance();
			
			node = this.funcExpr(new ListGetterNode(t.col, t.line, node, n), t);
		}
		return node;
	}
	public Node funcExpr(Node node, Token t) {
		while (this.current_token != null && this.current_token.type == TokenType.LPAREN) {
			this.advance();
			
			ArrayList<Node> exprs = new ArrayList<Node>();
			TokenType lastType = this.current_token.type;
			while(this.advanceResult && lastType != TokenType.RPAREN) {
				Node expr = this.bin();
				lastType = this.current_token.type;
				if (lastType != TokenType.RPAREN) {
					this.advance();
				}
				
				exprs.add(expr);
			}
			
			if (lastType != TokenType.RPAREN) {
				System.out.println("Syntax error: Missing right parenthesies");
				EntryPoint.raiseToken(t);
				return null;
			}
			this.advance();
			
			node = new FuncGetterNode(t.col, t.line, node, exprs);
		}
		return node;
	}
	private Node listSetExpr(Node node) {
		Token t = this.current_token;
		while (this.current_token != null && this.current_token.type == TokenType.LHOOK) {
			this.advance();
			
			Node n = this.bin();
			if (this.current_token.type != TokenType.RHOOK) {
				System.out.println("Syntax error: Missing right hook");
				EntryPoint.raiseToken(t);
				return null;
			}
			if (n == null) {
				System.out.println("Syntax error: Empty expression");
				EntryPoint.raiseToken(t);
				return null;
			}
			this.advance();
			
			node = new ListSetterNode(t.col, t.line, node, n);
		}
		return node;
	}
		
}
