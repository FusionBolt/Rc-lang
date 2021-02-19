require './parser'

# p Parser.parse('(this "is" a test( 1 2.0 3))')
#p Parser.parse('while()if+5')
p Parser.parse File.open('source.sp').read

def node_eval
end