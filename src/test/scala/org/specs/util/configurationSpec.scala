package org.specs.util
import org.specs.io.mock._

class configurationSpec extends spex.Specification {
  "A configuration object" should {
    "try to find the default configuration class, named DefaultConfiguration, in the default package" in {
      Configuration.getDefaultConfiguration must haveClass[DefaultConfiguration]
    }
    "try to find first the user configuration class, named configuration$ (an object), in the default package" +
    "defaulting to the default configuration if not found" in {
      Configuration.getUserConfiguration must haveClass[DefaultConfiguration]
    }
    "try to find a configuration class, with a given name defaulting to the user configuration" +
    "then defaulting to the DefaultConfiguration if the user config is not found"  in {
      Configuration.getConfiguration("missing") must haveClass[DefaultConfiguration]
    }
    "try to find a configuration class, with a given name defaulting to the user configuration" in {
      Configuration.getConfiguration("org.specs.util.TestConfiguration") must haveClass[TestConfiguration]
    }
    "try to find a configuration properties file and load the properties from there" in {
      val props = """"
stacktrace = false
failedAndErrorsOnly = true
statistics = false
finalStatisticsOnly = true
colorize = true
examplesWithoutExpectationsMustBePending = false
"""
      configuration.addFile("configuration.properties", props)
      val c = configuration.getConfiguration("configuration.properties")
      (c.stacktrace, c.statistics, c.examplesWithoutExpectationsMustBePending).foreach(_ must beFalse)
      (c.failedAndErrorsOnly, c.finalStatisticsOnly, c.colorize).foreach(_ must beTrue)
    }
  }
  "A configuration" can {
   "translate boolean properties from a properties file" in {
     val properties = new java.util.Properties
     properties.put("stacktrace", "true")
     "a missing name returns the default value" >> {
       Configuration.boolean(properties, "missing", true) must beTrue
       Configuration.boolean(properties, "missing", false) must beFalse
     }
     "a existing name returns the file value" >> {
       Configuration.boolean(properties, "stacktrace", true) must beTrue
       Configuration.boolean(properties, "stacktrace", false) must beTrue
     }
     "a value of y, yes, Y, yes is taken as true" >> {
       List("y", "yes", "Y", "Yes").foreach {  (s: String) =>
         properties.put("stacktrace", s)
         Configuration.boolean(properties, "stacktrace", false) must beTrue
       }
     }
     "a value of n, no, N, No is taken as false" >> { 
       List("n", "no", "N", "No").foreach { (s: String) =>
         properties.put("stacktrace", s)
         Configuration.boolean(properties, "stacktrace", true) must beFalse
       }
     }
   }
  }
  val configuration = new Configuration with MockFileSystem
}
class TestConfiguration extends Configuration {
  override def finalStatisticsOnly = true
  override def failedAndErrorsOnly = true
}
