require './parser'
require './log'
# p Parser.parse('(this "is" a test( 1 2.0 3))')
#p Parser.parse('while()if+5')

root = Parser.parse File.open('source.sp').read

env = {}
root.to_ast.eval(env)
p env