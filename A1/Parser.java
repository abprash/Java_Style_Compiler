// Outline of Parser for TinyPL - Part 1

public class Parser {
	public static void main(String[] args)  {
		System.out.println("Enter program and terminate with 'end'!\n");
		Lexer.lex();
		new Program();
		System.out.println("\nBytecodes:\n");
		Code.output();
	}
}

class Program {  // program ->  decls stmts end
 
}

class Decls {  // decls -> int idlist ';'

 
}

class Idlist { // idlist -> id [',' idlist ]
 
}

class Stmt { // stmt -> assign ';' | cmpd | cond | loop

 
} 

class Stmts { // stmts -> stmt [ stmts ]

 
}

class Assign { // assign -> id '=' expr 

 
}


class Cmpd { // cmpd -> '{' stmts '}'

 
}

class Expr { // expr -> term  [ ('+' | '-') expr ]

 
}

class Term { // term -> factor [ ('*' | '/') term ]
 
}

class Factor { // factor -> int_lit | id | '(' expr ')'
 
}

class Code {
 
}


    
