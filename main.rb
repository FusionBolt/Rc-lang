require './interpreter/interpreter'

Interpreter.new(File.open('demo.rc').read).interpret