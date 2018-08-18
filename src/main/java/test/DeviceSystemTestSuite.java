package main.java.test;

import org.junit.runners.Suite;
import org.junit.runner.RunWith;

/*NOTE: JUnit5 has broken test suites. This is an attempt at a hybrid JUnit4 Suite, but it is not working.
 * I have more work to do, once JUnit fixes Suites in JUnit5 
 * Reference: https://github.com/junit-team/junit5/issues/1334*/
@RunWith(Suite.class)
@Suite.SuiteClasses({NestSystemUnitTest.class, RingSystemUnitTest.class})
public class DeviceSystemTestSuite {
}