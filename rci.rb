#!/usr/bin/env ruby
require './interpreter/interpreter'

if ARGV.size == 0
  print("should set source file name")
end
$logger.level = :error
i = Rc::Interpreter.new
#i.run(source)
#i.run(File.open('demo.Rc').read)
# 'Demo/callstack.rc'
src = File.open(ARGV[0]).read
if src.nil?
  print "src is valid"
  return
end
i.run(src)
i.main