package systemTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ DiffWorkerTest.class, HecateBackEndManagerTest.class })
public class AllTests {

}