import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
/* Tokenizer, parser and semantic analyzer for the C minus language that accepts float's and int's.
 * 
 * Files must be called from the command line.
 */

public class Tokenize {
	public static ArrayList<String> ogCode = new ArrayList<String>();

	public static void main(String[] args) {
		File inFile;
		if (0 < args.length) {
			inFile = new File(args[0]);
		} else {
			System.out.println("Invalid file input");
			return;
		}
		BufferedReader br = null;
		try {
			String currentLine;
			br = new BufferedReader(new FileReader(inFile));
			while ((currentLine = br.readLine()) != null) {
				currentLine = (currentLine).replaceAll(";", " ;");
				currentLine = (currentLine).replaceAll("E0", "E+0");
				currentLine = (currentLine).replaceAll("E1", "E+1");
				currentLine = (currentLine).replaceAll("E2", "E+2");
				currentLine = (currentLine).replaceAll("E3", "E+3");
				currentLine = (currentLine).replaceAll("E4", "E+4");
				currentLine = (currentLine).replaceAll("E5", "E+5");
				currentLine = (currentLine).replaceAll("E6", "E+6");
				currentLine = (currentLine).replaceAll("E7", "E+7");
				currentLine = (currentLine).replaceAll("E8", "E+8");
				currentLine = (currentLine).replaceAll("E9", "E+9");

				ogCode.add(currentLine + " ");
			}
			ogCode.add(" ");
		} // try
		catch (IOException e) {
		}
		
		ArrayList<Token[]> temp = tokenizer(ogCode);
		
		ArrayList<Token> temp2 = printTokenList(temp);
		if(TopDownParser.parser(temp2))
			System.out.println("ACCEPT");
		}// main

	public static ArrayList<Token[]> tokenizer(ArrayList<String> list) {
		ArrayList<Token[]> tokenList = new ArrayList<>();
		TypeTable typeTable = new TypeTable();
		int nestDepth = 0;
		for (int i = 0; i < list.size(); i++) {
			Token[] tokenArray = new Token[80];
			String currentLine;
			int tokenCount = 0;
			currentLine = list.get(i);

			if (currentLine.length() > 1) {
				///////////// if a single line comment//////////////////////////////////
				if ((Character.toString(currentLine.charAt(0)) + Character.toString(currentLine.charAt(1))) == "//"
						|| currentLine == "\n") {
					// skip
					currentLine = "";
				} else if (currentLine.indexOf("//") != -1) {
					currentLine = currentLine.split("//")[0];
				}
			} ///////////// if a single line comment//////////////////////////////////
			String charSet = "";

			if (currentLine != "" || currentLine != "\n" || currentLine != null) {
				for (int z = 0; z < currentLine.length(); z++) {
					if (Character.toString(currentLine.charAt(z)).matches("[a-z]")
							|| Character.toString(currentLine.charAt(z)).matches("[0-9]")) {
						if (nestDepth == 0)
							charSet += currentLine.charAt(z);

					}
					////////////// if a multi line commment//////////////////////////////////
					else if (currentLine.charAt(z) == '/') {
						if (charSet != "") {
							String type = typeFinder(charSet);
							Token tempToken = new Token(charSet, type);
							tokenArray[tokenCount] = tempToken;
							tokenCount++;
							charSet = "";
						}
						if (z + 1 < currentLine.length()) {
							if (currentLine.charAt(z + 1) == '*') {
								z++;
								nestDepth++;
							} else {
								if (nestDepth == 0) {
									Token tempToken2 = new Token("/", "special");
									tokenArray[tokenCount] = tempToken2;
									tokenCount++;
									charSet = "";
								}
							}
						} else {
							if (nestDepth == 0) {
								Token tempToken2 = new Token("/", "special");
								tokenArray[tokenCount] = tempToken2;
								tokenCount++;
								charSet = "";
							}
						}
					} else if (currentLine.charAt(z) == '*' && nestDepth > 0) {
						if (z + 1 < currentLine.length()) {
							if (currentLine.charAt(z + 1) == '/') {
								z++;
								nestDepth--;
							}
						}
					}
					////////////// if a multi line commment//////////////////////////////////
					///////////// if space///////////////////////////////////////////////////
					else if (currentLine.charAt(z) == ' ' && nestDepth == 0) {
						if (charSet != "") {
							String type = typeFinder(charSet);
							Token tempToken = new Token(charSet, type);
							charSet = "";
							tokenArray[tokenCount] = tempToken;
							tokenCount++;
						}
					}
					///////////// if space///////////////////////////////////////////////////
					//////////// if E////////////////////////////////////////////////////////
					else if(currentLine.charAt(z)=='E' && !charSet.matches("\\d+[.]?\\d+"))
					{
						if (charSet != "") {
							String type = typeFinder(charSet);
							Token tempToken = new Token(charSet, type);
							charSet = "";
							tokenArray[tokenCount] = tempToken;
							tokenCount++;
						}
						while (typeTable.findType(Character.toString(currentLine.charAt(z))) != "delimiter"
								&& currentLine.charAt(z) != ' '
								&& typeTable.findType(Character.toString(currentLine.charAt(z))) != "special"
								&& z + 1 < currentLine.length()) {
							charSet += currentLine.charAt(z);
							z++;
						}
						Token tempToken2 = new Token(charSet, "error");
						tokenArray[tokenCount] = tempToken2;
						tokenCount++;
						charSet = "";
						if (typeTable.findType(Character.toString(currentLine.charAt(z))) == "delimiter") {
							Token tempToken3 = new Token(Character.toString(currentLine.charAt(z)), "delimiter");
							tokenArray[tokenCount] = tempToken3;
							tokenCount++;
						}
						else if (typeTable.findType(Character.toString(currentLine.charAt(z))) == "special") {
							Token tempToken3 = new Token(Character.toString(currentLine.charAt(z)), "special");
							tokenArray[tokenCount] = tempToken3;
							tokenCount++;
						}
					}
					else if(currentLine.charAt(z)=='E' && isValidFloat(charSet))
					{
						charSet += 'E';
					}
					//////////// if E////////////////////////////////////////////////////////
					/////////////if decimal/////////////////////////////////////////////////
					else if(currentLine.charAt(z) == '.' && nestDepth == 0)
					{
						if(charSet != "")
						{
							if(charSet.matches("\\d+"))
							{
								charSet += '.';
							}
							else
							{
								String type = typeFinder(charSet);
								Token tempToken = new Token(charSet, type);
								charSet = "";
								tokenArray[tokenCount] = tempToken;
								tokenCount++;
							}
						}else
						{
							Token tempToken = new Token(".", "error");
							tokenArray[tokenCount] = tempToken;
							tokenCount++;
						}
					}
					/////////////if decimal/////////////////////////////////////////////////

					//////////// if forbidden////////////////////////////////////////////////
					else if (typeTable.findType(Character.toString(currentLine.charAt(z))) == "forbidden"
							&& nestDepth == 0) {
						if (charSet != "") {
							String type = typeFinder(charSet);
							Token tempToken = new Token(charSet, type);
							charSet = "";
							tokenArray[tokenCount] = tempToken;
							tokenCount++;
						}
						while (typeTable.findType(Character.toString(currentLine.charAt(z))) != "delimiter"
								&& currentLine.charAt(z) != ' '
								&& typeTable.findType(Character.toString(currentLine.charAt(z))) != "special"
								&& z + 1 < currentLine.length()) {
							charSet += currentLine.charAt(z);
							z++;
						}
						Token tempToken2 = new Token(charSet, "error");
						tokenArray[tokenCount] = tempToken2;
						tokenCount++;
						charSet = "";
						if (typeTable.findType(Character.toString(currentLine.charAt(z))) == "delimiter") {
							Token tempToken3 = new Token(Character.toString(currentLine.charAt(z)), "delimiter");
							tokenArray[tokenCount] = tempToken3;
							tokenCount++;
						}
						else if (typeTable.findType(Character.toString(currentLine.charAt(z))) == "special") {
							Token tempToken3 = new Token(Character.toString(currentLine.charAt(z)), "special");
							tokenArray[tokenCount] = tempToken3;
							tokenCount++;
						}


					}
					//////////// if forbidden/////////////////////////////////////////////////

					//////////// if delimiter/////////////////////////////////////////////////
					else if (typeTable.findType(Character.toString(currentLine.charAt(z))) == "delimiter"
							&& nestDepth == 0) {
						if (charSet != "") {
							String type = typeFinder(charSet);
							Token tempToken = new Token(charSet, type);
							charSet = "";
							tokenArray[tokenCount] = tempToken;
							tokenCount++;
						}
						Token tempToken2 = new Token(Character.toString(currentLine.charAt(z)), "delimiter");
						tokenArray[tokenCount] = tempToken2;
						tokenCount++;
					}
					//////////// if delimiter/////////////////////////////////////////////////

					//////////// if EOL///////////////////////////////////////////////////////
					else if (z + 1 == currentLine.length() && nestDepth == 0) {
						if (charSet != "") {

							String type = typeFinder(charSet);
							Token tempToken = new Token(charSet, type);
							charSet = "";
							tokenArray[tokenCount] = tempToken;
							tokenCount++;
						}
					}
					//////////// if EOL///////////////////////////////////////////////////////

					//////////// if mathematic symbol//////////////////////////////////////////
					else if (currentLine.charAt(z) == '*' && nestDepth == 0) {
						if (charSet != "") {
							String type = typeFinder(charSet);
							Token tempToken = new Token(charSet, type);
							charSet = "";
							tokenArray[tokenCount] = tempToken;
							tokenCount++;
						}
						Token tempToken2 = new Token(Character.toString(currentLine.charAt(z)), "multiplier");
						tokenArray[tokenCount] = tempToken2;
						tokenCount++;
					}
					//////////// if mathematic symbol//////////////////////////////////////////

					//////////////////////////////// if float//////////////////////////////////
					else if (currentLine.charAt(z) == '-' ||currentLine.charAt(z) == '+' && nestDepth == 0) {
						
						if (charSet == "") {
							if(currentLine.charAt(z)=='+')
							{
							Token tempToken = new Token("+", "special");
							tokenArray[tokenCount] = tempToken;
							tokenCount++;
							}
							else if(currentLine.charAt(z) == '-' && nestDepth == 0)
							{
								Token tempToken = new Token("-", "special");
								tokenArray[tokenCount] = tempToken;
								tokenCount++;	
							}
						} else if (isValidFloat(charSet)) {//need to make this regular expresion work
							charSet += currentLine.charAt(z);
							z++;
							if(currentLine.charAt(z) == 'E' && nestDepth == 0)
							{
								charSet += 'E';
								z++;
							}
							if(currentLine.charAt(z) == '+' || currentLine.charAt(z) == '-')
							{
								charSet+= currentLine.charAt(z);
								z++;
							}
							while (Character.toString(currentLine.charAt(z)).matches("\\d")) {
								charSet += currentLine.charAt(z);
								z++;
							}
							currentLine = " " + currentLine;
							Token tempToken = new Token(charSet, "float");
							tokenArray[tokenCount] = tempToken;
							tokenCount++;
							charSet = "";
						} else {
							if (charSet != "" && nestDepth == 0) {
								String type = typeFinder(charSet);
								Token tempToken = new Token(charSet, type);
								tokenArray[tokenCount] = tempToken;
								tokenCount++;
								charSet = "";
							}
							if(nestDepth == 0)
							{
							Token tempToken2 = new Token("-", "minus");
							tokenArray[tokenCount] = tempToken2;
							tokenCount++;
							}
						}
					}
					//////////////////////////////// if float//////////////////////////////////

					//////////////////////////////// relOp////////////////////////////////////////////////////////////
					else if (currentLine.charAt(z) == '=' && nestDepth == 0) {
						if (charSet != "") {
							String type = typeFinder(charSet);
							Token tempToken = new Token(charSet, type);
							charSet = "";
							tokenArray[tokenCount] = tempToken;
							tokenCount++;
						}
						if (z + 1 < currentLine.length()) {
							if (currentLine.charAt(z + 1) == '=') {
								z++;
								Token tempToken = new Token("==", "special");
								tokenArray[tokenCount] = tempToken;
								tokenCount++;
							} else {
								Token tempToken = new Token("=", "special");
								tokenArray[tokenCount] = tempToken;
								tokenCount++;
							}
						} else {
							Token tempToken = new Token("=", "special");
							tokenArray[tokenCount] = tempToken;
							tokenCount++;
						}
					} else if (currentLine.charAt(z) == '!' && nestDepth == 0) {
						
						if (charSet != "") {
							String type = typeFinder(charSet);
							Token tempToken = new Token(charSet, type);
							charSet = "";
							tokenArray[tokenCount] = tempToken;
							tokenCount++;
						}
						
						if (z + 1 < currentLine.length()) {
							if (currentLine.charAt(z + 1) == '=') {
								z++;
								Token tempToken2 = new Token("!=", "special");
								tokenArray[tokenCount] = tempToken2;
								tokenCount++;
							} else {
								Token tempToken = new Token("!", "error");
								tokenArray[tokenCount] = tempToken;
								tokenCount++;
							}
						} else {
							Token tempToken = new Token("!", "error");
							tokenArray[tokenCount] = tempToken;
							tokenCount++;
						}
					} else if (currentLine.charAt(z) == '>' || currentLine.charAt(z) == '<' && nestDepth == 0) {
						if (z + 1 < currentLine.length()) {
							if (currentLine.charAt(z + 1) == '=' && currentLine.charAt(z) == '>') {
								Token tempToken = new Token(Character.toString(currentLine.charAt(z)) + ">=", "special");
								tokenArray[tokenCount] = tempToken;
								tokenCount++;
								z++;
								
							}else if (currentLine.charAt(z + 1) == '=' && currentLine.charAt(z) == '<') {
								Token tempToken = new Token(Character.toString(currentLine.charAt(z)) + "<=", "special");
								tokenArray[tokenCount] = tempToken;
								tokenCount++;
								z++;
							}  
							else {
								Token tempToken = new Token(Character.toString(currentLine.charAt(z)), "special");
								tokenArray[tokenCount] = tempToken;
								tokenCount++;
							}
						}
						//////////// if relOp////////////////////////////////////////////////////
					}
				} // for z
			} // if currentLine is a valid string
			tokenList.add(tokenArray);
		} // for i

		return tokenList;
	}// tokenize

