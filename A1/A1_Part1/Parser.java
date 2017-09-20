import java.util.ArrayList;
import java.util.HashMap;

// Outline of Parser for TinyPL - Part 1

public class Parser {
	public static void main(String[] args)  {
		System.out.println("Enter program and terminate with 'end'!\n");
		Lexer.lex();
		Code c = new Code(); 
		new Program(c);
		System.out.println("\nBytecodes:\n");
		c.output();
	}
}

class Program {  // program ->  decls stmts end
 
	Decls d;
	Stmts ss;
	public Program(Code c){
		d = new Decls(c);
		ss = new Stmts(c);
		c.push("end");
	}
}

class Decls {  // decls -> int idlist ';'

	Idlist i;
	
	public Decls(Code c){
		if(Lexer.nextToken==Token.KEY_INT){
			Lexer.lex();
			i = new Idlist(c);
			Lexer.lex(); //expected ';'
		}
	}
 
}

class Idlist { // idlist -> id [',' idlist ]
	
	String id;
	Idlist i;
	
	public Idlist(Code c){
		if(Lexer.nextToken==Token.ID){
			id = Lexer.ident;
			c.var_map.put(id, c.getVcount());
			Lexer.lex(); 
			if(Lexer.nextToken==Token.COMMA){
				Lexer.lex();//expected ','
				i = new Idlist(c);
			}
		}
		else{
//			throw System.err.println("Variable expected!");
		}
	}
 
}

class Stmt { // stmt -> assign ';' | cmpd | cond | loop

	Assign a;
	Cmpd cd;
	// to be declared cond and loop
	
	public Stmt(Code c){
		switch(Lexer.nextToken){
		case Token.ID: a = new Assign(c);
					   Lexer.lex();//expected ';'
					   break;
		case Token.LEFT_BRACE: cd = new Cmpd(c);
							   break;
		}
	}
} 

class Stmts { // stmts -> stmt [ stmts ]
	
	Stmt s;
	Stmts ss;
	
	public Stmts(Code c){
		s = new Stmt(c);
		if(Lexer.nextToken==Token.ID||Lexer.nextToken==Token.LEFT_BRACE){
			ss = new Stmts(c);
		}
	}
	
 
}

class Assign { // assign -> id '=' expr 

	Expr e;
	String id;
	
	
	public Assign(Code c){
		if(Lexer.nextToken==Token.ID){
			id=Lexer.ident;
			Lexer.lex();
			if(Lexer.nextToken==Token.ASSIGN_OP){
				Lexer.lex(); // expecting '='
				e = new Expr(c);
				c.push(id,'s');
			}
		}
	}
 
}


class Cmpd { // cmpd -> '{' stmts '}'
	
	Stmts ss;
	
	public Cmpd(Code c){
		if(Lexer.nextToken==Token.LEFT_BRACE){
			Lexer.lex();//expecting '{'
			ss = new Stmts(c);
			Lexer.lex();//expecting '}'
		}
	}

 
}

class Expr { // expr -> term  [ ('+' | '-') expr ]

	Term t;
	Expr e;
	char op;
	
	public Expr(Code c){
		t = new Term(c);
		if(Lexer.nextToken==Token.ADD_OP||Lexer.nextToken==Token.SUB_OP){
			op = Lexer.nextChar;
			Lexer.lex();
			e = new Expr(c);
			c.push(op);
		}
		
	}
 
}

class Term { // term -> factor [ ('*' | '/') term ]
	
	Factor f;
	Term t;
	char op;
	
	public Term(Code c){
		f = new Factor(c);
		if(Lexer.nextToken==Token.MULT_OP||Lexer.nextToken==Token.DIV_OP){
			op = Lexer.nextChar;
			Lexer.lex();
			t = new Term(c);
			c.push(op);
		}
	}
 
}

class Factor { // factor -> int_lit | id | '(' expr ')'
	
	Expr e;
	int num;
	String id;
	
	public Factor(Code c){
		switch(Lexer.nextToken){
		case Token.INT_LIT:
			num=Lexer.intValue;
			c.push(num);
			Lexer.lex();
			break;
		case Token.ID:
			id=Lexer.ident;
			c.push(id, 'l');
			Lexer.lex();
			break;
		case Token.LEFT_PAREN:
			Lexer.lex();
			e = new Expr(c);
			Lexer.lex();// Expecting ')'
			break;
		default:
			break;
		}
	}
	
 
}

class Code {
	
	ArrayList<String> code_stack = new ArrayList<>();
	ArrayList<Integer> Mem_use = new ArrayList<>();
	HashMap<String, Integer> var_map = new HashMap<>();
	int v_count = 0;
	
	public int getVcount(){
		v_count++;
		return v_count-1;
	}
	public void output(){
		System.out.println();
		for(int i=0,j=0; i<code_stack.size(); i++){
			System.out.println(j+": "+code_stack.get(i));
			j+=Mem_use.get(i);
		}
	}
	
	public void push(String s){
		if(s.equals("end")){
			code_stack.add("return");
			Mem_use.add(1);
		}
	}
	public void push(String id, char op){
		if(op=='s'){
			if(var_map.get(id)<4){
				code_stack.add("istore_"+var_map.get(id));
				Mem_use.add(1);
			}
			else{
				code_stack.add("istore "+var_map.get(id));
				Mem_use.add(2);
			}
		}
		else if(op=='l'){
			if(var_map.get(id)<4){
				code_stack.add("iload_"+var_map.get(id));
				Mem_use.add(1);
			}
			else{
				code_stack.add("iload "+var_map.get(id));
				Mem_use.add(2);
			}
		}
	}
	public void push(char op){
		switch(op){
		case '+': code_stack.add("iadd");
				  Mem_use.add(1);
				  break;
		case '-': code_stack.add("isub");
				  Mem_use.add(1);
				  break;
		case '*': code_stack.add("imul");
				  Mem_use.add(1);
				  break;
		case '/': code_stack.add("idiv");
				  Mem_use.add(1);
				  break;
		default:
		}
	}
	public void push(int num){
		if(num>127){
			code_stack.add("sipush "+num);
			Mem_use.add(3);
		}
		else if(num>5){ 
			code_stack.add("bipush "+num);
			Mem_use.add(2);
		}
		else{
			code_stack.add("iconst_"+num);
			Mem_use.add(1);
		}
	}
	
	
}


    
