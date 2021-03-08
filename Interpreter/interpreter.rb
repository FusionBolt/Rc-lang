require './Parser/parser'
require './env'
require_relative 'visitor'

class Interpreter
  def initialize(input)
    @ast = Parser.parse(input).to_ast
    @visitor = Visitor.new
  end

  def interpret
    # env = Env.new
    # @ast.eval(env).main(env)
    # p env
    @visitor.visit(@ast)
    @visitor.main()
  end
  # TODO:repl
end