	public static Boolean isValidInt(String intString) {
		if (intString.matches("\\d+"))
			return true;
		else
			return false;
	}

	public static Boolean isValidFloat(String floatString) {
		int i = 0;
		String temp = "";
		int opcount = 0;
		while(Character.toString(floatString.charAt(i)).matches("\\d") && i < floatString.length() - 1)
		{
			temp += floatString.charAt(i);
			i++;
		}
		if(floatString.charAt(i) == '.' )
		{
			temp += floatString.charAt(i);
			i++;
		}
		while(Character.toString(floatString.charAt(i)).matches("\\d") && i < floatString.length() - 1)
		{
			temp += floatString.charAt(i);
			i++;
		}if(floatString.charAt(i) == 'E')
		{
			temp += floatString.charAt(i);
			
		
		if(floatString.charAt(i) == '+'
					||floatString.charAt(i)== '-')
			{
				temp += floatString.charAt(i);
				i++;
			}
		}
		if(Character.toString(floatString.charAt(i)).matches("\\d"))
		{
			temp += floatString.charAt(i);
		}
		if(temp.length() == floatString.length())
		{
			return true;	
		}else {
			return false;
		}
		
	}

	public static Boolean isValidID(String idString) {
		if (idString.matches("[a-z]+"))
			return true;
		else
			return false;
	}

	public static String typeFinder(String charSet) {
		String type = "";
		TypeTable typeTable = new TypeTable();
		if (charSet != null && charSet != " " && charSet != "") {
			if (typeTable.findType(charSet) != "notfound") {
				type = typeTable.findType(charSet);
			} else if (isValidInt(charSet)) {
				type = "int";
			} else if (isValidFloat(charSet)) {
				type = "float";
			} else if (isValidID(charSet)) {
				type = "ID";
			} else
				type = "error";
		}
		return type;
	}
		// print token list
	public static ArrayList<Token> printTokenList(ArrayList<Token[]> list) {
		ArrayList<Token> temp = new ArrayList<Token>();
		for (int i = 0; i < list.size() - 1; i++) {
			for (int ii = 0; list.get(i)[ii] != null; ii++) {
				temp.add(list.get(i)[ii]);
			}
		}
		return temp;
	}
	
}

// Token Class
class Token {
	String value, tokenType;

	// constructor initialization with string value and token type
	public Token(String value, String tokenType) {
		this.value = value;
		this.tokenType = tokenType;
	}

	public String getTokenString() {
		return value;
	}

	public String getTokenType() {
		return tokenType;
	}
}

class TypeTable {

	static Hashtable<String, String> typeTable = new Hashtable<String, String>();

	public TypeTable() {
		typeTable.put(".", "decimal");
		typeTable.put("@", "error");
		typeTable.put("#", "error");
		typeTable.put("$", "error");
		typeTable.put("%", "error");
		typeTable.put("^", "error");
		typeTable.put("&", "error");
		typeTable.put("~", "error");
		typeTable.put("`", "error");
		typeTable.put(">=", "special");
		typeTable.put("<=", "special");
		typeTable.put("==", "special");
		typeTable.put("!=", "special");
		typeTable.put(">", "special");
		typeTable.put("<", "special");
		typeTable.put("@", "forbidden");
		typeTable.put("?", "forbidden");
		typeTable.put("_", "forbidden");
		typeTable.put("~", "forbidden");
		typeTable.put("`", "forbidden");
		typeTable.put("A", "forbidden");
		typeTable.put("B", "forbidden");
		typeTable.put("C", "forbidden");
		typeTable.put("D", "forbidden");
		typeTable.put("F", "forbidden");
		typeTable.put("H", "forbidden");
		typeTable.put("I", "forbidden");
		typeTable.put("J", "forbidden");
		typeTable.put("K", "forbidden");
		typeTable.put("L", "forbidden");
		typeTable.put("M", "forbidden");
		typeTable.put("N", "forbidden");
		typeTable.put("O", "forbidden");
		typeTable.put("P", "forbidden");
		typeTable.put("Q", "forbidden");
		typeTable.put("R", "forbidden");
		typeTable.put("S", "forbidden");
		typeTable.put("T", "forbidden");
		typeTable.put("U", "forbidden");
		typeTable.put("V", "forbidden");
		typeTable.put("W", "forbidden");
		typeTable.put("X", "forbidden");
		typeTable.put("Y", "forbidden");
		typeTable.put("Z", "forbidden");
		typeTable.put("+", "special");
		typeTable.put("-", "special");
		typeTable.put("*", "special");
		typeTable.put("/", "special");
		typeTable.put("/", "special");
		typeTable.put("=", "special");
		typeTable.put("!", "error");
		typeTable.put(";", "delimiter");
		typeTable.put(",", "delimiter");
		typeTable.put("(", "delimiter");
		typeTable.put(")", "delimiter");
		typeTable.put("[", "delimiter");
		typeTable.put("]", "delimiter");
		typeTable.put("{", "delimiter");
		typeTable.put("}", "delimiter");
		typeTable.put("if", "keyword");
		typeTable.put("for", "keyword");
		typeTable.put("return", "keyword");
		typeTable.put("else", "keyword");
		typeTable.put("int", "keyword");
		typeTable.put("float", "keyword");
		typeTable.put("void", "keyword");
		typeTable.put("while", "keyword");
	}

