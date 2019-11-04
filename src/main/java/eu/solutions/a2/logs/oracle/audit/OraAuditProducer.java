/**
 * Copyright (c) 2018-present, http://a2-solutions.eu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package eu.solutions.a2.logs.oracle.audit;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.solutions.a2.logs.BaseStandaloneProducer;
import eu.solutions.a2.logs.Constants;
import eu.solutions.a2.utils.ExceptionUtils;
import eu.solutions.a2.utils.fs.OpenFileGenericNio;
import eu.solutions.a2.utils.fs.OpenFileGenericX;
import eu.solutions.a2.utils.fs.OpenFileSystemV;
import eu.solutions.a2.utils.fs.OpenFilesIntf;


public class OraAuditProducer extends BaseStandaloneProducer  {

	private static final Logger LOGGER = LoggerFactory.getLogger(OraAuditProducer.class);

	private static OpenFilesIntf fileLockChecker = null;

	/** Number of threads for sending data */
	private static final String PARAM_WORKER_COUNT = "a2.worker.count";
	/** Default number of worker threads */
	private static final int WORKER_THREAD_COUNT = 4;
	/** Maximum number of worker threads */
	private static final int WORKER_THREAD_MAX = 150;
	/** Number of async workers for data transfer */
	private static int workerThreadCount = WORKER_THREAD_COUNT;
	/** Windows OS? **/
	private static boolean isWinOs = false;
	/** Locked files */
	private static ConcurrentLinkedQueue<OraAuditTraceNameHolder> lockedFiles = null;
	/**   Main thread pool for data tansfer jobs */
	private static ThreadPoolExecutor threadPool;

    public static void main( String[] argv) {
    	initLog4j(1);
		if (argv.length == 0) {
			printUsage(OraAuditProducer.class.getCanonicalName(), 2);
		}
		// Load producer properties and detect target system
		loadPropsAndParseCommonSettings(argv[0], 1);

		String watchedPath = props.getProperty(Constants.PARAM_A2_WATCHED_PATH).trim();
		if ("".equals(watchedPath) || watchedPath == null) {
			LOGGER.error("watched.path parameter must set in configuration file {}", argv[0]);
			LOGGER.error("Exiting.");
			System.exit(1);
		}
		// Sanity check - Check if path is a folder
		final Path watchedPathNio = Paths.get(watchedPath);
		try {
			Boolean isFolder = (Boolean) Files.getAttribute(watchedPathNio, "basic:isDirectory", LinkOption.NOFOLLOW_LINKS);
			if (!isFolder) {
				LOGGER.error("Path specified by {} parameter [{}] is not a folder", Constants.PARAM_A2_WATCHED_PATH, watchedPathNio);
				LOGGER.error("Exiting.");
				System.exit(1);
			}
		} catch (IOException ioe) {
			// Folder does not exists
			LOGGER.error(ExceptionUtils.getExceptionStackTrace(ioe));
			LOGGER.error("Path specified by {} parameter [{}] not exist!", Constants.PARAM_A2_WATCHED_PATH, watchedPathNio);
			LOGGER.error("Exiting.");
			System.exit(1);
		}
		final String watchedDir = watchedPathNio.toAbsolutePath().toString() + File.separator;

		String threadCountString = props.getProperty(PARAM_WORKER_COUNT).trim();
		if (threadCountString != null && !"".equals(threadCountString)) {
			try {
				workerThreadCount = Integer.parseInt(threadCountString);
			} catch (Exception e) {
				LOGGER.error("{} set to wrong value in configuration file {}", PARAM_WORKER_COUNT, argv[0]);
				LOGGER.error("Exiting.");
				System.exit(1);
			}
			if (workerThreadCount > WORKER_THREAD_MAX) {
				LOGGER.warn("{} is greater than allowed. Setting it to {}", PARAM_WORKER_COUNT, WORKER_THREAD_MAX);
				workerThreadCount = WORKER_THREAD_MAX;
			} else if (workerThreadCount < 0) {
				LOGGER.warn("{} is negative. Setting it to {}", PARAM_WORKER_COUNT, WORKER_THREAD_COUNT);
				workerThreadCount = WORKER_THREAD_COUNT;
			}
		}

		int lockedQueueRefreshInterval = Constants.PARAM_A2_LOCKED_QRY_RFRSH_INTERVAL_DEFAULT;
		String lockedQueueRefreshString = props.getProperty(Constants.PARAM_A2_LOCKED_QRY_RFRSH_INTERVAL);
		if (lockedQueueRefreshString != null && !"".equals(lockedQueueRefreshString)) {
			try {
				lockedQueueRefreshInterval = Integer.parseInt(lockedQueueRefreshString);
			} catch (Exception e) {
				LOGGER.warn("Incorrect value for {} -> ", Constants.PARAM_A2_LOCKED_QRY_RFRSH_INTERVAL, lockedQueueRefreshString);
				LOGGER.warn("Setting it to {}", Constants.PARAM_A2_LOCKED_QRY_RFRSH_INTERVAL_DEFAULT);
			}
		}

		// Instantiate correct file lock checker
		//TODO
		//TODO - more OS'es and precision here!!!
		//TODO
		String osName = System.getProperty("os.name").toUpperCase();
		LOGGER.info("Running on " + osName);
		if ("AIX".equals(osName) || "LINUX".equals(osName) || "SOLARIS".equals(osName) || "SUNOS".equals(osName)) {
			fileLockChecker = new OpenFileSystemV();
		} else if ("WIN".contains(osName)) {
			//TODO
			//TODO Need more precise handling and testing for Windows
			//TODO
			fileLockChecker = new OpenFileGenericNio();
			isWinOs = true;
		} else {
			// Free BSD, HP-UX, Mac OS X
			fileLockChecker = new OpenFileGenericX();
		}

		// Add special shutdown thread
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				LOGGER.info("Shutting down...");
				sendMethod.shutdown();
				//TODO - more information about processing
			}
		});

		// Initialize main thread pool
		BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<Runnable>(4096 * workerThreadCount);
		threadPool = new ThreadPoolExecutor(
				workerThreadCount,	// core pool size
				workerThreadCount,	// maximum pool size
				15,	// If the pool currently has more than corePoolSize threads, excess threads will be terminated if they have been idle for more than the keepAliveTime
				TimeUnit.SECONDS,	// Time unit for keep-alive
				blockingQueue	// Job queue
				);
		// Throw RejectedExecutionException with full queue
		threadPool.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());

		// Before listening to new files populate files queue with files already on filesystem
		try {
			initLockedFilesQueue(watchedPathNio);
		} catch (IOException ioe) {
			LOGGER.error(ExceptionUtils.getExceptionStackTrace(ioe));
			LOGGER.error("unable to read directory {} !", watchedPathNio);
			LOGGER.error("Exiting.");
			System.exit(1);
		}
		if (lockedFiles == null) {
			lockedFiles = new ConcurrentLinkedQueue<>();
		} else {
			LOGGER.info("Total of {} unprocessed file added to queue.", lockedFiles.size());
		}
		// Create background task for querying files queue
		ProcessLockedFilesMap mapProcessor = new ProcessLockedFilesMap();
		ScheduledExecutorService lockedFileExecutor = Executors.newSingleThreadScheduledExecutor(
				new ThreadFactory() {
					public Thread newThread(Runnable r) {
						Thread t = Executors.defaultThreadFactory().newThread(r);
						t.setDaemon(true);
						return t;
					}
				});
		// TODO scheduleAtFixedRate vs scheduleWithFixedDelay
		int lockedFilesJobDelay = 0;
		if (lockedFiles.size() == 0) {
			lockedFilesJobDelay = lockedQueueRefreshInterval;
		}
		lockedFileExecutor.scheduleWithFixedDelay(mapProcessor, lockedFilesJobDelay, lockedQueueRefreshInterval, TimeUnit.MILLISECONDS);

		// Start watching for new audit files
		LOGGER.info("Watching {} for Oracle RDBMS audit files", watchedPathNio.toString());
		final FileSystem fs = watchedPathNio.getFileSystem();
		try (final WatchService service = fs.newWatchService()) {
			// Only in new files
			watchedPathNio.register(service, StandardWatchEventKinds.ENTRY_CREATE);
			//TODO - https://www.programcreek.com/java-api-examples/?class=java.nio.file.WatchEvent&method=Modifier

			// Start the infinite polling loop
			while (true) {
				final WatchKey key = service.take();
				for (WatchEvent<?> watchEvent : key.pollEvents()) {
					final Kind<?> kind = watchEvent.kind();
					if (StandardWatchEventKinds.ENTRY_CREATE == kind) {
						@SuppressWarnings("unchecked")
						final Path newPath = ((WatchEvent<Path>) watchEvent).context();
						final StringBuilder sb = new StringBuilder(255);
						sb.append(watchedDir);
						sb.append(newPath.getFileName());
						final String fileName = sb.toString();
						LOGGER.info("New audit file created: {}", fileName);
						if (fileName.endsWith(".xml")) {
							lockedFiles.add(new OraAuditTraceNameHolder(fileName));
							LOGGER.info("{} added to locked files queue.", fileName);
							LOGGER.info("Total of opened audit files in queue = {}", lockedFiles.size());
						} else {
							LOGGER.error("Non XML file {} in audit file destination!", fileName);
						}
					} else if (StandardWatchEventKinds.OVERFLOW == kind) {
						LOGGER.error("File listener recieved an overflow event!");
						initLockedFilesQueue(watchedPathNio);
						continue;
					}
				}				

				if (!key.reset()) {
					break; // loop
                }
			}
		} catch (IOException | InterruptedException ioe) {
			LOGGER.error(ExceptionUtils.getExceptionStackTrace(ioe));
		}
    }

	private static void initLockedFilesQueue(final Path watchedPathNio) throws IOException {
		if (lockedFiles != null) {
			lockedFiles.clear();
		}
		lockedFiles = Files
				.list(watchedPathNio)
				.filter(Files::isRegularFile)
				.filter(dirPath -> dirPath.toString().endsWith(".xml"))
//				.map(Path::toString)
				.map(path -> {return new OraAuditTraceNameHolder(path.toString());})
				.collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
	}


    private static class ProcessLockedFilesMap implements Runnable {
		@Override
		public void run() {
			Iterator<OraAuditTraceNameHolder> lockedFilesIterator = lockedFiles.iterator();
			while (lockedFilesIterator.hasNext()) {
				OraAuditTraceNameHolder trcFile = lockedFilesIterator.next();
				try {
					if (!fileLockChecker.isLocked(trcFile.getPid(), trcFile.getFileName())) {
						LOGGER.info("{} is not locked, processing it.", trcFile.getFileName());
						try {
							threadPool.submit(sendMethod.oraAudJob(trcFile, lockedFiles));
						} catch (RejectedExecutionException ree) {
							LOGGER.error("Can't submit job to send audit information!");
							LOGGER.error(ExceptionUtils.getExceptionStackTrace(ree));
						}
						lockedFilesIterator.remove();
					}
				} catch (IOException e) {
					LOGGER.error("Exception while processing {}", trcFile.getFileName());
					LOGGER.error(ExceptionUtils.getExceptionStackTrace(e));
				}
			}
		}
    }

}
