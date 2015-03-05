package eu.earthobservatory.runtime.postgis;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({AggregateTests.class, GeneralTests.class, HavingTests.class, JoinTests.class,
    MeaningfulAggregateTests.class, SimpleTests.class, SPARQL11Tests.class, SpatialTests.class})
public class AllTests {

}