	public static void populate() {
		typeTable = new Hashtable<String, String>();
	}

	public static String findType(String key) {
		if (typeTable.get(key) == null)
			return "notfound";
		else
			return typeTable.get(key);
	}

	static public Boolean addID(String key) {
		if (typeTable.get(key) == null) {
			typeTable.put(key, "ID");
			return true;
		} else {
			return false;
		}
	}
}
class IDinfo
{
	String type = "";
	String subtype = "";
	ArrayList<String> paramTypes = new ArrayList<String>();
	public IDinfo(String type, String subtype, ArrayList<String> paramTypes)
	{
		this.type = type;
		this.subtype = subtype;
		this.paramTypes = paramTypes;
	}
}

class TopDownParser
{
	static Boolean checkQ = false;
	static ArrayList<quadTuple> tupleList = new ArrayList<quadTuple>();
	static ArrayList<Token> sourceb = new ArrayList<Token>();
	static String callCheck = "";
	static ArrayList<String> args = new ArrayList<String>();
	static String funcTrack = "";
	static ArrayList<String> params = new ArrayList<String>();
	public static ArrayList<Hashtable<String, IDinfo>> symTabList = new ArrayList<Hashtable<String, IDinfo>>();
	static String prevar = "";
	static String postvar = "";
	static String tokTracker = "";
	static quadTuple tempTuple = new quadTuple(null, null, null, null);
	public static Boolean parser(ArrayList<Token> source)
	{
		sourceb = new ArrayList<Token>(source);
		Hashtable<String, IDinfo> symTable = new Hashtable<String, IDinfo>();
		symTabList.add(symTable);
		source.add(new Token("$", "delimiter"));
		source = a(source);
		if(source.get(0).value == "$" && funcTrack.equals("main"))
		{
			source.remove(0);
			return true;
		}
		else
		{
			System.out.println("REJECT");
			System.exit(0);
			return false;
		}
	}
	
