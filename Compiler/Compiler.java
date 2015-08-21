import java.util.*;
import java.io.*;
import java.util.Map.Entry;
public class Compiler
{
	static Stack<Structure> level = new Stack<Structure>();
	static String programName, programTitle, inp, uinp;
	static String programCode = "[bits 32]\n\n";
	static Map<String, OClass> classes = new HashMap<String, OClass>();
	static boolean finishedMacros = false;
	static int lineNumber, tabLevel, lastTabLevel = 0, lastRealLine;
	static File f;
	static Scanner in;
	static OClass currentClass;
	static PrintWriter out;
	
	public static void main(String[] args) throws Exception
	{
		f = new File(args[0]);
		File outFile = new File(args[0].replaceAll(".orca", ".asm"));
		out = new PrintWriter(outFile);
		in = new Scanner(f);
		inp = in.nextLine();
		lineNumber++;
		try{
		while (inp != null)
		{
			uinp = removeComments(inp);
			inp = uinp.trim();
			if (!inp.equals(""))
			{
				handleLine();
				lastRealLine = lineNumber;
			}
			inp = in.nextLine();
			lineNumber++;
		}}
		catch (java.util.NoSuchElementException e){}
		finally
		{
			while (!level.isEmpty())
				level.pop().close();
			out.println(programCode);
			out.close();
		}
	}
	public static void handleLine() throws Exception
	{
		tabLevel = getTabCount(uinp);
		String[] words = inp.split(" ");
		trimArray(words);
		
		if (tabLevel < lastTabLevel)
			closeLastLevel();
		if (words[0].charAt(0)=='#')
			handleDirective();
		else
		{
			String firstRealWord = getFirstRealWord(words);
			if (firstRealWord.equals("func"))
				makeFunction(words);
			else if (firstRealWord.equals("return"))
				handleReturn();
			else if (firstRealWord.equals("program"))
				makeProgram();
			else if (firstRealWord.equals("classdef"))
			{
				makeClass(words);
			}
			else if (contains(Types.defined, firstRealWord) || contains(Types.primitive, firstRealWord))
				createVar(words);
			else if (isFuncCall(inp))
			{
				callFunction(inp);
			}
			else
				System.out.println(firstRealWord + " must be a variable!");
		}
		lastTabLevel = tabLevel;
	}
	public static void makeClass(String[] iwords) throws Exception
	{
		String className = getSecondRealWord(iwords);
		OClass claz = new OClass(className, getModifiers(iwords));
		classes.put(className, claz);
		currentClass = claz;
		level.add(claz);
		claz.open();
		
		claz.linkedOVars = new HashMap<String, OVar>();
		uinp = removeComments(in.nextLine());
		inp = uinp.trim();
		System.out.println("first: " + inp);
		while (getTabCount(uinp) != 0 && !contains(inp, "func"))
		{
			if (!inp.equals(""))
			{
				System.out.println("OVardec: " + inp);
				//
				String[] words = inp.split(" ");
				String name = getSecondRealWord(words);
				String type = getFirstRealWord(words);
				String[] mods = getModifiers(words);
				
				if (contains(mods, "linked"))
				{
					OVar v;
					if (contains(Types.primitive, type))
						v = new OPrimitive(type, mods, claz.classSize, className + ".$offs." + name);
					else
						v = new OVar(type, mods, claz.classSize, className + ".$offs." + name);
					claz.linkedOVars.put(name, v);
					claz.classSize += v.refSize;
				}
				else
				{
					OVar v;
					if (contains(Types.primitive, type))
					{
						v = new OPrimitive(type, mods, className + ".$global." + name);
					}
					else
						v = new OVar(type, mods, className + ".$global." + name);
					claz.vars.put(name, v);
				}
				//
			}
			inp = removeComments(in.nextLine()).trim();
		}
		
		for (Entry<String, OVar> e : claz.linkedOVars.entrySet())
		{
			OVar v = e.getValue();
			programCode += v.asmName + " equ " + v.linkedOffset + "\n";
		}
		programCode += claz.linkedOVars.entrySet().size()>0?"\n":"";
		for (Entry<String, OVar> e : claz.vars.entrySet())
		{
			OVar v = e.getValue();
			declareVar(v);
		}
		System.out.println(className + " size:: " + claz.classSize);
		handleLine();	// to handle the line that we stopped on
	}
	public static String getFirstRealWord(String[] words)
	{
		String firstRealWord = "";
		for (String word : words)
			if (!contains(Types.modifier, word))
			{
				firstRealWord = word;
				break;
			}
		return firstRealWord;
	}
	public static String getSecondRealWord(String[] words)
	{
		String secondRealWord = "";
		for (int z = 0; z < words.length; z++)
			if (!contains(Types.modifier, words[z]))
			{
				secondRealWord = words[z+1];
				break;
			}
		return secondRealWord;
	}
	public static String removeComments(String in)
	{
		String ret = "";
		boolean inQuotes = false;
		for (char c : in.toCharArray())
		{
			if (c == '\"')
				inQuotes = !inQuotes;
			if (c == ';' && !inQuotes)
				break;
			ret += c;
		}
		return ret;
	}
	public static void closeLastLevel()
	{
		System.out.println("Closing on " + inp);
		Structure s = level.pop();
		s.close();
	}
	public static void handleDirective()
	{}
	public static void makeFunction(String[] words)
	{
		System.out.println("make function: " + getSecondRealWord(words));
		String[] mods = getModifiers(words);
		boolean linked = contains(mods, "linked");
		Function func = new Function(currentClass, getSecondRealWord(words), mods, linked);
		level.add(func);
		func.open();
	}
	public static void handleReturn()
	{}
	public static void makeProgram()
	{}
	public static void callFunction(String inp)
	{
		int i = 0;
		for (char c : inp.toCharArray())
		{
			if (c=='(')
				break;
			i++;
		}
		String funcName = inp.substring(0, i);
		System.out.println("\tCall to func '" + funcName + "'");
		if (funcName.equals("asm"))
		{
			i++;
			int e = 0;
			boolean inQuotes = false;
			for (char c : inp.toCharArray())
			{
				if (c=='\"')
					inQuotes = !inQuotes;
				if (!inQuotes && e > i)
					break;
				e++;
			}
			String code = inp.substring(i+1, e);
			System.out.println("\t<asm> '" + code + "'");
			programCode += code + "\t; INLINE ASSEMBLY\n";
		}
	}
	public static boolean isFuncCall(String inp)
	{
		boolean inQuotes = false;
		for (char c : inp.toCharArray())
		{
			if (c==' ' && !inQuotes)	// if this parse isn't a function call, we don't care if the next one would be
				break;
			if (c==')' && !inQuotes) // if this parse is from within a block dec or func call, don't look outside of it
				break;
			if (c=='\"')
				inQuotes = !inQuotes;
			if (c=='(' && !inQuotes)
				return true;
		}
		return false;
	}
	public static void createVar(String[] words)
	{
		System.out.println("Create var " + getSecondRealWord(words));
		String name = getSecondRealWord(words);
		String type = getFirstRealWord(words);
		String[] mods = getModifiers(words);
		
		String asmName = level.peek().asmName;
		asmName += ".$local." + name;
					OVar v;
					if (contains(Types.primitive, type))
					{
						v = new OPrimitive(type, mods, asmName);
					}
					else
						v = new OVar(type, mods, asmName);
					level.peek().vars.put(name, v);
	}
	public static String[] getModifiers(String[] words)
	{
		String[] arr = new String[words.length];
		int count = 0;
		for (;count < words.length && contains(Types.modifier, words[count]); count++){arr[count] = words[count];}
		String[] ret = new String[count];
		count--;
		for (; count >= 0; count--)
		{
			ret[count] = arr[count];
		}
		return ret;
	}
	public static void trimArray(String[] arr)
	{
		for (int q = 0; q < arr.length; q++)
			arr[q] = arr[q].trim();
	}
	public static int getTabCount(String s)
	{
		int count;
		for (count = 0; count < s.length() && s.charAt(count)=='\t'; count++){}
		return count;
	}
	public static boolean contains(String[] a, String b)
	{
		for (String c : a)
			if (contains(c, b))
				return true;
		return false;
	}
	public static boolean contains(String a, String b)
	{
		return a==null?false:a.contains(b)||a.equals(b);
	}
	public static void declareVar(OVar v)
	{
		String dec = "dd";
		if (v.refSize == 1)
			dec = "db";
		else if (v.refSize == 2)
			dec = "dw";
		programCode += v.asmName + " :\n\t" + dec + " 0x0\n";
	}
	public static void parse(String inp)
	{
		String firstWord = getFirstRealWord(inp);
		if (contains(Types.declared, firstWord) || contains(Types.primitive, firstWord))
			createVar();
		if (contains(words, "="))
		{
			parse(inp.split("\\Q=\\E"));
			Var v = getVar(firstWord);
			programCode += "mov [" + v.asmName + "], " + v.getECXform() + "\n";
		}
		else if (firstWord.equals("return"))
		{
			String rest = "";
			String[] words = inp.split(" ");
			for (int i = 1; i < words.length; i++)
				rest += words + " ";
			parse(rest.trim());
			programCode += Function.RETURN_CODE;
			Function.returnedOn = lineNumber;
		}
		else if (contains(Types.block, firstWord))
		{
			Structure s = null;
			switch (firstWord)
			{
				case "if":
					s = new O_If(inp);
					break;
				case "for":
					s = new O_For(inp);
					break;
				case "while":
					s = new O_While(inp);
					break;
			}
			level.add(s);
			s.open();
		}
		else if (isFuncCall(inp))
		{
			parseArgs(inp);
			callFunction(firstWord);
		}
		else
		{
			System.out.println("Assuming that '" + inp + "' is a variable for now.");
			Var v = getVar(firstWord);
			programCode += "mov " + v.getECXform + ", [" + v.asmName + "]\n";f
		}
	}
}
class Types
{
	static String[] primitive = {"int", "int_s", "bool", "byte", "String"};	// String is special case
	static String[] special = {"null", "void"};
	static String[] defined = {"Window", "Image", "Buffer", "Pointer", "Color"};
	static String[] modifier = {"linked", "capped", "final", "invis"};
	static String[] block = {"if", "for", "while"};
}
class OVar
{
	String type;
	String asmName;
	String[] modifiers;
	int refSize = 4;
	int linkedOffset = -1;
	public OVar(String type, String[] modifiers, String asmName)
	{
		this.type = type;
		this.modifiers = modifiers;
		this.asmName = asmName;
	}
	public OVar(String type, String[] modifiers, int linkedOffset, String asmName)
	{
		this(type, modifiers, asmName);
		this.linkedOffset = linkedOffset;
	}
}
class OPrimitive extends OVar
{
	public OPrimitive(String ltype, String[] modifiers, String asmName)
	{
		//assert (ltype instanceof String);
		super((String)ltype, modifiers, asmName);
		if (ltype.equals("byte") || ltype.equals("char"))
			refSize = 1;
		if (ltype.equals("int_s"))
			refSize = 2;
		if (ltype.equals("String") || ltype.equals("int"))
			refSize = 4;
	}
	public OPrimitive(String ltype, String[] modifiers, int linkedOffset, String asmName)
	{
		this(ltype, modifiers, asmName);
		this.linkedOffset = linkedOffset;
	}
}
class OClass extends Structure
{
	Map<String, OVar> linkedOVars;
	String[] modifiers;
	String name;
	int classSize = 0;
	public OClass(String name, String[] modifiers)
	{
		this.modifiers = modifiers;
		this.name = name;
		this.asmName = name;
	}
	public void open()
	{
		Compiler.programCode += "dd " + name + ".$FILE_END - " + name + ".$FILE_START\n";	// File header
		Compiler.programCode += "db \"OrcaHLL Class\", 0\n";
		Compiler.programCode += "db \"" + name + "\", 0\n";
		Compiler.programCode += name + ".$FILE_START :\n\n";
	}
	public void close()
	{
		Compiler.programCode += name + ".$FILE_END :";
	}
}
class Function extends Structure
{
	OClass claz;
	String name;
	String[] mods;
	boolean linked;
	static final String RETURN_CODE = "pop edx\npop ebx\npop eax\nret\n";
	public Function(OClass claz, String name, String[] mods, boolean linked)
	{
		this.claz = claz;
		this.name = name;
		this.mods = mods;
		this.linked = linked;
		this.asmName = Compiler.currentClass.name + "." + name;
	}
	public void open()
	{
		Compiler.programCode += claz.name + "." + name + ": \npush eax\npush ebx\npush edx\n";
	}
	public void close()
	{
		if (returnedOn != Compiler.lastRealLine)
			programCode += RETURN_CODE;
		Compiler.programCode += "\t;Vars:\n";
		for (Entry e : vars.entrySet())
			Compiler.declareVar((OVar)e.getValue());
		Compiler.programCode += "\n\n";
	}
}
abstract class Structure
{
	Map<String, OVar> vars = new HashMap<String, OVar>();
	String asmName;
	public abstract void open();
	public abstract void close();
}