import java.util.*;
import java.io.*;
import java.util.Map.Entry;
public class Compiler
{
	static Stack<Structure> level = new Stack<Structure>();
	static String arg0, programTitle, inp, uinp;
	static String programCode = "[bits 32]\n\n";
	static Map<String, OClass> classes = new HashMap<String, OClass>();
	static boolean finishedMacros = false;
	static int lineNumber, tabLevel, lastTabLevel = 0, lastRealLine;
	static File f;
	static Scanner in;
	static OClass currentClass;
	static PrintWriter out;
	static Map<String, OVar> libvars = new HashMap<String, OVar>();
	static char[] acceptableNumericChars = {'-', 'x', 'a', 'b', 'c', 'd', 'e', 'f', 'A', 'B', 'C', 'D', 'E', 'F'};
	
	public static void main(String[] args) throws Exception
	{
		loadLib(".");
		f = new File(args[0]);
		File outFile = new File(args[0].replaceAll(".orca", ".asm"));
		arg0 = outFile.getName().replaceAll(".orca", "");
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
				makeFunction(words, inp);
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
					System.out.println("Var dec is linked.");
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
		System.out.println("[CloseLevel] Closing on " + inp);
		Structure s = level.pop();
		s.close();
	}
	public static void handleDirective()
	{}
	public static void makeFunction(String[] words, String inp)
	{
		System.out.println("[MakeFunc] make function: " + getSecondRealWord(words));
		String[] mods = getModifiers(words);
		boolean linked = contains(mods, "linked");
		String returnType = smartSplit(inp.split("\\Q(\\E")[1], ':')[0].trim();
		for (String s : smartSplit(inp, ':'))
			System.out.println("[MakeFunc] Found dec part: '" + s + "'");
		String[] params = smartSplit(smartSplit(inp, ':')[1], ',');
		trimArray(params);
		Function func = new Function(currentClass, getSecondRealWord(words), mods, returnType, params, linked);
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
			programCode += "call " + funcName + "\n";
			System.out.println("[CallFunc] Called function '" + funcName + "'");
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
	public static OVar createVar(String[] words, String inp)
	{
		System.out.println("[MakeVar] Handed '" + inp + "'");
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
					System.out.println("'" + level.peek().asmName + "' now knows " + (contains(mods, "linked")?"linked":"") + "var '" + name + "'");
		return v;
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
		String[] words = smartSplit(inp, ' ');
		trimArray(words);
		String firstWord = getFirstRealWord(words);
		if (contains(Types.defined, firstWord) || contains(Types.primitive, firstWord))
			createVar(words, inp);
		if (contains(words, "="))
		{
			String rest = "";
			for (int i = 1; i < inp.split("\\Q=\\E").length; i++)
				rest += inp.split("\\Q=\\E")[i];
			parse(rest);
			OVar v = null;
			if (contains(Types.defined, firstWord) || contains(Types.primitive, firstWord))
			{
				v = getVar(getSecondRealWord(words));
				System.out.println("'" + getSecondRealWord(words) + "'");
			}
			else 
				v = getVar(firstWord);
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
			System.out.println("MATH THINGS! : '" + inp + "'");
			programCode += "push edx\t; Math start\n";
			parse(smartSplit(inp, '+')[1]);
			programCode += "mov edx, ecx\n";
			parse(firstWord);
			if (contains(inp, "+"))
				programCode += "add ecx, edx\n";
			else if (contains(inp, "-"))
				programCode += "sub ecx, edx\n";
			else if (contains(inp, "*"))
				programCode += "imul ecx, edx\n";
			else if (contains(inp, "/"))
				throw new RuntimeException("Division is hard :(");
			programCode += "pop edx\t; Math end\n";
		}
		else if (isNumeric(inp))
		{
			System.out.println("Numeric value found: " + inp);
			programCode += "mov ecx, " + inp + "\n";
		}
		else if (contains(Types.swappable, firstWord))
		{
			System.out.println("Replace '" + firstWord + "'");
			programCode += "mov ecx, " + currentClass.classSize + "\n";
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
		System.out.println("[GetVar] Handed '" + commonName + "'");
		Stack<Structure> levelStor = new Stack<Structure>();
		OVar v = null;
		while (!level.isEmpty())
		{
			Structure struct = level.pop();
			levelStor.push(struct);
			v = struct.vars.get(commonName);
			if (v != null)
				break;
		}
		while (!levelStor.isEmpty())
			level.push(levelStor.pop());
		if (v==null)	// check libraries
			v = libvars.get(commonName);
		if (v==null && contains(commonName, "."))	// check linked ovars
		{
			String[] splits = commonName.split("\\Q.\\E");
			String stor = "";
			for (int i = 0; i < splits.length-1; i++)
				stor += splits[i] + ".";
			stor = stor.substring(0, stor.length()-1).trim();
			
			OVar var = getVar(stor);
			OClass claz = classes.get(var.type);
			
			for (Entry e : claz.linkedOVars.entrySet())
			{
				if (((String)e.getKey()).equals(splits[splits.length-1]))
				{
					System.out.println("[GetVar] ITS A LINKED VAR :O '" + commonName + "'");	// great... but how to proccess it? spit out the asm right here???
					
					return (OVar)e.getValue();
				}
			}
		}
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
		int lastSplit = -1;
		ArrayList<String> found = new ArrayList<String>();
		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			if (c=='\"')
				inQuotes = !inQuotes;
			if (!inQuotes && c==match)
			{
				found.add(s.substring(lastSplit+1, i+1));
				lastSplit = i;
			}
		}
		if (lastSplit != s.length())
			found.add(s.substring(lastSplit+1, s.length()));
		String[] ret = new String[found.size()];
		for (int i = 0; i < ret.length; i++)
		{
			ret[i] = found.get(i);
			//System.out.println("[Split]\t" + ret[i]);
		}
		return ret;	
	}
	static void loadLib(String path)
	{
		File dir = new File(path);
		for (File sub : dir.listFiles())
			if (contains(sub.getName(), ".varlist"))
				try
				{
					loadSingleLib(sub);
				}
				catch (Exception e)
				{
					System.out.println("[Library] Failed to load lib '" + sub.getName().replaceAll(".varlist", "") + "'");
					e.printStackTrace();
				}
	}
	static void loadSingleLib(File f) throws Exception
	{
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
		String className = f.getName().replaceAll(".varlist", "");
		System.out.println("[Library] Load lib: '" + className + "'");
		Map<String, Object> vars = (HashMap<String, Object>)in.readObject();
		Map<String, Object> linkedVars = (HashMap<String, Object>)in.readObject();
		for (Entry o : vars.entrySet())
		{
			if (o.getValue() instanceof SerializableOPrimitive)
				libvars.put(className + "." + o.getKey(), new OPrimitive((SerializableOPrimitive)o.getValue()));
			else
				libvars.put(className + "." + o.getKey(), (OVar)o.getValue());
		}
		// do something with the linked ovars here...
	}
}
class Types
{
	static String[] primitive = {"int", "int_s", "bool", "byte", "String", "this"};	// String and this are special cases
	static String[] special = {"null", "void"};
	static String[] defined = {"Window", "Image", "Buffer", "Pointer", "Color"};
	static String[] modifier = {"linked", "capped", "final", "invis"};
	static String[] block = {"if", "for", "while"};
	static String[] swappable = {"$LinkedClassSize"};
}
class OVar implements Serializable
{
	String type;
	String asmName;
	String[] modifiers;
	Integer refSize = 4;
	Integer linkedOffset = -1;
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
	protected OVar(){}
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
		if (ltype.equals("String") || ltype.equals("int") || ltype.equals("this"))
			refSize = 4;
		if (ltype.equals("this"))
			asmName = "ebx";
	}
	public OPrimitive(String ltype, String[] modifiers, int linkedOffset, String asmName)
	{
		this(ltype, modifiers, asmName);
		this.linkedOffset = linkedOffset;
	}
	public OPrimitive(SerializableOPrimitive p)
	{
		this(p.type, p.modifiers, p.asmName);
		linkedOffset = p.linkedOffset;
	}
}
class SerializableOPrimitive implements Serializable
{
	String type;
	String asmName;
	String[] modifiers;
	Integer refSize;
	Integer linkedOffset;
	public SerializableOPrimitive(OPrimitive p)
	{
		type = p.type;
		asmName = p.asmName;
		modifiers = p.modifiers;
		refSize = new Integer(p.refSize);
		linkedOffset = new Integer(p.linkedOffset);
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
		Compiler.programCode += name + ".returnVal:\n\tdd 0x0\n";
		Compiler.programCode += name + ".$FILE_END :";
		try{
			ObjectOutputStream varList = new ObjectOutputStream(new FileOutputStream(name + ".varlist"));
			Map<String, Object> vout = new HashMap<String, Object>();
			for (Entry e : vars.entrySet())
				if (e.getValue() instanceof OPrimitive)
				{
					OPrimitive stor = (OPrimitive)e.getValue();
					String key = (String)e.getKey();
					vout.put(key, new SerializableOPrimitive(stor));
				}
				else
					vout.put((String)e.getKey(), e.getValue());
			varList.writeObject(vout);
			Map<String, Object> linkedvout = new HashMap<String, Object>();
			for (Entry e : linkedOVars.entrySet())
				if (e.getValue() instanceof OPrimitive)
				{
					OPrimitive stor = (OPrimitive)e.getValue();
					String key = (String)e.getKey();
					linkedvout.put(key, new SerializableOPrimitive(stor));
				}
				else
					linkedvout.put((String)e.getKey(), e.getValue());
			varList.writeObject(linkedvout);
		}
		catch(Exception e){e.printStackTrace();}
	}
}
class Function extends Structure
{
	OClass claz;
	String name;
	String[] mods;
	boolean linked;
	static final String RETURN_CODE = "pop edx\npop ebx\npop eax\npush dword [" + Compiler.currentClass.name + ".returnVal]\nret\n";
	static int returnedOn;
	ArrayList<OVar> params = new ArrayList<OVar>();
	public Function(OClass claz, String name, String[] mods, String returnType, String[] params, boolean linked)
	{
		this.claz = claz;
		this.name = name;
		this.mods = mods;
		this.linked = linked;
		this.asmName = Compiler.currentClass.name + "." + name;
		if (!params[0].equals("null)"))
			for (String p : params)
			{
				p = p.substring(0, p.length()-1);
				OVar v = Compiler.createVar(Compiler.smartSplit(p, ' '), p);
				this.params.add(v);
			}
	}
	public void open()
	{
		Compiler.programCode += claz.name + "." + name + ": \n";
		Compiler.programCode += "pop dword [" + Compiler.currentClass.name + ".returnVal]\n";
		for (OVar v : params)
		{
			String sizeSpec = "dword";
			if (v.refSize == 1)
				sizeSpec = "byte";
			else if (v.refSize == 2)
				sizeSpec = "word";
			Compiler.programCode += "pop " + sizeSpec + " [" + v.asmName + "]\n";
		}
		Compiler.programCode += "push eax\npush ebx\npush edx\n";
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
		loopNum++;
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
		loopNum++;
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
		loopNum++;
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