	public static ArrayList<Token> a(ArrayList<Token> source)
	{

		Token token = source.get(0);
			switch(token.value) 
			{
				case "int":
					source = b(source);
					return source;
				case "void":
					source = b(source);
					return source;
				case "float":
					source = b(source);
					return source;
				case "$":
					return source;
				default:
					System.out.println("REJECT");
					System.exit(0);
		}
		return source;
	}
	public static ArrayList<Token> b(ArrayList<Token> source)
	{
		Token token = source.get(0);
		switch(token.value) 
		{
			case "int":
				source = c(source);
				source = b_prime(source);
				return source;
			case "void":
				source = c(source);
				source = b_prime(source);
				return source;
			case "float":
				source = c(source);
				source = b_prime(source);
				return source;
			case "$":
				return source;
			default:
				System.out.println("REJECT");
				System.exit(0);
		}
		return source;
	}
	public static ArrayList<Token> b_prime(ArrayList<Token> source)
	{		

		Token token = source.get(0);

		switch(token.value) 
		{
			case "int":
				source = c(source);
				source = b_prime(source);
				return source;
			case "void":
				source = c(source);
				source = b_prime(source);
				return source;
			case "float":
				source = c(source);
				source = b_prime(source);
				return source;
			case "$":
				if(funcTrack.equals("main"))
				{
				System.out.println("ACCEPT");
				System.exit(0);
				}
				break;
			default:
				System.out.println("REJECT");
				System.exit(0);
		}
		return source;
	}
	public static ArrayList<Token> c(ArrayList<Token> source)
	{
		//System.out.println("c");
		Token token = source.get(0);
		switch(token.value) 
		{
			case "int":
				source = e(source);
				
				if(source.get(0).getTokenType() == "ID")
				{
					String id = source.get(0).value;
					for(int i = 0; i < symTabList.size(); i++)
					{
						if(symTabList.get(i).containsKey(id))
						{
						System.out.println("REJECT");
						System.exit(0);
						}
					}
					source.remove(0);

					if(source.get(0).value.equals("("))
					{
						symTabList.get(symTabList.size() - 1).put(id, new IDinfo("int", "function", new ArrayList<String>()));	
						funcTrack = id;
					}
					else if(source.get(0).value.equals("["))
					{
						symTabList.get(symTabList.size() - 1).put(id, new IDinfo("int", "array", new ArrayList<String>()));								
					}
					else
					{
						symTabList.get(symTabList.size() - 1).put(id, new IDinfo("int", null, new ArrayList<String>()));	
					}
				}
				else
				{
					System.out.println("REJECT");
					System.exit(0);
				}
				
				source = c_prime(source);
				return source;
			case "void":
				source = e(source);
				if(source.get(0).getTokenType() == "ID")
				{
					String id = source.get(0).value;
					source.remove(0);
					if(source.get(0).value.equals(";") || source.get(0).value.equals("["))
					{
					System.out.println("REJECT");
					System.exit(0);
					}
					for(int i = 0; i < symTabList.size(); i++)
					{
						if(symTabList.get(i).containsKey(id))
						{
						System.out.println("REJECT");
						System.exit(0);
						}
					}
					funcTrack = id;
					symTabList.get(symTabList.size() - 1).put(id, new IDinfo("void", "function", new ArrayList<String>()));

				}
				else
				{
					System.out.println("REJECT");
					System.exit(0);
				}
				source = c_prime(source);
				return source;
			case "float":
				source = e(source);
				String id = "";
				if(source.get(0).getTokenType() == "ID")
					{
					id = source.get(0).value;

					for(int i = 0; i < symTabList.size(); i++)
						{
						if(symTabList.get(i).containsKey(id))
							{
							System.out.println("REJECT");
							System.exit(0);
							}
						}
				source.remove(0);
						if(source.get(0).value.equals("("))
						{
							symTabList.get(symTabList.size() - 1).put(id, new IDinfo("float", "function", new ArrayList<String>()));	
						}
						else if(source.get(0).value.equals("["))
						{
							symTabList.get(symTabList.size() - 1).put(id, new IDinfo("float", "array", new ArrayList<String>()));								
						}
						else
						{
							symTabList.get(symTabList.size() - 1).put(id, new IDinfo("float", null, new ArrayList<String>()));	
						}
					}
				else
					{
					System.out.println("REJECT");
					System.exit(0);
					}
				source = c_prime(source);
				return source;
			default:
				System.out.println("REJECT");
				System.exit(0);
		}
		return source;
	}
	public static ArrayList<Token> c_prime(ArrayList<Token> source)
	{
		//System.out.println("c'");

		Token token = source.get(0);
		switch(token.value) 
		{
			case "(":
				params = new ArrayList<String>();
				source.remove(0);
				Hashtable<String, IDinfo> symTable = new Hashtable<String, IDinfo>();
				symTabList.add(symTable);
				source = g(source);
				switch(source.get(0).value)
				{
				case ")":
					for(int i = 0; i < symTabList.size(); i++)
					{
						if(symTabList.get(i).get(funcTrack) != null)
						{
							symTabList.get(i).get(funcTrack).paramTypes = params;
						}
					}
					source.remove(0);
					break;
				default:
					System.out.println("REJECT");
					System.exit(0);
				}
				source = j(source);
				return source;
			case ";":
				source.remove(0);
				return source;
			case "[":
				source.remove(0);
				if(source.get(0).getTokenType() == "int")
				{
					source.remove(0);
				}
				else 
				{
					System.out.println("REJECT");
					System.exit(0);
				}
				if(source.get(0).value.equals("]"))
 					source.remove(0);
				else 
				{
					System.out.println("REJECT");
					System.exit(0);
				}
				if(source.get(0).value.equals(";"))
					source.remove(0);
				else 
				{
					System.out.println("REJECT");
					System.exit(0);
				}
				return source;
			default:
				System.out.println("REJECT");
				System.exit(0);
		}
		return source;
	}
	public static ArrayList<Token> e(ArrayList<Token> source)
	{
		//System.out.println("e");
		Token token = source.get(0);
		switch(token.value) 
		{
			case "int":
				source.remove(0);
				return source;
			case "void":
				source.remove(0);
				return source;
			case "float":
				source.remove(0);
				return source;
			default:
				System.out.println("REJECT");
				System.exit(0);
		}
	return source;
	}
	public static ArrayList<Token> g(ArrayList<Token> source)
	{
	//	System.out.println("g");
		Token token = source.get(0);
		switch(token.value) 
		{
			case "int":
				source.remove(0);
				String id = source.get(0).value;
				if(id.equals(")"))
				{
					
					System.out.println("REJECT");
					System.exit(0);
				}
				else
				{
					for(int i = 0; i < symTabList.size(); i++)
					{
						if(symTabList.get(i).containsKey(id))
						{
						System.out.println("REJECT");
						System.exit(0);
						}
					}
					if(source.get(1).value.equals("["))
					{
						symTabList.get(symTabList.size() - 1).put(id, new IDinfo("int", "array", new ArrayList<String>()));
						params.add("intarray");
						for(int i = 0; i < symTabList.size(); i++)
						{
							if(symTabList.get(i).containsKey(funcTrack))
							{
								symTabList.get(i).get(funcTrack).paramTypes = params;
							}
						}


					}
					symTabList.get(symTabList.size() - 1).put(id, new IDinfo("int", null, new ArrayList<String>()));	
					params.add("intnull");		
					for(int i = 0; i < symTabList.size(); i++)
					{
						if(symTabList.get(i).containsKey(funcTrack))
						{
							symTabList.get(i).get(funcTrack).paramTypes = params;
						}
					}

				}
				source = g_prime(source);
				return source;
			case "void":
				source.remove(0);
				if(!source.get(0).value.equals(")"))
				{
					System.out.println("REJECT");
					System.exit(0);
				}
				source = g_prime(source);
				return source;
			case "float":
				source.remove(0);
				id = source.get(0).value;
				

				if(id.equals(")"))
				{
					System.out.println("REJECT");
					System.exit(0);
				}
				else
				{
					for(int i = 0; i < symTabList.size(); i++)
					{
						if(symTabList.get(i).containsKey(id))
						{
						System.out.println("REJECT");
						System.exit(0);
						}
					}
					if(source.get(1).value.equals("["))
					{
						symTabList.get(symTabList.size() - 1).put(id, new IDinfo("float", "array", new ArrayList<String>()));	
						params.add("floatarray");
						for(int i = 0; i < symTabList.size(); i++)
						{
							if(symTabList.get(i).containsKey(funcTrack))
							{
								symTabList.get(i).get(funcTrack).paramTypes = params;
							}
						}

					}
					symTabList.get(symTabList.size() - 1).put(id, new IDinfo("float", null, new ArrayList<String>()));	
					params.add("floatnull");
					for(int i = 0; i < symTabList.size(); i++)
					{
						if(symTabList.get(i).containsKey(funcTrack))
						{
							symTabList.get(i).get(funcTrack).paramTypes = params;
						}
					}

				}

				source = g_prime(source);
				return source;
			default:
				System.out.println("REJECT");
				System.exit(0);
		}
	return source;
	}
	public static ArrayList<Token> g_prime(ArrayList<Token> source)
	{
	//	System.out.println("g'");
		Token token = source.get(0);
		String temp = token.value;
		if(token.getTokenType() == "float" 
				   || token.getTokenType() == "int")
					temp = "NUM";
				if(token.getTokenType() == "ID")
						temp = "ID";
		switch(temp) 
		{
		case "ID":
				source.remove(0);
				source = i_prime(source);
				source = h_prime(source);
				return source;
			case ")":
				return source;
			default:
				System.out.println("REJECT");
				System.exit(0);
		}
	return source;
	}
	public static ArrayList<Token> h_prime(ArrayList<Token> source)
	{
	//	System.out.println("h'");
		Token token = source.get(0);
		switch(token.value) 
		{
			case ",":
				tokTracker = "";

				source.remove(0);
				source = i(source);
				source = h_prime(source);
				return source;
			case ")":
				return source;
			default:
				System.out.println("REJECT");
				System.exit(0);
		}
	return source;
	}
	public static ArrayList<Token> i(ArrayList<Token> source)
	{
		int i = 0;
	//	System.out.println("i");
		Token token = source.get(0);
		switch(token.value) 
		{
			case "int":
				source = e(source);
				if(source.get(0).getTokenType() == "ID")
				{
					String id = source.get(0).value;
					for(i = 0; i < symTabList.size(); i++)
					{
						if(symTabList.get(i).containsKey(id))
						{
						System.out.println("REJECT");
						System.exit(0);
						}
					}
					source.remove(0);
					if(source.get(0).value.equals("["))
					{
						symTabList.get(symTabList.size() - 1).put(id, new IDinfo("int", "array", new ArrayList<String>()));
						params.add("intarray");
						
						for( i = 0; i < symTabList.size(); i++)
						{
							if(symTabList.get(i).containsKey(funcTrack))
							{
								symTabList.get(i).get(funcTrack).paramTypes = params;
							}
						}
					}
					else
					{
						

						symTabList.get(symTabList.size() - 1).put(id, new IDinfo("int", null, new ArrayList<String>()));	
						params.add("intnull");
						for( i = 0; i < symTabList.size(); i++)
						{
							if(symTabList.get(i).containsKey(funcTrack))
							{
								symTabList.get(i).get(funcTrack).paramTypes = params;
							}
						}
					}
				}
				else{
					System.out.println("REJECT");
					System.exit(0);
				}
				
				source = i_prime(source);
				return source;
			case "void":
				source = e(source);
				if(source.get(0).getTokenType() == "ID")
				{
					System.out.println("REJECT");
					System.exit(0);
				}
				source = i_prime(source);
				return source;
			case "float":
				source = e(source);
				if(source.get(0).getTokenType() == "ID")
				{
					String id = source.get(0).value;

					for(i = 0; i < symTabList.size(); i++)
					{
						if(symTabList.get(i).containsKey(id))
						{
						System.out.println("REJECT");
						System.exit(0);
						}
					}
					source.remove(0);
					if(source.get(0).value.equals("["))
					{
						symTabList.get(symTabList.size() - 1).put(id, new IDinfo("float", "array", new ArrayList<String>()));
						params.add("floatarray");
						for( i = 0; i < symTabList.size(); i++)
						{
							if(symTabList.get(i).containsKey(funcTrack))
							{
								symTabList.get(i).get(funcTrack).paramTypes = params;
							}
						}
					}
					else
					{
						symTabList.get(symTabList.size() - 1).put(id, new IDinfo("float", null, new ArrayList<String>()));	
						params.add("floatnull");
						for( i = 0; i < symTabList.size(); i++)
						{
							if(symTabList.get(i).containsKey(funcTrack))
							{
								symTabList.get(i).get(funcTrack).paramTypes = params;
							}
						}
					}
				}
				else{
					System.out.println("REJECT");
					System.exit(0);
				}
				
				source = i_prime(source);
				return source;

			default:
				System.out.println("REJECT");
				System.exit(0);
		}
		return source;
	}
	
	public static ArrayList<Token> i_prime(ArrayList<Token> source)
	{
//		System.out.println("i'");
		Token token = source.get(0);
		switch(token.value) 
		{
			case "[":
				source.remove(0);
				if(source.get(0).value == "]")
					source.remove(0);
				else
				{
					System.out.println("REJECT");
					System.exit(0);
				}
				return source;
			case ",":
				tokTracker = "";

				return source;
			case ")":
				return source;
			default:
				System.out.println("REJECT");
				System.exit(0);
		}
	return source;
	}
	
