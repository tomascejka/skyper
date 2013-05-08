package cz.derhaa.jenkins.messenger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import cz.derhaa.jenkins.messenger.build.BuildMonitor;
import cz.derhaa.jenkins.messenger.build.MessengerException;
import cz.derhaa.jenkins.messenger.resource.ResouceXml;
import cz.derhaa.jenkins.messenger.sender.SenderSkype;

/**
 * @author derhaa
 * 
 */
public class App { // NOPMD by tocecz on 8.5.13 7:35

	private static final String SEPARATOR = ",";

	public static void main(final String[] args) {
		//load program properties
		final Properties props = new Properties();
		FileInputStream fis = null;
		try {
			String filePath = args[0];
			fis = new FileInputStream(new File(filePath));
			props.load(fis);
		} catch (IOException e) {
			throw new MessengerException("Fail load properties", e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					throw new MessengerException(e);
				}
			}
		}

		// excluded jobs
		List<String> jobs = new ArrayList<String>();
		String excludeJobs = props.getProperty("exclude.jobs");
		if (excludeJobs != null && excludeJobs.indexOf(SEPARATOR) == -1) {
			jobs.add(excludeJobs);
		} else if (excludeJobs != null) {
			final String[] jbs = excludeJobs.split(SEPARATOR);
			for (int i = 0; i < jbs.length; i++) {
				jobs.add(jbs[i]);
			}
		}
		// contacts
		List<String> forMessage = new ArrayList<String>();
		String contacts = props.getProperty("contacts");
		if (contacts == null) {
			throw new MessengerException("Must define property 'users' in configuration file!");
		}
		if (contacts.indexOf(SEPARATOR) == -1) {
			forMessage.add(contacts);
		} else {
			final String[] usr = contacts.split(SEPARATOR);
			for (int i = 0; i < usr.length; i++) {
				forMessage.add(usr[i]);
			}
		}
		
		final String intr = props.getProperty("interval");
		final Integer interval = intr == null ? null : Integer.valueOf(intr);
		final String url = props.getProperty("jenkins.url");
		final BuildMonitor monitor = new BuildMonitor(jobs);
		monitor.setResource(new ResouceXml(url));
		monitor.setSender(new SenderSkype(forMessage, props));
		monitor.run(interval);
	}
}