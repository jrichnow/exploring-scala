name := "exploring-scala"

version := "1.0"

scalaVersion := "2.11.7"

//logLevel := Level.Debug

import com.trueaccord.scalapb.{ScalaPbPlugin => PB}

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

libraryDependencies ++= Seq(
  "net.liftweb" %% "lift-json" % "2.6",
  "org.postgresql" % "postgresql" % "9.3-1100-jdbc41",
//  "org.clojars.prepor" % "vertica-jdbc" % "7.0.1-0",
  "au.com.bytecode" % "opencsv" % "2.4",
  "com.trueaccord.scalapb" %% "scalapb-runtime" % "0.4.20" % PB.protobufConfig,
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.3",
  "com.gilt" %% "gfc-timeuuid" % "0.0.8",
  "com.jason-goodwin" %% "authentikat-jwt" % "0.4.1")

PB.runProtoc in PB.protobufConfig := (args =>
  com.github.os72.protocjar.Protoc.runProtoc("-v300" +: args.toArray))

PB.protobufSettings
PB.flatPackage in PB.protobufConfig := true
