package com.github.sbugat.rundeckmonitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JOptionPane;

public class VersionChecker implements Runnable{

	public static final String UPDATE_MARKER_ARGUMENT = "update"; //$NON-NLS-1$

	private static final String MAVEN_META_INF_DATE_COMMENT_FORMAT = "EEE MMM d HH:mm:ss zzz yyyy"; //$NON-NLS-1$
	private static final SimpleDateFormat BUILD_DATE_FORMAT = new SimpleDateFormat( MAVEN_META_INF_DATE_COMMENT_FORMAT, Locale.ENGLISH );

	private static final String GITHUB_MASTER_DIRECTORY = "/blob/master/target/"; //$NON-NLS-1$

	private static final String JAR_EXTENSION = ".jar"; //$NON-NLS-1$
	private static final String UPDATE_EXTENSION = ".update"; //$NON-NLS-1$
	private static final String TMP_EXTENSION = ".tmp"; //$NON-NLS-1$
	private static final String WINDOWS_EXE_EXTENSION = ".exe"; //$NON-NLS-1$
	private static final String JAVA_HOME_PROPERTY = "java.home"; //$NON-NLS-1$
	private static final String OS_NAME_PROPERTY = "os.name"; //$NON-NLS-1$
	private static final String WINDOWS_OS_NAME = "windows"; //$NON-NLS-1$

	private static final String BIN_DIRECTORY_AND_JAVA = "bin" + FileSystems.getDefault().getSeparator() + "java"; //$NON-NLS-1$

	private static final String JAR_ARGUMENT = "-jar"; //$NON-NLS-1$

	private final String gitHubProjectRootUrl;

	private final String jarFileName;

	private final String jarWithDependenciesFileName;

	private boolean downloadDone;

	/**
	 * Initialize the version checker with jar names and path to GitHub
	 *
	 * @param gitHubProjectRootUrlArg
	 * @param jarFileNameArg
	 * @param jarWithDependenciesSuffix
	 */
	public VersionChecker( final String gitHubProjectRootUrlArg, final String jarFileNameArg, final String jarWithDependenciesSuffix ) {

		gitHubProjectRootUrl = gitHubProjectRootUrlArg;
		jarFileName = jarFileNameArg;
		jarWithDependenciesFileName = jarFileName.replaceFirst( JAR_EXTENSION + '$', jarWithDependenciesSuffix + JAR_EXTENSION );
	}

	@Override
	public void run() {

		try( final InputStream remoteJarInputStream = new URL( gitHubProjectRootUrl + GITHUB_MASTER_DIRECTORY + jarFileName + "?raw=true" ).openStream() ) {

			final ZipInputStream zis = new ZipInputStream( remoteJarInputStream );

			ZipEntry entry = zis.getNextEntry();

			while( null != entry ) {

				System.out.println( entry.getName() );
				if( entry.getName().matches( "META-INF/maven/org.rundeck.monitor/rundeck-monitor/pom.properties" ) ) {

					final Date lastBuildDate = extractBuildDate( zis );

					final Date currentBuildDate = extractBuildDate( VersionChecker.class.getClassLoader().getResourceAsStream( "META-INF/maven/org.rundeck.monitor/rundeck-monitor/pom.properties" ) );

					if( lastBuildDate.after( currentBuildDate) ) {

						final int confirmDialogChoice = JOptionPane.showConfirmDialog( null, "An update is available, download it? (8-9MB)", "Rundeck Monitor update found!", JOptionPane.YES_NO_OPTION ); //$NON-NLS-1$ //$NON-NLS-2$

						if( JOptionPane.YES_OPTION == confirmDialogChoice ) {

							downloadFile( gitHubProjectRootUrl + GITHUB_MASTER_DIRECTORY + jarWithDependenciesFileName + "?raw=true", jarWithDependenciesFileName + UPDATE_EXTENSION + UPDATE_EXTENSION );

							Files.move( Paths.get( jarWithDependenciesFileName + UPDATE_EXTENSION + TMP_EXTENSION ), Paths.get( jarWithDependenciesFileName + UPDATE_EXTENSION ) );
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
			try {
				Files.delete( Paths.get( jarWithDependenciesFileName + UPDATE_EXTENSION + UPDATE_EXTENSION) );
			}
			catch( final IOException e1) {
				//Ignore any error
			}

			try {
				Files.delete( Paths.get( jarWithDependenciesFileName + UPDATE_EXTENSION ) );
			}
			catch( final IOException e1 ) {
				//Ignore any error
			}
		}
	}

	public boolean restart() {

		if( Files.exists( Paths.get( jarWithDependenciesFileName ) ) ) {

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

	public void replaceJarAndRestart() {

		try {
			Files.copy( Paths.get( jarWithDependenciesFileName + UPDATE_EXTENSION ), Paths.get( jarWithDependenciesFileName ), StandardCopyOption.REPLACE_EXISTING );

			//Restart again the process and exit
			final ProcessBuilder processBuilder = new ProcessBuilder( getJavaExecutable(), JAR_ARGUMENT, jarWithDependenciesFileName );
			processBuilder.start();

			System.exit( 0 );
		}
		catch ( final IOException e) {

			//Ignore any error during replacing and restart process
		}
	}

	public void cleanDownloadedJar() {

		if( Files.exists( Paths.get( jarWithDependenciesFileName + UPDATE_EXTENSION ) ) ) {

			try {
				Files.delete( Paths.get( jarWithDependenciesFileName + UPDATE_EXTENSION ) );
			}
			catch ( final IOException e ) {

				//Ignore any error during the cleanup
			}
		}

		if( Files.exists( Paths.get( jarWithDependenciesFileName + UPDATE_EXTENSION + TMP_EXTENSION ) ) ) {

			try {
				Files.delete( Paths.get( jarWithDependenciesFileName + UPDATE_EXTENSION + TMP_EXTENSION ) );
			}
			catch ( final IOException e ) {

				//Ignore any error during the cleanup
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

	private static void downloadFile( final String sourceFile, final String destFile ) throws IOException {

		final URL url = new URL( sourceFile );

		Files.copy( url.openStream(), Paths.get( destFile ) );
	}

	private static String getJavaExecutable() throws NoSuchFileException {

		final String javaDirectory = System.getProperty( JAVA_HOME_PROPERTY );

		if ( javaDirectory == null ) {
			throw new IllegalStateException( JAVA_HOME_PROPERTY );
		}

		String javaExecutablePath = javaDirectory + FileSystems.getDefault().getSeparator() + BIN_DIRECTORY_AND_JAVA;

		if ( isWindows() ) {
			javaExecutablePath = javaExecutablePath + WINDOWS_EXE_EXTENSION;
		}

		if ( ! Files.exists( Paths.get(javaExecutablePath ) ) ) {
			throw new NoSuchFileException( javaExecutablePath );
		}

		return javaExecutablePath;
	}

	private static boolean isWindows() {

		final String operatingSystem = System.getProperty( OS_NAME_PROPERTY );

		if( null == operatingSystem ) {
			return false;
		}

		return operatingSystem.toLowerCase().startsWith( WINDOWS_OS_NAME );
	}
}