	//////Started implementing empties in i_prime with the comma
	public static ArrayList<Token> j(ArrayList<Token> source)
	{
		

		Token token = source.get(0);
		switch(token.value) 
		{
			case "{":
				source.remove(0);
				source = k(source);
				source = l(source);

				if(source.get(0).value.equals("}"))
				{
						if(symTabList.get(symTabList.size() - 2).containsKey(funcTrack))
						{
							if(symTabList.get(symTabList.size() - 2).get(funcTrack).type != "void")
							{
								if(checkQ == true)
								{
									checkQ = false;
								}
								else
								{
									System.out.println("REJECT");
									System.exit(0);
								}
							}
						}
					//remove previous scope when exiting
					symTabList.remove(symTabList.size() - 1);
					source.remove(0);
				}
					else
				{
				System.out.println("REJECT");
				System.exit(0);	
				}
				break;
			default:
				System.out.println("REJECT");
				System.exit(0);
		}
	return source;
	}
	public static ArrayList<Token> k(ArrayList<Token> source)
	{
//		System.out.println("k");
		Token token = source.get(0);
		String temp = token.value;
		if(token.getTokenType() == "float" 
				   || token.getTokenType() == "int")
					temp = "NUM";
				if(token.getTokenType() == "ID")
						temp = "ID";
		switch(temp) 
		{
		case "int":
				source = k_prime(source);	
				return source;
			case "void":
				source = k_prime(source);
				return source;
			case "float":
				source = k_prime(source);
				return source;
			case "if":
				return source;
			case "while":
				return source;
			case "return":
				return source;
			case "+":
				return source;
			case "-":
				return source;
			case "*":
				return source;
			case "/":
				return source;
			case "<":
				return source;
			case ">":
				return source;
			case "<=":
				return source;
			case ">=":
				return source;
			case "==":
				return source;
			case "!=":
				return source;
			case "(":
				return source;
			case "ID":
				return source;
			case "NUM":
				return source;
			case "}":
				return source;
			default:
				System.out.println("REJECT");
				System.exit(0);
		}
	return source;		
	}
	public static ArrayList<Token> k_prime(ArrayList<Token> source)
	{
//		System.out.println("k'");
		Token token = source.get(0);
		String temp = token.value;
		if(token.getTokenType() == "float" 
				   || token.getTokenType() == "int")
					temp = "NUM";
				if(token.getTokenType() == "ID")
						temp = "ID";
		switch(temp) 
		{
		case "int":
				source = c(source);
				source = k_prime(source);	
				return source;
			case "void":
				source = c(source);
				source = k_prime(source);
				return source;
			case "float":
				source = c(source);
				source = k_prime(source);
				return source;
			case "follow":
				return source;
			case "if":
				return source;
			case "while":
				return source;
			case "return":
				return source;
			case "+":
				return source;
			case "-":
				return source;
			case "*":
				return source;
			case "/":
				return source;
			case "<":
				return source;
			case ">":
				return source;
			case "<=":
				return source;
			case ">=":
				return source;
			case "==":
				return source;
			case "!=":
				return source;
			case "(":
				return source;
			case "ID":
				return source;
			case "NUM":
				return source;
			case "}":
				return source;
			default:
				System.out.println("REJECT");
				System.exit(0);
		}
	return source;		
	}
	public static ArrayList<Token> l(ArrayList<Token> source)
	{
	//	System.out.println("l");
		Token token = source.get(0);
		String temp = token.value;
		if(token.getTokenType() == "float" 
				   || token.getTokenType() == "int")
					temp = "NUM";
				if(token.getTokenType() == "ID")
						temp = "ID";
		switch(temp) 
		{
		case "if":
				source = l_prime(source);
				return source;
			case "while":
				source = l_prime(source);
				return source;
			case "return":
				source = l_prime(source);
				return source;
			case "+":
				source = l_prime(source);
				return source;
			case "-":
				source = l_prime(source);
				return source;
			case "*":
				source = l_prime(source);
				return source;
			case "/":
				source = l_prime(source);
				return source;
			case "<":
				source = l_prime(source);
				return source;
			case ">":
				source = l_prime(source);
				return source;
			case "<=":
				source = l_prime(source);
				return source;
			case ">=":
				source = l_prime(source);
				return source;
			case "==":
				source = l_prime(source);
				return source;
			case "!=":
				source = l_prime(source);
				return source;
			case "(":
				source = l_prime(source);
				return source;
			case "ID":
				source = l_prime(source);
				return source;
			case "}":
				return source;
			default:
				System.out.println("REJECT");
				System.exit(0);
		}
	return source;		
	}
	public static ArrayList<Token> l_prime(ArrayList<Token> source)
	{
	//	System.out.println("l'");
		Token token = source.get(0);
		String temp = token.value;
		if(token.getTokenType() == "float" 
				   || token.getTokenType() == "int")
					temp = "NUM";
				if(token.getTokenType() == "ID")
						temp = "ID";
		switch(temp) 
		{
		case "if":
				source = m(source);
				source = l_prime(source);
				return source;
			case "while":
				source = m(source);
				source = l_prime(source);
				return source;
			case "return":
				source = m(source);
				source = l_prime(source);
				return source;
			case "+":
				source = m(source);
				source = l_prime(source);
				return source;
			case "-":
				source = m(source);
				source = l_prime(source);
				return source;
			case "*":
				source = m(source);
				source = l_prime(source);
				return source;
			case "/":
				source = m(source);
				source = l_prime(source);
				return source;
			case "<":
				source = m(source);
				source = l_prime(source);
				return source;
			case ">":
				source = m(source);
				source = l_prime(source);
				return source;
			case "<=":
				source = m(source);
				source = l_prime(source);
				return source;
			case ">=":
				source = m(source);
				source = l_prime(source);
				return source;
			case "==":
				source = m(source);
				source = l_prime(source);
				return source;
			case "!=":
				source = m(source);
				source = l_prime(source);
				return source;
			case "(":
				source = m(source);
				source = l_prime(source);
				return source;
			case "ID":
				source = m(source);
				source = l_prime(source);
				return source;
			case "}":
				return source;
			default:
					System.out.println("REJECT");
					System.exit(0);
			}
		return source;		
		}
	
	public static ArrayList<Token> m(ArrayList<Token> source)
	{
//		System.out.println("m");
		Token token = source.get(0);
		String temp = token.value;
		if(token.getTokenType() == "float" 
				   || token.getTokenType() == "int")
					temp = "NUM";
				if(token.getTokenType() == "ID")
						temp = "ID";
		switch(temp) 
		{
		case "if":
				source = o(source);
				return source;
			case "while":
				source = p(source);
				return source;
			case "return":
				source = q(source);
				return source;
			case "+":
				source = n(source);
				return source;
			case "-":
				source = n(source);
				return source;
			case "*":
				source = n(source);
				return source;
			case "/":
				source = n(source);
				return source;
			case "<":
				source = n(source);
				return source;
			case "<=":
				source = n(source);
				return source;
			case ">":
				source = n(source);
				return source;
			case ">=":
				source = n(source);
				return source;
			case "==":
				source = n(source);
				return source;
			case "!=":
				source = n(source);
				return source;
			case "(":
				source = n(source);
				return source;
			case "{":
				source = j(source);
				return source;
			case "}":
				source = n(source);
				return source;
			case "NUM":
				source = n(source);
				return source;
			case "ID":
				source = n(source);
				return source;
			default:
				System.out.println("REJECT");
				System.exit(0);
		}
	return source;		
	}
	public static ArrayList<Token> n(ArrayList<Token> source)
	{
//		System.out.println("n");
		Token token = source.get(0);
		String temp = token.value;
		if(token.getTokenType() == "float" 
				   || token.getTokenType() == "int")
					temp = "NUM";
				if(token.getTokenType() == "ID")
						temp = "ID";
		switch(temp) 
		{
		case "+":
			source = r(source);
			if(source.get(0).value == ";")
			{
				source.remove(0);
			}
			else
			{
				System.out.println("REJECT");
				System.exit(0);
			}
			return source;
		case "-":
			source = r(source);
			if(source.get(0).value == ";")
			{
				source.remove(0);
			}
			else
			{
				System.out.println("REJECT");
				System.exit(0);
			}
			return source;
		case "*":
			source = r(source);
			if(source.get(0).value == ";")
			{
				source.remove(0);
			}
			else
			{
				System.out.println("REJECT");
				System.exit(0);
			}
			return source;
		case "/":
			source = r(source);
			if(source.get(0).value == ";")
			{
				source.remove(0);
			}
			else
			{
				System.out.println("REJECT");
				System.exit(0);
			}
			return source;
		case "<":
			source = r(source);
			if(source.get(0).value == ";")
			{
				source.remove(0);
			}
			else
			{
				System.out.println("REJECT");
				System.exit(0);
			}
			return source;
		case "<=":
			source = r(source);
			if(source.get(0).value == ";")
			{
				source.remove(0);
			}
			else
			{
				System.out.println("REJECT");
				System.exit(0);
			}
			return source;
		case ">":
			source = r(source);
			if(source.get(0).value == ";")
			{
				source.remove(0);
			}
			else
			{
				System.out.println("REJECT");
				System.exit(0);
			}
			return source;
		case ">=":
			source = r(source);
			if(source.get(0).value == ";")
			{
				source.remove(0);
			}
			else
			{
				System.out.println("REJECT");
				System.exit(0);
			}
			return source;
		case "==":
			source = r(source);
			if(source.get(0).value == ";")
			{
				source.remove(0);
			}
			else
			{
				System.out.println("REJECT");
				System.exit(0);
			}
			return source;
		case "!=":
			source = r(source);
			if(source.get(0).value == ";")
			{
				source.remove(0);
			}
			else
			{
				System.out.println("REJECT");
				System.exit(0);
			}
			return source;
		case "(":
			source = r(source);
			if(source.get(0).value == ")")
			{
				source.remove(0);
			}
			else
			{
				System.out.println("REJECT");
				System.exit(0);
			}
			return source;
		case "NUM":
			source = r(source);
			if(source.get(0).value == ";")
			{
				source.remove(0);
			}
			else
			{
				System.out.println("REJECT");
				System.exit(0);
			}
			return source;
		case "ID":
			source = r(source);
			if(source.get(0).value.equals(";"))
			{
				source.remove(0);
			}
			else
			{
				System.out.println("REJECT");
				System.exit(0);
			}
			return source;
		case ";":
				source.remove(0);
				return source;
			default:
				System.out.println("REJECT");
				System.exit(0);
		}
	return source;		
	}
	public static ArrayList<Token> o(ArrayList<Token> source)
	{
//		System.out.println("o");
		Token token = source.get(0);
		switch(token.value)
		{
		case "if":
			source.remove(0);
			if(source.get(0).value.equals("("))
				source.remove(0);
			else
			{
				System.out.println("REJECT");
				System.exit(0);
			}
			source = r(source);
			if(source.get(0).value.equals(")"))
				source.remove(0);
			else
			{
				System.out.println("REJECT");
				System.exit(0);
			}
			if(source.get(0).value.equals("{")) 
			{
				Hashtable<String, IDinfo> symTable = new Hashtable<String, IDinfo>();
				symTabList.add(symTable);
			}
			source = m(source);
			source = o_prime(source);
			return source;
		default:
			System.out.println("REJECT");
			System.exit(0);
		}
		return source;
	}
	public static ArrayList<Token> o_prime(ArrayList<Token> source)
	{
	//	System.out.println("o'");
		Token token = source.get(0);
		String temp = token.value;
		if(token.getTokenType() == "float" 
				   || token.getTokenType() == "int")
					temp = "NUM";
				if(token.getTokenType() == "ID")
						temp = "ID";
		switch(temp) 
		{
		case "else":
				source.remove(0);
				source = m(source);		
				return source;
			case "if":
				return source;
			case "while":
				return source;
			case "return":
				return source;
			case "+":
				return source;
			case "-":
				return source;
			case "*":
				return source;
			case "/":
				return source;
			case "<":
				return source;
			case ">":
				return source;
			case "<=":
				return source;
			case ">=":
				return source;
			case "==":
				return source;
			case "!=":
				return source;
			case "(":
				return source;
			case "}":
				return source;
			case "ID":
				return source;
			case "NUM":
				return source;
			default:
				System.out.println("REJECT");
				System.exit(0);
		}
	return source;		
	}
	public static ArrayList<Token> p(ArrayList<Token> source)
	{
//		System.out.println("p");
		Token token = source.get(0);
		switch(token.value)
		{
		case "while":
			source.remove(0);
			if(source.get(0).value.equals("("))
			{
				source.remove(0);
			}
			else
			{
				System.out.println("REJECT");
				System.exit(0);				
			}
			source = r(source);
			if(source.get(0).value.equals(")"))
			{
				source.remove(0);
			}
			else
			{
				System.out.println("REJECT");
				System.exit(0);				
			}
			if(source.get(0).value.equals("{"))
			{
				Hashtable<String, IDinfo> symTable = new Hashtable<String, IDinfo>();
				symTabList.add(symTable);
			}
			source = m(source);
			return source;
		default:
			System.out.println("REJECT");
			System.exit(0);

		}
		
		return source;
	}
	public static ArrayList<Token> q(ArrayList<Token> source)
	{
		checkQ = true;
//		System.out.println("q");
		Token token = source.get(0);
		switch(token.value)
		{
		case "return":
			source.remove(0);
			source = q_prime(source);
			return source;
		default:
			System.out.println("REJECT");
			System.exit(0);
		}
		return source;
	}
	
