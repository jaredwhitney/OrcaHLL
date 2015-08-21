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
			else if (firstRealWord.equals("program"))
				makeProgram();
			else if (firstRealWord.equals("classdef"))
				makeClass(words);
			else
				parse(inp);
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
		System.out.println("[MakeFunc] On level_" + level.size());
		System.out.println("[MakeFunc] Added '" + level.peek().getClass().getName() + "'");
		func.open();
	}
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
		else
		{
			parseArgs(inp);
			// handle actual function calls here!
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
		System.out.println("Create var '" + getSecondRealWord(words) + "'");
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
					System.out.println("'" + level.peek().asmName + "' now knows var '" + name + "'");
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
		inp = inp.trim();
		String[] words = inp.split(" ");
		trimArray(words);
		String firstWord = getFirstRealWord(words);
		if (contains(Types.defined, firstWord) || contains(Types.primitive, firstWord))
			createVar(words);
		if (contains(words, "="))
		{
			String rest = "";
			for (int i = 1; i < inp.split("\\Q=\\E").length; i++)
				rest += inp.split("\\Q=\\E")[i];
			parse(rest);
			OVar v = getVar(firstWord);
			if (contains(Types.defined, firstWord) || contains(Types.primitive, firstWord))
			{
				v = getVar(getSecondRealWord(words));
				System.out.println("'" + getSecondRealWord(words) + "'");
			}
			System.out.println("'" + v.asmName + "' is a var. (I think...)");
			programCode += "mov [" + v.asmName + "], " + v.getECXform() + "\n";
		}
		else if (firstWord.equals("return"))
		{
			System.out.println("[Return] Begin handling return.");
			String rest = "";
			for (int i = 1; i < words.length; i++)
				rest += words[i] + " ";
			parse(rest.trim());
			programCode += Function.RETURN_CODE;
			Function.returnedOn = lineNumber;
			System.out.println("[Return] End handling return.");
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
			callFunction(inp);
		}
		else if (contains(inp, "+") || contains(inp, "-") || contains(inp, "*") || contains(inp, "/"))
		{
			System.out.println("AAH MATH THINGS IN THIS LINE: '" + inp + "'");
		}
		else if (isNumeric(inp))
		{
			System.out.println("Numeric value found: " + Integer.parseInt(inp));
		}
		else
		{
			System.out.println("Assuming that '" + inp + "' is a variable for now.");
			OVar v = getVar(firstWord);
			programCode += "mov " + v.getECXform() + ", [" + v.asmName + "]\n";
		}
	}
	public static OVar getVar(String commonName)	// should also check linked OVars...
	{
		Stack<Structure> levelStor = new Stack<Structure>();
		OVar v = null;
		while (!level.isEmpty())
		{
			Structure struct = level.pop();
			levelStor.push(struct);
			System.out.println("Look in " + struct.asmName + " (" + struct.getClass().getName() + ")");
			v = struct.vars.get(commonName);
			if (v != null)
				break;
		}
		while (!levelStor.isEmpty())
			level.push(levelStor.pop());
		return v;
	}
	public static void parseArgs(String inp)
	{
		System.out.println("[ParseArgs] Handed String '" + inp + "'");
		int i = 0;
		char[] arr = inp.toCharArray();
		for (; i < arr.length && arr[i]!='('; i++){}
		int e = i+1;
		boolean inQuotes = false;
		boolean inFunc = false;
		for (; e < arr.length && !(arr[e]==')' && !inQuotes && !inFunc); e++)
		{
			char c = arr[e];
			if (c=='\"')
				inQuotes = !inQuotes;
			if (c=='(' && !inQuotes)
				inFunc = true;
			if (c==')' && !inQuotes)
				inFunc = false;
		}
		String argstr = inp.substring(i+1, e);
		System.out.println("[ParseArgs] All args should appear in: '" + argstr + "'");
		String[] args = smartSplit(argstr, ',');
		System.out.println("[ParseArgs] Found " + args.length + " args");
		trimArray(args);
		for (String arg : args)
		{
			System.out.println("[ParseArgs] Parsing found arg: '" + arg + "'");
			parse(arg);
			programCode += "push ecx\n";
		}
	}
	static boolean isNumeric(String s)
	{
		String lookIn = s.split(" ")[0];
		char[] acceptableNumericChars = {'-', 'x', 'a', 'b', 'c', 'd', 'e', 'f', 'A', 'B', 'C', 'D', 'E', 'F'};
		for (char c : lookIn.toCharArray())
			if (!Character.isDigit(c))
			{
				boolean notGood = true;
				for (char x : acceptableNumericChars)
					if (x==c)
						notGood = true;
				if (notGood)
					return false;
			}
		return true;
	}
	static String[] smartSplit(String s, char match)
	{
		boolean inQuotes = false;
		int lastSplit = 0;
		ArrayList<String> found = new ArrayList<String>();
		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			if (c=='\"')
				inQuotes = !inQuotes;
			if (!inQuotes && c==match)
			{
				found.add(s.substring(lastSplit, i));
				lastSplit = i;
			}
		}
		if (found.size()==0)
			found.add(s);
		String[] ret = new String[found.size()];
		for (int i = 0; i < ret.length; i++)
		{
			ret[i] = found.get(i);
			System.out.println("[Split]\t" + ret[i]);
		}
		return ret;	
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
	public String getECXform()
	{
		if (refSize == 1)
			return "cl";
		if (refSize == 2)
			return "cx";
		return "ecx";
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
	static int returnedOn;
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
			Compiler.programCode += RETURN_CODE;
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
class O_If extends Structure
{
	public static int loopNum = 0;
	public O_If(String inp)
	{
		asmName = Compiler.currentClass.name + ".$loop_if." + loopNum;
		// parse inp for the needed values
	}
	public void open()
	{
		// code goes here
	}
	public void close()
	{
		// code goes here
	}
}
class O_For extends Structure
{
	public static int loopNum = 0;
	public O_For(String inp)
	{
		asmName = Compiler.currentClass.name + ".$loop_for." + loopNum;
		// parse inp for the needed values
	}
	public void open()
	{
		// code goes here
	}
	public void close()
	{
		// code goes here
	}
}
class O_While extends Structure
{
	public static int loopNum = 0;
	public O_While(String inp)
	{
		asmName = Compiler.currentClass.name + ".$loop_while." + loopNum;
		// parse inp for the needed values
	}
	public void open()
	{
		// code goes here
	}
	public void close()
	{
		// code goes here
	}
}