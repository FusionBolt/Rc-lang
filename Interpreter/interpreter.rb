require './Parser/parser'
require './env'
require_relative 'visitor'

module Rc
  class Interpreter
    def initialize(env = Env.new)
      @ast = nil
      @env = env
      @visitor = Visitor.new(env)
    end

    def run(input)
      @ast = Parser.parse(input).to_ast
      @visitor.visit(@ast)
    end

    def main
      @visitor.main
    end
  end
end