package org.apparatus_templi;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

import org.junit.Test;

public class MainTest {

	@Test
	public void testMainWithNoArguments() throws IOException {
		// test running the coordinator with no command line arguments. only the default options
		// should be used
		System.out.println("running Coordinator with no command line arguments");
		Process p = new ProcessBuilder("java", "-classpath",
				".:bin/:lib/*:lib/RXTX/macos/RXTXcomm.jar", "org.apparatus_templi.Coordinator",
				"--web_resourceFolder", "/").start();
		StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream());
		StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream());
		errorGobbler.start();
		outputGobbler.start();

		try {
			Thread.sleep(8000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("terminating service");
		// if running on a *nix platform we can get the process id within java
		if (p.getClass().getName().equals("java.lang.UNIXProcess")) {
			System.out.println("process running on unix/linux system, sending SIGINT");
			try {
				Field f = p.getClass().getDeclaredField("pid");
				f.setAccessible(true);
				int pid = f.getInt(p);
				// try to send the process a SIGINT, this should trigger the shutdown handler and
				// give a clean exit
				Process sigInt = new ProcessBuilder("kill", "-SIGINT", String.valueOf(pid)).start();
				// give a few seconds for the service to finish termination
				Thread.sleep(5000);

				long waitTime = System.currentTimeMillis() + (1000 * 10);
				boolean goodExit = false;
				while (System.currentTimeMillis() < waitTime) {
					try {
						int returnCode = p.exitValue();
						System.out.println("got exit code: " + returnCode);
						// a return code of 0 indicates that the service exited cleanly
						goodExit = (returnCode == 0);
						break;
					} catch (IllegalThreadStateException e) {
						// triggered when the process is not yet terminated
					}
				}
				// make sure the service exit before asserting
				if (!goodExit) {
					System.out.println("service did not shutdown from SIGINT, sending SIGKILL");
					Process sigKill = new ProcessBuilder("kill", "-SIGKILL", String.valueOf(pid))
							.start();
				}
				assert goodExit == true;
			} catch (Throwable e) {
			}
		} else {
			System.out.println("service running on non-unix/linux system, force killing");
			// on all other platforms we do a forceful process destroy
			p.destroy();
			assertTrue(p.exitValue() == 1);
		}
	}

	class StreamGobbler extends Thread {
		InputStream is;

		// reads everything from is until empty.
		StreamGobbler(InputStream is) {
			this.is = is;
		}

		@Override
		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null) {
					line = "\t--" + line;
					System.out.println(line);
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

}
