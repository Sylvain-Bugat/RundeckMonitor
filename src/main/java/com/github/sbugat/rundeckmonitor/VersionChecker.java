package com.github.sbugat.rundeckmonitor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JOptionPane;
import javax.xml.bind.DatatypeConverter;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.ContentsService;
import org.eclipse.egit.github.core.service.DataService;
import org.eclipse.egit.github.core.service.RepositoryService;

/**
 * Simple generic version checker on GitHub, inpect target jar and local jar build date to determinated if an update is available
 * If one is found, download the full jar and replace the original jar via a double restart
 *
 * The checker use an independant thread to check and download the file.
 * The main thread have to check is the download is done and order to restart the program
 *
 * @author Sylvain Bugat
 *
 */
public class VersionChecker implements Runnable{

	private static final String JAR_EXTENSION = ".jar"; //$NON-NLS-1$
	private static final String TMP_EXTENSION = ".tmp"; //$NON-NLS-1$
	private static final String WINDOWS_EXE_EXTENSION = ".exe"; //$NON-NLS-1$
	private static final String JAVA_HOME_PROPERTY = "java.home"; //$NON-NLS-1$
	private static final String OS_NAME_PROPERTY = "os.name"; //$NON-NLS-1$
	private static final String WINDOWS_OS_NAME = "windows"; //$NON-NLS-1$

	private static final String BIN_DIRECTORY_AND_JAVA = "bin" + FileSystems.getDefault().getSeparator() + "java"; //$NON-NLS-1$

	private static final String JAR_ARGUMENT = "-jar"; //$NON-NLS-1$

	/**Root URL of the GitHub project to update*/
	private final String gitHubUser;
	private final String gitHubRepository;

	/**Name of jar file name without dependencies*/
	//private final String jarFileName;
	/**Name of jar file containind all dependencies*/
	//private final String jarWithDependenciesFileName;

	/**Maven artifact identifier*/
	private final String mavenArtifactId;

	private final String jarWithDependenciesSuffix;

	/**Maven directory where jar files are located*/
	private final String repositoryJarDirectory;

	/**Indicate if the download is completed*/
	private boolean downloadDone;

	private String downloadedJar;

	/**
	 * Initialize the version checker with jar artifact and suffixnames and path to GitHub
	 *
	 * @param mavenArtifactIdArg
	 * @param mavenVersion
	 * @param mavenGroupIdArg
	 * @param jarWithDependenciesSuffixArg
	 */
	public VersionChecker( final String gitHubUserArg, final String gitHubRepositoryArg, final String mavenArtifactIdArg, final String repositoryJarDirectoryArg, final String jarWithDependenciesSuffixArg ) {

		gitHubUser = gitHubUserArg;
		gitHubRepository = gitHubRepositoryArg;

		mavenArtifactId = mavenArtifactIdArg;
		repositoryJarDirectory = repositoryJarDirectoryArg;
		jarWithDependenciesSuffix = jarWithDependenciesSuffixArg;
	}

