require './Parser/parser'
require './env'
require_relative 'visitor'
require_relative '../call_graph'

module Rc
  class Interpreter
    attr_reader :env
    def initialize(env = Env.new)
      @ast = nil
      @env = env
      @visitor = Interpret::Visitor.new(env)
    end

    def run(input)
      @ast = Parser.parse(input).to_ast
      @visitor.visit(@ast)
    end

    def call_graph
      graph = CallGraph.new(@env)
      graph.analysis(@env['main'])
    end

    def main
      @visitor.main
    end
  end
end