require './parser/parser'
require './lib/env'
require_relative 'visitor'
require './analysis/call_graph'

module Rc
  class ASTInterpreter
    attr_reader :define_env
    def initialize(env = Env.new)
      @ast = nil
      @env = env
      @visitor = Interpret::Visitor.new(env)
    end

    def run(input)
      @ast = Parser.parse(input).to_ast
      @visitor.visit(@ast)
      @visitor.main
    end

    def call_graph
      graph = CallGraph.new(@env)
      graph.analysis(@env['main'])
    end
  end
end