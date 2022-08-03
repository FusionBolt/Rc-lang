package rclang

import Interface.*

import compiler.CompileOption

@main def main = compiler.Driver(CompileOption("demo/simple.rc"))
