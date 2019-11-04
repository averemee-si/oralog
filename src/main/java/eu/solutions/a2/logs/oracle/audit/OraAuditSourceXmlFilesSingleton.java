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
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.solutions.a2.utils.ExceptionUtils;
import eu.solutions.a2.utils.fs.OpenFileGenericNio;
import eu.solutions.a2.utils.fs.OpenFileGenericX;
import eu.solutions.a2.utils.fs.OpenFileSystemV;
import eu.solutions.a2.utils.fs.OpenFilesIntf;

public class OraAuditSourceXmlFilesSingleton {

	private static final Logger LOGGER = LoggerFactory.getLogger(OraAuditSourceXmlFilesSingleton.class);
	private static OraAuditSourceXmlFilesSingleton instance;

	/** Locked files holder */
	private final ConcurrentLinkedQueue<OraAuditTraceNameHolder> lockedFiles;
	/** Ready to process files holder */
	private final ConcurrentLinkedQueue<OraAuditTraceNameHolder> readyFiles;
	/** Thread for FS watch */
	private AuditFileDestMonitorThread fileMonitor = null;
	/** Thread for detecting "ready" files */
	ProcessLockedFilesQueue queueProcessor = null;

	private OraAuditSourceXmlFilesSingleton() {
		lockedFiles = new ConcurrentLinkedQueue<>();
		readyFiles = new ConcurrentLinkedQueue<>();
	}

	public static OraAuditSourceXmlFilesSingleton getInstance() {
		if (instance == null) {
			instance = new OraAuditSourceXmlFilesSingleton();
		}
		return instance;
	}

	public void start(final int lockedQueueRefreshInterval, final Path watchedPathNio) {

		fileMonitor = new AuditFileDestMonitorThread(watchedPathNio, lockedFiles);
		LOGGER.info("Watching {} for Oracle RDBMS audit files", watchedPathNio.toString());
		fileMonitor.start();
		// Create background task for querying files queue
		queueProcessor = new ProcessLockedFilesQueue(lockedQueueRefreshInterval, lockedFiles, readyFiles);
		queueProcessor.start();
	}

	public void shutdown() {
		if (fileMonitor != null) {
			fileMonitor.shutdown();
		}
		if (queueProcessor != null) {
			queueProcessor.shutdown();
		}
	}

	public OraAuditTraceNameHolder getReadyToProcess() {
		return readyFiles.poll();
	}

	private static class AuditFileDestMonitorThread extends Thread {

		private final CountDownLatch shutdownLatch;
		private final Path watchedPathNio;
		private final String watchedDir;
		private final ConcurrentLinkedQueue<OraAuditTraceNameHolder> lockedFiles;

		AuditFileDestMonitorThread(final Path watchedPathNio,
				final ConcurrentLinkedQueue<OraAuditTraceNameHolder> lockedFiles) {
			this.watchedPathNio = watchedPathNio;
			this.lockedFiles = lockedFiles;
			this.shutdownLatch = new CountDownLatch(1);
			this.watchedDir = watchedPathNio.toAbsolutePath().toString() + File.separator;
		}

		@Override
		public void run() {
			final FileSystem fs = watchedPathNio.getFileSystem();
			try (final WatchService service = fs.newWatchService()) {
				initLockedFilesQueue();
				// Monitor only in new files
				watchedPathNio.register(service, StandardWatchEventKinds.ENTRY_CREATE);
				while (shutdownLatch.getCount() > 0) {
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
							initLockedFilesQueue();
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

		public void shutdown() {
			LOGGER.info("Shutting down AUDIT_FILE_DEST watcher service for {}", watchedDir);
			shutdownLatch.countDown();
		}

		private void initLockedFilesQueue() throws IOException {
			lockedFiles.clear();
			Files.walk(watchedPathNio)
				.filter(Files::isRegularFile)
				.filter(path -> path.toString().endsWith(".xml"))
				.forEach(path -> {lockedFiles.add(new OraAuditTraceNameHolder(path.toString()));});
		}
	}

    private static class ProcessLockedFilesQueue extends Thread {

    	private final int lockedQueueRefreshInterval;
		private final CountDownLatch shutdownLatch;
		private final ConcurrentLinkedQueue<OraAuditTraceNameHolder> lockedFiles;
		private final ConcurrentLinkedQueue<OraAuditTraceNameHolder> readyFiles;
		private final OpenFilesIntf fileLockChecker;

		ProcessLockedFilesQueue(
				final int lockedQueueRefreshInterval,
				final ConcurrentLinkedQueue<OraAuditTraceNameHolder> lockedFiles,
				final ConcurrentLinkedQueue<OraAuditTraceNameHolder> readyFiles) {
			this.lockedQueueRefreshInterval = lockedQueueRefreshInterval;
			this.shutdownLatch = new CountDownLatch(1);
			this.lockedFiles = lockedFiles;
			this.readyFiles = readyFiles;
			String osName = System.getProperty("os.name").toUpperCase();
			LOGGER.info("Running on " + osName);
			if ("AIX".equals(osName) || "LINUX".equals(osName) || "SOLARIS".equals(osName) || "SUNOS".equals(osName)) {
				this.fileLockChecker = new OpenFileSystemV();
			} else if ("WIN".contains(osName)) {
				this.fileLockChecker = new OpenFileGenericNio();
			} else {
				// Free BSD, HP-UX, Mac OS X
				this.fileLockChecker = new OpenFileGenericX();
			}
		}

		@Override
		public void run() {
			while (shutdownLatch.getCount() > 0) {
				try {
					boolean stopping = shutdownLatch.await(lockedQueueRefreshInterval, TimeUnit.MILLISECONDS);
					if (stopping) {
						return;
					}
				} catch (InterruptedException ie) {
					LOGGER.error(ExceptionUtils.getExceptionStackTrace(ie));
				}
				final Iterator<OraAuditTraceNameHolder> lockedFilesIterator = lockedFiles.iterator();
				while (lockedFilesIterator.hasNext()) {
					OraAuditTraceNameHolder trcFile = lockedFilesIterator.next();
					try {
						if (!fileLockChecker.isLocked(trcFile.getPid(), trcFile.getFileName())) {
							LOGGER.info("{} is not locked, processing it.", trcFile.getFileName());
							readyFiles.add(trcFile);
							lockedFilesIterator.remove();
						}
					} catch (IOException ioe) {
						LOGGER.error("Exception while processing {}", trcFile.getFileName());
						LOGGER.error(ExceptionUtils.getExceptionStackTrace(ioe));
					}
				}
			}
		}

		public void shutdown() {
			LOGGER.info("Shutting down file queue processor thread");
			shutdownLatch.countDown();
		}

    }

}
