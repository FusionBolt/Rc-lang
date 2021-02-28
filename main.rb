require './parser'
require './log'
require './env'

root = Parser.parse File.open('source.sp').read

env = Env.new
root.to_ast.eval(env).main(env)
p env