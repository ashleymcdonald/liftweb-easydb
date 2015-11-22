name := "EasyDb"

version := "0.0.1"

organization := "am.opensource"

scalaVersion := "2.11.2"

resolvers ++= Seq(
	"snapshots"     at "https://oss.sonatype.org/content/repositories/snapshots",
    "releases"        at "https://oss.sonatype.org/content/repositories/releases"
)

// scalacOptions ++= Seq("-deprecation", "-unchecked")
scalacOptions := Seq("-optimise","-unchecked","-deprecation","-Ydead-code","-Yinline-handlers","-Yinline-warnings","-Ybackend:GenBCode","-Ydelambdafy:method","-target:jvm-1.7","-explaintypes","-feature","-Xlint","-Dscalac.patmat.analysisBudget=512","-Ydebug" ,"-Ylog:refchecks") // some moreso evil flags to make sure code is clean

javacOptions ++= Seq("-Xlint:unchecked","-target","1.8") // target environment is java 8 or later only

libraryDependencies ++= {
  val liftVersion = "2.6.+"
  Seq(
    "com.zaxxer"        % "HikariCP"            % "1.+",        // Connection Pooling
    "net.liftweb"       %% "lift-util"          % liftVersion,
    "net.liftweb"       %% "lift-common"        % liftVersion,
    "net.liftweb"       %% "lift-mapper"        % liftVersion   // db orm
  )
}

