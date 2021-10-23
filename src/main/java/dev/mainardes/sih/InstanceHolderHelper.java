package dev.mainardes.sih;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

import dev.mainardes.sih.util.FileSystemWatcher;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.UUID;

public final class InstanceHolderHelper extends FileSystemWatcher {
	
	private final FileChannel channel;
	
	private InstanceHandler handler;
	
	private InstanceHolderHelper(Path directory, FileChannel channel) throws FileNotFoundException, IOException {
		super(directory, ENTRY_CREATE);
		this.channel = channel;
	}
	
	public final void setOnInstance(InstanceHandler handler) {
		this.handler = handler;
	}
	
	@Override
	public final void onCreation(Path context) {
		try {
			ObjectInputStream input = new ObjectInputStream(Files.newInputStream(context, StandardOpenOption.READ));
			Object obj = input.readObject();
			input.close();
			
			if (obj != null && obj instanceof String[]){
				String[] args = (String[])obj;
				if (args != null) handler.handle(args);
			}
			Files.delete(context);
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public final void onDelete(Path context) {}
	
	@Override
	public void onModify(Path context) {}
	
	@Override
	public synchronized void close() throws IOException {
		channel.close();
	}
	
	public static final InstanceHolderHelper createInstanceHandler(String[] args, String name, Class<?> mainClass) throws IOException {
		
		try {
			mainClass.getDeclaredMethod("main", String[].class);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
		
		final String instanceID = UUID.randomUUID().toString();
		Path instance = Files.createTempFile(instanceID, ".sih"),
			 instanceFolder = Files.createDirectories(instance.getParent().resolve(name)),
			 lock = instanceFolder.resolve(name + ".lock");
		
		if (!Files.exists(lock)) Files.write(lock, Collections.emptyList(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		
		FileChannel channel = FileChannel.open(lock, StandardOpenOption.WRITE);
		
		if (channel.tryLock() == null) {
			channel.close();
			
			ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(instance, StandardOpenOption.WRITE));
			output.writeObject(args);
			output.close();
			
			instance = Files.move(instance, instanceFolder.resolve(instanceID + ".sih"));
			
			return null;
		}
		
		Files.delete(instance);
		
		InstanceHolderHelper handler = new InstanceHolderHelper(instanceFolder, channel);
		handler.start();
		
		return handler;
		
	}
	
	public static final InstanceHolderHelper createInstanceHandler(String[] args, Class<?> mainClass) throws IOException {
		return createInstanceHandler(args, UUID.nameUUIDFromBytes(mainClass.getName().getBytes()).toString(), mainClass);
	}


}
