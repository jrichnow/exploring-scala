name := "exploring-scala"

version := "1.0"

scalaVersion := "2.11.7"

//logLevel := Level.Debug

import com.trueaccord.scalapb.{ScalaPbPlugin => PB}

libraryDependencies ++= Seq(
  "net.liftweb" %% "lift-json" % "2.6",
  "org.postgresql" % "postgresql" % "9.3-1100-jdbc41",
  "au.com.bytecode" % "opencsv" % "2.4",
  "com.trueaccord.scalapb" %% "scalapb-runtime" % "0.4.20" % PB.protobufConfig,
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.3")

PB.runProtoc in PB.protobufConfig := (args =>
  com.github.os72.protocjar.Protoc.runProtoc("-v300" +: args.toArray))

PB.protobufSettings
PB.flatPackage in PB.protobufConfig := true
