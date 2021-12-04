#!/usr/bin/env ruby
require './Compiler/compiler'

if ARGV.size == 0
  print("should set source file name")
end

src = File.open(ARGV[0]).read
if src.nil?
  print "src is valid"
  return
end

Rc::Compiler.new.compile(src)