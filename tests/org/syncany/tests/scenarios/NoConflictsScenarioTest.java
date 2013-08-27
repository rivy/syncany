package org.syncany.tests.scenarios;

import static org.syncany.tests.util.TestAssertUtil.assertFileEquals;
import static org.syncany.tests.util.TestAssertUtil.assertFileListEquals;

import org.junit.Test;
import org.syncany.connection.plugins.Connection;
import org.syncany.tests.util.TestClient;
import org.syncany.tests.util.TestConfigUtil;

public class NoConflictsScenarioTest {
	@Test
	public void testSingleClientLocalBackupAndRestoreNoConflicts() throws Exception {
		// Setup
		Connection testConnection = TestConfigUtil.createTestLocalConnection();		
		TestClient clientA1 = new TestClient("A", testConnection);
		TestClient clientA2 = new TestClient("A", testConnection); // same client!

		// Create files and upload
		clientA1.createNewFiles();		
		clientA1.up();
		
		// Download and reconstruct
		clientA2.down();		
		assertFileListEquals(clientA1.getLocalFiles(), clientA2.getLocalFiles());
		
		// Cleanup
		clientA1.cleanup();
		clientA2.cleanup();
	}
	
	@Test
	public void testNoConflicts() throws Exception {
		// Setup
		Connection testConnection = TestConfigUtil.createTestLocalConnection();
		
		TestClient clientA = new TestClient("A", testConnection);
		TestClient clientB = new TestClient("B", testConnection);
		TestClient clientC = new TestClient("C", testConnection);
		
		// Test
		clientA.createNewFile("1");
		clientA.up();
		
		clientB.down();
		assertFileEquals(clientA.getLocalFile("1"), clientB.getLocalFile("1"));
		assertFileListEquals(clientA.getLocalFiles(), clientB.getLocalFiles());
		
		clientA.moveFile("1", "2");
		assertFileEquals(clientA.getLocalFile("2"), clientB.getLocalFile("1"));
		
		clientA.up();
		clientA.up();
		
		clientB.down();
		assertFileEquals(clientA.getLocalFile("2"), clientB.getLocalFile("2"));
		assertFileListEquals(clientA.getLocalFiles(), clientB.getLocalFiles());
		
		clientC.down();
		assertFileEquals(clientA.getLocalFile("2"), clientC.getLocalFile("2"));
		assertFileEquals(clientB.getLocalFile("2"), clientC.getLocalFile("2"));
		assertFileListEquals(clientA.getLocalFiles(), clientC.getLocalFiles());
		assertFileListEquals(clientB.getLocalFiles(), clientC.getLocalFiles());
		
		clientC.createNewFile("3");
		clientC.changeFile("2");
		clientC.up();
		
		clientA.down();
		assertFileEquals(clientC.getLocalFile("3"), clientA.getLocalFile("3"));
		assertFileListEquals(clientC.getLocalFiles(), clientA.getLocalFiles());
		
		clientB.down();
		assertFileEquals(clientC.getLocalFile("3"), clientB.getLocalFile("3"));
		assertFileListEquals(clientC.getLocalFiles(), clientB.getLocalFiles());
		
		clientC.down();
		
		assertFileListEquals(clientA.getLocalFiles(), clientB.getLocalFiles());
		assertFileListEquals(clientA.getLocalFiles(), clientC.getLocalFiles());
		
		// Tear down
		clientA.cleanup();
		clientB.cleanup();
		clientC.cleanup();
	}
}