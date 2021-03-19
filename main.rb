require './interpreter/interpreter'
require './interpreter/repl'

# Rc::REPL.new.run

$logger.level = :error
i = Rc::Interpreter.new
i.run(File.open('demo.Rc').read)
i.main