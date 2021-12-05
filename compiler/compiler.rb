require_relative '../ir/ssa'
require_relative '../analysis/call_graph'
require_relative '../analysis/global_env'
require './parser/parser'

module Rc
  class Compiler
    def parse(input)
      Parser.parse(input).to_ast
    end

    def compile(input)
      ast = parse(input)
      env = Analysis::GlobalEnvVisitor.new.analysis(ast)
    end
  end
end