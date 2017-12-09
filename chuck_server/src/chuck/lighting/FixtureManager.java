package chuck.lighting;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

import chuck.defines.*;
import chuck.dmx.DMXDriver;

/**
 * Chuck Lighting Profile Manager Class Contains functions to keep track of
 * current profiles and to add profiles to set.
 * 
 * Manager class for keeping track of a set of fixtures (current setup).
 * 
 * @author Christian Krueger
 */
public class FixtureManager {

	/**
	 * path to directory containing set files
	 */
	private Path set_dir;

	/**
	 * Lighting profile set. Thread safe for updating.
	 */
	private CopyOnWriteArrayList<FixtureProfile> set;

	/**
	 * Constructor. Create an empty profile set.
	 * 
	 * @throws IOException
	 *             if unable to create default directories
	 */
	public FixtureManager() throws IOException {
		// get the path to our set directory
		set_dir = Paths.get(Filepaths.SET_DIR).normalize();
		// create the set directory if it doesn't exist
		Files.createDirectories(set_dir);
		set_dir = set_dir.toRealPath();
		// create the empty set
		set = new CopyOnWriteArrayList<FixtureProfile>();
	}

	/**
	 * Constructor. Try to open a file for creating this fixture set.
	 * 
	 * @param dmx
	 *            reference to dmx driver
	 * @param filename
	 *            name of set file that was created by this application (for
	 *            opening)
	 */
	public FixtureManager(DMXDriver dmx, String filename) throws IOException {
		// call basic constructor to instantiate members
		this();
		// don't allow sneaky filenames
		Path setFile = set_dir.resolve(filename).normalize();
		if (!setFile.getParent().equals(set_dir))
			throw new IllegalArgumentException("path traversal detected");

		// get an object stream for reading file
		ObjectInputStream ins = new ObjectInputStream(Files.newInputStream(setFile));
		// get the number of fixtures in the set
		int numFixtures = ins.readInt();
		// read and add each fixture to set
		FixtureProfile currFixture;
		for (int i = 0; i < numFixtures; i++) {
			try {
				// read the fixture object
				currFixture = (FixtureProfile) ins.readObject();
				currFixture.setDMXDriver(dmx);
				set.add(currFixture);
			} catch (ClassCastException | ClassNotFoundException ex) {
				// error reading fixtures from file
				throw new IOException("read non-fixture object from file (" + ex.getMessage() + ")");
			}
		}
		// sort the fixture set (by address)
		Collections.sort(set);
	}

	/**
	 * Add a fixture to this manager's set.
	 * 
	 * @param newFixture
	 *            new fixture to add (silently ignores null fixtures)
	 */
	public void addFixture(FixtureProfile newFixture) {
		if (newFixture == null)
			return;
		// add fixture to set
		set.add(newFixture);
		// sort the fixture set (by address)
		Collections.sort(set);
	}

	/**
	 * Remove the fixture at a specified index.
	 * 
	 * @param fixtureIndex
	 *            index of fixture to remove
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range (index < 0 || index >= size())
	 */
	public void removeFixture(int fixtureIndex) {
		// remove the light at that index
		set.remove(fixtureIndex);
		// no need to resort if just removing
	}

	/**
	 * Save this fixture manager's set to a file in the set directory.
	 * 
	 * @param filename
	 *            name of set file
	 * @throws IOException
	 *             if unable to perform the operation
	 */
	public void saveSetFile(String filename) throws IOException {
		if (!filename.endsWith(".set")) {
			// only save to .set files
			filename = filename + ".set";
		}
		// don't allow sneaky traversals
		Path setFile = set_dir.resolve(filename).normalize();
		if (!setFile.getParent().equals(set_dir)) {
			throw new IllegalArgumentException("path traversal detected");
		}

		// delete the old version
		Files.deleteIfExists(setFile);
		// create a new empty file
		Files.createFile(setFile);
		// get the output stream
		ObjectOutputStream stream = new ObjectOutputStream(Files.newOutputStream(setFile));
		// write the number of fixtures
		stream.writeInt(set.size());
		// write each fixture
		for (FixtureProfile f : set) {
			stream.writeObject(f);
		}

	}

	/**
	 * Get the number of fixtures held in this manager's set.
	 * 
	 * @return number of lights in current set
	 */
	public int getLightCount() {
		return set.size();
	}

	/**
	 * Get a reference to one of the lights in this set.
	 * 
	 * @param index
	 *            zero-based index into address-sorted list of fixtures
	 * @return fixture profile representing light at that index
	 */
	public FixtureProfile getLight(int index) {
		return set.get(index);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Current Profile Set:\n");
		for (FixtureProfile f : set) {
			sb.append(f.toString());
		}
		return sb.toString();
	}
}
