import java.util.ArrayList;
import java.util.HashMap;

// Outline of Parser for TinyPL - Part 1
// we need to do the same for if as we 

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
//		Lexer.lex();
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
	Cond co;
	Loop lp;
	
	// to be declared cond and loop
	
	public Stmt(Code c){
		switch(Lexer.nextToken){
		case Token.ID: a = new Assign(c);
					   Lexer.lex();//expected ';'
					   break;
		case Token.LEFT_BRACE: cd = new Cmpd(c);
							   break;
		case Token.KEY_IF: co = new Cond(c);
						   break;
		case Token.KEY_FOR: lp = new Loop(c);
			break;
		}
	}
} 

class Stmts { // stmts -> stmt [ stmts ]
	
	Stmt s;
	Stmts ss;
	
	public Stmts(Code c){
		s = new Stmt(c);
		if(Lexer.nextToken==Token.ID||Lexer.nextToken==Token.LEFT_BRACE||Lexer.nextToken==Token.KEY_IF||Lexer.nextToken==Token.KEY_FOR){
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


class Cond { // Cond -> if '(' rel_exp ')' stmt [ else stmt ]
	
	rel_exp ss;
	Stmt sti,ste;
	int if_jmp, el_jmp;
	
	public Cond(Code c){
		if(Lexer.nextToken==Token.KEY_IF){
			Lexer.lex();// Jump 'if'
			
			Lexer.lex();  //expecting '('
			ss = new rel_exp(c);
			Lexer.lex();//expecting ')'
			if_jmp = c.push('i',ss.op);
			sti = new Stmt(c);
			if(Lexer.nextToken==Token.KEY_ELSE)
			{
				el_jmp = c.push('i','g');
				c.updateif(if_jmp);
				Lexer.lex();
				ste = new Stmt(c);
				c.updateif(el_jmp);
			}
			else{
				c.updateif(if_jmp);
			}
		}
	}

 
}

class Loop{//loop -> for '(' [assign] ';' [rel_exp] ';' [assign] ')' stmt
	Assign as1;
	rel_exp re;
	Assign as2;
	Stmt st;
	int for_jmp=-1, for_st = -1, for_ed = -1, fn_goto=0;
	
	public Loop(Code c){
		if(Lexer.nextToken==Token.KEY_FOR){
			Lexer.lex();//Jump 'for'
			Lexer.lex();// expecting '('
			if(Lexer.nextToken!=Token.SEMICOLON) as1 = new Assign(c);
			Lexer.lex();//expecting ';'
			fn_goto = c.getStackIndex();
			if(Lexer.nextToken!=Token.SEMICOLON){
				re = new rel_exp(c);
				for_jmp = c.push('i', re.op);
			}
			Lexer.lex(); // expecting ';'
			if(Lexer.nextToken!=Token.RIGHT_PAREN){
				for_st = c.getStackIndex();
				as2 = new Assign(c);
				for_ed = c.getStackIndex();
			}
			Lexer.lex(); // expecting ')'
			st = new Stmt(c);
			if(for_st!=-1&&for_ed!=-1){
				c.repush(for_st, for_ed);
			}
			c.push("goto **-<"+(c.getStackIndex()-fn_goto));
			if(for_jmp!=-1){
				c.updatefor(for_jmp);
			}
		}
	}
}

class rel_exp { // rel_exp -> expr ('<' | '>' | '==' | '!= ') expr
	
	Expr ss,se;
	Cmpd st;
	char op;
	public rel_exp(Code c){
		ss = new Expr(c);
		if(Lexer.nextToken==Token.LESSER_OP||Lexer.nextToken==Token.GREATER_OP||Lexer.nextToken==Token.EQ_OP||Lexer.nextToken==Token.NOT_EQ){
			op = Lexer.nextChar;
			Lexer.lex();
			se = new Expr(c);
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
	ArrayList<String> for_stack = new ArrayList<>();
	int v_count = 0;
	int step_num = 0;
	
	public int getVcount(){
		v_count++;
		return v_count-1;
	}
	public int getStackIndex(){
		return code_stack.size()-1;
	}
	public int getStepNum(){
		return step_num;
	}
	public void output(){
		System.out.println();
		for(int i=0; i<code_stack.size(); i++){
			if(code_stack.get(i).contains("**->")){
				int strt = code_stack.get(i).indexOf("**->");
				int add_idx = Integer.parseInt(code_stack.get(i).substring(strt+4));
				int idx_val = Mem_use.get(i+add_idx+1);
				code_stack.set(i, code_stack.get(i).substring(0, strt)+idx_val);
			}
			else if(code_stack.get(i).contains("**-<")){
				int strt = code_stack.get(i).indexOf("**-<");
				int add_idx = Integer.parseInt(code_stack.get(i).substring(strt+4));
				int idx_val = Mem_use.get(i-add_idx);
				code_stack.set(i, code_stack.get(i).substring(0, strt)+idx_val);
			}
			System.out.println(Mem_use.get(i)+": "+code_stack.get(i));
		}
	}
	
	public void push(String s){
		if(s.equals("end")){
			code_stack.add("return");
			Mem_use.add(step_num);
			step_num += 1;
		}
		else if(s.contains("goto")){
			code_stack.add(s);
			Mem_use.add(step_num);
			step_num += 3;
		}
	}
	public void push(String id, char op){
		if(op=='s'){
			if(var_map.get(id)<4){
				code_stack.add("istore_"+var_map.get(id));
				Mem_use.add(step_num);
				step_num += 1;
			}
			else{
				code_stack.add("istore "+var_map.get(id));
				Mem_use.add(step_num);
				step_num += 2;
			}
		}
		else if(op=='l'){
			if(var_map.get(id)<4){
				code_stack.add("iload_"+var_map.get(id));
				Mem_use.add(step_num);
				step_num += 1;
			}
			else{
				code_stack.add("iload "+var_map.get(id));
				Mem_use.add(step_num);
				step_num += 2;
			}
		}
	}
	
	public int push(char id, char op){
		switch(op){
		case '>':
			code_stack.add("if_icmple"+" ");
			Mem_use.add(step_num);
			step_num += 3;
			break;
		case '<':
			code_stack.add("if_icmpge"+" ");
			Mem_use.add(step_num);
			step_num += 3;
			break;
		case '!':
			code_stack.add("if_icmpeq"+" ");
			Mem_use.add(step_num);
			step_num += 3;
			break;
		case '=':
			code_stack.add("if_icmpne"+" ");
			Mem_use.add(step_num);
			step_num += 3;
			break;
		case 'g': 
			code_stack.add("goto ");
			Mem_use.add(step_num);
			step_num += 3;
			break;
		}
		return code_stack.size()-1;
	}
	
	public void push(char op){
		switch(op){
		case '+': code_stack.add("iadd");
		  Mem_use.add(step_num);
		  step_num += 1;
          break;
		case '-': code_stack.add("isub");
		  Mem_use.add(step_num);
		  step_num += 1;
		  break;
		case '*': code_stack.add("imul");
		  Mem_use.add(step_num);
		  step_num += 1;
		  break;
		case '/': code_stack.add("idiv");
		  Mem_use.add(step_num);
		  step_num += 1;
		  break;
		default:
		}
	}
	public void push(int num){
		if(num>127){
			code_stack.add("sipush "+num);
			  Mem_use.add(step_num);
			  step_num += 3;
		}
		else if(num>5){ 
			code_stack.add("bipush "+num);
			  Mem_use.add(step_num);
			  step_num += 2;
		}
		else{
			code_stack.add("iconst_"+num);
			  Mem_use.add(step_num);
			  step_num += 1;
		}
	}
	public void updateif(int index){
		code_stack.set(index, code_stack.get(index)+"**->"+(this.getStackIndex()-index));
	}
	public void updatefor(int index){
		code_stack.set(index, code_stack.get(index)+"**->"+(this.getStackIndex()-index));
	}
	
	
	public void repush(int st, int ed){
		for(int i=0; i<ed-st; i++){
			code_stack.add(code_stack.get(st+1));
			code_stack.remove(st+1);
		}
		int cur_mem = Mem_use.size();
		int num_elem_af = cur_mem-ed-1;
		int num_elem_bf = ed-st;
		int inc = st+2;

		for(int i=num_elem_af-1; i>0; i--,inc++){
			Mem_use.add(inc, (Mem_use.get(inc-1)+(Mem_use.get(Mem_use.size()-i)-Mem_use.get(Mem_use.size()-i-1))));
		}
		Mem_use.add(inc, (Mem_use.get(inc-1)+(step_num-Mem_use.get(Mem_use.size()-1))));
		inc++;

		for(int i=1; i<num_elem_bf; i++,inc++){
			Mem_use.add(inc, (Mem_use.get(inc-1)+(Mem_use.get(inc+i)-Mem_use.get(inc+i-1))));
		}
		
		for(int i=inc; i<Mem_use.size();){
			Mem_use.remove(i);
		}

	}
}


    
