package com.github.sbugat.rundeckmonitor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JOptionPane;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryTag;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import com.github.sbugat.rundeckmonitor.tools.EnvironmentTools;

/**
 * Simple generic version checker on GitHub, inpect target jar and local jar build date to determinated if an update is available. If one is found, download the full jar and replace the original jar via a double restart.
 *
 * The checker use an independant thread to check and download the file. The main thread have to check is the download is done and order to restart the program.
 *
 * @author Sylvain Bugat
 *
 */
public final class VersionChecker implements Runnable {

	/** SLF4J XLogger. */
	private static final XLogger LOG = XLoggerFactory.getXLogger(VersionChecker.class);

	/** Precalculated one megabyte for indicating download size. */
	private static final int ONE_MEGABYTE = 1_024 * 1_024;

	/** Jar extension. */
	private static final String JAR_EXTENSION = ".jar"; //$NON-NLS-1$
	/** Tmp extension. */
	private static final String TMP_EXTENSION = ".tmp"; //$NON-NLS-1$
	/** Executable extension on Windows. */
	private static final String WINDOWS_EXE_EXTENSION = ".exe"; //$NON-NLS-1$
	/** Java home property. */
	private static final String JAVA_HOME_PROPERTY = "java.home"; //$NON-NLS-1$
	/** Target directory in zipball releases. */
	private static final String TARGET_DIRECTORY = "target"; //$NON-NLS-1$

	/** bin subdirectory and java executable. */
	private static final String BIN_DIRECTORY_AND_JAVA = "bin" + FileSystems.getDefault().getSeparator() + "java"; //$NON-NLS-1$ //$NON-NLS-2$

	/** Jar argument for java reloading. */
	private static final String JAR_ARGUMENT = "-jar"; //$NON-NLS-1$

	/** Root URL of the GitHub project to update. */
	private final String gitHubUser;
	/** GitHub repository. */
	private final String gitHubRepository;

	/** Maven artifact identifier. */
	private final String mavenArtifactId;

	/** Suffix of the full jar including all dependencies. */
	private final String jarWithDependenciesSuffix;

	/** Indicate if the download is completed. */
	private boolean downloadDone;

	/** Indicate if the version checker is disabled. */
	private boolean versionCheckerDisabled;

	/** Name of the downloaded jar. */
	private String downloadedJar;

	/**
	 * Initialize the version checker with jar artifact and suffixnames and path to GitHub.
	 *
	 * @param gitHubUserArg GitHub user
	 * @param gitHubRepositoryArg GitHub repository
	 * @param mavenArtifactIdArg maven artifact id
	 * @param jarWithDependenciesSuffixArg suffix of the full jar including all dependencies
	 */
	public VersionChecker(final String gitHubUserArg, final String gitHubRepositoryArg, final String mavenArtifactIdArg, final String jarWithDependenciesSuffixArg) {

		gitHubUser = gitHubUserArg;
		gitHubRepository = gitHubRepositoryArg;

		mavenArtifactId = mavenArtifactIdArg;
		jarWithDependenciesSuffix = jarWithDependenciesSuffixArg;
	}

