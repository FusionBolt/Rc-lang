#!/usr/bin/env ruby
require './Interpreter/interpreter'

if ARGV.size == 0
  print("should set source file name")
end
i = Rc::Interpreter.new
src = File.open(ARGV[0]).read
if src.nil?
  print "src is valid"
  return
end
i.run(src)
i.main