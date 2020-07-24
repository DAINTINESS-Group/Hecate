package systemTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import systemTest.v_0_4.DiffWorkerTest;
import systemTest.v_0_4.HecateBackEndManagerTest_0_4_1;
import systemTest.v_0_5_1.HecateBackEndManagerTest_0_5_1;

@RunWith(Suite.class)
@SuiteClasses({ DiffWorkerTest.class, HecateBackEndManagerTest_0_4_1.class, HecateBackEndManagerTest_0_5_1.class})
public class AllTests {

}