	/**
	 * Background thread launched to check the version on GitHub.
	 */
	@Override
	public void run() {

		LOG.entry();

		final String currentJar = currentJar();

		if (null == currentJar) {
			LOG.exit();
			return;
		}

		try {

			final GitHubClient gitHubClient = new GitHubClient();

			final RepositoryService rs = new RepositoryService(gitHubClient);
			final Repository repository = rs.getRepository(gitHubUser, gitHubRepository);

			final String currentVersion = 'v' + currentJar.replaceFirst("^" + mavenArtifactId + '-', "").replaceFirst(jarWithDependenciesSuffix + ".*$", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			RepositoryTag recentRelease = null;
			for (final RepositoryTag tag : rs.getTags(repository)) {

				if (null != recentRelease && tag.getName().compareTo(recentRelease.getName()) > 0) {
					recentRelease = tag;
				}
				else if (tag.getName().compareTo(currentVersion) > 0) {
					recentRelease = tag;
				}
			}

			if (null == recentRelease) {
				LOG.exit();
				return;
			}

			if (!findAndDownloadReleaseJar(recentRelease, true)) {
				findAndDownloadReleaseJar(recentRelease, false);
			}

			LOG.exit();
		}
		catch (final Exception e) {

			// Ignore any error during update process
			// Just delete the temporary file
			cleanOldAndTemporaryJar();
			LOG.exit(e);
		}
	}

	/**
	 * Find a jar in release and if it is a newer version, ask to download it.
	 *
	 * @param release GitHub last release to use
	 * @param withDependenciesSuffix indicate if the jar to download have a dependencies suffix
	 * @return true if a new release jar has been found
	 * @throws IOException in case of reading error
	 */
	private boolean findAndDownloadReleaseJar(final RepositoryTag release, final boolean withDependenciesSuffix) throws IOException {

		LOG.entry(release, withDependenciesSuffix);

		final String jarSuffix;
		if (withDependenciesSuffix) {
			jarSuffix = jarWithDependenciesSuffix;
		}
		else {
			jarSuffix = ""; //$NON-NLS-1$
		}
		try (final InputStream remoteJarInputStream = new URL(release.getZipballUrl()).openStream()) {

			final ZipInputStream zis = new ZipInputStream(remoteJarInputStream);

			ZipEntry entry = zis.getNextEntry();

			while (null != entry) {

				if (entry.getName().matches(".*/" + TARGET_DIRECTORY + '/' + mavenArtifactId + "-[0-9\\.]*" + jarSuffix + JAR_EXTENSION)) { //$NON-NLS-1$ //$NON-NLS-2$

					final Object[] options = { "Yes", "No", "Never ask me again" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					final int confirmDialogChoice = JOptionPane.showOptionDialog(null, "An update is available, download it? (" + entry.getCompressedSize() / ONE_MEGABYTE + "MB)", "Rundeck Monitor update found!", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					if (JOptionPane.YES_OPTION == confirmDialogChoice) {

						final String jarFileBaseName = entry.getName().replaceFirst("^.*/", ""); //$NON-NLS-1$ //$NON-NLS-2$

						downloadFile(zis, jarFileBaseName + TMP_EXTENSION);
						Files.move(Paths.get(jarFileBaseName + TMP_EXTENSION), Paths.get(jarFileBaseName));

						downloadedJar = jarFileBaseName;
						downloadDone = true;
					}
					else if (JOptionPane.CANCEL_OPTION == confirmDialogChoice) {

						versionCheckerDisabled = true;
					}

					LOG.exit(true);
					return true;
				}

				entry = zis.getNextEntry();
			}
		}

		LOG.exit(false);
		return false;
	}

	/**
	 * Restart the RunDeck monitor and use the newer jar file.
	 *
	 * @return true if a java file has been launched on the new jar file
	 */
	public boolean restart() {

		LOG.entry();

		if (Files.exists(Paths.get(downloadedJar))) {

			String javaExecutable = null;
			try {

				javaExecutable = getJavaExecutable();
				final ProcessBuilder processBuilder = new ProcessBuilder(javaExecutable, JAR_ARGUMENT, downloadedJar);
				processBuilder.start();

				LOG.exit(true);
				return true;
			}
			catch (final IOException e) {

				// Ignore any error during restart process
				LOG.error("Error during restarting process {} with arguments: {} {}", javaExecutable, JAR_ARGUMENT, downloadedJar, e); //$NON-NLS-1$
			}
		}

		LOG.exit(false);
		return false;
	}

	/**
	 * Clean the old jar and any existing temporary file.
	 */
	public void cleanOldAndTemporaryJar() {

		LOG.entry();

		final String currentJar = currentJar();

		try (final DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get("."))) { //$NON-NLS-1$

			for (final Path path : directoryStream) {

				final Path fileNamePath = path.getFileName();

				if (null != fileNamePath) {
					final String fileName = fileNamePath.toString();
					if (fileName.startsWith(mavenArtifactId)) {

						if (fileName.endsWith(JAR_EXTENSION) && null != currentJar && currentJar.compareTo(fileName) > 0) {

							deleteJar(path);
						}
						else if (fileName.endsWith(JAR_EXTENSION + TMP_EXTENSION)) {

							deleteJar(path);
						}
					}
				}
			}

			LOG.exit();
		}
		catch (final IOException e) {
			// Ignore any error during the delete process
			LOG.exit(e);
		}
	}

	/**
	 * Delete a jar file.
	 *
	 * @param jarFileToDelete file to delete
	 */
	private static void deleteJar(final Path jarFileToDelete) {

		LOG.entry();

		if (Files.exists(jarFileToDelete)) {

			try {
				Files.delete(jarFileToDelete);
				LOG.exit();
			}
			catch (final IOException e) {

				// Ignore any error during the delete process
				LOG.exit(e);
			}
		}
	}

	/**
	 * Return the current executed jar.
	 *
	 * @return the name of the current executed jar
	 */
	private String currentJar() {

		LOG.entry();

		String currentJar = null;
		try (final DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get("."))) { //$NON-NLS-1$

			for (final Path path : directoryStream) {

				final Path fileNamePath = path.getFileName();
				if (null != fileNamePath) {
					final String fileName = fileNamePath.toString();
					if (fileName.startsWith(mavenArtifactId) && fileName.endsWith(JAR_EXTENSION) && (null == currentJar || currentJar.compareTo(fileName) < 0)) {

						currentJar = fileName;
					}
				}
			}
		}
		catch (final IOException e) {
			// Ignore any error during the process
			currentJar = null;
		}

		LOG.exit(currentJar);
		return currentJar;
	}

	/**
	 * Get the download status.
	 *
	 * @return true if the download has been done
	 */
	public boolean isDownloadDone() {

		return downloadDone;
	}

	/**
	 * Enable the version checker.
	 */
	public void resetVersionCheckerDisabled() {

		versionCheckerDisabled = false;
	}

	/**
	 * Get the state of the version checker. True, if the user have disabled it.
	 *
	 * @return true if the version checker is disabled
	 */
	public boolean isversionCheckerDisabled() {

		return versionCheckerDisabled;
	}

	/**
	 * Download a file/URL and write it to a destination file.
	 *
	 * @param inputStream source stream
	 * @param destinationFile destination file
	 * @throws IOException in case of copy error
	 */
	private static void downloadFile(final InputStream inputStream, final String destinationFile) throws IOException {

		Files.copy(inputStream, Paths.get(destinationFile));
	}

	/**
	 * Get the java executable.
	 *
	 * @return absolte path to the java executable
	 * @throws NoSuchFileException if the java executable is not found
	 */
	private static String getJavaExecutable() throws NoSuchFileException {

		LOG.entry();

		final String javaDirectory = System.getProperty(JAVA_HOME_PROPERTY);

		if (javaDirectory == null) {
			throw new IllegalStateException(JAVA_HOME_PROPERTY);
		}

		final String javaExecutableFilePath;
		// Add .exe extension on Windows OS
		if (EnvironmentTools.isWindows()) {
			javaExecutableFilePath = javaDirectory + FileSystems.getDefault().getSeparator() + BIN_DIRECTORY_AND_JAVA + WINDOWS_EXE_EXTENSION;
		}
		else {
			javaExecutableFilePath = javaDirectory + FileSystems.getDefault().getSeparator() + BIN_DIRECTORY_AND_JAVA;
		}

		// Check if the executable exists and is executable
		final Path javaExecutablePath = Paths.get(javaExecutableFilePath);
		if (!Files.exists(javaExecutablePath) || !Files.isExecutable(javaExecutablePath)) {

			final NoSuchFileException exception = new NoSuchFileException(javaExecutableFilePath);
			LOG.exit(exception);
			throw exception;
		}

		LOG.exit(javaExecutableFilePath);
		return javaExecutableFilePath;
	}
}
