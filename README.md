# SingleInstanceHolder
 An application instance holder based on Java WatchService core API.

### Goals
- Make possible listening to the creation of new instances of the application.
- No using of *Sockets* or recurring to *native calls*.
- Pass the program arguments of newly created instances to the one which is already running.

### How it works
A helper class named `InstanceHolderHelper` is the one which determines if an instance is already running.

The method `createInstaceHandler` will look into a system temporary folder which name correspond an UID of the Main.class package location, and try to lock the `.lock` file. 

If it's not locked, it'll create an instance of `InstanceHolderHelper`,  which through `FileSystemWatcher` will listen to the creation of any file into that temporary folder, reading them through `ObjectInputStream` to get the content (which will be String array of arguments).

If it's locked, it'll create a file into the temporary folder and put into its content the arguments String array object through an `ObjectOuputStream`. The `FileSystemWatcher` of the already running instance will recognize the creation of this file and handle it there.

### Example
Into the main method, create an `InstanceHolderHelper` through `InstanceHolderHelper.createInstanceHandler` passing the class object and the arguments. If an instance is already running, it'll return **null**.

To known when an instance is created and get its arguments, just set an `InstanceHandler` through `InstanceHolderHelper.setOnInstance`.

    public class Main {
	
		public static void main(String[] args) throws IOException {
			
			InstanceHolderHelper instances = InstanceHolderHelper.createInstanceHandler(args, Main.class);
			
			if (instances == null) System.exit(0);

			JFrame frame = new JFrame();
			frame.setTitle("Test");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			frame.setSize(100, 100);
			frame.setLocationRelativeTo(null);
			frame.setResizable(false);
			
			instances.setOnInstance(a->{
				JOptionPane.showMessageDialog(frame, "Another instance started with arguments:\n" + Arrays.toString(args));
				
				frame.setVisible(true);
				frame.transferFocus();
			});
			
			frame.setVisible(true);
			
		}
	}

