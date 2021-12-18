#!/usr/bin/env ruby
require './interpreter/ast_interpreter'

if ARGV.size == 0
  print("should set source file name")
  return
end

src = File.open(ARGV[0]).read
if src.nil?
  print "src is valid"
  return
end
Rc::ASTInterpreter.new.run(src)