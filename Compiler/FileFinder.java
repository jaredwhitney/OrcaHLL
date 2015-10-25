import javax.swing.*;
import java.io.*;
import java.util.*;

public class FileFinder
{
	private String title;
	private String name;
	public static boolean needsForceClose = false;
	public FileFinder(String title, String fileName)
	{
		this.title  = title;
		name = fileName;
	}
	/**
	*
	*	Returns the location of <code>NOTIFY.exe</code>.
	*	
	*	Location is stored in 'exe.path', and is regenerated whenever <code>NOTIFY.exe</code> is not found.
	*
	*	@return path to <code>NOTIFY.exe</code>
	*
	**/
	public String lookForFile() throws Exception{
		File store = new File(name.replaceAll("\\Q.\\E", "_") + ".path");
		if (store.exists()) {
			Scanner s = new Scanner(store);
			String path = s.nextLine();
			File exeFile = new File(path);
			if (exeFile.exists())
				return path;
		}
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception e){}
		JFrame frame = new JFrame();
		JProgressBar workingNoti = new JProgressBar();
		workingNoti.setIndeterminate(true);
		JPanel pan = new JPanel();
		pan.add(new JLabel("Looking for " + title + "..."));
		pan.add(workingNoti);
		frame.add(pan);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(3);
		frame.setTitle("File Finder: " + name);
		frame.setVisible(true);
		
		File root = new File("C:\\");
		String installDir = searchForRep(root, name);
		
		if (installDir.equals("<NONE>")) {
			System.out.println("Unable to locate '" + name + "'");
			System.exit(0);
		}
		
		store.createNewFile();
		PrintWriter storeOut = new PrintWriter(new FileOutputStream(store));
		storeOut.println(installDir);
		storeOut.close();
		
		frame.setVisible(false);
		needsForceClose = true;
		return installDir;
	}
	
	/**
	*
	*	Searches the specified directory and all of its subdirectories until the specified file is found.
	*
	*	@param f		the directory to search
	*	@param search	the file name to search for
	*
	*	@return the absolute path of the first match found or <CODE>"<NONE>"</CODE> if no such file exists
	*
	**/
	public String searchForRep (File f, String search) {
		Stack<String> files = new Stack<String>();
		files.push(f.getAbsolutePath());
		while (!files.isEmpty()) {
			File currentFile = new File(files.pop());
			if (currentFile.isDirectory() && !currentFile.getName().equalsIgnoreCase("debug")) {
				if (currentFile.list()!=null) {
					for (String sub : currentFile.list()) {
						files.add(currentFile.getAbsolutePath() + "\\" + sub);
					}
				}
			}
			else if (currentFile.getName().equals(search)) {
				return currentFile.getAbsolutePath();
			}
		}
		return "<NONE>";
	}
}