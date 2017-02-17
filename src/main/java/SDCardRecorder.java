import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple utility for copying WAV and MP3 sound files to an SD Card used in Arduino DFPlayer Mini module, and
 * naming/organizing them for optimal performance.
 * <p>
 * The files will be copied respecting their natural (sort-by-name) order. It also creates an .H file with definitions
 * of sound file indexes and their original names in the comment. That file will be written to SD as 9999.H, just for
 * your reference only - it does not affect the sound card functionalities in any way
 * <p>
 * The utility also formats the SD prior to copying, and names the files as 0000.EEE (0000 being order number and EEE
 * the original file extension.
 * 
 * @author JonnieZG
 */
public class SDCardRecorder {
	/**
	 * List of #define statements.
	 */
	static List<String> defs = new ArrayList<>();

	static int targetIndex = 0;
	static int folderNumber = 0;

	static StringBuilder header = new StringBuilder();

	static void process(File dir, File target) throws Exception {
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException("Directory not found: " + dir);
		}
		TreeSet<File> files = new TreeSet<>();
		files.addAll(Arrays.asList(dir.listFiles()));
		for (File file : files) {
			if (file.isDirectory()) {
				folderNumber++;
				process(file, target);
			} else {
				String fileName = file.getName().toLowerCase();
				if (fileName.endsWith(".wav") || fileName.endsWith(".mp3")) {

					++targetIndex;
					copy(file, target, targetIndex);

					// Create #define name and suffix it with a number if a same one already exists
					String defBase = defName(file);
					String def = defBase;
					int idx = 0;
					while (defs.contains(def)) {
						def = defBase + "_" + (++idx);
					}

					// Create #define line with the original file name in the comment
					defs.add(def);
					String defLine = MessageFormat.format("#define {0}\t\t{1,number,#} /* {2}/{3} */", def, targetIndex,
							file.getParentFile().getName(), file.getName());

					System.out.println(defLine);
					header.append(defLine).append("\n");
				} else {
					System.out.println("Skipping invalid file: " + file);
				}
			}
		}
	}

	static final Pattern PTRN_FILE_NAME = Pattern.compile("([\\d-_\\s]*[- ])?(.*?)(\\..{0,3})?");

	/**
	 * Removes file/directory prefix-number.
	 * 
	 * @param str
	 * @return
	 */
	static String stripNumber(String str) {
		Matcher m = PTRN_FILE_NAME.matcher(str);
		if (m.matches())
			return m.group(2);
		else
			return str;
	}

	/**
	 * Creates #define name for specified file. Names will be formed as
	 * <code>SND_<i>modifiedFolderName</i>_<i>modifiedFileName</i></code>. Modified folder and file names are C-friendly
	 * capitalized strings with underscores replacing spaces and other "identifier-unfriendly" characters.
	 * 
	 * @param file
	 * @return
	 */
	static String defName(File file) {
		String str = "SND_" + (folderName(file) + "_" + fileName(file)).toUpperCase().replaceAll("[ \\-()\\[\\]]", "_");
		return str.replaceAll("_+", "_");
	}

	static String folderName(File file) {
		String str = stripNumber(file.getParentFile().getName());
		if (str.isEmpty())
			return "DIR" + folderNumber;
		else
			return str;
	}

	static String fileName(File file) {
		Matcher m = PTRN_FILE_NAME.matcher(file.getName());
		if (m.matches()) {
			return m.group(2);
		} else {
			return file.getName();
		}
	}

	static String extension(File file) {
		int idx = file.getName().lastIndexOf(".");
		if (idx < 0) {
			return "";
		}
		return file.getName().substring(idx + 1);
	}

	/**
	 * Copy file to specified SD target. The source file will be automatically renamed to match the target index.
	 * 
	 * @param file
	 * @param target
	 * @param targetIndex
	 * @throws IOException
	 */
	static void copy(File file, File target, int targetIndex) throws IOException {
		if (!target.exists()) {
			// Create target dir if missing
			target.mkdirs();
		}

		// Create target SD file name
		String str = "{0,number,0000}.{4}";
		File sdFile = new File(target, MessageFormat.format(str, targetIndex, folderName(file), fileName(file),
				extension(file), extension(file).toUpperCase()));

		// Copy the file payload
		byte[] buff = new byte[65536];
		FileInputStream fis = new FileInputStream(file);
		FileOutputStream fos = new FileOutputStream(sdFile);

		int len;
		while ((len = fis.read(buff)) > 0) {
			fos.write(buff, 0, len);
		}
		fis.close();
		fos.close();
	}

	/**
	 * Recursively delete given file or folder.
	 * 
	 * @param file
	 */
	static void deleteRecursively(File file) {
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				deleteRecursively(f);
			}
		} else {
			file.delete();
		}
	}

	/**
	 * Formatting a given target. If a target is a drive - it will be formatted. If it is a directory - its contents
	 * will be deleted.
	 * <p>
	 * This implementation is DOS-specific, but it can be easily modified for *Nix platforms.
	 * 
	 * @param target
	 * @throws IOException
	 */
	static void format(File target) throws IOException {

		if (target.getAbsolutePath().length() == 3) {
			char drive = target.getAbsolutePath().toUpperCase().charAt(0);

			if (drive == 'C' || drive == 'D')
				throw new IllegalArgumentException("You probably don't want to format " + drive);

			// Format only if target is an absolute drive address
			ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "format " + drive + ": /Q /Y");
			builder.redirectErrorStream(true);
			Process p = builder.start();
			BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while (true) {
				line = r.readLine();
				if (line == null) {
					break;
				}
				System.out.println(line);
			}
		} else {
			if (target.isFile()) {
				throw new IllegalArgumentException("Target should be a directory");
			}
			if (!target.exists()) {
				target.mkdirs();
			}
			for (File f : target.listFiles()) {
				deleteRecursively(f);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.out.println("You must specify source and target directories in the command-line!");
			System.exit(1);
		}

		File source = new File(args[0]);
		File target = new File(args[1]);

		System.out.println("Source: " + source);
		System.out.println("Target: " + target.getAbsolutePath());

		format(target);
		process(source, target);

		// Write the header file
		File sdFile = new File(target, "9999.H");
		FileOutputStream fos = new FileOutputStream(sdFile);
		fos.write(header.toString().getBytes());
		fos.close();

		System.out.println("DONE");
	}

}