	/**
	 * Background thread launched to check the version on GitHub
	 */
	@Override
	public void run() {

		final String currentJar = currentJar();

		if( null == currentJar ) {
			return;
		}

		try {

			final GitHubClient gitHubClient = new GitHubClient();

			final RepositoryService rs = new RepositoryService( gitHubClient );
			final Repository repository = rs.getRepository( gitHubUser, gitHubRepository );

			final ContentsService contentsService = new ContentsService( gitHubClient );

			RepositoryContents jarWithDependenciesRepositoryContents = null;
			for( final RepositoryContents repositoryContents : contentsService.getContents(repository, repositoryJarDirectory ) ) {

				if( repositoryContents.getName().startsWith( mavenArtifactId ) && repositoryContents.getName().endsWith( jarWithDependenciesSuffix + JAR_EXTENSION ) ) {

					jarWithDependenciesRepositoryContents = repositoryContents;
				}
			}

			if( null == jarWithDependenciesRepositoryContents ) {
				return;
			}

			if( jarWithDependenciesRepositoryContents.getName().compareTo( currentJar ) > 0 ) {

				final int confirmDialogChoice = JOptionPane.showConfirmDialog( null, "An update is available, download it? (" + jarWithDependenciesRepositoryContents.getSize() / 1_048_576 + "MB)", "Rundeck Monitor update found!", JOptionPane.YES_NO_OPTION ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				if( JOptionPane.YES_OPTION == confirmDialogChoice ) {

					final DataService dataService = new DataService( gitHubClient );
					downloadFile( new ByteArrayInputStream( DatatypeConverter.parseBase64Binary( dataService.getBlob( repository, jarWithDependenciesRepositoryContents.getSha() ).getContent() ) ), jarWithDependenciesRepositoryContents.getName() + TMP_EXTENSION );
					Files.move( Paths.get( jarWithDependenciesRepositoryContents.getName() + TMP_EXTENSION ), Paths.get( jarWithDependenciesRepositoryContents.getName() ) );

					downloadedJar = jarWithDependenciesRepositoryContents.getName();
					downloadDone = true;
				}
			}
		}
		catch( final Exception e) {

			//Ignore any error during update process
			//Just delete the temporary file
			cleanOldAndTemporaryJar();
		}
	}

	public boolean restart() {

		if( Files.exists( Paths.get( downloadedJar ) ) ) {

			try {

				final ProcessBuilder processBuilder = new ProcessBuilder( getJavaExecutable(), JAR_ARGUMENT, downloadedJar );
				processBuilder.start();

				return true;
			}
			catch( final IOException e ) {

				//Ignore any error during restart process
			}
		}

		return false;
	}

	public boolean isDownloadDone() {

		return downloadDone;
	}

	public void cleanOldAndTemporaryJar() {

		String currentJar = currentJar();

		try( final DirectoryStream<Path> directoryStream = Files.newDirectoryStream( Paths.get( "." ) ) ) {

			for( final Path path : directoryStream ) {

				final String fileName = path.getFileName().toString();
				if( fileName.startsWith( mavenArtifactId) ) {

					if( fileName.endsWith( jarWithDependenciesSuffix + JAR_EXTENSION ) && currentJar.compareTo( fileName ) > 0 ) {

						deleteJar( path );
					}
					else if( fileName.endsWith( jarWithDependenciesSuffix + JAR_EXTENSION + TMP_EXTENSION ) ) {

						deleteJar( path );
					}
				}
			}
		}
		catch ( final IOException ex ) {
			//Ignore any error during the delete process
		}
	}

	private static void deleteJar( final Path jarFileToDelete ) {

		if( Files.exists( jarFileToDelete ) ) {

			try {
				Files.delete( jarFileToDelete );
			}
			catch ( final IOException e ) {

				//Ignore any error during the delete process
			}
		}
	}

	private String currentJar() {

		String currentJar = null;
		try( final DirectoryStream<Path> directoryStream = Files.newDirectoryStream( Paths.get( "." ) ) ) {

			for( final Path path : directoryStream ) {

				final String fileName = path.getFileName().toString();
				if( fileName.startsWith( mavenArtifactId) && fileName.endsWith( jarWithDependenciesSuffix + JAR_EXTENSION ) ) {

					if( null == currentJar || currentJar.compareTo( fileName ) < 0 ) {
						currentJar = fileName;
					}
				}
			}
		}
		catch ( final IOException ex ) {
			//Ignore any error during the process
			currentJar = null;
		}

		return null;
	}

	/**
	 * Download a file/URL and write it to a destination file
	 *
	 * @param inputStream source stream
	 * @param destinationFile destination file
	 * @throws IOException
	 */
	private static void downloadFile( final InputStream inputStream, final String destinationFile ) throws IOException {

		Files.copy( inputStream, Paths.get( destinationFile ) );
	}

	/**
	 * Get the java executable
	 *
	 * @return absolte path to the java executable
	 * @throws NoSuchFileException if the java executable is not found
	 */
	private static String getJavaExecutable() throws NoSuchFileException {

		final String javaDirectory = System.getProperty( JAVA_HOME_PROPERTY );

		if ( javaDirectory == null ) {
			throw new IllegalStateException( JAVA_HOME_PROPERTY );
		}

		final String javaExecutableFilePath;
		//Add .exe extension on Windows OS
		if ( isWindows() ) {
			javaExecutableFilePath = javaDirectory + FileSystems.getDefault().getSeparator() + BIN_DIRECTORY_AND_JAVA + WINDOWS_EXE_EXTENSION;
		}
		else {
			javaExecutableFilePath = javaDirectory + FileSystems.getDefault().getSeparator() + BIN_DIRECTORY_AND_JAVA;
		}

		//Check if the executable exists and is executable
		final Path javaExecutablePath = Paths.get( javaExecutableFilePath );
		if ( ! Files.exists( javaExecutablePath ) && Files.isExecutable( javaExecutablePath ) ) {
			throw new NoSuchFileException( javaExecutableFilePath );
		}

		return javaExecutableFilePath;
	}

	/**
	 * Check if the operating system is a windows based on a property, otherwise it's a Linux/Mac-OS
	 *
	 * @return true if the OS is a windows*
	 */
	private static boolean isWindows() {

		final String operatingSystem = System.getProperty( OS_NAME_PROPERTY );

		if( null == operatingSystem ) {
			return false;
		}

		return operatingSystem.toLowerCase().startsWith( WINDOWS_OS_NAME );
	}
}
