package rclang

import Interface.*

import compiler.CompileOption

@main def rcc = compiler.Driver(CompileOption("demo/hello_world.rc"))