	public static ArrayList<Token> q_prime(ArrayList<Token> source)
	{		
//		System.out.println("q'");
		Token token = source.get(0);
		String temp = token.value;
		if(token.getTokenType() == "float" 
				   || token.getTokenType() == "int")
					temp = "NUM";
				if(token.getTokenType() == "ID")
						temp = "ID";
		switch(temp) 
		{			case "+":		
				source = r(source);
				if(source.get(0).value == ";")
				{
					source.remove(0);
				}
				else
				{
					System.out.println("REJECT");
					System.exit(0);
				}
				return source;
			case "-":		
				source = r(source);
				if(source.get(0).value == ";")
				{
					source.remove(0);
				}
				else
				{
					System.out.println("REJECT");
					System.exit(0);
				}
				return source;
			case "*":		
				source = r(source);
				if(source.get(0).value == ";")
				{
					source.remove(0);
				}
				else
				{
					System.out.println("REJECT");
					System.exit(0);
				}
				return source;
			case "/":		
				source = r(source);
				if(source.get(0).value == ";")
				{
					source.remove(0);
				}
				else
				{
					System.out.println("REJECT");
					System.exit(0);
				}
				return source;
			case ">":		
				source = r(source);
				if(source.get(0).value == ";")
				{
					source.remove(0);
				}
				else
				{
					System.out.println("REJECT");
					System.exit(0);
				}
				return source;
			case "<":		
				source = r(source);
				if(source.get(0).value == ";")
				{
					source.remove(0);
				}
				else
				{
					System.out.println("REJECT");
					System.exit(0);
				}
				return source;
			case ">=":		
				source = r(source);
				if(source.get(0).value == ";")
				{
					source.remove(0);
				}
				else
				{
					System.out.println("REJECT");
					System.exit(0);
				}
				return source;
			case "<=":		
				source = r(source);
				if(source.get(0).value == ";")
				{
					source.remove(0);
				}
				else
				{
					System.out.println("REJECT");
					System.exit(0);
				}
				return source;
			case "!=":		
				source = r(source);
				if(source.get(0).value == ";")
				{
					source.remove(0);
				}
				else
				{
					System.out.println("REJECT");
					System.exit(0);
				}
				return source;
			case "==":		
				source = r(source);
				if(source.get(0).value == ";")
				{
					source.remove(0);
				}
				else
				{
					System.out.println("REJECT");
					System.exit(0);
				}
				return source;
			case "(":		
				source = r(source);			
				if(source.get(0).value.equals(";"))
				{
					source.remove(0);
				}
				else
				{
					System.out.println("REJECT");
					System.exit(0);
				}
				return source;
			case "ID":
				if(symTabList.get(symTabList.size() - 1).get(source.get(0).value).subtype == "array"
				||symTabList.get(symTabList.size() - 1).get(source.get(0).value).subtype == "function")
				{
					System.out.println("REJECT");
					System.exit(0);
				}
				String functype = "";
				for(int i = 0; i < symTabList.size(); i++)
				{
					if(symTabList.get(i).get(funcTrack) != null)
					{
						functype = symTabList.get(i).get(funcTrack).type;
					}
				}
				String idtype = "";
				for(int i = 0; i < symTabList.size(); i++)
				{
					if(symTabList.get(i).get(source.get(0).value) != null)
					{
						idtype = symTabList.get(i).get(source.get(0).value).type;
					}
				}
				if(idtype != functype || functype.equals("void"))
				{
					System.out.println("REJECT");
					System.exit(0);
				}

				source = r(source);
				if(source.get(0).value.equals(";"))
				{
					source.remove(0);
				}
				else
				{
					System.out.println("REJECT");
					System.exit(0);
				}
				return source;
			case "NUM":		
				functype = "";
				for(int i = 0; i < symTabList.size(); i++)
				{
					if(symTabList.get(i).get(funcTrack) != null)
					{
						functype = symTabList.get(i).get(funcTrack).type;
					}
				}
				if(functype.equals("void"))
				{
					System.out.println("REJECT");
					System.exit(0);
				}

				source = r(source);
				if(source.get(0).value.equals(";"))
				{
					source.remove(0);
				}
				else
				{
					System.out.println("REJECT");
					System.exit(0);
				}
				return source;
			case ";":
				functype = "";
				for(int i = 0; i < symTabList.size(); i++)
				{
					if(symTabList.get(i).get(funcTrack) != null)
					{
						functype = symTabList.get(i).get(funcTrack).type;
					}
				}
				if(functype == "float" || functype == "int")
				{
					System.out.println("REJECT");
					System.exit(0);
				}
				source.remove(0);
				return source;
			default:
				System.out.println("REJECT");
				System.exit(0);
		}
	return source;		
	}
	public static ArrayList<Token> r(ArrayList<Token> source)
	{
//		System.out.println("r");
		Token token = source.get(0);
		String temp = token.value;
		if(token.getTokenType() == "float" 
				   || token.getTokenType() == "int")
					temp = "NUM";
				if(token.getTokenType() == "ID")
						temp = "ID";
		switch(temp) 
		{
		case "(":
			args = new ArrayList<String>();
			source.remove(0);
			source = r(source);
			if(source.get(0).value.equals(")"))
			{				
				for(int i = 0; i < symTabList.size(); i++)
				{
					
					if(symTabList.get(i).get(callCheck) != null)
					{
					if(symTabList.get(i).get(callCheck).paramTypes.size() == args.size())
						{	
							if(args.size() >0)
							{
								for(int ii = 0; ii< args.size(); ii++)
								{
									if(args.get(ii).equals(symTabList.get(i).get(funcTrack).paramTypes.get(ii)))
									{
									}
									else
									{
										System.out.println("REJECT");
										System.exit(0);
									}									
								}	
							}
						}
							else
							{
								System.out.println("REJECT");
								System.exit(0);
							}
						}
					}

				source.remove(0);
			}
			else
			{
				System.out.println("REJECT");
				System.exit(0);									
			}			
			source = x_prime(source);
			source = v_prime(source);
			source = t_prime(source);
			return source;
		case "NUM":
			if(tokTracker == "")
			{
				tokTracker = source.get(0).tokenType;
			}
			else if(tokTracker != source.get(0).tokenType)
			{
				System.out.println(tokTracker + " : " + source.get(0).tokenType);
				System.out.println("REJECT");
				System.exit(0);
			}
			args.add(token.tokenType+"null");
			source.remove(0);
			source = x_prime(source);
			source = v_prime(source);
			source = t_prime(source);
			return source;
		case "ID":
			String id = source.get(0).value;
			Boolean idCheck = false;
			String tokentype = "";
			for(int i = 0; i < symTabList.size(); i++)
			{
				if(symTabList.get(i).containsKey(id))
				{
					idCheck = true;
					tokentype = symTabList.get(i).get(id).type + symTabList.get(i).get(id).subtype; 
					if(tokTracker == "")
					{
						tokTracker = symTabList.get(i).get(id).type;
					}
					else if(tokTracker != symTabList.get(i).get(id).type)
					{
						System.out.println(tokTracker + " : " + id + " : " + source.get(1).value);
						System.out.println(tokTracker + " : " + symTabList.get(i).get(id).type);
						System.out.println("REJECT");
						System.exit(0);
					}
				}
			}
			args.add(tokentype);
			if(!idCheck)
			{
				System.out.println("REJECT");
				System.exit(0);
			}

			source.remove(0);
			r_prime(source);
			return source;
		case "+":
			source = x_prime(source);
			source = v_prime(source);
			source = t_prime(source);
			return source;
		case "-":
			source = x_prime(source);
			source = v_prime(source);
			source = t_prime(source);
			return source;
		case "*":
			source = x_prime(source);
			source = v_prime(source);
			source = t_prime(source);
			return source;
		case "/":
			source = x_prime(source);
			source = v_prime(source);
			source = t_prime(source);
			return source;
		case "<":
			source = x_prime(source);
			source = v_prime(source);
			source = t_prime(source);
			return source;
		case ">":
			source = x_prime(source);
			source = v_prime(source);
			source = t_prime(source);
			return source;
		case "<=":
			source = x_prime(source);
			source = v_prime(source);
			source = t_prime(source);
			return source;
		case ">=":
			source = x_prime(source);
			source = v_prime(source);
			source = t_prime(source);
			return source;
		case "==":
			source = x_prime(source);
			source = v_prime(source);
			source = t_prime(source);
			return source;
		case "!=":
			source = x_prime(source);
			source = v_prime(source);
			source = t_prime(source);
			return source;
		case ";":
			tokTracker = "";
			return source;
		case ",":
			tokTracker = "";
			source = delta_prime(source);
			return source;
		
		case ")":
			tokTracker = "";
			return source;
		case "]":
			tokTracker = "";
			return source;
		default:
			System.out.println("REJECT");
			System.exit(0);				
		}
		return source;
	}
	//THE ISSUE LIES WITHIN R
	public static ArrayList<Token> r_prime(ArrayList<Token> source)
	{
//		System.out.println("r'");
		Token token = source.get(0);
		switch(token.value)
		{
		case "+":
			source = x_prime(source);
			source = v_prime(source);
			source = t_prime(source);
			return source;
		case "-":
			source = x_prime(source);
			source = v_prime(source);
			source = t_prime(source);
			return source;
		case "*":
			source = x_prime(source);
			source = v_prime(source);
			source = t_prime(source);
			return source;
		case "/":
			source = x_prime(source);
			source = v_prime(source);
			source = t_prime(source);
			return source;
		case "<":
			source = x_prime(source);
			source = v_prime(source);
			source = t_prime(source);
			return source;
		case ">":
			source = x_prime(source);
			source = v_prime(source);
			source = t_prime(source);
			return source;
		case "<=":
			source = x_prime(source);
			source = v_prime(source);
			source = t_prime(source);
			return source;
		case ">=":
			source = x_prime(source);
			source = v_prime(source);
			source = t_prime(source);
			return source;
		case "==":
			source = x_prime(source);
			source = v_prime(source);
			source = t_prime(source);
			return source;
		case "!=":
			source = x_prime(source);
			source = v_prime(source);
			source = t_prime(source);
			return source;
		case "=":
			source.remove(0);
			source = r(source);
			return source;
		case "(":
			args = new ArrayList<String>();
			source.remove(0);
			source = r(source);		
			int i = 0;
			int ii = 0;
			if(source.get(0).value.equals(")"))
			{		
				
				for( i = 0; i < symTabList.size(); i++)
				{
					
					if(symTabList.get(i).get(callCheck) != null)
					{
						if(symTabList.get(i).get(callCheck).paramTypes.size() == args.size())
						{	
							if(args.size() >0)
							{
								for(ii = 0; ii< args.size(); ii++)
								{
									if(args.get(ii).equals(symTabList.get(i).get(funcTrack).paramTypes.get(ii)))
									{
									}
									else
									{
										System.out.println("REJECT");
										System.exit(0);
									}									
								}	
							}
						}
							else
							{
								System.out.println("REJECT");
								System.exit(0);
							}
						}
					}
				source.remove(0);
			}
			else
			{
				System.out.println("REJECT");
				System.exit(0);									
			}			
			source = x_prime(source);
			source = v_prime(source);
			source = t_prime(source);
			return source;
		case "[":
			source.remove(0);
			source = r(source);
			
			if(source.get(0).value.equals("]"))
				source.remove(0);
			else
			{
				System.out.println("REJECT");
				System.exit(0);						
			}
			source = r_prime_prime(source);
			return source;
		case ";":
			tokTracker = "";

			return source;
		case ",":
			tokTracker = "";
			source = delta_prime(source);
			return source;
		case ")":
			return source;

		case "]":
			return source;
			
		default:
			System.out.println("REJECT");
			System.exit(0);				
		}
		return source;
	}
	public static ArrayList<Token> r_prime_prime(ArrayList<Token> source)
	{
	//	System.out.println("r''");
		Token token = source.get(0);
		switch(token.value)
		{
		case "+":
			source = x_prime(source);
			source = v_prime(source);
			source = t_prime(source);
			return source;
		case "-":
			source = x_prime(source);
			source = v_prime(source);
			source = t_prime(source);
			return source;
		case "*":
			source = x_prime(source);
			source = v_prime(source);
			source = t_prime(source);
			return source;
		case "/":
			source = x_prime(source);
			source = v_prime(source);
			source = t_prime(source);
			return source;
		case "<":
			source = x_prime(source);
			source = v_prime(source);
			source = t_prime(source);
			return source;
		case ">":
			source = x_prime(source);
			source = v_prime(source);
			source = t_prime(source);
			return source;
		case "<=":
			source = x_prime(source);
			source = v_prime(source);
			source = t_prime(source);
			return source;
		case ">=":
			source = x_prime(source);
			source = v_prime(source);
			source = t_prime(source);
			return source;
		case "==":
			source = x_prime(source);
			source = v_prime(source);
			source = t_prime(source);
			return source;
		case "!=":
			source = x_prime(source);
			source = v_prime(source);
			source = t_prime(source);
			return source;
		case "=":
			source.remove(0);
			source = r(source);
			return source;
		case ";":
			tokTracker = "";

			return source;
		case ",":
			tokTracker = "";
			return source;
		case ")":
			return source;
		case "]":
			return source;
			
		default:
			System.out.println("REJECT");
			System.exit(0);				
		}
		return source;
	}
	public static ArrayList<Token> s_prime(ArrayList<Token> source)
	{
	//	System.out.println("s'");
		Token token = source.get(0);
		switch(token.value)
		{
		case "[":
			source.remove(0);
			source = r(source);
			if(source.get(0).value.equals("]"))
			{
				source.remove(0);
			}
			else
			{
				System.out.println("REJECT");
				System.exit(0);						
			}
			return source;
		case ";":
			tokTracker = "";
			return source;
		case ",":
			tokTracker = "";
			return source;
		case "(":
			return source;
		case "]":
			tokTracker = "";
			return source;
		default:
			System.out.println("REJECT");
			System.exit(0);				
		}
		return source;
	}
	public static ArrayList<Token> t_prime(ArrayList<Token> source)
	{
		//System.out.println("t'");
		Token token = source.get(0);
		switch(token.value)
		{
		case "<":
			source = u(source);
			source = v(source);
			return source;
		case "<=":
			source = u(source);
			source = v(source);
			return source;
		case ">=":
			source = u(source);
			source = v(source);
			return source;
		case ">":
			source = u(source);
			source = v(source);
			return source;
		case "==":
			source = u(source);
			source = v(source);
			return source;
		case "!=":
			source = u(source);
			source = v(source);
			return source;
		case ";":
			tokTracker = "";

			return source;
		case ",":			
			tokTracker = "";

			return source;
		case ")":
			return source;
		case "]":
			return source;
		default:
			System.out.println("REJECT");
			System.exit(0);				
		}
		return source;
	}
	public static ArrayList<Token> u(ArrayList<Token> source)
	{
	//	System.out.println("u");
		Token token = source.get(0);
		switch(token.value)
		{
		case "<":
			source.remove(0);
			return source;
		case ">":
			source.remove(0);
			return source;
		case "<=":
			source.remove(0);
			return source;
		case ">=":
			source.remove(0);
			return source;
		case "==":
			source.remove(0);
			return source;
		case "!=":
			source.remove(0);
			return source;
		default:
			System.out.println("REJECT");
			System.exit(0);					
		}
		return source;
	}
	public static ArrayList<Token> v(ArrayList<Token> source)
	{
//		System.out.println("v");
		Token token = source.get(0);
		String temp = token.value;
		if(token.getTokenType() == "float" 
				   || token.getTokenType() == "int")
					temp = "NUM";
				if(token.getTokenType() == "ID")
						temp = "ID";
		switch(temp) 
		{
		case "(":
			source = x(source);
			source = v_prime(source);
			return source;
		case "ID":
			source = x(source);
			source = v_prime(source);
			return source;
		case "NUM":
			source = x(source);
			source = v_prime(source);
			return source;
		default:

			System.out.println("REJECT");
			System.exit(0);					
		}
		return source;
	}
	public static ArrayList<Token> v_prime(ArrayList<Token> source)
	{
//		System.out.println("v'");
		Token token = source.get(0);
		switch(token.value)
		{
		case "+":
			source = w(source);
			source = x(source);
			source = v_prime(source);
			return source;
		case "-":
			source = w(source);
			source = x(source);
			source = v_prime(source);
			return source;
		case "<":
			return source;
		case ">":
			return source;
		case "<=":
			return source;
		case ">=":
			return source;
		case "==":
			return source;
		case "!=":
			return source;
		case ";":
			tokTracker = "";

			return source;
		case ",":
			tokTracker = "";

			return source;
		case ")":
			return source;
		case "]":
			return source;
		default:
			System.out.println("REJECT");
			System.exit(0);					
		}
		return source;
	}
	public static ArrayList<Token> w(ArrayList<Token> source)
	{
//		System.out.println("w");
		Token token = source.get(0);
		switch(token.value)
		{
		case "+":
			source.remove(0);
			return source;
		case "-":
			source.remove(0);
			return source;
		default:
			System.out.println("REJECT");
			System.exit(0);					
		}
		return source;
	}
	public static ArrayList<Token> x(ArrayList<Token> source)
	{
//		System.out.println("x");
		Token token = source.get(0);
		String temp = token.value;
		if(token.getTokenType() == "float" 
				   || token.getTokenType() == "int")
					temp = "NUM";
				if(token.getTokenType() == "ID")
						temp = "ID";
		switch(temp) 
		{
		case "(":
			source = z(source);
			source = x_prime(source);
			return source;
		case "ID":
			source = z(source);
			source = x_prime(source);
			return source;
		case "NUM":
			source = z(source);
			source = x_prime(source);
			return source;
		default:
			System.out.println("REJECT");
			System.exit(0);					
		}
		return source;
	}
	public static ArrayList<Token> x_prime(ArrayList<Token> source)
	{
//		System.out.println("x'");
		Token token = source.get(0);

		switch(token.value)
		{
		case "*":
			source = y(source);
			source = z(source);
			source = x_prime(source);
			return source;
		case "/":
			source = y(source);
			source = z(source);
			source = x_prime(source);
			return source;
		case "follow":
			return source;
		case "if":
			return source;
		case "while":
			return source;
		case "return":
			return source;
		case "+":
			return source;
		case "-":
			return source;
		case "<":
			return source;
		case "<=":
			return source;
		case ">":
			return source;
		case ">=":
			return source;
		case "==":
			return source;
		case "!=":
			return source;
		case "(":
			return source;
		case ";":
			tokTracker = "";

			return source;
		case ",":
			tokTracker = "";

			return source;
		case "]":
			return source;
		case ")":
			return source;

		default:
			System.out.println("REJECT");
			System.exit(0);					
		}
		return source;
	}
	public static ArrayList<Token> y(ArrayList<Token> source)
	{
	//	System.out.println("y");
		Token token = source.get(0);
		switch(token.value)
		{
		case "*":
			source.remove(0);
			return source;
		case "/":
			source.remove(0);
			return source;
		default:
			System.out.println("REJECT");
			System.exit(0);					
		}
		return source;
	}
	public static ArrayList<Token> z(ArrayList<Token> source)
	{
//		System.out.println("z");
		Token token = source.get(0);
		String temp = token.value;
		if(token.getTokenType() == "float" 
				   || token.getTokenType() == "int")
					temp = "NUM";
				if(token.getTokenType() == "ID")
						temp = "ID";
		switch(temp) 
		{
		case "(":
			args = new ArrayList<String>();
			source.remove(0);
			source = beta(source);
			if(source.get(0).value.equals(")"))
			{				
				for(int i = 0; i < symTabList.size(); i++)
				{
					if(symTabList.get(i).get(funcTrack) != null)
					{
						if(!symTabList.get(i).get(funcTrack).paramTypes.equals(args))
							{
							System.out.println("REJECT");
							System.exit(0);
							}
					}
				}

				source.remove(0);
			}
			else
			{
				System.out.println("REJECT");
				System.exit(0);									
			}
			return source;
		case "ID":
			String id = source.get(0).value;
			callCheck = id;
			Boolean idCheck = false;
			for(int i = 0; i < symTabList.size(); i++)
			{
				if(symTabList.get(i).containsKey(id))
				{
					idCheck = true;
					if(tokTracker == "")
					{
						tokTracker = symTabList.get(i).get(id).type;
					}
					else if(tokTracker != symTabList.get(i).get(id).type)
					{
						System.out.println("REJECT");
						System.exit(0);
					}

				}
			}
			if(!idCheck)
			{
				System.out.println("REJECT");
				System.exit(0);
			}
			source.remove(0);
			source = z_prime(source);
			return source;
		case "NUM":
			if(tokTracker == "")
			{
				tokTracker = source.get(0).tokenType;
			}
			else if(tokTracker != source.get(0).tokenType)
			{
				System.out.println("REJECT");
				System.exit(0);
			}
			source.remove(0);
			return source;
		default:
			
			System.out.println("REJECT");
			System.exit(0);					
		}
		return source;
	}

