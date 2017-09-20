import java.io.*;

public class Lexer {

	static public char ch = ' ';
	static public String ident = "";
	static private Buffer buffer = new Buffer(new DataInputStream(System.in));
	static public int nextToken;
	static public char nextChar;
	static public int intValue;

	public static int lex() {
		while (Character.isWhitespace(ch))
			ch = buffer.getChar();
		if (Character.isLetter(ch)) {
			ident = getIdent();
			switch (ident) {
			case "if": nextToken = Token.KEY_IF; break;
			case "int": nextToken = Token.KEY_INT; break;
			case "else": nextToken = Token.KEY_ELSE; break;
			case "end": nextToken = Token.KEY_END; break;
			case "for": nextToken = Token.KEY_FOR; break;
			default: nextToken = Token.ID;
			}
		} else if (Character.isDigit(ch)) {
			nextToken = getNumToken(); // intValue would be set
		} else {
			nextChar = ch;
			switch (ch) {
			case ';':
				nextToken = Token.SEMICOLON;
				ch = buffer.getChar();
				break;
			case ',':
				nextToken = Token.COMMA;
				ch = buffer.getChar();
				break;
			case '+':
				nextToken = Token.ADD_OP;
				ch = buffer.getChar();
				break;
			case '-':
				nextToken = Token.SUB_OP;
				ch = buffer.getChar();
				break;
			case '*':
				nextToken = Token.MULT_OP;
				ch = buffer.getChar();
				break;
			case '/':
				nextToken = Token.DIV_OP;
				ch = buffer.getChar();
				break;
			case '=':
				ch = buffer.getChar();
				if (ch == '=') 
					  { nextToken = Token.EQ_OP; 
					    ch = buffer.getChar(); 
					  }
				else nextToken = Token.ASSIGN_OP;
				break;
			case '<':
				ch = buffer.getChar();
				if (ch == '/') {nextToken = Token.LTDIV; 
				                ch = buffer.getChar(); }
				else nextToken = Token.LESSER_OP;
				break;
			case '>':
				nextToken = Token.GREATER_OP;
				ch = buffer.getChar();
				break;
			case '!':
				ch = buffer.getChar(); // '='
				nextToken = Token.NOT_EQ;
				ch = buffer.getChar();
				break;
			case '(':
				nextToken = Token.LEFT_PAREN;
				ch = buffer.getChar();
				break;
			case ')':
				nextToken = Token.RIGHT_PAREN;
				ch = buffer.getChar();
				break;
			case '{':
				nextToken = Token.LEFT_BRACE;
				ch = buffer.getChar();
				break;
			case '}':
				nextToken = Token.RIGHT_BRACE;
				ch = buffer.getChar();
				break;
			default:
				error("Illegal character " + ch);
				break;
			}
		}
		return nextToken;
	} // lex

	private static String getIdent() {
		// ch is declared in class Lexer
		String ident = "";
		do {
			ident = ident + ch;
			ch = buffer.getChar();
		} while (Character.isLetter(ch) || Character.isDigit(ch));
		return ident;
	}
	
	private static int getNumToken() {
		int num = 0;
		do {
			num = num * 10 + Character.digit(ch, 10);
			ch = buffer.getChar();
		} while (Character.isDigit(ch));
		intValue = num;
		return Token.INT_LIT;
	}
	
	public int number() {
		return intValue;
	} // number

	public String identifier() {
		return ident;
	} // letter

	public static void error(String msg) {
		System.err.println(msg);
		System.exit(1);
	} // error


}
