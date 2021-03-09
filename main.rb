require './interpreter/interpreter'
require './interpreter/repl'

# Rc::REPL.new.run
i = Rc::Interpreter.new
i.run(File.open('demo.rc').read)
i.main