	public static ArrayList<Token> z_prime(ArrayList<Token> source)
	{
//		System.out.println("z'");
		Token token = source.get(0);
		switch(token.value)
		{
		case "(":
			args = new ArrayList<String>();
			source.remove(0);
			source = beta(source);
			if(source.get(0).value.equals(")"))
			{				
				for(int i = 0; i < symTabList.size(); i++)
				{
					if(symTabList.get(i).get(funcTrack) != null)
					{
						if(!symTabList.get(i).get(funcTrack).paramTypes.equals(args))
							{
							System.out.println("REJECT");
							System.exit(0);
							}
					}
				}

				source.remove(0);
			}
			else
			{
				System.out.println("REJECT");
				System.exit(0);									
			}
			return source;
		case "[":
			source = s_prime(source);
			return source;
		case "+":
			return source;
		case "-":
			return source;
		case "*":
			return source;
		case "/":
			return source;
		case "<":
			return source;
		case "<=":
			return source;
		case ">":
			return source;
		case ">=":
			return source;
		case "==":
			return source;
		case "!=":
			return source;
		case ";":
			tokTracker = "";
			return source;
		case ",":
			tokTracker = "";
			return source;
		case "]":
			tokTracker = "";
			return source;
		case ")":
			tokTracker = "";
			return source;
		default:
			System.out.println("REJECT");
			System.exit(0);					
		}
		return source;
	}
	public static ArrayList<Token> beta(ArrayList<Token> source)
	{
	//	System.out.println("beta");
		Token token = source.get(0);
		String temp = token.value;
		if(token.getTokenType() == "float" 
				   || token.getTokenType() == "int")
					temp = "NUM";
				if(token.getTokenType() == "ID")
						temp = "ID";
		switch(temp) 
		{
		case "+":
			source = delta(source);
			return source;
		case "-":
			source = delta(source);
			return source;
		case "*":
			source = delta(source);
			return source;
		case "/":
			source = delta(source);
			return source;
		case "<":
			source = delta(source);
			return source;
		case ">":
			source = delta(source);
			return source;
		case ">=":
			source = delta(source);
			return source;
		case "<=":
			source = delta(source);
			return source;
		case "==":
			source = delta(source);
			return source;
		case "!=":
			source = delta(source);
			return source;
		case "(":
			source = delta(source);
			return source;
		case "NUM":
			source = delta(source);
			return source;
		case "ID":
			source = delta(source);
			return source;
		case ")":
			return source;
		default:
			System.out.println("REJECT");
			System.exit(0);					
		}
		return source;
	}
	public static ArrayList<Token> delta(ArrayList<Token> source)
	{
	//	System.out.println("delta");
		Token token = source.get(0);
		String temp = token.value;
		if(token.getTokenType() == "float" 
				   || token.getTokenType() == "int")
					temp = "NUM";
				if(token.getTokenType() == "ID")
						temp = "ID";
		switch(temp) 
		{
		case "+":
			source = r(source);
			source = delta_prime(source);
			return source;
		case "-":
			source = r(source);
			source = delta_prime(source);
			return source;
		case "*":
			source = r(source);
			source = delta_prime(source);
			return source;
		case "/":
			source = r(source);
			source = delta_prime(source);
			return source;
		case "<":
			source = r(source);
			source = delta_prime(source);
			return source;
		case ">":
			source = r(source);
			source = delta_prime(source);
			return source;
		case "<=":
			source = r(source);
			source = delta_prime(source);
			return source;
		case ">=":
			source = r(source);
			source = delta_prime(source);
			return source;
		case "!=":
			source = r(source);
			source = delta_prime(source);
			return source;
		case "==":
			source = r(source);
			source = delta_prime(source);
			return source;
		case "(":
			source = r(source);
			source = delta_prime(source);
			return source;
		case ",":
			tokTracker = "";
			source = r(source);
			source = delta_prime(source);
			return source;
		case "ID":
			source = r(source);
			source = delta_prime(source);
			return source;
		case "NUM":
			source = r(source);
			source = delta_prime(source);
			return source;
		case ")":
			return source;
		default:
			System.out.println("REJECT");
			System.exit(0);					
		}
		return source;
	}
	public static ArrayList<Token> delta_prime(ArrayList<Token> source)
	{
	//	System.out.println("delta'");
		Token token = source.get(0);
		switch(token.value)
		{
		case ",":
			tokTracker = "";
			source.remove(0);
			source = r(source);
			source = delta_prime(source);
			return source;
		case ")":
			return source;
		default:
			System.out.println("REJECT");
			System.exit(0);					
		}
		return source;
	}
}

class quadTuple
{
	String instruction;
	String op1;
	String op2;
	String resVar;
	public quadTuple(String instruction, String op1, String op2, String resVar)
	{
		this.instruction = instruction;
		this.op1 = op1;
		this.op2 = op2;
		this.resVar = resVar;
	}
	
	
}