package com.github.sbugat.rundeckmonitor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JOptionPane;
import javax.xml.bind.DatatypeConverter;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryContents;
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

	public static final String UPDATE_MARKER_ARGUMENT = "update"; //$NON-NLS-1$

	private static final String MAVEN_META_INF_DATE_COMMENT_FORMAT = "EEE MMM d HH:mm:ss zzz yyyy"; //$NON-NLS-1$
	private static final SimpleDateFormat BUILD_DATE_FORMAT = new SimpleDateFormat( MAVEN_META_INF_DATE_COMMENT_FORMAT, Locale.ENGLISH );

	private static final String GITHUB_MASTER_DIRECTORY = "/blob/master/target/"; //$NON-NLS-1$
	private static final String GITHUB_FULL_FILE_GET_ARGUMENT = "?raw=true"; //$NON-NLS-1$

	private static final String JAR_META_INT_ROOT = "META-INF/maven"; //$NON-NLS-1$
	private static final String JAR_POM_PROPERTIES_FILE_NAME = "pom.properties"; //$NON-NLS-1$

	private static final String JAR_EXTENSION = ".jar"; //$NON-NLS-1$
	private static final String UPDATE_EXTENSION = ".update"; //$NON-NLS-1$
	private static final String TMP_EXTENSION = ".tmp"; //$NON-NLS-1$
	private static final String WINDOWS_EXE_EXTENSION = ".exe"; //$NON-NLS-1$
	private static final String JAVA_HOME_PROPERTY = "java.home"; //$NON-NLS-1$
	private static final String OS_NAME_PROPERTY = "os.name"; //$NON-NLS-1$
	private static final String WINDOWS_OS_NAME = "windows"; //$NON-NLS-1$

	private static final String BIN_DIRECTORY_AND_JAVA = "bin" + FileSystems.getDefault().getSeparator() + "java"; //$NON-NLS-1$

	private static final String JAR_ARGUMENT = "-jar"; //$NON-NLS-1$

	/**Root URL of the GitHub project to update*/
	private final String gitHubProjectRootUrl;

	/**Name of jar file name without dependencies*/
	private final String jarFileName;
	/**Name of jar file containind all dependencies*/
	private final String jarWithDependenciesFileName;

	/**Maven artifact identifier*/
	private final String mavenArtifactId;
	/**Maven artefact group identifier*/
	private final String mavenGroupId;

	private final String jarWithDependenciesSuffix;

	/**Indicate if the download is completed*/
	private boolean downloadDone;

	/**
	 * Initialize the version checker with jar names and path to GitHub
	 *
	 * @param gitHubProjectRootUrlArg
	 * @param mavenArtifactIdArg
	 * @param mavenVersion
	 * @param mavenGroupIdArg
	 * @param jarWithDependenciesSuffixArg
	 */
	public VersionChecker( final String gitHubProjectRootUrlArg, final String mavenArtifactIdArg, final String mavenVersion, final String mavenGroupIdArg, final String jarWithDependenciesSuffixArg ) {

		gitHubProjectRootUrl = gitHubProjectRootUrlArg;
		jarFileName = mavenArtifactIdArg + '-' + mavenVersion + JAR_EXTENSION;
		jarWithDependenciesFileName =  mavenArtifactIdArg + '-' + mavenVersion + jarWithDependenciesSuffixArg + JAR_EXTENSION;

		mavenArtifactId = mavenArtifactIdArg;
		mavenGroupId = mavenGroupIdArg;
		jarWithDependenciesSuffix = jarWithDependenciesSuffixArg;
	}

	/**
	 * Background thread launched to check the version on GitHub
	 */
	@Override
	public void run() {

		try( final InputStream remoteJarInputStream = new URL( gitHubProjectRootUrl + GITHUB_MASTER_DIRECTORY + jarFileName + GITHUB_FULL_FILE_GET_ARGUMENT ).openStream() ) {

			final RepositoryService rs = new RepositoryService();
			final Repository repository = rs.getRepository( "Sylvain-Bugat", "RundeckMonitor" );

			final ContentsService contentsService = new ContentsService();

			String jarFileSha = null;
			for( final RepositoryContents repositoryContents : contentsService.getContents(repository, "target" ) ) {

				if( repositoryContents.getName().startsWith( mavenArtifactId ) ) {

					if( ! repositoryContents.getName().endsWith( jarWithDependenciesSuffix + JAR_EXTENSION ) ) {
						jarFileSha = repositoryContents.getSha();
					}
					else {
						//TODO
					}
				}
			}

			if( null == jarFileSha ) {
				return;
			}

			final DataService dataService = new DataService();
			final ZipInputStream zis = new ZipInputStream( new ByteArrayInputStream( DatatypeConverter.parseBase64Binary( dataService.getBlob( repository, jarFileSha ).getContent() ) ) );

			ZipEntry entry = zis.getNextEntry();

			while( null != entry ) {

				if( entry.getName().matches( JAR_META_INT_ROOT + '/' + mavenGroupId + '/' + mavenArtifactId + '/' + JAR_POM_PROPERTIES_FILE_NAME ) ) {

					final Date lastBuildDate = extractBuildDate( zis );
					final Date currentBuildDate = extractBuildDate( VersionChecker.class.getClassLoader().getResourceAsStream( JAR_META_INT_ROOT + '/' + mavenGroupId + '/' + mavenArtifactId + '/' + JAR_POM_PROPERTIES_FILE_NAME  ) );

					if( lastBuildDate.after( currentBuildDate) ) {

						final int confirmDialogChoice = JOptionPane.showConfirmDialog( null, "An update is available, download it? (8-9MB)", "Rundeck Monitor update found!", JOptionPane.YES_NO_OPTION ); //$NON-NLS-1$ //$NON-NLS-2$
						if( JOptionPane.YES_OPTION == confirmDialogChoice ) {

							downloadFile( gitHubProjectRootUrl + GITHUB_MASTER_DIRECTORY + jarWithDependenciesFileName + GITHUB_FULL_FILE_GET_ARGUMENT, jarWithDependenciesFileName + UPDATE_EXTENSION + TMP_EXTENSION );
							Files.move( Paths.get( jarWithDependenciesFileName + UPDATE_EXTENSION + TMP_EXTENSION ), Paths.get( jarWithDependenciesFileName + UPDATE_EXTENSION ) );

							downloadDone = true;
						}
					}

					return;
				}

				entry = zis.getNextEntry();
			}
		}
		catch ( final Exception e) {

			//Ignore any error during update process
			//Just delete the temporary file
			cleanDownloadedJar();
		}
	}

	public boolean restart() {

		if( Files.exists( Paths.get( jarWithDependenciesFileName + UPDATE_EXTENSION ) ) ) {

			try {

				final ProcessBuilder processBuilder = new ProcessBuilder( getJavaExecutable(), JAR_ARGUMENT, jarWithDependenciesFileName + UPDATE_EXTENSION, UPDATE_MARKER_ARGUMENT );
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

	public void replaceJarAndRestart() throws InterruptedException {

		boolean replacementDone = false;

		//Try to replace 3 times the old jar with the updated jar
		for( int i=1 ; i<=3 ; i++ ) {

			try {
				Files.copy( Paths.get( jarWithDependenciesFileName + UPDATE_EXTENSION ), Paths.get( jarWithDependenciesFileName ), StandardCopyOption.REPLACE_EXISTING );
				replacementDone = true;
			}
			catch ( final IOException e) {

				//Ignore any error during replacing and retry after witing a little
				Thread.sleep( 1 );
			}
		}

		//If the old jar is now replaced
		if( replacementDone ) {

			try {
				//Restart again the process and exit
				final ProcessBuilder processBuilder = new ProcessBuilder( getJavaExecutable(), JAR_ARGUMENT, jarWithDependenciesFileName );
				processBuilder.start();

				System.exit( 0 );
			}
			catch ( final IOException e) {

				//Ignore any error during the restart process
			}
		}
	}

	public void cleanDownloadedJar() {

		deleteJar( Paths.get( jarWithDependenciesFileName + UPDATE_EXTENSION ) );
		deleteJar( Paths.get( jarWithDependenciesFileName + UPDATE_EXTENSION + TMP_EXTENSION ) );
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

	private static Date extractBuildDate( final InputStream inputStream ) throws IOException, ParseException {

		final BufferedReader reader = new BufferedReader( new InputStreamReader( inputStream, StandardCharsets.UTF_8 ) );

		//Ignore the first line: #Generated by Maven
		reader.readLine();

		//Read the date: #Thu Jun 26 00:02:43 CEST 2014
		final String date = reader.readLine().substring( 1 );
		return BUILD_DATE_FORMAT.parse( date );
	}

	/**
	 * Download a file/URL and write it to a destination file
	 *
	 * @param sourceFile source file/URL
	 * @param destinationFile destination file
	 * @throws IOException
	 */
	private static void downloadFile( final String sourceFile, final String destinationFile ) throws IOException {

		final URL url = new URL( sourceFile );
		Files.copy( url.openStream(), Paths.get( destinationFile ) );
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
