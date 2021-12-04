#!/usr/bin/env ruby
require './Interpreter/interpreter'

if ARGV.size == 0
  print("should set source file name")
end

src = File.open(ARGV[0]).read
if src.nil?
  print "src is valid"
  return
end
Rc::Interpreter.new.run(src)