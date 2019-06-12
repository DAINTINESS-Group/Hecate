package systemTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import systemTest.v_0_4.DiffWorkerTest;
import systemTest.v_0_4.HecateBackEndManagerTest;

@RunWith(Suite.class)
@SuiteClasses({ DiffWorkerTest.class, HecateBackEndManagerTest.class })
public class AllTests {

}
