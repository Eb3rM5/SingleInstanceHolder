# SingleInstanceHolder
 An application instance holder based on Java WatchService core API.

### Goals
- Make possible listening to the creation of new instances of the application.
- No using of *Sockets* or recurring to *native calls*.
- Pass the program arguments of newly created instances to the one which is already running.

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

