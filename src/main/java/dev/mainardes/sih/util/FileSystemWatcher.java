package dev.mainardes.sih.util;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class FileSystemWatcher implements Runnable {

	private static ExecutorService EXECUTOR = Executors.newCachedThreadPool();
	
	private boolean isListening = false, isClosed = false;	
	private final WatchService service;
	
	private final Path path;
	
	@SafeVarargs
	public FileSystemWatcher(Path directory, Kind<Path> ... eventKinds) throws FileNotFoundException, IOException {
		Objects.requireNonNull(directory, "A path must be informed");
		
		if (Files.notExists(directory)) throw new FileNotFoundException();
		(this.path = directory).register(service = FileSystems.getDefault().newWatchService(), eventKinds);
	}
	
	public abstract void onCreation(Path context);
	
	public abstract void onDelete(Path context);
	
	public abstract void onModify(Path context);
	
	@Override
	public void run() {
		
		WatchKey key;
		while (isListening() && (key = awaitChange()) != null) {
			try {
				
				if (key.isValid()) {
					
					List<WatchEvent<?>> events = key.pollEvents();
					
					if (!events.isEmpty()) {
						
						for (WatchEvent<?> event : events) {
							Kind<?> kind = event.kind();
							
							Path context = path.resolve((Path)event.context());
							
							if (kind.equals(ENTRY_CREATE)) onCreation(context);
							else if (kind.equals(ENTRY_DELETE)) onDelete(context);
							else if (kind.equals(ENTRY_MODIFY)) onModify(context);
						}
						
					}
					
				}
				
				key.reset();
				
				if (isClosed()) {
					service.close();
					break;
				}
				
			} catch (ClosedWatchServiceException e) {
				isClosed = true;
				break;
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			
		}
		
	}
	
	public final synchronized void start() {
		if (!isListening) {
			isListening = true;
			EXECUTOR.submit(this);
		}
	}
	
	public final synchronized void stop() {
		isListening = false;
	}
	
	public synchronized void close() throws IOException {
		service.close();
	}
		
	public final synchronized boolean isListening() {
		return isListening;
	}
	
	public final synchronized boolean isClosed() {
		return isClosed;
	}
	
	public Path getPath() {
		return path;
	}
	
	private WatchKey awaitChange() {
		try {
			return service.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
