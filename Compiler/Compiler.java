import java.util.*;
import java.io.*;
import java.util.Map.Entry;
public class Compiler
{
	static Stack<Structure> level = new Stack<Structure>();
	static String arg0, programTitle, inp, uinp;
	static String programCode = "[bits 32]\n\n", closingCode = "";
	static Map<String, OClass> classes = new HashMap<String, OClass>();
	static boolean finishedMacros = false, syscall = false;
	static int lineNumber, tabLevel, lastTabLevel = 0, lastRealLine, gvarsubs = 0;
	static File f;
	static Scanner in;
	static OClass currentClass;
	static Function currentFunction;
	static PrintWriter out;
	static Map<String, OVar> libvars = new HashMap<String, OVar>();
	static String[] libdirs = {".", "../libs", "../libraries"};
	static char[] acceptableNumericChars = {'-', 'x', 'a', 'b', 'c', 'd', 'e', 'f', 'A', 'B', 'C', 'D', 'E', 'F'};
	
	public static void main(String[] args) throws Exception
	{
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
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			while (!level.isEmpty())
				level.pop().close();
			out.println(programCode);
			out.println(closingCode);
			out.close();
		}
	}
	public static void handleLine() throws Exception
	{
		tabLevel = getTabCount(uinp);
		String[] words = inp.split(" ");
		trimArray(words);
		
		if (tabLevel < lastTabLevel)
			for (int i = tabLevel; i < lastTabLevel; i++)
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
			else if (firstRealWord.equals("import"))
				importLib(getSecondRealWord(words));
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
						v = new OPrimitive(type, mods, claz.classSize, className + "." + name);
					else
						v = new OVar(type, mods, claz.classSize, className + "." + name);
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
		System.out.println("Check linked processing...");
		for (Entry<String, OVar> e : claz.linkedOVars.entrySet())
		{
			OVar v = e.getValue();
			programCode += v.asmName + " equ " + v.linkedOffset + "\n";
			System.out.println("Defined a constant");
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
		System.out.println("[MakeFunc] On level_" + level.size());
		System.out.println("[MakeFunc] Added '" + level.peek().getClass().getName() + "'");
		func.open();
	}
	public static void makeProgram()
	{}
	public static void callFunction(String inp)
	{
		boolean n_up_b = false;
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
		else if (funcName.equals("asm_var"))
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
			programCode += "mov ecx, [" + code + "]\n";
		}
		else
		{
			String[] twds = funcName.split("\\Q.\\E");
			if (!contains(funcName, "."))
				funcName = currentClass.asmName + "." + funcName;
			else if (isVar(twds[0]) || (twds.length > 2 && isVar(twds[0]+'.'+twds[1])))
			{
				System.out.println("Woah! '" + funcName + "' is a linked function :o");
				String lvar = "";
				for (int ic = 0; ic < twds.length-1; ic++)
					lvar += twds[ic] + '.';
				lvar = lvar.substring(0, lvar.length()-1);
				System.out.println("\tVar is '" + lvar + "'");
				OVar v = getVar(lvar);
				String funcToCall = v.type + funcName.substring(funcName.split("\\Q.\\E")[0].length(), funcName.length());
				System.out.println("Calling '" + funcToCall + "' on var '" + v.asmName + "'");
				programCode += "push ebx\n";
				n_up_b = true;
				programCode += "mov ebx, " + v.asmName + "\n";
				funcName = funcToCall;
				//System.exit(0);
			}
			parseArgs(inp);
			programCode += "call " + funcName + "\n";
			if (n_up_b)
				programCode += "pop ebx\n";
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
	public static void callSystemFunction(String inp)
	{
		int i = 0;
		for (char c : inp.toCharArray())
		{
			if (c=='(')
				break;
			i++;
		}
		String funcName = inp.substring(0, i);
		System.out.println("\tSyscall '" + funcName + "'");
		String name = SystemCall.lookup(funcName);
		syscall = false;
		parseArgs(inp);
		System.out.println("\t\tint 0x30 ax = " + name);
		programCode += "mov ax, " + name + "\n";
		programCode += "int 0x30\n";
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
	public static boolean existsIn(String[] a, String b)
	{
		for (String c : a)
			if (c.equalsIgnoreCase(b))
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
		programCode += v.asmName + " :\n\t" + dec + " " + v.ival + "\n";
	}
	public static void parse(String inp)
	{
		inp = inp.trim();
		System.out.println("[parse] " + inp);
		String[] words = smartSplit(inp, ' ');
		trimArray(words);
		String firstWord = getFirstRealWord(words);
		if (existsIn(Types.defined, firstWord) || existsIn(Types.primitive, firstWord) || isClassName(firstWord))
		{
			System.out.println("Kgetvardec " + firstWord);
			createVar(words, inp);
		}
		if (((words.length >= 3 && words[1].equals("=")) || (words.length >= 4 && words[2].equals("="))) && !firstWord.equalsIgnoreCase("for") && !contains(words, "!=") && !contains(words, "==") && !contains(words, "<=") && !contains(words, ">="))
		{
			String rest = "";
			for (int i = 1; i < inp.split("\\Q=\\E").length; i++)
				rest += inp.split("\\Q=\\E")[i];
			System.out.println("Parsing things after the '='...");
			parse(rest);
			System.out.println("Returned.");
			OVar v = null;
			if (existsIn(Types.defined, firstWord) || existsIn(Types.primitive, firstWord) || isClassName(firstWord))
			{
				v = getVar(getSecondRealWord(words));
				System.out.println("'" + getSecondRealWord(words) + "'");
			}
			else 
			{
				System.out.println("Kgetvar " + firstWord);
				v = getVar(firstWord);
			}
			System.out.println("'" + v.asmName + "' is a var. (I think...)");
			programCode += "mov [" + v.asmName + "], " + v.getECXform() + "\n";
		}
		else if (inp.charAt(0) == '\"')
		{
			System.out.println("'" + inp + "' contains a String!");
			OVar v = createString(inp);
			programCode += "mov " + v.getECXform() + ", [" + v.asmName + "]\n";
		}
		else if (firstWord.equals("new"))
		{
			System.out.println("***\nShould create object here\n***");
			// FIGURE THIS OUT!!!
		}
		else if (firstWord.equals("return"))
		{
			System.out.println("[Return] Begin handling return.");
			String rest = "";
			for (int i = 1; i < words.length; i++)
				rest += words[i] + " ";
			parse(rest.trim());
			programCode += currentFunction.RETURN_CODE;
			Function.returnedOn = lineNumber;
			System.out.println("[Return] End handling return.");
		}
		else if (existsIn(Types.block, firstWord))
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
			if (syscall)
			{
				callSystemFunction(inp);
			}
			else
				callFunction(inp);
		}
		else if (firstWord.equals("sys"))
		{
			syscall = true;
			String rest = inp.substring("sys".length(), inp.length());
			System.out.println("Syscall in '" + rest + "'");
			parse(rest);
		}
		else if (isComparison(inp))
		{
			System.out.println("[Compare] '" + inp + "'");
			handleComparison(inp);
		}
		/*else if (words.length > 1 && getSecondRealWord(words).equals("as"))	// There needs to be some better way of doing this...
		{
			String[] arr = smartSplit(inp, "as");
			String firstPart = arr[0];
			String secondPart = arr[1];
			System.out.println("\tFigure out '" + firstPart + "' as '" + secondPart + "'");
			if (isVar(firstPart))
				getVar(firstPart).type = secondPart;
		}*/
		else if (contains(inp, "+") || contains(inp, "-") || contains(inp, "*") || contains(inp, "/"))
		{
			System.out.println("MATH THINGS! : '" + inp + "'");
			programCode += "push edx\t; Math start\n";
			char c = '?';
			if(contains(inp, "*"))
				c = '*';
			else if(contains(inp, "/"))
				c = '/';
			else if(contains(inp, "+"))
				c = '+';
			else if(contains(inp, "-"))
				c = '-';
			parse(smartSplit(inp, c)[1]);
			programCode += "mov edx, ecx\n";
			String second = smartSplit(inp, c)[0];
			parse(second.substring(0, second.length()-1));
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
			System.out.println("Numeric value found: " + firstWord);
			programCode += "mov ecx, " + firstWord + "\n";
		}
		else if (existsIn(Types.swappable, firstWord))
		{
			System.out.println("Replace '" + firstWord + "'");
			programCode += "mov ecx, " + currentClass.classSize + "\n";
		}
		else if (SystemConstant.lookup(firstWord) != null)
		{
			System.out.println("Replace '" + firstWord + "'");
			programCode += "mov ecx, " + SystemConstant.lookup(firstWord) + "\t; System Constant\n";
		}
		else if (firstWord.charAt(0)=='\'')	// should add some handling to escaped chars
		{
			String val = inp.substring(0, 3);
			System.out.println("Replace _" + val + "_");
			programCode += "mov ecx, " + val + "\t; char\n";
		}
		else
		{
			boolean forceref = false;
			System.out.println("Assuming that '" + inp + "' is a variable for now.");
			if (firstWord.charAt(0)=='@')
			{
				System.out.println("isptr!");
				forceref = true;
				firstWord = firstWord.substring(1, firstWord.length());
			}
			
			OVar v = getVar(firstWord);
			if (!v.getECXform().equals("ecx"))
				programCode += "xor ecx, ecx\n";
			programCode += "mov " + v.getECXform() + ", [" + v.asmName + "]\n";
			if (forceref)
				programCode += "mov ecx, [ecx]\t; ptr\n";
		}
	}
	public static boolean isVar(String commonName)
	{
		return (getVar(commonName)!=null);
	}
	public static boolean isClassName(String inp)
	{
		return (classes.get(inp)!=null);
	}
	public static OVar getLinkedTypeFromLib(String className, String varName)
	{
		try
		{
			return classes.get(className).linkedOVars.get(varName);
		}
		catch(Exception e){e.printStackTrace();}
		return new OVar("NoClassFound", new String[0], "NoClassFound");
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
		boolean bToLinked = false;
		while (!levelStor.isEmpty())
			level.push(levelStor.pop());
		
		if (commonName.equals("this") && v==null)
		{
			System.out.println("Handle 'this'");
			return new OVar(currentClass.name, new String[0], "ebx");
		}
		if (contains(commonName, ".") && gvarsubs == 0)
		{
			String currentAdd = "";
			String uLevelType = "";
			String[] substrs = commonName.split("\\Q.\\E");
			trimArray(substrs);
			boolean gsubs = false;
			boolean isFirstSub = true;
			for (String s : substrs)
			{
				OVar vs = getVar(s);
				if (gsubs)
				{
					//System.out.println("need to grab subvar: Window$." + s);
					String offs = uLevelType + "." + s;
					String cName = s;	// ???
					gvarsubs++;
					OVar offsvar = getLinkedTypeFromLib(uLevelType, s);
					String type = offsvar.type;
					gvarsubs--;
					if (!type.equals("NoClassFound"))
					{
						System.err.println("Found its class: " + type);
					}
					System.out.println("subvar to grab is '" + cName + "' (" + type + ") [" + offs + "]");
					if (type.equals("NoClassFound"))
					{
						throw new RuntimeException("Could not determine the type of var '" + s + "'");
						//bToLinked = true;
						//break;
					}
					/*if (isFirstSub)
					{
						isFirstSub = false;
						programCode += "push edx\n";
						System.out.println("k.");
					}*/
					programCode += "add dl, " + offs + "\n";
					programCode += "mov eax, edx\n";
					programCode += "mov edx, [edx]\n";
				}
				else if (vs != null)
				{
					gsubs = true;
					System.out.println("\tisMainVar: " + s);
					//currentAdd = "";
					if (vs instanceof OPrimitive)
						throw new RuntimeException("... well that happened: " + commonName + " is invalid (" + s + ") is primitive and cannot have subvars)");
					System.out.println("\t\tType: " + vs.type);
					uLevelType = vs.type;
					// need to grab the subvar here!
					programCode += "push edx\t; Begin getting subvar\n";
					programCode += "mov edx, [" + vs.asmName + "]\n";
				}
				else
				{
					System.out.println("\tAdd static to path: " + s);
					currentAdd += s + ".";
				}
				if (bToLinked)
					break;
			}
			if (!bToLinked)
			{
				//throw new RuntimeException("Finish");
				programCode += "pop edx\t; End getting subvar\n";
				return new OVar(uLevelType, new String[0], "eax");
			}
		}
		if (v==null)	// check libraries
			v = libvars.get(commonName);
		if (v==null && contains(commonName, "."))	// check linked ovars
		{
			System.out.println("Check for linked...");
			String[] splits = commonName.split("\\Q.\\E");
			String stor = "";
			for (int i = 0; i < splits.length-1; i++)
				stor += splits[i] + ".";
			stor = stor.substring(0, stor.length()-1).trim();
			
			OVar var = getVar(stor);
			OClass claz = classes.get(var.type);
			System.out.println("Class '" + claz.name + "': " + claz.linkedOVars.entrySet().size() + " entries.");
			
			for (Entry e : claz.linkedOVars.entrySet())
			{
				System.out.println("'" + (String)e.getKey() + "' vs '" + splits[splits.length-1] + "'");
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
		if (i+1==e)
		{
			System.out.println("[ParseArgs] No arguments found.");
			return;
		}
		String[] args = smartSplit(argstr, ',');
		System.out.println("[ParseArgs] Found " + args.length + " args");
		trimArray(args);
		for (String arg : args)
		{
			if (arg.charAt(arg.length()-1)==',')
				arg = arg.substring(0, arg.length()-1);
			System.out.println("[ParseArgs] Parsing found arg: '" + arg + "'");
			parse(arg);
			programCode += "push ecx\n";
		}
	}
	static OVar createString(String inp)
	{
		//System.err.println("***\nFIX THIS\n***");
		System.out.println("[MakeString] Handed '" + inp + "'");
		String asmName = level.peek().asmName + ".string_" + level.peek().stringLevel++;
		String type = "String";
		String name = inp.trim();
		String dat_asmName = asmName + "_data";
		String dat_type = "byte";
		String dat_name = dat_asmName + "@INVALID:STR_REF";
		
		OVar dat = new OPrimitive(dat_type, new String[0], dat_asmName);
		dat.ival = inp + ", 0";
		level.peek().vars.put(dat_name, dat);
		OVar var = new OVar(type, new String[0], asmName);
		var.ival = dat_asmName;
		level.peek().vars.put(name, var);
		
		return var;
		
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
						notGood = false;
				if (notGood)
					return false;
			}
		return true;
	}
	static boolean isComparison(String s)	// should check to see if it is in quotes or not first
	{
		String[] words = s.split(" ");
		trimArray(words);
		if (words.length < 3)
			return false;
		if (contains(Types.comparator, words[1]))
			return true;
		return false;
	}
	static void handleComparison(String s)
	{
		String[] words = s.split(" ");
		trimArray(words);
		String comparator = words[1];
		String obj1 = words[0];
		String obj2 = words[2];
		programCode += "push edx\n";
		parse(obj1);
		programCode += "mov edx, ecx\n";
		parse(obj2);
		programCode += "cmp edx, ecx\npop edx\n";
		String st = "";
		switch (comparator)
		{
			case "!=" :
				st = "jne";
				break;
			case "==" :
				st = "je";
				break;
			case ">=" :
				st = "jge";
				break;
			case "<=" :
				st = "jle";
				break;
			case ">" :
				st = "jg";
				break;
			case "<" :
				st = "jl";
				break;
			default :
				System.exit(0);
		}
		String name = currentClass.name + ".$comp_" + lineNumber;
		programCode += st + " " + name + ".true\n";	// should probably not use lineNumber...
		programCode += "mov cl, 0x0\njmp " + name + ".done\n";
		programCode += name + ".true :\nmov cl, 0xFF\n" + name + ".done :\n\n";
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
	
	static String[] smartSplit(String s, String match)
	{
		boolean inQuotes = false;
		int lastSplit = -match.length();
		ArrayList<String> found = new ArrayList<String>();
		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			if (c=='\"')
				inQuotes = !inQuotes;
			if (!inQuotes && c==match.charAt(0))
			{
				boolean cont = true;
				for (int z = 0; z < match.length(); z++)
					if (s.charAt(z+i)=='\"' || s.charAt(z+i)!=match.charAt(z))
						cont = false;
				if (cont)
				{
					found.add(s.substring(lastSplit+match.length(), i));
					lastSplit = i;
				}
			}
		}
		if (lastSplit != s.length())
			found.add(s.substring(lastSplit+match.length(), s.length()));
		String[] ret = new String[found.size()];
		for (int i = 0; i < ret.length; i++)
		{
			ret[i] = found.get(i);
		}
		trimArray(ret);
		return ret;	
	}
	
	
	public static void copySingleLib(File asmLibFile)
	{	
		String inp = "";
		try
		{
			Scanner sc = new Scanner(asmLibFile);
			inp = sc.nextLine();
			while (inp != null)
			{
				inp += sc.nextLine() + "\n";
			}
		}
		catch (Exception e){e.printStackTrace();}
		finally
		{
			closingCode += "; *** LIB IMPORT '" + asmLibFile.getName().replaceAll(".asm", "") + "' ***\n" + inp + "\n";
		}
	}
	public static void importLib(String name)
	{
		try{
			name = name.substring(1, name.length()-1);
		System.out.println("Attempt to load lib '" + name + "'");
		for (String dir : libdirs)
		{
			System.out.println("\tSearch for lib in: '" + new File(dir).getAbsolutePath() + "' [" + (new File(dir).exists()) + "]");
			if (new File(dir).exists())
			for (File sub : new File(dir).listFiles())
			{
				File varfile = new File(sub.getAbsolutePath().replaceAll(".asm", ".varlist"));
				if (!sub.isDirectory() && sub.getName().equalsIgnoreCase(name + ".asm") && varfile.exists())
				{
					System.out.println("\t\tLoading lib from: '" + sub.getAbsolutePath() + "'");
					loadSingleLib(varfile);
					copySingleLib(sub);
					return;
				}
			}
		}
		}
		catch (Exception e)
		{
			System.out.println("\tFail.");
		}
		throw new RuntimeException("Failed to load lib: '" + name + "'");
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
		try
		{
		OClass claz = new OClass(className, null);
		claz.linkedOVars = new HashMap<String, OVar>();
		for (Entry o : linkedVars.entrySet())
		{
			if (o.getValue() instanceof SerializableOPrimitive)
				claz.linkedOVars.put((String)o.getKey(), new OPrimitive((SerializableOPrimitive)o.getValue()));
			else
				claz.linkedOVars.put((String)o.getKey(), (OVar)o.getValue());
			System.out.println("Record linked var '" + o.getKey() + "'");
		}
		classes.put(className, claz);
		}catch(Exception e){e.printStackTrace();}
	}
}
class Types
{
	static String[] primitive = {"int", "int_s", "bool", "byte", "char", "String"};	// String and this are special cases
	static String[] special = {"null", "void"};
	static String[] defined = {"Window", "Image", "Buffer", "Pointer", "Color"};
	static String[] modifier = {"linked", "capped", "final", "invis"};
	static String[] block = {"if", "for", "while"};
	static String[] comparator = {"!=", "<=", ">=", ">", "<", "==", "seq"};
	static String[] swappable = {"$LinkedClassSize"};
}
class SystemCall
{
	static boolean inited = false;
	static Map<String, String> callList = new HashMap<String, String>();
	public static void init()
	{
		callList.put("System.GetValue",	"0x0001");
		callList.put("Console.Print",		"0x0100");
		callList.put("Console.PrintLine",	"0x0101");
		callList.put("Console.PrintHex",	"0x0102");
		callList.put("Console.Newline",		"0x0103");
		callList.put("Console.Clear",		"0x0104");
		callList.put("Console.PrintChar",	"0x0105");
		callList.put("Console.GetWindow",   "0x0106");
		callList.put("Dolphin.RegisterWindow",		"0x0200");	// unimplemented
		callList.put("Dolphin.UnregisterWindow",	"0x0201");	// unimplemented
		callList.put("Dolphin.CreateWindow",		"0x0202");
		callList.put("Debug.Log",		"0x0301");	// unimplemented
		callList.put("Debug.LogHex",	"0x0302");	// unimplemented
		callList.put("Keyboard.AddKeypressHandler",		"0x0401");	// unimplemented
		callList.put("Keyboard.RemoveKeypressHandler",	"0x0402");	// unimplemented
		callList.put("Keyboard.IsKeyPressed",			"0x0403");	// unimplemented
		callList.put("Keyboard.HasEvent",				"0x0404");
		callList.put("Keyboard.TakeEvent",				"0x0405");
		callList.put("Program.Exit",	"0x0500");	// unimplemented
		callList.put("Program.Alloc",	"0x0501");
		callList.put("Program.Ealloc",	"0x0502");
		callList.put("Mouse.GetX",						"0x0600");	// unimplemented
		callList.put("Mouse.GetY",						"0x0601");	// unimplemented
		callList.put("Mouse.IsButtonPressed",			"0x0602");	// unimplemented
		callList.put("Mouse.AddButtonpressListener",	"0x0603");	// unimplemented
		callList.put("Mouse.RemoveButtonpressListener",	"0x0604");	// unimplemented
		callList.put("Time.GetSecond",	"0x0701");	// unimplemented
		callList.put("Time.GetMinute",	"0x0702");	// unimplemented
		callList.put("Time.GetHour",	"0x0703");	// unimplemented
		callList.put("Time.GetYear",	"0x0704");	// unimplemented
		callList.put("Minnow.Open",	"0x0801");	// unimplemented
		inited = true;
	}
	public static String lookup(String commonName)
	{
		if (!inited)
			init();
		return callList.get(commonName);
	}
}
class SystemConstant
{
	static boolean inited = false;
	static Map<String, String> valList = new HashMap<String, String>();
	public static void init()
	{
		valList.put("VIDEO_MODE", "0x0");
		valList.put("SCREEN_BYTEWIDTH", "0x1");
		valList.put("SCREEN_HEIGHT", "0x2");
		valList.put("SCREEN_BPP", "0x3");
		valList.put("RAM_TOTAL", "0x4");
		valList.put("RAM_USED", "0x5");
		valList.put("CLOCK_TICS", "0x6");
		valList.put("GRAPHICSCARD_NAME", "0x7");
		valList.put("PROCESSCOUNT", "0x8");
	}
	public static String lookup(String commonName)
	{
		if (!inited)
			init();
		return valList.get(commonName);
	}
}
class OVar implements Serializable
{
	String type;
	String asmName;
	String ival = "0x0";
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
		Compiler.programCode += name + ".$FILE_END :";
		try{
			ObjectOutputStream varList = new ObjectOutputStream(new FileOutputStream(Compiler.f.getAbsolutePath().replaceAll(".orca", ".varlist")));
			System.out.println("writing varList to: " + Compiler.f.getAbsolutePath().replaceAll(".orca", ".varlist"));
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
	final String RETURN_CODE;
	static int returnedOn;
	ArrayList<OVar> params = new ArrayList<OVar>();
	Map<String, OVar> subStructureVars = new HashMap<String, OVar>();
	public Function(OClass claz, String name, String[] mods, String returnType, String[] params, boolean linked)
	{
		this.claz = claz;
		this.name = name;
		this.mods = mods;
		this.linked = linked;
		this.asmName = Compiler.currentClass.name + "." + name;
		Compiler.level.push(this);
		if (!params[0].equals("null)"))
			for (String p : params)
			{
				p = p.substring(0, p.length()-1);
				String[] pmods = Compiler.smartSplit(p, ' ');
				Compiler.trimArray(pmods);
				OVar v = Compiler.createVar(pmods, p);
				this.params.add(v);
			}
		RETURN_CODE = "pop edx\npop ebx\npop eax\npush dword [" + Compiler.currentClass.name + "." + name + ".returnVal]\nret\n";
	}
	public void open()
	{
		Compiler.programCode += claz.name + "." + name + ": \n";
		Compiler.programCode += "pop dword [" + Compiler.currentClass.name + "." + name + ".returnVal]\n";
		for (int w = params.size()-1; w >= 0; w--)
		{
			OVar v = params.get(w);
			String sizeSpec = "dword";
			if (v.refSize == 1)
				sizeSpec = "byte";
			else if (v.refSize == 2)
				sizeSpec = "word";
			Compiler.programCode += "pop ecx\n";
			Compiler.programCode += "mov [" + v.asmName + "], " + v.getECXform() + "\n";
		}
		Compiler.programCode += "push eax\npush ebx\npush edx\n";
		Compiler.currentFunction = this;
	}
	public void close()
	{
		if (returnedOn != Compiler.lastRealLine)
			Compiler.programCode += RETURN_CODE;
		Compiler.programCode += "\t;Vars:\n";
		for (Entry e : vars.entrySet())
			Compiler.declareVar((OVar)e.getValue());
		for (Entry e : subStructureVars.entrySet())
			Compiler.declareVar((OVar)e.getValue());
		Compiler.programCode += Compiler.currentClass.name + "." + name + ".returnVal:\n\tdd 0x0\n";
		Compiler.programCode += "\n\n";
	}
}
abstract class Structure
{
	Map<String, OVar> vars = new HashMap<String, OVar>();
	String asmName;
	int stringLevel = 0;
	public abstract void open();
	public abstract void close();
}
class O_If extends Structure
{
	public static int loopNum = 0;
	static String condition;
	public O_If(String inp)
	{
		asmName = Compiler.currentClass.name + ".$loop_if." + loopNum;
		loopNum++;
		// parse inp for the needed values
		condition = inp.trim().substring("if".length(), inp.trim().length()).trim();
	}
	public void open()
	{
		Compiler.level.add(this);
		Compiler.parse(condition);
		Compiler.level.pop();
		Compiler.programCode += "cmp cl, 0xFF\n\tjne " + asmName + "_close\n";
	}
	public void close()
	{
		for (Entry e : vars.entrySet())
			Compiler.currentFunction.subStructureVars.put((String)e.getKey(), (OVar)e.getValue());
		Compiler.programCode += asmName + "_close :\n\n";
	}
}
class O_For extends Structure
{
	public static int loopNum = 0;
	static String firstParam, secondParam, thirdParam;
	public O_For(String inp)
	{
		asmName = Compiler.currentClass.name + ".$loop_for." + loopNum;
		loopNum++;
		// parse inp for the needed values
		String rems = inp.trim().substring("for".length(), inp.trim().length()).trim();
		String[] arr = Compiler.smartSplit(rems, ':');
		firstParam = arr[0];
		secondParam = arr[1];
		thirdParam = arr[2];
	}
	public void open()
	{
		// code goes here
		Compiler.level.add(this);
		Compiler.parse(firstParam);
		Compiler.level.pop();
		Compiler.programCode += asmName + "_open :\n";
	}
	public void close()
	{
		// code goes here
		Compiler.level.add(this);
		Compiler.parse(thirdParam);
		Compiler.parse(secondParam);
		for (Entry e : vars.entrySet())
			Compiler.currentFunction.subStructureVars.put((String)e.getKey(), (OVar)e.getValue());
		Compiler.level.pop();
		Compiler.programCode += "cmp cl, 0xFF\n\tje " + asmName + "_open\n\n";
	}
}
class O_While extends Structure
{
	public static int loopNum = 0;
	static String condition;
	public O_While(String inp)
	{
		asmName = Compiler.currentClass.name + ".$loop_while." + loopNum;
		loopNum++;
		condition = inp.trim().substring("while".length(), inp.trim().length()).trim();
		System.out.println("[While] '" + condition + "'");
		// parse inp for the needed values
	}
	public void open()
	{
		// code goes here
		Compiler.level.add(this);
		Compiler.programCode += asmName + "_open :\n";
		Compiler.parse(condition);
		for (Entry e : vars.entrySet())
			Compiler.currentFunction.subStructureVars.put((String)e.getKey(), (OVar)e.getValue());
		Compiler.level.pop();
		Compiler.programCode += "cmp cl, 0xFF\n\tjne " + asmName + "_end\n";
	}
	public void close()
	{
		// code goes here
		Compiler.programCode += "\tjmp " + asmName + "_open\n";
		Compiler.programCode += asmName + "_end :\n";
	}
}